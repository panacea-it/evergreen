package com.prod.evergreen.activities

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.prod.evergreen.XApplication
import com.prod.evergreen.R
import com.prod.evergreen.adapters.EquipmentUploadReviewAdapter
import com.prod.evergreen.api.MainRepository
import com.prod.evergreen.api.MainViewModel
import com.prod.evergreen.api.MyViewModelFactory
import com.prod.evergreen.api.RetrofitService
import com.prod.evergreen.databinding.ActivityBulkEquipmentUploadBinding
import com.prod.evergreen.helper.ConstantValues
import com.prod.evergreen.helper.EquipmentWorkbookRecord
import com.prod.evergreen.helper.EquipmentWorkbookReview
import com.prod.evergreen.helper.ProgressDialogUtil
import com.prod.evergreen.helper.SharedPreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class BulkEquipmentUploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBulkEquipmentUploadBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var adapter: EquipmentUploadReviewAdapter
    private lateinit var companyName: String
    private var companyId: Int = 0
    private var selectedFileName: String? = null
    private var uploadFile: File? = null
    private var isUploading = false

    private val pickWorkbook = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let(::readWorkbook)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBulkEquipmentUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getIntExtra(EXTRA_COMPANY_ID, 0)
        companyName = intent.getStringExtra(EXTRA_COMPANY_NAME).orEmpty()
        if (companyId == 0) {
            Toast.makeText(this, "Company could not be identified", Toast.LENGTH_LONG).show()
            finish()
            return
        }
        binding.tvCompanyName.text = "Company: $companyName"
        binding.btnBack.setOnClickListener { finish() }
        binding.btnChooseFile.setOnClickListener {
            pickWorkbook.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        }
        binding.btnDownloadTemplate.setOnClickListener { downloadTemplate() }

        adapter = EquipmentUploadReviewAdapter(::showEditDialog)
        binding.rvEquipmentPreview.layoutManager = LinearLayoutManager(this)
        binding.rvEquipmentPreview.adapter = adapter
        binding.btnSubmitUpload.isEnabled = false
        binding.btnSubmitUpload.setOnClickListener { submitReviewedWorkbook() }

        setViewModel()
        viewModel.loading.observe(this) { loading ->
            if (loading) ProgressDialogUtil.showProgressDialog(this, "Loading")
            else ProgressDialogUtil.hideProgressDialog()
        }
        viewModel.imageUploadDataResponse.observe(this) { response ->
            if (!isUploading) return@observe
            isUploading = false
            uploadFile?.delete()
            uploadFile = null
            val details = buildString {
                append(response.message ?: "Upload finished")
                response.errors.orEmpty().takeIf { it.isNotEmpty() }?.let { errors ->
                    append("\n\n")
                    append(errors.take(8).joinToString("\n"))
                    if (errors.size > 8) append("\n…and ${errors.size - 8} more")
                }
            }
            AlertDialog.Builder(this)
                .setTitle(if (response.status_code == 200) "Upload result" else "Upload failed")
                .setMessage(details)
                .setPositiveButton("Done") { _, _ ->
                    if (response.status_code == 200) {
                        setResult(RESULT_OK)
                        finish()
                    } else {
                        binding.btnSubmitUpload.isEnabled = adapter.itemCount > 0
                    }
                }
                .show()
        }
        viewModel.errorMessage.observe(this) { message ->
            if (isUploading) {
                isUploading = false
                uploadFile?.delete()
                uploadFile = null
                binding.btnSubmitUpload.isEnabled = adapter.itemCount > 0
                Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setViewModel() {
        val repository = MainRepository(
            RetrofitService.getInstance(this),
            XApplication.database.newsDao(),
            XApplication.database.companyDao()
        )
        viewModel = ViewModelProvider(this, MyViewModelFactory(repository))[MainViewModel::class.java]
    }

    private fun readWorkbook(uri: Uri) {
        binding.btnSubmitUpload.isEnabled = false
        binding.tvPreviewCount.text = "Reading Excel rows…"
        lifecycleScope.launch {
            try {
                val records = withContext(Dispatchers.IO) {
                    EquipmentWorkbookReview.read(this@BulkEquipmentUploadActivity, uri)
                }
                if (records.isEmpty()) error("The worksheet has no equipment rows")
                adapter.submit(records)
                selectedFileName = getFileName(uri) ?: "Equipment workbook"
                binding.tvFileName.text = selectedFileName
                binding.tvPreviewCount.text = "${records.size} equipment row${if (records.size == 1) "" else "s"} extracted. Review and edit before uploading."
                binding.tvEmptyPreview.visibility = View.GONE
                binding.rvEquipmentPreview.visibility = View.VISIBLE
                binding.btnSubmitUpload.isEnabled = true
                binding.btnSubmitUpload.alpha = 1f
            } catch (error: Exception) {
                selectedFileName = null
                binding.tvFileName.text = ""
                binding.tvPreviewCount.text = "Choose an Excel file to preview equipment rows"
                binding.tvEmptyPreview.visibility = View.VISIBLE
                binding.rvEquipmentPreview.visibility = View.GONE
                Toast.makeText(this@BulkEquipmentUploadActivity, error.message ?: "Unable to read this Excel file", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showEditDialog(position: Int) {
        if (position !in 0 until adapter.itemCount) return
        val current = adapter.recordAt(position)
        val fields = listOf(
            "Equipment name *" to current.name,
            "Make *" to current.make,
            "Model *" to current.model,
            "Specifications" to current.specifications,
            "Location" to current.location,
            "Description" to current.description,
            "Maintenance frequency" to current.tmFrequency,
            "Manufacturer date (YYYY-MM-DD)" to current.manufacturerDate,
            "Evergreen serial number" to current.egSerialNumber
        )
        val inputs = fields.map { (hint, value) ->
            EditText(this).apply {
                this.hint = hint
                setText(value)
                setSingleLine(true)
                textSize = 16f
                setPadding(dp(12), dp(10), dp(12), dp(10))
            }
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(4), dp(20), dp(4))
            inputs.forEach { input ->
                addView(input, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) })
            }
        }
        val scroll = ScrollView(this).apply { addView(content) }
        AlertDialog.Builder(this)
            .setTitle("Edit equipment row ${position + 1}")
            .setView(scroll)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save changes") { _, _ ->
                adapter.update(
                    position,
                    EquipmentWorkbookRecord(
                        name = inputs[0].text.toString().trim(),
                        make = inputs[1].text.toString().trim(),
                        model = inputs[2].text.toString().trim(),
                        specifications = inputs[3].text.toString().trim(),
                        location = inputs[4].text.toString().trim(),
                        description = inputs[5].text.toString().trim(),
                        tmFrequency = inputs[6].text.toString().trim(),
                        manufacturerDate = inputs[7].text.toString().trim(),
                        egSerialNumber = inputs[8].text.toString().trim()
                    )
                )
            }
            .show()
    }

    private fun submitReviewedWorkbook() {
        if (isUploading) return
        val token = SharedPreferencesHelper(this).getValueString(ConstantValues.AuthToken)
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }
        val records = adapter.allRecords()
        val invalidRows = records.mapIndexedNotNull { index, record ->
            if (record.name.isBlank() || record.make.isBlank() || record.model.isBlank()) index + 1 else null
        }
        if (invalidRows.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Required fields missing")
                .setMessage("Add equipment name, make, and model for row${if (invalidRows.size == 1) "" else "s"} ${invalidRows.joinToString()}.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        try {
            val editedFile = File.createTempFile("equipment_review_", ".xlsx", cacheDir)
            EquipmentWorkbookReview.write(this, records, editedFile)
            uploadFile = editedFile
            val filePart = MultipartBody.Part.createFormData(
                "file", selectedFileName ?: "equipment_upload.xlsx",
                editedFile.asRequestBody("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".toMediaType())
            )
            val companyPart = companyId.toString().toRequestBody("text/plain".toMediaType())
            isUploading = true
            binding.btnSubmitUpload.isEnabled = false
            viewModel.upLoadFile(filePart, token, companyPart)
        } catch (error: Exception) {
            uploadFile?.delete()
            uploadFile = null
            binding.btnSubmitUpload.isEnabled = true
            Toast.makeText(this, error.message ?: "Unable to prepare the Excel file", Toast.LENGTH_LONG).show()
        }
    }

    private fun downloadTemplate() {
        try {
            val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloads.exists()) downloads.mkdirs()
            val file = File(downloads, "evergreen_equipment_template.xlsx")
            assets.open("evergreen_equipment_template.xlsx").use { input ->
                FileOutputStream(file).use { output -> input.copyTo(output) }
            }
            Toast.makeText(this, "Template saved to Downloads", Toast.LENGTH_LONG).show()
        } catch (error: Exception) {
            Toast.makeText(this, error.message ?: "Unable to save the template", Toast.LENGTH_LONG).show()
        }
    }

    private fun getFileName(uri: Uri): String? = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index >= 0) cursor.getString(index) else null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        uploadFile?.delete()
        uploadFile = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_COMPANY_ID = "bulk_company_id"
        const val EXTRA_COMPANY_NAME = "bulk_company_name"
    }
}
