package com.prod.evergreen.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.equipment.EquipmentListScreen
import com.example.app.ui.equipment.findByUiId
import com.example.app.ui.equipment.toUiEquipment
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.EquipmentEditor
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.Data

class EquipmentsList : AppCompatActivity() {
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val allEquipment = mutableStateOf<List<Data>>(emptyList())
    private val searchQuery = mutableStateOf("")
    private val filterMode = mutableStateOf(FilterMode.ALL)

    private enum class FilterMode { ALL, ACTIVE, INACTIVE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        setViewmodel()

        val companyName = intent.getStringExtra("name").orEmpty()
        val title = companyName.takeIf { it.isNotBlank() }?.let { "$it Equipments" } ?: "Equipments List"

        setContent {
            val source by allEquipment
            val query by searchQuery
            val mode by filterMode
            val activeOnly = when (mode) {
                FilterMode.ALL -> null
                FilterMode.ACTIVE -> true
                FilterMode.INACTIVE -> false
            }
            EquipmentListScreen(
                equipments = source.toUiEquipment(query, activeOnly),
                searchQuery = query,
                title = title,
                onSearchQueryChange = { searchQuery.value = it },
                onEquipmentClick = { item ->
                    source.findByUiId(item)?.let { EquipmentEditor.openDetails(this, it) }
                },
                onEquipmentLongClick = { item ->
                    source.findByUiId(item)?.let { showEquipmentActions(it) }
                },
                onFilterClick = { showFilterDialog() },
                onAddClick = { onAddEquipment() },
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
                onProfileClick = {
                    startActivity(Intent(this, UserDetails::class.java))
                },
                onScanClick = {
                    startActivity(Intent(this, QrScanner::class.java))
                },
                onNotificationClick = {
                    startActivity(Intent(this, NotificationList::class.java))
                },
                onMenuClick = { finish() },
                onBackClick = { onBackPressedDispatcher.onBackPressed() }
            )
        }

        viewModel.loading.observe(this) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(this, "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.changePasswordDataResponse.observe(this) { data ->
            Toast.makeText(this, data.message ?: "Updated", Toast.LENGTH_SHORT).show()
            loadEquipments()
        }
        viewModel.companyEquipmentsDataResponse.observe(this) { data ->
            if (data.status == 200 || data.data != null) {
                allEquipment.value = data.data.orEmpty().toList()
            }
        }
        loadEquipments()
    }

    override fun onResume() {
        super.onResume()
        if (::viewModel.isInitialized) {
            loadEquipments()
        }
    }

    private fun loadEquipments() {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        val id = intent.getIntExtra("c_id", 0)
        val body = JsonObject()
        body.addProperty("company_link", id)
        viewModel.getAllEquipmentsByID(token, body)
    }

    private fun onAddEquipment() {
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (!RoleAccess.canManageEquipment(role)) {
            Toast.makeText(this, "You cannot add equipment", Toast.LENGTH_SHORT).show()
            return
        }
        val id = intent.getIntExtra("c_id", 0)
        val name = intent.getStringExtra("name")
        startActivity(
            Intent(this, AddEquipment::class.java)
                .putExtra("companyname", name)
                .putExtra("companylink", id)
                .putExtra("hide_company", true)
        )
    }

    private fun showFilterDialog() {
        val options = arrayOf("All equipment", "Active", "Inactive")
        AlertDialog.Builder(this)
            .setTitle("Filter")
            .setSingleChoiceItems(options, filterMode.value.ordinal) { dialog, which ->
                filterMode.value = FilterMode.entries[which]
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEquipmentActions(equipment: Data) {
        val name = equipment.name?.takeIf { it.isNotBlank() } ?: "Equipment"
        val companyName = intent.getStringExtra("name")
        val companyId = intent.getIntExtra("c_id", 0).takeIf { it != 0 }
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        val options = mutableListOf("View Equipment")
        if (RoleAccess.canManageEquipment(role)) {
            options.add("Edit Equipment")
            options.add(if (equipment.isActive()) "Mark Inactive" else "Mark Active")
        }
        options.add("Cancel")
        AlertDialog.Builder(this)
            .setTitle(name)
            .setItems(options.toTypedArray()) { dialog, which ->
                when (options[which]) {
                    "View Equipment" -> EquipmentEditor.openDetails(this, equipment)
                    "Edit Equipment" -> EquipmentEditor.openEdit(this, equipment, companyName, companyId)
                    "Mark Inactive", "Mark Active" -> toggleEquipmentActive(equipment)
                    else -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun toggleEquipmentActive(equipment: Data) {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        val body = JsonObject()
        body.addProperty("equipment_link", equipment.id)
        body.addProperty("action", if (equipment.isActive()) "delete" else "activate")
        viewModel.deleteEquipment(body, token)
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
}
