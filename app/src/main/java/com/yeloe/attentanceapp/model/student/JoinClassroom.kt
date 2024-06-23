package com.yeloe.attentanceapp.model.student

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class JoinClassroom(
    val uid: String = "",
    val teacherUid: String = "",
    val joinClassRoomUid: String = "",
    val classroomUid: String = "",
    var classroomName: String = "",
    var classroomCollege: String = "",
    var classroomBranch: String = "",
    var classroomYear: String = "",
    var classroomSemester: String = "",
    var teacherName: String = "",
    var teacherCollege: String = "",
    var teacherImage: String = "",
    val teacherEmail: String = "",
    @ServerTimestamp
    var timeStamp: Date = Date(),
    var keywords: List<String> = arrayListOf(),
    var studentName: String = "",
    var studentCollege: String = "",
    var studentBranch: String = "",
    var studentSemester: String = "",
    var studentYear: String = "",
    var studentProfileImage: String = "",
    var studentFaceImage: String = "",
)