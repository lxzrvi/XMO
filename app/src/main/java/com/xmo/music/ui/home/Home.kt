package com.xmo.music.ui.home

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.HomeCategoryAppearanceStore
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import java.util.UUID

@Composable
fun Home(
    songs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    hazeState: dev.chrisbanes.haze.HazeState,
    categories: List<UserCategory>,
    likedSongIds: Set<Long>,
    recentPlays: List<RecentPlay>,
    scanning: Boolean,
    currentSongId: Long?,
    isPlaying: Boolean,
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
    val context = LocalContext.current
    val c = homeColors(theme)

    val mode =
        runCatching {
            HomeMode.valueOf(modeName)
        }.getOrDefault(HomeMode.Home)

    var page by remember(mode) {
        mutableStateOf<HomePage>(HomePage.Root)
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

    var songCategoryId by remember {
        mutableStateOf<String?>(null)
    }

    var managedCategory by remember {
        mutableStateOf<UserCategory?>(null)
    }

    var renameCategory by remember {
        mutableStateOf<UserCategory?>(null)
    }

    var renameText by remember {
        mutableStateOf("")
    }

    var coverCategory by remember {
        mutableStateOf<UserCategory?>(null)
    }

    var createOpen by remember {
        mutableStateOf(false)
    }

    var createName by remember {
        mutableStateOf("")
    }

    var createSongs by remember {
        mutableStateOf<Set<Long>>(emptySet())
    }

    val covers =
        remember {
            mutableStateMapOf<String, String?>()
        }

    categories.forEach {
        if (!covers.containsKey(it.id)) {
            covers[it.id] =
                HomeCategoryAppearanceStore.cover(
                    context,
                    it.id
                )
        }
    }

    val gallery =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            val category =
                coverCategory

            if (
                uri != null &&
                category != null
            ) {
                runCatching {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                }

                val value =
                    "uri:$uri"

                covers[category.id] = value

                HomeCategoryAppearanceStore
                    .saveCover(
                        context,
                        category.id,
                        value
                    )

                coverCategory = null
            }
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
                        HomeRootContent(
                            songs = songs,
                            recentSongs = recentSongs,
                            allowed = allowed,
                            theme = theme,
                            c = c,
                            currentSongId =
                                currentSongId,
                            isPlaying = isPlaying,
                            togglePlay = togglePlay,
                            play = onPlaySong,
                            options = {
                                    song,
                                    recent ->
                                optionsSong = song
                                recentOption = recent
                                songCategoryId = null
                            }
                        )
                    }

                    HomeMode.Liked -> {
                        HomeLiked(
                            songs = likedSongs,
                            currentSongId =
                                currentSongId,
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
                                optionsSong = it
                                recentOption = false
                                songCategoryId = null
                            }
                        )
                    }

                    HomeMode.Categories -> {
                        HomeCategories(
                            songs = songs,
                            categories = categories,
                            covers = covers,
                            c = c,
                            back = {
                                changeMode(
                                    HomeMode.Home.name
                                )
                            },
                            create = {
                                createName = ""
                                createSongs = emptySet()
                                createOpen = true
                            },
                            open = {
                                page =
                                    HomePage.Category(
                                        it.id
                                    )
                            },
                            options = {
                                managedCategory = it
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
                    HomeCategoryDetail(
                        category = category,
                        songs = songs,
                        currentSongId =
                            currentSongId,
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
                        delete = {
                            deleteCategory(
                                context,
                                category,
                                categories,
                                saveCategories
                            )
                            page = HomePage.Root
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
                            optionsSong = it
                            recentOption = false
                            songCategoryId =
                                category.id
                        }
                    )
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
            allSongs = {
                menu = false
                page = HomePage.Root
                changeMode(HomeMode.Home.name)
            },
            liked = {
                menu = false
                page = HomePage.Root
                changeMode(HomeMode.Liked.name)
            },
            categories = {
                menu = false
                page = HomePage.Root
                changeMode(
                    HomeMode.Categories.name
                )
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

    if (createOpen) {
        HomeCreateCategory(
            name = createName,
            selected = createSongs,
            songs = songs,
            c = c,
            changeName = {
                createName = it
            },
            toggle = {
                createSongs =
                    if (it.id in createSongs) {
                        createSongs - it.id
                    } else {
                        createSongs + it.id
                    }
            },
            create = {
                val name =
                    createName.trim()

                if (
                    name.isNotEmpty() &&
                    createSongs.isNotEmpty()
                ) {
                    saveCategories(
                        categories +
                            UserCategory(
                                id =
                                    "cat_${UUID.randomUUID()}",
                                name = name,
                                icon =
                                    categories.size % 4,
                                songIds =
                                    createSongs
                            )
                    )

                    createOpen = false
                }
            },
            dismiss = {
                createOpen = false
            }
        )
    }

    managedCategory?.let { category ->
        XmoList(
            c = c,
            dismiss = {
                managedCategory = null
            }
        ) {
            XmoListAction(
                title = "Rename",
                icon = Icons.Rounded.Edit,
                c = c
            ) {
                renameText = category.name
                renameCategory = category
                managedCategory = null
            }

            XmoListAction(
                title = "Change Cover",
                icon = Icons.Rounded.Image,
                c = c
            ) {
                coverCategory = category
                managedCategory = null
            }

            XmoListAction(
                title =
                    "Delete ${category.name}",
                icon = Icons.Rounded.Delete,
                c = c
            ) {
                deleteCategory(
                    context,
                    category,
                    categories,
                    saveCategories
                )
                managedCategory = null
            }
        }
    }

    renameCategory?.let { category ->
        HomeRenameCategoryBox(
            value = renameText,
            c = c,
            change = {
                renameText = it.take(24)
            },
            dismiss = {
                renameCategory = null
            },
            save = {
                val name =
                    renameText.trim()

                if (name.isNotEmpty()) {
                    saveCategories(
                        categories.map {
                            if (it.id == category.id) {
                                it.copy(name = name)
                            } else {
                                it
                            }
                        }
                    )
                    renameCategory = null
                }
            }
        )
    }

    coverCategory?.let { category ->
        HomeCategoryCoverList(
            category = category,
            songs = songs.filter {
                it.id in category.songIds
            },
            c = c,
            dismiss = {
                coverCategory = null
            },
            default = {
                covers[category.id] = null
                HomeCategoryAppearanceStore
                    .saveCover(
                        context,
                        category.id,
                        null
                    )
                coverCategory = null
            },
            songCover = { song ->
                val value =
                    "song:${song.id}"

                covers[category.id] = value

                HomeCategoryAppearanceStore
                    .saveCover(
                        context,
                        category.id,
                        value
                    )

                coverCategory = null
            },
            custom = {
                gallery.launch(
                    arrayOf("image/*")
                )
            }
        )
    }

    optionsSong?.let { song ->
        val category =
            songCategoryId?.let { id ->
                categories.firstOrNull {
                    it.id == id
                }
            }

        XmoSongList(
            song = song,
            liked =
                song.id in likedSongIds,
            recent = recentOption,
            categoryName =
                category?.name,
            c = c,
            dismiss = {
                optionsSong = null
                recentOption = false
                songCategoryId = null
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

private fun deleteCategory(
    context: android.content.Context,
    category: UserCategory,
    categories: List<UserCategory>,
    save: (List<UserCategory>) -> Unit
) {
    HomeCategoryAppearanceStore.delete(
        context,
        category.id
    )

    save(
        categories.filterNot {
            it.id == category.id
        }
    )
}

@Composable
private fun HomeRootContent(
    songs: List<Song>,
    recentSongs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    c: HomeColors,
    currentSongId: Long?,
    isPlaying: Boolean,
    togglePlay: () -> Unit,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit,
    options: (Song, Boolean) -> Unit
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Spacer(Modifier.height(7.dp))

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

        HomeRecentlyPlayed(
            songs = recentSongs,
            c = c,
            currentSongId = currentSongId,
            isPlaying = isPlaying,
            play = {
                play(
                    it,
                    "Recently Played",
                    false,
                    recentSongs
                )
            },
            togglePlay = togglePlay,
            options = {
                options(it, true)
            }
        )

        Spacer(Modifier.height(7.dp))

        SectionTitle(
            title = "All Songs",
            subtitle = "${songs.size} songs",
            icon = R.drawable.ic_xmo_songs,
            c = c
        )

        Spacer(Modifier.height(5.dp))

        HomeAllSongs(
            songs = songs,
            allowed = allowed,
            c = c,
            theme = theme,
            currentSongId = currentSongId,
            isPlaying = isPlaying,
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
