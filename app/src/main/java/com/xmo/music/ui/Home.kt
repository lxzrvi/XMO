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
    saveCategories: (List<UserCategory>) -> Unit
) {
    val c = homeColors(theme)

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

    val baseSections =
        remember {
            listOf(
                HSection(
                    id = "songs",
                    name = "All Songs",
                    icon = R.drawable.ic_xmo_songs
                ),

                HSection(
                    id = "albums",
                    name = "Albums",
                    icon = R.drawable.ic_xmo_album
                ),

                HSection(
                    id = "liked",
                    name = "Liked Songs",
                    icon = R.drawable.ic_xmo_heart
                ),

                HSection(
                    id = "artists",
                    name = "Artists",
                    icon = R.drawable.ic_xmo_artist
                )
            )
        }

    val customSections =
        remember(categories) {
            categories.map { category ->

                val iconIndex =
                    category.icon.mod(
                        customIcons.size
                    )

                HSection(
                    id = category.id,
                    name = category.name,
                    icon =
                        customIcons[
                            iconIndex
                        ],
                    tint =
                        customTints[
                            iconIndex
                        ]
                )
            }
        }

    val sectionMap =
        remember(
            baseSections,
            customSections
        ) {
            (
                baseSections +
                    customSections
                )
                .associateBy {
                    it.id
                }
        }

    /*
     * Remove stale IDs, preserve saved order,
     * then append new categories.
     */
    val resolvedOrder =
        remember(
            order,
            sectionMap
        ) {
            order.filter {
                sectionMap.containsKey(
                    it
                )
            } +
                sectionMap.keys.filterNot {
                    order.contains(
                        it
                    )
                }
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

    var selected by
        remember {
            mutableStateOf(
                "all"
            )
        }

    var addDialog by
        remember {
            mutableStateOf(
                false
            )
        }

    var newName by
        remember {
            mutableStateOf("")
        }

    /*
     * Actual category dock height.
     */
    var categoryHeightPx by
        remember {
            mutableIntStateOf(
                0
            )
        }

    val albumCount =
        remember(songs) {
            Library
                .albums(songs)
                .size
        }

    /*
     * LazyColumn:
     *
     * 0 = Header
     * 1 = Sticky categories
     * 2 = Recently Played
     * 3+ = reordered sections
     */
    suspend fun openSection(
        id: String
    ) {
        if (
            id == "all"
        ) {
            state.animateScrollToItem(
                index = 0,
                scrollOffset = 0
            )

            return
        }

        val orderIndex =
            currentOrder.indexOf(
                id
            )

        if (
            orderIndex < 0
        ) {
            return
        }

        val lazyIndex =
            orderIndex + 3

        /*
         * First navigation.
         */
        state.animateScrollToItem(
            index = lazyIndex,
            scrollOffset =
                -categoryHeightPx
        )

        /*
         * Allow sticky header to settle.
         */
        withFrameNanos { }

        /*
         * Exact correction.
         */
        val info =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.index ==
                        lazyIndex
                }

        if (
            info != null
        ) {
            val wanted =
                categoryHeightPx

            val error =
                info.offset -
                    wanted

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
            .background(c.bg)
    ) {
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
             * =================================================
             * HEADER
             * =================================================
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
                        setTheme =
                            setTheme,
                        refresh =
                            refresh
                    )
                }
            }

            /*
             * =================================================
             * STICKY CATEGORY DOCK
             * =================================================
             */
            stickyHeader(
                key = "home_categories"
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            c.bg.copy(
                                alpha =
                                    .99f
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

                            selected =
                                id

                            scope.launch {
                                openSection(
                                    id
                                )
                            }
                        },

                        commit = {
                                next ->

                            if (
                                next !=
                                currentOrder
                            ) {
                                currentOrder =
                                    next

                                saveOrder(
                                    next
                                )
                            }
                        },

                        add = {
                            addDialog =
                                true
                        }
                    )
                }
            }

            /*
             * =================================================
             * RECENTLY PLAYED
             * =================================================
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
                            R.drawable
                                .ic_xmo_history,

                        c = c
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(
                                110.dp
                            ),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            "Nothing played yet",

                            color =
                                c.sub,

                            fontFamily =
                                XmoFont.normal,

                            fontSize =
                                13.sp
                        )
                    }
                }
            }

            /*
             * =================================================
             * HOME SECTIONS
             * =================================================
             */
            items(
                items =
                    currentOrder,

                key = {
                    "section_$it"
                }
            ) { id ->

                val section =
                    sectionMap[id]
                        ?: return@items

                HomeSection(
                    section =
                        section,

                    songs =
                        songs,

                    allowed =
                        allowed,

                    categories =
                        categories,

                    albumCount =
                        albumCount,

                    c = c,

                    theme =
                        theme
                )
            }

            /*
             * =================================================
             * FOOTER
             * =================================================
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

                            color =
                                c.text,

                            fontFamily =
                                XmoFont.logo,

                            fontSize =
                                18.sp
                        )

                        Text(
                            "lxzrvi  •  copyright © 2026",

                            color =
                                c.sub,

                            fontFamily =
                                XmoFont.thin,

                            fontSize =
                                9.sp,

                            modifier =
                                Modifier
                                    .padding(
                                        top = 3.dp
                                    )
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
        AlertDialog(
            onDismissRequest = {
                addDialog =
                    false

                newName =
                    ""
            },

            containerColor =
                c.surface,

            title = {
                Text(
                    "New category",

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.bold
                )
            },

            text = {
                OutlinedTextField(
                    value =
                        newName,

                    onValueChange = {
                        newName =
                            it.take(
                                24
                            )
                    },

                    singleLine =
                        true,

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
                            newName
                                .trim()

                        if (
                            name
                                .isNotEmpty()
                        ) {
                            val category =
                                UserCategory(
                                    id =
                                        "cat_${UUID.randomUUID()}",

                                    name =
                                        name,

                                    icon =
                                        categories
                                            .size %
                                            4
                                )

                            val nextCategories =
                                categories +
                                    category

                            val nextOrder =
                                currentOrder +
                                    category.id

                            /*
                             * Update immediately so chip and
                             * section appear together.
                             */
                            currentOrder =
                                nextOrder

                            saveCategories(
                                nextCategories
                            )

                            saveOrder(
                                nextOrder
                            )

                            newName =
                                ""

                            addDialog =
                                false
                        }
                    }
                ) {
                    Text(
                        "Add",

                        color =
                            XmoRed
                    )
                }
            },

            dismissButton = {
                TextButton(
                    onClick = {
                        newName =
                            ""

                        addDialog =
                            false
                    }
                ) {
                    Text(
                        "Cancel",

                        color =
                            c.sub
                    )
                }
            }
        )
    }
}

