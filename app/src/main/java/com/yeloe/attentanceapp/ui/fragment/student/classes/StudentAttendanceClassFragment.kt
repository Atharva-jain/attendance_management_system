package com.yeloe.attentanceapp.ui.fragment.student.classes

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentStudentAttendanceClassBinding
import com.yeloe.attentanceapp.databinding.FragmentStudentHomeClassroomBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.student.MarkedAttendance
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.activity.student.map.AttendanceMapActivity
import com.yeloe.attentanceapp.ui.activity.student.map.AttendanceMapsActivity
import com.yeloe.attentanceapp.ui.adapter.student.classes.LiveAttendanceListener
import com.yeloe.attentanceapp.ui.adapter.student.classes.ShowLiveAttendanceAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassesAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.PreviousClassListener
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.PreviousClassesAdapter
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.CheckGPSIsEnable
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast


class StudentAttendanceClassFragment : Fragment(), LiveAttendanceListener, PreviousClassListener {

    private var _binding: FragmentStudentAttendanceClassBinding? = null

    private val binding get() = _binding!!

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mClassesCollection =
        mFirebaseFireStoreInstance.collection(Constant.classesCollection)

    private lateinit var mLiveClassAdapter: ShowLiveAttendanceAdapter
    private lateinit var mPreviousClassesAdapter: PreviousClassesAdapter

    private var mCurrentClassroom = JoinClassroom()

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

    private fun calculateAttendancePercentage(totalClasses: Int, presentClasses: Int): Double {
        require(totalClasses >= 0 && presentClasses >= 0) { "Total classes and present classes must be non-negative." }

        return if (totalClasses == 0) {
            0.0 // Avoid division by zero
        } else {
            (presentClasses.toDouble() / totalClasses.toDouble()) * 100.0
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentAttendanceClassBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.STUDENT_CLASS)

        val uid = GetUid.getUid()

        mCurrentClassroom =
            (activity as StudentActivity).mAttendanceViewModel.getJoinedClassroomData

        // get attendance record data are available is here
        (activity as StudentActivity).mAttendanceViewModel.getDataOfAttendance(
            mCurrentClassroom.teacherUid,
            mCurrentClassroom.uid,
            mCurrentClassroom.classroomUid
        )

        (activity as StudentActivity).mAttendanceViewModel.mGetAttendanceRecordState.observe(
            viewLifecycleOwner
        ) { response ->

            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val attendance = response.data

                            Log.d(Constant.TEACHER_LOG, "\n $attendance")

                            if (attendance != null) {

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

                                (activity as StudentActivity).mAttendanceViewModel.setClassesListData(
                                    attendance.classes
                                )

                                (activity as StudentActivity).mAttendanceViewModel.setAttendanceListData(
                                    attendance.markedAttendance
                                )

                                (activity as StudentActivity).mAttendanceViewModel.setStudentData(
                                    mCurrentClassroom
                                )

                                (activity as StudentActivity).mAttendanceViewModel.mGetAttendanceRecordState.postValue(
                                    Resources.Completed()
                                )
                            } else {
                                ShowToast.showToast(
                                    requireContext(),
                                    "Something went please try again later!!"
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
                            (activity as StudentActivity).mAttendanceViewModel.mGetAttendanceRecordState.postValue(
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


        binding.viewAttendanceDetailsCardView.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_studentAttendanceClassFragment_to_viewAttendanceDetailsDialogFragment)
            } catch (e: Exception) {
            }
        }

        if (uid != null) {
            val currentClass =
                (activity as StudentActivity).mAttendanceViewModel.getJoinedClassroomData

            val queryOfLiveClass = mClassesCollection
                .whereEqualTo(Constant.CLASSROOM_UID, currentClass.classroomUid)
                .whereEqualTo(Constant.ALLOW_ATTENDANCE, true)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

            val queryOfPreviousClass = mClassesCollection
                .whereEqualTo(Constant.CLASSROOM_UID, currentClass.classroomUid)
                .whereEqualTo(Constant.ALLOW_ATTENDANCE, false)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)


            val optionsOfLiveClasses = FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(queryOfLiveClass, Class::class.java)
                .build()

            val optionsOfPreviousClasses = FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(queryOfPreviousClass, Class::class.java)
                .build()

            mPreviousClassesAdapter = PreviousClassesAdapter(this, optionsOfPreviousClasses)

            mLiveClassAdapter = ShowLiveAttendanceAdapter(this, optionsOfLiveClasses)

            val layout = WrapContentLinearLayoutManager(
                (activity as StudentActivity),
                LinearLayoutManager.VERTICAL,
                false
            )

            val layout2 = WrapContentLinearLayoutManager(
                (activity as StudentActivity),
                LinearLayoutManager.VERTICAL,
                false
            )

            binding.liveClassesRecyclerView.layoutManager = layout
            binding.liveClassesRecyclerView.adapter = mLiveClassAdapter

            binding.previousClassesRecyclerView.layoutManager = layout2
            binding.previousClassesRecyclerView.adapter = mPreviousClassesAdapter

            mLiveClassAdapter.startListening()
            mPreviousClassesAdapter.startListening()

        } else {
            ShowToast.showToast(requireContext(), "Something went wrong!!")
        }

        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        (activity as StudentActivity).binding.leaveTaskTopAppBar.setOnClickListener {
            leaveClassroomDialog(requireContext()) {
                (activity as StudentActivity).mAttendanceViewModel.leaveClassroom(mCurrentClassroom)
            }
        }

        (activity as StudentActivity).mAttendanceViewModel.mLeaveJoinClassroomState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            ShowToast.showToast(
                                requireContext(),
                                "Classroom leave successfully."
                            )
                            (activity as StudentActivity).mAttendanceViewModel.mLeaveJoinClassroomState.postValue(
                                Resources.Completed()
                            )
                            findNavController().popBackStack()
                        }

