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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

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
    saveCategories: (List<UserCategory>) -> Unit
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

    val custom = remember(categories) {
        categories.map { category ->
            val iconIndex =
                category.icon.mod(4)

            HSection(
                id = category.id,
                name = category.name,
                icon = customIcons[iconIndex],
                tint = customTints[iconIndex]
            )
        }
    }

    val sectionMap = remember(
        base,
        custom
    ) {
        (base + custom)
            .associateBy { it.id }
    }

    val resolved = remember(
        order,
        sectionMap
    ) {
        order.filter {
            sectionMap.containsKey(it)
        } +
            sectionMap.keys.filterNot {
                order.contains(it)
            }
    }

    var currentOrder by remember {
        mutableStateOf(resolved)
    }

    LaunchedEffect(resolved) {
        if (
            currentOrder != resolved
        ) {
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

    /*
     * Actual measured category row height.
     *
     * Status bar is NOT included because LazyColumn's
     * viewport itself starts below the status bar.
     */
    var categoryHeightPx by remember {
        mutableIntStateOf(0)
    }

    val albumCount = remember(songs) {
        Library.albums(songs).size
    }

    /*
     * LazyColumn:
     *
     * 0 Header
     * 1 sticky category
     * 2 Recently Played
     * 3 first reorderable section
     */
    suspend fun openSection(
        id: String
    ) {
        if (id == "all") {
            state.animateScrollToItem(0)
            return
        }

        val orderIndex =
            currentOrder.indexOf(id)

        if (orderIndex < 0) {
            return
        }

        val lazyIndex =
            orderIndex + 3

        /*
         * IMPORTANT:
         *
         * NEGATIVE scrollOffset means the item's top is left
         * categoryHeightPx BELOW the LazyColumn viewport top.
         *
         * That is exactly where we want the section title.
         */
        state.animateScrollToItem(
            index = lazyIndex,
            scrollOffset = -categoryHeightPx
        )

        /*
         * One-frame correction after stickyHeader has settled.
         * This removes small errors caused by a header becoming
         * sticky during animateScrollToItem.
         */
        withFrameNanos { }

        val info =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.index == lazyIndex
                }

        if (info != null) {
            val wanted =
                categoryHeightPx

            val error =
                info.offset - wanted

            if (
                kotlin.math.abs(error) >
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
            .background(c.bg)
    ) {
        /*
         * Only the interactive viewport gets status-bar inset.
         *
         * Background Box remains edge-to-edge.
         *
         * This eliminates the old header/category gap.
         */
        LazyColumn(
            state = state,
            modifier =
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.statusBars
                    )
        ) {
            /*
             * ------------------------------------------------
             * HEADER
             * ------------------------------------------------
             *
             * Normal list item.
             * It completely leaves the screen when scrolling.
             */
            item(
                key = "home_header"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg)
                        .padding(
                            top = 4.dp,
                            bottom = 4.dp
                        )
                ) {
                    HomeHeader(
                        c = c,
                        theme = theme,
                        setTheme = setTheme,
                        refresh = refresh
                    )
                }
            }

            /*
             * ------------------------------------------------
             * CATEGORIES
             * ------------------------------------------------
             *
             * Immediately replaces the header at viewport top.
             * No status spacer exists between them.
             */
            stickyHeader(
                key = "home_categories"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            c.bg.copy(
                                alpha = .99f
                            )
                        )
                        .onSizeChanged {
                            categoryHeightPx =
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

                        c = c,

                        select = { id ->
                            selected = id

                            scope.launch {
                                openSection(id)
                            }
                        },

                        commit = { next ->
                            if (
                                next != currentOrder
                            ) {
                                currentOrder =
                                    next

                                saveOrder(next)
                            }
                        },

                        add = {
                            addDialog = true
                        }
                    )
                }
            }

            item(
                key = "recent"
            ) {
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
                        title =
                            "Recently Played",
                        subtitle =
                            "0 tracks played",
                        icon =
                            R.drawable.ic_xmo_history,
                        c = c
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(110.dp),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            "Nothing played yet",
                            color = c.sub,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 13.sp
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

                HomeSection(
                    section = section,
                    songs = songs,
                    allowed = allowed,
                    categories =
                        categories,
                    albumCount =
                        albumCount,
                    c = c,
                    theme = theme
                )
            }

            /*
             * Enough trailing space for the LAST category
             * to reach exactly below sticky categories.
             */
            item(
                key = "branding"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Column(
                        horizontalAlignment =
                            Alignment
                                .CenterHorizontally
                    ) {
                        Text(
                            "XMO",
                            color = c.text,
                            fontFamily =
                                XmoFont.logo,
                            fontSize = 18.sp
                        )

                        Text(
                            "lxzrvi  •  copyright © 2026",
                            color = c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 9.sp,
                            modifier =
                                Modifier.padding(
                                    top = 3.dp
                                )
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

            containerColor =
                c.surface,

            title = {
                Text(
                    "New category",
                    color = c.text,
                    fontFamily =
                        XmoFont.bold
                )
            },

            text = {
                OutlinedTextField(
                    value = newName,

                    onValueChange = {
                        newName =
                            it.take(24)
                    },

                    singleLine = true,

                    label = {
                        Text(
                            "Category name"
                        )
                    }
                )
            },

            confirmButton = {
                TextButton(
                    onClick = {
                        val name =
                            newName.trim()

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

                            val next =
                                currentOrder +
                                    category.id

                            saveCategories(
                                categories +
                                    category
                            )

                            currentOrder =
                                next

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

/*
 * ============================================================
 * CATEGORY REORDER
 * ============================================================
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

    val edgePx =
        with(density) {
            64.dp.toPx()
        }

    var working by remember {
        mutableStateOf(order)
    }

    var draggingId by remember {
        mutableStateOf<String?>(
            null
        )
    }

    /*
     * Translation of ONLY the lifted item.
     */
    var dragOffset by remember {
        mutableFloatStateOf(0f)
    }

    var autoScroll by remember {
        mutableStateOf<Job?>(null)
    }

    LaunchedEffect(order) {
        if (
            draggingId == null
        ) {
            working = order
        }
    }

    fun stopAutoScroll() {
        autoScroll?.cancel()
        autoScroll = null
    }

    /*
     * LazyRow indexes:
     *
     * 0                 = All
     * 1 .. working.size = draggable chips
     * last              = Add
     */
    fun swapIfRequired(
        id: String
    ) {
        val sourceIndex =
            working.indexOf(id)

        if (
            sourceIndex < 0
        ) {
            return
        }

        val sourceKey =
            "drag_$id"

        val sourceInfo =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.key == sourceKey
                }
                ?: return

        val draggedCenter =
            sourceInfo.offset +
                sourceInfo.size / 2f +
                dragOffset

        /*
         * Compare with actual measured neighbour centers.
         */
        if (
            sourceIndex <
            working.lastIndex
        ) {
            val nextId =
                working[
                    sourceIndex + 1
                ]

            val nextInfo =
                state.layoutInfo
                    .visibleItemsInfo
                    .firstOrNull {
                        it.key ==
                            "drag_$nextId"
                    }

            if (
                nextInfo != null
            ) {
                val nextCenter =
                    nextInfo.offset +
                        nextInfo.size / 2f

                if (
                    draggedCenter >
                    nextCenter
                ) {
                    val next =
                        working
                            .toMutableList()

                    next.add(
                        sourceIndex + 1,
                        next.removeAt(
                            sourceIndex
                        )
                    )

                    /*
                     * Natural layout moves dragged item by
                     * neighbour width + spacing.
                     *
                     * Counter it so the lifted chip stays
                     * attached to the finger.
                     */
                    val movement =
                        nextInfo.size.toFloat() +
                            with(density) {
                                8.dp.toPx()
                            }

                    working = next

                    dragOffset -=
                        movement

                    return
                }
            }
        }

        if (
            sourceIndex > 0
        ) {
            val previousId =
                working[
                    sourceIndex - 1
                ]

            val previousInfo =
                state.layoutInfo
                    .visibleItemsInfo
                    .firstOrNull {
                        it.key ==
                            "drag_$previousId"
                    }

            if (
                previousInfo != null
            ) {
                val previousCenter =
                    previousInfo.offset +
                        previousInfo.size /
                            2f

                if (
                    draggedCenter <
                    previousCenter
                ) {
                    val next =
                        working
                            .toMutableList()

                    next.add(
                        sourceIndex - 1,
                        next.removeAt(
                            sourceIndex
                        )
                    )

                    val movement =
                        previousInfo.size
                            .toFloat() +
                            with(density) {
                                8.dp.toPx()
                            }

                    working = next

                    dragOffset +=
                        movement
                }
            }
        }
    }

    LazyRow(
        state = state,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp),

        contentPadding =
            PaddingValues(
                horizontal = 14.dp,
                vertical = 5.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        /*
         * ALL
         */
        item(
            key = "__all__"
        ) {
            CategoryChip(
                text = "All",
                active =
                    selected == "all",
                c = c,
                icon =
                    R.drawable.ic_xmo_all
            ) {
                if (
                    draggingId ==
                    null
                ) {
                    select("all")
                }
            }
        }

        items(
            items = working,
            key = {
                "drag_$it"
            }
        ) { id ->

            val section =
                sections[id]
                    ?: return@items

            val moving =
                draggingId == id

            Box(
                Modifier
                    .zIndex(
                        if (moving)
                            100f
                        else
                            0f
                    )
                    .graphicsLayer {
                        translationX =
                            if (moving)
                                dragOffset
                            else
                                0f

                        val scale =
                            if (moving)
                                1.09f
                            else
                                1f

                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingId =
                                    id

                                dragOffset =
                                    0f

                                haptic
                                    .performHapticFeedback(
                                        HapticFeedbackType
                                            .LongPress
                                    )
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                dragOffset +=
                                    amount.x

                                swapIfRequired(
                                    id
                                )

                                val current =
                                    state
                                        .layoutInfo
                                        .visibleItemsInfo
                                        .firstOrNull {
                                            it.key ==
                                                "drag_$id"
                                        }

                                if (
                                    current != null
                                ) {
                                    val center =
                                        current.offset +
                                            current.size /
                                                2f +
                                            dragOffset

                                    val start =
                                        state
                                            .layoutInfo
                                            .viewportStartOffset
                                            .toFloat()

                                    val end =
                                        state
                                            .layoutInfo
                                            .viewportEndOffset
                                            .toFloat()

                                    val left =
                                        center <
                                            start +
                                                edgePx &&
                                            state
                                                .canScrollBackward

                                    val right =
                                        center >
                                            end -
                                                edgePx &&
                                            state
                                                .canScrollForward

                                    if (
                                        left ||
                                        right
                                    ) {
                                        val direction =
                                            if (left)
                                                -1f
                                            else
                                                1f

                                        if (
                                            autoScroll
                                                ?.isActive !=
                                            true
                                        ) {
                                            autoScroll =
                                                scope.launch {
                                                    while (
                                                        isActive
                                                    ) {
                                                        val consumed =
                                                            state.scrollBy(
                                                                direction *
                                                                    12f
                                                            )

                                                        /*
                                                         * Keep the dragged visual
                                                         * under the same finger.
                                                         */
                                                        dragOffset +=
                                                            consumed

                                                        swapIfRequired(
                                                            id
                                                        )

                                                        if (
                                                            kotlin.math.abs(
                                                                consumed
                                                            ) <
                                                            .1f
                                                        ) {
                                                            break
                                                        }

                                                        delay(16)
                                                    }
                                                }
                                        }
                                    } else {
                                        stopAutoScroll()
                                    }
                                }
                            },

                            onDragEnd = {
                                stopAutoScroll()

                                val final =
                                    working.toList()

                                draggingId =
                                    null

                                dragOffset =
                                    0f

                                commit(final)
                            },

                            onDragCancel = {
                                stopAutoScroll()

                                draggingId =
                                    null

                                dragOffset =
                                    0f

                                working =
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

                    c = c,

                    icon =
                        section.icon,

                    tint =
                        section.tint,

                    modifier =
                        if (moving) {
                            Modifier.border(
                                1.dp,
                                XmoRed.copy(
                                    alpha = .7f
                                ),
                                RoundedCornerShape(
                                    18.dp
                                )
                            )
                        } else {
                            Modifier
                        }
                ) {
                    if (
                        draggingId ==
                        null
                    ) {
                        select(id)
                    }
                }
            }
        }

        /*
         * ADD
         */
        item(
            key = "__add__"
        ) {
            CategoryChip(
                text = "Add",
                active = false,
                c = c,
                icon =
                    R.drawable.ic_xmo_add,
                tint = XmoRed
            ) {
                if (
                    draggingId ==
                    null
                ) {
                    add()
                }
            }
        }
    }
}

/*
 * ============================================================
 * HOME SECTION
 * ============================================================
 */

@Stable
private class SongArrowController {
    var request by
        mutableIntStateOf(0)

    fun next() {
        request++
    }
}

@Composable
private fun HomeSection(
    section: HSection,
    songs: List<Song>,
    allowed: Boolean,
    categories: List<UserCategory>,
    albumCount: Int,
    c: HomeColors,
    theme: XmoTheme
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
                vertical = 10.dp
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp
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
                            "All songs: ${songs.size}"

                        "albums" ->
                            "$albumCount albums"

                        "liked" ->
                            "0 favorites"

                        else ->
                            ""
                    },

                icon =
                    section.icon,

                c = c,

                modifier =
                    Modifier.weight(1f)
            )

            if (
                section.id ==
                "albums" ||
                section.id ==
                "liked"
            ) {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            XmoRed.copy(
                                alpha = .18f
                            )
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    XmoIcon(
                        R.drawable.ic_xmo_add,
                        XmoRed,
                        Modifier.size(
                            14.dp
                        )
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
                    arrow = arrow!!
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
                val ids =
                    categories
                        .firstOrNull {
                            it.id ==
                                section.id
                        }
                        ?.songIds
                        ?: emptySet()

                val categorySongs =
                    remember(
                        songs,
                        ids
                    ) {
                        songs.filter {
                            it.id in ids
                        }
                    }

                CustomBody(
                    songs =
                        categorySongs,
                    c = c,
                    theme = theme
                )
            }
        }
    }
}

/*
 * ============================================================
 * ALL SONGS ARROW
 * ============================================================
 */

@Composable
private fun SongArrowButton(
    controller:
        SongArrowController
) {
    val scope =
        rememberCoroutineScope()

    Box(
        Modifier
            .padding(
                start = 7.dp
            )
            .size(28.dp)
            .clip(CircleShape)
            .background(
                XmoRed.copy(
                    alpha = .18f
                )
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
                                delay(250)

                                held = true

                                while (
                                    isActive
                                ) {
                                    controller.next()

                                    delay(70)
                                }
                            }

                        val released =
                            tryAwaitRelease()

                        job.cancel()

                        if (
                            released &&
                            !held
                        ) {
                            controller.next()
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

/*
 * ============================================================
 * ALL SONGS GRID
 * ============================================================
 */

@Composable
private fun SongsGrid(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    arrow: SongArrowController
) {
    if (!allowed) {
        Empty(
            "Music access required",
            c
        )
        return
    }

    if (
        songs.isEmpty()
    ) {
        Empty(
            "No local music found",
            c
        )
        return
    }

    val gridState =
        rememberLazyGridState()

    val request =
        arrow.request

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge = 8.dp
        val gap = 8.dp

        /*
         * Exact 4-card viewport.
         */
        val card =
            (
                maxWidth -
                    edge * 2 -
                    gap * 3
                ) / 4

        val cellHeight =
            card + 37.dp

        val height =
            cellHeight * 3 +
                gap * 2

        val slotCount =
            (
                (songs.size + 11) /
                    12
                ) * 12

        LaunchedEffect(request) {
            if (
                request <= 0
            ) {
                return@LaunchedEffect
            }

            val currentColumn =
                gridState
                    .firstVisibleItemIndex /
                    3

            val columns =
                slotCount / 3

            val target =
                (
                    currentColumn + 1
                    )
                    .coerceAtMost(
                        columns - 1
                    )

            if (
                target >
                currentColumn
            ) {
                gridState
                    .animateScrollToItem(
                        index =
                            target * 3,
                        scrollOffset = 0
                    )
            }
        }

        LazyHorizontalGrid(
            rows =
                GridCells.Fixed(3),

            state =
                gridState,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height),

            contentPadding =
                PaddingValues(
                    horizontal = edge
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
                    slotCount,

                key = {
                    "song_slot_$it"
                }
            ) { slot ->

                val page =
                    slot / 12

                val inside =
                    slot % 12

                val row =
                    inside % 3

                val column =
                    inside / 3

                val source =
                    page * 12 +
                        row * 4 +
                        column

                Box(
                    Modifier.width(
                        card
                    )
                ) {
                    songs
                        .getOrNull(
                            source
                        )
                        ?.let { song ->

                            SongTile(
                                song = song,
                                index = source,
                                c = c,
                                theme = theme,
                                modifier =
                                    Modifier.width(
                                        card
                                    )
                            )
                        }
                }
            }
        }
    }
}

/*
 * ============================================================
 * OTHER BODIES
 * ============================================================
 */

@Composable
private fun AlbumBody(
    songs: List<Song>,
    c: HomeColors
) {
    if (
        songs.isEmpty()
    ) {
        Empty(
            "No albums found",
            c
        )
    } else {
        Spacer(
            Modifier.height(
                8.dp
            )
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
            Library.artists(
                songs
            )
        }

    if (
        artists.isEmpty()
    ) {
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
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        artists
            .take(15)
            .forEach { artist ->

                Column(
                    Modifier.width(
                        66.dp
                    ),

                    horizontalAlignment =
                        Alignment
                            .CenterHorizontally
                ) {
                    Box(
                        Modifier
                            .size(62.dp)
                            .background(
                                XmoRed.copy(
                                    alpha = .16f
                                ),
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
    if (
        songs.isEmpty()
    ) {
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
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        songs
            .chunked(6)
            .forEachIndexed {
                    row,
                    items ->

                Row(
                    Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            5.dp
                        )
                ) {
                    repeat(6) { column ->

                        Box(
                            Modifier.weight(
                                1f
                            )
                        ) {
                            items
                                .getOrNull(
                                    column
                                )
                                ?.let { song ->

                                    SongTile(
                                        song = song,
                                        index =
                                            row * 6 +
                                                column,
                                        c = c,
                                        theme = theme,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
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
            text = text,
            color = c.sub,
            fontFamily =
                XmoFont.normal,
            fontSize = 12.sp
        )
    }
}
