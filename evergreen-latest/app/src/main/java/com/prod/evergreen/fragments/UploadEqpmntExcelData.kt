package com.prod.evergreen.fragments

import android.app.Dialog
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.ItemAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentUploadEqpmntExcelDataBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.helper.customdialog.PopupDialog
import com.prod.evergreen.helper.customdialog.Styles
import com.prod.evergreen.helper.customdialog.listener.OnDialogButtonClickListener
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.activeCompanies
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class UploadEqpmntExcelData : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
     var fileName: String? =null
     var uri_document: Uri? =null
     var company_link_id: Int? =null

    private var token: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel
    lateinit var binding:FragmentUploadEqpmntExcelDataBinding



    private val pickFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            // Handle the selected file URI here
         binding.fileName.text=getFileName(it)
            fileName=getFileName(it)
            uri_document=it
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
        // Inflate the layout for this fragment
        binding= FragmentUploadEqpmntExcelDataBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)

        return  binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()

        viewModel.getAllAmc(token!!)
        val accessType = sharedPreferencesHelper.getValueString(ConstantValues.TYPE_ROLE)
        if (RoleAccess.lockToAttachedCompany(accessType)) {
            val attachedId = sharedPreferencesHelper.getValueInt(ConstantValues.COMAPNY_LINK)
            val attachedName = sharedPreferencesHelper.getValueString(ConstantValues.COMPANYNAME)
            if (attachedId != 0) {
                company_link_id = attachedId
                binding.companySearc.setText(attachedName)
                binding.companySearc.isEnabled = false
                binding.companySearc.isClickable = false
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

        viewModel.imageUploadDataResponse.observe(viewLifecycleOwner) { data ->
            val errors = data.errors.orEmpty()
            val message = buildString {
                append(data.message ?: if (data.status_code == 200) "Uploaded" else "Upload failed")
                if (errors.isNotEmpty()) {
                    append("\n\n")
                    append(errors.joinToString("\n"))
                }
            }
            showDialog(message, if (data.status_code == 200 && errors.isEmpty()) Styles.SUCCESS else Styles.FAILED)
        }
        binding.downloadTemplate.setOnClickListener { copyExcelTemplate() }
        binding.uploadDocument.setOnClickListener {
            if (company_link_id == null) {
                Toast.makeText(requireActivity(), "Please select company", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (uri_document == null) {
                Toast.makeText(requireActivity(), "Please choose an Excel file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadFile(uri_document!!,fileName, company_link_id);
        }
        if (!RoleAccess.lockToAttachedCompany(accessType)) {
            binding.companySearc.setOnClickListener {
                showBottomSheetDialog { selectedItem ->
                    binding.companySearc.setText(selectedItem.name)
                    binding.branchSearc.setText(selectedItem.branchName)
                    company_link_id=selectedItem.id
                }
            }
        }
        binding.chooseFile.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    openFilePicker()
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "Permission needed to access files",
                        Toast.LENGTH_LONG
                    ).show()
                }
                } else {
                openFilePicker()
                }


        }
       // viewModel.upLoadFile(token)
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_STORAGE_PERMISSION
            )
        }


    }
    private fun copyExcelTemplate() {
        try {
            val file = java.io.File(
                android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
                "evergreen_equipment_template.xlsx"
            )
            requireActivity().assets.open("evergreen_equipment_template.xlsx").use { input ->
                java.io.FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            Toast.makeText(requireActivity(), "Template saved to ${file.absolutePath}", Toast.LENGTH_LONG).show()
            val uri = androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireActivity().packageName}.fileprovider",
                file
            )
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(android.content.Intent.createChooser(intent, "Open Excel template"))
        } catch (error: Exception) {
            Toast.makeText(requireActivity(), "Unable to save template: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setViewmodel() {
        val repository = MainRepository(RetrofitService.getInstance(requireActivity()),XApplication.database.newsDao(),XApplication.database.companyDao())
        val viewModelFactory = MyViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]
    }


    private fun openFilePicker() {
        pickFile.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openFilePicker()
        } else {
          Toast.makeText(requireActivity(),"Permission needed to access files",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor: Cursor? = requireActivity().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
    private fun uploadFile(uri: Uri, fileName: String?,id: Int?) {
        fileName?.let {
            val inputStream =requireActivity().contentResolver.openInputStream(uri)
            val fileBytes = inputStream?.readBytes()
            inputStream?.close()

            fileBytes?.let { bytes ->
                val requestFile = RequestBody.create("application/octet-stream".toMediaTypeOrNull(), bytes)
                val body = MultipartBody.Part.createFormData("file", fileName, requestFile)
                val companyLink = RequestBody.create("text/plain".toMediaTypeOrNull(), id.toString()) // Replace with actual value
                // Replace with actual token
                 viewModel.upLoadFile(body,token!!,companyLink)

            }
        }
    }
    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(requireActivity(),R.style.NoBackgroundDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_amc_layout, null)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val searchView: SearchView = view.findViewById(R.id.searchView)
        viewModel.allAmcDataResponse.observe(viewLifecycleOwner) { data ->

            val adapter = ItemAdapter(data.data.activeCompanies()) { selectedItem ->
                onItemSelected(selectedItem)
                dialog.dismiss()
            }

            recyclerView.layoutManager = LinearLayoutManager(requireActivity())
            recyclerView.adapter = adapter

            // Setup search view listener
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText)
                    return false
                }
            })
        }


        dialog.setContentView(view)
        val layoutParams = view.layoutParams
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.8).toInt()
        view.layoutParams = layoutParams
        dialog.show()

    }
    companion object {
        private const val REQUEST_CODE_STORAGE_PERMISSION = 1
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            UploadEqpmntExcelData().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun showDialog(message: String, failed: Styles) {
        PopupDialog.getInstance(requireActivity())!!
            .setStyle(failed)!!
            .setHeading("Message")!!
            .setDescription(message)!!
            .setCancelable(false)!!
            .setPositiveButtonText(getString(R.string.positive))!!
            .showDialog(object : OnDialogButtonClickListener() {
                override fun onPositiveClicked(dialog: Dialog?) {
                    super.onPositiveClicked(dialog)

                }
            }, true)
    }

}