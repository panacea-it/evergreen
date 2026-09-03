package com.prod.evergreen.activities

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import com.example.app.ui.auth.ForgotPasswordScreen
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.Validator
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener

class ForgotPassword : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private var phone by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setViewmodel()
        setContent {
            ForgotPasswordScreen(
                phone = phone,
                onPhoneChange = { phone = it },
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onSendClick = { sendReset() }
            )
        }

        viewModel.loading.observe(this) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(this, "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.forgotPasswordResponse.observe(this) { data ->
            if (data.status == 200) {
                showDialog(data.message!!, true)
            } else {
                showDialog(data.message!!)
            }
        }
        viewModel.errorMessage.observe(this) { errorMessage ->
            showDialog(errorMessage)
        }
    }

    private fun sendReset() {
        if (phone.isEmpty()) {
            Toast.makeText(this, "Please Enter Mobile Number", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Validator.isMobileValid(phone)) {
            Toast.makeText(this, "Please Enter Valid Mobile Number", Toast.LENGTH_SHORT).show()
            return
        }
        val body = JsonObject()
        body.addProperty("phone", phone)
        viewModel.forgotPassword(body)
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }

    fun showDialog(message: String, success: Boolean = false) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (success) {
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }, true)
    }
}
