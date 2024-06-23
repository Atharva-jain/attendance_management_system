package com.yeloe.attentanceapp.ui.fragment.student.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide


import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentStudentEditProfileBinding
import com.yeloe.attentanceapp.databinding.FragmentStudentProfileBinding
import com.yeloe.attentanceapp.model.authentication.Face
import com.yeloe.attentanceapp.model.authentication.SignIn
import com.yeloe.attentanceapp.ui.activity.face_detector.FaceDetectionActivity
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.face_classification.FaceClassifier
import com.yeloe.attentanceapp.ui.face_classification.TFLiteFaceRecognition

import com.yeloe.attentanceapp.ui.fragment.authentication.ScanFaceDialogFragment
import com.yeloe.attentanceapp.ui.fragment.authentication.SelectedFaceOnClickListener
import com.yeloe.attentanceapp.ui.fragment.authentication.SignUpBasicDetailsFromFragmentArgs
import com.yeloe.attentanceapp.ui.fragment.authentication.SignUpBasicDetailsFromFragmentDirections
import com.yeloe.attentanceapp.ui.fragment.authentication.TryAgainToSelectFaceListener
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.EmBeddingConverter
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.cameraPermissionRequest
import com.yeloe.attentanceapp.utils.isPermissionGranted
import com.yeloe.attentanceapp.utils.openPermissionSetting
import java.io.IOException

