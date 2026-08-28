package com.xmo.music.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.Album
import com.xmo.music.data.Library
import com.xmo.music.data.RecentPlay
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.blur.glassBorder
import com.xmo.music.ui.blur.liveBlur
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

private data class HomeSectionModel(
    val id: String,
    val title: String,
    val icon: Int,
    val tint: Color? = null
)

private sealed interface HomeLayer {
    data object Menu : HomeLayer
    data object Scanner : HomeLayer

    data class SongList(
        val title: String,
        val source: String,
        val category: Boolean,
        val songs: List<Song>
    ) : HomeLayer
}

@OptIn(
    ExperimentalFoundationApi::class
)
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
    refresh: () -> Unit,
    openProfile: () -> Unit,
    saveOrder: (List<String>) -> Unit,
    saveCategories: (List<UserCategory>) -> Unit,
    toggleLike: (Song) -> Unit,
    setSongInCategory: (
        Song,
        String,
        Boolean
    ) -> Unit,
    onPlaySong: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit
) {
    val c =
        homeColors(
            theme
        )

    val accent =
        LocalXmoAccent.current

    val scope =
        rememberCoroutineScope()

    val listState =
        rememberLazyListState()

    val fixedSections =
        remember {
            listOf(
                HomeSectionModel(
                    id = "songs",
                    title = "All Songs",
                    icon = R.drawable.ic_xmo_songs
                ),
                HomeSectionModel(
                    id = "albums",
                    title = "Albums",
                    icon = R.drawable.ic_xmo_album
                ),
                HomeSectionModel(
                    id = "liked",
                    title = "Liked Songs",
                    icon = R.drawable.ic_xmo_heart
                ),
                HomeSectionModel(
                    id = "artists",
                    title = "Artists",
                    icon = R.drawable.ic_xmo_artist
                )
            )
        }

    val customIcons =
        remember {
            listOf(
                R.drawable.ic_xmo_star,
                R.drawable.ic_xmo_spark,
                R.drawable.ic_xmo_diamond,
                R.drawable.ic_xmo_bolt
            )
        }

    val customColors =
        remember {
            listOf(
                Color(0xFFFFC107),
                Color(0xFFAF52DE),
                Color(0xFF00AEEF),
                Color(0xFFFF7043)
            )
        }

    val customSections =
        remember(
            categories
        ) {
            categories.map {
                val index =
                    Math.floorMod(
                        it.icon,
                        customIcons.size
                    )

                HomeSectionModel(
                    id = it.id,
                    title = it.name,
                    icon = customIcons[index],
                    tint = customColors[index]
                )
            }
        }

    val sectionMap =
        remember(
            fixedSections,
            customSections
        ) {
            (
                fixedSections +
                    customSections
                )
                .associateBy {
                    it.id
                }
        }

    val resolvedOrder =
        remember(
            order,
            sectionMap
        ) {
            (
                order.filter {
                    sectionMap.containsKey(
                        it
                    )
                } +
                    sectionMap.keys.filterNot {
                        it in order
                    }
                )
                .distinct()
        }

    var currentOrder by
        remember {
            mutableStateOf(
                resolvedOrder
            )
        }

    LaunchedEffect(
        resolvedOrder
    ) {
        if (
            currentOrder !=
            resolvedOrder
        ) {
            currentOrder =
                resolvedOrder
        }
    }

    val albums =
        remember(
            songs
        ) {
            Library.albums(
                songs
            )
        }

    val artists =
        remember(
            songs
        ) {
            Library.artists(
                songs
            )
        }

    val likedSongs =
        remember(
            songs,
            likedSongIds
        ) {
            songs.filter {
                it.id in
                    likedSongIds
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
                    map[
                        it.songId
                    ]
                }
                .take(
                    12
                )
        }

    var selectedCategory by
        remember {
            mutableStateOf(
                "all"
            )
        }

    var dockHeight by
        remember {
            mutableIntStateOf(
                0
            )
        }

    var addCategory by
        remember {
            mutableStateOf(
                false
            )
        }

    var newCategoryName by
        remember {
            mutableStateOf("")
        }

    var optionsSong by
        remember {
            mutableStateOf<Song?>(
                null
            )
        }

    var layer by
        remember {
            mutableStateOf<HomeLayer?>(
                null
            )
        }

    suspend fun navigateToSection(
        id: String
    ) {
        if (
            id ==
            "all"
        ) {
            listState.animateScrollToItem(
                0
            )

            return
        }

        val position =
            currentOrder.indexOf(
                id
            )

        if (
            position <
            0
        ) {
            return
        }

        val itemIndex =
            position +
                3

        listState.animateScrollToItem(
            index = itemIndex,
            scrollOffset = -dockHeight
        )

        withFrameNanos { }

        listState.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index ==
                    itemIndex
            }
            ?.let {
                val correction =
                    it.offset -
                        dockHeight

                if (
                    abs(
                        correction
                    ) >
                    1
                ) {
                    listState.scrollBy(
                        correction.toFloat()
                    )
                }
            }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
    ) {
        LazyColumn(
            state =
                listState,

            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    ),

            contentPadding =
                PaddingValues(
                    bottom =
                        190.dp
                )
        ) {
            item(
                key =
                    "header"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 5.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                24.dp
                            )
                        )
                        .liveBlur(
                            hazeState,
                            theme
                        )
                        .border(
                            .6.dp,
                            glassBorder(
                                theme
                            ),
                            RoundedCornerShape(
                                24.dp
                            )
                        )
                ) {
                    HomeHeader(
                        c =
                            c,

                        theme =
                            theme,

                        hazeState =
                            hazeState,

                        refresh = {
                            layer =
                                HomeLayer.Scanner
                        },

                        openMenu = {
                            layer =
                                HomeLayer.Menu
                        },

                        openProfile =
                            openProfile
                    )
                }
            }

            stickyHeader(
                key =
                    "category_dock"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 3.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                22.dp
                            )
                        )
                        .liveBlur(
                            hazeState,
                            theme
                        )
                        .border(
                            .6.dp,
                            glassBorder(
                                theme
                            ),
                            RoundedCornerShape(
                                22.dp
                            )
                        )
                        .onSizeChanged {
                            dockHeight =
                                it.height
                        }
                ) {
                    HomeCategoryRow(
                        sections =
                            sectionMap,

                        order =
                            currentOrder,

                        selected =
                            selectedCategory,

                        c =
                            c,

                        select = {
                            selectedCategory =
                                it

                            scope.launch {
                                navigateToSection(
                                    it
                                )
                            }
                        },

                        commit = {
                            if (
                                it !=
                                currentOrder
                            ) {
                                currentOrder =
                                    it

                                saveOrder(
                                    it
                                )
                            }
                        },

                        add = {
                            addCategory =
                                true
                        }
                    )
                }
            }

            item(
                key =
                    "recent"
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 8.dp,
                            bottom = 16.dp
                        )
                ) {
                    SectionTitle(
                        title =
                            "Recently Played",

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

                        c =
                            c
                    )

                    if (
                        recentSongs.isEmpty()
                    ) {
                        HomeEmpty(
                            "Nothing played yet",
                            c
                        )
                    } else {
                        HomeRecentRow(
                            songs =
                                recentSongs,

                            c =
                                c,

                            play = {
                                onPlaySong(
                                    it,
                                    "Recently Played",
                                    false,
                                    recentSongs
                                )
                            },

                            options = {
                                optionsSong =
                                    it
                            }
                        )
                    }
                }
            }

            items(
                items =
                    currentOrder,

                key = {
                    "section_$it"
                }
            ) { id ->

                val section =
                    sectionMap[
                        id
                    ]

                if (
                    section !=
                    null
                ) {
                    val customCategory =
                        categories
                            .firstOrNull {
                                it.id ==
                                    id
                            }

                    val categorySongs =
                        remember(
                            customCategory,
                            songs
                        ) {
                            if (
                                customCategory ==
                                null
                            ) {
                                emptyList()
                            } else {
                                songs.filter {
                                    it.id in
                                        customCategory.songIds
                                }
                            }
                        }

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                vertical =
                                    9.dp
                            )
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal =
                                        12.dp
                                ),

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {
                            SectionTitle(
                                title =
                                    section.title,

                                subtitle =
                                    when (
                                        id
                                    ) {
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

                                icon =
                                    section.icon,

                                c =
                                    c,

                                modifier =
                                    Modifier.weight(
                                        1f
                                    )
                            )

                            if (
                                id !=
                                "artists"
                            ) {
                                HomeCircleAction(
                                    icon =
                                        R.drawable.ic_xmo_add
                                ) {
                                    val target =
                                        when (
                                            id
                                        ) {
                                            "songs" ->
                                                HomeLayer.SongList(
                                                    "All Songs",
                                                    "All Songs",
                                                    false,
                                                    songs
                                                )

                                            "albums" ->
                                                HomeLayer.SongList(
                                                    "Library",
                                                    "All Songs",
                                                    false,
                                                    songs
                                                )

                                            "liked" ->
                                                HomeLayer.SongList(
                                                    "Liked Songs",
                                                    "Liked Songs",
                                                    false,
                                                    likedSongs
                                                )

                                            else ->
                                                HomeLayer.SongList(
                                                    section.title,
                                                    section.title,
                                                    true,
                                                    categorySongs
                                                )
                                        }

                                    layer =
                                        target
                                }
                            }
                        }

                        when (
                            id
                        ) {
                            "songs" -> {
                                HomeAllSongs(
                                    songs =
                                        songs,

                                    allowed =
                                        allowed,

                                    c =
                                        c,

                                    theme =
                                        theme,

                                    play = {
                                        onPlaySong(
                                            it,
                                            "All Songs",
                                            false,
                                            songs
                                        )
                                    },

                                    options = {
                                        optionsSong =
                                            it
                                    }
                                )
                            }

                            "albums" -> {
                                HomeAlbums(
                                    albums =
                                        albums,

                                    c =
                                        c,

                                    open = { album ->
                                        layer =
                                            HomeLayer.SongList(
                                                title =
                                                    album.name,

                                                source =
                                                    album.name,

                                                category =
                                                    false,

                                                songs =
                                                    album.songs
                                            )
                                    }
                                )
                            }

                            "liked" -> {
                                HomeCompactSongs(
                                    songs =
                                        likedSongs,

                                    empty =
                                        "No liked songs yet",

                                    c =
                                        c,

                                    play = {
                                        onPlaySong(
                                            it,
                                            "Liked Songs",
                                            false,
                                            likedSongs
                                        )
                                    },

                                    options = {
                                        optionsSong =
                                            it
                                    }
                                )
                            }

                            "artists" -> {
                                HomeArtists(
                                    songs =
                                        songs,

                                    c =
                                        c
                                )
                            }

                            else -> {
                                HomeCompactSongs(
                                    songs =
                                        categorySongs,

                                    empty =
                                        "No songs in this category",

                                    c =
                                        c,

                                    play = {
                                        onPlaySong(
                                            it,
                                            section.title,
                                            true,
                                            categorySongs
                                        )
                                    },

                                    options = {
                                        optionsSong =
                                            it
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item(
                key =
                    "footer"
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            360.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally,

                    verticalArrangement =
                        Arrangement.Center
                ) {
                    Text(
                        "XMO",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.logo,

                        fontSize =
                            19.sp
                    )

                    Text(
                        "lxzrvi • copyright © 2026",

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.thin,

                        fontSize =
                            9.sp
                    )
                }
            }
        }
    }

    if (
        addCategory
    ) {
        HomeDialog(
            title =
                "New category",

            c =
                c,

            dismiss = {
                addCategory =
                    false

                newCategoryName =
                    ""
            }
        ) {
            BasicTextField(
                value =
                    newCategoryName,

                onValueChange = {
                    newCategoryName =
                        it.take(
                            24
                        )
                },

                singleLine =
                    true,

                textStyle =
                    androidx.compose.ui.text.TextStyle(
                        color =
                            c.text,

                        fontFamily =
                            XmoFont.normal,

                        fontSize =
                            14.sp
                    ),

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            48.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .background(
                            c.button
                        )
                        .border(
                            .7.dp,
                            c.border,
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .padding(
                            horizontal =
                                14.dp
                        ),

                decorationBox = {
                    Box(
                        Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.CenterStart
                    ) {
                        if (
                            newCategoryName.isBlank()
                        ) {
                            Text(
                                "Category name",

                                color =
                                    c.sub,

                                fontFamily =
                                    XmoFont.thin,

                                fontSize =
                                    13.sp
                            )
                        }

                        it()
                    }
                }
            )

            Spacer(
                Modifier.height(
                    14.dp
                )
            )

            HomeDialogAction(
                text =
                    "Add Category",

                enabled =
                    newCategoryName
                        .trim()
                        .isNotEmpty()
            ) {
                val name =
                    newCategoryName
                        .trim()

                if (
                    name.isNotEmpty()
                ) {
                    val category =
                        UserCategory(
                            id =
                                "cat_${UUID.randomUUID()}",

                            name =
                                name,

                            icon =
                                categories.size %
                                    4
                        )

                    val nextOrder =
                        currentOrder +
                            category.id

                    currentOrder =
                        nextOrder

                    saveCategories(
                        categories +
                            category
                    )

                    saveOrder(
                        nextOrder
                    )

                    newCategoryName =
                        ""

                    addCategory =
                        false
                }
            }
        }
    }

    optionsSong?.let { song ->
        HomeSongOptions(
            song =
                song,

            liked =
                song.id in
                    likedSongIds,

            categories =
                categories,

            c =
                c,

            dismiss = {
                optionsSong =
                    null
            },

            toggleLike = {
                toggleLike(
                    song
                )
            },

            setCategory = {
                    categoryId,
                    added ->

                setSongInCategory(
                    song,
                    categoryId,
                    added
                )
            }
        )
    }

    layer?.let {
        when (
            it
        ) {
            HomeLayer.Menu -> {
                HomeMenuDialog(
                    c =
                        c,

                    dismiss = {
                        layer =
                            null
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
                        layer =
                            HomeLayer.Scanner
                    }
                )
            }

            HomeLayer.Scanner -> {
                HomeScannerDialog(
                    c =
                        c,

                    scanning =
                        scanning,

                    songCount =
                        songs.size,

                    scan =
                        refresh,

                    dismiss = {
                        if (
                            !scanning
                        ) {
                            layer =
                                null
                        }
                    }
                )
            }

            is HomeLayer.SongList -> {
                HomeFullSongList(
                    model =
                        it,

                    c =
                        c,

                    close = {
                        layer =
                            null
                    },

                    play =
                        onPlaySong,

                    options = {
                        optionsSong =
                            it
                    }
                )
            }
        }
    }
}

/*
 * =============================================================
 * CATEGORY REORDER
 * =============================================================
 */

@Composable
private fun HomeCategoryRow(
    sections: Map<String, HomeSectionModel>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    select: (String) -> Unit,
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    val state =
        rememberLazyListState()

    val scope =
        rememberCoroutineScope()

    val haptic =
        LocalHapticFeedback.current

    val density =
        LocalDensity.current

    val edge =
        with(
            density
        ) {
            66.dp.toPx()
        }

    var preview by
        remember(
            order
        ) {
            mutableStateOf(
                order
            )
        }

    var draggingId by
        remember {
            mutableStateOf<String?>(
                null
            )
        }

    var fingerX by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    var grabX by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    var autoJob by
        remember {
            mutableStateOf<Job?>(
                null
            )
        }

    LaunchedEffect(
        order
    ) {
        if (
            draggingId ==
            null
        ) {
            preview =
                order
        }
    }

    fun info(
        id: String
    ): LazyListItemInfo? =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.key ==
                    "category_$id"
            }

    fun stopAuto() {
        autoJob?.cancel()

        autoJob =
            null
    }

    fun updateDestination(
        id: String
    ) {
        val from =
            preview.indexOf(
                id
            )

        if (
            from <
            0
        ) {
            return
        }

        var destination =
            from

        preview.forEachIndexed {
                index,
                candidate ->

            if (
                candidate ==
                id
            ) {
                return@forEachIndexed
            }

            val item =
                info(
                    candidate
                )
                    ?: return@forEachIndexed

            val center =
                item.offset +
                    item.size /
                    2f

            if (
                index <
                from &&
                fingerX <
                center
            ) {
                destination =
                    minOf(
                        destination,
                        index
                    )
            }

            if (
                index >
                from &&
                fingerX >
                center
            ) {
                destination =
                    maxOf(
                        destination,
                        index
                    )
            }
        }

        if (
            destination ==
            from
        ) {
            return
        }

        val next =
            preview.toMutableList()

        val moving =
            next.removeAt(
                from
            )

        next.add(
            destination.coerceIn(
                0,
                next.size
            ),
            moving
        )

        preview =
            next
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(
                46.dp
            )
    ) {
        LazyRow(
            state =
                state,

            userScrollEnabled =
                draggingId ==
                    null,

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    horizontal = 12.dp,
                    vertical = 6.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            item(
                key =
                    "all"
            ) {
                CategoryChip(
                    text =
                        "All",

                    active =
                        selected ==
                            "all",

                    c =
                        c,

                    icon =
                        R.drawable.ic_xmo_all
                ) {
                    if (
                        draggingId ==
                        null
                    ) {
                        select(
                            "all"
                        )
                    }
                }
            }

            items(
                items =
                    preview,

                key = {
                    "category_$it"
                }
            ) { id ->

                val section =
                    sections[
                        id
                    ]
                        ?: return@items

                val dragging =
                    draggingId ==
                        id

                Box(
                    Modifier
                        .graphicsLayer {
                            alpha =
                                if (
                                    dragging
                                ) {
                                    0f
                                } else {
                                    1f
                                }
                        }
                        .pointerInput(
                            id
                        ) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { local ->

                                    val item =
                                        info(
                                            id
                                        )
                                            ?: return@detectDragGesturesAfterLongPress

                                    draggingId =
                                        id

                                    fingerX =
                                        item.offset +
                                            local.x

                                    grabX =
                                        local.x

                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                },

                                onDrag = {
                                        change,
                                        amount ->

                                    change.consume()

                                    fingerX +=
                                        amount.x

                                    updateDestination(
                                        id
                                    )

                                    val start =
                                        state.layoutInfo
                                            .viewportStartOffset
                                            .toFloat()

                                    val end =
                                        state.layoutInfo
                                            .viewportEndOffset
                                            .toFloat()

                                    val nearLeft =
                                        fingerX <
                                            start +
                                            edge &&
                                            state.canScrollBackward

                                    val nearRight =
                                        fingerX >
                                            end -
                                            edge &&
                                            state.canScrollForward

                                    if (
                                        nearLeft ||
                                        nearRight
                                    ) {
                                        stopAuto()

                                        val direction =
                                            if (
                                                nearLeft
                                            ) {
                                                -1f
                                            } else {
                                                1f
                                            }

                                        autoJob =
                                            scope.launch {
                                                while (
                                                    isActive &&
                                                    draggingId ==
                                                    id
                                                ) {
                                                    val consumed =
                                                        state.scrollBy(
                                                            direction *
                                                                16f
                                                        )

                                                    updateDestination(
                                                        id
                                                    )

                                                    if (
                                                        abs(
                                                            consumed
                                                        ) <
                                                        .1f
                                                    ) {
                                                        break
                                                    }

                                                    delay(
                                                        16L
                                                    )
                                                }
                                            }
                                    } else {
                                        stopAuto()
                                    }
                                },

                                onDragEnd = {
                                    stopAuto()

                                    val result =
                                        preview.toList()

                                    draggingId =
                                        null

                                    fingerX =
                                        0f

                                    grabX =
                                        0f

                                    commit(
                                        result
                                    )
                                },

                                onDragCancel = {
                                    stopAuto()

                                    draggingId =
                                        null

                                    preview =
                                        order

                                    fingerX =
                                        0f

                                    grabX =
                                        0f
                                }
                            )
                        }
                ) {
                    CategoryChip(
                        text =
                            section.title,

                        active =
                            selected ==
                                id,

                        c =
                            c,

                        icon =
                            section.icon,

                        tint =
                            section.tint
                                ?: c.icon
                    ) {
                        if (
                            draggingId ==
                            null
                        ) {
                            select(
                                id
                            )
                        }
                    }
                }
            }

            item(
                key =
                    "add"
            ) {
                CategoryChip(
                    text =
                        "Add",

                    active =
                        false,

                    c =
                        c,

                    icon =
                        R.drawable.ic_xmo_add,

                    tint =
                        LocalXmoAccent.current,

                    onClick =
                        add
                )
            }
        }

        draggingId?.let { id ->
            val section =
                sections[
                    id
                ]
                    ?: return@let

            Box(
                Modifier
                    .zIndex(
                        50f
                    )
                    .graphicsLayer {
                        translationX =
                            fingerX -
                                grabX

                        scaleX =
                            1.07f

                        scaleY =
                            1.07f

                        shadowElevation =
                            10f
                    }
            ) {
                CategoryChip(
                    text =
                        section.title,

                    active =
                        true,

                    c =
                        c,

                    icon =
                        section.icon,

                    tint =
                        section.tint
                            ?: c.icon
                ) {}
            }
        }
    }
}

