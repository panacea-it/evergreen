package com.prod.evergreen.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.EqupmentHistroyAdapter
import com.prod.evergreen.api.Constants
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityEquipmentDetailsBinding
import com.prod.evergreen.dialogs.MoreEqInfoFragment
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.CompanyDataResponse
import com.prod.evergreen.models.ResponseData

class EquipmentDetails : AppCompatActivity() {

    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    lateinit var equipmentAdapter: EqupmentHistroyAdapter
    var companyData: CompanyDataResponse? = null
    var accessType: String? = null
    var userid: Int? = null
    var equipmentData = ResponseData();
    lateinit var binding: ActivityEquipmentDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // enableEdgeToEdge()
        binding = ActivityEquipmentDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewmodel()
        sharedPreferencesHelper = SharedPreferencesHelper(this)
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        userid = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        val equipment = intent.getIntExtra("eq_id", 0)
        val eq_sn = intent.getStringExtra("eq_sn")
        val screentype = intent.getIntExtra("screentype", 0)

        viewModel.loading.observe(this) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(this, "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        if (accessType == "technician") {
            binding.createtask.visibility = View.INVISIBLE
        }
        equipmentAdapter = EqupmentHistroyAdapter(data = { responseData ->
            val gson = Gson()
            val responseTask = gson.toJson(responseData)
            val responseCompany = gson.toJson(companyData)
            MoreEqInfoFragment.newInstance(responseTask, responseCompany)
                .show(supportFragmentManager, "")
        }, selfAssign = { responseData ->
            val object1 = JsonObject()
            object1.addProperty("task_link", responseData.taskLink)
            object1.addProperty("technician_link", userid)
            viewModel.assignTechnician(object1, token!!)

        }, downloadFile = { downloadfile ->
            val object1 = JsonObject()
            object1.addProperty("task_link", downloadfile.id)
            viewModel.getServiceReport(object1, token!!)
        }
        )

        viewModel.downloadpdf.observe(this) { response ->
            if (response.status_code == 200) {
                openPdfInBrowser(Constants.BASE_URL + response.url!!)
            } else {
                showDialog(response.message!!)
            }

        }
        viewModel.assignTechnicianDataResponse.observe(this) { response ->
            if (response.status_code == 200) {
                showDialog(response.message!!)
                val object1 = JsonObject()
                object1.addProperty("equipment_link", equipment)
                object1.addProperty("self_assign", true)
                viewModel.GetEquipmentInfo(object1, token!!)
            } else {
                showDialog(response.message!!)
            }

        }

        binding.editEqip.setOnClickListener {
            val gson = Gson()
            val data = gson.toJson(equipmentData).toString()
            startActivity(
                Intent(
                    this@EquipmentDetails,
                    AddEquipment::class.java
                ).putExtra("equipment_data", data)
            )
        }

//        equipmentAdapter= EqupmentHistroyAdapter { responseData ->
//            val gson = Gson()
//            val responseTask = gson.toJson(responseData)
//            val responseCompany = gson.toJson(companyData)
//
//            MoreEqInfoFragment.newInstance(responseTask, responseCompany)
//                .show(supportFragmentManager, "")
//        }

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.createtask.setOnClickListener {
            val gson = Gson()
            val data = gson.toJson(equipmentData).toString()
            val intent = Intent(this@EquipmentDetails, CreateTask::class.java)
            intent.putExtra("equipment_data", data)
            startActivity(intent)
        }
        binding.specificationLayout.download.setOnClickListener {
            startActivity(Intent(this@EquipmentDetails, IndividualQRDownloader::class.java).putExtra("id",equipment).putExtra("eq_sn",eq_sn))

        }

        binding.recyclerviewHistroy.adapter = equipmentAdapter
        viewModel.equipmentDataResponse.observe(this) { data ->
            if (data.success == 200) {
                equipmentData = data.data!!
                companyData = equipmentData.company
                binding.specificationLayout.eqName.text = equipmentData.name
                binding.specificationLayout.tvMfd.text = equipmentData.manufacturerDate
                binding.specificationLayout.tvModelNum.text = equipmentData.model
                binding.specificationLayout.tvSNumber.text = equipmentData.serialNumber
                binding.specificationLayout.tvLocation.text = equipmentData.location
                binding.specificationLayout.tvFreqency.text = equipmentData.tmFrequency
                binding.specificationLayout.tvDescr.text = equipmentData.egserialnumber
                if (equipmentData.imageUrl != null) {
                    Glide.with(this).load(Constants.BASE_URL + equipmentData.imageUrl)
                        .into(binding.backdrop)
                }
                if (equipmentData.tasks!!.isEmpty()) {
                    binding.tvNoTask.visibility = View.VISIBLE
                } else {
                    if (screentype == 1) {
                        equipmentAdapter.addData(equipmentData.tasks, accessType, userid)

                    } else {
                        equipmentAdapter.addData(equipmentData.tasks)

                    }

                    binding.tvNoTask.visibility = View.GONE
                }


            }
        }

        val object1 = JsonObject()
        object1.addProperty("equipment_link", equipment)
        viewModel.GetEquipmentInfo(object1, token!!)

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

    fun showDialog(message: String) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)

                }
            }, true)
    }

    private fun openPdfInBrowser(pdfUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(pdfUrl)
        }

        startActivity(intent)
    }
}