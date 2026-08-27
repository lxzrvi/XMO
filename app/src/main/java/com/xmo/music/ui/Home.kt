import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.xmo.music.XmoTheme
import com.xmo.music.data.*
import java.util.UUID

private data class Sec(val id: String, val name: String, val icon: String)

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

    val built = listOf(
        Sec("songs", "All Songs", "♫"),
        Sec("albums", "Albums", "▣"),
        Sec("liked", "Liked Songs", "♥"),
        Sec("artists", "Top Artists", "●")
    )
    val custom = categories.map { Sec(it.id, it.name, listOf("★", "✦", "◆", "✿")[it.icon % 4]) }
    val sections = (built + custom).associateBy { it.id }
    val validOrder = order.filter(sections::containsKey) +
        sections.keys.filterNot(order::contains)

    var selected by remember { mutableStateOf("all") }
    var add by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(c.bg)) {
        LazyColumn(
            state = list,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 330.dp)
        ) {
            item("header") {
                Box(Modifier.statusBarsPadding()) {
                    HomeHeader(c, theme, setTheme, refresh)
                }
            }

            stickyHeader("categories") {
                Row(
                    Modifier.fillMaxWidth()
                        .background(c.bg.copy(.95f))
                        .horizontalScroll(rememberScrollState())
                        .padding(14.dp, 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip("All", selected == "all", c, "▦") {
                        selected = "all"
                        scope.launch { list.animateScrollToItem(0) }
                    }

                    validOrder.forEach { id ->
                        sections[id]?.let { sec ->
                            CategoryChip(sec.name, selected == id, c, sec.icon) {
                                selected = id
                                val i = validOrder.indexOf(id)
                                scope.launch { list.animateScrollToItem(3 + i) }
                            }
                        }
                    }

                    CategoryChip("+", false, c, "+") { add = true }
                }
            }

            item("recent") {
                Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    SectionTitle("Recently Played", "0 tracks played", "◷", c)
                    Box(
                        Modifier.fillMaxWidth().height(92.dp),
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

            itemsIndexed(validOrder, key = { _, id -> id }) { index, id ->
                sections[id]?.let { sec ->
                    DraggableSection(
                        sec = sec,
                        index = index,
                        order = validOrder,
                        c = c,
                        move = { from, to ->
                            val next = validOrder.toMutableList()
                            val moved = next.removeAt(from)
                            next.add(to, moved)
                            saveOrder(next)
                        }
                    ) {
                        when (id) {
                            "songs" -> SongsSection(songs, allowed, c)
                            "albums" -> AlbumsSection(songs, c)
                            "liked" -> LikedSection(c)
                            "artists" -> ArtistsSection(songs, c)
                            else -> CustomSection(
                                songs.filter { it.id in (categories.find { x -> x.id == id }?.songIds ?: emptySet()) },
                                c
                            )
                        }
                    }
                }
            }

            item("footer") {
                Column(
                    Modifier.fillMaxWidth().padding(top = 50.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("XMO", color = c.text, fontFamily = XmoFont.logo, fontSize = 26.sp)
                    Text(
                        "lxzrvi  •  copyright © 2026",
                        color = c.sub, fontFamily = XmoFont.thin, fontSize = 10.sp
                    )
                }
            }
        }
    }

    if (add) {
        AlertDialog(
            onDismissRequest = { add = false },
            containerColor = c.surface,
            title = { Text("New category", color = c.text, fontFamily = XmoFont.bold) },
            text = {
                OutlinedTextField(
                    name, { name = it.take(24) },
                    singleLine = true,
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton({
                    val clean = name.trim()
                    if (clean.isNotEmpty()) {
                        val cat = UserCategory(
                            "cat_${UUID.randomUUID()}",
                            clean,
                            categories.size % 4
                        )
                        saveCategories(categories + cat)
                        saveOrder(validOrder + cat.id)
                        name = ""
                        add = false
                    }
                }) { Text("Add", color = XmoRed) }
            },
            dismissButton = {
                TextButton({ add = false }) { Text("Cancel", color = c.sub) }
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
    var dragY by remember { mutableFloatStateOf(0f) }
    var lifted by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column(
        Modifier.fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .pointerInput(order, index) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        lifted = true
                        dragY = 0f
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onDrag = { change, dy ->
                        change.consume()
                        dragY += dy.y
                    },
                    onDragEnd = {
                        val step = 120f * density
                        val to = (index + (dragY / step).toInt()).coerceIn(order.indices)
                        if (to != index) move(index, to)
                        lifted = false
                        dragY = 0f
                    },
                    onDragCancel = {
                        lifted = false
                        dragY = 0f
                    }
                )
            }
            .graphicsLayer {
                translationY = if (lifted) dragY else 0f
                scaleX = if (lifted) 1.02f else 1f
                scaleY = if (lifted) 1.02f else 1f
                alpha = if (lifted) .94f else 1f
            }
    ) {
        SectionTitle(sec.name, "", sec.icon, c)
        if (!lifted) body()
    }
}

@Composable
private fun SongsSection(songs: List<Song>, allowed: Boolean, c: HomeColors) {
    Column {
        if (!allowed || songs.isEmpty()) {
            Empty(if (allowed) "No local music found" else "Music access required", c)
            return@Column
        }

        val pages = songs.chunked(12)
        val scroll = rememberScrollState()
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val width = maxWidth

            Row(Modifier.horizontalScroll(scroll)) {
                pages.forEachIndexed { p, page ->
                    Column(
                        Modifier.width(width).padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(3) { r ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                repeat(4) { col ->
                                    val i = r * 4 + col
                                    Box(Modifier.weight(1f)) {
                                        page.getOrNull(i)?.let { SongTile(it, p * 12 + i, c) }
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
private fun AlbumsSection(songs: List<Song>, c: HomeColors) {
    Column {
        SectionTitle("", "${Library.albums(songs).size} albums", "", c, "+")
        if (songs.isEmpty()) Empty("No albums found", c)
    }
}

@Composable
private fun LikedSection(c: HomeColors) {
    Column {
        SectionTitle("", "0 favorites", "", c, "+")
        Empty("No liked songs yet", c)
    }
}

@Composable
private fun ArtistsSection(songs: List<Song>, c: HomeColors) {
    val artists = Library.artists(songs)
    if (artists.isEmpty()) {
        Empty("No artists found", c)
        return
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = Modifier.fillMaxWidth().heightIn(max = 800.dp),
        userScrollEnabled = false,
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(artists.take(15)) { artist ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(1f)
                        .background(XmoRed.copy(.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        artist.name.firstOrNull()?.uppercase() ?: "?",
                        color = XmoRed,
                        fontFamily = XmoFont.bold
                    )
                }
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
private fun CustomSection(songs: List<Song>, c: HomeColors) {
    if (songs.isEmpty()) {
        Empty("No songs in this category", c)
        return
    }

    LazyVerticalGrid(
        GridCells.Fixed(6),
        Modifier.fillMaxWidth().heightIn(max = 1200.dp),
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        itemsIndexed(songs) { i, song -> SongTile(song, i, c) }
    }
}

@Composable
private fun Empty(text: String, c: HomeColors) {
    Box(
        Modifier.fillMaxWidth().height(75.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = c.sub, fontFamily = XmoFont.normal, fontSize = 12.sp)
    }
}
