package com.prod.evergreen.activities

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.app.ui.qr.IndividualQrScreen
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class IndividualQRDownloader : AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }

    private val qrBitmapState = mutableStateOf<Bitmap?>(null)
    private val serialState = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val equipment = intent.getIntExtra("eq_id", intent.getIntExtra("id", 0))
        val serial = intent.getStringExtra("eq_sn").orEmpty()
        serialState.value = serial
        val payload = if (equipment > 0) equipment.toString() else serial
        qrBitmapState.value = if (payload.isNotBlank()) {
            generateQRCodeWithSerialNumber(payload, serial.ifBlank { payload }, 600, 600)
        } else {
            null
        }

        setContent {
            IndividualQrScreen(
                serialNumber = serialState.value.ifBlank { if (equipment > 0) "ID $equipment" else "" },
                qrBitmap = qrBitmapState.value,
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onDownloadClick = { saveCurrentQr(equipment, serial) }
            )
        }
    }

    private fun saveCurrentQr(equipment: Int, serial: String) {
        val bitmap = qrBitmapState.value ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission()) {
            requestStoragePermission()
            return
        }
        val name = "qr_code_${if (equipment > 0) equipment else "eq"}_${serial.ifBlank { "code" }}"
        saveImageToGallery(bitmap, name)
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val equipment = intent.getIntExtra("eq_id", intent.getIntExtra("id", 0))
                val serial = intent.getStringExtra("eq_sn").orEmpty()
                saveCurrentQr(equipment, serial)
            } else {
                Toast.makeText(this, "Storage permission denied. Cannot save QR code.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateQRCodeWithSerialNumber(data: String, serialNumber: String, width: Int, height: Int): Bitmap? {
        val qrBitmap = generateQRCode(data, width, height) ?: return null
        val combinedHeight = height + 80
        val combinedBitmap = Bitmap.createBitmap(width, combinedHeight, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(combinedBitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawBitmap(qrBitmap, 0f, 0f, null)
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 36f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(serialNumber, (width / 2).toFloat(), (height + 48).toFloat(), paint)
        return combinedBitmap
    }

    private fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
        val bitMatrix: BitMatrix
        try {
            bitMatrix = MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, width, height)
        } catch (e: WriterException) {
            e.printStackTrace()
            return null
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    private fun saveImageToGallery(bitmap: Bitmap, fileName: String) {
        val filename = "$fileName.png"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(image)
                sendBroadcast(intent)
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Toast.makeText(this@IndividualQRDownloader, "QR Code saved successfully!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this@IndividualQRDownloader, "Failed to save QR Code.", Toast.LENGTH_SHORT).show()
        }
    }
}
