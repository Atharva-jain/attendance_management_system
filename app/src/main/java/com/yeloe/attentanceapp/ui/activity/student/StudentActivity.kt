package com.yeloe.attentanceapp.ui.activity.student

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.yeloe.attentanceapp.R
import com.yeloe.attentanceapp.databinding.ActivityStudentBinding
import com.yeloe.attentanceapp.model.authentication.SignIn
import com.yeloe.attentanceapp.repo.AttendanceRepository
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.utils.GetUid
import com.yeloe.attentanceapp.utils.Resources
import com.yeloe.attentanceapp.view_model.AttendanceViewModel
import com.yeloe.attentanceapp.view_model.factory.AttendanceViewModelFactory

class StudentActivity : AppCompatActivity() {

    lateinit var binding: ActivityStudentBinding
    lateinit var mAttendanceViewModel: AttendanceViewModel
    var mSignInDetails: SignIn = SignIn()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AttendanceRepository()
        val viewModelProviderFactory = AttendanceViewModelFactory(application, repository)
        mAttendanceViewModel = ViewModelProvider(
            this, viewModelProviderFactory
        )[AttendanceViewModel::class.java]

        val uid = GetUid.getUid()

        if (uid != null) mAttendanceViewModel.getStudentUserData(uid)

        mAttendanceViewModel.mGetCurrentUserState.observe(this) { response ->
            when (response) {
                is Resources.Success -> {

                    val data = response.data
                    Log.d(Constant.CREATE_ACCOUNT_LOG, "\n $data")
                    if (data != null) {
                        Glide.with(this).load(data.profileImage).into(binding.profileImageTopAppBar)
                        mSignInDetails = data
                        mAttendanceViewModel.mStateOfGettingCurrentData.postValue(true)
                    } else {
                        setStudentProgressBarVisibility(false)
                        mAttendanceViewModel.mStateOfGettingCurrentData.postValue(false)
                    }
                }

                is Resources.Error -> {
                    response.message.let { message ->
                        Log.d(Constant.TEACHER_LOG, "Error $message")
                    }
                    setStudentProgressBarVisibility(false)
                    mAttendanceViewModel.mStateOfGettingCurrentData.postValue(false)
                }

                is Resources.Loading -> {
                    setStudentProgressBarVisibility(true)
                    mAttendanceViewModel.mStateOfGettingCurrentData.postValue(false)
                }

                else -> {
                    mAttendanceViewModel.mGetCurrentUserState.postValue(
                        Resources.Completed()
                    )
                }
            }
        }

    }

    private fun setStudentProgressBarVisibility(value: Boolean) {
        if (value) {
            binding.studentLinearProgressIndicator.visibility = View.VISIBLE
        } else {
            binding.studentLinearProgressIndicator.visibility = View.GONE
        }
    }

    fun changeProfileImage(image: String) {
        Glide.with(this).load(image).into(binding.profileImageTopAppBar)
    }

    fun changeAppBarAccordingNavigation(navType: String) {
        when (navType) {

            Constant.STUDENT_CLASSROOM_HOME -> {
                binding.closeTopAppBar.visibility = View.GONE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.VISIBLE
                binding.titleTopAppBar.text = navType

            }

            Constant.JOIN_CLASSROOM -> {
                binding.closeTopAppBar.visibility = View.VISIBLE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.GONE
                binding.titleTopAppBar.text = navType
                binding.closeTopAppBar.setImageResource(R.drawable.close)
            }

            Constant.STUDENT_CLASS -> {
                binding.closeTopAppBar.visibility = View.VISIBLE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.VISIBLE
                binding.titleTopAppBar.text = navType
                binding.closeTopAppBar.setImageResource(R.drawable.back)
            }

            Constant.SHOW_ATTENDANCE -> {
                binding.closeTopAppBar.visibility = View.VISIBLE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.GONE
                binding.titleTopAppBar.text = navType
                binding.closeTopAppBar.setImageResource(R.drawable.back)
            }

            Constant.STUDENT_PROFILE -> {
                binding.closeTopAppBar.visibility = View.VISIBLE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.GONE
                binding.titleTopAppBar.text = navType
                binding.closeTopAppBar.setImageResource(R.drawable.back)
            }

            Constant.STUDENT_EDIT_PROFILE -> {
                binding.closeTopAppBar.visibility = View.VISIBLE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.GONE
                binding.titleTopAppBar.text = navType
                binding.closeTopAppBar.setImageResource(R.drawable.back)
            }

            else -> {
                binding.closeTopAppBar.visibility = View.GONE
                binding.taskButtonTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.GONE
                binding.leaveTaskTopAppBar.visibility = View.GONE
                binding.profileLayoutTopAppBar.visibility = View.VISIBLE
                binding.titleTopAppBar.text = navType
            }

        }

    }


}