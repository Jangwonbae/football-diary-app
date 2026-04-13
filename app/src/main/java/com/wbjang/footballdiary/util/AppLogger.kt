package com.wbjang.footballdiary.util

import android.util.Log
import com.wbjang.footballdiary.BuildConfig

object AppLogger {
    var isEnabled: Boolean = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (isEnabled) Log.d(tag, message)
    }

    fun w(tag: String, message: String) {
        if (isEnabled) Log.w(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            if (throwable != null) Log.e(tag, message, throwable)
            else Log.e(tag, message)
        }
    }
}
