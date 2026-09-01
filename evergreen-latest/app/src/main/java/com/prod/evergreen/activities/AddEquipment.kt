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
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
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
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.Constants
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityAddEquipmentBinding
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.YearPickerHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.activeCompanies
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

class AddEquipment : AppCompatActivity() {
 var backendIssueValue:String?=null
    private lateinit var photoFile: File
    private val cameraPermission = Manifest.permission.CAMERA
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val cameraRequestCode = 101

    lateinit var EqData:ResponseData
    private val galleryRequestCode = 102
    private var compressedImage: File? = null
    private lateinit var currentPhotoPath: String
    private var actualImage: File? = null


    private var file_name: String? = ""
    private var token: String? = ""
    private var company_link: Int? = null
    private var isSubmitting = false

    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel


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
    private fun setImage(uri: Uri) {

        try {
            actualImage = FileUtil.from(this@AddEquipment, uri)?.also {
//                actualImageView.setImageBitmap(loadBitmap(it))
//                actualSizeTextView.text = String.format("Size : %s", getReadableFileSize(it.length()))
//                clearImage()
            }
        } catch (e: IOException) {
            // showError("Failed to read picture data!")
            e.printStackTrace()
        }

        compressImage(actualImage!!)


    }

    private fun compressImage(file: File) {
        //  progressDialog.start("Uploading File\n0%",false)
        file.let { imageFile ->
            lifecycleScope.launch {
                // Default compression
                val compressedImage = Compressor.compress(this@AddEquipment, imageFile)
                setCompressedImage(compressedImage)
            }
        } ?: showError("Please choose an image!")
    }

