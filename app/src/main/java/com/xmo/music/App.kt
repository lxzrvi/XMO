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
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
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

    val stateHolder =
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

    val playbackState by
        player.state
            .collectAsState()

    val permission =
        if (
            Build.VERSION.SDK_INT >=
            33
        ) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    var allowed by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) ==
                PackageManager.PERMISSION_GRANTED
        )
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

    var showNowPlaying by remember {
        mutableStateOf(false)
    }

    var playingSource by remember {
        mutableStateOf(
            "All Songs"
        )
    }

    var sourceIsCategory by remember {
        mutableStateOf(false)
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            allowed = it
        }

    LaunchedEffect(Unit) {
        order =
            Store.order(
                context
            )

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
            permissionLauncher.launch(
                permission
            )
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        /*
         * =====================================================
         * TAB CONTENT
         * =====================================================
         */
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(0f)
        ) {
            stateHolder
                .SaveableStateProvider(
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
                                        permissionLauncher.launch(
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
                                        source,
                                        isCategory,
                                        queue ->

                                    val index =
                                        queue
                                            .indexOfFirst {
                                                it.id ==
                                                    song.id
                                            }

                                    if (
                                        index >= 0
                                    ) {
                                        playingSource =
                                            source

                                        sourceIsCategory =
                                            isCategory

                                        /*
                                         * Open immediately.
                                         */
                                        showNowPlaying =
                                            true

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
         * =====================================================
         * NAVBAR
         * =====================================================
         */
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(10f)
        ) {
            NavBar(
                selected = tab,
                theme = theme
            ) {
                tab = it
            }
        }

        /*
         * =====================================================
         * MINI PLAYER
         * =====================================================
         */
        MiniPlayer(
            state =
                playbackState,

            theme =
                theme,

            visible =
                playbackState.currentSongId !=
                    null &&
                    !showNowPlaying,

            openPlayer = {
                showNowPlaying =
                    true
            },

            togglePlay = {
                player
                    .togglePlayPause()
            },

            /*
             * User requested MiniPlayer:
             *
             * left = previous
             * right = next
             *
             * MiniPlayer itself handles that mapping.
             */
            previous = {
                player.previous()
            },

            next = {
                player.next()
            }
        )

        /*
         * =====================================================
         * NOW PLAYING
         * =====================================================
         */
        if (
            showNowPlaying
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                NowPlaying(
                    state =
                        playbackState,

                    theme =
                        theme,

                    source =
                        playingSource,

                    sourceIsCategory =
                        sourceIsCategory,

                    /*
                     * Current real queue provides adjacent
                     * local artwork for carousel.
                     */
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
                        player.seekTo(
                            it
                        )
                    },

                    /*
                     * Called only after full player exit.
                     * MiniPlayer visibility becomes true then,
                     * so it rises from bottom afterwards.
                     */
                    dismiss = {
                        showNowPlaying =
                            false
                    }
                )
            }
        }
    }
}
