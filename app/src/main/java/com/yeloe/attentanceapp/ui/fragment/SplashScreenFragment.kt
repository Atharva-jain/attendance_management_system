package com.yeloe.attentanceapp.ui.fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.FragmentSplashScreenBinding
import com.yeloe.attentanceapp.ui.activity.student.StudentActivity
import com.yeloe.attentanceapp.ui.activity.teacher.TeacherActivity
import com.yeloe.attentanceapp.utils.Constant


class SplashScreenFragment : Fragment() {


    private var _binding: FragmentSplashScreenBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSplashScreenBinding.inflate(inflater, container, false)

        if (isDarkModeOn()) {
            Glide.with(this).load(R.drawable.splash_screen_night).into(binding.splashImageView)
        } else {
            Glide.with(this).load(R.drawable.splash_screen).into(binding.splashImageView)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val sh: SharedPreferences? =
                    activity?.getSharedPreferences(Constant.USER_SIGN_IN_PREFERENCES, MODE_PRIVATE)
                val userType = sh?.getString(Constant.USER_ACCESS_SHARED, "")
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    when (userType) {
                        Constant.STUDENT_USER_SHARED -> {
                            val intent = Intent(activity, StudentActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }

                        Constant.TEACHER_USER_SHARED -> {
                            val intent = Intent(activity, TeacherActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }

                        Constant.USER_NOT_SIGN_IN -> {
                            findNavController().navigate(R.id.action_splashScreenFragment_to_chooseOptionLoginFragment)
                        }

                        else -> {
                            findNavController().navigate(R.id.action_splashScreenFragment_to_chooseOptionLoginFragment)
                        }
                    }
                } else {
                    findNavController().navigate(R.id.action_splashScreenFragment_to_chooseOptionLoginFragment)
                }


            } catch (e: Exception) {
            }

        }, Constant.SPLASH_SCREEN_DELAY)

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    // check if the app is in dark mode or not
    fun isDarkModeOn(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val isDarkModeOn = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
        return isDarkModeOn
    }

}