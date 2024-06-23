package com.yeloe.attentanceapp.ui.activity.face_detector

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.Event.Application.Execution
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.ActivityFaceDetectionBinding
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.view_model.face_detection.CameraXViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.math.max
import kotlin.math.min
import java.util.concurrent.Executors

class FaceDetectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaceDetectionBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Wait for 3 seconds (for example) before starting face detection
        lifecycleScope.launch {
            delay(1000) // Wait for 3000 milliseconds (3 seconds)
            startFaceDetection()
        }
    }

    private fun startFaceDetection() {
        try {
            cameraSelector =
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT).build()
            cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
                processCameraProvider = provider
                bindCameraPreview()
                bindInputAnalyser()
            }
        }catch (e: Exception){
            Log.d(Constant.FACE_DETECTION_LOG,"Error $e")
        }
    }

    private fun bindCameraPreview() {
        cameraPreview = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()
        cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(
                Constant.FACE_DETECTION_LOG,
                illegalStateException.message ?: "IllegalStateException"
            )
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(
                Constant.FACE_DETECTION_LOG,
                illegalArgumentException.message ?: "IllegalArgumentException"
            )
        }
    }

    private fun bindInputAnalyser() {
        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                .build()
        )
        imageAnalysis = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .build()

        val cameraExecutor = Executors.newSingleThreadExecutor()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(detector, imageProxy)
        }

        try {
            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(
                Constant.FACE_DETECTION_LOG,
                illegalStateException.message ?: "IllegalStateException"
            )
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(
                Constant.FACE_DETECTION_LOG,
                illegalArgumentException.message ?: "IllegalArgumentException"
            )
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    private fun processImageProxy(detector: FaceDetector, imageProxy: ImageProxy) {
        try {
            if (imageProxy.image != null) {
                val inputImage =
                    InputImage.fromMediaImage(
                        imageProxy.image!!,
                        imageProxy.imageInfo.rotationDegrees
                    )

                detector.process(inputImage).addOnSuccessListener { faces ->

                    binding.graphicOverlay.clear()
                    faces.forEach { face ->

//                        val image = imageProxy.toBitmap()
                        val faceBox =
                            FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect)
                        binding.graphicOverlay.add(faceBox)


                        val faceBound = face.boundingBox
                        // Crop the face region from the original image
                        val croppedFaceBitmap =
                            performFaceRecognition(faceBound, imageProxy.toBitmap(), imageProxy.imageInfo.rotationDegrees)
                        sendFaceImage(croppedFaceBitmap)

                    }
                }.addOnFailureListener {
                    Log.d(Constant.FACE_DETECTION_LOG, "Error on Face Detection $it")
                }.addOnCompleteListener {
                    imageProxy.close()

                }
            }
        } catch (e: Exception) {
            Log.d(Constant.FACE_DETECTION_LOG, "Error on Face Detection $e")
        }

    }


    private fun performFaceRecognition(bound: Rect, image: Bitmap, rotationDegrees: Int): Bitmap {
        return try {

            val rotatedBitmap = rotateBitmap(image, rotationDegrees)

            // Ensure the bounding rectangle is within the bounds of the image
            val cropLeft = max(0, bound.left)
            val cropTop = max(0, bound.top)
            val cropRight = min(rotatedBitmap.width, bound.right)
            val cropBottom = min(rotatedBitmap.height, bound.bottom)

            // Crop the face region from the rotated image
            Bitmap.createBitmap(rotatedBitmap, cropLeft, cropTop, cropRight - cropLeft, cropBottom - cropTop)
        } catch (e: Exception) {
            // Handle any exceptions
            Log.e(Constant.FACE_DETECTION_LOG, "Error cropping face: $e")
            // Return an empty bitmap or null as desired
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Int): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }



    private fun sendFaceImage(faceBitmap: Bitmap) {
        try {
            onFaceImage?.invoke(faceBitmap)
            onFaceImage = null
            finish()
        } catch (e: Exception) {
            Log.d(Constant.FACE_DETECTION_LOG, "Error on sending face bitmap $e")
        }
    }


//    private fun performFaceRecognition(bound: Rect, image: Bitmap) {
//        try {
//            if (bound.top < 0) {
//                bound.top = 0
//            }
//            if (bound.left < 0) {
//                bound.left = 0
//            }
//            if (bound.right > bound.width()) {
//                bound.right = bound.width() - 1
//            }
//            if (bound.bottom > bound.height()) {
//                bound.bottom = bound.height() - 1
//            }
//            val cropImage =
//                Bitmap.createBitmap(image, bound.left, bound.top, bound.width(), bound.height())
//            Log.d(Constant.FACE_DETECTION_LOG, "crop successfully...")
//            sendFaceImage(cropImage)
//
//        } catch (e: Exception) {
//            Log.d(Constant.FACE_DETECTION_LOG, "Error on Face Detection $e")
//        }
//
//    }

    companion object {
        private val TAG = FaceDetectionActivity::class.simpleName
        private var onFaceImage: ((image: Bitmap) -> Unit)? = null
        fun startActivity(context: Context, onFaceImage: ((image: Bitmap) -> Unit)) {
            this.onFaceImage = onFaceImage
            Intent(context, FaceDetectionActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }

}