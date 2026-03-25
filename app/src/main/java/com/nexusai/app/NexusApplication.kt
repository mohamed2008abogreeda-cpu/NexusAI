package com.nexusai.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NexusApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize global components
        // e.g. Crashlytics (if any), Logger, or trigger background pre-warming of Node.js thread
    }
}
