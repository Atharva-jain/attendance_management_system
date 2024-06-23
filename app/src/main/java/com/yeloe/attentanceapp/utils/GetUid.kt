package com.yeloe.attentanceapp.utils

import com.google.firebase.auth.FirebaseAuth

class GetUid {
    companion object {
        fun getUid(): String? {
            return FirebaseAuth.getInstance().currentUser?.uid
        }
    }
}