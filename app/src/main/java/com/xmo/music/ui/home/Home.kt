package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import dev.chrisbanes.haze.HazeState

@Composable
fun Home(
    songs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    hazeState: HazeState,
    categories: List<UserCategory>,
    likedSongIds: Set<Long>,
    recentPlays: List<RecentPlay>,
    scanning: Boolean,
    playback: PlaybackState,
    modeName: String,
    changeMode: (String) -> Unit,
    refresh: () -> Unit,
    openProfile: () -> Unit,
    togglePlay: () -> Unit,
    toggleLike: (Song) -> Unit,
    playNext: (Song) -> Unit,
    removeRecent: (Song) -> Unit,
    setSongInCategory: (Song, String, Boolean) -> Unit,
    shuffleSongs: (List<Song>, String, Boolean) -> Unit,
    onPlaySong: (Song, String, Boolean, List<Song>) -> Unit
) {
    val c = homeColors(theme)
    val top = homeTopColors(theme)

    val mode =
        runCatching {
            HomeMode.valueOf(modeName)
        }.getOrDefault(HomeMode.Home)

    var page by remember(mode) {
        mutableStateOf<HomePage>(
            HomePage.Root
        )
    }

    var optionsSong by remember {
        mutableStateOf<Song?>(null)
    }

    var recentOption by remember {
        mutableStateOf(false)
    }

    var menu by remember {
        mutableStateOf(false)
    }

    var scanner by remember {
        mutableStateOf(false)
    }

    val likedSongs =
        remember(songs, likedSongIds) {
            songs.filter {
                it.id in likedSongIds
            }
        }

    val recentSongs =
        remember(songs, recentPlays) {
            val byId =
                songs.associateBy { it.id }

            recentPlays
                .mapNotNull {
                    byId[it.songId]
                }
                .distinctBy { it.id }
                .take(12)
        }

    fun nextMode() {
        val next =
            when (mode) {
                HomeMode.Home ->
                    HomeMode.Liked

                HomeMode.Liked ->
                    HomeMode.Categories

                HomeMode.Categories ->
                    HomeMode.Home
            }

        page = HomePage.Root
        changeMode(next.name)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(top.background)
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
        ) {
            HomeHeader(
                c = c,
                theme = theme,
                mode = mode,
                changeMode = ::nextMode,
                refresh = {
                    scanner = true
                },
                openMenu = {
                    menu = true
                },
                openProfile = openProfile
            )
        }

        when (val current = page) {
            HomePage.Root -> {
                when (mode) {
                    HomeMode.Home -> {
                        HomeRoot(
                            songs = songs,
                            recentSongs = recentSongs,
                            allowed = allowed,
                            theme = theme,
                            c = c,
                            playback = playback,
                            play = onPlaySong,
                            togglePlay = togglePlay,
                            options = {
                                    song,
                                    recent ->

                                recentOption = recent
                                optionsSong = song
                            }
                        )
                    }

                    HomeMode.Liked -> {
                        HomeLiked(
                            songs = likedSongs,
                            playback = playback,
                            c = c,
                            play = {
                                onPlaySong(
                                    it,
                                    "Liked Songs",
                                    false,
                                    likedSongs
                                )
                            },
                            shuffle = {
                                shuffleSongs(
                                    likedSongs,
                                    "Liked Songs",
                                    false
                                )
                            },
                            options = {
                                recentOption = false
                                optionsSong = it
                            }
                        )
                    }

                    HomeMode.Categories -> {
                        HomeCategories(
                            songs = songs,
                            categories = categories,
                            c = c,
                            open = {
                                page =
                                    HomePage.Category(
                                        it.id
                                    )
                            }
                        )
                    }
                }
            }

            is HomePage.Category -> {
                val category =
                    categories.firstOrNull {
                        it.id == current.id
                    }

                if (category != null) {
                    val categorySongs =
                        songs.filter {
                            it.id in category.songIds
                        }

                    HomeCategoryDetail(
                        category = category,
                        songs = songs,
                        playback = playback,
                        c = c,
                        back = {
                            page = HomePage.Root
                        },
                        add = {
                            page =
                                HomePage.CategoryPicker(
                                    category.id
                                )
                        },
                        play = {
                            onPlaySong(
                                it,
                                category.name,
                                true,
                                categorySongs
                            )
                        },
                        options = {
                            recentOption = false
                            optionsSong = it
                        }
                    )
                } else {
                    page = HomePage.Root
                }
            }

            is HomePage.CategoryPicker -> {
                val category =
                    categories.firstOrNull {
                        it.id == current.id
                    }

                if (category != null) {
                    HomeCategoryPicker(
                        category = category,
                        songs = songs,
                        c = c,
                        back = {
                            page =
                                HomePage.Category(
                                    category.id
                                )
                        },
                        setMembership = {
                                song,
                                added ->

                            setSongInCategory(
                                song,
                                category.id,
                                added
                            )
                        }
                    )
                } else {
                    page = HomePage.Root
                }
            }
        }
    }

    optionsSong?.let { song ->
        XmoSongList(
            song = song,
            liked =
                song.id in likedSongIds,
            recent = recentOption,
            c = c,
            dismiss = {
                optionsSong = null
                recentOption = false
            },
            toggleLike = {
                toggleLike(song)
            },
            playNext = {
                playNext(song)
            },
            removeRecent = {
                removeRecent(song)
            }
        )
    }

    if (menu) {
        HomeMenuDialog(
            c = c,
            dismiss = {
                menu = false
            },
            allSongs = {
                menu = false
                changeMode(HomeMode.Home.name)
            },
            liked = {
                menu = false
                changeMode(HomeMode.Liked.name)
            },
            scanner = {
                menu = false
                scanner = true
            }
        )
    }

    if (scanner) {
        HomeScannerDialog(
            c = c,
            scanning = scanning,
            songCount = songs.size,
            scan = refresh,
            dismiss = {
                if (!scanning) {
                    scanner = false
                }
            }
        )
    }
}

