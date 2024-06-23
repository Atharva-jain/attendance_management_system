package com.yeloe.attentanceapp.ui.fragment.teacher.classes


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavOptions
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentClassHomeBinding
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.ui.adapter.teacher.view_pager.TeacherClassesViewPager
import com.yeloe.attentanceapp.ui.fragment.teacher.classroom.ClassroomHomeFragmentDirections
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.ShowToast


class ClassHomeFragment : Fragment() {

    private var _binding: FragmentClassHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var mClassesAdapter: TeacherClassesViewPager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassHomeBinding.inflate(inflater, container, false)
        (activity as TeacherActivity).changeAppBarAccordingNavigation(Constant.TEACHER_CLASS)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as TeacherActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        (activity as TeacherActivity).binding.deleteTaskTopAppBar.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection(requireContext())) {
                deleteClassroomDialog(requireContext()) {
                    val currentClassroom =
                        (activity as TeacherActivity).mAttendanceViewModel.getCreateClassroomData
                    (activity as TeacherActivity).mAttendanceViewModel.deleteClassroom(
                        currentClassroom
                    )
                }
            } else {
                ShowToast.showToast(requireContext(), Constant.CHECK_INTERNET_CONNECTION)
            }
        }

        (activity as TeacherActivity).binding.editTaskTopAppBar.setOnClickListener {
            try {
                val signIn = (activity as TeacherActivity).mSignInDetails
                signIn.type = Constant.UPDATE
                val action = ClassHomeFragmentDirections.actionClassHomeFragmentToCreateClassroomFragment(
                    signIn
                )
                findNavController().navigate(action)
            } catch (e: Exception) {
            }
        }

        // initializing the adapter
        mClassesAdapter = TeacherClassesViewPager((activity as TeacherActivity))

        // assign adapter to viewpager2
        binding.classesViewPager2.adapter = mClassesAdapter

        binding.classesTabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                binding.classesViewPager2.currentItem = tab?.position!!
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        binding.classesViewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.classesTabLayout.getTabAt(position)?.select()
            }
        })

        (activity as TeacherActivity).mAttendanceViewModel.mDeleteClassroomState.observe(
            viewLifecycleOwner
        ) { response ->

            if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                when (response) {
                    is Resources.Success -> {
                        val data = response.data
                        Log.d(Constant.STUDENT_LOG, "\n $data")
                        if (data != null) {
                            if (data) {
                                ShowToast.showToast(requireContext(), "Classroom Removed.")
                                findNavController().popBackStack()
                            } else {
                                ShowToast.showToast(
                                    requireContext(),
                                    "Unable to Remove Classroom"
                                )
                            }
                        } else {
                            ShowToast.showToast(requireContext(), "Unable to Remove Classroom")

                        }
                    }

                    is Resources.Error -> {
                        response.message.let { message ->
                            Log.d(Constant.STUDENT_LOG, "Error $message")
                            ShowToast.showToast(requireContext(), message.toString())
                        }

                    }

                    is Resources.Loading -> {

                    }

                    else -> {
                        (activity as TeacherActivity).mAttendanceViewModel.mDeleteClassroomState.postValue(
                            Resources.Completed()
                        )
                    }
                }
            }

        }


    }

    private fun deleteClassroomDialog(
        context: Context,
        onStopTakingAttendanceConfirmed: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Delete Classroom")
            setIcon(R.drawable.delete)
            setMessage("Are you sure you want to delete classroom?")
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