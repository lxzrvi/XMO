package com.xmo.music.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

private data class HSection(
    val id: String,
    val name: String,
    val icon: Int,
    val tint: Color = XmoRed
)

@OptIn(ExperimentalFoundationApi::class)
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
    saveCategories: (List<UserCategory>) -> Unit,

    /*
     * App.kt will use this to:
     * 1. start Media3 queue
     * 2. remember source
     * 3. open NowPlaying
     */
    onPlaySong: (
        song: Song,
        source: String,
        isCategory: Boolean,
        queue: List<Song>
    ) -> Unit = { _, _, _, _ -> }
) {
    val c = homeColors(theme)
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

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

    val base = remember {
        listOf(
            HSection("songs", "All Songs", R.drawable.ic_xmo_songs),
            HSection("albums", "Albums", R.drawable.ic_xmo_album),
            HSection("liked", "Liked Songs", R.drawable.ic_xmo_heart),
            HSection("artists", "Artists", R.drawable.ic_xmo_artist)
        )
    }

    val custom = remember(categories) {
        categories.map {
            val i = it.icon.mod(customIcons.size)

            HSection(
                id = it.id,
                name = it.name,
                icon = customIcons[i],
                tint = customTints[i]
            )
        }
    }

    val sectionMap = remember(base, custom) {
        (base + custom).associateBy { it.id }
    }

    val resolved = remember(order, sectionMap) {
        order.filter(sectionMap::containsKey) +
            sectionMap.keys.filterNot(order::contains)
    }

    var currentOrder by remember {
        mutableStateOf(resolved)
    }

    LaunchedEffect(resolved) {
        if (currentOrder != resolved) {
            currentOrder = resolved
        }
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

    var dockHeight by remember {
        mutableIntStateOf(0)
    }

    val albumCount = remember(songs) {
        Library.albums(songs).size
    }

    suspend fun openSection(id: String) {
        if (id == "all") {
            state.animateScrollToItem(0)
            return
        }

        val p = currentOrder.indexOf(id)

        if (p < 0) return

        val index = p + 3

        state.animateScrollToItem(
            index = index,
            scrollOffset = -dockHeight
        )

        withFrameNanos { }

        state.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == index }
            ?.let {
                val error = it.offset - dockHeight

                if (abs(error) > 1) {
                    state.scrollBy(error.toFloat())
                }
            }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            item("header") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg)
                        .padding(vertical = 4.dp)
                ) {
                    HomeHeader(
                        c = c,
                        theme = theme,
                        setTheme = setTheme,
                        refresh = refresh
                    )
                }
            }

            stickyHeader("categories") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg.copy(.99f))
                        .onSizeChanged {
                            dockHeight = it.height
                        }
                ) {
                    CategoryDragRow(
                        sections = sectionMap,
                        order = currentOrder,
                        selected = selected,
                        c = c,

                        select = { id ->
                            selected = id

                            scope.launch {
                                openSection(id)
                            }
                        },

                        commit = { next ->
                            if (next != currentOrder) {
                                currentOrder = next
                                saveOrder(next)
                            }
                        },

                        add = {
                            addDialog = true
                        }
                    )
                }
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
                        title = "Recently Played",
                        subtitle = "0 tracks played",
                        icon = R.drawable.ic_xmo_history,
                        c = c
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
                items = currentOrder,
                key = { "section_$it" }
            ) { id ->

                sectionMap[id]?.let { section ->
                    HomeSection(
                        section = section,
                        songs = songs,
                        allowed = allowed,
                        categories = categories,
                        albumCount = albumCount,
                        c = c,
                        theme = theme,
                        onPlaySong = onPlaySong
                    )
                }
            }

            item("branding") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment.CenterHorizontally
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
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
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
                        val name = newName.trim()

                        if (name.isNotEmpty()) {
                            val cat = UserCategory(
                                id = "cat_${UUID.randomUUID()}",
                                name = name,
                                icon = categories.size % 4
                            )

                            val nextOrder =
                                currentOrder + cat.id

                            /*
                             * Local order first:
                             * chip appears immediately.
                             */
                            currentOrder = nextOrder

                            saveCategories(
                                categories + cat
                            )

                            saveOrder(
                                nextOrder
                            )

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
                        addDialog = false
                        newName = ""
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

/*
 * =============================================================
 * CATEGORY REORDER
 *
 * Important design:
 *
 * The dragged category DOES NOT move around as a LazyRow item.
 * Its original LazyRow item becomes an invisible placeholder.
 *
 * A copy is rendered above LazyRow and follows the finger.
 * Therefore pointer ownership cannot jump to another chip.
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
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val edge = with(density) {
        68.dp.toPx()
    }

    val scrollStep = with(density) {
        11.dp.toPx()
    }

    var preview by remember(order) {
        mutableStateOf(order)
    }

    var draggedId by remember {
        mutableStateOf<String?>(null)
    }

    /*
     * Finger position in LazyRow viewport coordinates.
     */
    var fingerX by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Distance between finger and dragged chip's left edge.
     */
    var grabX by remember {
        mutableFloatStateOf(0f)
    }

    var draggedSize by remember {
        mutableStateOf(IntSize.Zero)
    }

    var autoJob by remember {
        mutableStateOf<Job?>(null)
    }

    LaunchedEffect(order) {
        if (draggedId == null) {
            preview = order
        }
    }

    fun info(id: String): LazyListItemInfo? =
        state.layoutInfo.visibleItemsInfo
            .firstOrNull {
                it.key == "cat_$id"
            }

    fun stopAuto() {
        autoJob?.cancel()
        autoJob = null
    }

    /*
     * Determine destination from the finger position.
     *
     * Same algorithm for both directions.
     */
    fun updateDestination(id: String) {
        val from = preview.indexOf(id)

        if (from < 0) return

        /*
         * Search all currently visible reorderable categories.
         */
        val candidates =
            preview.mapIndexedNotNull { index, otherId ->
                if (otherId == id) {
                    null
                } else {
                    info(otherId)?.let {
                        Triple(
                            index,
                            otherId,
                            it.offset + it.size / 2f
                        )
                    }
                }
            }

        if (candidates.isEmpty()) return

        var target = from

        /*
         * Left/right are symmetrical:
         * where is finger relative to real item centers?
         */
        for ((index, _, center) in candidates) {
            if (
                index < from &&
                fingerX < center
            ) {
                target = index
                break
            }

            if (
                index > from &&
                fingerX > center
            ) {
                target = index
            }
        }

        if (target == from) return

        val next = preview.toMutableList()
        val moving = next.removeAt(from)

        /*
         * removeAt shifts the indexes when travelling right.
         */
        val insertion =
            if (target > from) {
                target
            } else {
                target
            }

        next.add(
            insertion.coerceIn(
                0,
                next.size
            ),
            moving
        )

        preview = next
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        LazyRow(
            state = state,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled =
                draggedId == null,
            contentPadding =
                PaddingValues(
                    horizontal = 14.dp,
                    vertical = 5.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            item("__all__") {
                CategoryChip(
                    text = "All",
                    active = selected == "all",
                    c = c,
                    icon = R.drawable.ic_xmo_all
                ) {
                    if (draggedId == null) {
                        select("all")
                    }
                }
            }

            items(
                items = preview,
                key = { "cat_$it" }
            ) { id ->

                val section =
                    sections[id]
                        ?: return@items

                val dragging =
                    id == draggedId

                /*
                 * This remains the real slot/placeholder.
                 * Neighbours therefore always have physical room.
                 */
                Box(
                    Modifier
                        .onSizeChanged {
                            if (!dragging) {
                                /*
                                 * Size available for drag start.
                                 */
                            }
                        }
                        .graphicsLayer {
                            alpha =
                                if (dragging)
                                    0f
                                else
                                    1f
                        }
                        .pointerInput(id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { local ->

                                    val item =
                                        info(id)
                                            ?: return@detectDragGesturesAfterLongPress

                                    draggedId = id

                                    draggedSize =
                                        IntSize(
                                            item.size,
                                            size.height
                                        )

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

                                    fingerX += amount.x

                                    updateDestination(id)

                                    val start =
                                        state.layoutInfo
                                            .viewportStartOffset
                                            .toFloat()

                                    val end =
                                        state.layoutInfo
                                            .viewportEndOffset
                                            .toFloat()

                                    val left =
                                        fingerX <
                                            start + edge &&
                                            state.canScrollBackward

                                    val right =
                                        fingerX >
                                            end - edge &&
                                            state.canScrollForward

                                    if (left || right) {
                                        val direction =
                                            if (left)
                                                -1f
                                            else
                                                1f

                                        if (
                                            autoJob?.isActive !=
                                            true
                                        ) {
                                            autoJob =
                                                scope.launch {
                                                    while (
                                                        isActive &&
                                                        draggedId == id
                                                    ) {
                                                        val consumed =
                                                            state.scrollBy(
                                                                scrollStep *
                                                                    direction
                                                            )

                                                        /*
                                                         * Finger is fixed.
                                                         * Row content moves below it.
                                                         */
                                                        updateDestination(id)

                                                        if (
                                                            abs(consumed) <
                                                            .1f
                                                        ) {
                                                            break
                                                        }

                                                        delay(16)
                                                    }
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

                                    draggedId = null
                                    fingerX = 0f
                                    grabX = 0f

                                    commit(result)
                                },

                                onDragCancel = {
                                    stopAuto()

                                    draggedId = null
                                    fingerX = 0f
                                    grabX = 0f

                                    preview = order
                                }
                            )
                        }
                ) {
                    CategoryChip(
                        text = section.name,
                        active = selected == id,
                        c = c,
                        icon = section.icon,
                        tint = section.tint
                    ) {
                        if (draggedId == null) {
                            select(id)
                        }
                    }
                }
            }

            item("__add__") {
                CategoryChip(
                    text = "Add",
                    active = false,
                    c = c,
                    icon = R.drawable.ic_xmo_add,
                    tint = XmoRed
                ) {
                    if (draggedId == null) {
                        add()
                    }
                }
            }
        }

        /*
         * -----------------------------------------------------
         * DRAG OVERLAY
         * -----------------------------------------------------
         *
         * Pointer continues to belong to placeholder.
         * This visual copy never participates in LazyRow layout.
         */
        draggedId?.let { id ->

            val section =
                sections[id]
                    ?: return@let

            /*
             * Clamp visual overlay so it can approach screen edges
             * without disappearing outside parent.
             */
            val x =
                fingerX -
                    grabX

            Box(
                Modifier
                    .zIndex(1000f)
                    .graphicsLayer {
                        translationX = x

                        scaleX = 1.09f
                        scaleY = 1.09f
                    }
            ) {
                CategoryChip(
                    text = section.name,
                    active = selected == id,
                    c = c,
                    icon = section.icon,
                    tint = section.tint,
                    modifier =
                        Modifier.border(
                            1.dp,
                            XmoRed.copy(.72f),
                            RoundedCornerShape(18.dp)
                        )
                ) {}
            }
        }
    }
}

/*
 * =============================================================
 * SONG ARROW
 * =============================================================
 */

@Stable
private class SongArrowController {
    var tap by mutableIntStateOf(0)
        private set

    var fast by mutableStateOf(false)
        private set

    fun click() {
        tap++
    }

    fun start() {
        fast = true
    }

    fun stop() {
        fast = false
    }
}

@Composable
private fun SongArrowButton(
    controller: SongArrowController
) {
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .padding(start = 7.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(XmoRed.copy(.18f))
            .pointerInput(controller) {
                detectTapGestures(
                    onPress = {
                        var held = false

                        val job =
                            scope.launch {
                                delay(250)
                                held = true
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
        contentAlignment = Alignment.Center
    ) {
        XmoIcon(
            R.drawable.ic_xmo_arrow,
            XmoRed,
            Modifier.size(14.dp)
        )
    }
}

/*
 * =============================================================
 * HOME SECTION
 * =============================================================
 */

@Composable
private fun HomeSection(
    section: HSection,
    songs: List<Song>,
    allowed: Boolean,
    categories: List<UserCategory>,
    albumCount: Int,
    c: HomeColors,
    theme: XmoTheme,
    onPlaySong: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit
) {
    val arrow =
        if (section.id == "songs") {
            remember {
                SongArrowController()
            }
        } else {
            null
        }

    val customCategory =
        remember(
            section.id,
            categories
        ) {
            categories.firstOrNull {
                it.id == section.id
            }
        }

    val categorySongs =
        remember(
            customCategory,
            songs
        ) {
            customCategory?.let { category ->
                songs.filter {
                    it.id in
                        category.songIds
                }
            }.orEmpty()
        }

    val showAdd =
        section.id == "albums" ||
            section.id == "liked" ||
            customCategory != null

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

                subtitle =
                    when (section.id) {
                        "songs" ->
                            "All songs: ${songs.size}"

                        "albums" ->
                            "$albumCount albums"

                        "liked" ->
                            "0 favorites"

                        else ->
                            ""
                    },

                icon = section.icon,
                c = c,
                modifier = Modifier.weight(1f)
            )

            if (showAdd) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(XmoRed.copy(.18f)),
                    contentAlignment =
                        Alignment.Center
                ) {
                    XmoIcon(
                        R.drawable.ic_xmo_add,
                        XmoRed,
                        Modifier.size(14.dp)
                    )
                }
            }

            arrow?.let {
                SongArrowButton(it)
            }
        }

        Spacer(
            Modifier.height(5.dp)
        )

        when (section.id) {
            "songs" -> {
                SongsGrid(
                    songs = songs,
                    allowed = allowed,
                    c = c,
                    theme = theme,
                    arrow = arrow!!,
                    onSong = {
                        onPlaySong(
                            it,
                            "All Songs",
                            false,
                            songs
                        )
                    }
                )
            }

            "albums" -> {
                AlbumBody(
                    songs,
                    c
                )
            }

            "liked" -> {
                Empty(
                    "No liked songs yet",
                    c
                )
            }

            "artists" -> {
                ArtistBody(
                    songs,
                    c
                )
            }

            else -> {
                CustomBody(
                    songs = categorySongs,
                    c = c,
                    theme = theme,
                    onSong = {
                        onPlaySong(
                            it,
                            section.name,
                            true,
                            categorySongs
                        )
                    }
                )
            }
        }
    }
}

/*
 * =============================================================
 * ALL SONGS
 * =============================================================
 */

@Composable
private fun SongsGrid(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    arrow: SongArrowController,
    onSong: (Song) -> Unit
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

    val grid =
        rememberLazyGridState()

    val tapRequest =
        arrow.tap

    val fast =
        arrow.fast

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge = 8.dp
        val gap = 8.dp

        val card =
            (
                maxWidth -
                    edge * 2 -
                    gap * 3
                ) / 4

        val height =
            (card + 37.dp) * 3 +
                gap * 2

        val slots =
            ((songs.size + 11) / 12) *
                12

        /*
         * Tap -> one column.
         */
        LaunchedEffect(tapRequest) {
            if (
                tapRequest <= 0 ||
                slots <= 0
            ) {
                return@LaunchedEffect
            }

            val column =
                grid.firstVisibleItemIndex /
                    3

            val max =
                slots / 3 - 1

            val target =
                (column + 1)
                    .coerceAtMost(max)

            if (target > column) {
                grid.animateScrollToItem(
                    target * 3
                )
            }
        }

        /*
         * Hold -> continuous fast scroll.
         */
        LaunchedEffect(fast) {
            if (!fast) {
                return@LaunchedEffect
            }

            while (
                isActive &&
                arrow.fast
            ) {
                if (
                    abs(
                        grid.scrollBy(18f)
                    ) < .1f
                ) {
                    break
                }

                delay(16)
            }
        }

        LazyHorizontalGrid(
            rows =
                GridCells.Fixed(3),
            state = grid,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height),
            contentPadding =
                PaddingValues(
                    horizontal = edge
                ),
            horizontalArrangement =
                Arrangement.spacedBy(gap),
            verticalArrangement =
                Arrangement.spacedBy(gap)
        ) {
            items(
                count = slots,
                key = {
                    "song_$it"
                }
            ) { slot ->

                /*
                 * Visual:
                 *
                 * 1  2  3  4
                 * 5  6  7  8
                 * 9 10 11 12
                 */
                val page = slot / 12
                val local = slot % 12
                val row = local % 3
                val column = local / 3

                val source =
                    page * 12 +
                        row * 4 +
                        column

                Box(
                    Modifier.width(card)
                ) {
                    songs
                        .getOrNull(source)
                        ?.let { song ->

                            SongTile(
                                song = song,
                                index = source,
                                c = c,
                                theme = theme,
                                modifier =
                                    Modifier.width(card),

                                /*
                                 * Requires the updated SongTile
                                 * onClick parameter.
                                 */
                                onClick = {
                                    onSong(song)
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
 * OTHER SECTIONS
 * =============================================================
 */

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
        remember(songs) {
            Library.artists(songs)
        }

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
            .padding(horizontal = 8.dp),
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
                            fontFamily = XmoFont.bold,
                            fontSize = 17.sp
                        )
                    }

                    Text(
                        artist.name,
                        color = c.text,
                        fontFamily = XmoFont.medium,
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
    theme: XmoTheme,
    onSong: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Empty(
            "No songs in this category",
            c
        )
        return
    }

    /*
     * Custom categories are expected to be comparatively small.
     * Preserve current six-column layout.
     */
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
                    rowSongs ->

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(5.dp)
                ) {
                    repeat(6) { column ->

                        Box(
                            Modifier.weight(1f)
                        ) {
                            rowSongs
                                .getOrNull(column)
                                ?.let { song ->

                                    SongTile(
                                        song = song,
                                        index =
                                            row * 6 +
                                                column,
                                        c = c,
                                        theme = theme,
                                        modifier =
                                            Modifier.fillMaxWidth(),
                                        onClick = {
                                            onSong(song)
                                        }
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
            fontFamily = XmoFont.normal,
            fontSize = 12.sp
        )
    }
}
