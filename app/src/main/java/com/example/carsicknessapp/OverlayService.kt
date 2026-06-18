package com.example.carsicknessapp

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var params: WindowManager.LayoutParams

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()

        startForegroundServiceNotification()
        createOverlay()

        scope.launch {
            MotionBus.motionFlow.collect { (x, y) ->
                updateOverlay(x, y)
            }
        }

    }

    // -----------------------------
    // CREATE OVERLAY
    // -----------------------------
    private fun createOverlay() {

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        overlayView = LayoutInflater.from(this)
            .inflate(R.layout.overlay_circle, null)

        params = WindowManager.LayoutParams(
            300,
            600,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        // IMPORTANT: start in center so you KNOW it's visible
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 300
        params.y = 600

        windowManager.addView(overlayView, params)
    }

    // -----------------------------
    // MOVE OVERLAY (CALL THIS)
    // -----------------------------
    fun updateOverlay(x: Float, y: Float) {

        Log.d("OVERLAYSRV", "Called updateOverlay");
        params.x = (x).toInt()
        params.y = (y).toInt()

        windowManager.updateViewLayout(overlayView, params)
    }

    // -----------------------------
    // FOREGROUND SERVICE (REQUIRED)
    // -----------------------------
    private fun startForegroundServiceNotification() {

        val channelId = "overlay_channel"

        val channel = NotificationChannel(
            channelId,
            "Overlay Service",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("Overlay running")
            .setContentText("Ball active")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build()

        startForeground(1, notification)
    }

    // -----------------------------
    // CLEANUP
    // -----------------------------
    override fun onDestroy() {
        super.onDestroy()

        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }


    inner class LocalBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }
}