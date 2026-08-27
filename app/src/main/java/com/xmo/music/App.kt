package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.ui.*

enum class XmoTheme { Dark, Light, Amoled }

@Composable
fun App() {
    val context = LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    var tab by remember { mutableIntStateOf(0) }
    var theme by remember { mutableStateOf(XmoTheme.Dark) }
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var allowed by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val request = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { allowed = it }

    LaunchedEffect(allowed) {
        if (allowed) songs = Library.songs(context)
    }

    LaunchedEffect(Unit) {
        if (!allowed) request.launch(permission)
    }

    Box(Modifier.fillMaxSize()) {
        when (tab) {
            0 -> Home(
                songs = songs,
                allowed = allowed,
                theme = theme,
                setTheme = { theme = it },
                refresh = {
                    if (!allowed) request.launch(permission)
                }
            )
            1 -> Search()
            else -> Settings()
        }

        NavBar(tab) { tab = it }
    }
}
