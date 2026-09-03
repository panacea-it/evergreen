package com.prod.evergreen.helper

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.prod.evergreen.models.ChangePasswordData
import com.prod.evergreen.models.TaskCreated

object ServiceReportHelper {
    fun taskId(task: TaskCreated?): Int {
        if (task == null) return 0
        return listOfNotNull(task.taskLink, task.task?.id, task.id).firstOrNull { it != 0 } ?: 0
    }

    fun offer(context: Context, data: ChangePasswordData?) {
        if (data == null) return
        if (data.status_code == 200 && !data.pdf_base64.isNullOrBlank()) {
            offerLocal(context, data.pdf_base64)
            return
        }
        if (data.status_code == 200 && !data.url.isNullOrBlank()) {
            offerUrl(context, MediaUrl.resolve(data.url))
            return
        }
        Toast.makeText(context, data.message ?: "Unable to download report", Toast.LENGTH_SHORT).show()
    }

    private fun offerLocal(context: Context, base64: String) {
        try {
            val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val file = java.io.File(context.cacheDir, "evergreen_service_report.pdf")
            file.writeBytes(bytes)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            AlertDialog.Builder(context)
                .setTitle("Service Report")
                .setMessage("The report is ready. You can open or share the PDF.")
                .setPositiveButton("Open") { _, _ ->
                    val open = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(open, "Open service report"))
                }
                .setNeutralButton("Share") { _, _ ->
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Evergreen Service Report")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(share, "Share service report"))
                }
                .setNegativeButton("Close", null)
                .show()
        } catch (error: Exception) {
            Toast.makeText(context, error.message ?: "Unable to open service report", Toast.LENGTH_SHORT).show()
        }
    }

    private fun offerUrl(context: Context, pdfUrl: String) {
        AlertDialog.Builder(context)
            .setTitle("Service Report")
            .setMessage("The report is ready. You can open or share the PDF.")
            .setPositiveButton("Open") { _, _ ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = android.net.Uri.parse(pdfUrl) })
                } catch (_: Exception) {
                    Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Share") { _, _ ->
                val share = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, "Evergreen Service Report")
                    putExtra(Intent.EXTRA_TEXT, pdfUrl)
                }
                context.startActivity(Intent.createChooser(share, "Share service report"))
            }
            .setNegativeButton("Close", null)
            .show()
    }
}
