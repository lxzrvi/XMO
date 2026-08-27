package com.xmo.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var songArrowTick by remember { mutableIntStateOf(0) }

    val fixed = remember {
        listOf(
            HomeSection("songs", "All Songs", R.drawable.ic_xmo_songs),
            HomeSection("albums", "Albums", R.drawable.ic_xmo_album),
            HomeSection("liked", "Liked Songs", R.drawable.ic_xmo_heart),
            HomeSection("artists", "Artists", R.drawable.ic_xmo_artist)
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

    val custom = remember(categories) {
        categories.map {
            val i = (it.icon % 4 + 4) % 4
            HomeSection(
                id = it.id,
                name = it.name,
                icon = customIcons[i],
                color = customColors[i]
            )
        }
    }

    val sectionMap = remember(fixed, custom) { (fixed + custom).associateBy { it.id } }
    val actualOrder = remember(order, sectionMap) {
        order.filter(sectionMap::containsKey) + sectionMap.keys.filterNot(order::contains)
    }

    var selected by remember { mutableStateOf("all") }
    var addDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }

    val showProfile by remember {
        derivedStateOf {
            list.layoutInfo.visibleItemsInfo.any { it.key == "recent" } || list.firstVisibleItemIndex == 0
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
            stickyHeader(key = "homeDock") {
                HomeDock(
                    showProfile = showProfile,
                    sections = actualOrder.mapNotNull(sectionMap::get),
                    order = actualOrder,
                    selected = selected,
                    c = c,
                    theme = theme,
                    setTheme = setTheme,
                    refresh = refresh,
                    onSelect = { id ->
                        selected = id
                        scope.launch {
                            if (id == "all") {
                                list.animateScrollToItem(index = 0)
                            } else {
                                val position = actualOrder.indexOf(id)
                                if (position >= 0) {
                                    list.animateScrollToItem(index = position + 2)
                                }
                            }
                        }
                    },
                    onCommit = { saveOrder(it) },
                    onAdd = { addDialog = true }
                )
            }

            item(key = "recent") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 13.dp, end = 12.dp, bottom = 18.dp)
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
                items = actualOrder,
                key = { it }
            ) { id ->
                sectionMap[id]?.let { section ->
                    Section(
                        section = section,
                        c = c,
                        subtitle = when (id) {
                            "songs" -> "All songs: ${songs.size}"
                            "albums" -> "${Library.albums(songs).size} albums"
                            "liked" -> "0 favorites"
                            else -> ""
                        },
                        action = when (id) {
                            "albums", "liked" -> R.drawable.ic_xmo_add
                            else -> null
                        },
                        arrow = id == "songs",
                        onArrowTick = { songArrowTick++ }
                    ) {
                        when (id) {
                            "songs" -> Songs(songs, allowed, c, theme, songArrowTick)
                            "albums" -> Albums(songs, c)
                            "liked" -> Liked(c)
                            "artists" -> Artists(songs, c)
                            else -> {
                                val ids = categories.find { it.id == id }?.songIds ?: emptySet()
                                CustomSongs(songs.filter { it.id in ids }, c, theme)
                            }
                        }
                    }
                }
            }

            item(key = "footer") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "XMO",
                            color = c.text,
                            fontFamily = XmoFont.logo,
                            fontSize = 18.sp
                        )
                        Text(
                            "lxzrvi • copyright © 2026",
                            color = c.sub,
                            fontFamily = XmoFont.thin,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 3.dp)
                        )
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
                title = { Text("New category", color = c.text, fontFamily = XmoFont.bold) },
                text = {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it.take(24) },
                        singleLine = true,
                        label = { Text("Category name") }
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
    onCommit: (List<String>) -> Unit,
    onAdd: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.bg.copy(alpha = 0.97f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .animateContentSize(spring(dampingRatio = 0.86f, stiffness = 520f))
            .padding(top = 3.dp, bottom = 4.dp)
    ) {
        AnimatedVisibility(
            visible = showProfile,
            enter = fadeIn() + slideInVertically { -it / 3 },
            exit = fadeOut() + slideOutVertically { -it }
        ) {
            HomeHeader(c = c, theme = theme, setTheme = setTheme, refresh = refresh)
        }
        if (showProfile) {
            Spacer(Modifier.height(7.dp))
        }
        CategoryBar(
            sections = sections,
            order = order,
            selected = selected,
            c = c,
            select = onSelect,
            commit = onCommit,
            add = onAdd
        )
    }
}

