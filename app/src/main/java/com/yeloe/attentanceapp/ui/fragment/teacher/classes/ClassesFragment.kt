package com.yeloe.attentanceapp.ui.fragment.teacher.classes

import android.content.Context
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
import com.yeloe.attentanceapp.databinding.FragmentClassesBinding
import com.yeloe.attentanceapp.model.teacher.Class
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassListener
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.LiveClassesAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.PreviousClassListener
import com.yeloe.attentanceapp.ui.adapter.teacher.classes.PreviousClassesAdapter
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.CheckGPSIsEnable
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast
import com.yeloe.attentanceapp.utils.ShowToast.Companion.showToast
import java.lang.Exception


class ClassesFragment : Fragment(), LiveClassListener, PreviousClassListener {

    private var _binding: FragmentClassesBinding? = null

    private val binding get() = _binding!!

    private lateinit var mLiveClassAdapter: LiveClassesAdapter
    private lateinit var mPreviousClassesAdapter: PreviousClassesAdapter

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mClassCollection =
        mFirebaseFireStoreInstance.collection(Constant.classesCollection)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassesBinding.inflate(inflater, container, false)

        val uid = GetUid.getUid()

        if (uid != null) {
            val currentClassRoom =
                (activity as TeacherActivity).mAttendanceViewModel.getCreateClassroomData

            Log.d(Constant.STUDENT_LOG,"$currentClassRoom")

            val queryOfLiveClass = mClassCollection
                .whereEqualTo(Constant.UID, uid)
                .whereEqualTo(Constant.CLASSROOM_UID, currentClassRoom.classroomUid)
                .whereEqualTo(Constant.ALLOW_ATTENDANCE, true)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

            val queryOfPreviousClass = mClassCollection
                .whereEqualTo(Constant.UID, uid)
                .whereEqualTo(Constant.CLASSROOM_UID, currentClassRoom.classroomUid)
                .whereEqualTo(Constant.ALLOW_ATTENDANCE, false)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)

            val optionsOfLiveClasses = FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(queryOfLiveClass, Class::class.java)
                .build()

            val optionsOfPreviousClasses = FirestoreRecyclerOptions.Builder<Class>()
                .setQuery(queryOfPreviousClass, Class::class.java)
                .build()

            mLiveClassAdapter = LiveClassesAdapter(this, optionsOfLiveClasses)

            mPreviousClassesAdapter = PreviousClassesAdapter(this, optionsOfPreviousClasses)

            val layout = WrapContentLinearLayoutManager(
                (activity as TeacherActivity),
                LinearLayoutManager.VERTICAL,
                false
            )

            val layout2 = WrapContentLinearLayoutManager(
                (activity as TeacherActivity),
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
            showToast(requireContext(), "Something went wrong!!")
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TeacherActivity).mAttendanceViewModel.mDeleteClassState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            showToast(requireContext(), "Class Successfully Deleted.")
                            (activity as TeacherActivity).mAttendanceViewModel.mDeleteClassState.postValue(
                                Resources.Completed()
                            )
                        }

                        is Resources.Error -> {

                        }

                        is Resources.Loading -> {

                        }

                        else -> {
                            (activity as TeacherActivity).mAttendanceViewModel.mDeleteClassState.postValue(
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

        (activity as TeacherActivity).mAttendanceViewModel.mStopAttendanceClassState
            .observe(
                viewLifecycleOwner
            ) { response ->
                try {
                    if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                        when (response) {
                            is Resources.Success -> {
                                showToast(
                                    requireContext(),
                                    "Attendance recording for students has been halted."
                                )
                                (activity as TeacherActivity).mAttendanceViewModel.mStopAttendanceClassState.postValue(
                                    Resources.Completed()
                                )
                            }

                            is Resources.Error -> {

                            }

                            is Resources.Loading -> {

                            }

                            else -> {
                                (activity as TeacherActivity).mAttendanceViewModel.mStopAttendanceClassState.postValue(
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


        binding.createClassFloatingActionButton.setOnClickListener {
            if (isLocationPermissionGranted()) {
                if (CheckGPSIsEnable.isGPSEnable((activity as TeacherActivity))) {
                    try {
                        findNavController().navigate(R.id.action_classHomeFragment_to_createClassFragment)
                    } catch (e: Exception) {
                        Log.d(Constant.TEACHER_LOG, "$e")
                    }
                } else {
                    showToast(requireContext(), "Please activate location services on your device.")
                }
            } else {
                showToast(requireContext(), "Please grant permission for location access.")
            }

        }


    }


    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {

            }
        }

    private fun isLocationPermissionGranted(): Boolean {
        return if (ActivityCompat.checkSelfPermission(
                (activity as TeacherActivity),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                (activity as TeacherActivity),
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


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun stopAllowAttendanceInLiveClass(classes: Class) {
        stopAttendanceDialog(requireContext()) {
            val currentClasses = Class(
                uid = classes.uid,
                classroomUid = classes.classroomUid,
                classUid = classes.classUid,
                teacherCollege = classes.teacherCollege,
                teacherImage = classes.teacherImage,
                teacherEmail = classes.teacherEmail,
                teacherName = classes.teacherName,
                classroomName = classes.classroomName,
                classroomCollege = classes.classroomCollege,
                classroomBranch = classes.classroomBranch,
                classroomYear = classes.classroomYear,
                classroomSemester = classes.classroomSemester,
                classCode = classes.classCode,
                timeStamp = classes.timeStamp,
                location = classes.location,
                allowAttendance = false,
            )
            (activity as TeacherActivity).mAttendanceViewModel.stopAttendanceOnClass(currentClasses)
        }
    }

    override fun removeClass(classes: Class) {
        showDeleteConfirmationDialog(requireContext()) {
            (activity as TeacherActivity).mAttendanceViewModel.deleteClass(classes)
        }
    }

    override fun isLiveDataEmpty(value: Boolean) {
        setVisibilityOfEmptyLiveClassLayout(value)
    }

    override fun previousClassClicked(classes: Class) {

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

    private fun setVisibilityOfEmptyLiveClassLayout(value: Boolean) {
        if (value) {
            binding.liveClassEmptyLayout.visibility = View.VISIBLE
        } else {
            binding.liveClassEmptyLayout.visibility = View.GONE
        }
    }

    private fun showDeleteConfirmationDialog(context: Context, onDeleteConfirmed: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Confirm Deletion")
            setMessage("Are you sure you want to delete the class?")
            setPositiveButton("Delete") { dialog, _ ->
                // Call the onDeleteConfirmed function when delete is confirmed
                onDeleteConfirmed.invoke()
                dialog.dismiss()
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun stopAttendanceDialog(
        context: Context,
        onStopTakingAttendanceConfirmed: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Stop Attendance")
            setMessage("Are you sure you want to stop taking attendance?")
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

}