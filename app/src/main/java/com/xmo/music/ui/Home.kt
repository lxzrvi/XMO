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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
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
import kotlin.math.roundToInt

private data class HSection(
    val id: String,
    val name: String,
    val icon: Int,
    val tint: Color = XmoRed
)

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
            val i = it.icon.mod(4)

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

    /*
     * Committed section order.
     *
     * This drives the expensive Home body only after a drop.
     * CategoryDragRow keeps its own lightweight preview order
     * while the finger is moving.
     */
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
     * Calculate expensive metadata once for a song-list change.
     */
    val albumCount = remember(songs) {
        Library.albums(songs).size
    }

    /*
     * Header has 76dp collapsible height.
     *
     * No spring is placed between finger-scroll and collapse.
     * That removes the rubber-band / delayed feeling on device.
     */
    val collapseDistancePx = with(density) {
        76.dp.toPx()
    }

    val headerProgress by remember {
        derivedStateOf {
            if (listState.firstVisibleItemIndex != 0) {
                0f
            } else {
                (
                    1f -
                        listState.firstVisibleItemScrollOffset /
                        collapseDistancePx
                    ).coerceIn(0f, 1f)
            }
        }
    }

    /*
     * Category row itself is about 43dp high:
     * 34dp chip + vertical row padding.
     *
     * The list section is positioned below this overlay.
     */
    val pinnedDockHeight = 44.dp

    val pinnedDockPx = with(density) {
        pinnedDockHeight.roundToPx()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize()
        ) {
            /*
             * Expanded dock occupies:
             * status-bar inset + 76dp header + 44dp categories.
             *
             * While this spacer scrolls away, fixed HomeDock
             * independently fades/translates its header.
             */
            item(key = "top") {
                Spacer(
                    Modifier
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .height(120.dp)
                )
            }

            item(key = "recent") {
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
                items = visualOrder,
                key = { "sec_$it" }
            ) { id ->

                val section = sectionMap[id]
                    ?: return@items

                HomeSectionBlock(
                    section = section,
                    subtitle = when (id) {
                        "songs" ->
                            "All songs: ${songs.size}"

                        "albums" ->
                            "$albumCount albums"

                        "liked" ->
                            "0 favorites"

                        else ->
                            ""
                    },
                    c = c,
                    action = when (id) {
                        "albums", "liked" ->
                            R.drawable.ic_xmo_add

                        else ->
                            null
                    },
                    showArrow = id == "songs"
                ) { arrowRequests ->

                    when (id) {
                        "songs" -> {
                            SongsGrid(
                                songs = songs,
                                allowed = allowed,
                                c = c,
                                theme = theme,
                                arrowRequests = arrowRequests
                            )
                        }

                        "albums" -> {
                            AlbumBody(
                                songs = songs,
                                c = c
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
                                songs = songs,
                                c = c
                            )
                        }

                        else -> {
                            val ids = categories
                                .firstOrNull {
                                    it.id == id
                                }
                                ?.songIds
                                ?: emptySet()

                            val customSongs = remember(
                                songs,
                                ids
                            ) {
                                songs.filter {
                                    it.id in ids
                                }
                            }

                            CustomBody(
                                songs = customSongs,
                                c = c,
                                theme = theme
                            )
                        }
                    }
                }
            }

            /*
             * Full trailing viewport intentionally remains.
             * Therefore the final section can also be aligned
             * below the pinned category row.
             */
            item(key = "brand") {
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
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
            }
        }

        HomeDock(
            profile = headerProgress,
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
                        listState.animateScrollToItem(0)
                    } else {
                        /*
                         * Index:
                         * 0 = spacer
                         * 1 = Recently Played
                         * 2+ = reordered sections
                         *
                         * Positive offset leaves room for
                         * the fixed category overlay.
                         */
                        val position = visualOrder.indexOf(id)

                        if (position >= 0) {
                            listState.animateScrollToItem(
                                index = position + 2,
                                scrollOffset = pinnedDockPx
                            )
                        }
                    }
                }
            },
            commit = { next ->
                /*
                 * Body and categories switch to identical
                 * order once, at the actual drop.
                 */
                visualOrder = next
                saveOrder(next)
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
                        val name = newName.trim()

                        if (name.isNotEmpty()) {
                            val cat = UserCategory(
                                id = "cat_${UUID.randomUUID()}",
                                name = name,
                                icon = categories.size % 4
                            )

                            val next = visualOrder + cat.id

                            saveCategories(categories + cat)
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
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.bg.copy(alpha = .97f))
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        /*
         * The header loses physical height at exactly the same
         * rate as scroll. Its contents also move upward and fade.
         */
        Box(
            Modifier
                .fillMaxWidth()
                .height(76.dp * profile)
        ) {
            if (profile > 0.001f) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = profile

                            translationY =
                                -(1f - profile) *
                                    24.dp.toPx()
                        }
                ) {
                    HomeHeader(
                        c = c,
                        theme = theme,
                        setTheme = setTheme,
                        refresh = refresh
                    )
                }
            }
        }

        CategoryDragRow(
            sections = sections,
            order = order,
            selected = selected,
            c = c,
            select = select,
            commit = commit,
            add = add
        )
    }
}

