package com.prod.evergreen.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.EquipmentListAdapter
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

        val token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val id= intent.getIntExtra("c_id",0)
        val name= intent.getStringExtra("name")
        val object1 = JsonObject()
        object1.addProperty("company_link", id)
        viewModel.getAllEquipmentsByID(token!!,object1!!)
        equipmentlistAdapter= EquipmentListAdapter(sharedPreferencesHelper) { data ->
//            val gson = Gson()
//            val json = gson.toJson(data)
            startActivity(
                Intent(
                    this@EquipmentsList,
                    EquipmentDetails::class.java
                ).putExtra("eq_id", data.id).putExtra("eq_sn", data.eg_serial_number)
            )
        }

       binding.recyclerCompanies.adapter=equipmentlistAdapter


        binding.tvAddEq.setOnClickListener {
            startActivity(Intent(this@EquipmentsList, AddEquipment::class.java).putExtra("companyname",name).putExtra("companylink",id))
        }

        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
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