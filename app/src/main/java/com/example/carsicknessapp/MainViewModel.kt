package com.example.carsicknessapp

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

    var ballX by mutableFloatStateOf(0f)
        private set

    var ballForward by mutableFloatStateOf(0f)
        private set



    init {
        accelerationSensor.startListening()
        accelerationSensor.setOnSensorValuesChangedListener { values ->
            xAcceleration = values[0]
            forwardAcceleration = (values[1] + values[2]) / 2f

            rawDataProcessing()
        }
    }

    private fun rawDataProcessing(){

        ballX = dataFilter(xAcceleration) * 10f
        ballForward = dataFilter(forwardAcceleration) * -10f
    }

    private fun dataFilter(data: Float): Float{
        return if (abs(data) < 0.4f){
            0f
        } else
            data
    }

    override fun onCleared() {
        accelerationSensor.stopListening()
    }
}