                        is Resources.Error -> {

                        }

                        is Resources.Loading -> {

                        }

                        else -> {
                            (activity as StudentActivity).mAttendanceViewModel.mLeaveJoinClassroomState.postValue(
                                Resources.Completed()
                            )

                        }

                    }
                }
            } catch (e: java.lang.Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                ShowToast.showToast(requireContext(), e.message.toString())

            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        try {
            mLiveClassAdapter.startListening()
            mPreviousClassesAdapter.startListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mLiveClassAdapter.stopListening()
            mPreviousClassesAdapter.stopListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun markAttendance(classes: Class) {
        try {
            if (isLocationPermissionGranted()) {
                if (CheckGPSIsEnable.isGPSEnable(requireActivity())) {
                    val joinClassroom =
                        (activity as StudentActivity).mAttendanceViewModel.getJoinedClassroomData
                    AttendanceMapActivity.startActivity(
                        (activity as StudentActivity),
                        classes,
                        joinClassroom
                    )
                } else {
                    ShowToast.showToast(
                        requireContext(),
                        "Please activate location services on your device."
                    )
                }
//                val intent = Intent(activity, AttendanceMapActivity::class.java)
//                startActivity(intent)
            }

        } catch (e: Exception) {
        }
    }

    override fun isLiveDataEmpty(value: Boolean) {
        setVisibilityOfEmptyLiveClassLayout(value)
    }

    private fun setVisibilityOfEmptyLiveClassLayout(value: Boolean) {
        if (value) {
            binding.liveClassEmptyLayout.visibility = View.VISIBLE
        } else {
            binding.liveClassEmptyLayout.visibility = View.GONE
        }
    }

    override fun previousClassClicked(classes: Class) {

    }

    private fun leaveClassroomDialog(
        context: Context,
        onStopTakingAttendanceConfirmed: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Leave Classroom")
            setIcon(R.drawable.delete)
            setMessage("Are you sure you want to leave classroom?")
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

    override fun isPreviousDataEmpty(value: Boolean) {
        setVisibilityOfEmptyPreviousClassLayout(value)
    }

    private fun setVisibilityOfEmptyPreviousClassLayout(value: Boolean) {
        if (value) {
            binding.previousClassEmptyLayout.visibility = View.VISIBLE
        } else {
            binding.previousClassEmptyLayout.visibility = View.GONE
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                (activity as StudentActivity),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                (activity as StudentActivity),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
            )
            requestPermissionLauncher.launch(
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            false
        } else {
            true
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

            }
        }

}