package com.yeloe.attentanceapp.ui.fragment.student.classes

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentMarkedAttendanceDialogBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.activity.student.map.AttendanceMapActivity
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import com.yeloe.attentanceapp.utils.cameraPermissionRequest
import com.yeloe.attentanceapp.utils.isPermissionGranted
import com.yeloe.attentanceapp.utils.openPermissionSetting
import java.lang.Exception

class MarkedAttendanceDialogFragment(
    val classes: Class, val joinClassroom: JoinClassroom, val listener: OnMarkedAttendanceInterface
) : BottomSheetDialogFragment() {

    private val cameraPermission = android.Manifest.permission.CAMERA

    private var mFacesHash: HashMap<String, FaceClassifier.Recognition> = HashMap()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

    private var _binding: FragmentMarkedAttendanceDialogBinding? = null
    private val binding get() = _binding!!

    private var mIsFacedVerified = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarkedAttendanceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun setFaceVerificationEnability(value: Boolean) {
        binding.verifyImageMaterialCardView.isEnabled = value
    }

    private fun setProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.facesLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.facesLinearProgressIndicator.visibility = View.GONE
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(Constant.REPOSITORY_LOG, "uid ${joinClassroom.uid}")

        (activity as AttendanceMapActivity).mAttendanceViewModel.getFaces(joinClassroom.uid)

        setFaceVerificationEnability(false)

        (activity as AttendanceMapActivity).mAttendanceViewModel.mFacesDataState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            val faces = response.data
                            Log.d(Constant.STUDENT_LOG, "\n $faces")
                            if (faces != null) {

                                Log.d(Constant.REPOSITORY_LOG,"Faces : $faces ${faces.size} ")

                                faces.forEach {
                                    Log.d(Constant.REPOSITORY_LOG,"Faces Name : $faces ${it.key} ")
                                    Log.d(Constant.REPOSITORY_LOG,"Faces value : $faces ${it.value} ")
                                    Log.d(Constant.REPOSITORY_LOG,"Faces value : ")
                                }

                                (activity as AttendanceMapActivity).mAttendanceViewModel.setRegisteredRecognition(
                                    faces
                                )

                                mFacesHash = faces

                                (activity as AttendanceMapActivity).mAttendanceViewModel.mAddFacesState.postValue(
                                    Resources.Completed()
                                )

                                setFaceVerificationEnability(true)

                                setProgressBarVisibility(false)

                            } else {
                                ShowToast.showToast(
                                    requireContext(),
                                    "Unable get faces of user please try again later.."
                                )
                                setProgressBarVisibility(false)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.STUDENT_LOG, "Error $message")
                                ShowToast.showToast(requireContext(), message.toString())
                            }
                            setProgressBarVisibility(false)
                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                        }

                        else -> {
                            (activity as AttendanceMapActivity).mAttendanceViewModel.mAddFacesState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d(Constant.STUDENT_LOG, "Error $e")
                ShowToast.showToast(requireContext(), e.message.toString())
            }
        }

        binding.markedAttendanceButton.setOnClickListener {
            val verificationCode =
                binding.verificationCodeTextInputEditText.text.toString().trim()
            if (mIsFacedVerified) {
                if (validateVerificationCode(verificationCode)) {
                    if (classes.classCode == verificationCode) {
                        binding.verificationTextInputLayout.isErrorEnabled = false
                        listener.onMarkedAttendance()
                    } else {
                        binding.verificationTextInputLayout.isErrorEnabled = true
                        binding.verificationTextInputLayout.error = "Invalid verification code"
                    }
                }
            } else {
                ShowToast.showToast(requireContext(), "Please verified your face.")
            }
        }

        binding.verifyImageMaterialCardView.setOnClickListener {
            try {
                requestCameraAndStart()
            } catch (e: Exception) {

            }
        }

    }

    private fun validateVerificationCode(semester: String): Boolean {
        if (semester.isEmpty()) {
            binding.verificationTextInputLayout.isErrorEnabled = true
            binding.verificationTextInputLayout.error = "Please enter verification Code"
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun requestCameraAndStart() {
        if ((activity as AttendanceMapActivity).isPermissionGranted(cameraPermission)) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun startCamera() {
        listener.onStartCamera(mFacesHash)
    }

    fun setFaceVerifiedState(it: Boolean) {
        if (it) {
            binding.isFaceImageAddedImageView.visibility = View.VISIBLE
            binding.isFaceImageAddedImageView.setImageResource(R.drawable.right_correct)
            mIsFacedVerified = true
        } else {
            binding.isFaceImageAddedImageView.visibility = View.VISIBLE
            binding.isFaceImageAddedImageView.setImageResource(R.drawable.cancel)
            mIsFacedVerified = false
        }
    }


    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                (activity as AttendanceMapActivity).cameraPermissionRequest(positive = { (activity as AttendanceMapActivity).openPermissionSetting() })
            }

            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }

    }


}

interface OnMarkedAttendanceInterface {
    fun onMarkedAttendance()
    fun onStartCamera(faces: HashMap<String, FaceClassifier.Recognition>)
}