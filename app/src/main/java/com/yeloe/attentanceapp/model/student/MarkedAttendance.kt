package com.yeloe.attentanceapp.model.student

import com.google.firebase.firestore.ServerTimestamp
import com.yeloe.attentanceapp.model.teacher.Location
import java.util.Date

data class MarkedAttendance(
    val uid: String = "",
    val attendanceUid: String = "",
    val teacherUid: String = "",
    val joinClassRoomUid: String = "",
    val classroomUid: String = "",
    val classUid: String = "",
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
    val keywords: List<String> = arrayListOf(),
    val studentName: String = "",
    var studentCollege: String = "",
    var studentBranch: String = "",
    var studentSemester: String = "",
    var studentYear: String = "",
    var studentProfileImage: String = "",
    var studentFaceImage: String = "",
    val location: Location = Location(),
    val classCode: String = "",
)