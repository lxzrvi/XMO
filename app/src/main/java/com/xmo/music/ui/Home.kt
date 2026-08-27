package com.xmo.music.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

private data class HSection(
    val id: String,
    val name: String,
    val icon: Int,
    val tint: Color = XmoRed
)

private object Arrow {
    var tick by mutableIntStateOf(0)
}

@Composable
fun Home(
    songs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    order: List<String>,
    categories: List<UserCategory>,
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit,
    saveOrder: (List<String>) -> Unit,
    saveCategories: (List<UserCategory>) -> Unit
) {
    val c = homeColors(theme)
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    val customIcons = remember {
        listOf(
            R.drawable.ic_xmo_star,
            R.drawable.ic_xmo_spark,
            R.drawable.ic_xmo_diamond,
            R.drawable.ic_xmo_bolt
        )
    }

    val customTints = remember {
        listOf(
            Color(0xFFFFC107),
            Color(0xFFAF52DE),
            Color(0xFF00AEEF),
            Color(0xFFFF7043)
        )
    }

    val base = listOf(
        HSection("songs", "All Songs", R.drawable.ic_xmo_songs),
        HSection("albums", "Albums", R.drawable.ic_xmo_album),
        HSection("liked", "Liked Songs", R.drawable.ic_xmo_heart),
        HSection("artists", "Artists", R.drawable.ic_xmo_artist)
    )

    val custom = categories.map {
        val i = it.icon.mod(4)
        HSection(
            it.id,
            it.name,
            customIcons[i],
            customTints[i]
        )
    }

    val sectionMap = (base + custom).associateBy { it.id }

    val resolved =
        order.filter(sectionMap::containsKey) +
            sectionMap.keys.filterNot(order::contains)

    var visualOrder by remember {
        mutableStateOf(resolved)
    }

    LaunchedEffect(resolved) {
        visualOrder = resolved
    }

    var selected by remember {
        mutableStateOf("all")
    }

    var addDialog by remember {
        mutableStateOf(false)
    }

    var newName by remember {
        mutableStateOf("")
    }

    /*
     * Items:
     * 0 = top spacer/header area
     * 1 = recent
     * 2+ = actual sections
     * last = branding
     */
    val recentInfo by remember {
        derivedStateOf {
            state.layoutInfo.visibleItemsInfo
                .firstOrNull { it.key == "recent" }
        }
    }

    /*
     * Header only starts collapsing as Recently Played
     * approaches the top. It does not disappear as soon
     * as user starts scrolling.
     */
    val collapseTarget by remember {
        derivedStateOf {
            val recent = recentInfo ?: return@derivedStateOf 1f

            val bottom =
                recent.offset + recent.size

            val range =
                with(density) {
                    90.dp.roundToPx()
                }

            (
                1f -
                    ((range - bottom).toFloat() / range)
                ).coerceIn(0f, 1f)
        }
    }

    val profile by animateFloatAsState(
        collapseTarget,
        spring(
            dampingRatio = .88f,
            stiffness = 550f
        ),
        label = "profile"
    )

    /*
     * 56dp is the pinned category area under status bar.
     */
    val dockPx = with(density) {
        56.dp.roundToPx()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize()
        ) {
            /*
             * Space occupied by expanded top profile.
             * Dock itself is overlayed above the list.
             */
            item("top") {
                Spacer(
                    Modifier
                        .windowInsetsPadding(
                            WindowInsets.statusBars
                        )
                        .height(118.dp)
                )
            }

            item("recent") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 18.dp
                        )
                ) {
                    SectionTitle(
                        "Recently Played",
                        "0 tracks played",
                        R.drawable.ic_xmo_history,
                        c
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nothing played yet",
                            color = c.sub,
                            fontFamily = XmoFont.normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(
                items = visualOrder,
                key = { "sec_$it" }
            ) { id ->
                sectionMap[id]?.let { section ->
                    HomeSectionBlock(
                        section = section,
                        subtitle = when (id) {
                            "songs" ->
                                "All songs: ${songs.size}"

                            "albums" ->
                                "${Library.albums(songs).size} albums"

                            "liked" ->
                                "0 favorites"

                            else -> ""
                        },
                        c = c,
                        action = when (id) {
                            "albums", "liked" ->
                                R.drawable.ic_xmo_add

                            else -> null
                        },
                        arrow = id == "songs"
                    ) {
                        when (id) {
                            "songs" ->
                                SongsGrid(
                                    songs,
                                    allowed,
                                    c,
                                    theme
                                )

                            "albums" ->
                                AlbumBody(
                                    songs,
                                    c
                                )

                            "liked" ->
                                Empty(
                                    "No liked songs yet",
                                    c
                                )

                            "artists" ->
                                ArtistBody(
                                    songs,
                                    c
                                )

                            else -> {
                                val ids = categories
                                    .firstOrNull {
                                        it.id == id
                                    }
                                    ?.songIds
                                    ?: emptySet()

                                CustomBody(
                                    songs.filter {
                                        it.id in ids
                                    },
                                    c,
                                    theme
                                )
                            }
                        }
                    }
                }
            }

            /*
             * Exactly one full screen of branding/trailing
             * space. This lets even the final section reach
             * below the pinned category dock.
             */
            item("brand") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "XMO",
                            color = c.text,
                            fontFamily = XmoFont.logo,
                            fontSize = 18.sp
                        )

                        Text(
                            "lxzrvi  •  copyright © 2026",
                            color = c.sub,
                            fontFamily = XmoFont.thin,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }

        /*
         * Independent overlay dock.
         * Therefore LazyColumn/stickyHeader cannot clip
         * category dragging anymore.
         */
        HomeDock(
            profile = profile,
            sections = visualOrder.mapNotNull(sectionMap::get),
            order = visualOrder,
            selected = selected,
            c = c,
            theme = theme,
            setTheme = setTheme,
            refresh = refresh,

            select = { id ->
                selected = id

                scope.launch {
                    if (id == "all") {
                        state.animateScrollToItem(0)
                    } else {
                        val pos =
                            visualOrder.indexOf(id)

                        if (pos >= 0) {
                            /*
                             * top=0, recent=1, sections start 2.
                             * Negative offset keeps label below
                             * pinned category dock.
                             */
                            state.animateScrollToItem(
                                index = pos + 2,
                                scrollOffset = -dockPx
                            )
                        }
                    }
                }
            },

            preview = {
                visualOrder = it
            },

            commit = {
                visualOrder = it
                saveOrder(it)
            },

            add = {
                addDialog = true
            }
        )
    }

    if (addDialog) {
        AlertDialog(
            onDismissRequest = {
                addDialog = false
                newName = ""
            },
            containerColor = c.surface,

            title = {
                Text(
                    "New category",
                    color = c.text,
                    fontFamily = XmoFont.bold
                )
            },

            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = {
                        newName = it.take(24)
                    },
                    singleLine = true,
                    label = {
                        Text("Category name")
                    }
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val name =
                            newName.trim()

                        if (name.isNotEmpty()) {
                            val cat =
                                UserCategory(
                                    id =
                                        "cat_${UUID.randomUUID()}",
                                    name = name,
                                    icon =
                                        categories.size % 4
                                )

                            val next =
                                visualOrder + cat.id

                            saveCategories(
                                categories + cat
                            )

                            visualOrder = next
                            saveOrder(next)

                            newName = ""
                            addDialog = false
                        }
                    }
                ) {
                    Text(
                        "Add",
                        color = XmoRed
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        newName = ""
                        addDialog = false
                    }
                ) {
                    Text(
                        "Cancel",
                        color = c.sub
                    )
                }
            }
        )
    }
}

