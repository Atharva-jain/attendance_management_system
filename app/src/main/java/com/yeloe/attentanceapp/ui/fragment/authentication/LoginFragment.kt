package com.yeloe.attentanceapp.ui.fragment.authentication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yeloe.attentanceapp.MainActivity
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentLoginBinding
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.utils.CheckInternetConnection
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.utils.isDarkModeOn

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    private var mEmailAddress = ""
    private var mPasswordAddress = ""

    // get the arguments from the Registration fragment
    private val args: LoginFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)

        FirebaseAuth.getInstance().signOut()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDarkModeOn(requireContext())) {
            Glide.with(this).load(R.drawable.login_dark).into(binding.logoImageView)
        } else {
            Glide.with(this).load(R.drawable.login_dark).into(binding.logoImageView)
        }

        // Receive the arguments in a variable
        val type = args.type
        Log.d(Constant.CREATE_ACCOUNT_LOG, "Starting type $type")

        binding.backLayout.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.signUpClickTextView.setOnClickListener {
            try {
                val action =
                    LoginFragmentDirections.actionLoginFragmentToCreateAccountFragment(type)
                findNavController().navigate(action)
            } catch (e: Exception) {
            }
        }

        binding.forgotPasswordClickTextView.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordDialogFragment)
            } catch (e: Exception) {
            }
        }

        (activity as MainActivity).mAttendanceViewModel.mLoginStatusState.observe(
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
                                    isLoginInIsClicked(true)
                                    showToast(Constant.LOG_IN_SUCCESSFUL)
                                    if (type == Constant.TEACHER_LOGIN) {
                                        putUserDataInSharedPreferences(type)
                                        val intent = Intent(
                                            (activity as MainActivity), TeacherActivity::class.java
                                        )
                                        startActivity(intent)
                                        (activity as MainActivity).finish()
                                    } else if (type == Constant.STUDENT_LOGIN) {
                                        putUserDataInSharedPreferences(type)
                                        val intent = Intent(
                                            (activity as MainActivity), StudentActivity::class.java
                                        )
                                        startActivity(intent)
                                        (activity as MainActivity).finish()
                                    }

                                } else {
                                    showToast(Constant.UNABLE_TO_LOGIN_IN)
                                    setProgressBarVisibility(false)
                                    isLoginInIsClicked(true)
                                }
                            } else {
                                showToast(Constant.UNABLE_TO_LOGIN_IN)
                                setProgressBarVisibility(false)
                                isLoginInIsClicked(true)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $message")
                                showToast(message.toString())
                            }
                            setProgressBarVisibility(false)
                            isLoginInIsClicked(true)

                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                            isLoginInIsClicked(false)

                        }

                        else -> {
                            (activity as MainActivity).mAttendanceViewModel.mLoginStatusState.postValue(
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

        (activity as MainActivity).mAttendanceViewModel.mEmailIsExistThatTypeState.observe(
            viewLifecycleOwner
        ) { response ->
            try {
                if (viewLifecycleOwner.lifecycle.currentState == Lifecycle.State.RESUMED) {
                    when (response) {
                        is Resources.Success -> {

                            val map = response.data
                            Log.d(Constant.CREATE_ACCOUNT_LOG, "\n $map")
                            if (map != null) {

                                if (type == Constant.TEACHER_LOGIN) {
                                    map.forEach {
                                        if (it.key == Constant.NOBODY) {
                                            showToast("Account doesn't exist..")
                                            setProgressBarVisibility(false)
                                            isLoginInIsClicked(true)
                                        }
                                        if (it.key == Constant.STUDENT_LOGIN_REVISED) {
                                            showToast("This account already used by student..")
                                            setProgressBarVisibility(false)
                                            isLoginInIsClicked(true)
                                        }
                                        if (it.key == Constant.TEACHER_LOGIN) {
                                            if (it.value) {
                                                (activity as MainActivity).mAttendanceViewModel.loginThroughEmailAndPassword(
                                                    mEmailAddress, mPasswordAddress
                                                )
                                                (activity as MainActivity).mAttendanceViewModel.mLoginStatusState.postValue(
                                                    Resources.Loading()
                                                )
                                            }
                                        }

                                    }
                                }

                                if (type == Constant.STUDENT_LOGIN) {
                                    map.forEach {
                                        if (it.key == Constant.NOBODY) {
                                            showToast("Account doesn't exist..")
                                            setProgressBarVisibility(false)
                                            isLoginInIsClicked(true)
                                        }
                                        if (it.key == Constant.TEACHER_LOGIN_REVISED) {
                                            showToast("This account already used by teacher..")
                                            setProgressBarVisibility(false)
                                            isLoginInIsClicked(true)
                                        }
                                        if (it.key == Constant.STUDENT_LOGIN) {
                                            if (it.value) {
                                                (activity as MainActivity).mAttendanceViewModel.loginThroughEmailAndPassword(
                                                    mEmailAddress, mPasswordAddress
                                                )
                                                (activity as MainActivity).mAttendanceViewModel.mLoginStatusState.postValue(
                                                    Resources.Loading()
                                                )
                                            }
                                        }

                                    }
                                }

//                                if (data) {
//                                    (activity as MainActivity).mAttendanceViewModel.loginThroughEmailAndPassword(
//                                        mEmailAddress, mPasswordAddress
//                                    )
//                                    (activity as MainActivity).mAttendanceViewModel.mLoginStatusState.postValue(
//                                        Resources.Loading()
//                                    )
//                                } else {
//                                    showToast("Account doesn't exist")
//                                    setProgressBarVisibility(false)
//                                    isLoginInIsClicked(true)
//                                }
                            } else {
                                showToast(Constant.UNABLE_TO_LOGIN_IN)
                                setProgressBarVisibility(false)
                                isLoginInIsClicked(true)
                            }
                        }

                        is Resources.Error -> {
                            response.message.let { message ->
                                Log.d(Constant.CREATE_ACCOUNT_LOG, "Error $message")
                                showToast(message.toString())
                            }
                            setProgressBarVisibility(false)
                            isLoginInIsClicked(true)

                        }

                        is Resources.Loading -> {
                            setProgressBarVisibility(true)
                            isLoginInIsClicked(false)

                        }

                        else -> {
                            (activity as MainActivity).mAttendanceViewModel.mLoginStatusState.postValue(
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


        binding.loginButton.setOnClickListener {
            if (CheckInternetConnection.hasInternetConnection((activity as MainActivity))) {
                val email = binding.emailTextInputEditText.text.toString().trim()
                val password = binding.passwordTextInputEditText.text.toString().trim()
                if (validateFields(email, password)) {
                    mEmailAddress = email
                    mPasswordAddress = password
                    (activity as MainActivity).mAttendanceViewModel.checkDataIsEmailAvailableInCollection(
                        email, type
                    )
                    (activity as MainActivity).mAttendanceViewModel.mEmailIsExistThatTypeState.postValue(
                        Resources.Loading()
                    )

                } else {
                    isLoginInIsClicked(true)
                    setProgressBarVisibility(false)
                }
            } else {
                showToast(Constant.CHECK_INTERNET_CONNECTION)
            }
        }


    }


    private fun putUserDataInSharedPreferences(type: String) {
        val sharedPreferences: SharedPreferences? =
            activity?.getSharedPreferences(Constant.USER_SIGN_IN_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        if (type == Constant.TEACHER_LOGIN) {
            editor?.putString(Constant.USER_ACCESS_SHARED, Constant.TEACHER_USER_SHARED)
            editor?.apply()
        } else if (type == Constant.STUDENT_LOGIN) {
            editor?.putString(Constant.USER_ACCESS_SHARED, Constant.STUDENT_USER_SHARED)
            editor?.apply()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(
            requireContext(), message, Toast.LENGTH_LONG
        ).show()
    }

    private fun isLoginInIsClicked(
        value: Boolean
    ) {
        binding.loginButton.isEnabled = value
        binding.emailTextInputLayout.isEnabled = value
        binding.passwordTextInputLayout.isEnabled = value
        binding.backLayout.isEnabled = value
        binding.signUpClickTextView.isEnabled = value
    }

    private fun setProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.logInLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.logInLinearProgressIndicator.visibility = View.GONE
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

    private fun validateFields(email: String, password: String): Boolean {
        return validateEmail(email) && validatePassword(password)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}