package com.yeloe.attentanceapp.model.teacher

import com.yeloe.attentanceapp.model.student.MarkedAttendance

data class Attendance(
    val markedAttendance: ArrayList<MarkedAttendance>,
    val classes: ArrayList<Class>
)