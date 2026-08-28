package com.xmo.music

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.xmo.music.data.*
import com.xmo.music.player.XmoPlayer
import com.xmo.music.ui.*
import kotlinx.coroutines.launch

enum class XmoTheme {
    Dark,
    Light,
    Amoled
}

@Composable
fun App() {
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val holder =
        rememberSaveableStateHolder()

    val player =
        remember {
            XmoPlayer(context)
        }

    DisposableEffect(player) {
        player.connect()

        onDispose {
            player.release()
        }
    }

    val playback by
        player.state
            .collectAsState()

    val permission =
        if (
            Build.VERSION.SDK_INT >=
            33
        ) {
            Manifest.permission
                .READ_MEDIA_AUDIO
        } else {
            Manifest.permission
                .READ_EXTERNAL_STORAGE
        }

    var allowed by remember {
        mutableStateOf(
            ContextCompat
                .checkSelfPermission(
                    context,
                    permission
                ) ==
                PackageManager
                    .PERMISSION_GRANTED
        )
    }

    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            allowed = it
        }

    var tab by remember {
        mutableIntStateOf(0)
    }

    var theme by remember {
        mutableStateOf(
            XmoTheme.Dark
        )
    }

    var songs by remember {
        mutableStateOf<List<Song>>(
            emptyList()
        )
    }

    var order by remember {
        mutableStateOf(
            Store.defaults
        )
    }

    var categories by remember {
        mutableStateOf<List<UserCategory>>(
            emptyList()
        )
    }

    var showPlayer by remember {
        mutableStateOf(false)
    }

    var source by remember {
        mutableStateOf(
            "All Songs"
        )
    }

    var sourceIsCategory by remember {
        mutableStateOf(false)
    }

    /*
     * Only true after full player has actually exited.
     */
    var miniEnterFromBottom by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        order =
            Store.order(context)

        categories =
            Store.categories(
                context
            )

        if (allowed) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(allowed) {
        if (allowed) {
            songs =
                Library.songs(
                    context
                )
        }
    }

    LaunchedEffect(Unit) {
        if (!allowed) {
            launcher.launch(
                permission
            )
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            holder.SaveableStateProvider(
                "tab_$tab"
            ) {
                when (tab) {
                    0 -> {
                        Home(
                            songs =
                                songs,
                            allowed =
                                allowed,
                            theme =
                                theme,
                            order =
                                order,
                            categories =
                                categories,

                            setTheme = {
                                theme = it
                            },

                            refresh = {
                                if (!allowed) {
                                    launcher.launch(
                                        permission
                                    )
                                } else {
                                    scope.launch {
                                        songs =
                                            Library.songs(
                                                context
                                            )
                                    }
                                }
                            },

                            saveOrder = {
                                order = it

                                scope.launch {
                                    Store.saveOrder(
                                        context,
                                        it
                                    )
                                }
                            },

                            saveCategories = {
                                categories =
                                    it

                                scope.launch {
                                    Store.saveCategories(
                                        context,
                                        it
                                    )
                                }
                            },

                            onPlaySong = {
                                    song,
                                    from,
                                    isCategory,
                                    queue ->

                                val index =
                                    queue
                                        .indexOfFirst {
                                            it.id ==
                                                song.id
                                        }

                                if (index >= 0) {
                                    source = from
                                    sourceIsCategory =
                                        isCategory

                                    /*
                                     * MiniPlayer is removed
                                     * immediately without exit
                                     * animation.
                                     */
                                    miniEnterFromBottom =
                                        false

                                    showPlayer = true

                                    player.play(
                                        queue,
                                        index
                                    )
                                }
                            }
                        )
                    }

                    1 -> Search()

                    else -> Settings()
                }
            }
        }

        /*
         * Approved navbar.
         */
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            NavBar(
                selected =
                    tab,
                theme =
                    theme
            ) {
                tab = it
            }
        }

        /*
         * MiniPlayer.
         *
         * Direct conditional means opening full player causes
         * immediate disappearance — NO exit animation.
         */
        if (
            playback.currentSongId !=
                null &&
            !showPlayer
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(20f)
            ) {
                MiniPlayer(
                    state =
                        playback,

                    theme =
                        theme,

                    enterFromBottom =
                        miniEnterFromBottom,

                    openPlayer = {
                        /*
                         * Immediate removal.
                         */
                        miniEnterFromBottom =
                            false

                        showPlayer =
                            true
                    },

                    togglePlay = {
                        player
                            .togglePlayPause()
                    },

                    previous = {
                        player.previous()
                    },

                    next = {
                        player.next()
                    }
                )
            }
        }

        /*
         * Full Now Playing.
         */
        if (showPlayer) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                NowPlaying(
                    state =
                        playback,

                    theme =
                        theme,

                    source =
                        source,

                    sourceIsCategory =
                        sourceIsCategory,

                    queue =
                        player.queue(),

                    refreshPosition = {
                        player
                            .refreshPosition()
                    },

                    togglePlay = {
                        player
                            .togglePlayPause()
                    },

                    previous = {
                        player.previous()
                    },

                    next = {
                        player.next()
                    },

                    seekTo = {
                        player.seekTo(it)
                    },

                    /*
                     * Called ONLY once NowPlaying has completed
                     * its own downward exit.
                     */
                    dismiss = {
                        miniEnterFromBottom =
                            true

                        showPlayer =
                            false
                    }
                )
            }
        }
    }
}
