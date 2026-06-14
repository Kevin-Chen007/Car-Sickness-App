package com.example.carsicknessapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sensor = accelerationSensor(this)
        val viewModel = MainViewModel(sensor)

        setContent {
            Screen(viewModel)
        }
    }
}