package com.prod.evergreen.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import com.prod.evergreen.R
import com.prod.evergreen.XApplication
import com.prod.evergreen.adapters.ItemAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.FragmentDownloadQrBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.RoleAccess
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import com.prod.evergreen.models.AMCData
import com.prod.evergreen.models.activeCompanies
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DownloadQrFragment : Fragment() {
    lateinit var binding: FragmentDownloadQrBinding
    private var param1: String? = null
    private var param2: String? = null
    var company_link_id: Int? = null
    private val STORAGE_PERMISSION_CODE = 100
    private var token: String? = null
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    private lateinit var viewModel: MainViewModel

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
        binding = FragmentDownloadQrBinding.inflate(layoutInflater, container, false)
        sharedPreferencesHelper = SharedPreferencesHelper(requireActivity())
        token = sharedPreferencesHelper.getValueString(ConstantValues.AuthToken)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViewmodel()
        requestStoragePermission()
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

        if (!RoleAccess.lockToAttachedCompany(accessType)) {
            binding.companySearc.setOnClickListener {
                showBottomSheetDialog { selectedItem ->
                    binding.companySearc.setText(selectedItem.name)
                    binding.branchSearc.setText(selectedItem.branchName)
                    company_link_id = selectedItem.id
                }
            }
        }

        viewModel.downloadQrDataResponse.observe(viewLifecycleOwner) { response ->
            saveToFile(response)
        }

        binding.downloadDocument.setOnClickListener {
            if (binding.companySearc.text.isNotEmpty()) {
                downloadEquipment()
            } else {
                Toast.makeText(requireActivity(), "Please Select company", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            DownloadQrFragment().apply {
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

    private fun showBottomSheetDialog(onItemSelected: (AMCData) -> Unit) {
        val dialog = BottomSheetDialog(requireActivity(), R.style.NoBackgroundDialogTheme)
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

    private fun saveToFile(responseBody: ResponseBody) {
        try {
            if (!isExternalStorageWritable()) {
                showToast("External storage not writable")
                return
            }

            // Generate a random file name
            val fileName = "evergreen_${System.currentTimeMillis()}.pdf"

            // Use Environment.getExternalStoragePublicDirectory to save in the public Downloads directory
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, fileName)

            // Create the file and write the input stream to it
            val inputStream: InputStream = responseBody.byteStream()
            val outputStream = FileOutputStream(file)
            inputStream.use { input ->
                outputStream.use { output ->
                    val buffer = ByteArray(4 * 1024) // buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }

            showToast("Downloaded to Downloads:\n${file.absolutePath}")
            binding.viewfile.visibility = View.VISIBLE
            binding.viewfile.setOnClickListener {
                openPdfInSystemViewer(file)
            }
            openDownloadsFolder()
        } catch (e: Exception) {
            println(e.message)
            showToast("Error saving file: ${e.message}")
        }
    }


    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == state
    }

    private fun openPdfInSystemViewer(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireActivity().packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(Intent.createChooser(intent, "Open PDF"))
        } catch (e: Exception) {
            showToast("No PDF viewer found: ${e.message}")
        }
    }

    private fun openDownloadsFolder() {
        try {
            startActivity(Intent(android.app.DownloadManager.ACTION_VIEW_DOWNLOADS))
        } catch (_: Exception) {
            showToast("Open the Downloads folder to view the PDF")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun isStoragePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (!isStoragePermissionGranted()) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DownloadQrFragment", "Storage permission granted")
                downloadEquipment()
            } else {
                Log.d("DownloadQrFragment", "Storage permission denied")
                showToast("Storage permission denied")
            }
        }
    }

    private fun downloadEquipment() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (isStoragePermissionGranted()) {
                binding.viewfile.visibility = View.GONE
                val object1 = JsonObject()
                object1.addProperty("company_link", company_link_id)
                viewModel.downloadAllEquipment(object1, token!!)
            } else {
                requestStoragePermission()
            }
        } else {
            binding.viewfile.visibility = View.GONE
            val object1 = JsonObject()
            object1.addProperty("company_link", company_link_id)
            viewModel.downloadAllEquipment(object1, token!!)
        }

    }
}
