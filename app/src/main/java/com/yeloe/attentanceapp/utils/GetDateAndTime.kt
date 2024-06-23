package com.yeloe.attentanceapp.utils

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

class GetDateAndTime {
    companion object {
        @SuppressLint("SimpleDateFormat")
        fun getCurrentTime(date: Date): String {
            val time = date.time
            val timeFormat = SimpleDateFormat("HH:mm:ss")
            return timeFormat.format(time)
        }

        @SuppressLint("SimpleDateFormat")
        fun getCurrentDate(date: Date): String {
            val date = date.time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            return dateFormat.format(date)
        }
    }
}