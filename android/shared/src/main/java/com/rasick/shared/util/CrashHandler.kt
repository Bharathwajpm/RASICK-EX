package com.rasick.shared.util

import android.content.Context
import android.content.Intent
import android.os.Process
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import kotlin.system.exitProcess

class CrashHandler private constructor(
    private val context: Context,
    private val crashActivityClass: Class<*>
) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    init {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Stacktrace serialization
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val stackTrace = sw.toString()

            // Save crash report locally
            val file = File(context.cacheDir, "last_crash.txt")
            file.writeText(stackTrace)

            // Direct onto Custom Crash screen
            val intent = Intent(context, crashActivityClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("error_log", stackTrace)
            }
            context.startActivity(intent)

            // Kill active process
            Process.killProcess(Process.myPid())
            exitProcess(10)
        } catch (e: Exception) {
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        fun install(context: Context, crashActivityClass: Class<*>) {
            CrashHandler(context.applicationContext, crashActivityClass)
        }
    }
}
