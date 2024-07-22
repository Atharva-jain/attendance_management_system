package com.yeloe.attentanceapp.ui.fragment.authentication

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.yeloe.attentanceapp.MainActivity
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentForgotPasswordDialogBinding
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.isDarkModeOn
import java.lang.Exception


class ForgotPasswordDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentForgotPasswordDialogBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgotPasswordDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDarkModeOn(requireContext())) {
            Glide.with(this).load(R.drawable.forgot_password_dark).into(binding.imageView)
        } else {
            Glide.with(this).load(R.drawable.forgot_password_light).into(binding.imageView)
        }

        (activity as MainActivity).mAttendanceViewModel.mForgotPasswordState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {
                            val data = response.data
                            Log.d(Constant.CREATE_ACCOUNT_LOG, "\n $data")
                            if (data != null) {
                                if (data) {

                                    isForgotPasswordInIsClicked(true)
                                    showToast(Constant.FORGOT_PASSWORD_SUCCESSFUL)

                                } else {
                                    showToast(Constant.UNABLE_TO_FORGOT_PASSWORD_IN)

                                    isForgotPasswordInIsClicked(true)
                                }
                            } else {
                                showToast(Constant.UNABLE_TO_FORGOT_PASSWORD_IN)

                                isForgotPasswordInIsClicked(true)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $message")
                                showToast(message.toString())
                            }

                            isForgotPasswordInIsClicked(true)

                        }

                        is Resources.Loading -> {

                            isForgotPasswordInIsClicked(false)

                        }

                        else -> {
                            (activity as MainActivity).mAttendanceViewModel.mForgotPasswordState.postValue(
                                Resources.Completed()
                            )
                        }
                    }
                }

            } catch (e: Exception) {
                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $e")
                showToast(e.message.toString())
            }

        }


        binding.forgotPasswordButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection((activity as MainActivity))) {
                val email = binding.emailTextInputEditText.text.toString().trim()
                if (validateFields(email)) {
                    (activity as MainActivity).mAttendanceViewModel.sendForgotPasswordLink(
                        email
                    )
                    (activity as MainActivity).mAttendanceViewModel.mForgotPasswordState.postValue(
                        Resources.Loading()
                    )

                } else {
                    isForgotPasswordInIsClicked(true)

                }
            } else {
                showToast(Constant.CHECK_INTERNET_CONNECTION)
            }
        }


    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun isForgotPasswordInIsClicked(
        value: Boolean
    ) {
        binding.forgotPasswordButton.isEnabled = value
        binding.emailTextInputLayout.isEnabled = value
    }

    // Function to validate email field
    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailTextInputEditText.error = Constant.EMAIL_EMPTY
            return false
        }
        return true
    }

    private fun validateFields(email: String): Boolean {
        return validateEmail(email)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}