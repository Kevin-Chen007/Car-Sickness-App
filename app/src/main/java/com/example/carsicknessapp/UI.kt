package com.example.carsicknessapp


import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat


@Composable
fun Screen(viewModel: MainViewModel) {

    val context = LocalContext.current

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){


        Button(
            onClick = {
                val intent = Intent(context, OverlayService::class.java)
                ContextCompat.startForegroundService(context, intent)
            }
        ) {
            Text("Start Overlay")
        }

        Button(
            onClick = {
                val intent = Intent(context, OverlayService::class.java)
                context.stopService(intent)
            }
        ) {
            Text("Stop Overlay")
        }
    }
}
