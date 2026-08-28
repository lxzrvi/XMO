package com.xmo.music.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs
import androidx.compose.foundation.layout.Column
import kotlinx.coroutines.isActive

private data class HSection(
    val id: String,
    val name: String,
    val icon: Int,
    val tint: Color? = null
)

private sealed interface HomeOverlay {
    data object Menu : HomeOverlay
    data object Scanner : HomeOverlay

    data class Songs(
        val title: String,
        val songs: List<Song>,
        val source: String,
        val category: Boolean
    ) : HomeOverlay

    data class AlbumSongs(
        val album: Album
    ) : HomeOverlay
}

@OptIn(ExperimentalFoundationApi::class)
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

    val state =
        rememberLazyListState()

    val scope =
        rememberCoroutineScope()

    val customIcons =
        remember {
            listOf(
                R.drawable.ic_xmo_star,
                R.drawable.ic_xmo_spark,
                R.drawable.ic_xmo_diamond,
                R.drawable.ic_xmo_bolt
            )
        }

    val customTints =
        remember {
            listOf(
                Color(0xFFFFC107),
                Color(0xFFAF52DE),
                Color(0xFF00AEEF),
                Color(0xFFFF7043)
            )
        }

    val base =
        remember {
            listOf(
                HSection(
                    "songs",
                    "All Songs",
                    R.drawable.ic_xmo_songs
                ),
                HSection(
                    "albums",
                    "Albums",
                    R.drawable.ic_xmo_album
                ),
                HSection(
                    "liked",
                    "Liked Songs",
                    R.drawable.ic_xmo_heart
                ),
                HSection(
                    "artists",
                    "Artists",
                    R.drawable.ic_xmo_artist
                )
            )
        }

    val custom =
        remember(
            categories
        ) {
            categories.map {
                val i =
                    Math.floorMod(
                        it.icon,
                        customIcons.size
                    )

                HSection(
                    id =
                        it.id,

                    name =
                        it.name,

                    icon =
                        customIcons[i],

                    tint =
                        customTints[i]
                )
            }
        }

    val sectionMap =
        remember(
            base,
            custom
        ) {
            (
                base +
                    custom
                )
                .associateBy {
                    it.id
                }
        }

    val resolved =
        remember(
            order,
            sectionMap
        ) {
            (
                order.filter {
                    it in sectionMap
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
                resolved
            )
        }

    LaunchedEffect(
        resolved
    ) {
        if (
            currentOrder !=
            resolved
        ) {
            currentOrder =
                resolved
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
            val byId =
                songs.associateBy {
                    it.id
                }

            recentPlays
                .mapNotNull {
                    byId[
                        it.songId
                    ]
                }
                .take(12)
        }

    var selected by
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

    var addDialog by
        remember {
            mutableStateOf(
                false
            )
        }

    var categoryName by
        remember {
            mutableStateOf("")
        }

    var optionsSong by
        remember {
            mutableStateOf<Song?>(
                null
            )
        }

    var overlay by
        remember {
            mutableStateOf<HomeOverlay?>(
                null
            )
        }

    suspend fun openSection(
        id: String
    ) {
        if (
            id ==
            "all"
        ) {
            state.animateScrollToItem(
                0
            )

            return
        }

        val position =
            currentOrder
                .indexOf(
                    id
                )

        if (
            position < 0
        ) {
            return
        }

        val index =
            position +
                3

        state.animateScrollToItem(
            index =
                index,

            scrollOffset =
                -dockHeight
        )

        withFrameNanos { }

        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index ==
                    index
            }
            ?.let {
                val error =
                    it.offset -
                        dockHeight

                if (
                    abs(error) >
                    1
                ) {
                    state.scrollBy(
                        error.toFloat()
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
                state,

            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    ),

            contentPadding =
                PaddingValues(
                    bottom =
                        175.dp
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
                            .55.dp,
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
                            overlay =
                                HomeOverlay.Scanner
                        },

                        openMenu = {
                            overlay =
                                HomeOverlay.Menu
                        },

                        openProfile =
                            openProfile
                    )
                }
            }

            stickyHeader(
                key =
                    "categories"
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
                            .55.dp,
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
                    CategoryDragRow(
                        sections =
                            sectionMap,

                        order =
                            currentOrder,

                        selected =
                            selected,

                        c =
                            c,

                        select = {
                            selected =
                                it

                            scope.launch {
                                openSection(
                                    it
                                )
                            }
                        },

                        commit = {
                            currentOrder =
                                it

                            saveOrder(
                                it
                            )
                        },

                        add = {
                            addDialog =
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
                            bottom = 14.dp
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
                        EmptyHome(
                            "Nothing played yet",
                            c
                        )
                    } else {
                        RecentCarousel(
                            songs =
                                recentSongs,

                            c =
                                c,

                            theme =
                                theme,

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

                sectionMap[id]
                    ?.let { section ->

                        val category =
                            categories
                                .firstOrNull {
                                    it.id ==
                                        section.id
                                }

                        val categorySongs =
                            if (
                                category !=
                                null
                            ) {
                                songs.filter {
                                    it.id in
                                        category.songIds
                                }
                            } else {
                                emptyList()
                            }

                        HomeSection(
                            section =
                                section,

                            songs =
                                songs,

                            albums =
                                albums,

                            artistsCount =
                                artists.size,

                            likedSongs =
                                likedSongs,

                            categorySongs =
                                categorySongs,

                            allowed =
                                allowed,

                            c =
                                c,

                            theme =
                                theme,

                            onPlaySong =
                                onPlaySong,

                            options = {
                                optionsSong =
                                    it
                            },

                            openList = {
                                overlay =
                                    it
                            }
                        )
                    }
            }

            item(
                key =
                    "branding"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            360.dp
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
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
    }

    /*
     * =========================================================
     * ADD CATEGORY
     * =========================================================
     */

    if (
        addDialog
    ) {
        XmoDialogSurface(
            c =
                c,

            title =
                "New category",

            dismiss = {
                addDialog =
                    false

                categoryName =
                    ""
            }
        ) {
            BasicTextField(
                value =
                    categoryName,

                onValueChange = {
                    categoryName =
                        it.take(24)
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
                            .8.dp,
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
                        field ->

                    Box(
                        Modifier.fillMaxSize(),

                        contentAlignment =
                            Alignment.CenterStart
                    ) {
                        if (
                            categoryName.isBlank()
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

                        field()
                    }
                }
            )

            Spacer(
                Modifier.height(
                    15.dp
                )
            )

            Row(
                Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                DialogButton(
                    text =
                        "Cancel",

                    color =
                        c.button,

                    textColor =
                        c.text,

                    modifier =
                        Modifier.weight(1f)
                ) {
                    addDialog =
                        false

                    categoryName =
                        ""
                }

                DialogButton(
                    text =
                        "Add",

                    color =
                        accent,

                    textColor =
                        Color.White,

                    modifier =
                        Modifier.weight(1f),

                    enabled =
                        categoryName
                            .trim()
                            .isNotEmpty()
                ) {
                    val name =
                        categoryName
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

                        categoryName =
                            ""

                        addDialog =
                            false
                    }
                }
            }
        }
    }

    /*
     * =========================================================
     * SONG OPTIONS
     * =========================================================
     */

    optionsSong?.let {
        SongOptionsSheet(
            song =
                it,

            c =
                c,

            categories =
                categories,

            liked =
                it.id in
                    likedSongIds,

            dismiss = {
                optionsSong =
                    null
            },

            toggleLike = {
                toggleLike(
                    it
                )
            },

            setCategory = {
                    categoryId,
                    added ->

                setSongInCategory(
                    it,
                    categoryId,
                    added
                )
            }
        )
    }

    /*
     * =========================================================
     * OVERLAYS
     * =========================================================
     */

    overlay?.let {
        when (
            it
        ) {
            HomeOverlay.Menu -> {
                HomeMenu(
                    c =
                        c,

                    close = {
                        overlay =
                            null
                    },

                    scan = {
                        overlay =
                            HomeOverlay.Scanner
                    },

                    liked = {
                        overlay =
                            HomeOverlay.Songs(
                                title =
                                    "Liked Songs",

                                songs =
                                    likedSongs,

                                source =
                                    "Liked Songs",

                                category =
                                    false
                            )
                    },

                    all = {
                        overlay =
                            HomeOverlay.Songs(
                                title =
                                    "All Songs",

                                songs =
                                    songs,

                                source =
                                    "All Songs",

                                category =
                                    false
                            )
                    }
                )
            }

            HomeOverlay.Scanner -> {
                ScanDialog(
                    c =
                        c,

                    scanning =
                        scanning,

                    count =
                        songs.size,

                    close = {
                        if (
                            !scanning
                        ) {
                            overlay =
                                null
                        }
                    },

                    scan =
                        refresh
                )
            }

            is HomeOverlay.Songs -> {
                HomeSongListOverlay(
                    title =
                        it.title,

                    songs =
                        it.songs,

                    source =
                        it.source,

                    sourceIsCategory =
                        it.category,

                    c =
                        c,

                    close = {
                        overlay =
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

            is HomeOverlay.AlbumSongs -> {
                HomeSongListOverlay(
                    title =
                        it.album.name,

                    songs =
                        it.album.songs,

                    source =
                        it.album.name,

                    sourceIsCategory =
                        false,

                    c =
                        c,

                    close = {
                        overlay =
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
 * CATEGORY DRAG
 * =============================================================
 */

@Composable
private fun CategoryDragRow(
    sections: Map<String, HSection>,
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
            64.dp.toPx()
        }

    var preview by
        remember(
            order
        ) {
            mutableStateOf(
                order
            )
        }

    var draggedId by
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

    var autoScroll by
        remember {
            mutableStateOf<Job?>(
                null
            )
        }

    LaunchedEffect(
        order
    ) {
        if (
            draggedId ==
            null
        ) {
            preview =
                order
        }
    }

    fun itemInfo(
        id: String
    ): LazyListItemInfo? =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.key ==
                    "cat_$id"
            }

    fun stopAutoScroll() {
        autoScroll?.cancel()

        autoScroll =
            null
    }

    fun moveFromFinger(
        id: String
    ) {
        val from =
            preview.indexOf(
                id
            )

        if (
            from < 0
        ) {
            return
        }

        val visible =
            preview
                .mapIndexedNotNull {
                        index,
                        candidate ->

                    if (
                        candidate ==
                        id
                    ) {
                        null
                    } else {
                        itemInfo(
                            candidate
                        )?.let {
                            index to
                                (
                                    it.offset +
                                        it.size /
                                            2f
                                    )
                        }
                    }
                }

        var destination =
            from

        visible.forEach {
                (
                    index,
                    center
                ) ->

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

            modifier =
                Modifier.fillMaxSize(),

            userScrollEnabled =
                draggedId ==
                    null,

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
                    "__all__"
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
                        draggedId ==
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
                    "cat_$it"
                }
            ) { id ->

                val section =
                    sections[id]
                        ?: return@items

                val dragging =
                    draggedId ==
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
                                onDragStart = {
                                    val info =
                                        itemInfo(
                                            id
                                        )
                                            ?: return@detectDragGesturesAfterLongPress

                                    draggedId =
                                        id

                                    fingerX =
                                        info.offset +
                                            it.x

                                    grabX =
                                        it.x

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

                                    moveFromFinger(
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

                                    val leftDistance =
                                        (
                                            fingerX -
                                                start
                                            )
                                            .coerceAtLeast(
                                                0f
                                            )

                                    val rightDistance =
                                        (
                                            end -
                                                fingerX
                                            )
                                            .coerceAtLeast(
                                                0f
                                            )

                                    val left =
                                        leftDistance <
                                            edge &&
                                            state.canScrollBackward

                                    val right =
                                        rightDistance <
                                            edge &&
                                            state.canScrollForward

                                    if (
                                        left ||
                                        right
                                    ) {
                                        val distance =
                                            if (
                                                left
                                            ) {
                                                leftDistance
                                            } else {
                                                rightDistance
                                            }

                                        val strength =
                                            (
                                                1f -
                                                    distance /
                                                    edge
                                                )
                                                .coerceIn(
                                                    .15f,
                                                    1f
                                                )

                                        val direction =
                                            if (
                                                left
                                            ) {
                                                -1f
                                            } else {
                                                1f
                                            }

                                        stopAutoScroll()

                                        autoScroll =
                                            scope.launch {
                                                while (
                                                    isActive &&
                                                    draggedId ==
                                                    id
                                                ) {
                                                    val consumed =
                                                        state.scrollBy(
                                                            direction *
                                                                (
                                                                    5f +
                                                                        20f *
                                                                        strength
                                                                    )
                                                        )

                                                    moveFromFinger(
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
                                        stopAutoScroll()
                                    }
                                },

                                onDragEnd = {
                                    stopAutoScroll()

                                    val result =
                                        preview.toList()

                                    draggedId =
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
                                    stopAutoScroll()

                                    draggedId =
                                        null

                                    fingerX =
                                        0f

                                    grabX =
                                        0f

                                    preview =
                                        order
                                }
                            )
                        }
                ) {
                    CategoryChip(
                        text =
                            section.name,

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
                            draggedId ==
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
                    "__add__"
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
                        LocalXmoAccent.current
                ) {
                    if (
                        draggedId ==
                        null
                    ) {
                        add()
                    }
                }
            }
        }

        draggedId?.let { id ->

            val section =
                sections[id]
                    ?: return@let

            val scale by
                animateFloatAsState(
                    targetValue =
                        1.07f,

                    animationSpec =
                        spring(
                            dampingRatio =
                                .72f,

                            stiffness =
                                500f
                        ),

                    label =
                        "categoryLift"
                )

            Box(
                Modifier
                    .zIndex(
                        100f
                    )
                    .graphicsLayer {
                        translationX =
                            fingerX -
                                grabX

                        scaleX =
                            scale

                        scaleY =
                            scale

                        shadowElevation =
                            12f
                    }
            ) {
                CategoryChip(
                    text =
                        section.name,

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
 * HOME SECTIONS
 * =============================================================
 */

@Composable
private fun HomeSection(
    section: HSection,
    songs: List<Song>,
    albums: List<Album>,
    artistsCount: Int,
    likedSongs: List<Song>,
    categorySongs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    onPlaySong: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit,
    options: (Song) -> Unit,
    openList: (HomeOverlay) -> Unit
) {
    val arrow =
        if (
            section.id ==
            "songs"
        ) {
            remember {
                SongArrowController()
            }
        } else {
            null
        }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                vertical =
                    10.dp
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
                    section.name,

                subtitle =
                    when (
                        section.id
                    ) {
                        "songs" ->
                            "${songs.size} songs"

                        "albums" ->
                            "${albums.size} albums"

                        "liked" ->
                            "${likedSongs.size} favorites"

                        "artists" ->
                            "$artistsCount artists"

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
                section.id !=
                "artists"
            ) {
                HomeRoundAction(
                    icon =
                        R.drawable.ic_xmo_add
                ) {
                    when (
                        section.id
                    ) {
                        "songs" ->
                            openList(
                                HomeOverlay.Songs(
                                    "All Songs",
                                    songs,
                                    "All Songs",
                                    false
                                )
                            )

                        "albums" -> {
                            /*
                             * Album cards below open individual
                             * albums. The plus opens the library
                             * songs list; MediaStore albums are
                             * never mutated.
                             */
                            openList(
                                HomeOverlay.Songs(
                                    "Library",
                                    songs,
                                    "All Songs",
                                    false
                                )
                            )
                        }

                        "liked" ->
                            openList(
                                HomeOverlay.Songs(
                                    "Liked Songs",
                                    likedSongs,
                                    "Liked Songs",
                                    false
                                )
                            )

                        else ->
                            openList(
                                HomeOverlay.Songs(
                                    section.name,
                                    categorySongs,
                                    section.name,
                                    true
                                )
                            )
                    }
                }
            }

            arrow?.let {
                SongArrowButton(
                    it
                )
            }
        }

        Spacer(
            Modifier.height(
                5.dp
            )
        )

        when (
            section.id
        ) {
            "songs" -> {
                SongsGrid(
                    songs =
                        songs,

                    allowed =
                        allowed,

                    c =
                        c,

                    theme =
                        theme,

                    arrow =
                        arrow!!,

                    play = {
                        onPlaySong(
                            it,
                            "All Songs",
                            false,
                            songs
                        )
                    },

                    options =
                        options
                )
            }

            "albums" -> {
                AlbumsBody(
                    albums =
                        albums,

                    c =
                        c,

                    open =
                        openList
                )
            }

            "liked" -> {
                CompactSongRows(
                    songs =
                        likedSongs,

                    emptyText =
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

                    options =
                        options
                )
            }

            "artists" -> {
                ArtistBody(
                    songs =
                        songs,

                    c =
                        c
                )
            }

            else -> {
                CompactSongRows(
                    songs =
                        categorySongs,

                    emptyText =
                        "No songs in this category",

                    c =
                        c,

                    play = {
                        onPlaySong(
                            it,
                            section.name,
                            true,
                            categorySongs
                        )
                    },

                    options =
                        options
                )
            }
        }
    }
}

/*
 * =============================================================
 * RECENTS
 * =============================================================
 */

@Composable
private fun RecentCarousel(
    songs: List<Song>,
    c: HomeColors,
    theme: XmoTheme,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    val accent =
        LocalXmoAccent.current

    val list =
        rememberLazyListState()

    LazyRow(
        state =
            list,

        contentPadding =
            PaddingValues(
                horizontal =
                    22.dp
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

            BoxWithConstraints(
                Modifier
                    .width(
                        282.dp
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
                        Brush.horizontalGradient(
                            listOf(
                                c.surface,
                                accent.copy(
                                    alpha = .13f
                                )
                            )
                        )
                    )
                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(
                            18.dp
                        )
                    )
                    .padding(
                        7.dp
                    )
            ) {
                AsyncImage(
                    model =
                        song.artwork,

                    contentDescription =
                        null,

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    13.dp
                                )
                            )
                            .background(
                                c.button
                            ),

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
                                        alpha = .78f
                                    )
                                )
                            )
                        )
                )

                Text(
                    text =
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
                            .padding(
                                9.dp
                            )
                            .fillMaxWidth(
                                .76f
                            )
                )

                Box(
                    Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(
                            8.dp
                        )
                        .size(
                            32.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color.Black.copy(
                                alpha = .45f
                            )
                        )
                        .border(
                            .6.dp,
                            Color.White.copy(
                                alpha = .20f
                            ),
                            CircleShape
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
 * ALL SONGS GRID
 * =============================================================
 */

@Stable
private class SongArrowController {
    var tap by
        mutableIntStateOf(
            0
        )
        private set

    var fast by
        mutableStateOf(
            false
        )
        private set

    fun click() {
        tap++
    }

    fun start() {
        fast =
            true
    }

    fun stop() {
        fast =
            false
    }
}

@Composable
private fun SongArrowButton(
    controller: SongArrowController
) {
    val scope =
        rememberCoroutineScope()

    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .padding(
                start =
                    7.dp
            )
            .size(
                30.dp
            )
            .clip(
                CircleShape
            )
            .background(
                accent.copy(
                    alpha = .16f
                )
            )
            .border(
                .6.dp,
                accent.copy(
                    alpha = .28f
                ),
                CircleShape
            )
            .pointerInput(
                controller
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

                                controller.start()
                            }

                        val released =
                            tryAwaitRelease()

                        job.cancel()

                        controller.stop()

                        if (
                            released &&
                            !held
                        ) {
                            controller.click()
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
                accent,

            modifier =
                Modifier.size(
                    14.dp
                )
        )
    }
}

@Composable
private fun SongsGrid(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    arrow: SongArrowController,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (
        !allowed
    ) {
        EmptyHome(
            "Music access required",
            c
        )

        return
    }

    if (
        songs.isEmpty()
    ) {
        EmptyHome(
            "No local music found",
            c
        )

        return
    }

    val grid =
        rememberLazyGridState()

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
        arrow.tap
    ) {
        if (
            arrow.tap <=
            0
        ) {
            return@LaunchedEffect
        }

        val currentColumn =
            grid.firstVisibleItemIndex /
                3

        val lastColumn =
            slots /
                3 -
                1

        val next =
            (
                currentColumn +
                    1
                )
                .coerceAtMost(
                    lastColumn
                )

        if (
            next >
            currentColumn
        ) {
            grid.animateScrollToItem(
                next *
                    3
            )
        }
    }

    LaunchedEffect(
        arrow.fast
    ) {
        while (
            arrow.fast &&
            isActive
        ) {
            val consumed =
                grid.scrollBy(
                    19f
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

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge =
            8.dp

        val gap =
            8.dp

        val card =
            (
                maxWidth -
                    edge *
                    2 -
                    gap *
                    3
                ) /
                4

        val height =
            (
                card +
                    37.dp
                ) *
                3 +
                gap *
                2

        Box(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            c.bg,
                            c.surface.copy(
                                alpha = .68f
                            ),
                            c.bg
                        )
                    )
                )
        ) {
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
                            height
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
                        "slot_$it"
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

                    val source =
                        page *
                            12 +
                            row *
                            4 +
                            column

                    Box(
                        Modifier.width(
                            card
                        )
                    ) {
                        songs.getOrNull(
                            source
                        )?.let { song ->

                            SongTile(
                                song =
                                    song,

                                index =
                                    source,

                                c =
                                    c,

                                theme =
                                    theme,

                                modifier =
                                    Modifier.width(
                                        card
                                    ),

                                onClick = {
                                    play(
                                        song
                                    )
                                },

                                onOptions =
                                    options
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
 * =============================================================
 * ALBUMS / ARTISTS / COMPACT SONGS
 * =============================================================
 */

@Composable
private fun AlbumsBody(
    albums: List<Album>,
    c: HomeColors,
    open: (HomeOverlay) -> Unit
) {
    if (
        albums.isEmpty()
    ) {
        EmptyHome(
            "No albums found",
            c
        )

        return
    }

    Row(
        Modifier
            .fillMaxWidth()
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
            .take(20)
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
                                HomeOverlay.AlbumSongs(
                                    album
                                )
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
                            null,

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
                                top = 5.dp
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
private fun ArtistBody(
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
        EmptyHome(
            "No artists found",
            c
        )

        return
    }

    Row(
        Modifier
            .fillMaxWidth()
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
            .take(15)
            .forEach { artist ->

                Column(
                    Modifier.width(
                        72.dp
                    ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    AsyncImage(
                        model =
                            artist.artwork,

                        contentDescription =
                            null,

                        modifier =
                            Modifier
                                .size(
                                    66.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    LocalXmoAccent.current.copy(
                                        alpha = .13f
                                    )
                                ),

                        contentScale =
                            ContentScale.Crop
                    )

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

@Composable
private fun CompactSongRows(
    songs: List<Song>,
    emptyText: String,
    c: HomeColors,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (
        songs.isEmpty()
    ) {
        EmptyHome(
            emptyText,
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
            .take(8)
            .forEach { song ->

                CompactSongRow(
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
private fun CompactSongRow(
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
                .clip(
                    CircleShape
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
private fun SongOptionsSheet(
    song: Song,
    c: HomeColors,
    categories: List<UserCategory>,
    liked: Boolean,
    dismiss: () -> Unit,
    toggleLike: () -> Unit,
    setCategory: (
        String,
        Boolean
    ) -> Unit
) {
    val accent =
        LocalXmoAccent.current

    ModalBottomSheet(
        onDismissRequest =
            dismiss,

        containerColor =
            c.surface,

        contentColor =
            c.text,

        dragHandle = {
            Box(
                Modifier
                    .padding(
                        top = 10.dp,
                        bottom = 8.dp
                    )
                    .width(
                        42.dp
                    )
                    .height(
                        4.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            2.dp
                        )
                    )
                    .background(
                        c.sub.copy(
                            alpha = .45f
                        )
                    )
            )
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 8.dp
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

        OptionRow(
            title =
                if (
                    liked
                ) {
                    "Remove from Liked Songs"
                } else {
                    "Add to Liked Songs"
                },

            subtitle =
                "Keep this song in your favorites",

            icon =
                R.drawable.ic_xmo_heart,

            active =
                liked,

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
                    accent,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    10.sp,

                letterSpacing =
                    1.sp,

                modifier =
                    Modifier.padding(
                        start = 22.dp,
                        top = 15.dp,
                        bottom = 5.dp
                    )
            )

            categories.forEach { category ->

                val added =
                    song.id in
                        category.songIds

                OptionRow(
                    title =
                        category.name,

                    subtitle =
                        if (
                            added
                        ) {
                            "Added"
                        } else {
                            "Add song"
                        },

                    icon =
                        R.drawable.ic_xmo_add,

                    active =
                        added,

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
                28.dp
            )
        )
    }
}

@Composable
private fun OptionRow(
    title: String,
    subtitle: String,
    icon: Int,
    active: Boolean,
    c: HomeColors,
    click: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

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
        Box(
            Modifier
                .size(
                    38.dp
                )
                .clip(
                    RoundedCornerShape(
                        12.dp
                    )
                )
                .background(
                    if (
                        active
                    ) {
                        accent.copy(
                            alpha = .14f
                        )
                    } else {
                        c.button
                    }
                ),

            contentAlignment =
                Alignment.Center
        ) {
            XmoIcon(
                icon =
                    icon,

                tint =
                    if (
                        active
                    ) {
                        accent
                    } else {
                        c.icon
                    },

                modifier =
                    Modifier.size(
                        17.dp
                    )
            )
        }

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
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    12.sp
            )

            Text(
                subtitle,

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

/*
 * =============================================================
 * FULL SONG LIST
 * =============================================================
 */

@Composable
private fun HomeSongListOverlay(
    title: String,
    songs: List<Song>,
    source: String,
    sourceIsCategory: Boolean,
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
            .background(
                c.bg
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .zIndex(
                500f
            )
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            OverlayHeader(
                title =
                    title,

                subtitle =
                    "${songs.size} songs",

                c =
                    c,

                close =
                    close
            )

            if (
                songs.isEmpty()
            ) {
                EmptyHome(
                    "No songs here",
                    c
                )
            } else {
                LazyColumn(
                    contentPadding =
                        PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            bottom = 180.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            6.dp
                        )
                ) {
                    itemsIndexed(
                        items =
                            songs,

                        key = {
                                _,
                                song ->

                            song.id
                        }
                    ) {
                            _,
                            song ->

                        CompactSongRow(
                            song =
                                song,

                            c =
                                c,

                            play = {
                                play(
                                    song,
                                    source,
                                    sourceIsCategory,
                                    songs
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
}

/*
 * =============================================================
 * HAMBURGER MENU
 * =============================================================
 */

@Composable
private fun HomeMenu(
    c: HomeColors,
    close: () -> Unit,
    scan: () -> Unit,
    liked: () -> Unit,
    all: () -> Unit
) {
    XmoDialogSurface(
        c =
            c,

        title =
            "XMO",

        dismiss =
            close
    ) {
        MenuAction(
            "All Songs",
            "Browse the complete local library",
            R.drawable.ic_xmo_songs,
            c,
            all
        )

        MenuAction(
            "Liked Songs",
            "Open your favorites",
            R.drawable.ic_xmo_heart,
            c,
            liked
        )

        MenuAction(
            "Scan Music",
            "Refresh the MediaStore library",
            R.drawable.ic_xmo_refresh,
            c,
            scan
        )
    }
}

@Composable
private fun MenuAction(
    title: String,
    subtitle: String,
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
                vertical = 10.dp,
                horizontal = 6.dp
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
                    19.dp
                )
        )

        Column(
            Modifier.padding(
                start =
                    12.dp
            )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    12.sp
            )

            Text(
                subtitle,

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

/*
 * =============================================================
 * SCANNER
 * =============================================================
 */

@Composable
private fun ScanDialog(
    c: HomeColors,
    scanning: Boolean,
    count: Int,
    close: () -> Unit,
    scan: () -> Unit
) {
    val rotation by
        animateFloatAsState(
            targetValue =
                if (
                    scanning
                ) {
                    360f
                } else {
                    0f
                },

            animationSpec =
                spring(
                    dampingRatio =
                        .7f,

                    stiffness =
                        100f
                ),

            label =
                "scan"
        )

    XmoDialogSurface(
        c =
            c,

        title =
            if (
                scanning
            ) {
                "Scanning music…"
            } else {
                "Scan local music"
            },

        dismiss =
            close
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    88.dp
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
                    Modifier
                        .size(
                            34.dp
                        )
                        .graphicsLayer {
                            rotationZ =
                                rotation
                        }
            )
        }

        Text(
            if (
                scanning
            ) {
                "Reading Android MediaStore and refreshing local metadata."
            } else {
                "$count songs currently available. Scan again to detect library changes."
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

        DialogButton(
            text =
                if (
                    scanning
                ) {
                    "Scanning…"
                } else {
                    "Scan Now"
                },

            color =
                LocalXmoAccent.current,

            textColor =
                Color.White,

            modifier =
                Modifier.fillMaxWidth(),

            enabled =
                !scanning,

            click =
                scan
        )
    }
}

/*
 * =============================================================
 * COMMON UI
 * =============================================================
 */

@Composable
private fun HomeRoundAction(
    icon: Int,
    click: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .padding(
                start =
                    6.dp
            )
            .size(
                30.dp
            )
            .clip(
                CircleShape
            )
            .background(
                accent.copy(
                    alpha = .16f
                )
            )
            .border(
                .6.dp,
                accent.copy(
                    alpha = .28f
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
                accent,

            modifier =
                Modifier.size(
                    14.dp
                )
        )
    }
}

@Composable
private fun OverlayHeader(
    title: String,
    subtitle: String,
    c: HomeColors,
    close: () -> Unit
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
            Icon(
                imageVector =
                    Icons.Default.Close,

                contentDescription =
                    "Back",

                tint =
                    c.text,

                modifier =
                    Modifier.size(
                        18.dp
                    )
            )
        }

        Column(
            Modifier.padding(
                start =
                    12.dp
            )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    18.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                subtitle,

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

@Composable
private fun XmoDialogSurface(
    c: HomeColors,
    title: String,
    dismiss: () -> Unit,
    content:
        @Composable Column.() -> Unit
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
                .clickable(
                    onClick = {}
                )
                .padding(
                    18.dp
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        bottom =
                            15.dp
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
private fun DialogButton(
    text: String,
    color: Color,
    textColor: Color,
    modifier: Modifier,
    enabled: Boolean = true,
    click: () -> Unit
) {
    Box(
        modifier
            .height(
                45.dp
            )
            .clip(
                RoundedCornerShape(
                    14.dp
                )
            )
            .background(
                if (
                    enabled
                ) {
                    color
                } else {
                    color.copy(
                        alpha = .25f
                    )
                }
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
                if (
                    enabled
                ) {
                    textColor
                } else {
                    textColor.copy(
                        alpha = .45f
                    )
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
        )
    }
}

@Composable
private fun EmptyHome(
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
