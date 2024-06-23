package com.yeloe.attentanceapp.model.teacher

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

import androidx.annotation.Keep
import java.io.Serializable


data class CreateClassroom(
    val uid: String = "",
    val classroomUid: String = "",
    val classroomName: String = "",
    val classroomCollege: String = "",
    val classroomBranch: String = "",
    val classroomYear: String = "",
    val classroomSemester: String = "",
    var teacherName: String = "",
    var teacherCollege: String = "",
    var teacherImage: String = "",
    val teacherEmail: String = "",
    @ServerTimestamp
    var timeStamp: Date = Date(),
    val keywords: List<String> = arrayListOf()
)