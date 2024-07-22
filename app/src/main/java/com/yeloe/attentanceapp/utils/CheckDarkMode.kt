package com.yeloe.attentanceapp.utils

import android.content.Context
import android.content.res.Configuration

fun isDarkModeOn(context: Context): Boolean {
    val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    val isDarkModeOn = nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    return isDarkModeOn
}
