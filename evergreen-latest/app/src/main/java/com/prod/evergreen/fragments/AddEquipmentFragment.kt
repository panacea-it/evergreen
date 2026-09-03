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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.ui.equipment.AddEquipmentFormState
import com.example.app.ui.equipment.AddEquipmentScreen
import com.example.app.ui.equipment.PM_FREQUENCY_OPTIONS
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.AddEquipment
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.TabNav
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.YearPickerHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.activeCompanies
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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

class AddEquipmentFragment : Fragment() {
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val galleryRequestCode = 102
    var backendIssueValue: String? = null
    private var compressedImage: File? = null
    lateinit var currentPhotoPath: String
    private var actualImage: File? = null
    private lateinit var photoFile: File
    private var file_name: String? = null
    private var token: String? = ""
    private var company_link: Int? = null
    private var isSubmitting = false
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var param1: String? = null
    private var param2: String? = null
    private val formState = mutableStateOf(AddEquipmentFormState())

    val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    setImage(uri)
                }
            }
        }

    private fun setImage(uri: Uri) {
        try {
            actualImage = FileUtil.from(requireActivity(), uri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        actualImage?.let { compressImage(it) }
    }

    private fun compressImage(file: File) {
        lifecycleScope.launch {
            compressedImage = Compressor.compress(requireActivity(), file)
            setCompressedImage()
        }
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            formState.value = formState.value.copy(photoPreviewPath = it.absolutePath)
            val auth = SharedPreferencesHelper(requireActivity()).getValueString(ConstantValues.AuthToken)!!
            val fileReqBody = it.asRequestBody("image/png".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", it.name, fileReqBody)
            viewModel.upLoadImage(part, auth, "equipment_info")
        }
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
                    if (status) {
                        if (!findNavController().popBackStack()) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    } else {
                        isSubmitting = false
                    }
                }
            }, true)
    }

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else if (isAdded) {
                Toast.makeText(requireActivity(), "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun openCameraAndRequestIfNeeded() {
        val context = context ?: return
        if (CameraCaptureHelper.hasCameraPermission(context)) {
            openCamera()
        } else {
            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val host = activity ?: return
        photoFile = createImageFile()
        takeImageResult.launch(CameraCaptureHelper.createCaptureIntent(host, photoFile))
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("$timeStamp", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && currentPhotoPath.isNotEmpty()) {
                compressImage(File(currentPhotoPath))
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
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType)) {
            val attachedId = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
            val attachedName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
            if (attachedId != 0) {
                company_link = attachedId
                formState.value = formState.value.copy(
                    companyName = attachedName.orEmpty(),
                    hideCompany = true,
                    companyLocked = true
                )
            }
        }
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by formState
                AddEquipmentScreen(
                    state = state,
                    onStateChange = { formState.value = it },
                    onBackClick = { findNavController().popBackStack() },
                    onSaveEquipmentClick = { submitEquipment() },
                    onCancelClick = { findNavController().popBackStack() },
                    onPhotoClick = { showImageSourceDialog() },
                    onClearPhotoClick = { clearPhoto() },
                    onCompanyClick = {
                        showBottomSheetDialog { selectedItem ->
                            formState.value = formState.value.copy(companyName = selectedItem.name.orEmpty())
                            company_link = selectedItem.id
                        }
                    },
                    onYearClick = {
                        YearPickerHelper.show(
                            requireActivity(),
                            YearPickerHelper.yearFromStoredDate(formState.value.manufacturerYear)
                        ) { year ->
                            formState.value = formState.value.copy(manufacturerYear = year.toString())
                        }
                    },
                    onPmFrequencyClick = { showFrequencyDialog() },
                    onHomeClick = { TabNav.home(this@AddEquipmentFragment) },
                    onMessagesClick = { TabNav.equipment(this@AddEquipmentFragment) },
                    onTasksClick = { TabNav.tasks(this@AddEquipmentFragment) },
                    onProfileClick = { TabNav.profile(this@AddEquipmentFragment) },
                    onMenuClick = { openDrawer() },
                    onNotificationClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        viewModel.getAllAmc(token!!)
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code == 200) {
                showDialog(data.message!!, true)
            } else {
                showDialog(data.message!!, false)
            }
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { data ->
            showDialog(data!!, false)
        }
        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code == 200) {
                file_name = data.image_url
            } else {
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitEquipment() {
        if (isSubmitting) return
        if (!validateFields()) return
        isSubmitting = true
        val state = formState.value
        val equipment = AddEquipment.Equipment(
            company_link = company_link!!,
            name = state.equipmentName,
            make = state.make,
            model = state.model,
            image_url = file_name!!,
            serial_number = state.serialNumber,
            manufacturer_date = YearPickerHelper.apiDateFromYear(state.manufacturerYear),
            location = state.location,
            description = state.description,
            tm_frequency = backendIssueValue!!
        )
        viewModel.addEquipments(equipment, token!!)
    }

    private fun clearPhoto() {
        file_name = null
        compressedImage = null
        formState.value = formState.value.copy(photoPreviewPath = null, photoRemoteUrl = null)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AddEquipmentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
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

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun showImageSourceDialog() {
        if (!isAdded) return
        AlertDialog.Builder(requireActivity())
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
            requireActivity(),
            arrayOf(galleryPermission),
            galleryRequestCode
        )
    }

    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        pickImageFromGalleryForResult.launch(pickIntent)
    }

    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->
            val adapter = UserCompaniesAdapter(data.data.activeCompanies()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }
            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = adapter
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun showFrequencyDialog() {
        val options = PM_FREQUENCY_OPTIONS.keys.toTypedArray()
        AlertDialog.Builder(requireActivity())
            .setTitle("Select an Option")
            .setItems(options) { dialog, which ->
                val selectedDisplayName = options[which]
                backendIssueValue = PM_FREQUENCY_OPTIONS[selectedDisplayName]
                formState.value = formState.value.copy(pmFrequency = selectedDisplayName)
                dialog.dismiss()
            }
            .show()
    }

    private fun validateFields(): Boolean {
        val state = formState.value
        val message = when {
            state.equipmentName.isBlank() -> "Please enter name"
            state.make.isBlank() -> "Please enter make"
            state.model.isBlank() -> "Please enter model number"
            state.serialNumber.isBlank() -> "Please enter serial number"
            state.manufacturerYear.isBlank() -> "Please enter manufacturing year"
            state.location.isBlank() -> "Please enter location"
            backendIssueValue.isNullOrEmpty() -> "Please select frequency"
            file_name.isNullOrBlank() -> "Please select an image"
            company_link == null || company_link == 0 -> "Please select company"
            else -> null
        }
        if (message != null) {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
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

    @Suppress("unused")
    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: return null
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFile = File(fileDir, "IMG_$timeStamp.jpg")
            val outputStream: OutputStream = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            FileProvider.getUriForFile(context, "${requireActivity().packageName}.fileprovider", imageFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}
