package com.prod.evergreen.activities

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.AtachmentAdapter
import com.prod.evergreen.adapters.EquipmentsDialogAdapter
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityCreateTaskBinding
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.activeCompanies
import com.prod.evergreen.models.Data
import com.prod.evergreen.models.ResponseData
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateTask : AppCompatActivity() {
    var backendIssueValue:String?=null

    private val imageList = mutableListOf<String>()
    private val imageListserverimag = mutableListOf<String>()
    lateinit var atachmentAdapter: AtachmentAdapter


    private val cameraPermission = Manifest.permission.CAMERA
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val cameraRequestCode = 101

    private val galleryRequestCode = 102
    private var compressedImage: File? = null
    private var currentPhotoPath: String? = ""
    private var actualImage: File? = null
    private var file_name: String? = ""
    private var User_ID: Int? = null
    private var equipment_id: Int? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var token: String? = null
    private var isSubmitting = false
    private var company_link: Int? = null
    private var companyEquipments: List<Data> = emptyList()


    val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                val uri = intent!!.data
                if (uri != null) {
                    setImage(uri)
                }
            }
        }



    private lateinit var bindning: ActivityCreateTaskBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindning = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(bindning.root)
        setViewmodel()

        sharedPreferencesHelper = SharedPreferencesHelper(this)
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        User_ID = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)

        val companyName=sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
        val branchname=sharedPreferencesHelper.getValueString(ConstantValues.BRANCH_NAME)
        val companymail=sharedPreferencesHelper.getValueString(ConstantValues.COMPANY_EMAIL)
        val companylocation=sharedPreferencesHelper.getValueString(ConstantValues.LOCATION)
        val companyLink=sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)

        bindning.cancel.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        if (companyName != null) {
            bindning.siteName.text = companyName;
        }

        if (branchname != null) {
            bindning.siteBranch.text = branchname;
        }

        if (companylocation != null) {
            bindning.siteEmail.setText(companylocation)
        }
        if (companyLink != null) {
            val object1 = JsonObject()
            object1.addProperty("company_link", companyLink)
            viewModel.getAllEquipmentsByID(token!!, object1)
        }



        atachmentAdapter = AtachmentAdapter(imageList) { imageName -> removeImage(imageName) }
        bindning.rvAttachments.adapter = atachmentAdapter
        bindning.rvAttachments.layoutManager = LinearLayoutManager(this@CreateTask)

        val intent=intent
        if (intent!=null){
           val tempdata= intent.getStringExtra("equipment_data")
            val gson=Gson()
            val data = gson.fromJson(tempdata, ResponseData::class.java)
            equipment_id = data.id
            bindning.eqName.text = data.name
            bindning.siteName.text=data.company!!.name
            bindning.siteBranch.text= data.company.branchName
            bindning.specificationLayout.view.visibility = View.VISIBLE
          //  Glide.with(this).load(data.company.logo).into(bindning.eqImage)
            bindning.specificationLayout.tvMfd.text =
                com.prod.evergreen.helper.YearPickerHelper.displayYear(data.manufacturerDate)
            bindning.specificationLayout.tvMake.text = data.make?.takeIf { it.isNotBlank() } ?: "-"
            bindning.specificationLayout.tvModelNum.text = data.model
            bindning.specificationLayout.tvSNumber.text = data.serialNumber
            bindning.specificationLayout.tvLocation.text = data.location
            bindning.specificationLayout.tvFreqency.text = data.tmFrequency
            bindning.specificationLayout.tvDescr.text = data.egserialnumber

        }


        bindning.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        val listItems = arrayOf("Camera", "Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose One")

        builder.setItems(listItems) { _, which ->
            when (which) {
                0 -> {
                  handleCameraPermission()
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

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage.toString(), Toast.LENGTH_SHORT).show()
        }

        viewModel.getAllAmc(token!!)
        viewModel.companyEquipmentsDataResponse.observe(this) { data ->
            companyEquipments = data.data.orEmpty().toList()
        }

        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType) && companyLink != 0) {
            company_link = companyLink
            bindning.siteName.isEnabled = false
            bindning.siteName.isClickable = false
        } else {
            bindning.siteName.setOnClickListener {
                showBottomSheetDialog { selectedItem ->
                    bindning.siteName.text = selectedItem.name
                    bindning.siteBranch.text = selectedItem.branchName
                    bindning.siteEmail.setText(selectedItem.location.orEmpty())
                    company_link = selectedItem.id
                    val object1 = JsonObject()
                    object1.addProperty("company_link", company_link)
                    viewModel.getAllEquipmentsByID(token!!, object1)
                }
            }
        }


        bindning.eqName.setOnClickListener {
            showBottomSheetDialogEquipments { selectedItem ->
                equipment_id = selectedItem.id
                bindning.eqName.text = selectedItem.name
                bindning.specificationLayout.view.visibility = View.VISIBLE
                Glide.with(this).load(selectedItem).into(bindning.eqImage)
                bindning.specificationLayout.tvMfd.text =
                    com.prod.evergreen.helper.YearPickerHelper.displayYear(selectedItem.manufacturer_date)
                bindning.specificationLayout.tvMake.text = selectedItem.make?.takeIf { it.isNotBlank() } ?: "-"
                bindning.specificationLayout.tvModelNum.text = selectedItem.model
                bindning.specificationLayout.tvSNumber.text = selectedItem.serial_number
                bindning.specificationLayout.tvLocation.text = selectedItem.location
                bindning.specificationLayout.tvFreqency.text = selectedItem.tm_frequency
                bindning.specificationLayout.tvDescr.text = selectedItem.description

            }
        }


        viewModel.changePasswordDataResponse.observe(this) { data ->
            if (data.status_code == 200) {
                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()

            }
        }


        bindning.issueTye.setOnClickListener {

            val optionsMap = mapOf(
                "Service" to "service",
                "AMC Preventive Maintenance" to "amc_preventive_maintenance",
                "Break Down" to "breakdown"
            )
            val options = optionsMap.keys.toTypedArray()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select an Option")
            builder.setItems(options) { dialog, which ->
                val selectedDisplayName = options[which]

                // Update the UI with the display name
                bindning.issueTye.text = selectedDisplayName

                // Get the corresponding backend value
                 backendIssueValue = optionsMap[selectedDisplayName]

                dialog.dismiss()

            }
            val dialog = builder.create()
            dialog.show()

        }
        bindning.close.setOnClickListener {
            bindning.rl2.visibility = View.VISIBLE
            bindning.rl3.visibility = View.GONE
            compressedImage = null


        }

        viewModel.createTaskDataResponse.observe(this) { data ->
            val success = data.status_code == 200
            val message = data.message?.takeIf { it.isNotBlank() }
                ?: if (success) "Task created successfully" else "Unable to create task"
            showDialog(message, goBackOnOk = success)
        }
        viewModel.imageUploadDataResponse.observe(this) { data ->
            if (data.status_code == 200) {
                imageListserverimag.add(data.image_url!!)
                imageList.add(actualImage!!.name)
                atachmentAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
            }
        }

        bindning.attachfile.setOnClickListener {
            builder.show()
        }
        bindning.creatTask.setOnClickListener {
            if (isSubmitting) return@setOnClickListener
            val siteBranch = bindning.siteBranch.text.toString()
            val sitename = bindning.siteName.text.toString()
//            val siteemail = bindning.siteEmail.text.toString()
            val eq_name = bindning.eqName.text.toString()
            val title = bindning.title.text.toString()
            val issu_type = backendIssueValue
            val desc = bindning.desc.text.toString()
            val imagarrya = imageListserverimag
            if (com.prod.evergreen.helper.FormValidator.firstInvalid(
                    com.prod.evergreen.helper.FormValidator.Check(
                        bindning.siteName, "Please select company name", sitename.isNotBlank()
                    ),
                    com.prod.evergreen.helper.FormValidator.Check(
                        bindning.eqName, "Please select equipment", eq_name.isNotBlank()
                    ),
                    com.prod.evergreen.helper.FormValidator.Check(
                        bindning.issueTye, "Please choose issue type", issu_type != null
                    ),
                    com.prod.evergreen.helper.FormValidator.Check(
                        bindning.title, "Please enter subject", title.isNotBlank()
                    )
                ) != null
            ) {
                return@setOnClickListener
            }
            else {
                isSubmitting = true
                bindning.creatTask.isEnabled = false
                viewModel.createTask(createJsonObject(title,desc,equipment_id!!,User_ID!!,issu_type!!, imagarrya), token!!)
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

    fun createJsonObject(
        name: String,
        description: String,
        equipment_link: Int,
        client_link: Int,
        call_type: String,
        images: List<String>
    ): JsonObject {
        // Create a JsonArray from the list
        val images = JsonArray().apply {
            images.forEach { add(it) }
        }

        // Create and return the JsonObject
        return JsonObject().apply {
            addProperty("name", name)
            addProperty("description", description)
            addProperty("equipment_link", equipment_link)
            addProperty("client_link", client_link)
            addProperty("call_type", call_type)
            add("images", images)

        }
    }

    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        viewModel.allAmcDataResponse.observe(this) { data ->

            val adapter = UserCompaniesAdapter(data.data.activeCompanies()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }

            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.adapter = adapter
        }


        dialog.setContentView(view)
        dialog.show()
    }

    private fun showBottomSheetDialogEquipments(onItemSelected: (Data) -> Unit) {
        val active = companyEquipments.filter { it.isActive() }
        if (active.isEmpty()) {
            Toast.makeText(this, "No equipment found for this company", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = EquipmentsDialogAdapter(active) { selectedItem ->
            onItemSelected(selectedItem)
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageFromGalleryForResult.launch(pickIntent)
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(cameraPermission),
            cameraRequestCode
        )
    }

    private fun requestGaleryPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(galleryPermission),
            galleryRequestCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            cameraRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }
            galleryRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun openCamera() {
        try {
            actualImage = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val photo = actualImage ?: return
        takeImageResult.launch(CameraCaptureHelper.createCaptureIntent(this, photo))
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted: start the camera
                openCamera()
            }

            else -> {

                // Permission is not granted: request it
                cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }


    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted: proceed with opening the camera
            openCamera()
        } else {
            // Permission denied: inform the user to enable it through settings
            Toast.makeText(
                this,
                "you denied permission Go to settings and enable camera permission to use this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
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


    private val galleryPermissionRequestLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted: proceed with opening the camera
            openGallery()
        } else {
            // Permission denied: inform the user to enable it through settings
            Toast.makeText(
                this,
                "you denied permission Go to settings and enable storage permission to use this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    private val takeImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//
    //        if (result.resultCode == RESULT_OK) {
//            if (result.data != null) {
//                Log.d("result", result.toString())
//                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
//
//                // Set the image URI to the ImageView
//                // binding.pickFile.setImageBitmap(imageBitmap)
//                var uri = bitmapToUri(this, imageBitmap!!)!!
//                setImage(uri)
//            } else {
//                Toast.makeText(this, result.toString(), Toast.LENGTH_SHORT).show()
//            }
//        }

        compressImage()
    }

    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (fileDir != null) {
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "IMG_$timeStamp.jpg"
                val imageFile = File(fileDir, imageFileName)
                val outputStream: OutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()

                // Use FileProvider to get the content URI
                uri = FileProvider.getUriForFile(
                    context,
                    "${this.packageName}.fileprovider",
                    imageFile
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return uri
    }

    private fun setImage(uri: Uri) {

        try {
            actualImage = FileUtil.from(this, uri)?.also {
//                actualImageView.setImageBitmap(loadBitmap(it))
//                actualSizeTextView.text = String.format("Size : %s", getReadableFileSize(it.length()))
//                clearImage()
            }
        } catch (e: IOException) {
            // showError("Failed to read picture data!")
            e.printStackTrace()
        }

        compressImage()


    }

    private fun compressImage() {
        //  progressDialog.start("Uploading File\n0%",false)
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                // Default compression
                compressedImage = Compressor.compress(this@CreateTask, imageFile)
                setCompressedImage()
            }
        } ?: showError("Please choose an image!")
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            bindning.rl3.visibility = View.VISIBLE

            Glide.with(this).load(it.absolutePath).placeholder(R.drawable.dummy_text1)
                .into(bindning.selectedimage)
            bindning.rl2.visibility = View.GONE
            val token =
                SharedPreferencesHelper(this).getValueString(ConstantValues.AuthToken)!!
            if (compressedImage != null) {
                val file = File(compressedImage!!.path)
                val fileReqBody = file.asRequestBody("image/png".toMediaTypeOrNull())
                val typeReqBody = file.asRequestBody("text/plain".toMediaType())
                val part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)
                val mediaType = "text/plain".toMediaType()
                val type = RequestBody.create(mediaType, "1")

                viewModel.upLoadImage(part, token, "tasks")
            }

        }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun showDialog(message: String, goBackOnOk: Boolean = false) {
        PopupDialog.getInstance(this)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (goBackOnOk) {
                        onBackPressedDispatcher.onBackPressed()
                    } else {
                        isSubmitting = false
                        bindning.creatTask.isEnabled = true
                    }
                }
            }, true)
    }

    private fun removeImage(imageName: Int) {
        imageList.removeAt(imageName)
        imageListserverimag.removeAt(imageName)
        atachmentAdapter.notifyDataSetChanged()
    }
}