package com.yeloe.attentanceapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.lifecycle.ViewModelProvider
import com.yeloe.attentanceapp.databinding.ActivityMainBinding
import com.yeloe.attentanceapp.repo.AttendanceRepository
import com.yeloe.attentanceapp.utils.Constant
import com.yeloe.attentanceapp.view_model.AttendanceViewModel
import com.yeloe.attentanceapp.view_model.factory.AttendanceViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var mAttendanceViewModel: AttendanceViewModel
    lateinit var mBackPressedListener: OnBackPressedDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = AttendanceRepository()
        val viewModelProviderFactory = AttendanceViewModelFactory(application, repository)
        mAttendanceViewModel = ViewModelProvider(
            this,
            viewModelProviderFactory
        )[AttendanceViewModel::class.java]

    }


}