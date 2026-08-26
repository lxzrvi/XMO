package com.xmo.music

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.xmo.music.ui.*

@Composable
fun App() {
    var tab by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        when (tab) {
            0 -> Home()
            1 -> Search()
            else -> Settings()
        }
        NavBar(tab) { tab = it }
    }
}
