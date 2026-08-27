package com.xmo.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.shadow
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
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

private data class HomeSection(
    val id: String,
    val name: String,
    val icon: Int,
    val color: Color = XmoRed
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

    val base = listOf(
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

    val sections = (base + custom).associateBy { it.id }

    val actualOrder = order.filter(sections::containsKey) +
        sections.keys.filterNot(order::contains)

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
            contentPadding = PaddingValues(bottom = 250.dp)
        ) {
            item("status") {
                Spacer(Modifier.statusBarsPadding())
            }

            item("header") {
                HomeHeader(
                    c = c,
                    theme = theme,
                    setTheme = setTheme,
                    refresh = refresh
                )
            }

            stickyHeader("categories") {
                CategoryBar(
                    sections = actualOrder.mapNotNull(sections::get),
                    selected = selected,
                    c = c,
                    onSelect = { id ->
                        selected = id

                        scope.launch {
                            if (id == "all") {
                                state.animateScrollToItem(1)
                            } else {
                                val position = actualOrder.indexOf(id)

                                if (position >= 0) {
                                    // status + header + categories + recent
                                    state.animateScrollToItem(
                                        4 + position
                                    )
                                }
                            }
                        }
                    },
                    onOrder = saveOrder,
                    order = actualOrder,
                    onAdd = { addDialog = true }
                )
            }

            item("recent") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 12.dp,
                            top = 10.dp,
                            end = 12.dp,
                            bottom = 13.dp
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
                actualOrder,
                key = { _, id -> id }
            ) { index, id ->
                sections[id]?.let { section ->
                    ReorderSection(
                        section = section,
                        index = index,
                        order = actualOrder,
                        c = c,
                        saveOrder = saveOrder
                    ) {
                        when (id) {
                            "songs" -> Songs(
                                songs,
                                allowed,
                                c,
                                theme
                            )

                            "albums" -> Albums(
                                songs,
                                c
                            )

                            "liked" -> Liked(c)

                            "artists" -> Artists(
                                songs,
                                c
                            )

                            else -> {
                                val ids = categories
                                    .find { it.id == id }
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
                        .height(500.dp),
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
                        val name = categoryName.trim()

                        if (name.isNotEmpty()) {
                            val category = UserCategory(
                                id = "cat_${UUID.randomUUID()}",
                                name = name,
                                icon = categories.size % 4
                            )

                            saveCategories(categories + category)
                            saveOrder(actualOrder + category.id)

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
                    Text("Cancel", color = c.sub)
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
    val haptic = LocalHapticFeedback.current

    var dragging by remember { mutableStateOf<String?>(null) }
    var dragX by remember { mutableFloatStateOf(0f) }
    var working by remember(order) { mutableStateOf(order) }

    LaunchedEffect(order) {
        if (dragging == null) working = order
    }

    Box(
        Modifier
            .fillMaxWidth()
            .background(c.bg.copy(.96f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(
                    start = 14.dp,
                    top = 6.dp,
                    end = 14.dp,
                    bottom = 9.dp
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                "All",
                selected == "all",
                c,
                R.drawable.ic_xmo_all
            ) {
                onSelect("all")
            }

            working.forEachIndexed { index, id ->
                val section = sections.find { it.id == id }
                    ?: return@forEachIndexed

                val moving = dragging == id

                val gap by animateDpAsState(
                    if (moving) 3.dp else 0.dp,
                    spring(),
                    label = "catGap"
                )

                Box(
                    Modifier
                        .padding(horizontal = gap)
                        .shadow(
                            if (moving) 8.dp else 0.dp,
                            RoundedCornerShape(18.dp),
                            ambientColor = XmoRed.copy(.35f),
                            spotColor = XmoRed.copy(.35f)
                        )
                        .graphicsLayer {
                            translationX =
                                if (moving) dragX else 0f
                            scaleX = if (moving) 1.06f else 1f
                            scaleY = if (moving) 1.06f else 1f
                        }
                        .pointerInput(id, working) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    dragging = id
                                    dragX = 0f
                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                },

                                onDrag = { change, amount ->
                                    change.consume()
                                    dragX += amount.x

                                    val step = 90f * density

                                    if (dragX > step && index < working.lastIndex) {
                                        val next = working.toMutableList()
                                        next.add(index + 1, next.removeAt(index))
                                        working = next
                                        dragX -= step
                                    } else if (dragX < -step && index > 0) {
                                        val next = working.toMutableList()
                                        next.add(index - 1, next.removeAt(index))
                                        working = next
                                        dragX += step
                                    }
                                },

                                onDragEnd = {
                                    dragging = null
                                    dragX = 0f
                                    onOrder(working)
                                },

                                onDragCancel = {
                                    dragging = null
                                    dragX = 0f
                                    working = order
                                }
                            )
                        }
                ) {
                    CategoryChip(
                        section.name,
                        selected == section.id,
                        c,
                        section.icon,
                        section.color
                    ) {
                        if (dragging == null) {
                            onSelect(section.id)
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
                onClick = onAdd
            )
        }
    }
}

@Composable
private fun ReorderSection(
    section: HomeSection,
    index: Int,
    order: List<String>,
    c: HomeColors,
    saveOrder: (List<String>) -> Unit,
    body: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var lifted by remember { mutableStateOf(false) }
    var drag by remember { mutableFloatStateOf(0f) }
    var target by remember { mutableIntStateOf(index) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 9.dp
            )
            .animateContentSize()
            .graphicsLayer {
                translationY = if (lifted) drag else 0f
                scaleX = if (lifted) 1.015f else 1f
                scaleY = if (lifted) 1.015f else 1f
                alpha = if (lifted) .96f else 1f
            }
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(
                    if (lifted) {
                        Modifier
                            .shadow(
                                9.dp,
                                RoundedCornerShape(12.dp),
                                ambientColor = XmoRed.copy(.28f),
                                spotColor = XmoRed.copy(.28f)
                            )
                            .background(
                                c.surface,
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                .7.dp,
                                XmoRed.copy(.42f),
                                RoundedCornerShape(12.dp)
                            )
                    } else Modifier
                )
                .pointerInput(section.id, order) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            lifted = true
                            drag = 0f
                            target = index

                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress
                            )
                        },

                        onDrag = { change, amount ->
                            change.consume()
                            drag += amount.y

                            target = (
                                index +
                                    (drag / (110f * density))
                                        .roundToInt()
                                ).coerceIn(order.indices)
                        },

                        onDragEnd = {
                            if (target != index) {
                                val next = order.toMutableList()
                                val item = next.removeAt(index)
                                next.add(target, item)
                                saveOrder(next)
                            }

                            lifted = false
                            drag = 0f
                        },

                        onDragCancel = {
                            lifted = false
                            drag = 0f
                        }
                    )
                }
        ) {
            SectionTitle(
                title = section.name,
                subtitle = sectionSubtitle(section.id),
                icon = section.icon,
                c = c,
                action = when (section.id) {
                    "albums", "liked" -> R.drawable.ic_xmo_add
                    else -> null
                }
            )
        }

        AnimatedVisibility(!lifted) {
            body()
        }

        if (lifted && target != index) {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        XmoRed.copy(.20f),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

private fun sectionSubtitle(id: String) = when (id) {
    "songs" -> ""
    "albums" -> ""
    "liked" -> ""
    "artists" -> ""
    else -> ""
}

@Composable
private fun Songs(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme
) {
    if (!allowed) {
        Empty("Music access required", c)
        return
    }

    if (songs.isEmpty()) {
        Empty("No local music found", c)
        return
    }

    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 5.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "All songs: ${songs.size}",
                color = c.sub,
                fontFamily = XmoFont.thin,
                fontSize = 10.sp,
                modifier = Modifier.weight(1f)
            )

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
                                    .coerceAtMost(scroll.maxValue)
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

        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val width = maxWidth

            Row(
                Modifier.horizontalScroll(scroll)
            ) {
                songs.chunked(12).forEachIndexed { p, page ->
                    Column(
                        Modifier
                            .width(width)
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
                                    val local = row * 4 + column

                                    Box(Modifier.weight(1f)) {
                                        page.getOrNull(local)?.let {
                                            SongTile(
                                                it,
                                                p * 12 + local,
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
private fun Albums(
    songs: List<Song>,
    c: HomeColors
) {
    Text(
        "${Library.albums(songs).size} albums",
        color = c.sub,
        fontFamily = XmoFont.thin,
        fontSize = 10.sp,
        modifier = Modifier.padding(
            start = 10.dp,
            bottom = 12.dp
        )
    )

    if (songs.isEmpty()) {
        Empty("No albums found", c)
    }
}

@Composable
private fun Liked(c: HomeColors) {
    Text(
        "0 favorites",
        color = c.sub,
        fontFamily = XmoFont.thin,
        fontSize = 10.sp,
        modifier = Modifier.padding(
            start = 10.dp,
            bottom = 6.dp
        )
    )

    Empty("No liked songs yet", c)
}

@Composable
private fun Artists(
    songs: List<Song>,
    c: HomeColors
) {
    val artists = Library.artists(songs)

    if (artists.isEmpty()) {
        Empty("No artists found", c)
        return
    }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        artists.take(15).forEach { artist ->
            Column(
                Modifier.width(66.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(62.dp)
                        .background(
                            XmoRed.copy(.16f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        artist.name.firstOrNull()
                            ?.uppercase() ?: "?",
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
                    modifier = Modifier.padding(top = 5.dp)
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
        Empty("No songs in this category", c)
        return
    }

    songs.chunked(6).forEachIndexed { row, items ->
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            repeat(6) { column ->
                Box(Modifier.weight(1f)) {
                    items.getOrNull(column)?.let {
                        SongTile(
                            it,
                            row * 6 + column,
                            c,
                            theme
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))
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
