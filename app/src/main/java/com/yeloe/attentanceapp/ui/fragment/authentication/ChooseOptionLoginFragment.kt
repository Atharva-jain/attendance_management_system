package com.yeloe.attentanceapp.ui.fragment.authentication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentChooseOptionLoginBinding
import com.yeloe.attentanceapp.databinding.FragmentLoginBinding
import com.yeloe.attentanceapp.utils.Constant


class ChooseOptionLoginFragment : Fragment() {

    private var _binding: FragmentChooseOptionLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChooseOptionLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.studentLoginButton.setOnClickListener {
            try {
                val action =
                    ChooseOptionLoginFragmentDirections.actionChooseOptionLoginFragmentToLoginFragment(
                        Constant.STUDENT_LOGIN
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {

            }
        }

        binding.teacherLoginButton.setOnClickListener {
            try {
                val action =
                    ChooseOptionLoginFragmentDirections.actionChooseOptionLoginFragmentToLoginFragment(
                        Constant.TEACHER_LOGIN
                    )
                findNavController().navigate(action)
            } catch (e: Exception) {

            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}