/*
 * =============================================================
 * RECENTLY PLAYED
 * =============================================================
 */

@Composable
private fun HomeRecentRow(
    songs: List<Song>,
    c: HomeColors,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    LazyRow(
        contentPadding =
            PaddingValues(
                horizontal =
                    20.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        items(
            items =
                songs,

            key = {
                "recent_${it.id}"
            }
        ) { song ->

            Box(
                Modifier
                    .width(
                        284.dp
                    )
                    .height(
                        112.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {
                            play(
                                song
                            )
                        },

                        onLongClick = {
                            options(
                                song
                            )
                        }
                    )
                    .background(
                        c.surface
                    )
                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(
                            18.dp
                        )
                    )
            ) {
                AsyncImage(
                    model =
                        song.artwork,

                    contentDescription =
                        song.title,

                    modifier =
                        Modifier.fillMaxSize(),

                    contentScale =
                        ContentScale.Crop
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                    Color.Black.copy(
                                        alpha = .82f
                                    )
                                )
                            )
                        )
                )

                Text(
                    song.title,

                    color =
                        Color.White,

                    fontFamily =
                        XmoFont.bold,

                    fontSize =
                        12.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis,

                    modifier =
                        Modifier
                            .align(
                                Alignment.BottomStart
                            )
                            .fillMaxWidth(
                                .76f
                            )
                            .padding(
                                12.dp
                            )
                )

                Box(
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            10.dp
                        )
                        .size(
                            33.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color.Black.copy(
                                alpha = .46f
                            )
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.PlayArrow,

                        contentDescription =
                            "Play",

                        tint =
                            Color.White,

                        modifier =
                            Modifier.size(
                                19.dp
                            )
                    )
                }
            }
        }
    }
}

