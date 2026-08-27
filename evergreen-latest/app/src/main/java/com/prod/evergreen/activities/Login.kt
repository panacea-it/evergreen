package com.prod.evergreen.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.Constants
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityLoginBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.Validator



class Login : AppCompatActivity() {
  //  private lateinit var database: DatabaseReference

    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    lateinit var bindingLoginBinding: ActivityLoginBinding
    private var fcmToken:String=""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
           // sendNotification(this)

            // Permission is granted. Continue the action or workflow in your
            // app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        bindingLoginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(bindingLoginBinding.root)
//        database = FirebaseDatabase.getInstance(this).reference


//        database.child("BASEURL").child("baseURL").addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val value = dataSnapshot.getValue(String::class.java)
//                if (value != null) {
//                    // Successfully retrieved data
//                    Toast.makeText(this@Login, "Read value: $value", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // Failed to read value
//                Toast.makeText(this@Login, "Failed to read data!", Toast.LENGTH_SHORT).show()
//            }
//        })



        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {

            }
            shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                Snackbar.make(findViewById(android.R.id.content), "Notification blocked", Snackbar.LENGTH_LONG).setAction("Settings") {
                    // Responds to click on the action
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }.show()
                //  Toast.makeText(this, "NOT ALLOWED", Toast.LENGTH_SHORT).show()
            }
            else -> {

                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            }
        }


        val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
//        Firebase.messaging.isAutoInitEnabled = true
       FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                //   Log.w("token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            Log.d("fcm",task.result)
             fcmToken = task.result


        })

        sharedPreferencesHelper = SharedPreferencesHelper(this)



        if (sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) != null) {
            startActivity(Intent(this@Login, MainActivity::class.java))
            finish()
        }





        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        viewModel.userloginresponse.observe(this) { data ->
            if (data.status == 200) {
                    val user = data.data
                    if (user == null) {
                        Toast.makeText(
                            this@Login,
                            data.message ?: "Login failed. Please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@observe
                    }
                    sharedPreferencesHelper.save(ConstantValues.AuthToken, data.access_token)
                    sharedPreferencesHelper.save(ConstantValues.PREF_USERNAME, user.name)
                    sharedPreferencesHelper.save(ConstantValues.PREF_EMAIL, user.email)
                    sharedPreferencesHelper.save(ConstantValues.TYPE_ROLE, user.access_level)
                    sharedPreferencesHelper.save(ConstantValues.PREF_MOBILE, user.phone)
                    user.id?.let { sharedPreferencesHelper.saveInt(ConstantValues.USER_ID, it) }
                    val company = user.company_user?.firstOrNull()?.company
                    if (company != null) {
                        company.id?.let {
                            sharedPreferencesHelper.saveInt(ConstantValues.COMAPNY_LINK, it)
                        }
                        sharedPreferencesHelper.save(ConstantValues.BRANCH_NAME, company.branch_name)
                        sharedPreferencesHelper.save(ConstantValues.COMPANYNAME, company.name)
                        sharedPreferencesHelper.save(ConstantValues.LOCATION, company.location)
                        sharedPreferencesHelper.save(ConstantValues.COMPANY_EMAIL, company.email)
                    }
                    startActivity(Intent(this@Login, MainActivity::class.java))
                    finish()


            }
            else{
                Toast.makeText(this@Login, data.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }


        }


        viewModel.errorMessage.observe(this) { errorMessage ->
            // Show error message to user
            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
        }
        bindingLoginBinding.apply {

            signincard.setOnClickListener {
                val email = editTextPhone.text.toString()
                val password = editTextTextPassword.text.toString()

                if (email.isEmpty()){
                    Toast.makeText(this@Login,"Please Enter Mobile Number",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                else if(!Validator.isMobileValid(email)){
                    Toast.makeText(this@Login,"Please Enter Valid Mobile Number",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener

                }
                else if (password.isEmpty()){
                    Toast.makeText(this@Login,"Please Enter password",Toast.LENGTH_SHORT).show()
                    return@setOnClickListener

                }



                val object1 = JsonObject()
                object1.addProperty("phone", email)
                object1.addProperty("password", password)
                if (fcmToken.isNotBlank()) {
                    object1.addProperty("fcm_id", fcmToken)
                }
                if (!androidId.isNullOrBlank()) {
                    object1.addProperty("device_id", androidId)
                }
                Log.d("Login", "signIn phone=$email fcm=${fcmToken.isNotBlank()} device=${!androidId.isNullOrBlank()}")
                viewModel.userLogin(object1)


            }


            tvForgot.setOnClickListener {


               startActivity(Intent(this@Login, ForgotPassword::class.java))
            }


        }

    }

    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        // This regex pattern validates phone numbers with optional country code, spaces, dashes, or parentheses
        val phoneNumberRegex = "^(\\+\\d{1,3}[- ]?)?\\d{10}$".toRegex()
        return phoneNumber.matches(phoneNumberRegex)
    }


}