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
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.prod.evergreen.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class IndividualQRDownloader : AppCompatActivity() {

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_individual_qr_downloader)

        val equipment = intent.getIntExtra("eq_id", 0)
        val eq_sn = intent.getStringExtra("eq_sn")

        val imageView = findViewById<ImageView>(R.id.qrCodeImageView)
        val downloadqr = findViewById<ImageView>(R.id.downloadqr)
        val back = findViewById<ImageView>(R.id.back)

        back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Generate the QR code with serial number
        val qrCodeBitmap = eq_sn?.let { generateQRCodeWithSerialNumber(equipment.toString(), it, 600, 600) }

        // Set the QR code to the ImageView
        qrCodeBitmap?.let {
            imageView.setImageBitmap(it)
        }

        // Handle download button click
        downloadqr.setOnClickListener {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !hasStoragePermission()) {
                // Request storage permission for Android below 10
                requestStoragePermission()
            } else {
                // Save QR code if permission is granted or not needed
                qrCodeBitmap?.let { bitmap ->
                    saveImageToGallery(bitmap, "qr_code_${equipment}_$eq_sn")
                }
            }
        }
    }

    // Check if storage permission is granted
    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Request storage permission
    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_CODE
        )
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with saving the image
                val equipment = intent.getIntExtra("eq_id", 0)
                val eq_sn = intent.getStringExtra("eq_sn")
                val qrCodeBitmap = eq_sn?.let { generateQRCodeWithSerialNumber(equipment.toString(), it, 600, 600) }
                qrCodeBitmap?.let { bitmap ->
                    saveImageToGallery(bitmap, "qr_code_${equipment}_$eq_sn")
                }
            } else {
                // Permission denied, show a message
                Toast.makeText(this, "Storage permission denied. Cannot save QR code.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Generate QR code and combine with serial number as a single Bitmap
    fun generateQRCodeWithSerialNumber(data: String, serialNumber: String, width: Int, height: Int): Bitmap? {
        val qrBitmap = generateQRCode(data, width, height + 20)
        val combinedHeight = height + 20 // Add more space below the serial number
        val combinedBitmap = Bitmap.createBitmap(width, combinedHeight, Bitmap.Config.ARGB_8888)

        val canvas = android.graphics.Canvas(combinedBitmap)

        // Draw the QR code
        qrBitmap?.let {
            canvas.drawBitmap(qrBitmap, 0f, 0f, null)
        }

        // Draw the serial number below the QR code
        val paint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 50f // Adjust text size as needed
            textAlign = android.graphics.Paint.Align.CENTER
        }

        // Draw text with some space below the QR code
        canvas.drawText(serialNumber, (width / 2).toFloat(), (height - 15).toFloat(), paint)

        return combinedBitmap
    }

    // Generate QR code as a Bitmap
    fun generateQRCode(data: String, width: Int, height: Int): Bitmap? {
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

    // Save the combined image (QR code + serial number) to the gallery
    fun saveImageToGallery(bitmap: Bitmap, fileName: String) {
        val filename = "$fileName.png"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // For Android 10 and above
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                // For Android below 10
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)

                // Notify Gallery about the new image
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                intent.data = Uri.fromFile(image)
                sendBroadcast(intent)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }

            // Show success message
            Toast.makeText(this@IndividualQRDownloader, "QR Code saved successfully!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            // Show error message
            Toast.makeText(this@IndividualQRDownloader, "Failed to save QR Code.", Toast.LENGTH_SHORT).show()
        }
    }
}
