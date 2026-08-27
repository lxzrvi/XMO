package com.xmo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Home() {
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF050505), Color(0xFF321010))
                )
            )
            .padding(28.dp, 60.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("XMO", fontFamily = XmoFont.logo, fontSize = 42.sp)
        Text("Alex Morgan", fontFamily = XmoFont.user, fontSize = 28.sp)

        Text("Your Music", fontFamily = XmoFont.bold, fontSize = 24.sp)
        Text("Recently Played", fontFamily = XmoFont.bold, fontSize = 20.sp)

        Text("Midnight Drive", fontFamily = XmoFont.normal, fontSize = 18.sp)
        Text("All Songs", fontFamily = XmoFont.normal, fontSize = 17.sp)

        Text("128 songs", fontFamily = XmoFont.medium, fontSize = 15.sp)
        Text("Local music library", fontFamily = XmoFont.medium, fontSize = 14.sp)

        Text("Updated just now", fontFamily = XmoFont.thin, fontSize = 14.sp)
        Text("Music lives here.", fontFamily = XmoFont.thin, fontSize = 13.sp)
    }
}
