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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app.ui.task.CreateTaskFormState
import com.example.app.ui.task.CreateTaskScreen
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.activities.MainActivity
import com.prod.evergreen.activities.NotificationList
import com.prod.evergreen.adapters.EquipmentsDialogAdapter
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.YearPickerHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.Data
import com.prod.evergreen.models.activeCompanies
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

class CreateTaskFragment : Fragment() {
    var backendIssueValue: String? = null
    private val imageList = mutableListOf<String>()
    private val imageListserverimag = mutableListOf<String>()
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private var compressedImage: File? = null
    private var currentPhotoPath: String? = ""
    private var actualImage: File? = null
    private var file_name: String? = ""
    private var User_ID: Int? = null
    private var isSubmitting = false
    private var equipment_id: Int? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    private var token: String? = null
    private var companyLink: Int? = null
    private val formState = mutableStateOf(CreateTaskFormState())
    private val companyEquipments = mutableStateOf<List<Data>>(emptyList())

    private val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    setImage(uri)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        User_ID = sharedPreferencesHelper.getValueInt(ConstantValues.USER_ID)
        val companyName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
        val branchname = sharedPreferencesHelper.getValueString(ConstantValues.BRANCH_NAME)
        val companylocation = sharedPreferencesHelper.getValueString(ConstantValues.LOCATION)
        companyLink = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        formState.value = formState.value.copy(
            companyName = companyName.orEmpty(),
            branchName = branchname.orEmpty(),
            location = companylocation.orEmpty(),
            companyLocked = RoleAccess.lockToAttachedCompany(accessType) && (companyLink ?: 0) != 0
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val state by formState
                CreateTaskScreen(
                    state = state,
                    onStateChange = { formState.value = it },
                    onBackClick = { findNavController().popBackStack() },
                    onCreateTaskClick = { submitTask() },
                    onCancelClick = { findNavController().popBackStack() },
                    onHomeClick = { goTo(R.id.homeFragment, "Home") },
                    onMessagesClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onTasksClick = { goTo(R.id.taskFragment, "Tasks List") },
                    onProfileClick = {
                        startActivity(Intent(requireActivity(), com.prod.evergreen.activities.UserDetails::class.java))
                    },
                    onMenuClick = { openDrawer() },
                    onNotificationClick = {
                        startActivity(Intent(requireActivity(), NotificationList::class.java))
                    },
                    onCompanyClick = {
                        if (!formState.value.companyLocked) {
                            showBottomSheetDialog { selectedItem ->
                                formState.value = formState.value.copy(
                                    companyName = selectedItem.name.orEmpty(),
                                    branchName = selectedItem.branchName.orEmpty(),
                                    location = selectedItem.location.orEmpty(),
                                    equipment = "",
                                    equipmentSummary = ""
                                )
                                companyLink = selectedItem.id
                                equipment_id = null
                                val body = JsonObject()
                                body.addProperty("company_link", companyLink)
                                viewModel.getAllEquipmentsByID(token!!, body)
                            }
                        }
                    },
                    onEquipmentClick = {
                        showBottomSheetDialogEquipments { selectedItem ->
                            equipment_id = selectedItem.id
                            formState.value = formState.value.copy(
                                equipment = selectedItem.name.orEmpty(),
                                equipmentSummary = listOfNotNull(
                                    selectedItem.make?.takeIf { it.isNotBlank() }?.let { "Make: $it" },
                                    selectedItem.model?.takeIf { it.isNotBlank() }?.let { "Model: $it" },
                                    selectedItem.serial_number?.takeIf { it.isNotBlank() }?.let { "S.no: $it" },
                                    YearPickerHelper.displayYear(selectedItem.manufacturer_date)
                                        .takeIf { it.isNotBlank() && it != "-" }
                                        ?.let { "MFD: $it" }
                                ).joinToString("  ·  ")
                            )
                        }
                    },
                    onIssueTypeClick = { showIssueTypeDialog() },
                    onPhotoClick = { showImageSourceDialog() },
                    onClearPhotoClick = { clearLatestPhoto() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        if (companyLink != null && companyLink != 0) {
            val object1 = JsonObject()
            object1.addProperty("company_link", companyLink)
            viewModel.getAllEquipmentsByID(token!!, object1)
        }
        viewModel.getAllAmc(token!!)
        viewModel.companyEquipmentsDataResponse.observe(viewLifecycleOwner) { data ->
            companyEquipments.value = data.data.orEmpty().toList()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireActivity(), errorMessage.toString(), Toast.LENGTH_SHORT).show()
        }
        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code == 200) {
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }
        viewModel.createTaskDataResponse.observe(viewLifecycleOwner) { data ->
            val message = data.message?.takeIf { it.isNotBlank() }
                ?: if (data.status_code == 200) "Task created successfully" else "Unable to create task"
            showDialog(message, data.status_code == 200)
        }
        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code == 200) {
                imageListserverimag.add(data.image_url!!)
                imageList.add(actualImage?.name.orEmpty())
                formState.value = formState.value.copy(
                    photoPreviewPath = compressedImage?.absolutePath,
                    photoCount = imageListserverimag.size
                )
            } else {
                compressedImage = null
                file_name = ""
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun submitTask() {
        if (isSubmitting) return
        val state = formState.value
        val sitename = state.companyName.trim()
        val eqName = state.equipment.trim()
        val title = state.subject.trim()
        val desc = state.description.trim()
        val issuType = backendIssueValue
        val message = when {
            sitename.isBlank() -> "Please select company name"
            eqName.isBlank() || equipment_id == null -> "Please select equipment"
            issuType == null -> "Please choose issue type"
            title.isBlank() -> "Please enter subject"
            else -> null
        }
        if (message != null) {
            Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
            return
        }
        isSubmitting = true
        viewModel.createTask(
            createJsonObject(title, desc, equipment_id!!, User_ID!!, issuType!!, imageListserverimag),
            token!!
        )
    }

    private fun showIssueTypeDialog() {
        val optionsMap = mapOf(
            "Service" to "service",
            "AMC Preventive Maintenance" to "amc_preventive_maintenance",
            "Break Down" to "breakdown"
        )
        val options = optionsMap.keys.toTypedArray()
        AlertDialog.Builder(requireActivity())
            .setTitle("Select an Option")
            .setItems(options) { dialog, which ->
                val selectedDisplayName = options[which]
                formState.value = formState.value.copy(issueType = selectedDisplayName)
                backendIssueValue = optionsMap[selectedDisplayName]
                dialog.dismiss()
            }
            .show()
    }

    private fun showImageSourceDialog() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Choose One")
            .setItems(arrayOf("Camera", "Gallery", "Cancel")) { _, which ->
                when (which) {
                    0 -> handleCameraPermission()
                    1 -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pickImageFromGalleryForResult.launch(
                                Intent(MediaStore.ACTION_PICK_IMAGES).apply { type = "image/*" }
                            )
                        } else {
                            handleGalleryPermission()
                        }
                    }
                }
            }
            .show()
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

    fun createJsonObject(
        name: String,
        description: String,
        equipment_link: Int,
        client_link: Int,
        call_type: String,
        images: List<String>
    ): JsonObject {
        val imageArray = JsonArray().apply { images.forEach { add(it) } }
        return JsonObject().apply {
            addProperty("name", name)
            addProperty("description", description)
            addProperty("equipment_link", equipment_link)
            addProperty("client_link", client_link)
            addProperty("call_type", call_type)
            add("images", imageArray)
        }
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

    private fun showBottomSheetDialogEquipments(onItemSelected: (Data) -> Unit) {
        val active = companyEquipments.value.filter { it.isActive() }
        if (active.isEmpty()) {
            Toast.makeText(requireActivity(), "No equipment found for this company", Toast.LENGTH_SHORT).show()
            return
        }
        val dialog = BottomSheetDialog(requireActivity())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = EquipmentsDialogAdapter(active) { selectedItem ->
            onItemSelected(selectedItem)
            dialog.dismiss()
        }
        dialog.setContentView(view)
        dialog.show()
    }

    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        pickImageFromGalleryForResult.launch(pickIntent)
    }

    fun handleCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            cameraPermissionRequestLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val cameraPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "you denied permission Go to settings and enable camera permission to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    fun handleGalleryPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            openGallery()
        } else {
            galleryPermissionRequestLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private val galleryPermissionRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openGallery()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "you denied permission Go to settings and enable storage permission to use this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun openCamera() {
        val host = activity ?: return
        try {
            actualImage = createImageFile()
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        val photo = actualImage ?: return
        takeImageResult.launch(CameraCaptureHelper.createCaptureIntent(host, photo))
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("MMdd_HHmm", Locale.getDefault()).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val takeImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                compressImage()
            }
        }

    fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        var uri: Uri? = null
        try {
            val fileDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            if (fileDir != null) {
                val timeStamp =
                    SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFile = File(fileDir, "IMG_$timeStamp.jpg")
                val outputStream: OutputStream = FileOutputStream(imageFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
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
        compressedImage?.let {
            val auth = SharedPreferencesHelper(requireActivity()).getValueString(ConstantValues.AuthToken)
            if (auth.isNullOrBlank()) {
                showError("Session expired. Please login again.")
                return
            }
            val file = File(it.path)
            val fileReqBody = file.asRequestBody("image/png".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, fileReqBody)
            val mediaType = "text/plain".toMediaType()
            val type = RequestBody.create(mediaType, "1")
            viewModel.upLoadImage(part, auth, "tasks")
        }
    }

    private fun clearLatestPhoto() {
        if (imageList.isNotEmpty()) {
            imageList.removeAt(imageList.lastIndex)
        }
        if (imageListserverimag.isNotEmpty()) {
            imageListserverimag.removeAt(imageListserverimag.lastIndex)
        }
        compressedImage = null
        file_name = ""
        formState.value = formState.value.copy(
            photoPreviewPath = null,
            photoCount = imageListserverimag.size
        )
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    fun showDialog(message: String, issuucessBoolean: Boolean = false) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (issuucessBoolean) {
                        try {
                            val nav = findNavController()
                            if (!nav.popBackStack(R.id.homeFragment, false)) {
                                nav.navigate(R.id.homeFragment)
                            }
                        } catch (_: Exception) {
                            isSubmitting = false
                        }
                    } else {
                        isSubmitting = false
                    }
                }
            }, true)
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
}
