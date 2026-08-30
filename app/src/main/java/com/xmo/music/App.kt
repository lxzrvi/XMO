package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.xmo.music.app.XmoAppActions
import com.xmo.music.app.XmoAppContent
import com.xmo.music.app.XmoAppPersistence
import com.xmo.music.app.XmoAppState
import com.xmo.music.app.XmoAppUiState
import com.xmo.music.app.buildXmoAppActions
import com.xmo.music.data.Store
import com.xmo.music.data.ThemeMode
import com.xmo.music.player.XmoPlayer
import com.xmo.music.ui.Setup
import com.xmo.music.ui.blur.rememberLiveBlurState
import com.xmo.music.ui.homeColors
import kotlinx.coroutines.launch

enum class XmoTheme {
    Dark,
    Light,
    Amoled
}

@Composable
fun App() {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scope = rememberCoroutineScope()
    val hazeState = rememberLiveBlurState()

    val player = remember {
        XmoPlayer(context)
    }

    DisposableEffect(player) {
        player.connect()

        onDispose {
            player.release()
        }
    }

    val playback by
        player.state.collectAsState()

    val appState = remember {
        XmoAppState()
    }

    val persistence =
        remember(context, appState) {
            XmoAppPersistence(
                context.applicationContext,
                appState
            )
        }

    val audioPermission =
        if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            appState.allowed = it
        }

    LaunchedEffect(Unit) {
        appState.allowed =
            ContextCompat.checkSelfPermission(
                context,
                audioPermission
            ) == PackageManager.PERMISSION_GRANTED

        persistence.loadInitial()
    }

    LaunchedEffect(
        playback.connected,
        appState.playbackPreferences
    ) {
        if (playback.connected) {
            persistence.applyPlaybackPreferences(
                player
            )
        }
    }

    LaunchedEffect(playback.connected) {
        if (
            playback.connected &&
            !appState.playerPersistenceReady
        ) {
            persistence.restorePlayerModes(
                player
            )
        }
    }

    LaunchedEffect(
        playback.shuffleEnabled,
        playback.repeatMode,
        appState.playerPersistenceReady
    ) {
        if (appState.playerPersistenceReady) {
            persistence.persistPlayerModes(
                playback
            )
        }
    }

    LaunchedEffect(playback.currentSongId) {
        playback.currentSongId?.let {
            persistence.recordCurrentSong(it)
        }
    }

    LaunchedEffect(
        playback.currentSongId,
        appState.showNowPlaying,
        appState.profileOpen
    ) {
        when {
            playback.currentSongId == null -> {
                appState.miniVisible = false
            }

            !appState.showNowPlaying &&
                !appState.profileOpen &&
                !appState.miniVisible -> {
                appState.miniVisible = true
            }
        }
    }

    if (!appState.loaded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    homeColors(XmoTheme.Dark).bg
                )
        )

        return
    }

    if (!appState.setupComplete) {
        Setup(
            initialProfile = appState.profile,
            existingCategories = appState.categories,
            onCategoriesChanged = { categories ->
                appState.categories = categories

                scope.launch {
                    Store.saveCategories(
                        context,
                        categories
                    )

                    val customIds =
                        categories.map { it.id }

                    val builtIns =
                        appState.order.filter {
                            it in Store.defaults
                        }

                    val existingCustom =
                        appState.order.filter {
                            it !in Store.defaults &&
                                it in customIds
                        }

                    val missing =
                        customIds.filterNot {
                            it in existingCustom
                        }

                    val nextOrder =
                        builtIns +
                            existingCustom +
                            missing

                    appState.order = nextOrder

                    Store.saveOrder(
                        context,
                        nextOrder
                    )
                }
            },
            finish = { profile ->
                scope.launch {
                    appState.allowed =
                        ContextCompat.checkSelfPermission(
                            context,
                            audioPermission
                        ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (!appState.allowed) {
                        permissionLauncher.launch(
                            audioPermission
                        )
                        return@launch
                    }

                    appState.profile = profile

                    Store.finishSetup(
                        context,
                        profile
                    )

                    appState.setupComplete = true
                    persistence.loadLibrary()
                }
            },
            setupLater = { profile ->
                scope.launch {
                    appState.allowed =
                        ContextCompat.checkSelfPermission(
                            context,
                            audioPermission
                        ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (!appState.allowed) {
                        permissionLauncher.launch(
                            audioPermission
                        )
                        return@launch
                    }

                    appState.profile = profile

                    Store.finishSetup(
                        context,
                        profile
                    )

                    appState.setupComplete = true
                    persistence.loadLibrary()
                }
            }
        )

        return
    }

    val systemDark =
        (
            configuration.uiMode and
                android.content.res.Configuration
                    .UI_MODE_NIGHT_MASK
            ) ==
            android.content.res.Configuration
                .UI_MODE_NIGHT_YES

    val theme =
        when (appState.appearance.themeMode) {
            ThemeMode.System ->
                if (systemDark) {
                    XmoTheme.Dark
                } else {
                    XmoTheme.Light
                }

            ThemeMode.Dark ->
                XmoTheme.Dark

            ThemeMode.Light ->
                XmoTheme.Light

            ThemeMode.Amoled ->
                XmoTheme.Amoled
        }

    val actions: XmoAppActions =
        buildXmoAppActions(
            context = context,
            state = appState,
            player = player,
            scope = scope,
            persistence = persistence,
            requestAudioPermission = {
                permissionLauncher.launch(
                    audioPermission
                )
            }
        )

    val currentSong =
        playback.currentSongId?.let { id ->
            appState.songs.firstOrNull {
                it.id == id
            }
        } ?: player.currentSong()

    XmoAppContent(
        state =
            XmoAppUiState(
                playback = playback,
                theme = theme,
                hazeState = hazeState,

                profile = appState.profile,
                appearance = appState.appearance,
                libraryPreferences =
                    appState.libraryPreferences,
                playbackPreferences =
                    appState.playbackPreferences,
                resumeOnHeadphones =
                    appState.resumeOnHeadphones,

                songs = appState.songs,
                playbackQueue = player.queue(),
                currentSong = currentSong,

                order = appState.order,
                categories = appState.categories,
                likedSongIds =
                    appState.likedSongIds,
                recentPlays =
                    appState.recentPlays,
                lyricsFiles =
                    appState.lyricsFiles,

                allowed = appState.allowed,
                scanning = appState.scanning,
                loaded = appState.loaded,

                tab = appState.tab,
                profileOpen =
                    appState.profileOpen,

                showNowPlaying =
                    appState.showNowPlaying,
                miniVisible =
                    appState.miniVisible,
                miniRiseKey =
                    appState.miniRiseKey,

                playingSource =
                    appState.playingSource,
                playingSourceIsCategory =
                    appState.playingSourceIsCategory
            ),
        actions = actions
    )
}