/*
 * =============================================================
 * ALL SONGS
 * =============================================================
 */

@Stable
private class HomeSongScroller {
    var click by
        mutableIntStateOf(
            0
        )
        private set

    var hold by
        mutableStateOf(
            false
        )
        private set

    fun tap() {
        click++
    }

    fun begin() {
        hold =
            true
    }

    fun stop() {
        hold =
            false
    }
}

@Composable
private fun HomeAllSongs(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (
        !allowed
    ) {
        HomeEmpty(
            "Music access required",
            c
        )

        return
    }

    if (
        songs.isEmpty()
    ) {
        HomeEmpty(
            "No local music found",
            c
        )

        return
    }

    val arrow =
        remember {
            HomeSongScroller()
        }

    val grid =
        rememberLazyGridState()

    val scope =
        rememberCoroutineScope()

    val slots =
        (
            (
                songs.size +
                    11
                ) /
                12
            ) *
            12

    LaunchedEffect(
        arrow.click
    ) {
        if (
            arrow.click >
            0
        ) {
            val column =
                grid.firstVisibleItemIndex /
                    3

            val maxColumn =
                slots /
                    3 -
                    1

            val target =
                (
                    column +
                        1
                    )
                    .coerceAtMost(
                        maxColumn
                    )

            if (
                target >
                column
            ) {
                grid.animateScrollToItem(
                    target *
                        3
                )
            }
        }
    }

    LaunchedEffect(
        arrow.hold
    ) {
        while (
            arrow.hold &&
            isActive
        ) {
            if (
                abs(
                    grid.scrollBy(
                        19f
                    )
                ) <
                .1f
            ) {
                break
            }

            delay(
                16L
            )
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        c.bg,
                        c.surface.copy(
                            alpha = .52f
                        ),
                        c.bg
                    )
                )
            )
    ) {
        val edge =
            8.dp

        val gap =
            8.dp

        val cardWidth =
            (
                maxWidth -
                    edge *
                    2 -
                    gap *
                    3
                ) /
                4

        val gridHeight =
            (
                cardWidth +
                    37.dp
                ) *
                3 +
                gap *
                2

        LazyHorizontalGrid(
            rows =
                GridCells.Fixed(
                    3
                ),

            state =
                grid,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(
                        gridHeight
                    ),

            contentPadding =
                PaddingValues(
                    horizontal =
                        edge
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    gap
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    gap
                )
        ) {
            items(
                count =
                    slots,

                key = {
                    "all_song_slot_$it"
                }
            ) { slot ->

                val page =
                    slot /
                        12

                val local =
                    slot %
                        12

                val row =
                    local %
                        3

                val column =
                    local /
                        3

                val sourceIndex =
                    page *
                        12 +
                        row *
                        4 +
                        column

                Box(
                    Modifier.width(
                        cardWidth
                    )
                ) {
                    songs.getOrNull(
                        sourceIndex
                    )?.let { song ->

                        SongTile(
                            song =
                                song,

                            index =
                                sourceIndex,

                            c =
                                c,

                            theme =
                                theme,

                            modifier =
                                Modifier.width(
                                    cardWidth
                                ),

                            onClick = {
                                play(
                                    song
                                )
                            },

                            onOptions = {
                                options(
                                    song
                                )
                            }
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .align(
                    Alignment.CenterEnd
                )
                .padding(
                    end =
                        9.dp
                )
                .size(
                    31.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    LocalXmoAccent.current.copy(
                        alpha = .20f
                    )
                )
                .pointerInput(
                    arrow
                ) {
                    detectTapGestures(
                        onPress = {
                            var held =
                                false

                            val job =
                                scope.launch {
                                    delay(
                                        250L
                                    )

                                    held =
                                        true

                                    arrow.begin()
                                }

                            val released =
                                tryAwaitRelease()

                            job.cancel()

                            arrow.stop()

                            if (
                                released &&
                                !held
                            ) {
                                arrow.tap()
                            }
                        }
                    )
                },

            contentAlignment =
                Alignment.Center
        ) {
            XmoIcon(
                icon =
                    R.drawable.ic_xmo_arrow,

                tint =
                    LocalXmoAccent.current,

                modifier =
                    Modifier.size(
                        14.dp
                    )
            )
        }
    }
}

/*
 * =============================================================
 * ALBUM / ARTIST
 * =============================================================
 */

@Composable
private fun HomeAlbums(
    albums: List<Album>,
    c: HomeColors,
    open: (Album) -> Unit
) {
    if (
        albums.isEmpty()
    ) {
        HomeEmpty(
            "No albums found",
            c
        )

        return
    }

    Row(
        Modifier
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal =
                    9.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        albums
            .take(
                20
            )
            .forEach { album ->

                Column(
                    Modifier
                        .width(
                            106.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                13.dp
                            )
                        )
                        .clickable {
                            open(
                                album
                            )
                        }
                        .padding(
                            4.dp
                        )
                ) {
                    AsyncImage(
                        model =
                            album.artwork,

                        contentDescription =
                            album.name,

                        modifier =
                            Modifier
                                .size(
                                    98.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        11.dp
                                    )
                                )
                                .background(
                                    c.button
                                ),

                        contentScale =
                            ContentScale.Crop
                    )

                    Text(
                        album.name,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            10.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis,

                        modifier =
                            Modifier.padding(
                                top =
                                    5.dp
                            )
                    )

                    Text(
                        album.artist,

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.thin,

                        fontSize =
                            8.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }
    }
}

@Composable
private fun HomeArtists(
    songs: List<Song>,
    c: HomeColors
) {
    val artists =
        remember(
            songs
        ) {
            Library.artists(
                songs
            )
        }

    if (
        artists.isEmpty()
    ) {
        HomeEmpty(
            "No artists found",
            c
        )

        return
    }

    Row(
        Modifier
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal =
                    9.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                12.dp
            )
    ) {
        artists
            .take(
                15
            )
            .forEach { artist ->

                Column(
                    Modifier.width(
                        72.dp
                    ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(
                                66.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                LocalXmoAccent.current.copy(
                                    alpha = .14f
                                )
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        if (
                            artist.artwork !=
                            null
                        ) {
                            AsyncImage(
                                model =
                                    artist.artwork,

                                contentDescription =
                                    artist.name,

                                modifier =
                                    Modifier.fillMaxSize(),

                                contentScale =
                                    ContentScale.Crop
                            )
                        } else {
                            Text(
                                artist.name
                                    .firstOrNull()
                                    ?.uppercase()
                                    ?: "?",

                                color =
                                    LocalXmoAccent.current,

                                fontFamily =
                                    XmoFont.bold,

                                fontSize =
                                    18.sp
                            )
                        }
                    }

                    Text(
                        artist.name,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            9.sp,

                        maxLines =
                            2,

                        overflow =
                            TextOverflow.Ellipsis,

                        modifier =
                            Modifier.padding(
                                top =
                                    5.dp
                            )
                    )
                }
            }
    }
}

/*
 * =============================================================
 * COMPACT SONGS
 * =============================================================
 */

@Composable
private fun HomeCompactSongs(
    songs: List<Song>,
    empty: String,
    c: HomeColors,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (
        songs.isEmpty()
    ) {
        HomeEmpty(
            empty,
            c
        )

        return
    }

    Column(
        Modifier.padding(
            horizontal =
                10.dp
        ),

        verticalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        songs
            .take(
                8
            )
            .forEach { song ->

                HomeSongRow(
                    song =
                        song,

                    c =
                        c,

                    play = {
                        play(
                            song
                        )
                    },

                    options = {
                        options(
                            song
                        )
                    }
                )
            }
    }
}

@Composable
private fun HomeSongRow(
    song: Song,
    c: HomeColors,
    play: () -> Unit,
    options: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(
                58.dp
            )
            .clip(
                RoundedCornerShape(
                    13.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    13.dp
                )
            )
            .combinedClickable(
                onClick =
                    play,

                onLongClick =
                    options
            )
            .padding(
                5.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model =
                song.artwork,

            contentDescription =
                null,

            modifier =
                Modifier
                    .size(
                        48.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            9.dp
                        )
                    )
                    .background(
                        c.button
                    ),

            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    horizontal =
                        10.dp
                )
        ) {
            Text(
                song.title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    11.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                song.artist,

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    9.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Box(
            Modifier
                .size(
                    36.dp
                )
                .clickable(
                    onClick =
                        options
                ),

            contentAlignment =
                Alignment.Center
        ) {
            XmoIcon(
                icon =
                    R.drawable.ic_xmo_more,

                tint =
                    c.sub,

                modifier =
                    Modifier.size(
                        15.dp
                    )
            )
        }
    }
}

/*
 * =============================================================
 * SONG OPTIONS
 * =============================================================
 */

@OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class
)
@Composable
private fun HomeSongOptions(
    song: Song,
    liked: Boolean,
    categories: List<UserCategory>,
    c: HomeColors,
    dismiss: () -> Unit,
    toggleLike: () -> Unit,
    setCategory: (
        String,
        Boolean
    ) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest =
            dismiss,

        containerColor =
            c.surface,

        contentColor =
            c.text
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    18.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AsyncImage(
                model =
                    song.artwork,

                contentDescription =
                    null,

                modifier =
                    Modifier
                        .size(
                            58.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(
                            c.button
                        ),

                contentScale =
                    ContentScale.Crop
            )

            Column(
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            12.dp
                    )
            ) {
                Text(
                    song.title,

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.bold,

                    fontSize =
                        14.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    "${song.artist} • ${song.album}",

                    color =
                        c.sub,

                    fontFamily =
                        XmoFont.thin,

                    fontSize =
                        10.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }

        HomeOption(
            title =
                if (
                    liked
                ) {
                    "Remove from Liked Songs"
                } else {
                    "Add to Liked Songs"
                },

            active =
                liked,

            icon =
                R.drawable.ic_xmo_heart,

            c =
                c,

            click =
                toggleLike
        )

        if (
            categories.isNotEmpty()
        ) {
            Text(
                "CATEGORIES",

                color =
                    LocalXmoAccent.current,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    10.sp,

                letterSpacing =
                    1.sp,

                modifier =
                    Modifier.padding(
                        start = 22.dp,
                        top = 14.dp,
                        bottom = 5.dp
                    )
            )

            categories.forEach { category ->
                val added =
                    song.id in
                        category.songIds

                HomeOption(
                    title =
                        category.name,

                    active =
                        added,

                    icon =
                        R.drawable.ic_xmo_add,

                    c =
                        c
                ) {
                    setCategory(
                        category.id,
                        !added
                    )
                }
            }
        }

        Spacer(
            Modifier.height(
                32.dp
            )
        )
    }
}

@Composable
private fun HomeOption(
    title: String,
    active: Boolean,
    icon: Int,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 20.dp,
                vertical = 11.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoIcon(
            icon =
                icon,

            tint =
                if (
                    active
                ) {
                    LocalXmoAccent.current
                } else {
                    c.icon
                },

            modifier =
                Modifier.size(
                    18.dp
                )
        )

        Text(
            title,

            color =
                if (
                    active
                ) {
                    LocalXmoAccent.current
                } else {
                    c.text
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                12.sp,

            modifier =
                Modifier.padding(
                    start =
                        14.dp
                )
        )
    }
}

/*
 * =============================================================
 * FULL SONG LIST
 * =============================================================
 */

@Composable
private fun HomeFullSongList(
    model: HomeLayer.SongList,
    c: HomeColors,
    close: () -> Unit,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit,
    options: (Song) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(
                500f
            )
            .background(
                c.bg
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        66.dp
                    )
                    .padding(
                        horizontal =
                            14.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(
                            38.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            c.button
                        )
                        .clickable(
                            onClick =
                                close
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "‹",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            28.sp
                    )
                }

                Column(
                    Modifier.padding(
                        start =
                            12.dp
                    )
                ) {
                    Text(
                        model.title,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            18.sp
                    )

                    Text(
                        "${model.songs.size} songs",

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.thin,

                        fontSize =
                            9.sp
                    )
                }
            }

            LazyColumn(
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 190.dp
                    ),

                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                items(
                    items =
                        model.songs,

                    key = {
                        it.id
                    }
                ) { song ->

                    HomeSongRow(
                        song =
                            song,

                        c =
                            c,

                        play = {
                            play(
                                song,
                                model.source,
                                model.category,
                                model.songs
                            )
                        },

                        options = {
                            options(
                                song
                            )
                        }
                    )
                }
            }
        }
    }
}

