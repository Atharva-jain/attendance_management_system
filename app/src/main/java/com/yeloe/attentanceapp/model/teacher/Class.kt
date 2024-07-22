package com.yeloe.attentanceapp.model.teacher

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Class(
    val uid: String = "",
    val classUid: String = "",
    val classroomUid: String = "",
    val classroomName: String = "",
    val classroomCollege: String = "",
    val classroomBranch: String = "",
    val classroomYear: String = "",
    val classroomSemester: String = "",
    val teacherName: String = "",
    val teacherCollege: String = "",
    val teacherImage: String = "",
    val teacherEmail: String = "",
    @ServerTimestamp
    var timeStamp: Date = Date(),
    val className: String = "",
    val classCode: String = "",
    val location: Location = Location(),
    val allowAttendance: Boolean = true
)