private data class ChipMeasure(
    val width: IntSize = IntSize.Zero
)

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
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    val density = LocalDensity.current

    val gapPx = with(density) {
        8.dp.toPx()
    }

    val edgeZonePx = with(density) {
        72.dp.toPx()
    }

    var rowWidthPx by remember {
        mutableFloatStateOf(0f)
    }

    var working by remember(order) {
        mutableStateOf(order)
    }

    var draggingId by remember {
        mutableStateOf<String?>(null)
    }

    var dragTranslationX by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Widths are measured from the real chips.
     * Reordering no longer assumes every category is 48dp.
     */
    val widths = remember {
        mutableStateMapOf<String, Float>()
    }

    var autoScrollJob by remember {
        mutableStateOf<Job?>(null)
    }

    LaunchedEffect(order) {
        if (draggingId == null) {
            working = order
        }
    }

    fun stopAutoScroll() {
        autoScrollJob?.cancel()
        autoScrollJob = null
    }

    /*
     * Calculates how far the moving item should be translated
     * back toward its finger after it swaps with a neighbour.
     */
    fun widthFor(id: String): Float {
        return widths[id] ?: with(density) {
            80.dp.toPx()
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .onGloballyPositioned {
                rowWidthPx = it.size.width.toFloat()
            }
            .horizontalScroll(
                state = scroll,
                enabled = draggingId == null
            )
            .padding(
                horizontal = 14.dp,
                vertical = 5.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            text = "All",
            active = selected == "all",
            c = c,
            icon = R.drawable.ic_xmo_all
        ) {
            if (draggingId == null) {
                select("all")
            }
        }

        working.forEach { id ->
            val section = sections.firstOrNull {
                it.id == id
            } ?: return@forEach

            val moving = draggingId == id

            Box(
                Modifier
                    .onGloballyPositioned {
                        widths[id] =
                            it.size.width.toFloat()
                    }
                    .zIndex(
                        if (moving) 100f else 0f
                    )
                    .graphicsLayer {
                        translationX =
                            if (moving)
                                dragTranslationX
                            else
                                0f

                        val scale =
                            if (moving) 1.07f else 1f

                        scaleX = scale
                        scaleY = scale
                    }
                    .pointerInput(id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingId = id
                                dragTranslationX = 0f

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },
                            onDrag = { change, amount ->
                                change.consume()

                                dragTranslationX += amount.x

                                var current =
                                    working.indexOf(id)

                                if (current < 0) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                /*
                                 * Cross the REAL neighbour midpoint.
                                 *
                                 * Moving right:
                                 * half current + gap + half next.
                                 */
                                while (
                                    current < working.lastIndex
                                ) {
                                    val nextId =
                                        working[current + 1]

                                    val threshold =
                                        widthFor(id) / 2f +
                                            gapPx +
                                            widthFor(nextId) / 2f

                                    if (
                                        dragTranslationX <
                                        threshold
                                    ) {
                                        break
                                    }

                                    val next =
                                        working.toMutableList()

                                    next.add(
                                        current + 1,
                                        next.removeAt(current)
                                    )

                                    working = next

                                    /*
                                     * Layout itself moved the chip by this
                                     * distance; compensate so it stays
                                     * beneath the same finger.
                                     */
                                    dragTranslationX -=
                                        widthFor(nextId) +
                                            gapPx

                                    current++
                                }

                                /*
                                 * Moving left.
                                 */
                                while (current > 0) {
                                    val previousId =
                                        working[current - 1]

                                    val threshold =
                                        widthFor(id) / 2f +
                                            gapPx +
                                            widthFor(previousId) / 2f

                                    if (
                                        dragTranslationX >
                                        -threshold
                                    ) {
                                        break
                                    }

                                    val next =
                                        working.toMutableList()

                                    next.add(
                                        current - 1,
                                        next.removeAt(current)
                                    )

                                    working = next

                                    dragTranslationX +=
                                        widthFor(previousId) +
                                            gapPx

                                    current--
                                }

                                /*
                                 * Determine approximate moving-chip
                                 * viewport position for edge auto-scroll.
                                 */
                                val beforeWidth =
                                    working
                                        .take(
                                            working
                                                .indexOf(id)
                                                .coerceAtLeast(0)
                                        )
                                        .sumOf {
                                            widthFor(it)
                                                .toDouble()
                                        }
                                        .toFloat() +
                                        gapPx *
                                        working
                                            .indexOf(id)
                                            .coerceAtLeast(0)

                                val viewportX =
                                    beforeWidth -
                                        scroll.value +
                                        dragTranslationX

                                val goLeft =
                                    viewportX < edgeZonePx &&
                                        scroll.value > 0

                                val goRight =
                                    viewportX +
                                        widthFor(id) >
                                        rowWidthPx -
                                            edgeZonePx &&
                                        scroll.value <
                                        scroll.maxValue

                                if (goLeft || goRight) {
                                    if (
                                        autoScrollJob == null ||
                                        autoScrollJob?.isActive != true
                                    ) {
                                        autoScrollJob =
                                            scope.launch {
                                                while (isActive) {
                                                    val direction =
                                                        if (goLeft)
                                                            -1
                                                        else
                                                            1

                                                    val before =
                                                        scroll.value

                                                    scroll.scrollTo(
                                                        (
                                                            before +
                                                                direction *
                                                                12
                                                            ).coerceIn(
                                                            0,
                                                            scroll.maxValue
                                                        )
                                                    )

                                                    /*
                                                     * Keep visual finger tracking
                                                     * stable while content itself
                                                     * scrolls below the pointer.
                                                     */
                                                    val moved =
                                                        scroll.value -
                                                            before

                                                    dragTranslationX +=
                                                        moved.toFloat()

                                                    delay(16)
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
                                    working.toList()

                                draggingId = null
                                dragTranslationX = 0f

                                /*
                                 * One expensive Home reorder and one
                                 * persistence call, only on release.
                                 */
                                commit(final)
                            },
                            onDragCancel = {
                                stopAutoScroll()

                                draggingId = null
                                dragTranslationX = 0f
                                working = order
                            }
                        )
                    }
            ) {
                CategoryChip(
                    text = section.name,
                    active = selected == id,
                    c = c,
                    icon = section.icon,
                    tint = section.tint,
                    modifier =
                        if (moving) {
                            Modifier
                                .background(
                                    XmoRed.copy(alpha = .12f),
                                    RoundedCornerShape(18.dp)
                                )
                                .border(
                                    1.dp,
                                    XmoRed.copy(alpha = .65f),
                                    RoundedCornerShape(18.dp)
                                )
                        } else {
                            Modifier
                        }
                ) {
                    if (draggingId == null) {
                        select(id)
                    }
                }
            }
        }

        CategoryChip(
            text = "Add",
            active = false,
            c = c,
            icon = R.drawable.ic_xmo_add,
            tint = XmoRed,
            onClick = {
                if (draggingId == null) {
                    add()
                }
            }
        )
    }
}

/*
 * Arrow requests now belong to the All Songs section itself.
 * There is no app-global mutable Arrow singleton.
 */
@Stable
private class SongArrowRequests {
    var request by mutableIntStateOf(0)

    fun next() {
        request++
    }
}

@Composable
private fun HomeSectionBlock(
    section: HSection,
    subtitle: String,
    c: HomeColors,
    action: Int?,
    showArrow: Boolean,
    body: @Composable (SongArrowRequests?) -> Unit
) {
    val arrowRequests =
        if (showArrow) {
            remember {
                SongArrowRequests()
            }
        } else {
            null
        }

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
                modifier = Modifier.weight(1f)
            )

            action?.let {
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            XmoRed.copy(alpha = .18f)
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

            arrowRequests?.let {
                SongArrowButton(it)
            }
        }

        Spacer(
            Modifier.height(5.dp)
        )

        body(arrowRequests)
    }
}

