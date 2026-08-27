package com.xmo.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
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

private data class HomeSection(
    val id: String,
    val name: String,
    val icon: Int,
    val color: Color = XmoRed
)

private object SongArrow {
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
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val fixed = remember {
        listOf(
            HomeSection(
                "songs",
                "All Songs",
                R.drawable.ic_xmo_songs
            ),
            HomeSection(
                "albums",
                "Albums",
                R.drawable.ic_xmo_album
            ),
            HomeSection(
                "liked",
                "Liked Songs",
                R.drawable.ic_xmo_heart
            ),
            HomeSection(
                "artists",
                "Artists",
                R.drawable.ic_xmo_artist
            )
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

    val customColors = remember {
        listOf(
            Color(0xFFFFC107),
            Color(0xFFAF52DE),
            Color(0xFF00AEEF),
            Color(0xFFFF7043)
        )
    }

    val custom = categories.map {
        val i = it.icon.mod(4)

        HomeSection(
            id = it.id,
            name = it.name,
            icon = customIcons[i],
            color = customColors[i]
        )
    }

    val sectionMap =
        (fixed + custom).associateBy {
            it.id
        }

    val resolvedOrder =
        order.filter(sectionMap::containsKey) +
            sectionMap.keys.filterNot(order::contains)

    var previewOrder by remember {
        mutableStateOf(resolvedOrder)
    }

    LaunchedEffect(resolvedOrder) {
        previewOrder = resolvedOrder
    }

    var selected by remember {
        mutableStateOf("all")
    }

    var addDialog by remember {
        mutableStateOf(false)
    }

    var categoryName by remember {
        mutableStateOf("")
    }

    /*
     * As soon as Home leaves absolute top,
     * profile area collapses while category
     * strip remains pinned.
     */
    val headerVisible by remember {
        derivedStateOf {
            list.firstVisibleItemIndex == 0 &&
                list.firstVisibleItemScrollOffset < 28
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = list,
            modifier = Modifier.fillMaxSize()
        ) {
            stickyHeader(
                key = "homeDock"
            ) {
                HomeDock(
                    showProfile = headerVisible,
                    sections = previewOrder
                        .mapNotNull(sectionMap::get),
                    order = previewOrder,
                    selected = selected,
                    c = c,
                    theme = theme,
                    setTheme = setTheme,
                    refresh = refresh,

                    onSelect = { id ->
                        selected = id

                        scope.launch {
                            if (id == "all") {
                                list.animateScrollToItem(
                                    index = 0
                                )
                            } else {
                                val position =
                                    previewOrder
                                        .indexOf(id)

                                if (position >= 0) {
                                    /*
                                     * 0 dock
                                     * 1 recent
                                     * 2+ sections
                                     *
                                     * Sticky header remains
                                     * above this target.
                                     */
                                    list.animateScrollToItem(
                                        index =
                                            position + 2,
                                        scrollOffset = -8
                                    )
                                }
                            }
                        }
                    },

                    onPreview = {
                        previewOrder = it
                    },

                    onCommit = {
                        previewOrder = it
                        saveOrder(it)
                    },

                    onAdd = {
                        addDialog = true
                    }
                )
            }

            item(
                key = "recent"
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            top = 12.dp,
                            end = 12.dp,
                            bottom = 16.dp
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

            itemsIndexed(
                items = previewOrder,
                key = { _, id -> id }
            ) { index, id ->

                sectionMap[id]?.let {
                        section ->

                    SectionItem(
                        section = section,
                        index = index,
                        order = previewOrder,
                        c = c,

                        subtitle = when (id) {
                            "songs" ->
                                "All songs: ${songs.size}"

                            "albums" ->
                                "${Library.albums(songs).size} albums"

                            "liked" ->
                                "0 favorites"

                            else -> ""
                        },

                        action = when (id) {
                            "albums",
                            "liked" ->
                                R.drawable.ic_xmo_add

                            else -> null
                        },

                        arrow =
                            id == "songs",

                        onPreview = {
                            previewOrder = it
                        },

                        onCommit = {
                            previewOrder = it
                            saveOrder(it)
                        }
                    ) {
                        when (id) {
                            "songs" ->
                                Songs(
                                    songs,
                                    allowed,
                                    c,
                                    theme
                                )

                            "albums" ->
                                Albums(
                                    songs,
                                    c
                                )

                            "liked" ->
                                Liked(c)

                            "artists" ->
                                Artists(
                                    songs,
                                    c
                                )

                            else -> {
                                val ids =
                                    categories
                                        .find {
                                            it.id == id
                                        }
                                        ?.songIds
                                        ?: emptySet()

                                CustomSongs(
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
             * Large final viewport is intentional:
             * last category can also reach the same
             * top position underneath sticky dock.
             */
            item(
                key = "footer"
            ) {
                BoxWithConstraints(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            maxHeight.coerceAtLeast(
                                620.dp
                            )
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
                categoryName = ""
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
                    value = categoryName,
                    onValueChange = {
                        categoryName =
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
                            categoryName.trim()

                        if (
                            name.isNotEmpty()
                        ) {
                            val category =
                                UserCategory(
                                    id =
                                        "cat_${UUID.randomUUID()}",
                                    name = name,
                                    icon =
                                        categories.size % 4
                                )

                            saveCategories(
                                categories +
                                    category
                            )

                            val next =
                                previewOrder +
                                    category.id

                            previewOrder =
                                next

                            saveOrder(next)

                            categoryName = ""
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
                        categoryName = ""
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
    showProfile: Boolean,
    sections: List<HomeSection>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit,
    onSelect: (String) -> Unit,
    onPreview: (List<String>) -> Unit,
    onCommit: (List<String>) -> Unit,
    onAdd: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.bg.copy(.97f))
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .animateContentSize(
                spring(
                    dampingRatio = .86f,
                    stiffness = 650f
                )
            )
    ) {
        AnimatedVisibility(
            visible = showProfile,
            enter =
                fadeIn() +
                    slideInVertically {
                        -it / 2
                    },
            exit =
                fadeOut() +
                    slideOutVertically {
                        -it
                    }
        ) {
            HomeHeader(
                c = c,
                theme = theme,
                setTheme = setTheme,
                refresh = refresh
            )
        }

        CategoryBar(
            sections = sections,
            order = order,
            selected = selected,
            c = c,
            onSelect = onSelect,
            onPreview = onPreview,
            onCommit = onCommit,
            onAdd = onAdd
        )
    }
}

@Composable
private fun CategoryBar(
    sections: List<HomeSection>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    onSelect: (String) -> Unit,
    onPreview: (List<String>) -> Unit,
    onCommit: (List<String>) -> Unit,
    onAdd: () -> Unit
) {
    val haptic =
        LocalHapticFeedback.current

    val density =
        LocalDensity.current

    var dragged by remember {
        mutableStateOf<String?>(null)
    }

    var dx by remember {
        mutableFloatStateOf(0f)
    }

    var dragOrder by remember(order) {
        mutableStateOf(order)
    }

    LaunchedEffect(order) {
        if (dragged == null) {
            dragOrder = order
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                )
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 8.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                text = "All",
                active =
                    selected == "all",
                c = c,
                icon =
                    R.drawable.ic_xmo_all
            ) {
                onSelect("all")
            }

            dragOrder.forEach { id ->
                val section =
                    sections
                        .firstOrNull {
                            it.id == id
                        }
                        ?: return@forEach

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
                                    1.07f
                                else 1f

                            scaleY =
                                if (moving)
                                    1.07f
                                else 1f

                            alpha =
                                if (moving)
                                    .95f
                                else 1f
                        }
                        .then(
                            if (moving) {
                                Modifier
                                    .background(
                                        XmoRed.copy(.10f),
                                        RoundedCornerShape(
                                            18.dp
                                        )
                                    )
                                    .border(
                                        .8.dp,
                                        XmoRed.copy(.55f),
                                        RoundedCornerShape(
                                            18.dp
                                        )
                                    )
                            } else Modifier
                        )
                        .pointerInput(
                            id,
                            dragOrder
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

                                    dx +=
                                        amount.x

                                    val from =
                                        dragOrder
                                            .indexOf(
                                                id
                                            )

                                    if (
                                        from < 0
                                    ) {
                                        return@detectDragGesturesAfterLongPress
                                    }

                                    /*
                                     * Chip widths vary, so use
                                     * a conservative crossing
                                     * threshold. The list itself
                                     * reorders immediately,
                                     * making neighbors create
                                     * the destination slot.
                                     */
                                    val threshold =
                                        with(
                                            density
                                        ) {
                                            52.dp
                                                .toPx()
                                        }

                                    if (
                                        dx >
                                        threshold &&
                                        from <
                                        dragOrder
                                            .lastIndex
                                    ) {
                                        val next =
                                            dragOrder
                                                .toMutableList()

                                        val item =
                                            next
                                                .removeAt(
                                                    from
                                                )

                                        next.add(
                                            from + 1,
                                            item
                                        )

                                        dragOrder =
                                            next

                                        onPreview(
                                            next
                                        )

                                        dx -=
                                            threshold
                                    } else if (
                                        dx <
                                        -threshold &&
                                        from > 0
                                    ) {
                                        val next =
                                            dragOrder
                                                .toMutableList()

                                        val item =
                                            next
                                                .removeAt(
                                                    from
                                                )

                                        next.add(
                                            from - 1,
                                            item
                                        )

                                        dragOrder =
                                            next

                                        onPreview(
                                            next
                                        )

                                        dx +=
                                            threshold
                                    }
                                },

                                onDragEnd = {
                                    dragged = null
                                    dx = 0f

                                    onCommit(
                                        dragOrder
                                    )
                                },

                                onDragCancel = {
                                    dragged = null
                                    dx = 0f

                                    dragOrder =
                                        order

                                    onPreview(
                                        order
                                    )
                                }
                            )
                        }
                ) {
                    CategoryChip(
                        text =
                            section.name,
                        active =
                            selected ==
                                section.id,
                        c = c,
                        icon =
                            section.icon,
                        tint =
                            section.color
                    ) {
                        if (
                            dragged ==
                            null
                        ) {
                            onSelect(
                                section.id
                            )
                        }
                    }
                }
            }

            CategoryChip(
                text = "Add",
                active = false,
                c = c,
                icon =
                    R.drawable.ic_xmo_add,
                tint = XmoRed,
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun SectionItem(
    section: HomeSection,
    index: Int,
    order: List<String>,
    c: HomeColors,
    subtitle: String,
    action: Int?,
    arrow: Boolean,
    onPreview: (List<String>) -> Unit,
    onCommit: (List<String>) -> Unit,
    body: @Composable () -> Unit
) {
    val haptic =
        LocalHapticFeedback.current

    val density =
        LocalDensity.current

    var lifted by remember {
        mutableStateOf(false)
    }

    var dy by remember {
        mutableFloatStateOf(0f)
    }

    var dragOrder by remember(order) {
        mutableStateOf(order)
    }

    LaunchedEffect(order) {
        if (!lifted) {
            dragOrder = order
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp)
            .animateContentSize(
                spring(
                    dampingRatio = .86f,
                    stiffness = 600f
                )
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
            /*
             * Only icon + title text block
             * owns the long-press gesture.
             */
            Row(
                Modifier
                    .weight(1f)
                    .graphicsLayer {
                        translationY =
                            if (lifted)
                                dy
                            else 0f

                        scaleX =
                            if (lifted)
                                1.02f
                            else 1f

                        scaleY =
                            if (lifted)
                                1.02f
                            else 1f
                    }
                    .then(
                        if (lifted) {
                            Modifier
                                .background(
                                    c.surface,
                                    RoundedCornerShape(
                                        12.dp
                                    )
                                )
                                .border(
                                    .9.dp,
                                    XmoRed.copy(.58f),
                                    RoundedCornerShape(
                                        12.dp
                                    )
                                )
                        } else Modifier
                    )
                    .pointerInput(
                        section.id,
                        dragOrder
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                lifted = true
                                dy = 0f
                                dragOrder =
                                    order

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                dy +=
                                    amount.y

                                val from =
                                    dragOrder
                                        .indexOf(
                                            section.id
                                        )

                                if (
                                    from < 0
                                ) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                val threshold =
                                    with(
                                        density
                                    ) {
                                        72.dp
                                            .toPx()
                                    }

                                if (
                                    dy >
                                    threshold &&
                                    from <
                                    dragOrder
                                        .lastIndex
                                ) {
                                    val next =
                                        dragOrder
                                            .toMutableList()

                                    val item =
                                        next
                                            .removeAt(
                                                from
                                            )

                                    next.add(
                                        from + 1,
                                        item
                                    )

                                    dragOrder =
                                        next

                                    onPreview(
                                        next
                                    )

                                    dy -=
                                        threshold
                                } else if (
                                    dy <
                                    -threshold &&
                                    from > 0
                                ) {
                                    val next =
                                        dragOrder
                                            .toMutableList()

                                    val item =
                                        next
                                            .removeAt(
                                                from
                                            )

                                    next.add(
                                        from - 1,
                                        item
                                    )

                                    dragOrder =
                                        next

                                    onPreview(
                                        next
                                    )

                                    dy +=
                                        threshold
                                }
                            },

                            onDragEnd = {
                                lifted = false
                                dy = 0f

                                onCommit(
                                    dragOrder
                                )
                            },

                            onDragCancel = {
                                lifted = false
                                dy = 0f

                                onPreview(
                                    order
                                )
                            }
                        )
                    },
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                XmoIcon(
                    section.icon,
                    XmoRed,
                    Modifier.size(
                        17.dp
                    )
                )

                Column(
                    Modifier.padding(
                        start = 8.dp,
                        top = 8.dp,
                        end = 8.dp,
                        bottom = 8.dp
                    )
                ) {
                    Text(
                        section.name,
                        color = c.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 17.sp
                    )

                    if (
                        subtitle.isNotBlank()
                    ) {
                        Text(
                            subtitle,
                            color = c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            /*
             * Actions are outside pointerInput:
             * long-holding + / arrow never
             * initiates section reorder.
             */
            action?.let {
                Box(
                    Modifier
                        .padding(
                            start = 6.dp
                        )
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
                        Modifier.size(
                            14.dp
                        )
                    )
                }
            }

            if (arrow) {
                SongArrowButton()
            }
        }

        AnimatedVisibility(
            visible = !lifted,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            /*
             * Content intentionally full width.
             * It does NOT inherit title padding.
             */
            Box(
                Modifier.fillMaxWidth()
            ) {
                body()
            }
        }

        if (lifted) {
            Box(
                Modifier
                    .padding(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    )
                    .fillMaxWidth()
                    .height(5.dp)
                    .background(
                        XmoRed.copy(.25f),
                        RoundedCornerShape(
                            50
                        )
                    )
            )
        }
    }
}

@Composable
private fun SongArrowButton() {
    val scope =
        rememberCoroutineScope()

    Box(
        Modifier
            .padding(start = 6.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(
                XmoRed.copy(.18f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        var holding =
                            false

                        val job =
                            scope.launch {
                                delay(250)

                                holding =
                                    true

                                while (
                                    holding
                                ) {
                                    SongArrow
                                        .tick++

                                    delay(55)
                                }
                            }

                        val released =
                            tryAwaitRelease()

                        val wasHold =
                            holding

                        holding =
                            false

                        job.cancel()

                        if (
                            released &&
                            !wasHold
                        ) {
                            SongArrow.tick++
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
            Modifier.size(13.dp)
        )
    }
}

@Composable
private fun Songs(
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

    val scroll =
        rememberScrollState()

    val tick =
        SongArrow.tick

    LaunchedEffect(tick) {
        if (tick > 0) {
            /*
             * One rendered column.
             */
            val column =
                (
                    scroll.maxValue /
                        ((songs.size + 2) / 3)
                            .coerceAtLeast(1)
                    )
                    .coerceAtLeast(1)

            scroll.animateScrollTo(
                (
                    scroll.value +
                        column
                    ).coerceAtMost(
                    scroll.maxValue
                )
            )
        }
    }

    /*
     * Prevent stale half-column offset after
     * screen recreation/tab recreation.
     */
    LaunchedEffect(songs.size) {
        if (
            scroll.value >
            scroll.maxValue
        ) {
            scroll.scrollTo(
                scroll.maxValue
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

        val usable =
            maxWidth -
                edge * 2

        val cardWidth =
            (
                usable -
                    gap * 3
                ) / 4

        val pageWidth =
            maxWidth

        Row(
            Modifier.horizontalScroll(
                scroll
            )
        ) {
            songs
                .chunked(12)
                .forEachIndexed {
                        page,
                        items ->

                    Column(
                        Modifier
                            .width(
                                pageWidth
                            )
                            .padding(
                                horizontal =
                                    edge,
                                vertical =
                                    4.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {
                        repeat(3) {
                                row ->

                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(
                                        gap
                                    )
                            ) {
                                repeat(4) {
                                        column ->

                                    val i =
                                        row * 4 +
                                            column

                                    Box(
                                        Modifier
                                            .width(
                                                cardWidth
                                            )
                                    ) {
                                        items
                                            .getOrNull(
                                                i
                                            )
                                            ?.let {
                                                SongTile(
                                                    song =
                                                        it,
                                                    index =
                                                        page *
                                                            12 +
                                                            i,
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
        }
    }
}

@Composable
private fun Albums(
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
            Modifier.height(6.dp)
        )
    }
}

@Composable
private fun Liked(
    c: HomeColors
) {
    Empty(
        "No liked songs yet",
        c
    )
}

@Composable
private fun Artists(
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
                                XmoRed.copy(
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
private fun CustomSongs(
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
                        Arrangement.spacedBy(
                            5.dp
                        )
                ) {
                    repeat(6) {
                            column ->

                        Box(
                            Modifier.weight(1f)
                        ) {
                            items
                                .getOrNull(
                                    column
                                )
                                ?.let {
                                    SongTile(
                                        song =
                                            it,
                                        index =
                                            row * 6 +
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
            color = c.sub,
            fontFamily =
                XmoFont.normal,
            fontSize = 12.sp
        )
    }
}