@Composable
private fun HomeDock(
    profile: Float,
    sections: List<HSection>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit,
    select: (String) -> Unit,
    preview: (List<String>) -> Unit,
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.bg.copy(.97f))
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .padding(
                top = 3.dp,
                bottom = 3.dp
            )
    ) {
        /*
         * Real continuous profile collapse rather than
         * visibility on/off.
         */
        Box(
            Modifier
                .fillMaxWidth()
                .height(68.dp * profile)
                .graphicsLayer {
                    alpha = profile
                    translationY =
                        -(18.dp.toPx()) *
                            (1f - profile)
                }
        ) {
            if (profile > .01f) {
                HomeHeader(
                    c,
                    theme,
                    setTheme,
                    refresh
                )
            }
        }

        Spacer(
            Modifier.height(
                6.dp * profile
            )
        )

        CategoryDragRow(
            sections,
            order,
            selected,
            c,
            select,
            preview,
            commit,
            add
        )
    }
}

@Composable
private fun CategoryDragRow(
    sections: List<HSection>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    select: (String) -> Unit,
    preview: (List<String>) -> Unit,
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    val haptic =
        LocalHapticFeedback.current

    val density =
        LocalDensity.current

    var working by remember(order) {
        mutableStateOf(order)
    }

    var dragged by remember {
        mutableStateOf<String?>(null)
    }

    var dx by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(order) {
        if (dragged == null) {
            working = order
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 14.dp,
                vertical = 5.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            "All",
            selected == "all",
            c,
            R.drawable.ic_xmo_all
        ) {
            select("all")
        }

        working.forEach { id ->
            val section =
                sections.firstOrNull {
                    it.id == id
                } ?: return@forEach

            val moving =
                dragged == id

            Box(
                Modifier
                    .graphicsLayer {
                        translationX =
                            if (moving)
                                dx
                            else 0f

                        scaleX =
                            if (moving)
                                1.08f
                            else 1f

                        scaleY =
                            if (moving)
                                1.08f
                            else 1f
                    }
                    .then(
                        if (moving) {
                            Modifier
                                .background(
                                    XmoRed.copy(.12f),
                                    RoundedCornerShape(18.dp)
                                )
                                .border(
                                    1.dp,
                                    XmoRed.copy(.65f),
                                    RoundedCornerShape(18.dp)
                                )
                        } else Modifier
                    )
                    .pointerInput(
                        id,
                        working
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragged = id
                                dx = 0f

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()
                                dx += amount.x

                                var from =
                                    working.indexOf(id)

                                if (from < 0) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                /*
                                 * 36dp threshold makes crossing
                                 * responsive. Every crossing changes
                                 * preview order immediately, so
                                 * neighboring chips open the slot.
                                 */
                                val threshold =
                                    with(density) {
                                        36.dp.toPx()
                                    }

                                while (
                                    dx > threshold &&
                                    from <
                                    working.lastIndex
                                ) {
                                    val next =
                                        working.toMutableList()

                                    next.add(
                                        from + 1,
                                        next.removeAt(from)
                                    )

                                    working = next
                                    preview(next)

                                    dx -= threshold
                                    from++
                                }

                                while (
                                    dx < -threshold &&
                                    from > 0
                                ) {
                                    val next =
                                        working.toMutableList()

                                    next.add(
                                        from - 1,
                                        next.removeAt(from)
                                    )

                                    working = next
                                    preview(next)

                                    dx += threshold
                                    from--
                                }
                            },

                            onDragEnd = {
                                val result =
                                    working

                                dragged = null
                                dx = 0f

                                commit(result)
                            },

                            onDragCancel = {
                                dragged = null
                                dx = 0f

                                working = order
                                preview(order)
                            }
                        )
                    }
            ) {
                CategoryChip(
                    section.name,
                    selected == id,
                    c,
                    section.icon,
                    section.tint
                ) {
                    if (
                        dragged == null
                    ) {
                        select(id)
                    }
                }
            }
        }

        CategoryChip(
            "Add",
            false,
            c,
            R.drawable.ic_xmo_add,
            XmoRed,
            onClick = add
        )
    }
}

