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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.app.ui.amc.CreateAmcFormState
import com.example.app.ui.amc.CreateAmcScreen
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.FormValidator
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.Validator
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
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val galleryRequestCode = 102
    private var compressedImage: File? = null
    private var currentPhotoPath: String? = ""
    private var actualImage: File? = null
    lateinit var fileUri: Uri
    private var file_name: String? = null
    private var isSubmitting = false
    private var param1: String? = null
    private var param2: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private val formState = mutableStateOf(CreateAmcFormState())

    val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    setImage(uri)
                } else {
                    showError("Unable to read selected image")
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by formState
                CreateAmcScreen(
                    state = state,
                    onStateChange = { formState.value = it },
                    onBackClick = { findNavController().popBackStack() },
                    onMenuClick = { openDrawer() },
                    onNotificationClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onStartDateClick = {
                        showDatePicker { date, timestamp ->
                            selectedStartDate = timestamp
                            formState.value = formState.value.copy(startDate = date, endDate = "")
                        }
                    },
                    onEndDateClick = {
                        if (selectedStartDate != null) {
                            showDatePicker(minDate = selectedStartDate) { date, _ ->
                                formState.value = formState.value.copy(endDate = date)
                            }
                        } else {
                            Toast.makeText(
                                requireActivity(),
                                "Please select the start date first",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onLogoClick = { showImageSourceDialog() },
                    onClearLogoClick = { clearLogo() },
                    onSaveClick = { submitAmc() },
                    onHomeClick = { goTo(R.id.homeFragment, "Home") },
                    onMessagesClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onTasksClick = { goTo(R.id.taskFragment, "Tasks List") },
                    onProfileClick = {
                        startActivity(Intent(requireActivity(), com.prod.evergreen.activities.UserDetails::class.java))
                    }
                )
            }
        }
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
        setViewmodel()

        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            val message = data.message ?: "Operation completed"
            showDialog(message, data.status_code == 200)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { data ->
            showDialog(data.toString(), false)
        }

        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code == 200) {
                file_name = data.image_url
            } else {
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitAmc() {
        if (isSubmitting) return
        val state = formState.value
        val sitename = state.siteName.trim()
        val siteBranch = state.branchName.trim()
        val startDate = state.startDate.trim()
        val endDate = state.endDate.trim()
        val siteLocation = state.siteLocation.trim()
        val siteemail = state.companyEmail.trim()
        val password = state.password.trim()
        val pocMobile = state.mobileNumber.trim()
        val pocName = state.clientName.trim()
        val pocMail = state.email.trim()

        val validationMessage = when {
            sitename.isBlank() -> "Please enter site name"
            siteBranch.isBlank() -> "Please enter branch name"
            startDate.isBlank() -> "Select start date"
            endDate.isBlank() -> "Select end date"
            compressedImage == null || file_name.isNullOrBlank() -> "Please select image"
            !FormValidator.cardCompleteOrEmpty(pocName, pocMobile, pocMail, password) ->
                "Please fill all Client Admin details or leave them empty"
            pocMobile.isNotBlank() && !Validator.isMobileValid(pocMobile) ->
                "Please enter valid mobile number"
            pocMail.isNotBlank() && !Validator.isEmailValid(pocMail) ->
                "Please enter valid email address"
            siteemail.isNotBlank() && !Validator.isEmailValid(siteemail) ->
                "Please enter valid email address"
            else -> null
        }
        if (validationMessage != null) {
            Toast.makeText(requireActivity(), validationMessage, Toast.LENGTH_SHORT).show()
            return
        }

        val object1 = JsonObject().apply {
            addProperty("company_name", sitename)
            addProperty("branch_name", siteBranch)
            addProperty("company_email", siteemail)
            addProperty("password", password)
            addProperty("start_date", startDate)
            addProperty("end_date", endDate)
            addProperty("company_location", siteLocation)
            addProperty("name", pocName)
            addProperty("location", "")
            addProperty("phone", pocMobile)
            addProperty("email", pocMail)
            addProperty("logo", file_name)
        }

        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        if (token.isNullOrBlank()) {
            Toast.makeText(requireActivity(), "Session expired. Please login again.", Toast.LENGTH_SHORT).show()
            return
        }
        isSubmitting = true
        viewModel.createAMC(object1, token)
    }

    private fun showDatePicker(minDate: Long? = null, onDateSelected: (String, Long) -> Unit) {
        val datePickerBuilder = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")
        if (minDate != null) {
            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(minDate))
                .build()
            datePickerBuilder.setCalendarConstraints(constraintsBuilder)
        }
        val datePicker = datePickerBuilder.build()
        datePicker.showNow(parentFragmentManager, "DATE_PICKER")
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

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else if (isAdded) {
                Toast.makeText(requireActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun showImageSourceDialog() {
        if (!isAdded) return
        AlertDialog.Builder(requireActivity())
            .setTitle("Choose One")
            .setItems(arrayOf("Camera", "Gallery", "Cancel")) { _, which ->
                when (which) {
                    0 -> {
                        if (CameraCaptureHelper.hasCameraPermission(requireActivity())) {
                            openCamera()
                        } else {
                            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
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
            requireActivity(),
            arrayOf(galleryPermission),
            galleryRequestCode
        )
    }

    private fun openCamera() {
        val host = activity ?: return
        val currentTimeMillis: Long = System.currentTimeMillis()
        actualImage = File(
            host.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "${currentTimeMillis}captured.jpg"
        )
        fileUri = FileProvider.getUriForFile(host, "${host.packageName}.fileprovider", actualImage!!)
        takeImageResult.launch(CameraCaptureHelper.createCaptureIntent(host, actualImage!!))
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
                uri = FileProvider.getUriForFile(
                    context,
                    "${requireActivity().packageName}.fileprovider",
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
            actualImage = FileUtil.from(requireActivity(), uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        compressImage()
    }

    private fun compressImage() {
        actualImage?.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(requireActivity(), imageFile)
                setCompressedImage()
            }
        } ?: showError("Please choose an image!")
    }

    private fun setCompressedImage() {
        if (!isAdded) return
        compressedImage?.let {
            formState.value = formState.value.copy(logoPreviewPath = it.absolutePath)
            val token = SharedPreferencesHelper(requireActivity()).getValueString(ConstantValues.AuthToken)
            if (!token.isNullOrBlank()) {
                val file = File(it.path)
                val fileReqBody = file.asRequestBody("image/png".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)
                val mediaType = "text/plain".toMediaType()
                val type = RequestBody.create(mediaType, "1")
                viewModel.upLoadImage(part, token, "logo")
            } else {
                showError("Session expired. Please login again.")
            }
        }
    }

    private fun clearLogo() {
        compressedImage = null
        actualImage = null
        file_name = null
        formState.value = formState.value.copy(logoPreviewPath = null)
    }

    private fun showError(errorMessage: String) {
        context?.let {
            Toast.makeText(it, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }

    fun showDialog(message: String, status: Boolean = false) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    isSubmitting = false
                    if (status) {
                        clearFormAfterSuccess()
                    }
                }
            }, true)
    }

    private fun clearFormAfterSuccess() {
        selectedStartDate = null
        compressedImage = null
        actualImage = null
        file_name = null
        formState.value = CreateAmcFormState()
    }

    private fun goTo(destinationId: Int, title: String) {
        findNavController().navigate(destinationId)
        (activity as? MainActivity)?.setTitleTextView(title)
    }

    private fun openDrawer() {
        (activity as? MainActivity)
            ?.findViewById<androidx.drawerlayout.widget.DrawerLayout>(R.id.drawer_layout)
            ?.openDrawer(GravityCompat.START)
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
