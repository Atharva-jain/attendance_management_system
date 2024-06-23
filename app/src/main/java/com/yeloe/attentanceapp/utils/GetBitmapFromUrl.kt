package com.yeloe.attentanceapp.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


class GetBitmapFromUrl {
    companion object{
        fun getBitmapFromURL(src: String): Bitmap? {
            return try {
                val url = URL(src)
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }
    }
}