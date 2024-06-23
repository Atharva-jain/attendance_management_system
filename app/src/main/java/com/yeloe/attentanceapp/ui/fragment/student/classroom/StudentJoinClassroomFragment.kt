package com.yeloe.attentanceapp.ui.fragment.student.classroom

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

import com.yeloe.attentanceapp.databinding.FragmentStudentJoinClassroomBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.adapter.student.classroom.JoinClassroomAdapter
import com.yeloe.attentanceapp.ui.adapter.student.classroom.JoinClassroomListener

import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Constant.SEARCH_MOVIE_TIME_DELAY
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception


class StudentJoinClassroomFragment : Fragment(), JoinClassroomListener {


    private var _binding: FragmentStudentJoinClassroomBinding? = null

    private val binding get() = _binding!!

    private lateinit var mJoinClassroomAdapter: JoinClassroomAdapter

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mTeacherClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.teacherClassRoomCollection)

    private val mJoinClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.joinClassroomCollection)

    private lateinit var mLatestClassroomQuery: Query
    private lateinit var mSearchClassroomQuery: Query

    private lateinit var mLatestClassroomQueryOptions: FirestoreRecyclerOptions<CreateClassroom>
    private lateinit var mSearchClassroomQueryOptions: FirestoreRecyclerOptions<CreateClassroom>

    private var mCurrentUid: String? = null
    private var mCreateClassroom = CreateClassroom()

    override fun onStart() {
        super.onStart()
        try {
            mJoinClassroomAdapter.startListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mJoinClassroomAdapter.stopListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentJoinClassroomBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.JOIN_CLASSROOM)

        val signInData = (activity as StudentActivity).mSignInDetails

        Log.d(Constant.STUDENT_LOG, "Sign in Data :$signInData")

        mCurrentUid = GetUid.getUid()

        if (mCurrentUid != null) {

            mLatestClassroomQuery = mTeacherClassroomCollection
                .whereEqualTo(Constant.COLLEGE, signInData.college)
                .whereEqualTo(Constant.BRANCH, signInData.branch)
                .whereEqualTo(Constant.YEAR, signInData.year)
                .whereEqualTo(Constant.SEMESTER, signInData.semester)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

            mLatestClassroomQueryOptions = FirestoreRecyclerOptions.Builder<CreateClassroom>()
                .setQuery(mLatestClassroomQuery, CreateClassroom::class.java)
                .build()

            mJoinClassroomAdapter = JoinClassroomAdapter(this, mLatestClassroomQueryOptions)
            val layout = WrapContentLinearLayoutManager(
                (activity as StudentActivity),
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.joinClassRecyclerView.layoutManager = layout
            binding.joinClassRecyclerView.adapter = mJoinClassroomAdapter
            mJoinClassroomAdapter.startListening()

        } else {
            ShowToast.showToast(requireContext(), "Something went wrong!!")
        }


        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var job: Job? = null
        binding.searchEditText.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(SEARCH_MOVIE_TIME_DELAY)
                editable?.let {
                    if (editable.toString().isNotEmpty()) {

                        if (mCurrentUid != null) {

                            val searchQuery = editable.toString()
                            mSearchClassroomQuery = mTeacherClassroomCollection
                                .whereArrayContains("keywords", searchQuery.trim())
                                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

                            mSearchClassroomQueryOptions =
                                FirestoreRecyclerOptions.Builder<CreateClassroom>()
                                    .setQuery(mSearchClassroomQuery, CreateClassroom::class.java)
                                    .build()

                            mJoinClassroomAdapter.updateOptions(mSearchClassroomQueryOptions)

                        } else {
                            ShowToast.showToast(requireContext(), "Something went wrong!!")
                        }

                    }
                }
            }
        }


        (activity as StudentActivity).mAttendanceViewModel.mCheckClassroomJoinState.observe(
            viewLifecycleOwner
        ) { response ->

            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            val data = response.data
                            Log.d(Constant.STUDENT_LOG, "\n $data")
                            if (data != null) {
                                if (data) {

                                    (activity as StudentActivity).mAttendanceViewModel.mCheckClassroomJoinState.postValue(
                                        Resources.Completed()
                                    )

                                    val joinClassUid = mJoinClassroomCollection.document().id
                                    val signInData = (activity as StudentActivity).mSignInDetails
                                    val joinClassroom = JoinClassroom(
                                        uid = mCurrentUid!!,
                                        classroomUid = mCreateClassroom.classroomUid,
                                        joinClassRoomUid = joinClassUid,
                                        teacherUid = mCreateClassroom.uid,
                                        classroomSemester = mCreateClassroom.classroomSemester,
                                        classroomYear = mCreateClassroom.classroomYear,
                                        classroomBranch = mCreateClassroom.classroomBranch,
                                        classroomCollege = mCreateClassroom.classroomCollege,
                                        classroomName = mCreateClassroom.classroomName,
                                        teacherName = mCreateClassroom.teacherName,
                                        teacherEmail = mCreateClassroom.teacherEmail,
                                        teacherImage = mCreateClassroom.teacherImage,
                                        teacherCollege = mCreateClassroom.teacherCollege,
                                        timeStamp = mCreateClassroom.timeStamp,
                                        keywords = mCreateClassroom.keywords,
                                        studentBranch = signInData.branch,
                                        studentCollege = signInData.college,
                                        studentFaceImage = signInData.faceImage,
                                        studentName = signInData.name,
                                        studentProfileImage = signInData.profileImage,
                                        studentSemester = signInData.semester,
                                        studentYear = signInData.year
                                    )
                                    (activity as StudentActivity).mAttendanceViewModel.joinClassroom(
                                        joinClassroom
                                    )


                                } else {
                                    ShowToast.showToast(
                                        requireContext(),
                                        "You are already a member of this classroom."
                                    )
                                    setProgressBarVisibility(false)
                                }
                            } else {
                                ShowToast.showToast(requireContext(), "Unable to Join classroom")
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
                            (activity as StudentActivity).mAttendanceViewModel.mCheckClassroomJoinState.postValue(
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


        (activity as StudentActivity).mAttendanceViewModel.mJoinClassroomState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            val data = response.data
                            Log.d(Constant.STUDENT_LOG, "\n $data")
                            if (data != null) {
                                if (data) {
                                    setProgressBarVisibility(false)
                                    ShowToast.showToast(
                                        requireContext(),
                                        "Classroom joined successfully"
                                    )
                                    (activity as StudentActivity).mAttendanceViewModel.mJoinClassroomState.postValue(
                                        Resources.Completed()
                                    )

                                    findNavController().popBackStack()

                                } else {
                                    ShowToast.showToast(
                                        requireContext(),
                                        "Unable to Join classroom"
                                    )
                                    setProgressBarVisibility(false)
                                }
                            } else {
                                ShowToast.showToast(requireContext(), "Unable to Join classroom")
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
                            (activity as StudentActivity).mAttendanceViewModel.mJoinClassroomState.postValue(
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


    }

    private fun setProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.joinClassLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.joinClassLinearProgressIndicator.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClickJoinedClassroom(createClassroom: CreateClassroom) {
        try {

            if (mCurrentUid != null) {
                mCreateClassroom = createClassroom
                (activity as StudentActivity).mAttendanceViewModel.checkClassroomIsJoined(
                    mCurrentUid!!,
                    createClassroom.classroomUid
                )
            } else {
                ShowToast.showToast(requireContext(), "Something went wrong!!")
            }

        } catch (e: Exception) {
        }
    }

    override fun isClassRoomJoinedEmpty(value: Boolean) {
        setVisibilityOfJoinEmptyClassroom(value)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setVisibilityOfJoinEmptyClassroom(value: Boolean) {
        if (value) {
            try {
                mJoinClassroomAdapter.notifyDataSetChanged()
            } catch (e: Exception) {
            }
            binding.emptyClassroomToJoinLayout.visibility = View.VISIBLE
        } else {
            binding.emptyClassroomToJoinLayout.visibility = View.GONE
        }
    }

}