@Composable
private fun SongArrowButton(
    requests: SongArrowRequests
) {
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .padding(start = 7.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(
                XmoRed.copy(alpha = .18f)
            )
            .pointerInput(requests) {
                detectTapGestures(
                    onPress = {
                        var repeating = false

                        val repeatJob =
                            scope.launch {
                                delay(250)
                                repeating = true

                                while (isActive) {
                                    requests.next()
                                    delay(65)
                                }
                            }

                        val released =
                            tryAwaitRelease()

                        repeatJob.cancel()

                        if (
                            released &&
                            !repeating
                        ) {
                            requests.next()
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
    theme: XmoTheme,
    arrowRequests: SongArrowRequests?
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

    val gridState =
        rememberLazyGridState()

    val request =
        arrowRequests?.request ?: 0

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge = 8.dp
        val gap = 8.dp

        /*
         * The viewport contains exactly:
         *
         * edge + 4 cards + 3 gaps + edge
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

        /*
         * Logical horizontal column count.
         */
        val columnCount =
            (songs.size + 2) / 3

        LaunchedEffect(request) {
            if (
                request > 0 &&
                columnCount > 1
            ) {
                /*
                 * LazyHorizontalGrid indexes cells column-major.
                 * Align to a column first, then advance exactly
                 * one column = three grid slots.
                 */
                val currentColumn =
                    gridState
                        .firstVisibleItemIndex / 3

                val targetColumn =
                    (currentColumn + 1)
                        .coerceAtMost(
                            columnCount - 1
                        )

                gridState.animateScrollToItem(
                    targetColumn * 3,
                    scrollOffset = 0
                )
            }
        }

        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            state = gridState,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(height),
            contentPadding =
                PaddingValues(horizontal = edge),
            horizontalArrangement =
                Arrangement.spacedBy(gap),
            verticalArrangement =
                Arrangement.spacedBy(gap)
        ) {
            /*
             * Keep complete page slots so row-major mapping
             * remains valid even on the final partial page.
             */
            val pageCount =
                (songs.size + 11) / 12

            val slots =
                pageCount * 12

            items(
                count = slots,
                key = { slot ->
                    "song_slot_$slot"
                }
            ) { slot ->

                /*
                 * LazyHorizontalGrid:
                 *
                 * slot positions:
                 * 0 3 6 9
                 * 1 4 7 10
                 * 2 5 8 11
                 *
                 * Required visible song numbering:
                 *
                 * 1  2  3  4
                 * 5  6  7  8
                 * 9 10 11 12
                 */
                val page = slot / 12
                val inside = slot % 12

                val row =
                    inside % 3

                val column =
                    inside / 3

                val sourceIndex =
                    page * 12 +
                        row * 4 +
                        column

                Box(
                    Modifier
                        .width(card)
                        .fillMaxHeight()
                ) {
                    songs
                        .getOrNull(sourceIndex)
                        ?.let { song ->
                            SongTile(
                                song = song,
                                index = sourceIndex,
                                c = c,
                                theme = theme,
                                modifier =
                                    Modifier
                                        .width(card)
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
    val artists = remember(songs) {
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
                                XmoRed.copy(alpha = .16f),
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
                            Modifier.padding(top = 5.dp)
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
        Modifier.padding(horizontal = 8.dp),
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        songs
            .chunked(6)
            .forEachIndexed { row, items ->

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
                                ?.let { song ->
                                    SongTile(
                                        song = song,
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
            fontFamily = XmoFont.normal,
            fontSize = 12.sp
        )
    }
}
