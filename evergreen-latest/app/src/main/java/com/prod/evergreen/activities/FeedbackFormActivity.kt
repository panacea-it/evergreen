package com.prod.evergreen.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.AtachmentAdapter
import com.prod.evergreen.api.Constants
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentFeedBackFormDialogBinding
import com.prod.evergreen.dialogs.BlankFragment
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.TaskCreated
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedbackFormActivity : AppCompatActivity(), BlankFragment.SignatureDialogListener {

    private val pickImageFromGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val uri = intent!!.data
            if (uri != null) {
                setImage(uri)
            }
        }
    }
   // private var actualImage: File? = null
    private lateinit var photoFile: File
    private val imageList = mutableListOf<String>()
    private val imageListserverimag = mutableListOf<String>()
    lateinit var atachmentAdapter: AtachmentAdapter
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentPhotoPath: String
    private lateinit var viewModel: MainViewModel
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    private var token: String? = null
    private var sinature: String? = null
    private var accesstype: String? = null
    private var follow_up: Boolean? = false

    lateinit var binding: FragmentFeedBackFormDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=FragmentFeedBackFormDialogBinding.inflate(layoutInflater)
        setViewmodel()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        sharedPreferencesHelper= SharedPreferencesHelper(this)
        token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        accesstype=sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)


        val listItems = arrayOf("Camera", "Gallery", "Cancel")
        val builder = AlertDialog.Builder(this@FeedbackFormActivity)
        builder.setTitle("Choose One")
        // val dialog = builder.create()
        builder.setItems(listItems) { _, which ->
            when (which) {
                0 -> {
                    checkCameraPermission()
                }

                1 -> {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pickImageFromGalleryForResult.launch(
                            Intent(
                                MediaStore.ACTION_PICK_IMAGES
                            ).apply {
                                type = "image/*"
                            })
                    } else {
                        handleGalleryPermission()

                    }


                }

                2 -> {

                }
            }

        }



        atachmentAdapter = AtachmentAdapter(imageList) { imageName -> removeImage(imageName) }
        binding.attachments.adapter = atachmentAdapter
        binding.attachments.layoutManager = LinearLayoutManager(this)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                if (currentPhotoPath.isNotEmpty()) {
                    val file = File(currentPhotoPath)
                    compressImage(file)

                }
            }
        }

        setViewmodel()
        val gson = Gson()
        val data=intent.getStringExtra("jsonData")
        val taskFromJson = gson.fromJson(data, TaskCreated::class.java)

        binding.hideIcon1.setOnClickListener {
           // checkLocationPermission()
            builder.show()

        }
        binding.addachmentText.setOnClickListener {
            //checkLocationPermission()
            builder.show()

        }
        binding.etCalltype.text = taskFromJson.task?.callType
        if (taskFromJson.task?.followUp!=null) {
            binding.tvActionDescription.setText(taskFromJson.task?.actionTaken)
            binding.etReqDetails.setText(taskFromJson.task?.reqDetails)
            binding.actionReqDetails.setText(taskFromJson.task?.actionReqDetails)

            if (taskFromJson.task?.followUp == true) {
                binding.actionYes.isChecked=true
            } else {
                binding.actionNo.isChecked=true
            }
        }
        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        if(accesstype=="technician") {
            binding.rl1.visibility= View.GONE
            binding.rl2.visibility= View.GONE
            binding.rl3.visibility= View.GONE
            binding.tv1.visibility= View.GONE
            binding.tv2.visibility= View.GONE
            binding.uploadimageRlSig.visibility= View.GONE
            binding.uploadimageRl.visibility= View.VISIBLE
            binding.descLayout.visibility= View.GONE
        }
        else{
            binding.uploadimageRl.visibility= View.GONE
            binding.etCalltype.isEnabled=false
            binding.rl1.visibility= View.VISIBLE
            binding.tvActionDescription.isEnabled=false
            binding.uploadimageRlSig.visibility= View.VISIBLE
            binding.etReqDetails.isEnabled=false

            for (index in 0 until binding.rgActionRequired.childCount){
                binding.rgActionRequired.getChildAt(index).isEnabled = false
            }

            binding.actionReqDetails.isEnabled=false


            binding.rl3.visibility= View.VISIBLE
            binding.tv1.visibility= View.VISIBLE
            binding.tv2.visibility= View.VISIBLE
            binding.descLayout.visibility= View.VISIBLE
            binding.descLayout.visibility= View.VISIBLE
        }

        binding.close.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.addachmentTextSig.setOnClickListener {
           val showsignature=  BlankFragment();
            showsignature.setCustomDialogListener(this)
            showsignature.show(supportFragmentManager,"")

        }


        binding.etCalltype.setOnClickListener {
            val options = arrayOf("service","amc","breakdown")
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select an Option")
            builder.setItems(options) { dialog, which ->
                val selectedOption = options[which]
                binding.etCalltype.text = selectedOption
                dialog.dismiss()

            }
            val dialog = builder.create()
            dialog.show()

        }

        viewModel.imageUploadDataResponse.observe(this) { data ->
            if (data.status_code == 200) {
                imageListserverimag.add(data.image_url!!)
                imageList.add(photoFile.name)
                atachmentAdapter.notifyDataSetChanged()

            } else {
                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.genaratepdffile.observe(this){resources->
            if (resources.status_code==200){
                showDialog(resources.message ?: "Client approval saved",true)
            }
            else{
                showDialog(resources.message ?: "Approval saved. Report will be available shortly",true)
            }

        }

        viewModel.upDateTaskStatusDataResponse.observe(this) { response ->

            if (response.status_code==200){
               val object2 = JsonObject()
               object2.addProperty("task_link", taskFromJson.taskLink)
               object2.addProperty("task_user_link", taskFromJson.id)
               object2.addProperty("sign_url", sinature)
                viewModel.generateServiceReport(object2, token!!)
           } else if (accesstype != "technician") {
               showDialog(response.message ?: "Unable to close task", false)
           }

        }

        viewModel.taskUpdateFeedbackDataResponse.observe(this) { response ->
            if (response.status_code==200){

                if (accesstype!="technician"){
                    val object1 = JsonObject()
                    object1.addProperty("task_user_link", taskFromJson.id)
                    object1.addProperty("status", "closed")
                    viewModel.upDateTaskStatus(object1, token!!)
                }
                else{
                    showDialog(response.message!!,true)
                   // onBackPressedDispatcher.onBackPressed()
                }

            }
            else{
                showDialog(response.message!!,false)
            }

        }

        binding.tvRating.text=binding.rating.rating.toString()+"/5"
        binding.submit.setOnClickListener {

            if(accesstype=="technician") {
                val object1 = JsonObject().apply {
                    addProperty("task_link", taskFromJson.taskLink)
                    addProperty("call_type", binding.etCalltype.text.toString())
                    addProperty("follow_up", binding.actionYes.isChecked)
                    addProperty("action_taken", binding.tvActionDescription.text.toString())
                    addProperty("req_details", binding.etReqDetails.text.toString())
                    addProperty("action_req_details", binding.actionReqDetails.text.toString())

                    val imageArray = JsonArray()
                    imageListserverimag.forEach { image ->
                        imageArray.add(image)
                    }
                    add("out_images", imageArray)
                }

                val jsonString = object1

// Log or print the JSON string
                Log.d("objectdata",jsonString.toString())

                viewModel.taskUpDateFeedback(jsonString,token!!)

            }
            else{
                if (binding.addachmentTextSig.text.isEmpty() || sinature.isNullOrBlank()){
                    Toast.makeText(this@FeedbackFormActivity,"Please add your signature",Toast.LENGTH_SHORT).show()
                }
                else {
                    val object1 = JsonObject()
                    object1.addProperty("task_link", taskFromJson.taskLink)
                    object1.addProperty("task_user_link", taskFromJson.id)
                    object1.addProperty("sign_url", sinature)
                    object1.addProperty("service_satisfactory", binding.repairedYes.isChecked)
                    object1.addProperty("is_running_smoothly", binding.smoothlyYes.isChecked)
                    object1.addProperty("feedback", binding.desc.text.toString())
                    object1.addProperty("rating", Math.round(binding.rating.rating))
                    viewModel.generateServiceReport(object1, token!!)
                }
                // Log.d("objectdata",object1.toString())

            }

        }

    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }
    fun showDialog(message: String,status:Boolean=false) {
        PopupDialog.getInstance(this@FeedbackFormActivity)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (status){
                        val resultIntent = Intent()
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                       // onBackPressedDispatcher.onBackPressed()
                    }
                    else{
                        onBackPressedDispatcher.onBackPressed()
                    }


                }
            }, true)
    }

    fun showDialog(message: String,type:Int=0) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText("0k")!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (type==1){
                        openAppSettings()
                    }

                }
            }, false)
    }

    private fun openAppSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun openCamera() {
        photoFile = createImageFile()
        cameraLauncher.launch(CameraCaptureHelper.createCaptureIntent(this, photoFile))
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        Log.d("printdata",timeStamp.toString())
        return File.createTempFile("$timeStamp",".jpg",storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath

        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                1
            )
        } else {

            openCamera()
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                openCamera()

            } else {
                // Permission denied, show a message to the user
                showDialog("Camera permission is required to get your location",1)
            }
        }
    }
    private fun removeImage(imageName: Int) {
        imageList.removeAt(imageName)
        imageListserverimag.removeAt(imageName)
        atachmentAdapter.notifyDataSetChanged()
    }
    private fun compressImage(file: File) {
        //  progressDialog.start("Uploading File\n0%",false)
        file.let { imageFile ->
            lifecycleScope.launch {
                // Default compression
                val compressedImage = Compressor.compress(this@FeedbackFormActivity, imageFile)
                setCompressedImage(compressedImage)
            }
        } ?: showError("Please choose an image!")
    }

    private fun setCompressedImage(compressedImage: File) {
        compressedImage.let {
            val token = SharedPreferencesHelper(this).getValueString(ConstantValues.AuthToken)!!
            val file = File(compressedImage!!.path)
            val fileReqBody = file.asRequestBody("image/png".toMediaTypeOrNull())
            val typeReqBody = file.asRequestBody("text/plain".toMediaType())
            val part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)
            val mediaType = "text/plain".toMediaType()
            val type = RequestBody.create(mediaType, "1")

            viewModel.upLoadImage(part, token, "out_images")

        }
    }
    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onSignatureUploaded(signatureUrl: String) {
        binding.addachmentTextSig.text="my-signature.png"
        sinature=signatureUrl
    }
    private fun setImage(uri: Uri) {

        try {
            photoFile = FileUtil.from(this@FeedbackFormActivity, uri)
        } catch (e: IOException) {
            // showError("Failed to read picture data!")
            e.printStackTrace()
        }

        compressImage(photoFile)


    }
    fun handleGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted: start the camera
                openGallery()
            }

            else -> {
                // Permission is not granted: request it
                galleryPermissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }
    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageFromGalleryForResult.launch(pickIntent)
    }
    private val galleryPermissionRequestLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted: proceed with opening the camera
            openGallery()
        } else {
            // Permission denied: inform the user to enable it through settings
            Toast.makeText(
                this@FeedbackFormActivity,
                "you denied permission Go to settings and enable storage permission to use this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
