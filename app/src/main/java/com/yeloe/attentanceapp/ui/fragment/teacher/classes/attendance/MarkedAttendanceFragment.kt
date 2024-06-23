package com.yeloe.attentanceapp.ui.fragment.teacher.classes.attendance

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeloe.attentanceapp.databinding.FragmentMarkedAttendanceBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.adapter.teacher.attendance.OnMarkedAbsentListener
import com.yeloe.attentanceapp.ui.adapter.teacher.attendance.PresentRecordAdapter
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import java.lang.Exception


class MarkedAttendanceFragment : Fragment(), OnMarkedAbsentListener {

    private var mClassroomData: JoinClassroom = JoinClassroom()
    private var _binding: FragmentMarkedAttendanceBinding? = null

    private val binding get() = _binding!!

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mAttendanceCollection =
        mFirebaseFireStoreInstance.collection(Constant.attendance)

    private lateinit var mPresentStudentAdapter: PresentRecordAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMarkedAttendanceBinding.inflate(inflater, container, false)
        (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_ATTENDANCE_ABSENT)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mClassroomData = (activity as TeacherActivity).mAttendanceViewModel.getStudentData

        val uid = GetUid.getUid()
        if (uid != null) {

            val queryOfStudent = mAttendanceCollection
                .whereEqualTo(Constant.UID, mClassroomData.uid)
                .whereEqualTo(Constant.CLASSROOM_UID, mClassroomData.classroomUid)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

            val optionsOfStudent = FirestoreRecyclerOptions.Builder<MarkedAttendance>()
                .setQuery(queryOfStudent, MarkedAttendance::class.java)
                .build()

            mPresentStudentAdapter = PresentRecordAdapter(this, optionsOfStudent)

            val layout = WrapContentLinearLayoutManager(
                (activity as TeacherActivity),
                LinearLayoutManager.VERTICAL,
                false
            )

            binding.absentRecyclerView.layoutManager = layout
            binding.absentRecyclerView.adapter = mPresentStudentAdapter

            mPresentStudentAdapter.startListening()


        } else {
            ShowToast.showToast(requireContext(), "Something went wrong please try again later!")
        }

        (activity as TeacherActivity).mAttendanceViewModel.mRemoveAttendanceState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            ShowToast.showToast(
                                requireContext(),
                                "Absentee marked."
                            )
                            (activity as TeacherActivity).mAttendanceViewModel.mRemoveAttendanceState.postValue(
                                Resources.Completed()
                            )
                        }

                        is Resources.Error -> {

                        }

                        is Resources.Loading -> {

                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mRemoveAttendanceState.postValue(
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
            mPresentStudentAdapter.startListening()

        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mPresentStudentAdapter.stopListening()

        } catch (e: java.lang.Exception) {
        }

    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setVisibilityOfEmptyStudentLayout(value: Boolean) {
        if (value) {
            binding.presentEmptyLayout.visibility = View.VISIBLE
        } else {
            binding.presentEmptyLayout.visibility = View.GONE
        }
    }

    override fun onMarkAbsent(markedAttendance: MarkedAttendance) {
        (activity as TeacherActivity).mAttendanceViewModel.removeAttendance(markedAttendance)
    }

    override fun isAbsentDataEmpty(value: Boolean) {
        setVisibilityOfEmptyStudentLayout(value)
    }

}