package com.yeloe.attentanceapp.ui.fragment.student.classes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentShowAttendanceBinding
import com.yeloe.attentanceapp.databinding.FragmentStudentAttendanceClassBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.adapter.student.classes.attendance.IsEmptyAttendanceListener
import com.yeloe.attentanceapp.ui.adapter.student.classes.attendance.ShowAttendanceAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.attendance.PresentRecordAdapter
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.ShowToast

class ShowAttendanceFragment : Fragment(), IsEmptyAttendanceListener {

    private var _binding: FragmentShowAttendanceBinding? = null

    private val binding get() = _binding!!
    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()

    private var mCurrentClassroom = JoinClassroom()

    private val mAttendanceCollection =
        mFirebaseFireStoreInstance.collection(Constant.attendance)

    private lateinit var mShowAttendanceAdapter: ShowAttendanceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowAttendanceBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.SHOW_ATTENDANCE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mCurrentClassroom =
            (activity as StudentActivity).mAttendanceViewModel.getJoinedClassroomData

        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        val uid = GetUid.getUid()
        if (uid != null) {

            val queryOfStudent = mAttendanceCollection
                .whereEqualTo(Constant.UID, mCurrentClassroom.uid)
                .whereEqualTo(Constant.CLASSROOM_UID, mCurrentClassroom.classroomUid)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

            val optionsOfStudent = FirestoreRecyclerOptions.Builder<MarkedAttendance>()
                .setQuery(queryOfStudent, MarkedAttendance::class.java)
                .build()

            mShowAttendanceAdapter = ShowAttendanceAdapter(this, optionsOfStudent)

            val layout = WrapContentLinearLayoutManager(
                (activity as StudentActivity),
                LinearLayoutManager.VERTICAL,
                false
            )

            binding.attendanceRecyclerView.layoutManager = layout
            binding.attendanceRecyclerView.adapter = mShowAttendanceAdapter

            mShowAttendanceAdapter.startListening()


        } else {
            ShowToast.showToast(requireContext(), "Something went wrong please try again later!")
        }


    }

    override fun onStart() {
        super.onStart()
        try {
            mShowAttendanceAdapter.startListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mShowAttendanceAdapter.stopListening()
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

    override fun isEmptyAttendanceListener(value: Boolean) {
        setVisibilityOfEmptyStudentLayout(value)
    }
}