/*
 * =============================================================
 * MENU / SCANNER
 * =============================================================
 */

@Composable
private fun HomeMenuDialog(
    c: HomeColors,
    dismiss: () -> Unit,
    allSongs: () -> Unit,
    liked: () -> Unit,
    scanner: () -> Unit
) {
    HomeDialog(
        title =
            "XMO",

        c =
            c,

        dismiss =
            dismiss
    ) {
        HomeMenuItem(
            "All Songs",
            R.drawable.ic_xmo_songs,
            c,
            allSongs
        )

        HomeMenuItem(
            "Liked Songs",
            R.drawable.ic_xmo_heart,
            c,
            liked
        )

        HomeMenuItem(
            "Scan Music",
            R.drawable.ic_xmo_refresh,
            c,
            scanner
        )
    }
}

@Composable
private fun HomeScannerDialog(
    c: HomeColors,
    scanning: Boolean,
    songCount: Int,
    scan: () -> Unit,
    dismiss: () -> Unit
) {
    HomeDialog(
        title =
            if (
                scanning
            ) {
                "Scanning music…"
            } else {
                "Scan local music"
            },

        c =
            c,

        dismiss =
            dismiss
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    80.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {
            XmoIcon(
                icon =
                    R.drawable.ic_xmo_refresh,

                tint =
                    LocalXmoAccent.current,

                modifier =
                    Modifier.size(
                        32.dp
                    )
            )
        }

        Text(
            if (
                scanning
            ) {
                "Reading Android MediaStore and local audio metadata…"
            } else {
                "$songCount songs currently available."
            },

            color =
                c.sub,

            fontFamily =
                XmoFont.normal,

            fontSize =
                11.sp
        )

        Spacer(
            Modifier.height(
                14.dp
            )
        )

        HomeDialogAction(
            text =
                if (
                    scanning
                ) {
                    "Scanning…"
                } else {
                    "Scan Now"
                },

            enabled =
                !scanning,

            click =
                scan
        )
    }
}

