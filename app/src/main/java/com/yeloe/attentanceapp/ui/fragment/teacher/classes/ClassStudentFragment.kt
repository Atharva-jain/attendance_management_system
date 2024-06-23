package com.yeloe.attentanceapp.ui.fragment.teacher.classes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentClassStudentBinding
import com.yeloe.attentanceapp.databinding.FragmentCreateClassBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassesAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.student_list.OnJoinedStudentListListener
import com.yeloe.attentanceapp.ui.adapter.teacher.student_list.StudentClassroomJoinedListAdapter
import com.yeloe.attentanceapp.ui.fragment.teacher.classes.attendance.OnNavigateListener
import com.yeloe.attentanceapp.ui.fragment.teacher.classes.attendance.ShowStudentAttendanceDialogFragment
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import java.lang.Exception

class ClassStudentFragment : Fragment(), OnJoinedStudentListListener, OnNavigateListener {

    private var _binding: FragmentClassStudentBinding? = null

    private val binding get() = _binding!!

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mJoinClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.joinClassroomCollection)
    private lateinit var mShowAttendanceOfStudentDialog: ShowStudentAttendanceDialogFragment

    private lateinit var mStudentListAdapter: StudentClassroomJoinedListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassStudentBinding.inflate(inflater, container, false)

        val currentClassRoom =
            (activity as TeacherActivity).mAttendanceViewModel.getCreateClassroomData

        val queryOfStudent = mJoinClassroomCollection.whereEqualTo(
                Constant.CLASSROOM_UID,
                currentClassRoom.classroomUid
            ).orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

        val optionsOfStudent = FirestoreRecyclerOptions.Builder<JoinClassroom>()
            .setQuery(queryOfStudent, JoinClassroom::class.java).build()

        mStudentListAdapter = StudentClassroomJoinedListAdapter(this, optionsOfStudent)

        val layout = WrapContentLinearLayoutManager(
            (activity as TeacherActivity), LinearLayoutManager.VERTICAL, false
        )

        binding.studentRecyclerView.layoutManager = layout
        binding.studentRecyclerView.adapter = mStudentListAdapter

        mStudentListAdapter.startListening()


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TeacherActivity).mAttendanceViewModel.mRemoveStudentJoinClassroomState.observe(
            viewLifecycleOwner
        ) { response ->

            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val attendance = response.data

                            Log.d(Constant.TEACHER_LOG, "\n $attendance")

                            if (attendance != null) {

                                if (attendance) {
                                    ShowToast.showToast(
                                        requireContext(), "Student are remove form classroom."
                                    )
                                    try {
                                        mShowAttendanceOfStudentDialog.dismiss()
                                    }catch (e:Exception){}

                                } else {
                                    ShowToast.showToast(
                                        requireContext(),
                                        "Student are not remove form classroom, please try again later."
                                    )

                                }
                                (activity as TeacherActivity).mAttendanceViewModel.mRemoveStudentJoinClassroomState.postValue(
                                    Resources.Completed()
                                )
                            } else {
                                ShowToast.showToast(
                                    requireContext(),
                                    "Student are not remove form classroom, please try again later."
                                )

                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.TEACHER_LOG, "Error $message")
                                ShowToast.showToast(requireContext(), message.toString())
                            }

                        }

                        is Resources.Loading -> {

                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mRemoveStudentJoinClassroomState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {

                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                ShowToast.showToast(requireContext(), e.message.toString())
            }
        }

    }

    override fun onStart() {
        super.onStart()
        try {
            mStudentListAdapter.startListening()

        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mStudentListAdapter.stopListening()

        } catch (e: java.lang.Exception) {
        }

    }

    private fun setVisibilityOfEmptyStudentLayout(value: Boolean) {
        if (value) {
            binding.emptyStudentLayout.visibility = View.VISIBLE
        } else {
            binding.emptyStudentLayout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun studentClick(joinClassroom: JoinClassroom) {
        try {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                mShowAttendanceOfStudentDialog =
                    ShowStudentAttendanceDialogFragment(joinClassroom, this)
                mShowAttendanceOfStudentDialog.show(
                    (activity as TeacherActivity).supportFragmentManager, "ShowAttendance"
                )
            } else {
                ShowToast.showToast(requireContext(), Constant.CHECK_INTERNET_CONNECTION)
            }

        } catch (e: Exception) {
        }
    }

    override fun isLiveDataEmpty(value: Boolean) {
        setVisibilityOfEmptyStudentLayout(value)
    }

    override fun onNavigateAbsent() {
        try {
            mShowAttendanceOfStudentDialog.dismiss()
            findNavController().navigate(R.id.action_classHomeFragment_to_markedAttendanceFragment)
        } catch (e: Exception) {
        }
    }

    override fun onNavigatePresent() {
        try {
            mShowAttendanceOfStudentDialog.dismiss()
            findNavController().navigate(R.id.action_classHomeFragment_to_markedAbsentFragment)
        } catch (e: Exception) {
        }
    }

    override fun onStudentRemove(joinClassroom: JoinClassroom) {
        try {
            (activity as TeacherActivity).mAttendanceViewModel.removeStudentFromClassroom(
                joinClassroom
            )
        } catch (e: Exception) {
        }
    }


}