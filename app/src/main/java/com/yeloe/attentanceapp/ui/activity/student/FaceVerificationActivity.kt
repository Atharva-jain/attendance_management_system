package com.yeloe.attentanceapp.ui.activity.student

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.yeloe.attentanceapp.databinding.ActivityFaceVerificationBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.repo.AttendanceRepository
import com.yeloe.attentanceapp.ui.activity.face_detector.FaceBox
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier.Recognition
import com.yeloe.attentanceapp.ui.face_classification.TFLiteFaceRecognition
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Constant.TF_OD_API_INPUT_SIZE2
import com.yeloe.attentanceapp.utils.ShowToast
import com.yeloe.attentanceapp.view_model.AttendanceViewModel
import com.yeloe.attentanceapp.view_model.face_detection.CameraXViewModel
import com.yeloe.attentanceapp.view_model.factory.AttendanceViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.min


class FaceVerificationActivity : AppCompatActivity() {

    lateinit var binding: ActivityFaceVerificationBinding
    private lateinit var cameraSelector: CameraSelector
    private lateinit var processCameraProvider: ProcessCameraProvider
    private lateinit var cameraPreview: Preview
    private lateinit var imageAnalysis: ImageAnalysis
    lateinit var mAttendanceViewModel: AttendanceViewModel
    private lateinit var faceClassifier: FaceClassifier

    private val cameraXViewModel = viewModels<CameraXViewModel>()

