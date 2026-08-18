package com.prod.evergreen.activities

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.goodiebag.pinview.Pinview
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityVerifyOtpBinding

class VerifyOtp : AppCompatActivity() {
    companion object{
        var otp:String=""
    }
    private lateinit var viewModel: MainViewModel
    lateinit var binding: ActivityVerifyOtpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVerifyOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViewmodel()
//        viewModel.errorMessage.observe(this) { errorMessage ->
//            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
//        }


//        viewModel.verifyOtpResponse.observe(this) { data ->
//if(data.status!!){
//                startActivity(Intent(this@VerifyOtp, ChangePassword::class.java).putExtra("email",intent.getStringExtra("email")).putExtra("tempid",data.temporaryId))
//
//        }}

        binding.verifyCard.setOnClickListener {
//
//            val object1 = JsonObject()
//            object1.addProperty("email", intent.getStringExtra("email"))
//            object1.addProperty("otp", otp.toInt())
//            viewModel.verifyOtp(object1)

            startActivity(Intent(this@VerifyOtp, ChangePassword::class.java).putExtra("email",intent.getStringExtra("email")).putExtra("tempid","123456"))

        }
        binding.pinview.setPinViewEventListener(object : Pinview.PinViewEventListener {
            override fun onDataEntered(pinview: Pinview?, fromUser: Boolean) {
                otp=pinview!!.value
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.pinview.windowToken, 0)
            }
        })
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