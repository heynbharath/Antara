package org.circle13.antara

import android.app.Application
import android.util.Log

class AntaraApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        
        // Uncaught Exception Handler to prevent process crashes
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("AntaraApp", "Uncaught Exception in thread ${thread.name}", throwable)
        }
    }
}
