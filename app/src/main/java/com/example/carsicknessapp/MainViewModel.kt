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

    var alpha = 0.8f
    private set

    var onMotionUpdate: ((Float, Float) -> Unit)? = null



    init {
        accelerationSensor.startListening()
        accelerationSensor.setOnSensorValuesChangedListener { values ->
            xAcceleration = values[0]
            forwardAcceleration = (values[1] + values[2]) / 2f

            updateBall()
        }
    }

    private fun updateBall() {
        val targetX = lowPassFilter(xAcceleration) * 10f
        val targetForward = lowPassFilter(forwardAcceleration) * -20f

        ballX += (targetX - ballX) * 0.8f
        ballForward += (targetForward - ballForward) * 0.8f

        MotionBus.motionFlow.tryEmit(ballX to ballForward)
    }


    private fun lowPassFilter(curr: Float): Float{
        val previous = previousReading

        if (previous == null){
            previousReading = curr
            return curr
        }
        else{
            val output = previous + alpha * (curr - previous)
            previousReading = curr
            return output
        }
    }

    override fun onCleared() {
        accelerationSensor.stopListening()
    }
}