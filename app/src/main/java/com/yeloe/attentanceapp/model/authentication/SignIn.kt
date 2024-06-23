package com.yeloe.attentanceapp.model.authentication

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class SignIn(
    var uid: String = "",
    var email: String = "",
    var name: String = "",
    var college: String = "",
    var branch: String = "",
    var semester: String = "",
    var year: String = "",
    var profileImage: String = "",
    var faceImage: String = "",
    var type: String = "",
) : Serializable