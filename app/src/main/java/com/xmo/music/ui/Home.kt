package com.xmo.music.ui

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
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
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
import kotlin.math.abs

private data class HSection(
    val id: String,
    val name: String,
    val icon: Int,
    val tint: Color = XmoRed
)

private data class ChipBounds(
    val left: Float,
    val right: Float
) {
    val center: Float
        get() = (left + right) / 2f
}

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

    val listState = rememberLazyListState()
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

    val baseSections = remember {
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

    val customSections = remember(
        categories
    ) {
        categories.map { category ->
            val i = category.icon.mod(4)

            HSection(
                id = category.id,
                name = category.name,
                icon = customIcons[i],
                tint = customTints[i]
            )
        }
    }

    val sectionMap = remember(
        baseSections,
        customSections
    ) {
        (baseSections + customSections)
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
        if (resolved != currentOrder) {
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
     * REAL measured sticky dock height.
     */
    var dockHeightPx by remember {
        mutableIntStateOf(0)
    }

    val albums = remember(songs) {
        Library.albums(songs)
    }

    /*
     * Current LazyColumn structure:
     *
     * 0 = header
     * 1 = sticky categories
     * 2 = Recently Played
     * 3 = first reorderable section
     * 4 = second ...
     */
    fun indexForSection(
        id: String
    ): Int {
        val position =
            currentOrder.indexOf(id)

        return if (position < 0) {
            -1
        } else {
            position + 3
        }
    }

    suspend fun scrollToCategory(
        id: String
    ) {
        if (id == "all") {
            listState.animateScrollToItem(
                index = 0,
                scrollOffset = 0
            )

            return
        }

        val target =
            indexForSection(id)

        if (target < 0) {
            return
        }

        /*
         * In a LazyColumn scrollOffset is the amount by
         * which that item goes ABOVE normal viewport start.
         *
         * Because the sticky dock occupies the top,
         * passing its height places the section content
         * immediately below it.
         */
        listState.animateScrollToItem(
            index = target,
            scrollOffset =
                dockHeightPx.coerceAtLeast(0)
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier.fillMaxSize()
        ) {
            /*
             * -----------------------------------------------
             * REAL HOME HEADER
             * -----------------------------------------------
             *
             * It belongs to the scroll itself.
             *
             * No overlay.
             * No fake alpha animation.
             * No shrinking spacer.
             *
             * Scroll -> entire thing physically leaves screen.
             */
            item(
                key = "home_header"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg)
                        .windowInsetsPadding(
                            WindowInsets.statusBars
                        )
                        .padding(
                            top = 4.dp,
                            bottom = 5.dp
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
             * -----------------------------------------------
             * STICKY CATEGORIES
             * -----------------------------------------------
             *
             * Status-bar inset belongs to THIS pinned block.
             *
             * When header leaves, this immediately becomes
             * the top Home UI below the status bar.
             */
            stickyHeader(
                key = "home_categories"
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            c.bg.copy(
                                alpha = .985f
                            )
                        )
                        .onGloballyPositioned {
                            dockHeightPx =
                                it.size.height
                        }
                ) {
                    Spacer(
                        Modifier.windowInsetsTopHeight(
                            WindowInsets.statusBars
                        )
                    )

                    CategoryDragRow(
                        sections =
                            currentOrder
                                .mapNotNull(
                                    sectionMap::get
                                ),

                        order = currentOrder,

                        selected = selected,

                        c = c,

                        select = { id ->
                            selected = id

                            scope.launch {
                                scrollToCategory(id)
                            }
                        },

                        commit = { next ->
                            if (
                                next != currentOrder
                            ) {
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

            /*
             * -----------------------------------------------
             * RECENTLY PLAYED
             * -----------------------------------------------
             *
             * Always first. Not reorderable.
             */
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

            /*
             * SAME order as category strip.
             */
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
                    categories = categories,
                    albumCount =
                        albums.size,
                    c = c,
                    theme = theme
                )
            }

            /*
             * Full viewport trailing area.
             *
             * Last section therefore gets enough vertical
             * range to become the top selected section too.
             */
            item(
                key = "brand"
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

    /*
     * -------------------------------------------------------
     * ADD CATEGORY
     * -------------------------------------------------------
     */
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
                                    name = name,
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

                            addDialog =
                                false
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
private fun CategoryDragRow(
    sections: List<HSection>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    select: (String) -> Unit,
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    val scrollState =
        rememberScrollState()

    val scope =
        rememberCoroutineScope()

    val haptic =
        LocalHapticFeedback.current

    val density =
        LocalDensity.current

    /*
     * The category preview order is local.
     *
     * Therefore dragging chips does NOT recompose/reorder
     * all artwork-heavy sections under the finger.
     */
    var working by remember {
        mutableStateOf(order)
    }

    var draggingId by remember {
        mutableStateOf<String?>(
            null
        )
    }

    var dragX by remember {
        mutableFloatStateOf(0f)
    }

    var rowWidth by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Actual category coordinates.
     */
    val bounds =
        remember {
            mutableStateMapOf<
                String,
                ChipBounds
                >()
        }

    var autoScrollJob by remember {
        mutableStateOf<Job?>(null)
    }

    val edgeZone =
        with(density) {
            58.dp.toPx()
        }

    LaunchedEffect(order) {
        if (
            draggingId == null
        ) {
            working = order
        }
    }

    fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    fun swapForFinger(
        id: String
    ) {
        val currentIndex =
            working.indexOf(id)

        if (
            currentIndex < 0
        ) {
            return
        }

        val movingBounds =
            bounds[id]
                ?: return

        val movingCenter =
            movingBounds.center +
                dragX

        /*
         * Going right.
         */
        if (
            currentIndex <
            working.lastIndex
        ) {
            val rightId =
                working[
                    currentIndex + 1
                ]

            val right =
                bounds[rightId]

            if (
                right != null &&
                movingCenter >
                right.center
            ) {
                val next =
                    working.toMutableList()

                next.add(
                    currentIndex + 1,
                    next.removeAt(
                        currentIndex
                    )
                )

                /*
                 * The composable itself changes its natural
                 * layout position after swap.
                 *
                 * Compensate by the real neighbour width so
                 * it visually stays with the same finger.
                 */
                val distance =
                    right.center -
                        movingBounds.center

                working = next

                dragX -=
                    distance

                return
            }
        }

        /*
         * Going left.
         */
        if (
            currentIndex > 0
        ) {
            val leftId =
                working[
                    currentIndex - 1
                ]

            val left =
                bounds[leftId]

            if (
                left != null &&
                movingCenter <
                left.center
            ) {
                val next =
                    working.toMutableList()

                next.add(
                    currentIndex - 1,
                    next.removeAt(
                        currentIndex
                    )
                )

                val distance =
                    movingBounds.center -
                        left.center

                working = next

                dragX +=
                    distance
            }
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                rowWidth =
                    it.size.width.toFloat()
            }
            .horizontalScroll(
                state = scrollState,
                enabled =
                    draggingId ==
                        null
            )
            .padding(
                horizontal = 14.dp,
                vertical = 5.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        /*
         * ---------------------------------------------------
         * ALL
         * Fixed forever.
         * ---------------------------------------------------
         */
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

        working.forEach { id ->

            val section =
                sections.firstOrNull {
                    it.id == id
                } ?: return@forEach

            val moving =
                draggingId == id

            Box(
                Modifier
                    .onGloballyPositioned {
                        val rect =
                            it.boundsInParent()

                        /*
                         * Convert from scroll-content coordinate
                         * into current visible Row coordinate.
                         */
                        bounds[id] =
                            ChipBounds(
                                left =
                                    rect.left,
                                right =
                                    rect.right
                            )
                    }
                    .zIndex(
                        if (moving)
                            100f
                        else
                            0f
                    )
                    .graphicsLayer {
                        translationX =
                            if (moving)
                                dragX
                            else
                                0f

                        val scale =
                            if (moving)
                                1.08f
                            else
                                1f

                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(
                        id
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingId =
                                    id

                                dragX =
                                    0f

                                haptic
                                    .performHapticFeedback(
                                        HapticFeedbackType
                                            .LongPress
                                    )
                            },

                            onDrag = {
                                    change,
                                    dragAmount ->

                                change.consume()

                                dragX +=
                                    dragAmount.x

                                /*
                                 * Swap repeatedly as centers cross.
                                 */
                                swapForFinger(id)

                                val current =
                                    bounds[id]

                                if (
                                    current != null
                                ) {
                                    val fingerCenter =
                                        current.center +
                                            dragX -
                                            scrollState.value

                                    val goLeft =
                                        fingerCenter <
                                            edgeZone &&
                                            scrollState.value >
                                            0

                                    val goRight =
                                        fingerCenter >
                                            rowWidth -
                                                edgeZone &&
                                            scrollState.value <
                                            scrollState.maxValue

                                    if (
                                        goLeft ||
                                        goRight
                                    ) {
                                        val direction =
                                            if (goLeft)
                                                -1
                                            else
                                                1

                                        if (
                                            autoScrollJob ==
                                                null ||
                                            autoScrollJob
                                                ?.isActive !=
                                            true
                                        ) {
                                            autoScrollJob =
                                                scope.launch {
                                                    while (
                                                        isActive
                                                    ) {
                                                        val before =
                                                            scrollState.value

                                                        scrollState.scrollTo(
                                                            (
                                                                before +
                                                                    direction *
                                                                    13
                                                                )
                                                                .coerceIn(
                                                                    0,
                                                                    scrollState
                                                                        .maxValue
                                                                )
                                                        )

                                                        val consumed =
                                                            scrollState.value -
                                                                before

                                                        /*
                                                         * Content moved underneath
                                                         * a stationary finger.
                                                         */
                                                        dragX +=
                                                            consumed.toFloat()

                                                        swapForFinger(
                                                            id
                                                        )

                                                        if (
                                                            consumed ==
                                                            0
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

                                dragX =
                                    0f

                                /*
                                 * Sections + DataStore change only here.
                                 */
                                commit(final)
                            },

                            onDragCancel = {
                                stopAutoScroll()

                                draggingId =
                                    null

                                dragX =
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
                                width = 1.dp,
                                color =
                                    XmoRed.copy(
                                        alpha = .7f
                                    ),
                                shape =
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
         * ---------------------------------------------------
         * ADD
         * Fixed forever.
         * ---------------------------------------------------
         */
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
                    songs = songs,
                    allowed = allowed,
                    c = c,
                    theme = theme,
                    arrow =
                        arrow!!
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

                val customSongs =
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
                        customSongs,
                    c = c,
                    theme = theme
                )
            }
        }
    }
}

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
         * Exactly FOUR complete columns.
         */
        val card =
            (
                maxWidth -
                    edge * 2 -
                    gap * 3
                ) / 4

        val cellHeight =
            card + 37.dp

        val totalHeight =
            cellHeight * 3 +
                gap * 2

        /*
         * Whole 12-item pages are allocated so mapping
         * stays correct on the last partial page too.
         */
        val slotCount =
            (
                (songs.size + 11) /
                    12
                ) * 12

        LaunchedEffect(
            request
        ) {
            if (
                request <= 0
            ) {
                return@LaunchedEffect
            }

            val currentColumn =
                gridState
                    .firstVisibleItemIndex /
                    3

            val totalColumns =
                slotCount / 3

            val target =
                (
                    currentColumn +
                        1
                    )
                    .coerceAtMost(
                        totalColumns -
                            1
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
                    .height(
                        totalHeight
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
                    slotCount,

                key = {
                    "song_slot_$it"
                }
            ) { slot ->

                /*
                 * Compose grid:
                 *
                 * 0 3 6 9
                 * 1 4 7 10
                 * 2 5 8 11
                 *
                 * Desired:
                 *
                 * 1  2  3  4
                 * 5  6  7  8
                 * 9 10 11 12
                 */
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
                                song =
                                    song,

                                index =
                                    source,

                                c = c,

                                theme =
                                    theme,

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
                horizontal =
                    8.dp
            ),

        horizontalArrangement =
            Arrangement.spacedBy(
                10.dp
            )
    ) {
        artists
            .take(15)
            .forEach {
                    artist ->

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
                            .size(
                                62.dp
                            )
                            .background(
                                XmoRed.copy(
                                    alpha =
                                        .16f
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

                            color =
                                XmoRed,

                            fontFamily =
                                XmoFont.bold,

                            fontSize =
                                17.sp
                        )
                    }

                    Text(
                        artist.name,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            9.sp,

                        lineHeight =
                            10.sp,

                        maxLines =
                            2,

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
            horizontal =
                8.dp
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
                    Modifier
                        .fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            5.dp
                        )
                ) {
                    repeat(6) {
                            column ->

                        Box(
                            Modifier.weight(
                                1f
                            )
                        ) {
                            items
                                .getOrNull(
                                    column
                                )
                                ?.let {
                                        song ->

                                    SongTile(
                                        song =
                                            song,

                                        index =
                                            row *
                                                6 +
                                                column,

                                        c = c,

                                        theme =
                                            theme,

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
