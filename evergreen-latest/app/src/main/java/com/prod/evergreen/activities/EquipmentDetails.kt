package com.prod.evergreen.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.equipment.EquipmentDetailsData
import com.example.app.ui.equipment.EquipmentDetailsScreen
import com.example.app.ui.equipment.EquipmentHistoryItem
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.dialogs.MoreEqInfoFragment
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.DateConverter
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.YearPickerHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.CompanyDataResponse
import com.prod.evergreen.models.ResponseData
import com.prod.evergreen.models.TasksItem

class EquipmentDetails : AppCompatActivity() {

    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    var companyData: CompanyDataResponse? = null
    var accessType: String? = null
    var userid: Int? = null
    var equipmentData = ResponseData()
    private val detailsState = mutableStateOf(EquipmentDetailsData())
    private val showEdit = mutableStateOf(false)
    private val showCreateTask = mutableStateOf(false)
    private val loadError = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setViewmodel()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        userid = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        val equipment = intent.getIntExtra("eq_id", 0)
        val eq_sn = intent.getStringExtra("eq_sn")
        val screentype = intent.getIntExtra("screentype", 0)
        showEdit.value = RoleAccess.canManageEquipment(accessType)
        showCreateTask.value = accessType != "technician"

        setContent {
            val details by detailsState
            val error by loadError
            val editEnabled by showEdit
            val createTaskEnabled by showCreateTask
            EquipmentDetailsScreen(
                equipment = details,
                errorMessage = error,
                showEdit = editEnabled,
                showCreateTask = createTaskEnabled,
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onEditClick = {
                    val data = Gson().toJson(equipmentData)
                    startActivity(Intent(this, AddEquipment::class.java).putExtra("equipment_data", data))
                },
                onCreateTaskClick = {
                    val data = Gson().toJson(equipmentData)
                    com.prod.evergreen.helper.DashboardNav.pendingEquipmentJson = data
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            .putExtra("open_create_task", true)
                            .putExtra("equipment_data", data)
                    )
                },
                onDownloadQrClick = {
                    val currentId = equipmentData.id ?: equipment
                    val currentSn = equipmentData.egserialnumber ?: eq_sn
                    startActivity(
                        Intent(this, IndividualQRDownloader::class.java)
                            .putExtra("eq_id", currentId)
                            .putExtra("id", currentId)
                            .putExtra("eq_sn", currentSn)
                    )
                },
                onHistoryClick = { item ->
                    equipmentData.tasks?.firstOrNull { "${it.taskLink ?: it.task?.id}" == item.id }?.let { task ->
                        MoreEqInfoFragment.newInstance(Gson().toJson(task), Gson().toJson(companyData))
                            .show(supportFragmentManager, "")
                    }
                },
                onSelfAssignClick = { item ->
                    equipmentData.tasks?.firstOrNull { "${it.taskLink ?: it.task?.id}" == item.id }?.let { task ->
                        val body = JsonObject()
                        body.addProperty("task_link", task.taskLink)
                        body.addProperty("technician_link", userid)
                        viewModel.assignTechnician(body, token!!)
                    }
                }
            )
        }

        viewModel.loading.observe(this) { data ->
            if (data && detailsState.value.name.isBlank()) {
                ProgressDialogUtil.showProgressDialog(this, "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.downloadpdf.observe(this) { response ->
            if (response.status_code == 200 && !response.url.isNullOrBlank()) {
                val pdfUrl = com.prod.evergreen.helper.MediaUrl.resolve(response.url)
                androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Service Report")
                    .setMessage("The report is ready. You can open or share the PDF.")
                    .setPositiveButton("Open") { _, _ -> openPdfInBrowser(pdfUrl) }
                    .setNeutralButton("Share") { _, _ ->
                        val share = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Evergreen Service Report")
                            putExtra(Intent.EXTRA_TEXT, pdfUrl)
                        }
                        startActivity(Intent.createChooser(share, "Share service report"))
                    }
                    .setNegativeButton("Close", null)
                    .show()
            } else {
                showDialog(response.message ?: "Unable to download report")
            }
        }
        viewModel.assignTechnicianDataResponse.observe(this) { response ->
            if (response.status_code == 200) {
                showDialog(response.message!!)
                loadEquipment()
            } else {
                showDialog(response.message!!)
            }
        }
        viewModel.equipmentDataResponse.observe(this) { data ->
            val payload = data.data
            if (payload != null) {
                equipmentData = payload
                companyData = payload.company
                loadError.value = null
                detailsState.value = payload.toDetails(screentype)
            } else {
                loadError.value = data.message ?: "Equipment details were not found."
            }
        }
        viewModel.errorMessage.observe(this) { message ->
            loadError.value = message
        }
        loadEquipment()
    }

    private var detailsReady = false

    override fun onResume() {
        super.onResume()
        if (!detailsReady) {
            detailsReady = true
            return
        }
        if (::viewModel.isInitialized) {
            loadEquipment()
        }
    }

    private fun ResponseData.toDetails(screentype: Int): EquipmentDetailsData {
        return EquipmentDetailsData(
            name = name.orEmpty(),
            companyName = company?.name.orEmpty(),
            make = make.orEmpty(),
            model = model.orEmpty(),
            serialNumber = serialNumber.orEmpty(),
            egSerial = egserialnumber.orEmpty(),
            year = YearPickerHelper.displayYear(manufacturerDate),
            location = location.orEmpty(),
            frequency = tmFrequency.orEmpty(),
            description = description.orEmpty(),
            imageUrl = imageUrl,
            history = tasks.orEmpty().map { it.toHistory(screentype) }
        )
    }

    private fun TasksItem.toHistory(screentype: Int): EquipmentHistoryItem {
        val assigned = technicianLink != null
        val canSelf = screentype == 1 && accessType == "technician" && (
            !assigned || (technicianLink != userid && (status == "open" || status == "hold"))
        )
        return EquipmentHistoryItem(
            id = "${taskLink ?: task?.id}",
            date = DateConverter.convertToLocalUtcAndFormat(createdAt),
            technician = technician?.name.orEmpty(),
            title = task?.name.orEmpty(),
            status = status.orEmpty(),
            canSelfAssign = canSelf
        )
    }

    private fun loadEquipment() {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        val equipment = intent.getIntExtra("eq_id", 0)
        val serial = intent.getStringExtra("eq_sn")?.trim().orEmpty()
        val body = JsonObject()
        when {
            equipment > 0 -> body.addProperty("equipment_link", equipment)
            serial.isNotBlank() -> body.addProperty("equipment_link", serial)
            else -> {
                loadError.value = "This QR code is not a valid equipment code"
                return
            }
        }
        viewModel.GetEquipmentInfo(body, token)
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }

    fun showDialog(message: String) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(android.R.string.ok))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                }
            }, true)
    }

    private fun openPdfInBrowser(pdfUrl: String) {
        startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(pdfUrl) })
    }
}
