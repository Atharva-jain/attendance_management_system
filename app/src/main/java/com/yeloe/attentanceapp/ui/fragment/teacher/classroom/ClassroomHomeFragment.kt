package com.yeloe.attentanceapp.ui.fragment.teacher.classroom

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentClassroomHomeBinding
import com.yeloe.attentanceapp.model.teacher.CreateClassroom
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.adapter.teacher.ClassroomDetailsAdapter
import com.yeloe.attentanceapp.ui.adapter.teacher.ClassroomListener
import com.yeloe.attentanceapp.ui.layout_manager.WrapContentLinearLayoutManager
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.ShowToast.Companion.showToast


class ClassroomHomeFragment : Fragment(), ClassroomListener {

    private var _binding: FragmentClassroomHomeBinding? = null

    private val binding get() = _binding!!
    private lateinit var mClassroomDetailsAdapter: ClassroomDetailsAdapter

    private val mFirebaseFireStoreInstance = FirebaseFirestore.getInstance()
    private val mTeacherClassroomCollection =
        mFirebaseFireStoreInstance.collection(Constant.teacherClassRoomCollection)

    override fun onStart() {
        super.onStart()
        try {
            mClassroomDetailsAdapter.startListening()
        } catch (e: java.lang.Exception) {
        }

    }

    override fun onStop() {
        super.onStop()
        try {
            mClassroomDetailsAdapter.stopListening()
        } catch (e: java.lang.Exception) {
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassroomHomeBinding.inflate(inflater, container, false)
        (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_CLASSROOM_HOME)


        val uid = FirebaseAuth.getInstance().currentUser?.uid

        Log.d(Constant.TEACHER_LOG, "ClassroomHomeFragment $uid")

        if (uid != null) {

            Log.d(Constant.TEACHER_LOG, "ClassroomHomeFragment if is true $uid")

            val query = mTeacherClassroomCollection.whereEqualTo(Constant.UID, uid)
                .orderBy(Constant.TIMESTAMP, Query.Direction.DESCENDING)
            val options = FirestoreRecyclerOptions.Builder<CreateClassroom>()
                .setQuery(query, CreateClassroom::class.java)
                .build()
            mClassroomDetailsAdapter = ClassroomDetailsAdapter(this, options)
            val layout = WrapContentLinearLayoutManager(
                (activity as TeacherActivity),
                LinearLayoutManager.VERTICAL,
                false
            )
            binding.classroomRecyclerView.layoutManager = layout
            binding.classroomRecyclerView.adapter = mClassroomDetailsAdapter
            mClassroomDetailsAdapter.startListening()

            Log.d(Constant.TEACHER_LOG, "ClassroomHomeFragment if close $uid")

        } else {
            showToast(requireContext(), "Something went wrong!!")
        }



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TeacherActivity).binding.profileLayoutTopAppBar.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_classroomHomeFragment_to_profileFragment)
            } catch (e: java.lang.Exception) {
            }
        }


        (activity as TeacherActivity).mAttendanceViewModel.mStateOfGettingCurrentData.observe(
            viewLifecycleOwner
        ) {
            try {
                if (it) {
                    binding.floatingActionButton.isEnabled = true
                } else {
                    showToast(
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
                val signIn = (activity as TeacherActivity).mSignInDetails
                signIn.type = Constant.CREATE
                val action =
                    ClassroomHomeFragmentDirections.actionClassroomHomeFragmentToCreateClassroomFragment(
                        signIn
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setIsEmptyClassVisibility(value: Boolean) {
        if (value) {
            binding.emptyClassroomLayout.visibility = View.VISIBLE
        } else {
            binding.emptyClassroomLayout.visibility = View.GONE
        }
    }

    override fun onClickClassroom(createClassroom: CreateClassroom) {
        try {
            (activity as TeacherActivity).mAttendanceViewModel.setCreateClassroomData(
                createClassroom
            )
            findNavController().navigate(R.id.action_classroomHomeFragment_to_classHomeFragment)
        } catch (e: java.lang.Exception) {
        }
    }

    override fun isClassRoomEmpty(value: Boolean) {
        setIsEmptyClassVisibility(value)
    }


}

