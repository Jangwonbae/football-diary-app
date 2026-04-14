package com.wbjang.footballdiary.util

import android.util.Log
import com.wbjang.footballdiary.BuildConfig

object AppLogger {
    var isEnabled: Boolean = BuildConfig.DEBUG

    fun d(tag: String, message: String) {
        if (isEnabled) Log.d("wbjang : $tag", message)
    }

    fun w(tag: String, message: String) {
        if (isEnabled) Log.w("wbjang : $tag", message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isEnabled) {
            if (throwable != null) Log.e("wbjang : $tag", message, throwable)
            else Log.e("wbjang : $tag", message)
        }
    }
}
