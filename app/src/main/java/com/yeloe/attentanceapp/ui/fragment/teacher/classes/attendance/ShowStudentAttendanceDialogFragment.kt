package com.yeloe.attentanceapp.ui.fragment.teacher.classes.attendance

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentShowStudentAttendanceDialogBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast


class ShowStudentAttendanceDialogFragment(
    val joinClassroom: JoinClassroom,
    private val listener: OnNavigateListener
) :
    BottomSheetDialogFragment() {

    private var _binding: FragmentShowStudentAttendanceDialogBinding? = null

    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShowStudentAttendanceDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun getCountOfClass(classes: ArrayList<Class>): Int {
        return if (classes.isEmpty()) {
            0
        } else {
            classes.size
        }
    }

    private fun getCountOfAttendance(attendance: ArrayList<MarkedAttendance>): Int {
        return if (attendance.isEmpty()) {
            0
        } else {
            attendance.size
        }
    }

    private fun getPresentPercentage(classesCount: Int, presentCount: Int): Int {
        if (classesCount == 0 || presentCount == 0) {
            return 0
        }
        return (presentCount / classesCount) * 100
    }

    private fun calculateAttendancePercentage(totalClasses: Int, presentClasses: Int): Double {
        require(totalClasses >= 0 && presentClasses >= 0) { "Total classes and present classes must be non-negative." }

        return if (totalClasses == 0) {
            0.0 // Avoid division by zero
        } else {
            (presentClasses.toDouble() / totalClasses.toDouble()) * 100.0
        }
    }


//    private fun calculateAttendancePercentage(totalClasses: Int, presentClasses: Int): Double {
//        //require(totalClasses >= 0 && presentClasses >= 0) { "Total classes and present classes must be non-negative." }
//        return if ((totalClasses >= 0 && presentClasses >= 0)) {
//            0.0
//        } else if (totalClasses == 0) {
//            0.0 // Avoid division by zero
//        } else {
//            (presentClasses.toDouble() / totalClasses.toDouble()) * 100.0
//        }
//    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TeacherActivity).mAttendanceViewModel.getDataOfAttendance(
            joinClassroom.teacherUid,
            joinClassroom.uid,
            joinClassroom.classroomUid
        )

        binding.removeStudentButton.setOnClickListener {
            try {
                removeStudentClassroomDialog((activity as TeacherActivity)) {
                    listener.onStudentRemove(joinClassroom)
                }
            } catch (e: Exception) {
            }
        }

        (activity as TeacherActivity).mAttendanceViewModel.mGetAttendanceRecordState.observe(
            viewLifecycleOwner
        ) { response ->

            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val attendance = response.data

                            Log.d(Constant.TEACHER_LOG, "\n $attendance")

                            if (attendance != null) {
                                setVisibilityOfAttendanceProgressBar(false)

                                val classCount = getCountOfClass(classes = attendance.classes)
                                val attendanceCount =
                                    getCountOfAttendance(attendance = attendance.markedAttendance)
                                val totalAttendance =
                                    calculateAttendancePercentage(classCount, attendanceCount)
                                Log.d(Constant.TEACHER_LOG, "$classCount")
                                Log.d(Constant.TEACHER_LOG, "$attendanceCount")
                                Log.d(Constant.TEACHER_LOG, "$totalAttendance")
                                val attendanceCountPer = totalAttendance * 100
                                Log.d(Constant.TEACHER_LOG, "$attendanceCountPer")
                                binding.attendanceResultProgressBar.setProgress(
                                    totalAttendance.toInt(),
                                    true
                                )

                                binding.precentageResultTextView.text =
                                    "${totalAttendance.toInt()}%"
                                binding.classResultTextView.text =
                                    "Classes attend: $attendanceCount/$classCount"

                                (activity as TeacherActivity).mAttendanceViewModel.setClassesListData(
                                    attendance.classes
                                )

                                (activity as TeacherActivity).mAttendanceViewModel.setAttendanceListData(
                                    attendance.markedAttendance
                                )

                                (activity as TeacherActivity).mAttendanceViewModel.setStudentData(
                                    joinClassroom
                                )

                                (activity as TeacherActivity).mAttendanceViewModel.mGetAttendanceRecordState.postValue(
                                    Resources.Completed()
                                )
                            } else {
                                ShowToast.showToast(
                                    requireContext(),
                                    "Something went please try again later!!"
                                )
                                setVisibilityOfAttendanceProgressBar(false)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.TEACHER_LOG, "Error $message")
                                ShowToast.showToast(requireContext(), message.toString())
                            }
                            setVisibilityOfAttendanceProgressBar(false)
                        }

                        is Resources.Loading -> {
                            setVisibilityOfAttendanceProgressBar(true)
                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mGetAttendanceRecordState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                setVisibilityOfAttendanceProgressBar(false)
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                ShowToast.showToast(requireContext(), e.message.toString())
            }
        }

        binding.markedAbsentCardView.setOnClickListener {
            listener.onNavigateAbsent()
        }

        binding.markedAttendanceCardView.setOnClickListener {
            listener.onNavigatePresent()
        }

    }

    private fun setVisibilityOfAttendanceProgressBar(value: Boolean) {
        if (value) {
            binding.attendanceProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.attendanceProgressIndicator.visibility = View.GONE
        }
    }

    private fun removeStudentClassroomDialog(
        context: Context,
        onStopTakingAttendanceConfirmed: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Remove Student from Classroom")
            setIcon(R.drawable.delete)
            setMessage("Are you sure you want to remove student from classroom?")
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

interface OnNavigateListener {
    fun onNavigateAbsent()
    fun onNavigatePresent()
    fun onStudentRemove(joinClassroom: JoinClassroom)
}