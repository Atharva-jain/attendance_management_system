package com.yeloe.attentanceapp.ui.fragment.student.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yeloe.attentanceapp.MainActivity
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentProfileBinding
import com.yeloe.attentanceapp.databinding.FragmentStudentProfileBinding
import com.yeloe.attentanceapp.model.authentication.SignIn
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.utils.Constant
import java.lang.Exception


class StudentProfileFragment : Fragment() {

    private var _binding: FragmentStudentProfileBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentProfileBinding.inflate(inflater, container, false)
        (activity as StudentActivity).changeAppBarAccordingNavigation(Constant.STUDENT_PROFILE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setStudentSignInInformation((activity as StudentActivity).mSignInDetails)

        (activity as StudentActivity).binding.closeTopAppBar.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.editProfileCardView.setOnClickListener {
            try {
                findNavController().navigate(R.id.action_studentProfileFragment_to_studentEditProfileFragment)
            } catch (e: Exception) {

            }
        }

        binding.logOutProfileCardView.setOnClickListener {
            logOutDialog((activity as StudentActivity)) {
                FirebaseAuth.getInstance().signOut()
                putUserDataInSharedPreferences()
                val intent = Intent((activity as StudentActivity), MainActivity::class.java)
                startActivity(intent)
                (activity as StudentActivity).finish()
            }
        }

    }

    private fun putUserDataInSharedPreferences() {
        val sharedPreferences: SharedPreferences? =
            activity?.getSharedPreferences(Constant.USER_SIGN_IN_PREFERENCES, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor? = sharedPreferences?.edit()
        editor?.putString(Constant.USER_ACCESS_SHARED, Constant.USER_NOT_SIGN_IN)
        editor?.apply()
    }

    private fun logOutDialog(
        context: Context,
        onStopTakingAttendanceConfirmed: () -> Unit
    ) {
        val builder = AlertDialog.Builder(context)
        builder.apply {
            setTitle("Log out")
            setIcon(R.drawable.logout)
            setMessage("Are you sure you want to log out?")
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

    private fun setStudentSignInInformation(signIn: SignIn) {
        binding.nameTextView.text = signIn.name
        binding.collegeTextView.text = signIn.college
        binding.infoTextView.text = "${signIn.year}:${signIn.branch}:${signIn.semester}"
        Glide.with(this).load(signIn.profileImage).into(binding.profileImageView)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}