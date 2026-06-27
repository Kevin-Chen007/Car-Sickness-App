package com.example.carsicknessapp

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlin.math.abs

class MainViewModel (
    private val accelerationSensor: accelerationSensor
): ViewModel() {

    var xAcceleration = 0f
        private set
    var forwardAcceleration = 0f
        private set

    var ballX = 0f
        private set

    var ballForward = 0f
        private set
    var previousReading: Float? = null
    private set



    var alpha = 0.5f
    private set

    var onMotionUpdate: ((Float, Float) -> Unit)? = null



    init {
        accelerationSensor.startListening()
        accelerationSensor.setOnSensorValuesChangedListener { values ->
            xAcceleration = values[0]
            forwardAcceleration = if (abs(values[1]) > abs(values[2])){
                values[1]
            }
            else {
                values[2]
            }

            updateBall()
        }
    }

    private fun updateBall() {
        val targetX = lowPassFilter(xAcceleration) * -50f
        val targetForward = lowPassFilter(forwardAcceleration) * -50f

        ballX += (targetX - ballX) * 0.8f
        ballForward += (targetForward - ballForward) * 0.8f

        MotionBus.motionFlow.tryEmit(ballX to ballForward)
    }


    private fun lowPassFilter(curr: Float): Float {
        val prev = previousReading

        if (prev == null) {
            previousReading = curr
            return curr
        }

        val output = prev + alpha * (curr - prev)
        previousReading = output   // <-- FIX
        return output
    }

    override fun onCleared() {
        accelerationSensor.stopListening()
    }
}