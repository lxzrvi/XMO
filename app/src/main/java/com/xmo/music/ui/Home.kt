package com.xmo.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

private data class HomeSection(
    val id: String,
    val name: String,
    val icon: Int,
    val color: androidx.compose.ui.graphics.Color = XmoRed
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
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val built = listOf(
        HomeSection("songs", "All Songs", R.drawable.ic_xmo_songs),
        HomeSection("albums", "Albums", R.drawable.ic_xmo_album),
        HomeSection("liked", "Liked Songs", R.drawable.ic_xmo_heart),
        HomeSection("artists", "Artists", R.drawable.ic_xmo_artist)
    )

    val customIcons = listOf(
        R.drawable.ic_xmo_star,
        R.drawable.ic_xmo_spark,
        R.drawable.ic_xmo_diamond,
        R.drawable.ic_xmo_bolt
    )

    val customColors = listOf(
        androidx.compose.ui.graphics.Color(0xFFFFC107),
        androidx.compose.ui.graphics.Color(0xFFAF52DE),
        androidx.compose.ui.graphics.Color(0xFF00AEEF),
        androidx.compose.ui.graphics.Color(0xFFFF7043)
    )

    val custom = categories.map {
        HomeSection(
            it.id,
            it.name,
            customIcons[it.icon.mod(customIcons.size)],
            customColors[it.icon.mod(customColors.size)]
        )
    }

    val map = (built + custom).associateBy { it.id }
    val currentOrder =
        order.filter(map::containsKey) +
            map.keys.filterNot(order::contains)

    var selected by remember { mutableStateOf("all") }
    var addDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            state = state,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 360.dp)
        ) {
            item("header") {
                Box(
                    Modifier
                        .statusBarsPadding()
                        .padding(top = 4.dp)
                ) {
                    HomeHeader(
                        c,
                        theme,
                        setTheme,
                        refresh
                    )
                }
            }

            stickyHeader("categories") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg.copy(.96f))
                        .statusBarsPadding()
                ) {
                    CategoryBar(
                        sections = currentOrder.mapNotNull(map::get),
                        selected = selected,
                        c = c,
                        onSelect = { id ->
                            selected = id

                            if (id == "all") {
                                scope.launch {
                                    state.animateScrollToItem(0)
                                }
                            } else {
                                val i = currentOrder.indexOf(id)
                                if (i >= 0) {
                                    scope.launch {
                                        state.animateScrollToItem(
                                            index = 3 + i
                                        )
                                    }
                                }
                            }
                        },
                        onMove = { from, to ->
                            val next = currentOrder.toMutableList()
                            val moving = next.removeAt(from)
                            next.add(to, moving)
                            saveOrder(next)
                        },
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
                            bottom = 16.dp
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
                            .height(105.dp),
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

            itemsIndexed(
                currentOrder,
                key = { _, id -> id }
            ) { index, id ->
                map[id]?.let { section ->
                    ReorderSection(
                        section = section,
                        index = index,
                        count = currentOrder.size,
                        c = c,
                        onMove = { from, to ->
                            val next =
                                currentOrder.toMutableList()

                            val moving =
                                next.removeAt(from)

                            next.add(to, moving)
                            saveOrder(next)
                        }
                    ) {
                        when (id) {
                            "songs" ->
                                SongsSection(
                                    songs,
                                    allowed,
                                    c,
                                    theme
                                )

                            "albums" ->
                                AlbumsSection(songs, c)

                            "liked" ->
                                LikedSection(c)

                            "artists" ->
                                ArtistsSection(
                                    songs,
                                    c
                                )

                            else -> {
                                val cat =
                                    categories.find {
                                        it.id == id
                                    }

                                CustomSection(
                                    songs.filter {
                                        it.id in (
                                            cat?.songIds
                                                ?: emptySet()
                                            )
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
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 58.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "XMO",
                        color = c.text,
                        fontFamily = XmoFont.logo,
                        fontSize = 20.sp
                    )

                    Text(
                        "lxzrvi  •  copyright © 2026",
                        color = c.sub,
                        fontFamily = XmoFont.thin,
                        fontSize = 9.sp,
                        modifier = Modifier.padding(top = 3.dp)
                    )

                    // Allows the last section to reach sticky-nav top.
                    Spacer(Modifier.height(320.dp))
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
                    fontFamily = XmoFont.bold
                )
            },
            text = {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it.take(24)
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
                            val cat = UserCategory(
                                id = "cat_${UUID.randomUUID()}",
                                name = name,
                                icon = categories.size % 4
                            )

                            saveCategories(
                                categories + cat
                            )

                            saveOrder(
                                currentOrder + cat.id
                            )

                            categoryName = ""
                            addDialog = false
                        }
                    }
                ) {
                    Text("Add", color = XmoRed)
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
private fun CategoryBar(
    sections: List<HomeSection>,
    selected: String,
    c: HomeColors,
    onSelect: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onAdd: () -> Unit
) {
    val scroll = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    var dragging by remember {
        mutableIntStateOf(-1)
    }

    var dragX by remember {
        mutableFloatStateOf(0f)
    }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(
                start = 14.dp,
                top = 8.dp,
                end = 14.dp,
                bottom = 10.dp
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

        sections.forEachIndexed { index, section ->
            Box(
                Modifier
                    .graphicsLayer {
                        translationX =
                            if (dragging == index)
                                dragX
                            else 0f

                        scaleX =
                            if (dragging == index)
                                1.06f
                            else 1f

                        scaleY =
                            if (dragging == index)
                                1.06f
                            else 1f
                    }
                    .pointerInput(
                        sections.map { it.id },
                        index
                    ) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragging = index
                                dragX = 0f

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },

                            onDrag = { change, amount ->
                                change.consume()
                                dragX += amount.x
                            },

                            onDragEnd = {
                                val step =
                                    105f * density

                                val target = (
                                    index +
                                        (dragX / step)
                                            .roundToInt()
                                    ).coerceIn(
                                    sections.indices
                                )

                                if (target != index) {
                                    onMove(
                                        index,
                                        target
                                    )
                                }

                                dragging = -1
                                dragX = 0f
                            },

                            onDragCancel = {
                                dragging = -1
                                dragX = 0f
                            }
                        )
                    }
            ) {
                CategoryChip(
                    text = section.name,
                    active = selected == section.id,
                    c = c,
                    icon = section.icon,
                    tint = section.color
                ) {
                    if (dragging < 0) {
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
    count: Int,
    c: HomeColors,
    onMove: (Int, Int) -> Unit,
    body: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var lifted by remember {
        mutableStateOf(false)
    }

    var dragY by remember {
        mutableFloatStateOf(0f)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 9.dp
            )
            .graphicsLayer {
                translationY =
                    if (lifted) dragY else 0f

                scaleX =
                    if (lifted) 1.015f else 1f

                scaleY =
                    if (lifted) 1.015f else 1f

                alpha =
                    if (lifted) .94f else 1f
            }
            .animateContentSize()
    ) {
        // Gesture ONLY on the section label.
        Box(
            Modifier.pointerInput(
                section.id,
                index
            ) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        lifted = true
                        dragY = 0f

                        haptic.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                    },

                    onDrag = { change, amount ->
                        change.consume()
                        dragY += amount.y
                    },

                    onDragEnd = {
                        val step =
                            145f * density

                        val target = (
                            index +
                                (dragY / step)
                                    .roundToInt()
                            ).coerceIn(
                            0,
                            count - 1
                        )

                        if (target != index) {
                            onMove(index, target)
                        }

                        lifted = false
                        dragY = 0f
                    },

                    onDragCancel = {
                        lifted = false
                        dragY = 0f
                    }
                )
            }
        ) {
            SectionTitle(
                title = section.name,
                subtitle = "",
                icon = section.icon,
                c = c
            )
        }

        AnimatedVisibility(
            visible = !lifted
        ) {
            body()
        }
    }
}

@Composable
private fun SongsSection(
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

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(end = 10.dp),
            horizontalArrangement =
                Arrangement.End
        ) {
            Box(
                Modifier
                    .size(28.dp)
                    .background(
                        XmoRed.copy(.18f),
                        CircleShape
                    )
                    .clickable {
                        scope.launch {
                            scroll.animateScrollTo(
                                (scroll.value + 100)
                                    .coerceAtMost(
                                        scroll.maxValue
                                    )
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                XmoIcon(
                    R.drawable.ic_xmo_arrow,
                    XmoRed,
                    Modifier.size(13.dp)
                )
            }
        }

        BoxWithConstraints(
            Modifier.fillMaxWidth()
        ) {
            val pageWidth = maxWidth
            val pages = songs.chunked(12)

            Row(
                Modifier.horizontalScroll(scroll)
            ) {
                pages.forEachIndexed {
                        pageIndex,
                        page ->

                    Column(
                        Modifier
                            .width(pageWidth)
                            .padding(4.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(4) { column ->
                                    val local =
                                        row * 4 + column

                                    Box(
                                        Modifier.weight(1f)
                                    ) {
                                        page.getOrNull(local)
                                            ?.let { song ->
                                                SongTile(
                                                    song,
                                                    pageIndex * 12 +
                                                        local,
                                                    c,
                                                    theme
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
}

@Composable
private fun AlbumsSection(
    songs: List<Song>,
    c: HomeColors
) {
    Column {
        SectionTitle(
            title = "",
            subtitle =
                "${Library.albums(songs).size} albums",
            icon = R.drawable.ic_xmo_album,
            c = c,
            action = R.drawable.ic_xmo_add
        )

        if (songs.isEmpty()) {
            Empty("No albums found", c)
        }
    }
}

@Composable
private fun LikedSection(
    c: HomeColors
) {
    Column {
        SectionTitle(
            title = "",
            subtitle = "0 favorites",
            icon = R.drawable.ic_xmo_heart,
            c = c,
            action = R.drawable.ic_xmo_add
        )

        Empty("No liked songs yet", c)
    }
}

@Composable
private fun ArtistsSection(
    songs: List<Song>,
    c: HomeColors
) {
    val artists =
        Library.artists(songs)

    if (artists.isEmpty()) {
        Empty("No artists found", c)
        return
    }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 5.dp,
                vertical = 5.dp
            ),
        horizontalArrangement =
            Arrangement.spacedBy(10.dp)
    ) {
        artists.forEach { artist ->
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
                        Modifier.padding(top = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomSection(
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

    val rows = songs.chunked(6)

    Column(
        verticalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        rows.forEachIndexed { row, items ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(5.dp)
            ) {
                repeat(6) { column ->
                    Box(
                        Modifier.weight(1f)
                    ) {
                        items.getOrNull(column)
                            ?.let { song ->
                                SongTile(
                                    song,
                                    row * 6 + column,
                                    c,
                                    theme
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
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = c.sub,
            fontFamily = XmoFont.normal,
            fontSize = 12.sp
        )
    }
}
