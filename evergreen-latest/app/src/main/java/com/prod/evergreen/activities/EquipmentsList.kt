package com.prod.evergreen.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.EquipmentListAdapter
import com.prod.evergreen.helper.EquipmentEditor
import com.prod.evergreen.models.Data
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityEquipmentsListBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper

class EquipmentsList : AppCompatActivity() {
    lateinit var equipmentlistAdapter: EquipmentListAdapter
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel

    lateinit var binding: ActivityEquipmentsListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityEquipmentsListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewmodel()
        sharedPreferencesHelper= SharedPreferencesHelper(this)

        val id= intent.getIntExtra("c_id",0)
        val name= intent.getStringExtra("name")
        loadEquipments()
        equipmentlistAdapter = EquipmentListAdapter(
            sharedPreferencesHelper,
            onViewClick = { data -> EquipmentEditor.openDetails(this, data) },
            onActionClick = { data -> showEquipmentActions(data) }
        )

       binding.recyclerCompanies.adapter=equipmentlistAdapter


        binding.tvAddEq.setOnClickListener {
            startActivity(Intent(this@EquipmentsList, AddEquipment::class.java).putExtra("companyname",name).putExtra("companylink",id).putExtra("hide_company", true))
        }

        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.changePasswordDataResponse.observe(this) { data ->
            android.widget.Toast.makeText(this, data.message ?: "Updated", android.widget.Toast.LENGTH_SHORT).show()
            loadEquipments()
        }
        viewModel.allequipmentsDataResponse.observe(this) { data ->
if(data.status==200)
            equipmentlistAdapter.addData(data.data)

            //need to check
            if (data.data!!.isEmpty()){
                binding.etSearch!!.visibility= View.GONE
                binding.noDataLayout!!.visibility= View.VISIBLE
            }
            else{
                binding.etSearch!!.visibility= View.VISIBLE
                binding.noDataLayout!!.visibility= View.GONE
            }
        }



        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
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
        val object1 = JsonObject()
        object1.addProperty("company_link", id)
        viewModel.getAllEquipmentsByID(token, object1)
    }

    private fun showEquipmentActions(equipment: Data) {
        val name = equipment.name?.takeIf { it.isNotBlank() } ?: "Equipment"
        val companyName = intent.getStringExtra("name")
        val companyId = intent.getIntExtra("c_id", 0).takeIf { it != 0 }
        val role = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        val options = mutableListOf("View Equipment")
        if (com.prod.evergreen.helper.RoleAccess.canManageEquipment(role)) {
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
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }
}