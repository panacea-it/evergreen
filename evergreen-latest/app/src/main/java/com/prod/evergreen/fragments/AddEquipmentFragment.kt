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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.GsonBuilder
import com.prod.evergreen.activities.AddEquipment
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.UserCompaniesAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentAddEquipmentBinding
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
class AddEquipmentFragment : Fragment() {
    private val cameraPermission = Manifest.permission.CAMERA
    private val galleryPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private val cameraRequestCode = 101
     var backendIssueValue:String?=null
    lateinit var binding: FragmentAddEquipmentBinding
    private val galleryRequestCode = 102
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

    val pickImageFromGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val uri = intent!!.data
                if (uri != null) {
                    setImage(uri)
                }
            }
        }
    private fun setImage(uri: Uri) {

        try {
            actualImage = FileUtil.from(requireActivity(), uri)?.also {
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
                compressedImage = Compressor.compress(requireActivity(), imageFile)
                setCompressedImage()
            }
        } ?: showError("Please choose an image!")
    }

    private fun setCompressedImage() {
        compressedImage?.let {
            binding.rl3.visibility = View.VISIBLE

            Glide.with(this).load(it.absolutePath).placeholder(R.drawable.dummy_text1)
                .into(binding.selectedimage)
            binding.rl2.visibility = View.GONE
            val token =
                SharedPreferencesHelper(requireActivity()).getValueString(ConstantValues.AuthToken)!!
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
        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
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
                    if (status) {
                        if (!findNavController().popBackStack()) {
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
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
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        Log.d("printdata",timeStamp.toString())
        return File.createTempFile("$timeStamp",".jpg",storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath

        }
    }
    private val takeImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            if (currentPhotoPath.isNotEmpty()) {
                val file = File(currentPhotoPath)
                compressImage(file)

            }
//            if (result.data != null) {
//                Log.d("result", result.toString())
//                val imageBitmap = result.data?.extras?.get("data") as Bitmap?
//
//                // Set the image URI to the ImageView
//                // binding.pickFile.setImageBitmap(imageBitmap)
//                var uri = bitmapToUri(requireActivity(), imageBitmap!!)!!
//                setImage(uri)
//            } else {
//                Toast.makeText(requireActivity(), result.toString(), Toast.LENGTH_SHORT).show()
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()

                // Use FileProvider to get the content URI
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


    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
        // Inflate the layout for this fragment
        binding= FragmentAddEquipmentBinding.inflate(layoutInflater, container, false)

    return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()

        sharedPreferencesHelper= SharedPreferencesHelper(requireActivity())
        token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)


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


binding.close.setOnClickListener {

    binding.rl3.visibility = View.GONE
    binding.rl2.visibility=View.VISIBLE

}

        binding.tmFrequency.setOnClickListener {
            showFrequencyDialog()
        }

        binding.tvDate.setOnClickListener {
            YearPickerHelper.show(
                requireActivity(),
                YearPickerHelper.yearFromStoredDate(binding.tvDate.text?.toString())
            ) { year ->
                binding.tvDate.text = year.toString()
            }
        }
        binding.creatEq.setOnClickListener {

            if (isSubmitting) return@setOnClickListener
            if (validateFields()) {
                isSubmitting = true
                binding.creatEq.isEnabled = false
                val equipment = AddEquipment.Equipment(
                    company_link = company_link!!,
                    name = binding.tvEqName.text.toString(),
                    make = binding.tvMake.text.toString(),
                    model = binding.tvModelNum.text.toString(),
                    image_url = file_name!!,
                    serial_number = binding.tvSpecifications.text.toString(),
                    manufacturer_date = YearPickerHelper.apiDateFromYear(binding.tvDate.text.toString()),
                    location = binding.location.text.toString(),
                    description = binding.desc.text.toString(),
                    tm_frequency = backendIssueValue!!
                )
                val gson = GsonBuilder().setPrettyPrinting().create()
                val json= gson.toJson(equipment)
                Log.d("outputdata",json.toString())
                viewModel.addEquipments(equipment, token!!)
            }
        }

        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType)) {
            val attachedId = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
            val attachedName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
            if (attachedId != 0) {
                company_link = attachedId
                binding.siteName.text = attachedName
                binding.siteName.isEnabled = false
                binding.siteName.isClickable = false
            }
        } else {
            binding.siteName.setOnClickListener {
                showBottomSheetDialog { selectedItem ->
                    binding.siteName.text = selectedItem.name
                    company_link = selectedItem.id
                }
            }
        }
        viewModel.changePasswordDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code==200){
                showDialog(data.message!!,true)
            }
            else{
                showDialog(data.message!!,false)
            }


        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { data ->
            showDialog(data!!,false)


        }
        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
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
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
    }
    data class Equipment(
        val company_link: Int,
        val name: String,
        val make: String,
        val model: String,
        val serial_number: String,
        val image: List<String>,
        val specifications: String,
        val manufacturer_date: String,
        val location: String,
        val description: String,
        val tm_frequency: String
    )

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
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
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



        val optionsMap = mapOf(
            "Daily" to "daily",
            "Weekly" to "weekly",
            "BY-Weekly" to "biweekly",
            "Monthly" to "monthly",
            "BY-Monthly" to "bi_monthly",
            "Tri-Monthly" to "tri_monthly",
            "Quarterly" to "quarterly",
            "Semi-Annual" to "semi_annual",
            "Annual" to "annual"
        )
        val options = optionsMap.keys.toTypedArray()
        val builder = AlertDialog.Builder(requireActivity())
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
        Toast.makeText(requireActivity(),msg,Toast.LENGTH_SHORT).show()
    }

}