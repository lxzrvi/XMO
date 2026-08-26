package com.xmo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Search() = Box(
    Modifier.fillMaxSize().background(
        Brush.linearGradient(listOf(Color(0xFF050505), Color(0xFF101C2A)))
    )
)
