package com.prod.evergreen.dialogs

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity.RESULT_OK
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.AtachmentAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentFeedBackFormDialogBinding
import com.prod.evergreen.helper.CameraCaptureHelper
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.interfaces.FeedbackFormDialogListener
import com.prod.evergreen.models.Task
import com.prod.evergreen.models.TaskCreated
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private const val ARG_PARAM2 = "param2"



class FeedBackFormFragmentDialog(private val listener: FeedbackFormDialogListener) : DialogFragment() {
    private lateinit var photoFile: File
    private val imageList = mutableListOf<String>()
    lateinit var atachmentAdapter: AtachmentAdapter
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var currentPhotoPath: String
    companion object {
        @JvmStatic
        fun newInstance(param2: String,listener: FeedbackFormDialogListener) =
            FeedBackFormFragmentDialog(listener).apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM2, param2)


                }
            }
    }
    private lateinit var viewModel: MainViewModel
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    // TODO: Rename and change types of parameters

    private var param2: String? = null

    private var token: String? = null
    private var accesstype: String? = null
    private var follow_up: Boolean? = false

lateinit var binding:FragmentFeedBackFormDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialog)
        arguments?.let {
            param2 = it.getString(ARG_PARAM2)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentFeedBackFormDialogBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper= SharedPreferencesHelper(requireActivity())
        token=sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        accesstype=sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        atachmentAdapter = AtachmentAdapter(imageList) { imageName -> removeImage(imageName) }
        binding.attachments.adapter = atachmentAdapter
        binding.attachments.layoutManager = LinearLayoutManager(context)
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                compressAndUploadImage(currentPhotoPath)

            }
        }



        setViewmodel()
        val gson = Gson()
        val taskFromJson = gson.fromJson(param2, TaskCreated::class.java)

        binding.hideIcon1.setOnClickListener {
            checkLocationPermission()

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
        viewModel.loading.observe(viewLifecycleOwner) { data ->
            if (data){
                ProgressDialogUtil.showProgressDialog(requireActivity(),"Loading")
            }
            else{
                ProgressDialogUtil.hideProgressDialog()
            }
        }




        if(accesstype=="technician") {
binding.rl1.visibility=View.GONE
binding.rl2.visibility=View.GONE
binding.rl3.visibility=View.GONE
binding.tv1.visibility=View.GONE
binding.tv2.visibility=View.GONE
binding.descLayout.visibility=View.GONE


        }
        else{
            binding.etCalltype.isEnabled=false
            binding.rl1.visibility=View.VISIBLE
            binding.tvActionDescription.isEnabled=false

            binding.etReqDetails.isEnabled=false

            for (index in 0 until binding.rgActionRequired.childCount){
                binding.rgActionRequired.getChildAt(index).isEnabled = false
                }

            binding.actionReqDetails.isEnabled=false


            binding.rl3.visibility=View.VISIBLE
            binding.tv1.visibility=View.VISIBLE
            binding.tv2.visibility=View.VISIBLE
            binding.descLayout.visibility=View.VISIBLE
            binding.descLayout.visibility=View.VISIBLE



        }


        binding.close.setOnClickListener {
            dismiss()
        }


        binding.etCalltype.setOnClickListener {
            val options = arrayOf("service","amc","breakdown")
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Select an Option")
            builder.setItems(options) { dialog, which ->
                val selectedOption = options[which]
                binding.etCalltype.text = selectedOption
                dialog.dismiss()

            }
            val dialog = builder.create()
            dialog.show()

        }

        viewModel.taskUpdateFeedbackDataResponse.observe(viewLifecycleOwner) { response ->
            if (response.status_code==200){
                listener.onFeedbackSubmittedSuccessfully(true,taskFromJson.id)
                if (accesstype=="technician"){
                    showDialog(response.message!!)
                }
                else{
                    dismiss()
                }

            }
            else{
                listener.onFeedbackSubmittedSuccessfully(false,taskFromJson.id)
                showDialog(response.message!!)
            }

        }

        binding.tvRating.text=binding.rating.rating.toString()+"/5"
        binding.submit.setOnClickListener {

            if(accesstype=="technician") {
                val object1 = JsonObject()
                object1.addProperty("task_link", taskFromJson.taskLink)
                object1.addProperty("call_type", binding.etCalltype.text.toString())
                object1.addProperty("follow_up", binding.actionYes.isChecked)
                object1.addProperty("action_taken", binding.tvActionDescription.text.toString())
                object1.addProperty("req_details", binding.etReqDetails.text.toString())
                object1.addProperty("action_req_details", binding.actionReqDetails.text.toString())
               viewModel.taskUpDateFeedback(object1,token!!)



            }
            else{
                val object1 = JsonObject()
                object1.addProperty("task_link", taskFromJson.taskLink)
                object1.addProperty("service_satisfactory",binding.repairedYes.isChecked)
                object1.addProperty("is_running_smoothly", binding.smoothlyYes.isChecked)
                object1.addProperty("feedback", binding.desc.text.toString())
                object1.addProperty("rating", binding.rating.rating)
                viewModel.taskUpDateFeedback(object1,token!!)
               // Log.d("objectdata",object1.toString())

            }

        }




    }


    private fun setViewmodel() {
        val repository = MainRepository(
            RetrofitService.getInstance(requireActivity()),
            XApplication.database.newsDao(),
            XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }
    fun showDialog(message: String) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(Styles.IOS)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)
                    dismiss()

                }
            }, true)
    }

    fun showDialog(message: String,type:Int=0) {
        PopupDialog.getInstance(requireActivity())!!
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
        intent.data = Uri.fromParts("package", requireActivity().packageName, null)
        startActivity(intent)
    }

    private fun openCamera() {
        val host = activity ?: return
        photoFile = createImageFile()
        cameraLauncher.launch(CameraCaptureHelper.createCaptureIntent(host, photoFile))
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("MMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("$timeStamp",".jpg",storageDir).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath

        }
    }
    private fun compressAndUploadImage(filePath: String) {
        val imageFileBc = File(filePath)



    }
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
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
                // Permission denied, show a message to the user
                showDialog("Camera permission is required to get your location",1)
            }
        }
    }
    private fun removeImage(imageName: Int) {

//        val imageFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName)
//        if (imageFile.exists()) {
//            imageFile.delete()
//        }
//
//        imageList.remove(imageName)
//        atachmentAdapter.notifyDataSetChanged()


    }
}