    private fun setVisibilityOfMultipleFaces(value: Boolean) {
        if (value) {
            binding.multipleFacesCardView.visibility = View.VISIBLE
        } else {
            binding.multipleFacesCardView.visibility = View.INVISIBLE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaceVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AttendanceRepository()
        val viewModelProviderFactory = AttendanceViewModelFactory(application, repository)
        mAttendanceViewModel = ViewModelProvider(
            this, viewModelProviderFactory
        )[AttendanceViewModel::class.java]

        try {
            faceClassifier = TFLiteFaceRecognition.create(
                assets,
                Constant.MODEL_NAME,
                TF_OD_API_INPUT_SIZE2,
                false,
                applicationContext,
                mFaces
            )
        } catch (e: IOException) {
            //e.printStackTrace()
            val toast = Toast.makeText(
                applicationContext, "Classifier could not be initialized", Toast.LENGTH_SHORT
            )
            toast.show()
            finish()
        }

        mAttendanceViewModel.mFaceVerifyState.observe(this) {
            try {

                if (it) {
                    Log.d(Constant.STUDENT_LOG, "Face Detected")
                    onFaceVerified?.invoke(it)

                    finish()
                } else {
                    Log.d(Constant.STUDENT_LOG, "Face Not Detected")
                    onFaceVerified?.invoke(it)

                    finish()
                }
            } catch (e: Exception) {

            }
        }

        //Fetch the face image asynchronously
//        lifecycleScope.launch {
//            try {
//                // This suspending function fetches the image from URL asynchronously
//                mFaceImage = withContext(Dispatchers.IO) {
//                    GetBitmapFromUrl.getBitmapFromURL(mJoinedClassroom.studentFaceImage)
//                }
//
//            } catch (e: Exception) {
//                Log.d(Constant.STUDENT_LOG, "Unable to get your face image. $e")
//                ShowToast.showToast(this@FaceVerificationActivity, "Unable to get your face image.")
//            }
//
//            // Wait for 3 seconds (for example) before starting face detection
//            delay(3000) // Wait for 3000 milliseconds (3 seconds)
//
//        }


//         Wait for 3 seconds (for example) before starting face detection
        lifecycleScope.launch {
            // Wait for 3000 milliseconds (3 seconds)
            startFaceDetection()
            delay(1000)
        }


    }

    override fun onStart() {
        super.onStart()
        //startFaceDetection()
    }

    private fun startFaceDetection() {
        try {
            cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()

            cameraXViewModel.value.processCameraProvider.observe(this) { provider ->
                processCameraProvider = provider
                bindCameraPreview()
                bindInputAnalyser()
            }
        } catch (e: Exception) {
            Log.e(Constant.FACE_DETECTION_LOG, "Error starting face detection: $e")
        }
    }

    private fun bindCameraPreview() {
        try {
            cameraPreview = if (binding.previewView.display?.rotation != null) {
                Preview.Builder()
                    .setTargetRotation(binding.previewView.display.rotation)
                    .build()
            } else {
                Preview.Builder()
                    .build()
            }
            cameraPreview.setSurfaceProvider(binding.previewView.surfaceProvider)
            processCameraProvider.bindToLifecycle(this, cameraSelector, cameraPreview)
        } catch (e: Exception) {
            Log.e(Constant.FACE_DETECTION_LOG, "Error binding camera preview: $e")
        }
    }


    private fun bindInputAnalyser() {
        try {
            val detector = FaceDetection.getClient(
                FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
                    .build()
            )

            imageAnalysis = if (binding.previewView.display?.rotation != null) {
                ImageAnalysis.Builder()
                    .setTargetRotation(binding.previewView.display.rotation)
                    .build()
            } else {
                ImageAnalysis.Builder()
                    .build()
            }
//            imageAnalysis = ImageAnalysis.Builder()
//                .setTargetRotation(binding.previewView.display.rotation)
//                .build()

            val cameraExecutor = Executors.newSingleThreadExecutor()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(detector, imageProxy)
            }

            processCameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
        } catch (e: Exception) {
            Log.e(Constant.FACE_DETECTION_LOG, "Error binding input analyzer: $e")
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

                    if (faces.size == 1) {
                        setVisibilityOfMultipleFaces(false)
                        binding.graphicOverlay.clear()
                        faces.forEach { face ->

//                        val image = imageProxy.toBitmap()
                            val faceBox =
                                FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect)
                            binding.graphicOverlay.add(faceBox)

                            val faceBound = face.boundingBox
                            // Crop the face region from the original image
                            val croppedFaceBitmap =
                                performFaceRecognition(
                                    faceBound,
                                    imageProxy.toBitmap(),
                                    imageProxy.imageInfo.rotationDegrees
                                )

                            classifyFace(croppedFaceBitmap)

//                        if (mFaceImage != null) {
//                            verifyFace(mFaceImage!!, croppedFaceBitmap)
//                        }

//                        mAttendanceViewModel.verifyFace(
//                            this@FaceVerificationActivity,
//                            mStudentFaceImage,
//                            croppedFaceBitmap
//                        )


                        }
                    } else {
                        setVisibilityOfMultipleFaces(true)
                        binding.graphicOverlay.clear()
                        faces.forEach { face ->
                            val faceBox =
                                FaceBox(binding.graphicOverlay, face, imageProxy.image!!.cropRect)
                            binding.graphicOverlay.add(faceBox)
                        }
                    }

                }.addOnFailureListener {
                    Log.d(Constant.FACE_DETECTION_LOG, "Error on Face Detection listener $it")
                }.addOnCompleteListener {
                    imageProxy.close()
                }
            }
        } catch (e: Exception) {
            Log.d(Constant.FACE_DETECTION_LOG, "Error on Face Detection $e")
        }
    }

    private fun classifyFace(faceImage: Bitmap) {
        if (faceClassifier != null) {
            val scaledImage = Bitmap.createScaledBitmap(
                faceImage,
                TF_OD_API_INPUT_SIZE2,
                TF_OD_API_INPUT_SIZE2,
                false
            );
            val result: Recognition =
                faceClassifier.recognizeImage(scaledImage, false)
            var title: String? = "Unknown"
            if (result != null) {
                if (result.distance < 0.75f) {
                    title = result.title
                    if (title == mJoinedClassroom.uid) {
                        Log.d(Constant.STUDENT_LOG, "Face Detected")
                        onFaceVerified?.invoke(true)
                        finish()
                    } else {
                        Log.d(Constant.STUDENT_LOG, "Face not Detected")
                        onFaceVerified?.invoke(false)
                        finish()
                    }
                } else {
                    Log.d(Constant.STUDENT_LOG, "Face not Detected")
                    onFaceVerified?.invoke(false)
                    finish()
                }
            }
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
            Bitmap.createBitmap(
                rotatedBitmap,
                cropLeft,
                cropTop,
                cropRight - cropLeft,
                cropBottom - cropTop
            )
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

    companion object {
        private val TAG = FaceVerificationActivity::class.simpleName
        private var mClass: Class = Class()
        private var mJoinedClassroom = JoinClassroom()
        private var onFaceVerified: ((verified: Boolean) -> Unit)? = null
        private lateinit var mStudentFaceImage: Bitmap
        private lateinit var mFaces: HashMap<String, FaceClassifier.Recognition>
        fun startActivity(
            context: Context,
            classes: Class,
            joinedClassroom: JoinClassroom,
            studentFaceImage: Bitmap,
            faces: HashMap<String, FaceClassifier.Recognition>,
            onFaceVerified: ((verified: Boolean) -> Unit)
        ) {
            this.onFaceVerified = onFaceVerified
            this.mClass = classes
            this.mJoinedClassroom = joinedClassroom
            this.mStudentFaceImage = studentFaceImage
            this.mFaces = faces
            Intent(context, FaceVerificationActivity::class.java).also {
                context.startActivity(it)
            }
        }
    }

}