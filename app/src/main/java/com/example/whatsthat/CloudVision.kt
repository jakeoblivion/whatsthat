package com.example.whatsthat

import android.graphics.Bitmap
import android.util.Log
import java.io.IOException

class CloudVision {
    private val TAG = MainActivity::class.java.simpleName


    fun makeApiCall(bitmap: Bitmap?) {
        try {

        } catch (e: IOException) {
            Log.d(TAG, "failed to make API request because of other IOException " + e.message)
        }

    }
}