@Composable
private fun CategoryBar(
    sections: List<HomeSection>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    select: (String) -> Unit,
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    var working by remember(order) { mutableStateOf(order) }
    var dragged by remember { mutableStateOf<String?>(null) }
    var dragX by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(order) {
        if (dragged == null) {
            working = order
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            text = "All",
            active = selected == "all",
            c = c,
            icon = R.drawable.ic_xmo_all
        ) { select("all") }

        working.forEach { id ->
            val section = sections.firstOrNull { it.id == id } ?: return@forEach
            val activeDrag = dragged == id
            Box(
                Modifier
                    .graphicsLayer {
                        translationX = if (activeDrag) dragX else 0f
                        scaleX = if (activeDrag) 1.07f else 1f
                        scaleY = if (activeDrag) 1.07f else 1f
                        alpha = if (activeDrag) 0.96f else 1f
                    }
                    .then(
                        if (activeDrag) {
                            Modifier
                                .background(XmoRed.copy(0.10f), RoundedCornerShape(18.dp))
                                .border(0.8.dp, XmoRed.copy(0.55f), RoundedCornerShape(18.dp))
                        } else Modifier
                    )
                    .pointerInput(id, working) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragged = id
                                dragX = 0f
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragX += amount.x
                                val from = working.indexOf(id)
                                if (from < 0) return@detectDragGesturesAfterLongPress
                                val threshold = with(density) { 44.dp.toPx() }
                                if (dragX > threshold && from < working.lastIndex) {
                                    val next = working.toMutableList()
                                    val moving = next.removeAt(from)
                                    next.add(from + 1, moving)
                                    working = next
                                    dragX -= threshold
                                } else if (dragX < -threshold && from > 0) {
                                    val next = working.toMutableList()
                                    val moving = next.removeAt(from)
                                    next.add(from - 1, moving)
                                    working = next
                                    dragX += threshold
                                }
                            },
                            onDragEnd = {
                                val result = working
                                dragged = null
                                dragX = 0f
                                commit(result)
                            },
                            onDragCancel = {
                                dragged = null
                                dragX = 0f
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
                    tint = section.color
                ) {
                    if (dragged == null) {
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
            onClick = add
        )
    }
}

@Composable
private fun Section(
    section: HomeSection,
    c: HomeColors,
    subtitle: String,
    action: Int?,
    arrow: Boolean,
    onArrowTick: () -> Unit = {},
    body: @Composable () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
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
                        .background(XmoRed.copy(0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    XmoIcon(it, XmoRed, Modifier.size(14.dp))
                }
            }
            if (arrow) {
                SongArrowButton(onArrowTick = onArrowTick)
            }
        }
        Spacer(Modifier.height(5.dp))
        body()
    }
}

@Composable
private fun SongArrowButton(onArrowTick: () -> Unit) {
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .padding(start = 7.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(XmoRed.copy(0.18f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        var isHolding = true
                        val job = scope.launch {
                            delay(250)
                            while (isHolding) {
                                onArrowTick()
                                delay(55)
                            }
                        }
                        val released = tryAwaitRelease()
                        isHolding = false
                        job.cancel()
                        if (released) {
                            onArrowTick()
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        XmoIcon(R.drawable.ic_xmo_arrow, XmoRed, Modifier.size(14.dp))
    }
}

@Composable
private fun Songs(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    tick: Int
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

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val availableWidth = this.maxWidth
        val edge = 8.dp
        val gap = 8.dp
        val cardWidth = (availableWidth - edge * 2 - gap * 3) / 4
        val stepPx = with(LocalDensity.current) { (cardWidth + gap).roundToPx() }

        LaunchedEffect(tick) {
            if (tick > 0) {
                scroll.animateScrollTo((scroll.value + stepPx).coerceAtMost(scroll.maxValue))
            }
        }

        LaunchedEffect(songs.size, scroll.maxValue) {
            if (scroll.value > scroll.maxValue) {
                scroll.scrollTo(scroll.maxValue)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scroll)
        ) {
            songs.chunked(12).forEachIndexed { page, items ->
                Column(
                    Modifier
                        .width(availableWidth)
                        .padding(horizontal = edge, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            repeat(4) { column ->
                                val i = row * 4 + column
                                Box(Modifier.width(cardWidth)) {
                                    items.getOrNull(i)?.let {
                                        SongTile(
                                            song = it,
                                            index = page * 12 + i,
                                            c = c,
                                            theme = theme,
                                            modifier = Modifier.fillMaxWidth()
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
private fun Albums(songs: List<Song>, c: HomeColors) {
    if (songs.isEmpty()) {
        Empty("No albums found", c)
    } else {
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun Liked(c: HomeColors) {
    Empty("No liked songs yet", c)
}

@Composable
private fun Artists(songs: List<Song>, c: HomeColors) {
    val artists = Library.artists(songs)
    if (artists.isEmpty()) {
        Empty("No artists found", c)
        return
    }
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 8.dp),
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
                        .background(XmoRed.copy(0.16f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        artist.name.firstOrNull()?.uppercase() ?: "?",
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
private fun CustomSongs(songs: List<Song>, c: HomeColors, theme: XmoTheme) {
    if (songs.isEmpty()) {
        Empty("No songs in this category", c)
        return
    }
    Column(
        Modifier.padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        songs.chunked(6).forEachIndexed { row, items ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(6) { column ->
                    Box(Modifier.weight(1f)) {
                        items.getOrNull(column)?.let {
                            SongTile(
                                song = it,
                                index = row * 6 + column,
                                c = c,
                                theme = theme,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Empty(text: String, c: HomeColors) {
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
