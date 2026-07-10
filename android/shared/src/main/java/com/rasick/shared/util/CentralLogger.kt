package com.rasick.shared.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Log

object CentralLogger {
    private var isDebugEnabled = true

    fun initialize(context: Context) {
        val appFlags = context.applicationInfo.flags
        isDebugEnabled = (appFlags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    fun d(tag: String, msg: String) {
        if (isDebugEnabled) Log.d(tag, msg)
    }

    fun i(tag: String, msg: String) {
        if (isDebugEnabled) Log.i(tag, msg)
    }

    fun w(tag: String, msg: String, tr: Throwable? = null) {
        if (isDebugEnabled) Log.w(tag, msg, tr)
    }

    fun e(tag: String, msg: String, tr: Throwable? = null) {
        if (isDebugEnabled) Log.e(tag, msg, tr)
    }
}
