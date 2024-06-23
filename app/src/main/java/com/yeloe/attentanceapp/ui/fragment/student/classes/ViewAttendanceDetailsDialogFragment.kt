package com.yeloe.attentanceapp.ui.fragment.student.classes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentShowStudentAttendanceDialogBinding
import com.yeloe.attentanceapp.databinding.FragmentViewAttendanceDetailsDialogBinding
import com.yeloe.attentanceapp.utils.Constant
import java.lang.Exception

class ViewAttendanceDetailsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentViewAttendanceDetailsDialogBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewAttendanceDetailsDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showAttendanceCardView.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_viewAttendanceDetailsDialogFragment_to_showAttendanceFragment)
            } catch (e: Exception) {

            }
        }

        binding.showAbsentCardView.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_viewAttendanceDetailsDialogFragment_to_showAbsentFragment)
            } catch (e: Exception) {

            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}