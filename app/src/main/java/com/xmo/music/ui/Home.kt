package com.xmo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
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
import com.xmo.music.XmoTheme
import com.xmo.music.data.*
import kotlinx.coroutines.launch
import java.util.UUID

private data class Sec(
    val id: String,
    val name: String,
    val icon: String
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
        Sec("songs", "All Songs", "♫"),
        Sec("albums", "Albums", "▣"),
        Sec("liked", "Liked Songs", "♥"),
        Sec("artists", "Top Artists", "●")
    )

    val symbols = listOf("★", "✦", "◆", "✿")

    val custom = categories.map {
        Sec(
            it.id,
            it.name,
            symbols[it.icon.mod(symbols.size)]
        )
    }

    val sections = (built + custom).associateBy { it.id }

    val validOrder =
        order.filter { sections.containsKey(it) } +
            sections.keys.filterNot { it in order }

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
            contentPadding = PaddingValues(bottom = 340.dp)
        ) {
            item(key = "header") {
                Box(Modifier.statusBarsPadding()) {
                    HomeHeader(
                        c = c,
                        theme = theme,
                        setTheme = setTheme,
                        refresh = refresh
                    )
                }
            }

            stickyHeader(key = "categories") {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(c.bg.copy(.95f))
                        .horizontalScroll(rememberScrollState())
                        .padding(
                            horizontal = 14.dp,
                            vertical = 9.dp
                        ),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        "All",
                        selected == "all",
                        c,
                        "▦"
                    ) {
                        selected = "all"

                        scope.launch {
                            state.animateScrollToItem(0)
                        }
                    }

                    validOrder.forEach { id ->
                        sections[id]?.let { section ->
                            CategoryChip(
                                section.name,
                                selected == id,
                                c,
                                section.icon
                            ) {
                                selected = id

                                val index =
                                    validOrder.indexOf(id)

                                scope.launch {
                                    state.animateScrollToItem(
                                        index = 3 + index
                                    )
                                }
                            }
                        }
                    }

                    CategoryChip(
                        "+",
                        false,
                        c,
                        "+"
                    ) {
                        addDialog = true
                    }
                }
            }

            item(key = "recent") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 12.dp,
                            vertical = 8.dp
                        )
                ) {
                    SectionTitle(
                        "Recently Played",
                        "0 tracks played",
                        "◷",
                        c
                    )

                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(100.dp),
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
                items = validOrder,
                key = { _, id -> id }
            ) { index, id ->

                sections[id]?.let { section ->
                    DraggableSection(
                        sec = section,
                        index = index,
                        order = validOrder,
                        c = c,
                        move = { from, to ->
                            val newOrder =
                                validOrder.toMutableList()

                            val moved =
                                newOrder.removeAt(from)

                            newOrder.add(to, moved)
                            saveOrder(newOrder)
                        }
                    ) {
                        when (id) {
                            "songs" ->
                                SongsSection(
                                    songs,
                                    allowed,
                                    c
                                )

                            "albums" ->
                                AlbumsSection(
                                    songs,
                                    c
                                )

                            "liked" ->
                                LikedSection(c)

                            "artists" ->
                                ArtistsSection(
                                    songs,
                                    c
                                )

                            else -> {
                                val category =
                                    categories.find {
                                        it.id == id
                                    }

                                CustomSection(
                                    songs.filter {
                                        it.id in (
                                            category?.songIds
                                                ?: emptySet()
                                            )
                                    },
                                    c
                                )
                            }
                        }
                    }
                }
            }

            item(key = "footer") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 55.dp,
                            bottom = 100.dp
                        ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "XMO",
                        color = c.text,
                        fontFamily = XmoFont.logo,
                        fontSize = 26.sp
                    )

                    Text(
                        "lxzrvi  •  copyright © 2026",
                        color = c.sub,
                        fontFamily = XmoFont.thin,
                        fontSize = 10.sp
                    )

                    Spacer(Modifier.height(160.dp))
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
                        Text("Name")
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clean =
                            categoryName.trim()

                        if (clean.isNotEmpty()) {
                            val category =
                                UserCategory(
                                    id = "cat_${UUID.randomUUID()}",
                                    name = clean,
                                    icon = categories.size % 4
                                )

                            saveCategories(
                                categories + category
                            )

                            saveOrder(
                                validOrder + category.id
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
private fun DraggableSection(
    sec: Sec,
    index: Int,
    order: List<String>,
    c: HomeColors,
    move: (Int, Int) -> Unit,
    body: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    var drag by remember {
        mutableFloatStateOf(0f)
    }

    var lifted by remember {
        mutableStateOf(false)
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
            .pointerInput(order, index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        drag = 0f
                        lifted = true

                        haptic.performHapticFeedback(
                            HapticFeedbackType.LongPress
                        )
                    },

                    onDrag = { change, amount ->
                        change.consume()
                        drag += amount.y
                    },

                    onDragEnd = {
                        val step = 120f * density

                        val target = (
                            index +
                                (drag / step).toInt()
                            ).coerceIn(order.indices)

                        if (target != index) {
                            move(index, target)
                        }

                        drag = 0f
                        lifted = false
                    },

                    onDragCancel = {
                        drag = 0f
                        lifted = false
                    }
                )
            }
            .graphicsLayer {
                translationY =
                    if (lifted) drag else 0f

                scaleX =
                    if (lifted) 1.02f else 1f

                scaleY =
                    if (lifted) 1.02f else 1f

                alpha =
                    if (lifted) .94f else 1f
            }
    ) {
        SectionTitle(
            sec.name,
            "",
            sec.icon,
            c
        )

        if (!lifted) {
            body()
        }
    }
}

@Composable
private fun SongsSection(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors
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
    val pages = songs.chunked(12)

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val pageWidth = maxWidth

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
                                                c
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
private fun AlbumsSection(
    songs: List<Song>,
    c: HomeColors
) {
    val albums = Library.albums(songs)

    Column {
        SectionTitle(
            "",
            "${albums.size} albums",
            "",
            c,
            "+"
        )

        if (albums.isEmpty()) {
            Empty(
                "No albums found",
                c
            )
        }
    }
}

@Composable
private fun LikedSection(
    c: HomeColors
) {
    Column {
        SectionTitle(
            "",
            "0 favorites",
            "",
            c,
            "+"
        )

        Empty(
            "No liked songs yet",
            c
        )
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
        Empty(
            "No artists found",
            c
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 900.dp),
        userScrollEnabled = false,
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement =
            Arrangement.spacedBy(8.dp),
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        items(
            artists.take(20)
        ) { artist ->

            Column(
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
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
                        fontFamily = XmoFont.bold
                    )
                }

                Spacer(
                    Modifier.height(5.dp)
                )

                Text(
                    artist.name,
                    color = c.text,
                    fontFamily = XmoFont.medium,
                    fontSize = 9.sp,
                    maxLines = 2,
                    lineHeight = 10.sp
                )
            }
        }
    }
}

@Composable
private fun CustomSection(
    songs: List<Song>,
    c: HomeColors
) {
    if (songs.isEmpty()) {
        Empty(
            "No songs in this category",
            c
        )
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 1200.dp),
        userScrollEnabled = false,
        horizontalArrangement =
            Arrangement.spacedBy(5.dp),
        verticalArrangement =
            Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(
            songs,
            key = { _, song -> song.id }
        ) { index, song ->
            SongTile(
                song,
                index,
                c
            )
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
            .height(78.dp),
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