@Composable
private fun HomeSectionBlock(
    section: HSection,
    subtitle: String,
    c: HomeColors,
    action: Int?,
    arrow: Boolean,
    body: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            SectionTitle(
                title = section.name,
                subtitle = subtitle,
                icon = section.icon,
                c = c,
                modifier =
                    Modifier.weight(1f)
            )

            action?.let {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            XmoRed.copy(.18f)
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    XmoIcon(
                        it,
                        XmoRed,
                        Modifier.size(14.dp)
                    )
                }
            }

            if (arrow) {
                SongArrowButton()
            }
        }

        Spacer(
            Modifier.height(5.dp)
        )

        body()
    }
}

@Composable
private fun SongArrowButton() {
    val scope =
        rememberCoroutineScope()

    Box(
        Modifier
            .padding(start = 7.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(
                XmoRed.copy(.18f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        var hold = false

                        val job =
                            scope.launch {
                                delay(250)

                                hold = true

                                while (hold) {
                                    Arrow.tick++
                                    delay(55)
                                }
                            }

                        val released =
                            tryAwaitRelease()

                        val wasHold = hold
                        hold = false
                        job.cancel()

                        if (
                            released &&
                            !wasHold
                        ) {
                            Arrow.tick++
                        }
                    }
                )
            },
        contentAlignment =
            Alignment.Center
    ) {
        XmoIcon(
            R.drawable.ic_xmo_arrow,
            XmoRed,
            Modifier.size(14.dp)
        )
    }
}

