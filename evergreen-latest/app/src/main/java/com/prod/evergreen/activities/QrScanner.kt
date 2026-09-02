package com.prod.evergreen.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.app.ui.scan.ScanCodeScreen
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import com.prod.evergreen.helper.BarcodeAnalyzer
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

class QrScanner : AppCompatActivity() {

    private val multiFormatReader = MultiFormatReader()
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private val flashOnState = mutableStateOf(false)
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraExecutor = Executors.newSingleThreadExecutor()
        previewView = PreviewView(this).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }

        setContent {
            val flashOn by flashOnState
            ScanCodeScreen(
                onBackClick = { onBackPressedDispatcher.onBackPressed() },
                onFlashClick = { toggleFlashlight() },
                onPickPhotoClick = { openGallery() },
                flashOn = flashOn,
                cameraPreview = {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
        cameraProvider?.unbindAll()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases() {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val preview = Preview.Builder().build()
        val analysisWidth = previewView.width.takeIf { it > 0 } ?: 1280
        val analysisHeight = previewView.height.takeIf { it > 0 } ?: 720

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(analysisWidth, analysisHeight))
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { qrCodes ->
                    qrCodes.firstOrNull()?.let { qrCode ->
                        handleQrCodeData(qrCode.rawValue!!)
                    }
                })
            }

        cameraProvider?.unbindAll()
        camera = cameraProvider?.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera!!.cameraInfo))
    }

    private fun toggleFlashlight() {
        val next = !flashOnState.value
        flashOnState.value = next
        camera?.cameraControl?.enableTorch(next)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun openGallery() {
        val pickIntent = Intent(Intent.ACTION_PICK)
        pickIntent.setDataAndType(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            "image/*"
        )
        pickImageFromGalleryForResult.launch(pickIntent)
    }

    private val pickImageFromGalleryForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                if (intent != null) {
                    val uri = intent.data
                    if (uri != null) {
                        try {
                            val bitmap = if (Build.VERSION.SDK_INT < 28) {
                                MediaStore.Images.Media.getBitmap(contentResolver, uri)
                            } else {
                                val source = ImageDecoder.createSource(contentResolver, uri)
                                ImageDecoder.decodeBitmap(source)
                            }
                            decodeQrCode(bitmap)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } catch (e: OutOfMemoryError) {
                            e.printStackTrace()
                            Toast.makeText(this, "Out of memory", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

    private fun decodeQrCode(bitmap: Bitmap?) {
        if (bitmap == null) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            return
        }

        val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = softwareBitmap.width
        val height = softwareBitmap.height
        val pixels = IntArray(width * height)
        softwareBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result: Result = multiFormatReader.decode(binaryBitmap)
            handleQrCodeData(result.text)
        } catch (e: NotFoundException) {
            Toast.makeText(this, "QR code not found", Toast.LENGTH_SHORT).show()
        } finally {
            softwareBitmap.recycle()
        }
    }

    private fun handleQrCodeData(qrText: String) {
        if (!hasNavigated) {
            hasNavigated = true
            startActivity(
                Intent(this@QrScanner, EquipmentDetails::class.java)
                    .putExtra("eq_id", qrText.toInt())
                    .putExtra("screentype", 1)
            )
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS && allPermissionsGranted()) {
            startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        if (!cameraExecutor.isShutdown) {
            cameraExecutor.shutdown()
        }
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        if (cameraExecutor.isShutdown) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }
}
