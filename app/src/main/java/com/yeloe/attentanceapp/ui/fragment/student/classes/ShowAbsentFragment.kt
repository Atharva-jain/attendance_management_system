package com.yeloe.attentanceapp.ui.fragment.student.classes

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentMarkedAbsentBinding
import com.yeloe.attentanceapp.databinding.FragmentShowAbsentBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.adapter.student.classes.attendance.ShowAbsentAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.attendance.AbsentRecordAdapter
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import java.util.Calendar


class ShowAbsentFragment : Fragment() {

    private var _binding: FragmentShowAbsentBinding? = null

    private val binding get() = _binding!!

    private var mClasses: ArrayList<Class> = ArrayList()
    private var mAttendances: ArrayList<MarkedAttendance> = ArrayList()
    private var mAbsentList: ArrayList<MarkedAttendance> = ArrayList()
    private var mJoinedClassroom: JoinClassroom = JoinClassroom()
    private lateinit var mAbsentAdapter: ShowAbsentAdapter
    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mAttendanceCollection =
        mFirebaseFireStoreInstance.collection(Constant.attendance)

    private fun getAbsentList(
        classes: ArrayList<Class>,
        attendance: ArrayList<MarkedAttendance>
    ): ArrayList<MarkedAttendance> {

        val absentList = ArrayList<MarkedAttendance>()

        if (classes.isEmpty()) {
            return absentList
        }

        for (classItem in classes) {
            val classUid = classItem.classUid
            var attended = false

            for (attendanceItem in attendance) {
                if (attendanceItem.classUid == classUid) {
                    attended = true
                    break
                }
            }

            if (!attended) {
                // Student is absent from this class
                val date = Calendar.getInstance().time
                val attendanceUid = mAttendanceCollection.document().id
                val markedAttendance = MarkedAttendance(
                    classUid = classItem.classUid,
                    classroomName = classItem.classroomName,
                    classroomCollege = classItem.classroomCollege,
                    classroomBranch = classItem.classroomBranch,
                    classroomYear = classItem.classroomYear,
                    classroomSemester = classItem.classroomSemester,
                    classCode = classItem.classCode,
                    teacherCollege = classItem.teacherCollege,
                    teacherImage = classItem.teacherImage,
                    timeStamp = date,
                    teacherEmail = classItem.teacherEmail,
                    teacherName = classItem.teacherName,
                    location = classItem.location,
                    uid = mJoinedClassroom.uid,
                    attendanceUid = attendanceUid,
                    teacherUid = mJoinedClassroom.teacherUid,
                    joinClassRoomUid = mJoinedClassroom.joinClassRoomUid,
                    classroomUid = mJoinedClassroom.classroomUid,
                    studentYear = mJoinedClassroom.studentYear,
                    studentSemester = mJoinedClassroom.studentSemester,
                    studentProfileImage = mJoinedClassroom.studentProfileImage,
                    studentName = mJoinedClassroom.studentName,
                    studentFaceImage = mJoinedClassroom.studentFaceImage,
                    studentCollege = mJoinedClassroom.studentCollege,
                    studentBranch = mJoinedClassroom.studentBranch,
                    keywords = mJoinedClassroom.keywords,
                )
                absentList.add(markedAttendance)
            }
        }

        return absentList
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowAbsentBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.SHOW_ATTENDANCE)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        mClasses = (activity as StudentActivity).mAttendanceViewModel.getClassesListData
        mAttendances = (activity as StudentActivity).mAttendanceViewModel.getAttendanceListData
        mJoinedClassroom = (activity as StudentActivity).mAttendanceViewModel.getStudentData

        Log.d(Constant.TEACHER_LOG, "Classes $mClasses")
        Log.d(Constant.TEACHER_LOG, "Attendance $mAttendances")

        mAbsentList = getAbsentList(mClasses, mAttendances)
        checkListIsEmpty()

        Log.d(Constant.TEACHER_LOG, "Absent Data ${mAbsentList.size} \n $mAbsentList")

        mAbsentAdapter = ShowAbsentAdapter()

        val layout = WrapContentLinearLayoutManager(
            (activity as StudentActivity),
            LinearLayoutManager.VERTICAL,
            false
        )

        binding.absentRecyclerView.layoutManager = layout
        binding.absentRecyclerView.adapter = mAbsentAdapter

        mAbsentAdapter.submitList(mAbsentList.toList())

        (activity as StudentActivity).mAttendanceViewModel.mMarkedAttendanceState.observe(
            viewLifecycleOwner
        ) { response ->
            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        Log.d(Constant.STUDENT_LOG, "\n $data")
                        if (data != null) {
                            if (data) {
                                ShowToast.showToast(requireContext(), "Attendance Marked!!")
                            } else {
                                ShowToast.showToast(
                                    requireContext(),
                                    "Unable to Marked Attendance"
                                )
                                setVisibilityOfProgressbar(false)
                            }
                        } else {
                            ShowToast.showToast(requireContext(), "Unable to Marked Attendance")
                            setVisibilityOfProgressbar(false)
                        }
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.STUDENT_LOG, "Error $message")
                            ShowToast.showToast(requireContext(), message.toString())
                        }
                        setVisibilityOfProgressbar(false)
                    }

                    is Resources.Loading -> {
                        setVisibilityOfProgressbar(true)
                    }

                    else -> {
                        (activity as StudentActivity).mAttendanceViewModel.mMarkedAttendanceState.postValue(
                            Resources.Completed()
                        )
                    }
                }
            }
        }
    }

    private fun setVisibilityOfProgressbar(value: Boolean) {
        if (value) {
            binding.absentProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.absentProgressIndicator.visibility = View.GONE
        }
    }

    private fun setVisibilityOfEmptyAbsentLayout(value: Boolean) {
        if (value) {
            binding.absentEmptyLayout.visibility = View.VISIBLE
        } else {
            binding.absentEmptyLayout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun checkListIsEmpty() {
        if (mAbsentList.isEmpty()) {
            setVisibilityOfEmptyAbsentLayout(true)
        } else {
            setVisibilityOfEmptyAbsentLayout(false)
        }
    }


}