package com.prod.evergreen.activities

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.ui.equipment.AddEquipmentFormState
import com.example.app.ui.equipment.AddEquipmentScreen
import com.example.app.ui.equipment.PM_FREQUENCY_OPTIONS
import com.example.app.ui.equipment.pmFrequencyLabel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.YearPickerHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.ResponseData
import com.prod.evergreen.models.activeCompanies
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddEquipment : AppCompatActivity() {
    var backendIssueValue: String? = null
    private lateinit var photoFile: File
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val galleryRequestCode = 102
    lateinit var EqData: ResponseData
    private var compressedImage: File? = null
    private lateinit var currentPhotoPath: String
    private var actualImage: File? = null
    private var file_name: String? = ""
    private var token: String? = ""
    private var company_link: Int? = null
    private var isSubmitting = false
    private var isUpdate = false
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val formState = mutableStateOf(AddEquipmentFormState())

    val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    setImage(uri)
                }
            }
        }

    private fun setImage(uri: Uri) {
        try {
            actualImage = FileUtil.from(this, uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        actualImage?.let { compressImage(it) }
    }

    private fun compressImage(file: File) {
        lifecycleScope.launch {
            val compressed = Compressor.compress(this@AddEquipment, file)
            setCompressedImage(compressed)
        }
    }

    private fun setCompressedImage(compressedImage: File) {
        this.compressedImage = compressedImage
        formState.value = formState.value.copy(photoPreviewPath = compressedImage.absolutePath)
        val auth = SharedPreferencesHelper(this).getValueString(ConstantValues.AuthToken)!!
        val fileReqBody = compressedImage.asRequestBody("image/png".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", compressedImage.name, fileReqBody)
        viewModel.upLoadImage(part, auth, "equipment_info")
    }

    fun showDialog(message: String, goBackOnOk: Boolean = false) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (goBackOnOk) {
                        finish()
                    } else {
                        isSubmitting = false
                    }
                }
            }, true)
    }

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCameraAndRequestIfNeeded() {
        if (CameraCaptureHelper.hasCameraPermission(this)) {
            openCamera()
        } else {
            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        photoFile = createImageFile()
        takeImageResult.launch(CameraCaptureHelper.createCaptureIntent(this, photoFile))
    }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && currentPhotoPath.isNotEmpty()) {
                compressImage(File(currentPhotoPath))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        setViewmodel()
        applyIntentState()

        setContent {
            val state by formState
            AddEquipmentScreen(
                state = state,
                onStateChange = { formState.value = it },
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onSaveEquipmentClick = { submitEquipment() },
                onCancelClick = { onBackPressedDispatcher.onBackPressed() },
                onPhotoClick = { showImageSourceDialog() },
                onClearPhotoClick = { clearPhoto() },
                onCompanyClick = {
                    showBottomSheetDialog { selectedItem ->
                        formState.value = formState.value.copy(companyName = selectedItem.name.orEmpty())
                        company_link = selectedItem.id
                    }
                },
                onYearClick = {
                    YearPickerHelper.show(
                        this,
                        YearPickerHelper.yearFromStoredDate(formState.value.manufacturerYear)
                    ) { year ->
                        formState.value = formState.value.copy(manufacturerYear = year.toString())
                    }
                },
                onPmFrequencyClick = { showFrequencyDialog() },
                onHomeClick = {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    finish()
                },
                onMessagesClick = {
                    startActivity(Intent(this, NotificationList::class.java))
                },
                onTasksClick = {
                    startActivity(
                        Intent(this, MainActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    )
                    finish()
                },
                onProfileClick = {
                    startActivity(Intent(this, UserDetails::class.java))
                },
                onMenuClick = { finish() },
                onNotificationClick = {
                    startActivity(Intent(this, NotificationList::class.java))
                }
            )
        }

        viewModel.getAllAmc(token!!)
        viewModel.errorMessage.observe(this) { data ->
            showDialog(data, goBackOnOk = false)
        }
        viewModel.changePasswordDataResponse.observe(this) { data ->
            val success = data.status_code == 200
            showDialog(data.message!!, goBackOnOk = success)
        }
        viewModel.loading.observe(this) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(this, "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.imageUploadDataResponse.observe(this) { data ->
            if (data.status_code == 200) {
                file_name = data.image_url
            } else {
                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun applyIntentState() {
        var next = AddEquipmentFormState()
        val tempdata = intent.getStringExtra("equipment_data")
        if (tempdata != null) {
            isUpdate = true
            EqData = Gson().fromJson(tempdata, ResponseData::class.java)
            backendIssueValue = EqData.tmFrequency
            file_name = EqData.imageUrl
            company_link = EqData.company?.id
                ?: EqData.companyLink
                ?: intent.getIntExtra("companylink", 0).takeIf { it != 0 }
            next = next.copy(
                title = "Update Equipment Details",
                subtitle = "Edit Equipment",
                saveLabel = "Update Equipment",
                equipmentName = EqData.name.orEmpty(),
                make = EqData.make.orEmpty(),
                model = EqData.model.orEmpty(),
                serialNumber = EqData.specifications?.takeIf { it.isNotBlank() }
                    ?: EqData.serialNumber.orEmpty(),
                manufacturerYear = YearPickerHelper.displayYear(EqData.manufacturerDate),
                location = EqData.location.orEmpty(),
                pmFrequency = pmFrequencyLabel(EqData.tmFrequency),
                description = EqData.description.orEmpty(),
                companyName = EqData.company?.name ?: intent.getStringExtra("companyname").orEmpty(),
                photoRemoteUrl = EqData.imageUrl
            )
        } else {
            val name = intent.getStringExtra("companyname")
            val id = intent.getIntExtra("companylink", 0)
            company_link = id
            next = next.copy(companyName = name.orEmpty())
        }

        val hideCompany = intent.getBooleanExtra("hide_company", false)
        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType)) {
            val attachedId = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
            val attachedName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
            if (attachedId != 0) {
                company_link = attachedId
                next = next.copy(
                    companyName = attachedName.orEmpty(),
                    hideCompany = true,
                    companyLocked = true
                )
            }
        } else if (hideCompany) {
            next = next.copy(hideCompany = true)
        }
        formState.value = next
    }

    private fun submitEquipment() {
        if (isSubmitting) return
        if (!validateFields()) return
        isSubmitting = true
        val state = formState.value
        if (!isUpdate) {
            val equipment = Equipment(
                company_link = company_link!!,
                name = state.equipmentName,
                make = state.make,
                model = state.model,
                image_url = file_name,
                serial_number = state.serialNumber,
                manufacturer_date = YearPickerHelper.apiDateFromYear(state.manufacturerYear),
                location = state.location,
                description = state.description,
                tm_frequency = backendIssueValue!!
            )
            viewModel.addEquipments(equipment, token!!)
        } else {
            val equipment = EquipmentUpdate(
                equipment_link = EqData.id!!,
                name = state.equipmentName,
                make = state.make,
                model = state.model,
                image_url = file_name,
                serial_number = state.serialNumber,
                specifications = state.serialNumber,
                manufacturer_date = YearPickerHelper.apiDateFromYear(state.manufacturerYear),
                location = state.location,
                description = state.description,
                tm_frequency = backendIssueValue!!
            )
            viewModel.updateEquipment(equipment, token!!)
        }
    }

    private fun clearPhoto() {
        file_name = ""
        compressedImage = null
        formState.value = formState.value.copy(photoPreviewPath = null, photoRemoteUrl = null)
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    data class Equipment(
        val company_link: Int,
        val name: String,
        val make: String,
        val model: String,
        val image_url: String? = null,
        val serial_number: String,
        val manufacturer_date: String,
        val location: String,
        val description: String,
        val tm_frequency: String
    )

    data class EquipmentUpdate(
        val equipment_link: Int,
        val name: String,
        val make: String,
        val model: String,
        val image_url: String? = null,
        val serial_number: String? = null,
        val specifications: String,
        val manufacturer_date: String,
        val location: String,
        val description: String,
        val tm_frequency: String
    )

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Choose One")
            .setItems(arrayOf("Camera", "Gallery", "Cancel")) { _, which ->
                when (which) {
                    0 -> openCameraAndRequestIfNeeded()
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pickImageFromGalleryForResult.launch(
                                Intent(MediaStore.ACTION_PICK_IMAGES).apply { type = "image/*" }
                            )
                        } else if (checkPermission(galleryPermission)) {
                            openGallery()
                        } else {
                            requestGaleryPermission()
                        }
                    }
                }
            }
            .show()
    }

    private fun requestGaleryPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(galleryPermission), galleryRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == galleryRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        pickImageFromGalleryForResult.launch(pickIntent)
    }

    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        viewModel.allAmcDataResponse.observe(this) { data ->
            val adapter = UserCompaniesAdapter(data.data.activeCompanies()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showFrequencyDialog() {
        val options = PM_FREQUENCY_OPTIONS.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Select an Option")
            .setItems(options) { dialog, which ->
                val selectedDisplayName = options[which]
                backendIssueValue = PM_FREQUENCY_OPTIONS[selectedDisplayName]
                formState.value = formState.value.copy(pmFrequency = selectedDisplayName)
                dialog.dismiss()
            }
            .show()
    }

    private fun validateFields(): Boolean {
        val state = formState.value
        val message = when {
            state.equipmentName.isBlank() -> "Please enter name"
            state.make.isBlank() -> "Please enter make"
            state.model.isBlank() -> "Please enter model number"
            state.serialNumber.isBlank() -> "Please enter serial number"
            state.manufacturerYear.isBlank() -> "Please enter manufacturing year"
            state.location.isBlank() -> "Please enter location"
            backendIssueValue.isNullOrEmpty() -> "Please select frequency"
            file_name.isNullOrBlank() -> "Please select an image"
            company_link == null || company_link == 0 -> "Please select company"
            else -> null
        }
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("$timeStamp", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
