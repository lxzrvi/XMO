package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.Library
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.XmoFont
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun Home(
    songs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    hazeState: HazeState,
    order: List<String>,
    categories: List<UserCategory>,
    likedSongIds: Set<Long>,
    recentPlays: List<RecentPlay>,
    scanning: Boolean,
    playback: PlaybackState,
    refresh: () -> Unit,
    openProfile: () -> Unit,
    saveOrder: (List<String>) -> Unit,
    saveCategories: (List<UserCategory>) -> Unit,
    toggleLike: (Song) -> Unit,
    playNext: (Song) -> Unit,
    removeRecent: (Song) -> Unit,
    setSongInCategory: (Song, String, Boolean) -> Unit,
    onPlaySong: (Song, String, Boolean, List<Song>) -> Unit
) {
    val c = homeColors(theme)
    val top = homeTopColors(theme)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val fixedSections = remember {
        listOf(
            HomeSectionModel("songs", "All Songs", R.drawable.ic_xmo_songs),
            HomeSectionModel("albums", "Albums", R.drawable.ic_xmo_album),
            HomeSectionModel("liked", "Liked Songs", R.drawable.ic_xmo_heart),
            HomeSectionModel("artists", "Artists", R.drawable.ic_xmo_artist)
        )
    }

    val customIcons = remember {
        listOf(
            R.drawable.ic_xmo_star,
            R.drawable.ic_xmo_spark,
            R.drawable.ic_xmo_diamond,
            R.drawable.ic_xmo_bolt
        )
    }

    val customSections = remember(categories) {
        categories.map {
            HomeSectionModel(
                id = it.id,
                title = it.name,
                icon = customIcons[
                    Math.floorMod(
                        it.icon,
                        customIcons.size
                    )
                ]
            )
        }
    }

    val sectionMap =
        remember(fixedSections, customSections) {
            (fixedSections + customSections)
                .associateBy { it.id }
        }

    val resolvedOrder =
        remember(order, sectionMap) {
            (
                order.filter(sectionMap::containsKey) +
                    sectionMap.keys.filterNot {
                        it in order
                    }
                ).distinct()
        }

    var currentOrder by remember {
        mutableStateOf(resolvedOrder)
    }

    LaunchedEffect(resolvedOrder) {
        if (currentOrder != resolvedOrder) {
            currentOrder = resolvedOrder
        }
    }

    val albums = remember(songs) {
        Library.albums(songs)
    }

    val artists = remember(songs) {
        Library.artists(songs)
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

    var selectedCategory by remember {
        mutableStateOf("all")
    }

    var dockHeight by remember {
        mutableIntStateOf(0)
    }

    var addCategory by remember {
        mutableStateOf(false)
    }

    var newCategoryName by remember {
        mutableStateOf("")
    }

    var optionsSong by remember {
        mutableStateOf<Song?>(null)
    }

    var optionsFromRecent by remember {
        mutableStateOf(false)
    }

    var layer by remember {
        mutableStateOf<HomeLayer?>(null)
    }

    fun openSection(id: String) {
        selectedCategory = id

        scope.launch {
            if (id == "all") {
                listState.animateScrollToItem(0)
                return@launch
            }

            val position =
                currentOrder.indexOf(id)

            if (position >= 0) {
                listState.animateScrollToItem(
                    index = position + 3,
                    scrollOffset = -dockHeight
                )
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(bottom = 620.dp)
        ) {
            item(key = "top") {
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
                        refresh = {
                            layer = HomeLayer.Scanner
                        },
                        openMenu = {
                            layer = HomeLayer.Menu
                        },
                        openProfile = openProfile
                    )
                }
            }

            stickyHeader(
                key = "category_dock"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(top.background)
                        .onSizeChanged {
                            dockHeight = it.height
                        }
                ) {
                    HomeCategoryDock(
                        sections = sectionMap,
                        order = currentOrder,
                        selected = selectedCategory,
                        c = c,
                        select = ::openSection,
                        commit = {
                            if (it != currentOrder) {
                                currentOrder = it
                                saveOrder(it)
                            }
                        },
                        add = {
                            addCategory = true
                        }
                    )
                }
            }

            item(key = "recent") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 10.dp,
                            bottom = 18.dp
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

                    Spacer(Modifier.height(4.dp))

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
                                onPlaySong(
                                    it,
                                    "Recently Played",
                                    false,
                                    recentSongs
                                )
                            },
                            options = {
                                optionsFromRecent = true
                                optionsSong = it
                            }
                        )
                    }
                }
            }

            items(
                items = currentOrder,
                key = {
                    "section_$it"
                }
            ) { id ->
                val section =
                    sectionMap[id]
                        ?: return@items

                val customCategory =
                    categories.firstOrNull {
                        it.id == id
                    }

                val categorySongs =
                    remember(
                        customCategory,
                        songs
                    ) {
                        if (customCategory == null) {
                            emptyList()
                        } else {
                            songs.filter {
                                it.id in customCategory.songIds
                            }
                        }
                    }

                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 10.dp,
                            bottom = 18.dp
                        )
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            title = section.title,
                            subtitle =
                                when (id) {
                                    "songs" ->
                                        "${songs.size} songs"
                                    "albums" ->
                                        "${albums.size} albums"
                                    "liked" ->
                                        "${likedSongs.size} favorites"
                                    "artists" ->
                                        "${artists.size} artists"
                                    else ->
                                        "${categorySongs.size} songs"
                                },
                            icon = section.icon,
                            c = c,
                            modifier =
                                Modifier.weight(1f)
                        )

                        if (
                            id == "liked" ||
                            customCategory != null
                        ) {
                            HomeCircleAdd {
                                layer =
                                    HomeLayer.SongList(
                                        title = section.title,
                                        source = section.title,
                                        category =
                                            customCategory != null,
                                        songs =
                                            if (id == "liked") {
                                                likedSongs
                                            } else {
                                                categorySongs
                                            }
                                    )
                            }
                        }
                    }

                    Spacer(Modifier.height(5.dp))

                    when (id) {
                        "songs" -> {
                            HomeAllSongs(
                                songs = songs,
                                allowed = allowed,
                                c = c,
                                theme = theme,
                                play = {
                                    onPlaySong(
                                        it,
                                        "All Songs",
                                        false,
                                        songs
                                    )
                                },
                                options = {
                                    optionsFromRecent = false
                                    optionsSong = it
                                }
                            )
                        }

                        "albums" -> {
                            HomeAlbums(
                                albums = albums,
                                c = c,
                                open = { album ->
                                    layer =
                                        HomeLayer.SongList(
                                            title = album.name,
                                            source = album.name,
                                            category = false,
                                            songs = album.songs
                                        )
                                }
                            )
                        }

                        "liked" -> {
                            HomeCompactSongs(
                                songs = likedSongs,
                                empty =
                                    "No liked songs yet",
                                c = c,
                                play = {
                                    onPlaySong(
                                        it,
                                        "Liked Songs",
                                        false,
                                        likedSongs
                                    )
                                },
                                options = {
                                    optionsFromRecent = false
                                    optionsSong = it
                                }
                            )
                        }

                        "artists" -> {
                            HomeArtists(
                                songs = songs,
                                c = c
                            )
                        }

                        else -> {
                            HomeCompactSongs(
                                songs = categorySongs,
                                empty =
                                    "No songs in this category",
                                c = c,
                                play = {
                                    onPlaySong(
                                        it,
                                        section.title,
                                        true,
                                        categorySongs
                                    )
                                },
                                options = {
                                    optionsFromRecent = false
                                    optionsSong = it
                                }
                            )
                        }
                    }
                }
            }

            item(key = "footer") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.weight(1f))

                    Text(
                        text = "XMO",
                        color = c.text,
                        fontFamily = XmoFont.logo,
                        fontSize = 19.sp
                    )

                    Text(
                        text =
                            "lxzrvi • copyright © 2026",
                        color = c.sub,
                        fontFamily = XmoFont.normal,
                        fontSize = 9.sp
                    )

                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }

    if (addCategory) {
        XmoBox(
            title = "New category",
            c = c,
            dismiss = {
                addCategory = false
                newCategoryName = ""
            }
        ) {
            BasicTextField(
                value = newCategoryName,
                onValueChange = {
                    newCategoryName =
                        it.take(24)
                },
                singleLine = true,
                textStyle =
                    androidx.compose.ui.text.TextStyle(
                        color = c.text,
                        fontFamily = XmoFont.normal,
                        fontSize = 14.sp
                    ),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(
                            c.button,
                            RoundedCornerShape(15.dp)
                        )
                        .border(
                            .7.dp,
                            c.border,
                            RoundedCornerShape(15.dp)
                        )
                        .padding(horizontal = 14.dp),
                decorationBox = { field ->
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.CenterStart
                    ) {
                        if (newCategoryName.isBlank()) {
                            Text(
                                text = "Category name",
                                color = c.sub,
                                fontFamily = XmoFont.normal,
                                fontSize = 13.sp
                            )
                        }
                        field()
                    }
                }
            )

            Spacer(Modifier.height(14.dp))

            HomeDialogAction(
                text = "Add Category",
                enabled =
                    newCategoryName
                        .trim()
                        .isNotEmpty()
            ) {
                val name =
                    newCategoryName.trim()

                if (name.isNotEmpty()) {
                    val category =
                        UserCategory(
                            id =
                                "cat_${UUID.randomUUID()}",
                            name = name,
                            icon = categories.size % 4
                        )

                    val next =
                        currentOrder + category.id

                    currentOrder = next
                    saveCategories(
                        categories + category
                    )
                    saveOrder(next)
                    newCategoryName = ""
                    addCategory = false
                }
            }
        }
    }

    optionsSong?.let { song ->
        XmoSongList(
            song = song,
            liked = song.id in likedSongIds,
            recent = optionsFromRecent,
            c = c,
            dismiss = {
                optionsSong = null
                optionsFromRecent = false
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

    layer?.let { current ->
        when (current) {
            HomeLayer.Menu -> {
                HomeMenuDialog(
                    c = c,
                    dismiss = {
                        layer = null
                    },
                    allSongs = {
                        layer =
                            HomeLayer.SongList(
                                "All Songs",
                                "All Songs",
                                false,
                                songs
                            )
                    },
                    liked = {
                        layer =
                            HomeLayer.SongList(
                                "Liked Songs",
                                "Liked Songs",
                                false,
                                likedSongs
                            )
                    },
                    scanner = {
                        layer = HomeLayer.Scanner
                    }
                )
            }

            HomeLayer.Scanner -> {
                HomeScannerDialog(
                    c = c,
                    scanning = scanning,
                    songCount = songs.size,
                    scan = refresh,
                    dismiss = {
                        if (!scanning) {
                            layer = null
                        }
                    }
                )
            }

            is HomeLayer.SongList -> {
                HomeFullSongList(
                    model = current,
                    c = c,
                    close = {
                        layer = null
                    },
                    play = onPlaySong,
                    options = {
                        optionsFromRecent = false
                        optionsSong = it
                    }
                )
            }
        }
    }
}
