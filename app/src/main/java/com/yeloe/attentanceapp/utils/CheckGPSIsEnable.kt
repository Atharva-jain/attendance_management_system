package com.yeloe.attentanceapp.utils

import android.app.Activity
import android.content.Context
import android.location.LocationManager

class CheckGPSIsEnable {
    companion object{
        fun isGPSEnable(activity: Activity): Boolean{
            val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }
}