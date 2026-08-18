package com.prod.evergreen.activities

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.NetworkState
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityForgotPasswordBinding
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.Validator
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener

class ForgotPassword : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
          setViewmodel()


        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.forgotPasswordResponse.observe(this) { data ->
            if (data.status==200) {

                showDialog(data.message!!,true)
            }
            else{
                showDialog(data.message!!)
            }
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            showDialog(errorMessage)

        }


        binding.apply {
            cardGetOtp.setOnClickListener {
                val email=binding.editTextTextEmailAddress.text.toString()
                if (email.isEmpty()){
                    Toast.makeText(this@ForgotPassword,"Please Enter Mobile Number",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                else if(!Validator.isMobileValid(email)){
                    Toast.makeText(this@ForgotPassword,"Please Enter Valid Mobile Number",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener

                }

                val object1 = JsonObject()
                object1.addProperty("phone", email)
                viewModel.forgotPassword(object1)

            }
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

    fun showDialog(message: String,success:Boolean=false) {
        PopupDialog.getInstance(this@ForgotPassword)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (success){
                        onBackPressedDispatcher.onBackPressed()
                    }

                }
            }, true)
    }
}