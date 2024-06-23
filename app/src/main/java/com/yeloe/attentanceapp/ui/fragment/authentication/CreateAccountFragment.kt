package com.yeloe.attentanceapp.ui.fragment.authentication

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.yeloe.attentanceapp.MainActivity
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentCreateAccountBinding
import com.yeloe.attentanceapp.databinding.FragmentLoginBinding
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources


class CreateAccountFragment : Fragment() {

    private var _binding: FragmentCreateAccountBinding? = null

    private val binding get() = _binding!!

    // get the arguments from the Registration fragment
    private val args: CreateAccountFragmentArgs by navArgs()

    private var mEmail = ""
    private var mPassword = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Receive the arguments in a variable
        val type = args.type

        binding.backLayout.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.loginInClickTextView.setOnClickListener {
            findNavController().popBackStack()
        }

        (activity as MainActivity).mAttendanceViewModel.mDeleteAccountStatus.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    Log.d(Constant.CREATE_ACCOUNT_LOG, "Delete Account called")
                    when (response) {
                        is Resources.Success -> {
                            val data = response.data
                            Log.d(Constant.CREATE_ACCOUNT_LOG, "\n $data")
                            if (data != null) {
                                if (data) {
                                    showToast("Account setup is in progress. Please try signing in again with this account.")
                                    (activity as MainActivity).mAttendanceViewModel.mDeleteAccountStatus.postValue(
                                        Resources.Completed()
                                    )
                                    setProgressBarVisibility(false)
                                    isSignUpIsClicked(true)
                                } else {
                                    showToast(Constant.ACCOUNT_ALREADY_EXIST_ERROR)
                                    setProgressBarVisibility(false)
                                    isSignUpIsClicked(true)
                                }
                            } else {
                                showToast(Constant.UNABLE_TO_SIGN_IN)
                                setProgressBarVisibility(false)
                                isSignUpIsClicked(true)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $message")
                                showToast(message.toString())
                            }
                            setProgressBarVisibility(false)
                            isSignUpIsClicked(true)

                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                            isSignUpIsClicked(false)

                        }

                        else -> {
                            (activity as MainActivity).mAttendanceViewModel.mDeleteAccountStatus.postValue(
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

        (activity as MainActivity).mAttendanceViewModel.mCheckCurrentUserExist.observe(
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
                                    Log.d(
                                        Constant.CREATE_ACCOUNT_LOG,
                                        "Account delete process started... \n $mEmail $mPassword"
                                    )
                                    (activity as MainActivity).mAttendanceViewModel.deleteAccount(
                                        mEmail,
                                        mPassword
                                    )
                                    (activity as MainActivity).mAttendanceViewModel.mCheckCurrentUserExist.postValue(
                                        Resources.Completed()
                                    )
                                } else {
                                    showToast(Constant.ACCOUNT_ALREADY_EXIST_ERROR)
                                    setProgressBarVisibility(false)
                                    isSignUpIsClicked(true)
                                }
                            } else {
                                showToast(Constant.UNABLE_TO_SIGN_IN)
                                setProgressBarVisibility(false)
                                isSignUpIsClicked(true)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $message")
                                showToast(message.toString())
                            }
                            setProgressBarVisibility(false)
                            isSignUpIsClicked(true)

                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                            isSignUpIsClicked(false)

                        }

                        else -> {
                            (activity as MainActivity).mAttendanceViewModel.mCheckCurrentUserExist.postValue(
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

        (activity as MainActivity).mAttendanceViewModel.mCreateAccountStatusState.observe(
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
                                    setProgressBarVisibility(false)
                                    isSignUpIsClicked(true)
                                    showToast(Constant.SIGN_IN_SUCCESSFUL)
                                    (activity as MainActivity).mAttendanceViewModel.mCreateAccountStatusState.postValue(
                                        Resources.Completed()
                                    )
                                    val action =
                                        CreateAccountFragmentDirections.actionCreateAccountFragmentToSignUpBasicDetailsFromFragment(
                                            type
                                        )
                                    findNavController().navigate(action)
                                } else {
                                    showToast(Constant.UNABLE_TO_SIGN_IN)
                                    setProgressBarVisibility(false)
                                    isSignUpIsClicked(true)
                                }
                            } else {
                                showToast(Constant.UNABLE_TO_SIGN_IN)
                                setProgressBarVisibility(false)
                                isSignUpIsClicked(true)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                if (Constant.ACCOUNT_ALREADY_EXIST_ERROR == message) {
                                    Log.d(
                                        Constant.CREATE_ACCOUNT_LOG,
                                        "Account already exist called... \n $mEmail"
                                    )
                                    (activity as MainActivity).mAttendanceViewModel.checkAccountExist(
                                        mEmail
                                    )
                                } else {
                                    Log.d(
                                        Constant.CREATE_ACCOUNT_LOG,
                                        "Account already exist notttt called..."
                                    )
                                    Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $message")
                                    showToast(message.toString())
                                    setProgressBarVisibility(false)
                                    isSignUpIsClicked(true)
                                }
                            }
                            // setProgressBarVisibility(false)
                            // isSignUpIsClicked(true)

                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                            isSignUpIsClicked(false)

                        }

                        else -> {
                            (activity as MainActivity).mAttendanceViewModel.mCreateAccountStatusState.postValue(
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

        binding.submitButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection((activity as MainActivity))) {
                val email = binding.emailTextInputEditText.text.toString().trim()
                val password = binding.passwordTextInputEditText.text.toString().trim()
                val rePassword = binding.rePasswordTextInputEditText.text.toString().trim()
                mEmail = email
                mPassword = password
                if (validateFields(email, password, rePassword)) {
                    (activity as MainActivity).mAttendanceViewModel.createAccountFromEmailAndPassword(
                        email,
                        password
                    )
                    (activity as MainActivity).mAttendanceViewModel.mCreateAccountStatusState.postValue(
                        Resources.Loading()
                    )

                } else {
                    isSignUpIsClicked(true)
                    setProgressBarVisibility(false)
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

    private fun isSignUpIsClicked(
        value: Boolean
    ) {
        binding.submitButton.isEnabled = value
        binding.emailTextInputLayout.isEnabled = value
        binding.passwordTextInputLayout.isEnabled = value
        binding.rePasswordTextInputLayout.isEnabled = value
        binding.backLayout.isEnabled = value
        binding.loginInClickTextView.isEnabled = value
    }

    private fun setProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.signUpLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.signUpLinearProgressIndicator.visibility = View.GONE
        }
    }


    // Function to validate email field
    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailTextInputEditText.error = Constant.EMAIL_EMPTY
            return false
        }
        return true
    }

    // Function to validate password field
    private fun validatePassword(password: String): Boolean {
        if (password.isEmpty()) {
            binding.passwordTextInputEditText.error = Constant.PASSWORD_EMPTY
            return false
        }
        return true
    }

    // Function to validate re-entered password field
    private fun validateRePassword(password: String, rePassword: String): Boolean {
        if (rePassword.isEmpty()) {
            binding.rePasswordTextInputEditText.error = "Please re-enter your password"
            return false
        }
        if (password != rePassword) {
            binding.rePasswordTextInputEditText.error = "Passwords do not match"
            return false
        }
        return true
    }

    // Function to validate all fields
    private fun validateFields(email: String, password: String, rePassword: String): Boolean {
        return validateEmail(email) && validatePassword(password) && validateRePassword(
            password,
            rePassword
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}