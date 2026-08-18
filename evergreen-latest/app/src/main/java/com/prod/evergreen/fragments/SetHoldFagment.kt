package com.prod.evergreen.fragments

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.compressor.Compressor
import com.prod.evergreen.helper.compressor.FileUtil
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.HoldReason
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SetHoldFagment : DialogFragment() {

    private var photoFile: File? = null
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private var currentPhotoPath: String? = null
    private var imageurl: String? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private var token: String? = null
    private var accesstype: String? = null

    interface ReasonDialogListener {
        fun onreason(feedback: String, id: Int, hold_reason: String, spare_part_number: String, image: String)
        fun onreasonUpdate(feedback: String, id: Int, hold_reason: String, spare_part_number: String, image: String)
    }

    private var listener: ReasonDialogListener? = null

    fun setListener(listener: ReasonDialogListener) {
        this.listener = listener
    }

    private var param1: Int? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getInt(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_set_hold_fagment, container, false)
    }

    override fun onViewCreated(dialogView: View, savedInstanceState: Bundle?) {
        super.onViewCreated(dialogView, savedInstanceState)
        dialog?.window?.setLayout(
            (requireContext().resources.displayMetrics.widthPixels).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val listItems = arrayOf("Camera", "Gallery", "Cancel")
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Choose One")
        // val dialog = builder.create()
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

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                currentPhotoPath?.let {
                    val file = File(it)
                    compressImage(file)
                }
            }
        }
        setViewmodel()
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        accesstype = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)

        val fileName: TextView = dialogView.findViewById(R.id.choose_tv)
        val attachFile: ImageView = dialogView.findViewById(R.id.hide_icon1)
        val spinner: Spinner = dialogView.findViewById(R.id.sp_reason)
       // val switch = dialogView.findViewById<SwitchCompat>(R.id.sw_req)
        val feedbackInput = dialogView.findViewById<EditText>(R.id.feedback_input)
        val sparsPartNumber = dialogView.findViewById<EditText>(R.id.spars_part_number)

        if (param2!=""){

               val gson=Gson()
            val type = object : TypeToken<List<HoldReason>>() {}.type
            val data: List<HoldReason> = gson.fromJson(param2, type)
            if (data[data.size - 1].image != null) {
                fileName.text=data[data.size-1].image
                imageurl=data[data.size-1].image
            }
            if (data[data.size - 1].spare_part_number != null){
                sparsPartNumber.visibility=View.VISIBLE
                sparsPartNumber.setText(data[data.size - 1].spare_part_number)
            }
            if (data[data.size - 1].reason != null)
                feedbackInput.setText(data[data.size - 1].reason)


            if (data[data.size - 1].hold != null){

                if (data[data.size - 1].hold=="spare_required"){
                 spinner.setSelection(1)}
                if (data[data.size - 1].hold=="expert_service_required") {
                    spinner.setSelection(2)
                }
            }

        }



        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data) {
                ProgressDialogUtil.showProgressDialog(requireActivity(), "Loading")
            } else {
                ProgressDialogUtil.hideProgressDialog()
            }
        }

        val submitButton = dialogView.findViewById<MaterialCardView>(R.id.submit_button)
        val close = dialogView.findViewById<MaterialCardView>(R.id.close_button)

        close.setOnClickListener {
            dismiss()
        }
        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            if (data.status_code == 200) {
                fileName.text = photoFile?.name ?: ""
                imageurl = data.image_url
            } else {
                Toast.makeText(requireActivity(), data.message, Toast.LENGTH_SHORT).show()
            }
        }

//        switch.setOnCheckedChangeListener { _, isChecked ->
//            feedbackInput.visibility = if (isChecked) View.GONE else View.VISIBLE
//        }


        fileName.setOnClickListener {
           // checkCameraPermission()
            builder.show()
        }


        attachFile.setOnClickListener {
            checkCameraPermission()
        }


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (position==1){
                    sparsPartNumber.visibility=View.VISIBLE
                }
                else{
                    sparsPartNumber.visibility=View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
    }

        submitButton.setOnClickListener {
            val selectedItem: String? = when (spinner.selectedItemPosition) {
                0 -> {
                    Toast.makeText(context, "Please choose one", Toast.LENGTH_SHORT).show()
                    sparsPartNumber.visibility = View.GONE
                    null
                }
                1 -> {
                    sparsPartNumber.visibility = View.VISIBLE
                    "spare_required"
                }
                2 -> {
                    sparsPartNumber.visibility = View.GONE
                    "expert_service_required"
                }
                else -> null
            }
            when {
                selectedItem == null -> return@setOnClickListener
                feedbackInput.text.isEmpty() -> Toast.makeText(context, "Please type reason", Toast.LENGTH_SHORT).show()
                else -> {
                    val sparepartNumber=if (selectedItem=="expert_service_required"){
                        ""
                    }
                    else{
                        sparsPartNumber.text.toString()
                    }



                   if (param2==""){

                        if (imageurl==null){
                            listener?.onreason(feedbackInput.text.toString().trim(), param1 ?: return@setOnClickListener, selectedItem, sparepartNumber, "")
                        }
                        else{
                            listener?.onreason(feedbackInput.text.toString().trim(), param1 ?: return@setOnClickListener, selectedItem, sparepartNumber, imageurl!!)
                        }
                    }
                    else{
                       val gson=Gson()
                       val type = object : TypeToken<List<HoldReason>>() {}.type
                       val data: List<HoldReason> = gson.fromJson(param2, type)
                       if (imageurl==null){
                           listener?.onreasonUpdate(feedbackInput.text.toString().trim(), data[data.size - 1].id ?: return@setOnClickListener, selectedItem, sparepartNumber, "")
                       }
                       else{
                           listener?.onreasonUpdate(feedbackInput.text.toString().trim(), data[data.size - 1].id ?: return@setOnClickListener, selectedItem, sparepartNumber, imageurl!!)
                       }

                    }


                    dismiss()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Int, param2: String) =
            SetHoldFagment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM1, param1)
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

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
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
                showDialog("Camera permission is required", 1)
            }
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireActivity(),
                "com.prod.evergreen.fileprovider",
                it
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(cameraIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("$timeStamp", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun compressImage(file: File) {
        lifecycleScope.launch {
            val compressedImage = Compressor.compress(requireActivity(), file)
            setCompressedImage(compressedImage)
        }
    }

    private fun setCompressedImage(compressedImage: File) {
        val token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken) ?: return
        val fileReqBody = compressedImage.asRequestBody("image/png".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", compressedImage.name, fileReqBody)
        viewModel.upLoadImage(part, token, "out_images")
    }

    private fun showDialog(message: String, type: Int = 0) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText("0k")!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    if (type == 1) {
                        openAppSettings()
                    }
                }
            }, false)
    }

    private fun openAppSettings() {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        startActivity(intent)
    }
    fun handleCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
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
                requireActivity(),
                "you denied permission Go to settings and enable camera permission to use this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private val pickImageFromGalleryForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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
            photoFile = FileUtil.from(requireActivity(), uri)
        } catch (e: IOException) {
            // showError("Failed to read picture data!")
            e.printStackTrace()
        }
        compressImage(photoFile!!)
    }

    fun handleGalleryPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(),
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
                requireActivity(),
                "you denied permission Go to settings and enable storage permission to use this feature",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
