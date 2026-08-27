package com.xmo.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

private data class HomeSection(
    val id: String,
    val name: String,
    val icon: Int,
    val color: Color = XmoRed
)

private object SongsArrow {
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

    val fixed = listOf(
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

    val customIcons = listOf(
        R.drawable.ic_xmo_star,
        R.drawable.ic_xmo_spark,
        R.drawable.ic_xmo_diamond,
        R.drawable.ic_xmo_bolt
    )

    val customColors = listOf(
        Color(0xFFFFC107),
        Color(0xFFAF52DE),
        Color(0xFF00AEEF),
        Color(0xFFFF7043)
    )

    val custom = categories.map {
        val i = it.icon.mod(4)

        HomeSection(
            it.id,
            it.name,
            customIcons[i],
            customColors[i]
        )
    }

    val map =
        (fixed + custom).associateBy {
            it.id
        }

    val actualOrder =
        order.filter(map::containsKey) +
            map.keys.filterNot(order::contains)

    var selected by remember {
        mutableStateOf("all")
    }

    var addDialog by remember {
        mutableStateOf(false)
    }

    var categoryName by remember {
        mutableStateOf("")
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(bottom = 180.dp)
        ) {
            item("header") {
                Box(
                    Modifier.windowInsetsPadding(
                        WindowInsets.statusBars
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

            stickyHeader("categories") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg.copy(.96f))
                        .windowInsetsPadding(
                            WindowInsets.statusBars
                        )
                ) {
                    CategoryBar(
                        sections =
                            actualOrder.mapNotNull(
                                map::get
                            ),
                        selected = selected,
                        c = c,
                        order = actualOrder,

                        onSelect = { id ->
                            selected = id

                            scope.launch {
                                if (id == "all") {
                                    state.animateScrollToItem(
                                        index = 0
                                    )
                                } else {
                                    val position =
                                        actualOrder.indexOf(id)

                                    if (position >= 0) {
                                        state.animateScrollToItem(
                                            index = 3 + position
                                        )
                                    }
                                }
                            }
                        },

                        onOrder = saveOrder,

                        onAdd = {
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
                            top = 12.dp,
                            end = 12.dp,
                            bottom = 14.dp
                        )
                ) {
                    SectionTitle(
                        title = "Recently Played",
                        subtitle = "0 tracks played",
                        icon =
                            R.drawable.ic_xmo_history,
                        c = c
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(105.dp),
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
                items = actualOrder,
                key = { _, id -> id }
            ) { index, id ->

                map[id]?.let { section ->
                    val subtitle = when (id) {
                        "songs" ->
                            "All songs: ${songs.size}"

                        "albums" ->
                            "${Library.albums(songs).size} albums"

                        "liked" ->
                            "0 favorites"

                        else -> ""
                    }

                    ReorderSection(
                        section = section,
                        index = index,
                        order = actualOrder,
                        c = c,
                        subtitle = subtitle,
                        action = when (id) {
                            "albums",
                            "liked" ->
                                R.drawable.ic_xmo_add

                            else -> null
                        },
                        saveOrder = saveOrder,
                        arrow =
                            id == "songs"
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

            item("footer") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(560.dp),
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
                        Text("Category name")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name =
                            categoryName.trim()

                        if (name.isNotEmpty()) {
                            val newCategory =
                                UserCategory(
                                    id =
                                        "cat_${UUID.randomUUID()}",
                                    name = name,
                                    icon =
                                        categories.size % 4
                                )

                            saveCategories(
                                categories +
                                    newCategory
                            )

                            saveOrder(
                                actualOrder +
                                    newCategory.id
                            )

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
                        addDialog = false
                        categoryName = ""
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
private fun CategoryBar(
    sections: List<HomeSection>,
    selected: String,
    c: HomeColors,
    order: List<String>,
    onSelect: (String) -> Unit,
    onOrder: (List<String>) -> Unit,
    onAdd: () -> Unit
) {
    val haptic =
        LocalHapticFeedback.current

    var working by remember(order) {
        mutableStateOf(order)
    }

    var held by remember {
        mutableStateOf<String?>(null)
    }

    var dx by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(order) {
        if (held == null) {
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
                start = 14.dp,
                top = 6.dp,
                end = 14.dp,
                bottom = 8.dp
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
            onSelect("all")
        }

        working.forEach { id ->
            val section =
                sections.firstOrNull {
                    it.id == id
                } ?: return@forEach

            val moving =
                held == id

            Box(
                Modifier
                    .animateContentSize(
                        animationSpec = spring()
                    )
                    .graphicsLayer {
                        translationX =
                            if (moving)
                                dx
                            else 0f

                        scaleX =
                            if (moving)
                                1.06f
                            else 1f

                        scaleY =
                            if (moving)
                                1.06f
                            else 1f
                    }
                    .then(
                        if (moving) {
                            Modifier.border(
                                1.dp,
                                XmoRed.copy(.62f),
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
                                held = id
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

                                val from =
                                    working.indexOf(id)

                                if (from < 0) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                val threshold =
                                    55f * density

                                if (
                                    dx > threshold &&
                                    from <
                                    working.lastIndex
                                ) {
                                    val next =
                                        working
                                            .toMutableList()

                                    val item =
                                        next.removeAt(
                                            from
                                        )

                                    next.add(
                                        from + 1,
                                        item
                                    )

                                    working = next
                                    dx -= threshold
                                } else if (
                                    dx < -threshold &&
                                    from > 0
                                ) {
                                    val next =
                                        working
                                            .toMutableList()

                                    val item =
                                        next.removeAt(
                                            from
                                        )

                                    next.add(
                                        from - 1,
                                        item
                                    )

                                    working = next
                                    dx += threshold
                                }
                            },

                            onDragEnd = {
                                held = null
                                dx = 0f
                                onOrder(working)
                            },

                            onDragCancel = {
                                held = null
                                dx = 0f
                                working = order
                            }
                        )
                    }
            ) {
                CategoryChip(
                    text = section.name,
                    active =
                        selected ==
                            section.id,
                    c = c,
                    icon = section.icon,
                    tint = section.color
                ) {
                    if (held == null) {
                        onSelect(section.id)
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
            onClick = onAdd
        )
    }
}

@Composable
private fun ReorderSection(
    section: HomeSection,
    index: Int,
    order: List<String>,
    c: HomeColors,
    subtitle: String,
    action: Int?,
    saveOrder: (List<String>) -> Unit,
    arrow: Boolean,
    body: @Composable () -> Unit
) {
    val haptic =
        LocalHapticFeedback.current

    var lifted by remember {
        mutableStateOf(false)
    }

    var dy by remember {
        mutableFloatStateOf(0f)
    }

    var working by remember(order) {
        mutableStateOf(order)
    }

    LaunchedEffect(order) {
        if (!lifted) {
            working = order
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .animateContentSize()
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            /*
             * Only this title block starts
             * section dragging.
             */
            Row(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
                    .graphicsLayer {
                        translationY =
                            if (lifted)
                                dy
                            else 0f

                        scaleX =
                            if (lifted)
                                1.015f
                            else 1f

                        scaleY =
                            if (lifted)
                                1.015f
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
                                    .8.dp,
                                    XmoRed.copy(.55f),
                                    RoundedCornerShape(
                                        12.dp
                                    )
                                )
                        } else Modifier
                    )
                    .pointerInput(
                        section.id,
                        order
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                lifted = true
                                dy = 0f
                                working = order

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()
                                dy += amount.y

                                val from =
                                    working.indexOf(
                                        section.id
                                    )

                                if (from < 0) {
                                    return@detectDragGesturesAfterLongPress
                                }

                                val threshold =
                                    72f * density

                                if (
                                    dy > threshold &&
                                    from <
                                    working.lastIndex
                                ) {
                                    val next =
                                        working
                                            .toMutableList()

                                    val item =
                                        next.removeAt(
                                            from
                                        )

                                    next.add(
                                        from + 1,
                                        item
                                    )

                                    working = next
                                    saveOrder(next)
                                    dy -= threshold
                                } else if (
                                    dy < -threshold &&
                                    from > 0
                                ) {
                                    val next =
                                        working
                                            .toMutableList()

                                    val item =
                                        next.removeAt(
                                            from
                                        )

                                    next.add(
                                        from - 1,
                                        item
                                    )

                                    working = next
                                    saveOrder(next)
                                    dy += threshold
                                }
                            },

                            onDragEnd = {
                                saveOrder(working)
                                lifted = false
                                dy = 0f
                            },

                            onDragCancel = {
                                lifted = false
                                dy = 0f
                            }
                        )
                    },
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                XmoIcon(
                    section.icon,
                    XmoRed,
                    Modifier.size(17.dp)
                )

                Column(
                    Modifier.padding(
                        start = 8.dp,
                        top = 9.dp,
                        end = 8.dp,
                        bottom = 9.dp
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

            action?.let {
                Box(
                    Modifier
                        .padding(end = 10.dp)
                        .size(28.dp)
                        .background(
                            XmoRed.copy(.18f),
                            CircleShape
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

        AnimatedVisibility(
            visible = !lifted
        ) {
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
                        horizontal = 12.dp
                    )
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(
                        XmoRed.copy(.24f),
                        RoundedCornerShape(50)
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
            .padding(end = 10.dp)
            .size(28.dp)
            .background(
                XmoRed.copy(.18f),
                CircleShape
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
                                    SongsArrow.tick++
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
                            SongsArrow.tick++
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
        SongsArrow.tick

    LaunchedEffect(tick) {
        if (tick > 0) {
            scroll.animateScrollTo(
                (scroll.value + 96)
                    .coerceAtMost(
                        scroll.maxValue
                    )
            )
        }
    }

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val pageWidth =
            maxWidth

        val gap =
            8.dp

        val cardWidth =
            (pageWidth - gap * 3) / 4

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
                            .width(pageWidth)
                            .padding(
                                vertical = 4.dp
                            ),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {
                        repeat(3) { row ->
                            Row(
                                Modifier
                                    .width(
                                        pageWidth
                                    ),
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
                                        Modifier.width(
                                            cardWidth
                                        )
                                    ) {
                                        items
                                            .getOrNull(
                                                i
                                            )
                                            ?.let {
                                                SongTile(
                                                    song = it,
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
            ),
        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        Spacer(Modifier.width(5.dp))

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

        Spacer(Modifier.width(5.dp))
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
                                        Modifier
                                            .fillMaxWidth()
                                )
                            }
                    }
                }
            }

            Spacer(
                Modifier.height(6.dp)
            )
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