    private fun setCompressedImage(compressedImage: File) {
        compressedImage.let {
            binding.rl3.visibility = View.VISIBLE

            Glide.with(this).load(it.absolutePath).placeholder(R.drawable.dummy_text1)
                .into(binding.selectedimage)
            binding.rl2.visibility = View.GONE
            val token =
                SharedPreferencesHelper(this@AddEquipment).getValueString(ConstantValues.AuthToken)!!
            if (compressedImage != null) {
                val file = File(compressedImage!!.path)
                val fileReqBody = file.asRequestBody("image/png".toMediaTypeOrNull())
                val typeReqBody = file.asRequestBody("text/plain".toMediaType())
                val part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)
                val mediaType = "text/plain".toMediaType()
                val type = RequestBody.create(mediaType, "1")

                viewModel.upLoadImage(part, token, "equipment_info")
            }

        }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(this@AddEquipment, errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun showDialog(message: String, goBackOnOk: Boolean = false) {
        PopupDialog.getInstance(this@AddEquipment)!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (goBackOnOk) {
                        finish()
                    } else {
                        isSubmitting = false
                        binding.creatEq.isEnabled = true
                    }
                }
            }, true)
    }
    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCameraAndRequestIfNeeded() {
        if (CameraCaptureHelper.hasCameraPermission(this)) {
            openCamera()
        } else {
            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        photoFile = createImageFile()
        takeImageResult.launch(CameraCaptureHelper.createCaptureIntent(this, photoFile))
    }
    private val takeImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {

            if (currentPhotoPath.isNotEmpty()) {
                val file = File(currentPhotoPath)
                compressImage(file)

            }
//            if (result.data != null) {
////                Log.d("result", result.toString())
////                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
////
////                // Set the image URI to the ImageView
////                // binding.pickFile.setImageBitmap(imageBitmap)
////                val uri = bitmapToUri(this@AddEquipment, imageBitmap!!)!!
////                setImage(uri)
//            } else {
//                Toast.makeText(this@AddEquipment, result.toString(), Toast.LENGTH_SHORT).show()
//            }
        }
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

                // Use FileProvider to get the content URI
                uri = FileProvider.getUriForFile(
                    context,
                    "${packageName}.fileprovider",
                    imageFile
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return uri
    }

    lateinit var binding: ActivityAddEquipmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding= ActivityAddEquipmentBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferencesHelper=SharedPreferencesHelper(this)
        setViewmodel()

        binding.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.cancel.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        val intent=intent
        if (intent!=null) {
            val tempdata = intent.getStringExtra("equipment_data")
            if (tempdata!=null){
                val gson = Gson()
                EqData = gson.fromJson(tempdata, ResponseData::class.java)
                binding.tvUpdate.text = "Update"
                if (EqData.name != null) {
                    binding.tvEqName.setText(EqData.name)
                }
                binding.tvHeader.text="Update Equipment Details"
                if (EqData.make != null){
                    binding.tvMake.setText(EqData.make)
                }
                binding.tvModelNum.setText(EqData.model)
                binding.tvDate.text = YearPickerHelper.displayYear(EqData.manufacturerDate)
                binding.location.setText(EqData.location)
                backendIssueValue=EqData.tmFrequency
                binding.tmFrequency.text = EqData.tmFrequency
                binding.tvSpecifications.setText(
                    EqData.specifications?.takeIf { it.isNotBlank() } ?: EqData.serialNumber
                )
                binding.desc.setText(EqData.description)
                binding.siteName.text = EqData.company?.name ?: intent.getStringExtra("companyname")
                company_link = EqData.company?.id ?: EqData.companyLink ?: intent.getIntExtra("companylink", 0).takeIf { it != 0 }


                if (EqData.imageUrl!=null){
                    binding.rl3.visibility=View.VISIBLE
                    binding.rl2.visibility=View.GONE
                    file_name=EqData.imageUrl
                    Glide.with(this).load(Constants.BASE_URL+EqData.imageUrl).into(binding.selectedimage)
                }
            }
            else{
                val name = intent.getStringExtra("companyname")
                val id = intent.getIntExtra("companylink",0)
                binding.siteName.text = name
                company_link =id
            }
            if (intent.getBooleanExtra("hide_company", false)) {
                binding.companySection.visibility = View.GONE
            }

        }

        sharedPreferencesHelper= SharedPreferencesHelper(this)
         token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)



        binding.close.setOnClickListener {

            binding.rl3.visibility = View.GONE
            binding.rl2.visibility=View.VISIBLE

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
            viewModel.getAllAmc(token!!)

        binding.rl2.setOnClickListener {
            showImageSourceDialog()
        }
        binding.selectedimage.setOnClickListener {
            showImageSourceDialog()
        }
        binding.changePhoto.setOnClickListener {
            showImageSourceDialog()
        }



        binding.tmFrequency.setOnClickListener {
            showFrequencyDialog()
        }

        binding.tvDate.setOnClickListener {
            showYearPicker()
        }
binding.creatEq.setOnClickListener {
    if (isSubmitting) return@setOnClickListener
    if (validateFields()) {
        isSubmitting = true
        binding.creatEq.isEnabled = false
      if (binding.tvUpdate.text!="Update"){
          val equipment = Equipment(
              company_link = company_link!!,
              name = binding.tvEqName.text.toString(),
              make = binding.tvMake.text.toString(),
              model = binding.tvModelNum.text.toString(),
              image_url = file_name,
              serial_number = binding.tvSpecifications.text.toString(),
              manufacturer_date = YearPickerHelper.apiDateFromYear(binding.tvDate.text.toString()),
              location = binding.location.text.toString(),
              description = binding.desc.text.toString(),
              tm_frequency = backendIssueValue!!
          )
          viewModel.addEquipments(equipment, token!!)
      }
        else{
          val equipment = EquipmentUpdate(
              equipment_link = EqData.id!!,
              name = binding.tvEqName.text.toString(),
              make = binding.tvMake.text.toString(),
              model = binding.tvModelNum.text.toString(),
              image_url = file_name,
              serial_number = binding.tvSpecifications.text.toString(),
              specifications = binding.tvSpecifications.text.toString(),
              manufacturer_date = YearPickerHelper.apiDateFromYear(binding.tvDate.text.toString()),
              location = binding.location.text.toString(),
              description = binding.desc.text.toString(),
              tm_frequency = backendIssueValue!!
          )
          viewModel.updateEquipment(equipment, token!!)
        }

    }
}

        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType)) {
            val attachedId = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
            val attachedName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
            if (attachedId != 0) {
                company_link = attachedId
                binding.siteName.text = attachedName
                binding.companySection.visibility = View.GONE
            }
        } else {
            binding.siteName.setOnClickListener {
                showBottomSheetDialog { selectedItem ->
                    binding.siteName.text = selectedItem.name
                    company_link = selectedItem.id
                }
            }
        }
        viewModel.errorMessage.observe(this) { data ->
            showDialog(data, goBackOnOk = false)
        }
        viewModel.changePasswordDataResponse.observe(this) { data ->
            val success = data.status_code == 200
            showDialog(data.message!!, goBackOnOk = success)
        }
        viewModel.loading.observe(this) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(this,"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.imageUploadDataResponse.observe(this) { data ->
            if (data.status_code == 200) {

                file_name = data.image_url
            } else {
                Toast.makeText(this, data.message, Toast.LENGTH_SHORT).show()
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

    data class Equipment(
        val company_link: Int,
        val name: String,
        val make: String,
        val model: String,
        val image_url: String?=null,
        val serial_number: String,
        val manufacturer_date: String,
        val location: String,
        val description: String,
        val tm_frequency: String
    )

    data class EquipmentUpdate(
        val equipment_link: Int,
        val name: String,
        val make: String,
        val model: String,
        val image_url: String?=null,
        val serial_number: String? = null,
        val specifications: String,
        val manufacturer_date: String,
        val location: String,
        val description: String,
        val tm_frequency: String
    )

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(this)
            .setTitle("Choose One")
            .setItems(arrayOf("Camera", "Gallery", "Cancel")) { _, which ->
                when (which) {
                    0 -> openCameraAndRequestIfNeeded()
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pickImageFromGalleryForResult.launch(
                                Intent(MediaStore.ACTION_PICK_IMAGES).apply { type = "image/*" }
                            )
                        } else if (checkPermission(galleryPermission)) {
                            openGallery()
                        } else {
                            requestGaleryPermission()
                        }
                    }
                }
            }
            .show()
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
        if (requestCode == galleryRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Gallery permission denied", Toast.LENGTH_SHORT).show()
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

    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(this@AddEquipment)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        viewModel.allAmcDataResponse.observe(this) { data ->

            val adapter = UserCompaniesAdapter(data.data.activeCompanies()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }

            recyclerView.layoutManager = LinearLayoutManager(this@AddEquipment)
            recyclerView.adapter = adapter
        }


        dialog.setContentView(view)
        dialog.show()
    }
    private fun showYearPicker() {
        YearPickerHelper.show(
            this,
            YearPickerHelper.yearFromStoredDate(binding.tvDate.text?.toString())
        ) { year ->
            binding.tvDate.text = year.toString()
        }
    }

    private fun showFrequencyDialog() {



        val optionsMap = mapOf(
            "Daily" to "daily",
            "Weekly" to "weekly",
            "Bi-Weekly" to "biweekly",
            "Monthly" to "monthly",
            "Bi-Monthly" to "bi_monthly",
            "Tri-Monthly" to "tri_monthly",
            "Quarterly" to "quarterly",
            "Semi-Annual" to "semi_annual",
            "Annual" to "annual"
        )
            val options = optionsMap.keys.toTypedArray()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Select an Option")
            builder.setItems(options) { dialog, which ->
                val selectedDisplayName = options[which]
                binding.tmFrequency.text = selectedDisplayName
                // Get the corresponding backend value
                backendIssueValue = optionsMap[selectedDisplayName]

                dialog.dismiss()

            }
            val dialog = builder.create()
            dialog.show()
    }

    private fun validateFields(): Boolean {
        return com.prod.evergreen.helper.FormValidator.firstInvalid(
            com.prod.evergreen.helper.FormValidator.Check(
                binding.tvEqName, "Please enter name", !binding.tvEqName.text.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.tvMake, "Please enter make", !binding.tvMake.text.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.tvModelNum, "Please enter model number", !binding.tvModelNum.text.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.tvSpecifications, "Please enter serial number", !binding.tvSpecifications.text.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.tvDate, "Please enter manufacturing year", !binding.tvDate.text.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.location, "Please enter location", !binding.location.text.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.tmFrequency, "Please select frequency", !backendIssueValue.isNullOrEmpty()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.eqImg, "Please select an image", !file_name.isNullOrBlank()
            ),
            com.prod.evergreen.helper.FormValidator.Check(
                binding.siteName,
                "Please select company",
                company_link != null && company_link != 0
            )
        ) == null
    }

    private fun showMsg(msg:String) {
        Toast.makeText(this@AddEquipment,msg,Toast.LENGTH_SHORT).show()
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("$timeStamp",".jpg",storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath

        }
    }

}