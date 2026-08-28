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

    /*
     * Rebuilt only when categories actually change.
     */
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
     * DataStore can contain old/removed IDs.
     * Strip them, then append newly created categories.
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
     * Real category dock height.
     */
    var categoryHeightPx by
        remember {
            mutableIntStateOf(
                0
            )
        }

    /*
     * Metadata calculation only when songs change.
     */
    val albumCount =
        remember(songs) {
            Library
                .albums(songs)
                .size
        }

    /*
     * LazyColumn:
     *
     * 0 Header
     * 1 Sticky category dock
     * 2 Recently Played
     * 3+ reordered sections
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
         * Place the section below the pinned categories.
         */
        state.animateScrollToItem(
            index = lazyIndex,
            scrollOffset =
                -categoryHeightPx
        )

        /*
         * Let stickyHeader settle.
         */
        withFrameNanos { }

        /*
         * Exact final correction.
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
                abs(error) > 1
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
             *
             * Real list item.
             * Scrolls completely off screen.
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
             * PINNED CATEGORIES
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
             * REORDERABLE HOME SECTIONS
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
             *
             * One complete viewport gives the final section
             * enough trailing scroll range.
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
     * ADD CUSTOM CATEGORY
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

                            /*
                             * IMPORTANT:
                             *
                             * Update category + local order before
                             * dialog closes.
                             *
                             * This fixes custom chip disappearing
                             * while its section already exists.
                             */
                            val nextCategories =
                                categories +
                                    category

                            val nextOrder =
                                currentOrder +
                                    category.id

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
 * CATEGORY REORDER
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

    val edgePx =
        with(density) {
            64.dp.toPx()
        }

    val gapPx =
        with(density) {
            8.dp.toPx()
        }

    /*
     * IMPORTANT FIX:
     *
     * New custom category changes "order".
     * remember(order) immediately gives the row the new item.
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

    var dragOffset by
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

    fun swapIfRequired(
        id: String
    ) {
        val sourceIndex =
            working.indexOf(
                id
            )

        if (
            sourceIndex < 0
        ) {
            return
        }

        val sourceInfo =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.key ==
                        "drag_$id"
                }
                ?: return

        val sourceCenter =
            sourceInfo.offset +
                sourceInfo.size /
                    2f +
                dragOffset

        /*
         * Move RIGHT.
         */
        if (
            sourceIndex <
            working.lastIndex
        ) {
            val rightId =
                working[
                    sourceIndex + 1
                ]

            val rightInfo =
                state.layoutInfo
                    .visibleItemsInfo
                    .firstOrNull {
                        it.key ==
                            "drag_$rightId"
                    }

            if (
                rightInfo != null
            ) {
                val rightCenter =
                    rightInfo.offset +
                        rightInfo.size /
                            2f

                if (
                    sourceCenter >
                    rightCenter
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

                    working =
                        next

                    /*
                     * Layout moves the item automatically.
                     * Counter that movement so it stays with finger.
                     */
                    dragOffset -=
                        rightInfo.size +
                            gapPx

                    return
                }
            }
        }

        /*
         * Move LEFT.
         */
        if (
            sourceIndex >
            0
        ) {
            val leftId =
                working[
                    sourceIndex - 1
                ]

            val leftInfo =
                state.layoutInfo
                    .visibleItemsInfo
                    .firstOrNull {
                        it.key ==
                            "drag_$leftId"
                    }

            if (
                leftInfo != null
            ) {
                val leftCenter =
                    leftInfo.offset +
                        leftInfo.size /
                            2f

                if (
                    sourceCenter <
                    leftCenter
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

                    working =
                        next

                    dragOffset +=
                        leftInfo.size +
                            gapPx
                }
            }
        }
    }

    LazyRow(
        state =
            state,

        modifier =
            Modifier
                .fillMaxWidth()
                .height(44.dp),

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
                )
    ) {
        /*
         * ALL
         * permanently fixed.
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
         * Reorderable middle categories.
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
                        if (moving)
                            100f
                        else
                            0f
                    )
                    .graphicsLayer {
                        translationX =
                            if (
                                moving
                            ) {
                                dragOffset
                            } else {
                                0f
                            }

                        val scale =
                            if (
                                moving
                            ) {
                                1.09f
                            } else {
                                1f
                            }

                        scaleX =
                            scale

                        scaleY =
                            scale
                    }
                    .pointerInput(
                        id
                    ) {
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
                                    state.layoutInfo
                                        .visibleItemsInfo
                                        .firstOrNull {
                                            it.key ==
                                                "drag_$id"
                                        }

                                if (
                                    current !=
                                    null
                                ) {
                                    val center =
                                        current.offset +
                                            current.size /
                                                2f +
                                            dragOffset

                                    val viewportStart =
                                        state.layoutInfo
                                            .viewportStartOffset
                                            .toFloat()

                                    val viewportEnd =
                                        state.layoutInfo
                                            .viewportEndOffset
                                            .toFloat()

                                    val goLeft =
                                        center <
                                            viewportStart +
                                                edgePx &&
                                            state
                                                .canScrollBackward

                                    val goRight =
                                        center >
                                            viewportEnd -
                                                edgePx &&
                                            state
                                                .canScrollForward

                                    if (
                                        goLeft ||
                                        goRight
                                    ) {
                                        val direction =
                                            if (
                                                goLeft
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
                                                        isActive
                                                    ) {
                                                        val consumed =
                                                            state.scrollBy(
                                                                direction *
                                                                    14f
                                                            )

                                                        /*
                                                         * Compensate scrolling
                                                         * under stationary finger.
                                                         */
                                                        dragOffset +=
                                                            consumed

                                                        swapIfRequired(
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
                                    1.dp,

                                    XmoRed.copy(
                                        alpha =
                                            .7f
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
                        select(
                            id
                        )
                    }
                }
            }
        }

        /*
         * ADD
         * permanently fixed final item.
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
 * ALL SONGS ARROW CONTROLLER
 * =============================================================
 *
 * Tap and hold are separate states.
 *
 * Tap:
 * exact one-column movement.
 *
 * Hold:
 * continuous pixel scrolling.
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

            if (
                section.id ==
                "albums" ||
                section.id ==
                "liked"
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
 * ARROW INPUT
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
                start = 7.dp
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

                        /*
                         * Only detects hold.
                         *
                         * It DOES NOT produce repeated
                         * "next column" requests.
                         */
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
                         * Release must stop fast scroll
                         * immediately.
                         */
                        controller
                            .stopFast()

                        /*
                         * Short press only:
                         * exactly one column.
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
 * ALL SONGS
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
         * Exactly four cards.
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

        /*
         * Complete 12-item visual pages.
         */
        val slotCount =
            (
                (songs.size + 11) /
                    12
                ) * 12

        /*
         * -----------------------------------------------------
         * TAP
         * -----------------------------------------------------
         *
         * One press = one horizontal column.
         */
        LaunchedEffect(
            tapRequest
        ) {
            if (
                tapRequest <= 0
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
         * HOLD
         * -----------------------------------------------------
         *
         * Fast continuous scrolling.
         *
         * No column-by-column animation.
         * No repeated arrow requests.
         */
        LaunchedEffect(
            fastScrolling
        ) {
            if (
                !fastScrolling
            ) {
                return@LaunchedEffect
            }

            /*
             * ~1100 px/sec at 60fps.
             *
             * Smooth + visibly fast without giant jumps.
             */
            val pixelsPerFrame =
                18f

            while (
                isActive &&
                arrow.fastScrolling
            ) {
                val consumed =
                    gridState.scrollBy(
                        pixelsPerFrame
                    )

                /*
                 * End reached.
                 */
                if (
                    abs(consumed) <
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
                 * LazyHorizontalGrid naturally fills:
                 *
                 * 0  3  6  9
                 * 1  4  7 10
                 * 2  5  8 11
                 *
                 * Map it to:
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
 * ALBUM
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
    /*
     * Don't rebuild grouping during normal Home scroll.
     */
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
