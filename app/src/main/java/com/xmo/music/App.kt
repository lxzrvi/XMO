package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.xmo.music.data.*
import com.xmo.music.ui.*
import kotlinx.coroutines.launch

enum class XmoTheme { Dark, Light, Amoled }

@Composable
fun App() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val permission = if (Build.VERSION.SDK_INT >= 33)
        Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE

    var tab by remember { mutableIntStateOf(0) }
    var theme by remember { mutableStateOf(XmoTheme.Dark) }
    var songs by remember { mutableStateOf(emptyList<Song>()) }
    var order by remember { mutableStateOf(Store.defaults) }
    var categories by remember { mutableStateOf(emptyList<UserCategory>()) }
    var allowed by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { allowed = it }

    suspend fun load() {
        order = Store.order(context)
        categories = Store.categories(context)
        if (allowed) songs = Library.songs(context)
    }

    LaunchedEffect(allowed) { load() }
    LaunchedEffect(Unit) {
        if (!allowed) permissionLauncher.launch(permission)
    }

    Box(Modifier.fillMaxSize()) {
        when (tab) {
            0 -> Home(
                songs, allowed, theme, order, categories,
                setTheme = { theme = it },
                refresh = {
                    if (!allowed) permissionLauncher.launch(permission)
                    else scope.launch { songs = Library.songs(context) }
                },
                saveOrder = {
                    order = it
                    scope.launch { Store.saveOrder(context, it) }
                },
                saveCategories = {
                    categories = it
                    scope.launch { Store.saveCategories(context, it) }
                }
            )
            1 -> Search()
            else -> Settings()
        }

        NavBar(tab) { tab = it }
    }
}