class StudentEditProfileFragment : Fragment(), TryAgainToSelectFaceListener,
    SelectedFaceOnClickListener {

    private var _binding: FragmentStudentEditProfileBinding? = null
    private val binding get() = _binding!!
    private var mProfileImage: Bitmap? = null
    private var mScanImage: Bitmap? = null
    private lateinit var mProfileImageUri: Uri
    private var mSignInData: SignIn = SignIn()
    private lateinit var mScanFaceDialogFragment: ScanFaceDialogFragment
    private var mIsBottomSheetCreated: Boolean = false
    private var mIsFaceImageUploaded: Boolean = false
    private var mIsProfileImageUploaded: Boolean = false
    private lateinit var faceClassifier: FaceClassifier

    // get the arguments from the Registration fragment
    //private val args: SignUpBasicDetailsFromFragmentArgs by navArgs()

    private val cameraPermission = android.Manifest.permission.CAMERA

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            }
        }

    private fun enableSubmitButton(value: Boolean) {
        binding.loginButton.isEnabled = value
    }

    private fun setVisibilityOfAddDataProgressBar(value: Boolean) {
        if (value) {
            binding.addDataLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.addDataLinearProgressIndicator.visibility = View.GONE
        }
    }

    private fun setVisibilityOfImage(value: Boolean, image: Bitmap?) {
        if (value) {
            binding.fillProfileImageView.visibility = View.VISIBLE
            binding.addProfileImageView.visibility = View.GONE
            if (image != null) {
                binding.fillProfileImageView.setImageBitmap(image)
            }
        } else {
            if (mProfileImage == null) {
                binding.fillProfileImageView.visibility = View.GONE
                binding.addProfileImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun setUploadButtonVisibility(value: Boolean) {
        if (value) {
            binding.uploadProfileImageButton.visibility = View.VISIBLE
        } else {
            if (mProfileImage == null) {
                binding.uploadProfileImageButton.visibility = View.GONE
            }
        }
    }

    private fun setVisibilityOfProfileProgressBar(value: Boolean) {
        if (value) {
            binding.profileProgressBar.visibility = View.VISIBLE
        } else {
            binding.profileProgressBar.visibility = View.GONE
        }
    }

    private fun setVisibilityOfFaceProgressbar(value: Boolean) {
        if (value) {
            binding.faceProgressBar.visibility = View.VISIBLE
        } else {
            binding.faceProgressBar.visibility = View.GONE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentEditProfileBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.STUDENT_EDIT_PROFILE)

        try {
            faceClassifier = TFLiteFaceRecognition.create(
                (activity as StudentActivity).assets,
                Constant.MODEL_NAME,
                Constant.TF_OD_API_INPUT_SIZE2,
                false,
                (activity as StudentActivity).applicationContext,
                HashMap()
            )
        } catch (e: IOException) {
            //e.printStackTrace()
            val toast = Toast.makeText(
                (activity as StudentActivity).applicationContext,
                "Classifier could not be initialized",
                Toast.LENGTH_SHORT
            )
            toast.show()
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSignInData = (activity as StudentActivity).mSignInDetails


        setStudentProfileData((activity as StudentActivity).mSignInDetails)

        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            editBackDialog((activity as StudentActivity)) {
                findNavController().popBackStack()
            }
        }

        binding.addProfileImageMaterialCardView.setOnClickListener {
            getSelectedImage.launch(Constant.GETTING_IMAGE_INTENT)
        }

        (activity as StudentActivity).mAttendanceViewModel.mAddFacesState.observe(
            viewLifecycleOwner
        ) { response ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        Log.d(Constant.FACE_DETECTION_LOG, " Faces add \n $data")
                        if (data != null) {
                            showToast("Face Uploaded Successfully!!....")
                            (activity as StudentActivity).mAttendanceViewModel.updateSignInAndJoinClassrooms(
                                signIn = mSignInData
                            )
                        } else {
                            enableSubmitButton(true)
                            setVisibilityOfAddDataProgressBar(false)
                        }
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                            showToast(message.toString())
                        }
                        enableSubmitButton(true)
                        setVisibilityOfAddDataProgressBar(false)

                    }

                    is Resources.Loading -> {
                        setVisibilityOfAddDataProgressBar(true)
                    }

                    else -> {
                        (activity as StudentActivity).mAttendanceViewModel.mAddFacesState.postValue(
                            Resources.Completed()
                        )
                        enableSubmitButton(true)
                        setVisibilityOfAddDataProgressBar(false)
                    }

                }
            }
        }

        (activity as StudentActivity).mAttendanceViewModel.mUpdateStudentRecordState.observe(
            viewLifecycleOwner
        ) { response ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        Log.d(Constant.FACE_DETECTION_LOG, "\n $data")
                        setVisibilityOfProfileProgressBar(false)
                        if (data != null) {
                            showToast("Profile updated....")
                            setVisibilityOfAddDataProgressBar(false)
                            enableSubmitButton(true)
                            (activity as StudentActivity).mSignInDetails = mSignInData
                            (activity as StudentActivity).changeProfileImage(mSignInData.profileImage)
                            findNavController().popBackStack()
                        } else {
                            enableSubmitButton(true)
                            setVisibilityOfAddDataProgressBar(false)
                        }
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                            showToast(message.toString())
                        }
                        enableSubmitButton(true)
                        setVisibilityOfAddDataProgressBar(false)

                    }

                    is Resources.Loading -> {
                        setVisibilityOfAddDataProgressBar(true)
                    }

                    else -> {
                        (activity as StudentActivity).mAttendanceViewModel.mAddStudentUserState.postValue(
                            Resources.Completed()
                        )
                        enableSubmitButton(true)
                        setVisibilityOfAddDataProgressBar(false)
                    }

                }
            }
        }

        (activity as StudentActivity).mAttendanceViewModel.mProfileImageProcessState.observe(
            viewLifecycleOwner
        ) { response ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        setVisibilityOfProfileProgressBar(true)
                        Log.d(Constant.FACE_DETECTION_LOG, "\n $data")
                        if (data != null) {
                            binding.profileProgressBar.progress = data.toInt()
                        }
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                            showToast(message.toString())
                        }
                        setVisibilityOfProfileProgressBar(false)
                        mIsProfileImageUploaded = false
                    }

                    is Resources.Loading -> {
                        setVisibilityOfProfileProgressBar(true)
                        mIsProfileImageUploaded = false
                    }

                    else -> {
                        (activity as StudentActivity).mAttendanceViewModel.mProfileImageUploadState.postValue(
                            Resources.Completed()
                        )
                        (activity as StudentActivity).mAttendanceViewModel.mProfileImageProcessState.postValue(
                            Resources.Completed()
                        )
                        mIsProfileImageUploaded = false
                    }

                }
            }
        }

        (activity as StudentActivity).mAttendanceViewModel.mProfileImageUploadState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            val url = response.data
                            Log.d(Constant.FACE_DETECTION_LOG, "\n $url")
                            if (url != null) {
                                setVisibilityOfProfileProgressBar(false)
                                mIsProfileImageUploaded = true
                                mSignInData.profileImage = url
                            } else {
                                setVisibilityOfProfileProgressBar(false)
                                response.message.let { message ->
                                    Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                                    showToast("Unable to Upload Image please try again...")
                                }
                                mIsProfileImageUploaded = false
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                                showToast(message.toString())
                            }
                            setVisibilityOfProfileProgressBar(false)
                            mIsProfileImageUploaded = false
                        }

                        is Resources.Loading -> {
                            setVisibilityOfProfileProgressBar(true)
                            mIsProfileImageUploaded = false
                        }

                        else -> {
                            (activity as StudentActivity).mAttendanceViewModel.mProfileImageUploadState.postValue(
                                Resources.Completed()
                            )
                            (activity as StudentActivity).mAttendanceViewModel.mProfileImageProcessState.postValue(
                                Resources.Completed()
                            )
                            mIsProfileImageUploaded = false
                        }

                    }
                }
            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                showToast(e.message.toString())
                mIsProfileImageUploaded = false
            }
        }

        (activity as StudentActivity).mAttendanceViewModel.mFaceImageProcessState.observe(
            viewLifecycleOwner
        ) { response ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        setVisibilityOfFaceProgressbar(true)
                        Log.d(Constant.FACE_DETECTION_LOG, "\n $data")

                        if (data != null) {
                            binding.faceProgressBar.progress = data.toInt()
                        }
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                            showToast(message.toString())
                        }
                        setVisibilityOfFaceProgressbar(false)
                        mIsFaceImageUploaded = false
                    }

                    is Resources.Loading -> {
                        setVisibilityOfFaceProgressbar(true)
                        mIsFaceImageUploaded = false
                    }

                    else -> {
                        (activity as StudentActivity).mAttendanceViewModel.mFaceImageUploadState.postValue(
                            Resources.Completed()
                        )
                        (activity as StudentActivity).mAttendanceViewModel.mFaceImageProcessState.postValue(
                            Resources.Completed()
                        )
                        mIsFaceImageUploaded = false
                    }

                }
            }
        }

        (activity as StudentActivity).mAttendanceViewModel.mFaceImageUploadState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            val url = response.data
                            Log.d(Constant.FACE_DETECTION_LOG, "\n $url")
                            if (url != null) {
                                setVisibilityOfFaceProgressbar(false)
                                mIsFaceImageUploaded = true
                                mSignInData.faceImage = url
                            } else {
                                setVisibilityOfFaceProgressbar(false)
                                response.message.let { message ->
                                    Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                                    showToast("Unable to Upload Image please try again...")
                                }
                                mIsFaceImageUploaded = false
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                                showToast(message.toString())
                            }
                            setVisibilityOfFaceProgressbar(false)
                            mIsFaceImageUploaded = false
                        }

                        is Resources.Loading -> {
                            setVisibilityOfFaceProgressbar(true)
                            mIsFaceImageUploaded = false
                        }

                        else -> {
                            (activity as StudentActivity).mAttendanceViewModel.mFaceImageProcessState.postValue(
                                Resources.Completed()
                            )
                            (activity as StudentActivity).mAttendanceViewModel.mFaceImageUploadState.postValue(
                                Resources.Completed()
                            )
                            mIsFaceImageUploaded = false
                        }

                    }
                }
            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                showToast(e.message.toString())
                mIsFaceImageUploaded = false
            }
        }

        binding.uploadProfileImageButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                try {
                    (activity as StudentActivity).mAttendanceViewModel.addProfileImage(
                        (activity as StudentActivity), mProfileImageUri
                    )

                } catch (e: java.lang.Exception) {
                    Log.d(Constant.FACE_DETECTION_LOG, "Profile Image Error $e")
                    showToast("Please Add Profile Image....")

                }
            } else {
                showToast(Constant.CHECK_INTERNET_CONNECTION)
            }
        }

        binding.scanImageMaterialCardView.setOnClickListener {
            mIsBottomSheetCreated = false
            requestCameraAndStart()
        }

        binding.loginButton.setOnClickListener {
            val name = binding.nameTextInputEditText.text.toString().trim()
            val college = binding.collegeAutoCompleteTextView.text.toString().trim()
            val branch = binding.branchAutoCompleteTextView.text.toString().trim()
            val year = binding.yearAutoCompleteTextView.text.toString().trim()
            val semester = binding.semesterAutoCompleteTextView.text.toString().trim()

            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                if (validateAllFields(name, college, branch, year, semester)) {
                    if (mProfileImage != null) {

                        if (mIsProfileImageUploaded) {

                            enableSubmitButton(false)
                            mSignInData.name = name
                            mSignInData.branch = branch
                            mSignInData.college = college
                            mSignInData.email = mSignInData.email
                            mSignInData.uid = mSignInData.uid
                            mSignInData.semester = semester
                            mSignInData.year = year
                            mSignInData.type = mSignInData.type

                            Log.d(Constant.FACE_DETECTION_LOG, "Student Profile Called")

                            (activity as StudentActivity).mAttendanceViewModel.updateSignInAndJoinClassrooms(
                                signIn = mSignInData
                            )


                        } else {
                            showToast("It Seen Profile Image is not Uploaded...")
                            enableSubmitButton(true)
                        }

                    } else if (mScanImage != null) {
                        if (mIsFaceImageUploaded) {

                            try {
                                enableSubmitButton(false)
                                mSignInData.name = name
                                mSignInData.branch = branch
                                mSignInData.college = college
                                mSignInData.email = mSignInData.email
                                mSignInData.uid = mSignInData.uid
                                mSignInData.semester = semester
                                mSignInData.year = year
                                mSignInData.type = mSignInData.type

                                Log.d(Constant.FACE_DETECTION_LOG, "Student Face Called")

                                val faceImage = Bitmap.createScaledBitmap(
                                    mScanImage!!,
                                    Constant.TF_OD_API_INPUT_SIZE2,
                                    Constant.TF_OD_API_INPUT_SIZE2,
                                    false
                                )

                                val recognition: FaceClassifier.Recognition =
                                    faceClassifier.recognizeImage(faceImage, true)
                                faceClassifier.register(mSignInData.uid, recognition)

                                val face = Face(
                                    name = mSignInData.uid,
                                    embedding = EmBeddingConverter.getStringFromEmbedding(
                                        recognition
                                    )
                                )

                                (activity as StudentActivity).mAttendanceViewModel.addFace(face)

                            } catch (e: Exception) {
                                Log.d(Constant.FACE_DETECTION_LOG, "Error $e")
                            }

                        } else {
                            showToast("It Seen Face Image is not Uploaded...")
                            enableSubmitButton(true)
                        }

                    } else {
                        // already profile image exist
                        enableSubmitButton(false)
                        mSignInData.name = name
                        mSignInData.branch = branch
                        mSignInData.college = college
                        mSignInData.email = mSignInData.email
                        mSignInData.uid = mSignInData.uid
                        mSignInData.semester = semester
                        mSignInData.year = year
                        mSignInData.type = mSignInData.type

                        Log.d(Constant.FACE_DETECTION_LOG, "Student Data Called")

                        (activity as StudentActivity).mAttendanceViewModel.updateSignInAndJoinClassrooms(
                            signIn = mSignInData
                        )


                    }
                } else {
                    // Validation failed, show appropriate error messages
                    enableSubmitButton(true)
                }

            } else {
                showToast(Constant.CHECK_INTERNET_CONNECTION)
            }


        }

    }

    private fun editBackDialog(
        context: Context, onStopTakingAttendanceConfirmed: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Waring")
            setIcon(R.drawable.edit)
            setMessage("Are you certain you wish to proceed without editing your profile?")
            setPositiveButton("Yes") { dialog, _ ->
                // Call the onDeleteConfirmed function when delete is confirmed
                onStopTakingAttendanceConfirmed.invoke()
                dialog.dismiss()
            }
            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun setStudentProfileData(signIn: SignIn) {

        binding.nameTextInputEditText.setText(signIn.name)
        binding.branchAutoCompleteTextView.setText(signIn.branch)
        binding.yearAutoCompleteTextView.setText(signIn.year)
        binding.semesterAutoCompleteTextView.setText(signIn.semester)
        binding.collegeAutoCompleteTextView.setText(signIn.college)
        Glide.with(this).load(signIn.profileImage).into(binding.fillProfileImageView)

    }

    private fun requestCameraAndStart() {
        if ((activity as StudentActivity).isPermissionGranted(cameraPermission)) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun startCamera() {
        FaceDetectionActivity.startActivity((activity as StudentActivity)) { faceImage ->
            Log.d(Constant.FACE_DETECTION_LOG, "In Main get image .....")
            try {
                // Post a delayed task to show the dialog fragment after a short delay
                Handler(Looper.getMainLooper()).postDelayed({
                    if (mIsBottomSheetCreated) {
                        try {
                            mScanFaceDialogFragment.setImageOfFace(faceImage)
                        } catch (e: Exception) {
                            Log.d(Constant.FACE_DETECTION_LOG, "Error on face $e")
                        }
                    } else {
                        try {
                            mScanFaceDialogFragment = ScanFaceDialogFragment(faceImage, this, this)
                            mScanFaceDialogFragment.show(
                                (activity as StudentActivity).supportFragmentManager,
                                "FaceDetection"
                            )
                            mIsBottomSheetCreated = true
                        } catch (e: Exception) {
                            Log.d(Constant.FACE_DETECTION_LOG, "Error on face $e")
                        }
                    }
                }, 100)
            } catch (e: Exception) {
                Log.d(Constant.FACE_DETECTION_LOG, "Error on face $e")
            }
        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                (activity as StudentActivity).cameraPermissionRequest(positive = { (activity as StudentActivity).openPermissionSetting() })
            }

            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }

    }

    // Function to validate name field
    private fun validateName(name: String): Boolean {
        if (name.isEmpty()) {
            binding.nameTextInputEditText.error = "Name cannot be empty"
            return false
        }
        return true
    }

    // Function to validate college field
    private fun validateCollege(college: String): Boolean {
        if (college.isEmpty()) {
            binding.collegeAutoCompleteTextView.error = "College cannot be empty"
            return false
        }
        return true
    }

    // Function to validate branch field
    private fun validateBranch(branch: String): Boolean {
        if (branch.isEmpty()) {
            binding.branchAutoCompleteTextView.error = "Branch cannot be empty"
            return false
        }
        return true
    }

    // Function to validate year field
    private fun validateYear(year: String): Boolean {
        if (year.isEmpty()) {
            binding.yearAutoCompleteTextView.error = "Year cannot be empty"
            return false
        }
        return true
    }

    // Function to validate semester field
    private fun validateSemester(semester: String): Boolean {
        if (semester.isEmpty()) {
            binding.semesterAutoCompleteTextView.error = "Semester cannot be empty"
            return false
        }
        return true
    }

    // Function to validate all fields
    private fun validateAllFields(
        name: String, college: String, branch: String, year: String, semester: String
    ): Boolean {
        return validateName(name) && validateCollege(college) && validateBranch(branch) && validateYear(
            year
        ) && validateSemester(semester)
    }

    // create a object getting selected image data form startActivityResult
    private val getSelectedImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            Log.d(Constant.CREATE_ACCOUNT_LOG, "GetSelectedIntent is called")
            if (uri != null) {
                try {
                    Log.d(Constant.CREATE_ACCOUNT_LOG, "Uri Consist")
                    mProfileImageUri = uri

                    mProfileImage = MediaStore.Images.Media.getBitmap(
                        (activity as StudentActivity).contentResolver, mProfileImageUri
                    )
                    setVisibilityOfImage(true, mProfileImage)
                    setUploadButtonVisibility(true)

                } catch (e: Exception) {
                    Log.d(
                        Constant.CREATE_ACCOUNT_LOG,
                        "Image data is not converted to Bitmap error is below: \n $e"
                    )
                    setVisibilityOfImage(false, null)
                    setUploadButtonVisibility(false)
                }
            } else {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Uri data is NUll")
                setVisibilityOfImage(false, null)
                setUploadButtonVisibility(false)
            }
        }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(), message, Toast.LENGTH_LONG
        ).show()
    }

    private fun setVisibilityOfScanImageSet(value: Boolean) {
        if (value) {
            binding.isFaceImageAddedImageView.visibility = View.VISIBLE
        } else {
            binding.isFaceImageAddedImageView.visibility = View.GONE
        }
    }

    override fun tryAgainToSelectFaceListener() {
        requestCameraAndStart()
    }

    override fun selectedFaceOnClickListener(faceImage: Bitmap) {
        try {
            setVisibilityOfScanImageSet(true)
            mScanImage = faceImage
            mScanFaceDialogFragment.dismiss()
            (activity as StudentActivity).mAttendanceViewModel.addFaceImage(faceImage)
        } catch (e: java.lang.Exception) {
            Log.d(Constant.FACE_DETECTION_LOG, "Error Face $e")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}