@Composable
private fun HomeMenuItem(
    title: String,
    icon: Int,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    13.dp
                )
            )
            .clickable(
                onClick =
                    click
            )
            .padding(
                12.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoIcon(
            icon =
                icon,

            tint =
                LocalXmoAccent.current,

            modifier =
                Modifier.size(
                    18.dp
                )
        )

        Text(
            title,

            color =
                c.text,

            fontFamily =
                XmoFont.medium,

            fontSize =
                12.sp,

            modifier =
                Modifier.padding(
                    start =
                        13.dp
                )
        )
    }
}

/*
 * =============================================================
 * DIALOG
 * =============================================================
 */

@Composable
private fun HomeDialog(
    title: String,
    c: HomeColors,
    dismiss: () -> Unit,
    content:
        @Composable ColumnScope.() -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(
                1000f
            )
            .background(
                Color.Black.copy(
                    alpha = .56f
                )
            )
            .clickable(
                onClick =
                    dismiss
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal =
                        24.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .background(
                    c.surface
                )
                .border(
                    .8.dp,
                    c.border,
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .clickable {}
                .padding(
                    18.dp
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom =
                            14.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    title,

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.bold,

                    fontSize =
                        17.sp,

                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                Box(
                    Modifier
                        .size(
                            30.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            c.button
                        )
                        .clickable(
                            onClick =
                                dismiss
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Close,

                        contentDescription =
                            "Close",

                        tint =
                            c.sub,

                        modifier =
                            Modifier.size(
                                15.dp
                            )
                    )
                }
            }

            content()
        }
    }
}

