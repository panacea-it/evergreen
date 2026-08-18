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
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer

import com.prod.evergreen.databinding.ActivityQrScannerBinding
import com.prod.evergreen.helper.BarcodeAnalyzer
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
private const val REQUEST_CODE_PERMISSIONS = 10
private val REQUIRED_PERMISSIONS = arrayOf(android.Manifest.permission.CAMERA)

class QrScanner : AppCompatActivity() {

    private val multiFormatReader = MultiFormatReader()
    private lateinit var binding: ActivityQrScannerBinding
    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var isFlashOn = false
    private var hasNavigated = false  // Add this flag

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.back.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.tvFlash.setOnClickListener {
            toggleFlashlight()
        }
        binding.tvPickFrom.setOnClickListener {
            openGallery()
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
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

        val preview = Preview.Builder()
            .build()


        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetResolution(Size(binding.previeView.width, binding.previeView.height))
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
        preview.setSurfaceProvider(binding.previeView.createSurfaceProvider(camera!!.cameraInfo))
    }

    private fun toggleFlashlight() {
        val cameraControl = camera?.cameraControl
        isFlashOn = !isFlashOn
        cameraControl?.enableTorch(isFlashOn)
        binding.tvFlash.text = if (isFlashOn) "Turn off flash" else "Turn on flash"
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

    val pickImageFromGalleryForResult =
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

        // Convert the bitmap to a software-backed bitmap
        val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        val width = softwareBitmap.width
        val height = softwareBitmap.height
        val pixels = IntArray(width * height)
        softwareBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val source = RGBLuminanceSource(width, height, pixels)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        try {
            val result: Result = multiFormatReader.decode(binaryBitmap)
            val qrText = result.text
            handleQrCodeData(qrText)
        } catch (e: NotFoundException) {
            Toast.makeText(this, "QR code not found", Toast.LENGTH_SHORT).show()
        } finally {
            // Recycle the software-backed bitmap to free up memory
            softwareBitmap.recycle()
        }
    }

    private fun handleQrCodeData(qrText: String) {
        if (!hasNavigated) {  // Check if navigation has already happened
            hasNavigated = true  // Set the flag to true to prevent future navigations
            startActivity(Intent(this@QrScanner, EquipmentDetails::class.java).putExtra("eq_id", qrText.toInt()).putExtra("screentype",1))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        // Release camera resources
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }
    }
}