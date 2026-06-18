package com.example.carsicknessapp

import kotlinx.coroutines.flow.MutableSharedFlow

object MotionBus {
    val motionFlow = MutableSharedFlow<Pair<Float, Float>>(
        replay = 0,
        extraBufferCapacity = 1
    )
}