@Composable
private fun HomeDialogAction(
    text: String,
    enabled: Boolean = true,
    click: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(
                45.dp
            )
            .clip(
                RoundedCornerShape(
                    14.dp
                )
            )
            .background(
                LocalXmoAccent.current.copy(
                    alpha =
                        if (
                            enabled
                        ) {
                            1f
                        } else {
                            .25f
                        }
                )
            )
            .clickable(
                enabled =
                    enabled,

                onClick =
                    click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text,

            color =
                Color.White.copy(
                    alpha =
                        if (
                            enabled
                        ) {
                            1f
                        } else {
                            .45f
                        }
                ),

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
        )
    }
}

@Composable
private fun HomeCircleAction(
    icon: Int,
    click: () -> Unit
) {
    Box(
        Modifier
            .size(
                30.dp
            )
            .clip(
                CircleShape
            )
            .background(
                LocalXmoAccent.current.copy(
                    alpha = .16f
                )
            )
            .border(
                .6.dp,
                LocalXmoAccent.current.copy(
                    alpha = .32f
                ),
                CircleShape
            )
            .clickable(
                onClick =
                    click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        XmoIcon(
            icon =
                icon,

            tint =
                LocalXmoAccent.current,

            modifier =
                Modifier.size(
                    14.dp
                )
        )
    }
}

@Composable
private fun HomeEmpty(
    text: String,
    c: HomeColors
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(
                82.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text,

            color =
                c.sub,

            fontFamily =
                XmoFont.normal,

            fontSize =
                12.sp
        )
    }
}
