package com.nexusai.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class NodeBridgeService : Service() {

    override fun onCreate() {
        super.onCreate()
        Log.d("NodeBridgeService", "Service started, initializing Node.js runtime...")
        // Initialize nodejs-mobile-android runtime in a separate thread
        // Thread {
        //     NodeJSAssetExtractor.extract(this)
        //     NodeJSMobile.startNodeWithArguments(arrayOf("node", "main.js"))
        // }.start()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Keep service running for background tasks
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // Return binder for UI to communicate with this service if needed
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NodeBridgeService", "Service destroyed, cleaning up Node.js runtime...")
    }
}
