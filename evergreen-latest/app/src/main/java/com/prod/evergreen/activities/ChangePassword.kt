package com.prod.evergreen.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityChangePasswordBinding

class ChangePassword : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    lateinit var bindinChangePassword: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindinChangePassword= ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(bindinChangePassword.root)
        setViewmodel()

//        viewModel.errorMessage.observe(this) { errorMessage ->
//            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
//        }
//
//
//        viewModel.changePasswordDataResponse.observe(this) { data ->
//            if(data.status!!){
//                startActivity(Intent(this@ChangePassword,login::class.java))
//                finishAffinity()
//
//            }}

        bindinChangePassword.cardUpdate.setOnClickListener {

            val pswrd=bindinChangePassword.editTextTextPassword.text.toString()
            val pswrdconfirm=bindinChangePassword.editTextTextPasswordConfirm.text.toString()

            if(pswrd == pswrdconfirm){

                startActivity(Intent(this@ChangePassword, Login::class.java))
                finishAffinity()
//                val object1 = JsonObject()
//                object1.addProperty("email", intent.getStringExtra("email"))
//                object1.addProperty("temporaryId", intent.getStringExtra("tempid"))
//                object1.addProperty("password", pswrd)
//                viewModel.updatePassword(object1)
            }
            else{
                Toast.makeText(this, "both passwords are not same", Toast.LENGTH_SHORT).show()
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
}