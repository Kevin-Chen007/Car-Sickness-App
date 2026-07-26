package com.example.carsicknessapp

import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import android.content.res.Configuration

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val movingDots = mutableListOf<View>()
    private val stationaryDots = mutableListOf<View>()

    private val movingParams = mutableListOf<WindowManager.LayoutParams>()

    private val movingBasePositions = mutableListOf<Pair<Int, Int>>()

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

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val screenWidth: Int
        val screenHeight: Int


        val metrics = windowManager.currentWindowMetrics
        val bounds = metrics.bounds
        screenWidth = bounds.width()
        screenHeight = bounds.height()


        val count = 6

        val fracFromEdge = 0.1f
        val leftX = (screenWidth * fracFromEdge).toInt()
        val rightX = (screenWidth * (1f-fracFromEdge)).toInt()

        val spacingY = screenHeight / (count + 1)


        for (i in 0 until count) {

            val y = spacingY * (i + 1)


            // LEFT SIDE
            createDotPair(
                leftX,
                y
            )


            // RIGHT SIDE
            createDotPair(
                rightX,
                y
            )
        }


    }

    private fun createDotPair(x: Int, y: Int) {


        // -------- MOVING DOT --------

        val movingDot = LayoutInflater.from(this)
            .inflate(R.layout.overlay_circle_moving, null)


        val movingParam = WindowManager.LayoutParams(
            50,
            50,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )


        movingParam.gravity = Gravity.TOP or Gravity.START

        movingParam.x = x - 25
        movingParam.y = y - 25


        movingDots.add(movingDot)
        movingParams.add(movingParam)


        // save original position
        movingBasePositions.add(
            Pair(
                movingParam.x,
                movingParam.y
            )
        )




        // -------- STATIONARY DOT --------


        val staticDot = LayoutInflater.from(this)
            .inflate(R.layout.overlay_circle_static, null)


        val staticParam = WindowManager.LayoutParams(
            50,
            50,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )


        staticParam.gravity = Gravity.TOP or Gravity.START

        staticParam.x = x - 25
        staticParam.y = y - 25


        stationaryDots.add(staticDot)


        windowManager.addView(
            staticDot,
            staticParam
        )

        windowManager.addView(
            movingDot,
            movingParam
        )
    }

    // -----------------------------
    // MOVE OVERLAY (CALL THIS)
    // -----------------------------
    fun updateOverlay(x: Float, y: Float) {

        for (i in movingDots.indices) {

            val params = movingParams[i]
            val base = movingBasePositions[i]


            params.x = base.first + x.toInt()
            params.y = base.second + y.toInt()


            windowManager.updateViewLayout(
                movingDots[i],
                params
            )
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        movingDots.forEach { windowManager.removeView(it) }
        stationaryDots.forEach { windowManager.removeView(it) }

        movingDots.clear()
        stationaryDots.clear()
        movingParams.clear()
        movingBasePositions.clear()

        createOverlay()
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

        movingDots.forEach { view ->
            windowManager.removeView(view)
        }

        stationaryDots.forEach { view ->
            windowManager.removeView(view)
        }

        movingDots.clear()
        stationaryDots.clear()
        movingParams.clear()
        movingBasePositions.clear()
    }


    inner class LocalBinder : Binder() {
        fun getService(): OverlayService = this@OverlayService
    }

    override fun onBind(intent: Intent): IBinder {
        return LocalBinder()
    }
}