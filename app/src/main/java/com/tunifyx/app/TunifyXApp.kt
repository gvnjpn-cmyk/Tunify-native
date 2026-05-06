package com.tunifyx.app

import android.app.Application
import android.content.Intent
import android.os.Build

class TunifyXApp : Application() {
    override fun onCreate() {
        super.onCreate()
        setupCrashReporter()
    }

    private fun setupCrashReporter() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val log = buildString {
                    appendLine("=== TunifyX Crash Report ===")
                    appendLine("Android: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})")
                    appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                    appendLine("Thread: ${thread.name}")
                    appendLine("---")
                    appendLine(throwable.stackTraceToString())
                }
                getExternalFilesDir(null)?.resolve("crash.txt")?.writeText(log)
                val intent = Intent(this, CrashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("crash_log", log)
                }
                startActivity(intent)
            } catch (_: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
