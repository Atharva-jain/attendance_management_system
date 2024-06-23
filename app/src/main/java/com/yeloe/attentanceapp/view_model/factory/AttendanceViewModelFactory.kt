package com.yeloe.attentanceapp.view_model.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yeloe.attentanceapp.repo.AttendanceRepository
import com.yeloe.attentanceapp.view_model.AttendanceViewModel

class AttendanceViewModelFactory(
    private val app: Application,
    private val repository: AttendanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AttendanceViewModel(app, repository) as T
    }
}