@Composable
private fun SongsGrid(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme
) {
    if (!allowed) {
        Empty(
            "Music access required",
            c
        )
        return
    }

    if (songs.isEmpty()) {
        Empty(
            "No local music found",
            c
        )
        return
    }

    val density =
        LocalDensity.current

    val state =
        rememberLazyGridState()

    val scope =
        rememberCoroutineScope()

    val tick =
        Arrow.tick

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val width =
            this.maxWidth

        val edge = 8.dp
        val gap = 8.dp

        /*
         * Four exact cards:
         * 8 edge + 4 cards + 3 gaps + 8 edge.
         */
        val card =
            (
                width -
                    edge * 2 -
                    gap * 3
                ) / 4

        /*
         * Song card includes artwork + text.
         */
        val cellHeight =
            card + 37.dp

        val gridHeight =
            cellHeight * 3 +
                gap * 2

        LaunchedEffect(tick) {
            if (tick > 0) {
                val next =
                    (
                        state.firstVisibleItemIndex +
                            3
                        ).coerceAtMost(
                        songs.lastIndex
                    )

                state.animateScrollToItem(
                    next
                )
            }
        }

        LazyHorizontalGrid(
            rows =
                GridCells.Fixed(3),
            state = state,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
            contentPadding =
                PaddingValues(
                    horizontal = edge,
                    vertical = 0.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(gap),
            verticalArrangement =
                Arrangement.spacedBy(gap)
        ) {
            items(
                count = songs.size,
                key = {
                    songs[it].id
                }
            ) { index ->
                /*
                 * LazyHorizontalGrid with 3 fixed rows
                 * fills column-major internally. To get:
                 *
                 * 1 2 3 4
                 * 5 6 7 8
                 * 9...
                 *
                 * map visible grid position to row-major
                 * 12-item pages.
                 */
                val page =
                    index / 12

                val inside =
                    index % 12

                val row =
                    inside % 3

                val column =
                    inside / 3

                val mapped =
                    page * 12 +
                        row * 4 +
                        column

                songs.getOrNull(mapped)?.let {
                    SongTile(
                        song = it,
                        index = mapped,
                        c = c,
                        theme = theme,
                        modifier =
                            Modifier.width(card)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlbumBody(
    songs: List<Song>,
    c: HomeColors
) {
    if (songs.isEmpty()) {
        Empty(
            "No albums found",
            c
        )
    } else {
        Spacer(
            Modifier.height(8.dp)
        )
    }
}

@Composable
private fun ArtistBody(
    songs: List<Song>,
    c: HomeColors
) {
    val artists =
        Library.artists(songs)

    if (artists.isEmpty()) {
        Empty(
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
                horizontal = 8.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        artists
            .take(15)
            .forEach { artist ->

                Column(
                    Modifier.width(66.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(62.dp)
                            .background(
                                XmoRed.copy(.16f),
                                CircleShape
                            ),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            artist.name
                                .firstOrNull()
                                ?.uppercase()
                                ?: "?",
                            color = XmoRed,
                            fontFamily =
                                XmoFont.bold,
                            fontSize = 17.sp
                        )
                    }

                    Text(
                        artist.name,
                        color = c.text,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 9.sp,
                        lineHeight = 10.sp,
                        maxLines = 2,
                        modifier =
                            Modifier.padding(
                                top = 5.dp
                            )
                    )
                }
            }
    }
}

@Composable
private fun CustomBody(
    songs: List<Song>,
    c: HomeColors,
    theme: XmoTheme
) {
    if (songs.isEmpty()) {
        Empty(
            "No songs in this category",
            c
        )
        return
    }

    Column(
        Modifier.padding(
            horizontal = 8.dp
        ),
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        songs
            .chunked(6)
            .forEachIndexed {
                    row,
                    items ->

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    repeat(6) { column ->
                        Box(
                            Modifier.weight(1f)
                        ) {
                            items
                                .getOrNull(column)
                                ?.let {
                                    SongTile(
                                        song = it,
                                        index =
                                            row * 6 +
                                                column,
                                        c = c,
                                        theme = theme,
                                        modifier =
                                            Modifier.fillMaxWidth()
                                    )
                                }
                        }
                    }
                }
            }
    }
}

@Composable
private fun Empty(
    text: String,
    c: HomeColors
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(82.dp),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text,
            color = c.sub,
            fontFamily =
                XmoFont.normal,
            fontSize = 12.sp
        )
    }
}
