package com.yeloe.attentanceapp.ui.fragment.teacher.classroom

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentClassroomHomeBinding
import com.yeloe.attentanceapp.databinding.FragmentCreateClassroomBinding
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.fragment.authentication.CreateAccountFragmentDirections
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GenerateKeywords.Companion.generateKeywords
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import com.yeloe.attentanceapp.utils.ShowToast.Companion.showToast
import java.util.Calendar


class CreateClassroomFragment : Fragment() {

    private var _binding: FragmentCreateClassroomBinding? = null
    private val binding get() = _binding!!
    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mTeacherClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.teacherClassRoomCollection)
    private var mCurrentClassroom = CreateClassroom()

    private val args: CreateClassroomFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateClassroomBinding.inflate(inflater, container, false)
        val signInDetails = args.signInDetails
        if (signInDetails.type == Constant.CREATE) {
            (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_CLASSROOM_CREATE)
        }
        if (signInDetails.type == Constant.UPDATE) {
            (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_CLASSROOM_UPDATE)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signInDetails = args.signInDetails

        if (Constant.UPDATE == signInDetails.type) {
            val classroom =
                (activity as TeacherActivity).mAttendanceViewModel.getCreateClassroomData
            binding.nameTextInputEditText.setText(classroom.classroomName)
            binding.collegeAutoCompleteTextView.setText(classroom.classroomCollege)
            binding.branchAutoCompleteTextView.setText(classroom.classroomBranch)
            binding.yearAutoCompleteTextView.setText(classroom.classroomYear)
            binding.semesterAutoCompleteTextView.setText(classroom.classroomSemester)
        }

        (activity as TeacherActivity).binding.closeTopAppBar.setOnClickListener {
            try {
                findNavController().popBackStack()
            } catch (e: Exception) {
            }
        }

        (activity as TeacherActivity).mAttendanceViewModel.mUpdateClassroomState.observe(
            viewLifecycleOwner
        ){ response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val data = response.data
                            Log.d(Constant.CREATE_ACCOUNT_LOG, "\n $data")
                            if (data != null) {
                                if (data) {
                                    setProgressBarVisibility(false)
                                    if (Constant.CREATE == signInDetails.type) {
                                        showToast(
                                            requireContext(), "Classroom updated successfully"
                                        )
                                    }

                                    (activity as TeacherActivity).mAttendanceViewModel.mUpdateClassroomState.postValue(
                                        Resources.Completed()
                                    )

                                    (activity as TeacherActivity).mAttendanceViewModel.setCreateClassroomData(
                                        mCurrentClassroom
                                    )

                                    findNavController().popBackStack()

                                } else {
                                    if (Constant.UPDATE == signInDetails.type) {
                                        showToast(requireContext(), "Unable to update  classroom")
                                    }

                                    setProgressBarVisibility(false)
                                }
                            } else {
                                if (Constant.UPDATE == signInDetails.type) {
                                    showToast(requireContext(), "Unable to update  classroom")
                                }
                                setProgressBarVisibility(false)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.TEACHER_LOG, "Error $message")
                                showToast(requireContext(), message.toString())
                            }
                            setProgressBarVisibility(false)
                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mCreateAccountStatusState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                showToast(requireContext(), e.message.toString())
            }
        }

        (activity as TeacherActivity).mAttendanceViewModel.mCreateClassroomState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val data = response.data
                            Log.d(Constant.CREATE_ACCOUNT_LOG, "\n $data")
                            if (data != null) {
                                if (data) {
                                    setProgressBarVisibility(false)
                                    if (Constant.CREATE == signInDetails.type) {
                                        showToast(
                                            requireContext(), "Classroom created successfully"
                                        )
                                    }

                                    (activity as TeacherActivity).mAttendanceViewModel.mCreateClassroomState.postValue(
                                        Resources.Completed()
                                    )

                                    (activity as TeacherActivity).mAttendanceViewModel.setCreateClassroomData(
                                        mCurrentClassroom
                                    )

                                    findNavController().popBackStack()

                                } else {
                                    if (Constant.CREATE == signInDetails.type) {
                                        showToast(requireContext(), "Unable to create  classroom")
                                    }

                                    setProgressBarVisibility(false)
                                }
                            } else {
                                if (Constant.CREATE == signInDetails.type) {
                                    showToast(requireContext(), "Unable to create  classroom")
                                }
                                setProgressBarVisibility(false)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.TEACHER_LOG, "Error $message")
                                showToast(requireContext(), message.toString())
                            }
                            setProgressBarVisibility(false)
                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mCreateAccountStatusState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                showToast(requireContext(), e.message.toString())
            }

        }

        (activity as TeacherActivity).binding.taskButtonTopAppBar.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                val name = binding.nameTextInputEditText.text.toString().trim().lowercase()
                val college = binding.collegeAutoCompleteTextView.text.toString().trim()
                val branch = binding.branchAutoCompleteTextView.text.toString().trim()
                val year = binding.yearAutoCompleteTextView.text.toString().trim()
                val semester = binding.semesterAutoCompleteTextView.text.toString().trim()
                val date = Calendar.getInstance().time;
                if (validateAllFields(name, college, branch, year, semester)) {
                    val uid = FirebaseAuth.getInstance().uid
                    if (uid != null) {
                        var classroomUid = ""
                        classroomUid = if (Constant.UPDATE == signInDetails.type) {
                            val classroom =
                                (activity as TeacherActivity).mAttendanceViewModel.getCreateClassroomData
                            classroom.classroomUid
                        } else {
                            mTeacherClassroomCollection.document().id
                        }
                        mCurrentClassroom = CreateClassroom(
                            uid = uid,
                            classroomUid = classroomUid,
                            classroomName = name,
                            classroomCollege = college,
                            classroomBranch = branch,
                            classroomYear = year,
                            classroomSemester = semester,
                            teacherCollege = signInDetails.college,
                            teacherImage = signInDetails.profileImage,
                            teacherEmail = signInDetails.email,
                            teacherName = signInDetails.name,
                            timeStamp = date,
                            keywords = generateKeywords(name)
                        )

                        if (Constant.CREATE == signInDetails.type) {
                            (activity as TeacherActivity).mAttendanceViewModel.createClassroom(
                                mCurrentClassroom
                            )
                            (activity as TeacherActivity).mAttendanceViewModel.mCreateClassroomState.postValue(
                                Resources.Loading()
                            )
                        }
                        if (Constant.UPDATE == signInDetails.type) {
                            (activity as TeacherActivity).mAttendanceViewModel.updateClassroomAndJoinClassroom(
                                mCurrentClassroom
                            )
                            (activity as TeacherActivity).mAttendanceViewModel.mUpdateClassroomState.postValue(
                                Resources.Loading()
                            )
                        }


                    } else {
                        ShowToast.showToast(requireContext(), "Something went wrong")
                    }

                }

            } else {
                ShowToast.showToast(requireContext(), Constant.CHECK_INTERNET_CONNECTION)
            }
        }


    }

    private fun setProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.createClassroomLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.createClassroomLinearProgressIndicator.visibility = View.GONE
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


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}