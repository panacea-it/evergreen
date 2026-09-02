package com.prod.evergreen.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.ui.users.AddUserFormState
import com.example.app.ui.users.AddUserScreen
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.ItemAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.Validator
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.Users
import com.prod.evergreen.models.activeCompanies

class AddUser : AppCompatActivity() {
    private var companyLinksArray: JsonArray = JsonArray()
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var amc_id: String? = null
    private var selected_accessleve: String? = null
    private var isSubmitting = false
    private var editUserId: Int? = null
    private val formState = mutableStateOf(AddUserFormState())
    private var roleLabels: List<String> = emptyList()
    private var roleKeys: List<String> = emptyList()
    private var creatorAccessType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        setViewmodel()

        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        creatorAccessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        viewModel.getAllAmc(token!!)
        prepareRoles(creatorAccessType)
        applyEditState()
        applyCompanyPickerForRole(selected_accessleve, creatorAccessType)

        setContent {
            val state by formState
            AddUserScreen(
                state = state,
                onStateChange = { formState.value = it },
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onSaveUserClick = { submitUser() },
                onCancelClick = { onBackPressedDispatcher.onBackPressed() },
                onAccessTypeClick = { showAccessTypePicker() },
                onAmcClick = {
                    if (!formState.value.amcLocked && !formState.value.hideAmc) {
                        showBottomSheetDialog(token) { selectedItem ->
                            formState.value = formState.value.copy(amc = selectedItem.name.orEmpty())
                            amc_id = selectedItem.id.toString()
                        }
                    }
                },
                onMenuClick = { finish() },
                onNotificationClick = {
                    startActivity(Intent(this, NotificationList::class.java))
                }
            )
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            isSubmitting = false
            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
        }
        viewModel.changePasswordDataResponse.observe(this) { data ->
            if (data.status_code == 200) {
                showDialog(data.message!!, data.status_code)
            } else {
                showDialog(data.message!!, data.status_code ?: 0)
            }
        }
    }

    private fun prepareRoles(accessType: String?) {
        val spinnerItems = listOf(
            Pair("eg_super_admin", "Super Admin"),
            Pair("eg_admin", "Manager"),
            Pair("client_admin", "Client Admin"),
            Pair("client", "Client"),
            Pair("technician", "Technician")
        )
        var labels = spinnerItems.map { it.second }
        var keys = spinnerItems.map { it.first }
        if (accessType.equals("client_admin", ignoreCase = true)) {
            labels = labels.filter { it.equals("Client", ignoreCase = true) }
            keys = keys.filter { it.equals("client", ignoreCase = true) }
        }
        if (accessType.equals("eg_admin", ignoreCase = true)) {
            labels = labels.filter {
                it.equals("Client Admin", ignoreCase = true) ||
                    it.equals("Client", ignoreCase = true) ||
                    it.equals("Technician", ignoreCase = true)
            }
            keys = keys.filter {
                it.equals("client_admin", ignoreCase = true) ||
                    it.equals("client", ignoreCase = true) ||
                    it.equals("technician", ignoreCase = true)
            }
        }
        roleLabels = labels
        roleKeys = keys
    }

    private fun applyEditState() {
        val editJson = intent.getStringExtra("user_data")
        if (editJson.isNullOrBlank()) return
        val existing = Gson().fromJson(editJson, Users::class.java)
        editUserId = existing.id
        val roleIndex = roleKeys.indexOfFirst { it.equals(existing.access_level, true) }
        if (roleIndex >= 0) {
            selected_accessleve = roleKeys[roleIndex]
        } else {
            selected_accessleve = existing.access_level
        }
        formState.value = formState.value.copy(
            title = "User Details",
            subtitle = "Edit user",
            saveLabel = "Update User",
            name = existing.name.orEmpty(),
            email = existing.email.orEmpty(),
            mobile = existing.phone.orEmpty(),
            accessType = roleLabels.getOrNull(roleIndex)
                ?: existing.access_level.orEmpty().replace('_', ' '),
            passwordPlaceholder = "Leave blank to keep password",
            passwordRequired = false
        )
    }

    private fun showAccessTypePicker() {
        if (roleLabels.isEmpty()) return
        AlertDialog.Builder(this)
            .setTitle("Select access type")
            .setItems(roleLabels.toTypedArray()) { dialog, which ->
                selected_accessleve = roleKeys[which]
                formState.value = formState.value.copy(accessType = roleLabels[which])
                applyCompanyPickerForRole(selected_accessleve, creatorAccessType)
                dialog.dismiss()
            }
            .show()
    }

    private fun submitUser() {
        if (isSubmitting) return
        val state = formState.value
        val name = state.name.trim()
        val email = state.email.trim()
        val password = state.password
        val mobile = state.mobile.trim()
        val message = when {
            name.isBlank() -> "Please enter name"
            mobile.isBlank() -> "Please enter mobile number"
            !Validator.isMobileValid(mobile) -> "Please enter valid mobile number"
            email.isBlank() -> "Please enter email address"
            !Validator.isEmailValid(email) -> "Please enter valid email address"
            editUserId == null && password.isBlank() -> "Please enter password"
            selected_accessleve.isNullOrBlank() -> "Please select access type"
            !selected_accessleve.equals("technician", ignoreCase = true) && amc_id == null ->
                "Please select company"
            else -> null
        }
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            return
        }

        val technicianSelected = selected_accessleve.equals("technician", ignoreCase = true)
        companyLinksArray = JsonArray()
        if (!technicianSelected && amc_id != null) {
            companyLinksArray = JsonArray().apply { add(amc_id!!.toInt()) }
        }
        val jsondata = JsonObject().apply {
            addProperty("email", email)
            addProperty("location", "")
            addProperty("notes", "")
            addProperty("pan_id", "")
            addProperty("aadhaar_id", "")
            addProperty("permanent_address", "")
            addProperty("password", password)
            addProperty("name", name)
            addProperty("phone", mobile)
            if (!technicianSelected) {
                add("company_link", companyLinksArray)
            }
            addProperty("access_level", selected_accessleve)
        }
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        isSubmitting = true
        if (editUserId != null) {
            if (password.isBlank()) {
                jsondata.remove("password")
            }
            viewModel.updateUser(editUserId!!, jsondata, token)
        } else {
            viewModel.createTechnician(jsondata, token)
        }
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }

    private fun showBottomSheetDialog(token: String?, onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(this, R.style.NoBackgroundDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val searchView: SearchView = view.findViewById(R.id.searchView)
        viewModel.allAmcDataResponse.observe(this) { data ->
            val adapter = ItemAdapter(data.data.activeCompanies()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean = false
                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
        }
        dialog.setContentView(view)
        val layoutParams = view.layoutParams
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        view.layoutParams = layoutParams
        dialog.show()
    }

    fun showDialog(message: String, code: Int) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (code == 200) {
                        onBackPressedDispatcher.onBackPressed()
                    } else {
                        isSubmitting = false
                    }
                }
            }, true)
    }

    private fun applyCompanyPickerForRole(role: String?, creatorAccessType: String?) {
        val technicianSelected = role.equals("technician", ignoreCase = true)
        if (technicianSelected) {
            amc_id = null
            formState.value = formState.value.copy(hideAmc = true, amc = "")
            return
        }
        if (RoleAccess.lockToAttachedCompany(creatorAccessType)) {
            val attachedId = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
            val attachedName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
            if (attachedId != 0) {
                amc_id = attachedId.toString()
                formState.value = formState.value.copy(
                    hideAmc = false,
                    amc = attachedName.orEmpty(),
                    amcLocked = true
                )
                return
            }
        }
        formState.value = formState.value.copy(hideAmc = false)
    }
}
