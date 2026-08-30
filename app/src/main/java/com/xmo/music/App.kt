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

    val state = remember {
        XmoAppState()
    }

    val persistence =
        remember(context, state) {
            XmoAppPersistence(
                context.applicationContext,
                state
            )
        }

    val permission =
        if (Build.VERSION.SDK_INT >= 33) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            state.allowed = it
        }

    LaunchedEffect(Unit) {
        state.allowed =
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED

        persistence.loadInitial()
    }

    LaunchedEffect(
        playback.connected,
        state.playbackPreferences
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
            !state.playerPersistenceReady
        ) {
            persistence.restorePlayerModes(
                player
            )
        }
    }

    LaunchedEffect(
        playback.shuffleEnabled,
        playback.repeatMode,
        state.playerPersistenceReady
    ) {
        if (state.playerPersistenceReady) {
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
        state.showNowPlaying,
        state.profileOpen
    ) {
        if (playback.currentSongId == null) {
            state.miniVisible = false
        } else if (
            !state.showNowPlaying &&
            !state.profileOpen &&
            !state.miniVisible
        ) {
            state.miniVisible = true
        }
    }

    if (!state.loaded) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    homeColors(XmoTheme.Dark).bg
                )
        )
        return
    }

    if (!state.setupComplete) {
        Setup(
            initialProfile = state.profile,
            existingCategories =
                state.categories,
            onCategoriesChanged = {
                state.categories = it

                scope.launch {
                    Store.saveCategories(
                        context,
                        it
                    )
                }
            },
            finish = { profile ->
                scope.launch {
                    state.allowed =
                        ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (!state.allowed) {
                        launcher.launch(permission)
                        return@launch
                    }

                    state.profile = profile

                    Store.finishSetup(
                        context,
                        profile
                    )

                    state.setupComplete = true
                    persistence.loadLibrary()
                }
            },
            setupLater = { profile ->
                scope.launch {
                    state.allowed =
                        ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) ==
                            PackageManager.PERMISSION_GRANTED

                    if (!state.allowed) {
                        launcher.launch(permission)
                        return@launch
                    }

                    state.profile = profile

                    Store.finishSetup(
                        context,
                        profile
                    )

                    state.setupComplete = true
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
        when (state.appearance.themeMode) {
            ThemeMode.System ->
                if (systemDark) {
                    XmoTheme.Dark
                } else {
                    XmoTheme.Light
                }

            ThemeMode.Dark -> XmoTheme.Dark
            ThemeMode.Light -> XmoTheme.Light
            ThemeMode.Amoled -> XmoTheme.Amoled
        }

    val actions =
        buildXmoAppActions(
            context = context,
            state = state,
            player = player,
            scope = scope,
            persistence = persistence,
            requestAudioPermission = {
                launcher.launch(permission)
            }
        )

    val currentSong =
        playback.currentSongId?.let { id ->
            state.songs.firstOrNull {
                it.id == id
            }
        } ?: player.currentSong()

    XmoAppContent(
        state =
            XmoAppUiState(
                playback = playback,
                theme = theme,
                hazeState = hazeState,
                profile = state.profile,
                appearance = state.appearance,
                libraryPreferences =
                    state.libraryPreferences,
                playbackPreferences =
                    state.playbackPreferences,
                resumeOnHeadphones =
                    state.resumeOnHeadphones,
                songs = state.songs,
                playbackQueue = player.queue(),
                currentSong = currentSong,
                order = state.order,
                categories = state.categories,
                likedSongIds =
                    state.likedSongIds,
                recentPlays =
                    state.recentPlays,
                lyricsFiles =
                    state.lyricsFiles,
                homeMode =
                    state.homeMode,
                allowed = state.allowed,
                scanning = state.scanning,
                loaded = state.loaded,
                tab = state.tab,
                profileOpen =
                    state.profileOpen,
                showNowPlaying =
                    state.showNowPlaying,
                miniVisible =
                    state.miniVisible,
                miniRiseKey =
                    state.miniRiseKey,
                playingSource =
                    state.playingSource,
                playingSourceIsCategory =
                    state.playingSourceIsCategory
            ),
        actions = actions
    )
}
