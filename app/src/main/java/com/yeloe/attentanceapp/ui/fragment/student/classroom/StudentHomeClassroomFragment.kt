package com.yeloe.attentanceapp.ui.fragment.student.classroom

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
import com.yeloe.attentanceapp.databinding.FragmentStudentHomeClassroomBinding
import com.yeloe.attentanceapp.model.student.JoinClassroom
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity

import com.yeloe.attentanceapp.ui.adapter.student.classroom.JoinedClassAdapter
import com.yeloe.attentanceapp.ui.adapter.student.classroom.JoinedClassroomListener
import com.yeloe.attentanceapp.ui.adapter.teacher.ClassroomDetailsAdapter
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.ShowToast
import java.lang.Exception


class StudentHomeClassroomFragment : Fragment(), JoinedClassroomListener {

    private var _binding: FragmentStudentHomeClassroomBinding? = null

    private val binding get() = _binding!!

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mJoinClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.joinClassroomCollection)

    private lateinit var mJoinedClassAdapter: JoinedClassAdapter

    override fun onStart() {
        super.onStart()
        try {
            mJoinedClassAdapter.startListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mJoinedClassAdapter.stopListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentHomeClassroomBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.STUDENT_CLASSROOM_HOME)

        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        (activity as StudentActivity).binding.profileLayoutTopAppBar.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_studentHomeClassroomFragment_to_studentProfileFragment)
            } catch (e: Exception) {
            }
        }

        val uid = GetUid.getUid()

        if (uid != null) {
            val query = mJoinClassroomCollection.whereEqualTo(Constant.UID, uid)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)
            val options = FirestoreRecyclerOptions.Builder<JoinClassroom>()
                .setQuery(query, JoinClassroom::class.java)
                .build()
            mJoinedClassAdapter = JoinedClassAdapter(this, options)
            val layout = WrapContentLinearLayoutManager(
                (activity as StudentActivity),
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.studentClassroomRecyclerView.layoutManager = layout
            binding.studentClassroomRecyclerView.adapter = mJoinedClassAdapter
            mJoinedClassAdapter.startListening()
        } else {
            ShowToast.showToast(requireContext(), "Something went wrong!!")
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as StudentActivity).mAttendanceViewModel.mStateOfGettingCurrentData.observe(
            viewLifecycleOwner
        ) {
            try {
                if (it) {
                    binding.floatingActionButton.isEnabled = true
                } else {
                    ShowToast.showToast(
                        requireContext(),
                        "Unable to get user data. please try again later or check internet connection"
                    )
                    binding.floatingActionButton.isEnabled = false
                }
            } catch (e: java.lang.Exception) {
            }

        }

        binding.floatingActionButton.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_studentHomeClassroomFragment_to_studentJoinClassroomFragment)
            } catch (e: Exception) {
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClickClassroom(createClassroom: JoinClassroom) {
        (activity as StudentActivity).mAttendanceViewModel.setJoinedClassroomData(joinClassroom = createClassroom)
        try {
            findNavController().navigate(R.id.action_studentHomeClassroomFragment_to_studentAttendanceClassFragment)
        } catch (e: Exception) {
        }

    }

    override fun isClassRoomEmpty(value: Boolean) {
        setIsEmptyClassVisibility(value)
    }

    private fun setIsEmptyClassVisibility(value: Boolean) {
        if (value) {
            binding.emptyClassroomLayout.visibility = View.VISIBLE
        } else {
            binding.emptyClassroomLayout.visibility = View.GONE
        }
    }


}