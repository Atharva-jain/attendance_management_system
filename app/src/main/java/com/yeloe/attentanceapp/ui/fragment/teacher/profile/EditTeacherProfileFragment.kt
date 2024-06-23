package com.yeloe.attentanceapp.ui.fragment.teacher.profile

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentEditTeacherProfileBinding
import com.yeloe.attentanceapp.databinding.FragmentProfileBinding
import com.yeloe.attentanceapp.model.authentication.SignIn
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.fragment.authentication.SignUpBasicDetailsFromFragmentDirections
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import com.yeloe.attentanceapp.utils.ShowToast.Companion.showToast

class EditTeacherProfileFragment : Fragment() {

    private var _binding: FragmentEditTeacherProfileBinding? = null
    private val binding get() = _binding!!

    private var mProfileImage: Bitmap? = null
    private lateinit var mProfileImageUri: Uri
    private var mIsProfileImageUploaded: Boolean = false
    private var mSignInData: SignIn = SignIn()

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

    private fun setVisibilityOfProfileProgressBar(value: Boolean) {
        if (value) {
            binding.profileProgressBar.visibility = View.VISIBLE
        } else {
            binding.profileProgressBar.visibility = View.GONE
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditTeacherProfileBinding.inflate(inflater, container, false)
        (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_EDIT_PROFILE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSignInData = (activity as TeacherActivity).mSignInDetails

        setTeacherSignInInformation(mSignInData)

        (activity as TeacherActivity).binding.closeTopAppBar.setOnClickListener {
            editBackDialog((activity as TeacherActivity)) {
                findNavController().popBackStack()
            }
        }


        (activity as TeacherActivity).mAttendanceViewModel.mUpdateTeacherRecordState.observe(
            viewLifecycleOwner
        ) { response ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        Log.d(Constant.FACE_DETECTION_LOG, "\n $data")
                        setVisibilityOfProfileProgressBar(false)
                        if (data != null) {
                            showToast(requireContext(), "Profile updated..")
                            setVisibilityOfAddDataProgressBar(false)
                            enableSubmitButton(true)
                            (activity as TeacherActivity).mSignInDetails = mSignInData
                            (activity as TeacherActivity).setProfileImage(mSignInData.profileImage)
                            findNavController().popBackStack()

                        } else {
                            enableSubmitButton(true)
                            setVisibilityOfAddDataProgressBar(false)
                        }
                        (activity as TeacherActivity).mAttendanceViewModel.mUpdateTeacherRecordState.postValue(
                            Resources.Completed()
                        )
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                            showToast(requireContext(), message.toString())
                        }
                        enableSubmitButton(true)
                        setVisibilityOfAddDataProgressBar(false)

                    }

                    is Resources.Loading -> {
                        setVisibilityOfAddDataProgressBar(true)
                    }

                    else -> {
                        (activity as TeacherActivity).mAttendanceViewModel.mUpdateTeacherRecordState.postValue(
                            Resources.Completed()
                        )
                        enableSubmitButton(true)
                        setVisibilityOfAddDataProgressBar(false)
                    }

                }
            }
        }

        (activity as TeacherActivity).mAttendanceViewModel.mProfileImageProcessState.observe(
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
                            ShowToast.showToast(requireContext(), message.toString())
                        }
                        setVisibilityOfProfileProgressBar(false)
                        mIsProfileImageUploaded = false
                    }

                    is Resources.Loading -> {
                        setVisibilityOfProfileProgressBar(true)
                        mIsProfileImageUploaded = false
                    }

                    else -> {
                        (activity as TeacherActivity).mAttendanceViewModel.mProfileImageUploadState.postValue(
                            Resources.Completed()
                        )
                        (activity as TeacherActivity).mAttendanceViewModel.mProfileImageProcessState.postValue(
                            Resources.Completed()
                        )
                        mIsProfileImageUploaded = false
                    }

                }
            }
        }

        (activity as TeacherActivity).mAttendanceViewModel.mProfileImageUploadState.observe(
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
                                    ShowToast.showToast(
                                        requireContext(),
                                        "Unable to Upload Image please try again..."
                                    )
                                }
                                mIsProfileImageUploaded = false
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.FACE_DETECTION_LOG, "Error $message")
                                ShowToast.showToast(requireContext(), message.toString())
                            }
                            setVisibilityOfProfileProgressBar(false)
                            mIsProfileImageUploaded = false
                        }

                        is Resources.Loading -> {
                            setVisibilityOfProfileProgressBar(true)
                            mIsProfileImageUploaded = false
                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mProfileImageUploadState.postValue(
                                Resources.Completed()
                            )
                            (activity as TeacherActivity).mAttendanceViewModel.mProfileImageProcessState.postValue(
                                Resources.Completed()
                            )
                            mIsProfileImageUploaded = false
                        }

                    }
                }
            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                showToast(requireContext(), e.message.toString())
                mIsProfileImageUploaded = false
            }
        }

        binding.addProfileImageMaterialCardView.setOnClickListener {
            getSelectedImage.launch(Constant.GETTING_IMAGE_INTENT)
        }

        binding.uploadProfileImageButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                try {
                    (activity as TeacherActivity).mAttendanceViewModel.addProfileImage(
                        (activity as TeacherActivity), mProfileImageUri
                    )

                } catch (e: java.lang.Exception) {
                    Log.d(Constant.FACE_DETECTION_LOG, "Profile Image Error $e")
                    showToast(requireContext(), "Please Add Profile Image....")

                }
            } else {
                showToast(requireContext(), Constant.CHECK_INTERNET_CONNECTION)
            }
        }

        binding.loginButton.setOnClickListener {
            val name = binding.nameTextInputEditText.text.toString().trim()
            val college = binding.collegeAutoCompleteTextView.text.toString().trim()

            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                if (validateName(name)) {
                    if (validateCollege(college)) {
                        if (mProfileImage != null) {
                            if (mIsProfileImageUploaded) {
                                enableSubmitButton(false)
                                mSignInData.name = name
                                mSignInData.college = college
                                mSignInData.email = mSignInData.email
                                mSignInData.uid = mSignInData.uid
                                mSignInData.type = mSignInData.type
                                (activity as TeacherActivity).mAttendanceViewModel.updateSignInAndClassrooms(
                                    signIn = mSignInData
                                )
                            } else {
                                showToast(requireContext(), "It Seen Image is not Uploaded...")
                                enableSubmitButton(true)
                            }
                        } else {
                            enableSubmitButton(false)
                            mSignInData.name = name
                            mSignInData.college = college
                            mSignInData.email = mSignInData.email
                            mSignInData.uid = mSignInData.uid
                            mSignInData.type = mSignInData.type
                            (activity as TeacherActivity).mAttendanceViewModel.updateSignInAndClassrooms(
                                signIn = mSignInData
                            )
                        }
                    }
                }
            }

        }


    }

    private fun setTeacherSignInInformation(signIn: SignIn) {
        binding.nameTextInputEditText.setText(signIn.name)
        binding.collegeAutoCompleteTextView.setText(signIn.college)
        Glide.with(this).load(signIn.profileImage).into(binding.fillProfileImageView)
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
                        (activity as TeacherActivity).contentResolver, mProfileImageUri
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}