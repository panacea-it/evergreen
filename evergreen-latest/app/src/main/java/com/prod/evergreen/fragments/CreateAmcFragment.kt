package com.prod.evergreen.fragments

import android.Manifest
import android.app.Activity
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentCreateAmcBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
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


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class CreateAmcFragment : Fragment() {
    private var selectedStartDate: Long? = null

    private val cameraPermission = Manifest.permission.CAMERA
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val cameraRequestCode = 101
    lateinit var fileUri: Uri
    private val galleryRequestCode = 102
    private var compressedImage: File? = null
    private var currentPhotoPath: String? = ""
    private var actualImage: File? = null


private var file_name:String?=null
    private var param1: String? = null
    private var param2: String? = null
lateinit var binding:FragmentCreateAmcBinding
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel


    val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val uri = intent!!.data
                if (uri != null) {
                    setImage(uri)
                }
            }
        }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentCreateAmcBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper=SharedPreferencesHelper(requireActivity())
        return  binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CreateAmcFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val listItems = arrayOf("Camera", "Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Choose One")
        // val dialog = builder.create()
        builder.setItems(listItems) { _, which ->
            when (which) {
                0 -> {
                    if (checkPermission(cameraPermission)) {
                        openCamera()

                    } else {
                        requestCameraPermission()

                    }
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
                        if (checkPermission(galleryPermission)) {
                            openGallery()

                        } else {
                            requestGaleryPermission()

                        }
                    }


                }

                2 -> {

                }
            }

        }
        binding.imageupload.setOnClickListener {
            builder.show()
        }
        setViewmodel()
        binding.startDate.setOnClickListener {
            showDatePicker { date, timestamp ->
                selectedStartDate = timestamp // Store the start date timestamp
                binding.startDate.setText(date)
            }
        }
        binding.endDate.setOnClickListener {
            if (selectedStartDate != null) {
                // Pass the selectedStartDate to restrict the minimum date
                showDatePicker(minDate = selectedStartDate) { date, _ ->
                    binding.endDate.setText(date)
                }
            } else {
                // Optionally, show a message to select the start date first
                Toast.makeText(requireActivity(), "Please select the start date first", Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireActivity(), errorMessage.toString(), Toast.LENGTH_SHORT).show()
        }

        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code==200) {

                showDialog(data.message!!,true)

            }
            else{

                showDialog(data.message!!,false)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { data ->
            showDialog(data.toString(),false)



        }




        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code==200) {
                file_name=data.image_url
            }
            else{
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }

        binding.close.setOnClickListener {
            binding.imageupload.visibility = View.VISIBLE
            binding.rl2.visibility = View.GONE
            compressedImage=null


        }



        binding.createAmc.setOnClickListener {
            if (compressedImage != null) {
                val endDate = binding.endDate.text?.toString()
                val startDate = binding.startDate.text?.toString() ?: ""
                val siteLocation = binding.siteLocation.text?.toString() ?: ""
                val siteBranch = binding.siteBranch.text?.toString() ?: ""
                val sitename = binding.sitename.text?.toString() ?: ""
                val siteemail = binding.siteEmail.text?.toString() ?: ""
                val password = binding.poc.sitePswd.text?.toString() ?: ""
                val pocMobile = binding.poc.pocNumber.text?.toString() ?: ""
                val pocName = binding.poc.pocName.text?.toString() ?: ""
                val pocMail = binding.poc.pocMail.text?.toString() ?: ""


                val object1 = JsonObject()
                object1.addProperty("company_name", sitename)
                object1.addProperty("branch_name", siteBranch)
                object1.addProperty("company_email", siteemail)
                object1.addProperty("password", password)
                object1.addProperty("start_date", startDate)
                object1.addProperty("end_date", endDate)
                object1.addProperty("company_location", siteLocation)
                object1.addProperty("name", pocName)
                object1.addProperty("location", "")
                object1.addProperty("phone", pocMobile)
                object1.addProperty("email", pocMail)
                object1.addProperty("logo", file_name)

                val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
                viewModel.createAMC(object1, token!!)
            }
            else{
                Toast.makeText(requireActivity(),"Please Select Image",Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun showDatePicker(minDate: Long? = null, onDateSelected: (String, Long) -> Unit) {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")

        // If minDate is provided, set it as the minimum selectable date
        if (minDate != null) {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minDate)) // Only allow dates after minDate
                .build()

            datePickerBuilder.setCalendarConstraints(constraintsBuilder)
        }

        val datePicker = datePickerBuilder.build()

        datePicker.showNow(parentFragmentManager, "DATE_PICKER") // Show the date picker immediately

        datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
            val date = Date(selectedDateInMillis)
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            onDateSelected(format.format(date), selectedDateInMillis)
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

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(cameraPermission),
            cameraRequestCode
        )
    }

    private fun requestGaleryPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
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
                    Toast.makeText(requireActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show()
                }
            }

            galleryRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(requireActivity(), "Gallery permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val currentTimeMillis: Long = System.currentTimeMillis()

        actualImage = File(requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${currentTimeMillis}captured.jpg")
        fileUri = FileProvider.getUriForFile(requireActivity(), requireActivity().packageName + ".fileprovider", actualImage!!)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        takeImageResult.launch(intent)

    }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setImage(fileUri)

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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()

                // Use FileProvider to get the content URI
                uri = FileProvider.getUriForFile(context, "${requireActivity().packageName}.fileprovider", imageFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return uri
    }

    private fun setImage(uri: Uri) {

        try {
            actualImage = FileUtil.from(requireActivity(), uri)?.also {
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
                compressedImage = Compressor.compress(requireActivity(), imageFile)
                setCompressedImage()
            }
        } ?: showError("Please choose an image!")
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            binding.rl2.visibility=View.VISIBLE

            Glide.with(this).load(it.absolutePath).placeholder(R.drawable.dummy_text1).into(binding.selectedimage)
             binding.imageupload.visibility=View.GONE
            val token = SharedPreferencesHelper(requireActivity()).getValueString(ConstantValues.AuthToken)!!
            if (compressedImage != null) {
                val file = File(compressedImage!!.path)
                val fileReqBody = file.asRequestBody("image/png".toMediaTypeOrNull())
                val typeReqBody = file.asRequestBody("text/plain".toMediaType())
                val part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)
                val mediaType = "text/plain".toMediaType()
                val type = RequestBody.create(mediaType, "1")

                viewModel.upLoadImage(part, token,"logo")
            }

        }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
    }
private fun setViewmodel() {
    val repository = MainRepository(RetrofitService.getInstance(requireActivity()),XApplication.database.newsDao(),XApplication.database.companyDao())
    val viewModelFactory = MyViewModelFactory(repository)
    viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
}

    fun showDialog(message: String,status:Boolean=false) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (status){
                        findNavController().navigate(R.id.companiesFragment)
                        (activity as MainActivity).setTitleTextView("Company's List")
                    }

                }
            }, true)
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}