/*
 * =============================================================
 * CATEGORY DRAG / REORDER
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

    val edgeZonePx =
        with(density) {
            76.dp.toPx()
        }

    val autoScrollBasePx =
        with(density) {
            8.dp.toPx()
        }

    /*
     * Important for newly added custom categories.
     */
    var working by
        remember(order) {
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

    /*
     * Current graphics translation of lifted chip.
     */
    var dragOffset by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * Finger X in LazyRow viewport coordinates.
     *
     * The finger stays here while the list itself auto-scrolls.
     */
    var fingerX by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    var autoScrollJob by
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
            working =
                order
        }
    }

    fun stopAutoScroll() {
        autoScrollJob
            ?.cancel()

        autoScrollJob =
            null
    }

    /*
     * Use stable keys rather than reorder-sensitive indexes.
     */
    fun itemInfo(
        id: String
    ): LazyListItemInfo? {
        return state
            .layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.key ==
                    "drag_$id"
            }
    }

    /*
     * Keep dragged item's visual center attached to finger.
     */
    fun lockToFinger(
        id: String
    ) {
        val info =
            itemInfo(id)
                ?: return

        val naturalCenter =
            info.offset +
                info.size /
                    2f

        dragOffset =
            fingerX -
                naturalCenter
    }

    /*
     * Swap with neighbour when finger crosses its center.
     *
     * One swap per layout frame lets LazyRow publish the
     * new keyed positions cleanly before the next swap.
     */
    fun reorderForFinger(
        id: String
    ) {
        val from =
            working.indexOf(
                id
            )

        if (
            from < 0
        ) {
            return
        }

        /*
         * RIGHT
         */
        if (
            from <
            working.lastIndex
        ) {
            val rightId =
                working[
                    from + 1
                ]

            val right =
                itemInfo(
                    rightId
                )

            if (
                right != null
            ) {
                val rightCenter =
                    right.offset +
                        right.size /
                            2f

                if (
                    fingerX >
                    rightCenter
                ) {
                    val next =
                        working
                            .toMutableList()

                    next.add(
                        from + 1,
                        next.removeAt(
                            from
                        )
                    )

                    working =
                        next

                    return
                }
            }
        }

        /*
         * LEFT
         */
        if (
            from > 0
        ) {
            val leftId =
                working[
                    from - 1
                ]

            val left =
                itemInfo(
                    leftId
                )

            if (
                left != null
            ) {
                val leftCenter =
                    left.offset +
                        left.size /
                            2f

                if (
                    fingerX <
                    leftCenter
                ) {
                    val next =
                        working
                            .toMutableList()

                    next.add(
                        from - 1,
                        next.removeAt(
                            from
                        )
                    )

                    working =
                        next
                }
            }
        }
    }

    /*
     * After a swap, the same stable-key chip receives a new
     * natural slot. Re-lock it to the same finger.
     */
    LaunchedEffect(
        working,
        draggingId
    ) {
        val id =
            draggingId
                ?: return@LaunchedEffect

        withFrameNanos { }

        lockToFinger(
            id
        )

        /*
         * If finger travelled far enough to cross another chip,
         * continue on the next layout frame.
         */
        reorderForFinger(
            id
        )
    }

    LazyRow(
        state =
            state,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    44.dp
                ),

        contentPadding =
            PaddingValues(
                horizontal =
                    14.dp,

                vertical =
                    5.dp
            ),

        horizontalArrangement =
            Arrangement
                .spacedBy(
                    8.dp
                ),

        /*
         * While reordering, only our edge auto-scroll controls
         * LazyRow. This avoids gesture conflict.
         */
        userScrollEnabled =
            draggingId ==
                null
    ) {
        /*
         * -----------------------------------------------------
         * ALL — FIXED
         * -----------------------------------------------------
         */
        item(
            key = "__all__"
        ) {
            CategoryChip(
                text =
                    "All",

                active =
                    selected ==
                        "all",

                c = c,

                icon =
                    R.drawable
                        .ic_xmo_all
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

        /*
         * -----------------------------------------------------
         * REORDERABLE CATEGORIES
         * -----------------------------------------------------
         */
        items(
            items =
                working,

            key = {
                "drag_$it"
            }
        ) { id ->

            val section =
                sections[id]
                    ?: return@items

            val moving =
                draggingId ==
                    id

            Box(
                Modifier
                    .zIndex(
                        if (
                            moving
                        ) {
                            1000f
                        } else {
                            0f
                        }
                    )
                    .graphicsLayer {
                        if (
                            moving
                        ) {
                            translationX =
                                dragOffset

                            scaleX =
                                1.09f

                            scaleY =
                                1.09f
                        }
                    }
                    .pointerInput(
                        id
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                    offset ->

                                draggingId =
                                    id

                                val info =
                                    itemInfo(
                                        id
                                    )

                                /*
                                 * Convert local pointer coordinate
                                 * into LazyRow viewport coordinate.
                                 */
                                fingerX =
                                    if (
                                        info !=
                                        null
                                    ) {
                                        info.offset +
                                            offset.x
                                    } else {
                                        offset.x
                                    }

                                lockToFinger(
                                    id
                                )

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

                                /*
                                 * Only real finger movement changes
                                 * fingerX.
                                 */
                                fingerX +=
                                    amount.x

                                lockToFinger(
                                    id
                                )

                                reorderForFinger(
                                    id
                                )

                                val viewportStart =
                                    state.layoutInfo
                                        .viewportStartOffset
                                        .toFloat()

                                val viewportEnd =
                                    state.layoutInfo
                                        .viewportEndOffset
                                        .toFloat()

                                val nearLeft =
                                    fingerX <
                                        viewportStart +
                                            edgeZonePx

                                val nearRight =
                                    fingerX >
                                        viewportEnd -
                                            edgeZonePx

                                val canGoLeft =
                                    nearLeft &&
                                        state
                                            .canScrollBackward

                                val canGoRight =
                                    nearRight &&
                                        state
                                            .canScrollForward

                                if (
                                    canGoLeft ||
                                    canGoRight
                                ) {
                                    val direction =
                                        if (
                                            canGoLeft
                                        ) {
                                            -1f
                                        } else {
                                            1f
                                        }

                                    if (
                                        autoScrollJob
                                            ?.isActive !=
                                        true
                                    ) {
                                        autoScrollJob =
                                            scope.launch {

                                                while (
                                                    isActive &&
                                                    draggingId ==
                                                        id
                                                ) {
                                                    /*
                                                     * Re-read viewport every frame.
                                                     */
                                                    val start =
                                                        state.layoutInfo
                                                            .viewportStartOffset
                                                            .toFloat()

                                                    val end =
                                                        state.layoutInfo
                                                            .viewportEndOffset
                                                            .toFloat()

                                                    val strength =
                                                        if (
                                                            direction <
                                                            0f
                                                        ) {
                                                            (
                                                                1f -
                                                                    (
                                                                        fingerX -
                                                                            start
                                                                        ) /
                                                                    edgeZonePx
                                                                )
                                                                .coerceIn(
                                                                    0f,
                                                                    1f
                                                                )
                                                        } else {
                                                            (
                                                                1f -
                                                                    (
                                                                        end -
                                                                            fingerX
                                                                        ) /
                                                                    edgeZonePx
                                                                )
                                                                .coerceIn(
                                                                    0f,
                                                                    1f
                                                                )
                                                        }

                                                    val amountToScroll =
                                                        direction *
                                                            autoScrollBasePx *
                                                            (
                                                                .8f +
                                                                    strength *
                                                                    1.8f
                                                                )

                                                    val consumed =
                                                        state.scrollBy(
                                                            amountToScroll
                                                        )

                                                    /*
                                                     * Do NOT move fingerX.
                                                     *
                                                     * Content moves underneath
                                                     * the stationary finger.
                                                     */
                                                    lockToFinger(
                                                        id
                                                    )

                                                    reorderForFinger(
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
                                                        16
                                                    )
                                                }
                                            }
                                    }
                                } else {
                                    stopAutoScroll()
                                }
                            },

                            onDragEnd = {
                                stopAutoScroll()

                                val final =
                                    working
                                        .toList()

                                draggingId =
                                    null

                                dragOffset =
                                    0f

                                fingerX =
                                    0f

                                commit(
                                    final
                                )
                            },

                            onDragCancel = {
                                stopAutoScroll()

                                draggingId =
                                    null

                                dragOffset =
                                    0f

                                fingerX =
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
                        if (
                            moving
                        ) {
                            Modifier
                                .border(
                                    width =
                                        1.dp,

                                    color =
                                        XmoRed.copy(
                                            alpha =
                                                .72f
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
                        select(
                            id
                        )
                    }
                }
            }
        }

        /*
         * -----------------------------------------------------
         * ADD — FIXED
         * -----------------------------------------------------
         */
        item(
            key = "__add__"
        ) {
            CategoryChip(
                text =
                    "Add",

                active =
                    false,

                c = c,

                icon =
                    R.drawable
                        .ic_xmo_add,

                tint =
                    XmoRed
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
 * =============================================================
 * SONG ARROW CONTROLLER
 * =============================================================
 */

@Stable
private class SongArrowController {

    var tapRequest by
        mutableIntStateOf(0)
        private set

    var fastScrolling by
        mutableStateOf(false)
        private set

    fun tap() {
        tapRequest++
    }

    fun startFast() {
        fastScrolling =
            true
    }

    fun stopFast() {
        fastScrolling =
            false
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

    /*
     * + on:
     *
     * Albums
     * Liked Songs
     * Every custom category
     *
     * No + on:
     *
     * All Songs
     * Artists
     */
    val showAddAction =
        section.id ==
            "albums" ||
            section.id ==
            "liked" ||
            (
                section.id !=
                    "songs" &&
                    section.id !=
                    "artists"
                )

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
                Alignment
                    .CenterVertically
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
                    Modifier.weight(
                        1f
                    )
            )

            /*
             * Custom category also gets the circular +.
             *
             * It is deliberately not wired to fake behavior yet.
             */
            if (
                showAddAction
            ) {
                Box(
                    Modifier
                        .size(
                            28.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            XmoRed.copy(
                                alpha =
                                    .18f
                            )
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    XmoIcon(
                        R.drawable
                            .ic_xmo_add,

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
                    songs =
                        songs,

                    allowed =
                        allowed,

                    c = c,

                    theme =
                        theme,

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

                val categorySongs =
                    remember(
                        songs,
                        ids
                    ) {
                        songs.filter {
                            it.id in
                                ids
                        }
                    }

                CustomBody(
                    songs =
                        categorySongs,

                    c = c,

                    theme =
                        theme
                )
            }
        }
    }
}

/*
 * =============================================================
 * SONG ARROW
 * =============================================================
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
                start =
                    7.dp
            )
            .size(
                28.dp
            )
            .clip(
                CircleShape
            )
            .background(
                XmoRed.copy(
                    alpha =
                        .18f
                )
            )
            .pointerInput(
                controller
            ) {
                detectTapGestures(
                    onPress = {
                        var holdStarted =
                            false

                        val holdJob =
                            scope.launch {
                                delay(
                                    250
                                )

                                holdStarted =
                                    true

                                controller
                                    .startFast()
                            }

                        val released =
                            tryAwaitRelease()

                        holdJob.cancel()

                        /*
                         * Release instantly stops hold scrolling.
                         */
                        controller
                            .stopFast()

                        /*
                         * Short tap = exactly one column.
                         */
                        if (
                            released &&
                            !holdStarted
                        ) {
                            controller
                                .tap()
                        }
                    }
                )
            },

        contentAlignment =
            Alignment.Center
    ) {
        XmoIcon(
            R.drawable
                .ic_xmo_arrow,

            XmoRed,

            Modifier.size(
                14.dp
            )
        )
    }
}

/*
 * =============================================================
 * ALL SONGS GRID
 * =============================================================
 */

@Composable
private fun SongsGrid(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    arrow: SongArrowController
) {
    if (
        !allowed
    ) {
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

    val tapRequest =
        arrow.tapRequest

    val fastScrolling =
        arrow.fastScrolling

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge =
            8.dp

        val gap =
            8.dp

        /*
         * Exactly 4 complete visible columns.
         */
        val card =
            (
                maxWidth -
                    edge * 2 -
                    gap * 3
                ) / 4

        val cellHeight =
            card +
                37.dp

        val height =
            cellHeight *
                3 +
                gap * 2

        val slotCount =
            (
                (songs.size + 11) /
                    12
                ) * 12

        /*
         * -----------------------------------------------------
         * TAP = ONE COLUMN
         * -----------------------------------------------------
         */
        LaunchedEffect(
            tapRequest
        ) {
            if (
                tapRequest <=
                0
            ) {
                return@LaunchedEffect
            }

            val currentColumn =
                gridState
                    .firstVisibleItemIndex /
                    3

            val totalColumns =
                slotCount /
                    3

            if (
                totalColumns <=
                0
            ) {
                return@LaunchedEffect
            }

            val targetColumn =
                (
                    currentColumn +
                        1
                    )
                    .coerceAtMost(
                        totalColumns -
                            1
                    )

            if (
                targetColumn >
                currentColumn
            ) {
                gridState
                    .animateScrollToItem(
                        index =
                            targetColumn *
                                3,

                        scrollOffset =
                            0
                    )
            }
        }

        /*
         * -----------------------------------------------------
         * HOLD = CONTINUOUS FAST SCROLL
         * -----------------------------------------------------
         */
        LaunchedEffect(
            fastScrolling
        ) {
            if (
                !fastScrolling
            ) {
                return@LaunchedEffect
            }

            while (
                isActive &&
                arrow.fastScrolling
            ) {
                val consumed =
                    gridState.scrollBy(
                        18f
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
                    16
                )
            }
        }

        LazyHorizontalGrid(
            rows =
                GridCells.Fixed(
                    3
                ),

            state =
                gridState,

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
                Arrangement
                    .spacedBy(
                        gap
                    ),

            verticalArrangement =
                Arrangement
                    .spacedBy(
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
                 * Required visual order:
                 *
                 * 1  2  3  4
                 * 5  6  7  8
                 * 9 10 11 12
                 */
                val page =
                    slot /
                        12

                val inside =
                    slot %
                        12

                val row =
                    inside %
                        3

                val column =
                    inside /
                        3

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
                        ?.let {
                                song ->

                            SongTile(
                                song =
                                    song,

                                index =
                                    source,

                                c = c,

                                theme =
                                    theme,

                                modifier =
                                    Modifier
                                        .width(
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
 * =============================================================
 * ALBUMS
 * =============================================================
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

/*
 * =============================================================
 * ARTISTS
 * =============================================================
 */

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
            Arrangement
                .spacedBy(
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
                            Modifier
                                .padding(
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
 * CUSTOM CATEGORY BODY
 * =============================================================
 */

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
        Modifier
            .padding(
                horizontal =
                    8.dp
            ),

        verticalArrangement =
            Arrangement
                .spacedBy(
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
                        Arrangement
                            .spacedBy(
                                5.dp
                            )
                ) {
                    repeat(6) {
                            column ->

                        Box(
                            Modifier
                                .weight(
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

/*
 * =============================================================
 * EMPTY
 * =============================================================
 */

@Composable
private fun Empty(
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
            text =
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