@Composable
private fun HomeRoot(
    songs: List<Song>,
    recentSongs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    c: HomeColors,
    playback: PlaybackState,
    play: (Song, String, Boolean, List<Song>) -> Unit,
    togglePlay: () -> Unit,
    options: (Song, Boolean) -> Unit
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(bottom = 190.dp)
    ) {
        item(key = "recent") {
            Column(
                Modifier.padding(
                    top = 8.dp,
                    bottom = 14.dp
                )
            ) {
                SectionTitle(
                    title = "Recently Played",
                    subtitle =
                        if (recentSongs.isEmpty()) {
                            "Nothing played yet"
                        } else {
                            "${recentSongs.size} recent tracks"
                        },
                    icon = R.drawable.ic_xmo_history,
                    c = c
                )

                if (recentSongs.isEmpty()) {
                    HomeEmpty(
                        "Nothing played yet",
                        c
                    )
                } else {
                    HomeRecentlyPlayed(
                        songs = recentSongs,
                        c = c,
                        playback = playback,
                        play = { song ->
                            if (
                                playback.currentSongId ==
                                song.id
                            ) {
                                togglePlay()
                            } else {
                                play(
                                    song,
                                    "Recently Played",
                                    false,
                                    recentSongs
                                )
                            }
                        },
                        options = {
                            options(it, true)
                        }
                    )
                }
            }
        }

        item(key = "all_songs") {
            Column(
                Modifier.padding(
                    top = 6.dp,
                    bottom = 16.dp
                )
            ) {
                SectionTitle(
                    title = "All Songs",
                    subtitle =
                        "${songs.size} songs",
                    icon = R.drawable.ic_xmo_songs,
                    c = c
                )

                HomeAllSongs(
                    songs = songs,
                    allowed = allowed,
                    c = c,
                    theme = theme,
                    play = {
                        play(
                            it,
                            "All Songs",
                            false,
                            songs
                        )
                    },
                    options = {
                        options(it, false)
                    }
                )
            }
        }
    }
}
