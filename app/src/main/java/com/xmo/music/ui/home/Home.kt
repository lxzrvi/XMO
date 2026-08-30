package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
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
import java.util.UUID

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
    saveCategories: (List<UserCategory>) -> Unit,
    shuffleSongs: (List<Song>, String, Boolean) -> Unit,
    onPlaySong: (Song, String, Boolean, List<Song>) -> Unit
) {
    val c = homeColors(theme)

    val mode =
        runCatching {
            HomeMode.valueOf(modeName)
        }.getOrDefault(HomeMode.Home)

    var page by remember(mode) {
        mutableStateOf<HomePage>(
            HomePage.Root
        )
    }

    var menu by remember {
        mutableStateOf(false)
    }

    var scanner by remember {
        mutableStateOf(false)
    }

    var optionsSong by remember {
        mutableStateOf<Song?>(null)
    }

    var recentOption by remember {
        mutableStateOf(false)
    }

    var optionCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    var createCategory by remember {
        mutableStateOf(false)
    }

    var categoryName by remember {
        mutableStateOf("")
    }

    var categorySelection by remember {
        mutableStateOf<Set<Long>>(
            emptySet()
        )
    }

    val likedSongs =
        remember(
            songs,
            likedSongIds
        ) {
            songs.filter {
                it.id in likedSongIds
            }
        }

    val recentSongs =
        remember(
            songs,
            recentPlays
        ) {
            val map =
                songs.associateBy {
                    it.id
                }

            recentPlays
                .mapNotNull {
                    map[it.songId]
                }
                .distinctBy {
                    it.id
                }
                .take(12)
        }

    BackHandler(
        enabled =
            page != HomePage.Root ||
                mode != HomeMode.Home
    ) {
        when (val current = page) {
            is HomePage.CategoryPicker ->
                page =
                    HomePage.Category(
                        current.id
                    )

            is HomePage.Category ->
                page = HomePage.Root

            HomePage.Root ->
                changeMode(
                    HomeMode.Home.name
                )
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
    ) {
        HomeHeader(
            c = c,
            refresh = {
                scanner = true
            },
            openMenu = {
                menu = true
            },
            openProfile = openProfile
        )

        when (val current = page) {
            HomePage.Root -> {
                when (mode) {
                    HomeMode.Home -> {
                        HomeRoot(
                            songs = songs,
                            recentSongs =
                                recentSongs,
                            allowed = allowed,
                            theme = theme,
                            c = c,
                            playback = playback,
                            togglePlay =
                                togglePlay,
                            play =
                                onPlaySong,
                            options = {
                                    song,
                                    recent ->

                                recentOption =
                                    recent

                                optionCategoryId =
                                    null

                                optionsSong =
                                    song
                            }
                        )
                    }

                    HomeMode.Liked -> {
                        HomeLiked(
                            songs = likedSongs,
                            playback = playback,
                            c = c,
                            play = {
                                if (
                                    playback.currentSongId !=
                                    it.id
                                ) {
                                    onPlaySong(
                                        it,
                                        "Liked Songs",
                                        false,
                                        likedSongs
                                    )
                                }
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
                                optionCategoryId = null
                                optionsSong = it
                            }
                        )
                    }

                    HomeMode.Categories -> {
                        HomeCategories(
                            songs = songs,
                            categories = categories,
                            c = c,
                            back = {
                                changeMode(
                                    HomeMode.Home.name
                                )
                            },
                            create = {
                                categoryName = ""
                                categorySelection =
                                    emptySet()
                                createCategory = true
                            },
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
                    categories
                        .firstOrNull {
                            it.id ==
                                current.id
                        }

                if (category != null) {
                    HomeCategoryDetail(
                        category = category,
                        songs = songs,
                        playback = playback,
                        c = c,
                        back = {
                            page =
                                HomePage.Root
                        },
                        add = {
                            page =
                                HomePage.CategoryPicker(
                                    category.id
                                )
                        },
                        delete = {
                            saveCategories(
                                categories.filterNot {
                                    it.id ==
                                        category.id
                                }
                            )

                            page =
                                HomePage.Root
                        },
                        shuffle = {
                            shuffleSongs(
                                it,
                                category.name,
                                true
                            )
                        },
                        play = {
                                song,
                                queue ->

                            onPlaySong(
                                song,
                                category.name,
                                true,
                                queue
                            )
                        },
                        options = {
                            recentOption = false
                            optionCategoryId =
                                category.id
                            optionsSong = it
                        }
                    )
                }
            }

            is HomePage.CategoryPicker -> {
                val category =
                    categories
                        .firstOrNull {
                            it.id ==
                                current.id
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
                }
            }
        }
    }

    if (menu) {
        HomeMenuDialog(
            c = c,
            dismiss = {
                menu = false
            },
            liked = {
                menu = false
                page = HomePage.Root
                changeMode(
                    HomeMode.Liked.name
                )
            },
            categories = {
                menu = false
                page = HomePage.Root
                changeMode(
                    HomeMode.Categories.name
                )
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

    if (createCategory) {
        HomeCreateCategory(
            name = categoryName,
            selected =
                categorySelection,
            songs = songs,
            c = c,
            changeName = {
                categoryName = it
            },
            toggle = { song ->
                categorySelection =
                    if (
                        song.id in
                        categorySelection
                    ) {
                        categorySelection -
                            song.id
                    } else {
                        categorySelection +
                            song.id
                    }
            },
            create = {
                val name =
                    categoryName.trim()

                if (
                    name.isNotEmpty() &&
                    categorySelection
                        .isNotEmpty()
                ) {
                    val category =
                        UserCategory(
                            id =
                                "cat_${UUID.randomUUID()}",
                            name = name,
                            icon =
                                categories.size %
                                    4,
                            songIds =
                                categorySelection
                        )

                    saveCategories(
                        categories +
                            category
                    )

                    createCategory = false
                    categoryName = ""
                    categorySelection =
                        emptySet()
                }
            },
            dismiss = {
                createCategory = false
                categoryName = ""
                categorySelection =
                    emptySet()
            }
        )
    }

    optionsSong?.let { song ->
        val category =
            optionCategoryId
                ?.let { id ->
                    categories
                        .firstOrNull {
                            it.id == id
                        }
                }

        XmoSongList(
            song = song,
            liked =
                song.id in
                    likedSongIds,
            recent =
                recentOption,
            categoryName =
                category?.name,
            c = c,
            dismiss = {
                optionsSong = null
                recentOption = false
                optionCategoryId = null
            },
            toggleLike = {
                toggleLike(song)
            },
            playNext = {
                playNext(song)
            },
            removeRecent = {
                removeRecent(song)
            },
            removeCategory = {
                category?.let {
                    setSongInCategory(
                        song,
                        it.id,
                        false
                    )
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
    togglePlay: () -> Unit,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit,
    options: (
        Song,
        Boolean
    ) -> Unit
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Spacer(
            Modifier.height(7.dp)
        )

        SectionTitle(
            title = "Recently Played",
            subtitle =
                if (
                    recentSongs.isEmpty()
                ) {
                    "Nothing played yet"
                } else {
                    "${recentSongs.size} recent tracks"
                },
            icon =
                R.drawable.ic_xmo_history,
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
                play = {
                    play(
                        it,
                        "Recently Played",
                        false,
                        recentSongs
                    )
                },
                togglePlay =
                    togglePlay,
                options = {
                    options(
                        it,
                        true
                    )
                }
            )
        }

        Spacer(
            Modifier.height(7.dp)
        )

        SectionTitle(
            title = "All Songs",
            subtitle =
                "${songs.size} songs",
            icon =
                R.drawable.ic_xmo_songs,
            c = c
        )

        Spacer(
            Modifier.height(5.dp)
        )

        HomeAllSongs(
            songs = songs,
            allowed = allowed,
            c = c,
            theme = theme,
            playback = playback,
            play = {
                play(
                    it,
                    "All Songs",
                    false,
                    songs
                )
            },
            options = {
                options(
                    it,
                    false
                )
            }
        )
    }
}
