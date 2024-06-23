package com.yeloe.attentanceapp.utils

import android.content.Context
import android.widget.Toast

class ShowToast {
    companion object {
        fun showToast(context: Context, message: String) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_LONG
            ).show()
        }
    }
}