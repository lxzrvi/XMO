package com.xmo.music.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var songArrowTick by remember { mutableIntStateOf(0) }
    var measuredDockHeightPx by remember { mutableIntStateOf(0) }

    val fixed = remember {
        listOf(
            HomeSection("songs", "All Songs", R.drawable.ic_xmo_songs, Color(0xFFE53935)),
            HomeSection("albums", "Albums", R.drawable.ic_xmo_album, Color(0xFF8E24AA)),
            HomeSection("liked", "Liked Songs", R.drawable.ic_xmo_heart, Color(0xFFD81B60)),
            HomeSection("artists", "Artists", R.drawable.ic_xmo_artist, Color(0xFF1E88E5))
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
            Color(0xFFFFB300),
            Color(0xFFAB47BC),
            Color(0xFF039BE5),
            Color(0xFFFF5722)
        )
    }

    val custom = remember(categories) {
        categories.mapIndexed { idx, cat ->
            val iconIdx = (cat.icon % customIcons.size + customIcons.size) % customIcons.size
            val colorIdx = idx % customColors.size
            HomeSection(
                id = cat.id,
                name = cat.name,
                icon = customIcons[iconIdx],
                color = customColors[colorIdx]
            )
        }
    }

    val sectionMap = remember(fixed, custom) { (fixed + custom).associateBy { it.id } }
    val actualOrder = remember(order, sectionMap) {
        val validOrder = order.filter { sectionMap.containsKey(it) }
        val missingKeys = sectionMap.keys.filterNot { validOrder.contains(it) }
        validOrder + missingKeys
    }

    var selected by remember { mutableStateOf("all") }
    var addDialog by remember { mutableStateOf(false) }
    var categoryName by remember { mutableStateOf("") }

    // Direct offset interpolation without jumps
    val headerProgress by remember {
        derivedStateOf {
            val visibleInfo = listState.layoutInfo.visibleItemsInfo
            val recentItem = visibleInfo.firstOrNull { it.key == "recentSection" }
            if (recentItem != null) {
                val totalH = recentItem.size.toFloat().coerceAtLeast(1f)
                val currentOffset = -recentItem.offset.toFloat()
                (1f - (currentOffset / (totalH * 0.6f))).coerceIn(0f, 1f)
            } else if (listState.firstVisibleItemIndex > 0) {
                0f
            } else {
                1f
            }
        }
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
            stickyHeader(key = "homeDockSticky") {
                Box(
                    Modifier.onGloballyPositioned { coords ->
                        measuredDockHeightPx = coords.size.height
                    }
                ) {
                    HomeDock(
                        headerProgress = headerProgress,
                        sections = actualOrder.mapNotNull { sectionMap[it] },
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
                                    listState.animateScrollToItem(0, 0)
                                } else {
                                    val sectionIdx = actualOrder.indexOf(id)
                                    if (sectionIdx != -1) {
                                        // Precise index positioning matching actual LazyColumn list layout
                                        listState.animateScrollToItem(
                                            index = sectionIdx + 2,
                                            scrollOffset = 0
                                        )
                                    }
                                }
                            }
                        },
                        onCommit = { saveOrder(it) },
                        onAdd = { addDialog = true }
                    )
                }
            }

            item(key = "recentSection") {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 12.dp, end = 12.dp, bottom = 16.dp)
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
                            "artists" -> "${Library.artists(songs).size} artists"
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
                                val catIds = categories.find { it.id == id }?.songIds ?: emptySet()
                                CustomSongs(songs.filter { it.id in catIds }, c, theme)
                            }
                        }
                    }
                }
            }

            item(key = "footerSpace") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("XMO", color = c.text, fontFamily = XmoFont.logo, fontSize = 18.sp)
                        Text(
                            "lxzrvi • copyright © 2026",
                            color = c.sub,
                            fontFamily = XmoFont.thin,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 2.dp)
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
                title = { Text("New Category", color = c.text, fontFamily = XmoFont.bold) },
                text = {
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it.take(24) },
                        singleLine = true,
                        label = { Text("Category Name") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val trimmed = categoryName.trim()
                            if (trimmed.isNotEmpty()) {
                                val newCat = UserCategory(
                                    id = "cat_${UUID.randomUUID()}",
                                    name = trimmed,
                                    icon = categories.size
                                )
                                saveCategories(categories + newCat)
                                saveOrder(actualOrder + newCat.id)
                                categoryName = ""
                                addDialog = false
                            }
                        }
                    ) { Text("Add", color = XmoRed) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        categoryName = ""
                        addDialog = false
                    }) { Text("Cancel", color = c.sub) }
                }
            )
        }
    }
}

@Composable
private fun HomeDock(
    headerProgress: Float,
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
            .background(c.bg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(bottom = 6.dp)
    ) {
        if (headerProgress > 0.001f) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = headerProgress
                        translationY = -28.dp.toPx() * (1f - headerProgress)
                    }
            ) {
                HomeHeader(c = c, theme = theme, setTheme = setTheme, refresh = refresh)
            }
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

    var workingList by remember(order) { mutableStateOf(order) }
    var draggedId by remember { mutableStateOf<String?>(null) }
    var currentDragX by remember { mutableFloatStateOf(0f) }

    val slotWidthPx = with(density) { 92.dp.toPx() }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryChip(
            text = "All",
            active = selected == "all",
            c = c,
            icon = R.drawable.ic_xmo_all
        ) { select("all") }

        workingList.forEach { id ->
            val section = sections.firstOrNull { it.id == id } ?: return@forEach
            val isDragging = draggedId == id

            val translationXAnim = remember { Animatable(0f) }

            LaunchedEffect(currentDragX, isDragging) {
                if (isDragging) {
                    translationXAnim.snapTo(currentDragX)
                } else {
                    translationXAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                }
            }

            Box(
                Modifier
                    .graphicsLayer {
                        translationX = translationXAnim.value
                        scaleX = if (isDragging) 1.12f else 1f
                        scaleY = if (isDragging) 1.12f else 1f
                        shadowElevation = if (isDragging) 14f else 0f
                        alpha = if (isDragging) 0.95f else 1f
                    }
                    .pointerInput(id, workingList) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedId = id
                                currentDragX = 0f
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentDragX += dragAmount.x
                                val curIndex = workingList.indexOf(id)

                                if (curIndex != -1) {
                                    if (currentDragX > slotWidthPx * 0.65f && curIndex < workingList.lastIndex) {
                                        val mutable = workingList.toMutableList()
                                        val item = mutable.removeAt(curIndex)
                                        mutable.add(curIndex + 1, item)
                                        workingList = mutable
                                        currentDragX -= slotWidthPx
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    } else if (currentDragX < -slotWidthPx * 0.65f && curIndex > 0) {
                                        val mutable = workingList.toMutableList()
                                        val item = mutable.removeAt(curIndex)
                                        mutable.add(curIndex - 1, item)
                                        workingList = mutable
                                        currentDragX += slotWidthPx
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                }
                            },
                            onDragEnd = {
                                val updated = workingList
                                draggedId = null
                                currentDragX = 0f
                                commit(updated)
                            },
                            onDragCancel = {
                                draggedId = null
                                currentDragX = 0f
                                workingList = order
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
                    if (draggedId == null) {
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
            .padding(vertical = 8.dp)
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
                        .background(XmoRed.copy(0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    XmoIcon(it, XmoRed, Modifier.size(14.dp))
                }
            }
            if (arrow) {
                SongArrowButton(onArrowTick = onArrowTick)
            }
        }
        Spacer(Modifier.height(6.dp))
        body()
    }
}

@Composable
private fun SongArrowButton(onArrowTick: () -> Unit) {
    val scope = rememberCoroutineScope()
    Box(
        Modifier
            .padding(start = 6.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(XmoRed.copy(0.15f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        var isHolding = true
                        val job = scope.launch {
                            delay(220)
                            while (isHolding) {
                                onArrowTick()
                                delay(50)
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

    val scrollState = rememberScrollState()

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val totalWidth = this.maxWidth
        val outerPadding = 12.dp
        val itemGap = 6.dp

        val availableWidth = totalWidth - (outerPadding * 2)
        val cardWidth = (availableWidth - (itemGap * 3)) / 4
        val stepPx = with(LocalDensity.current) { (cardWidth + itemGap).toPx() }

        LaunchedEffect(tick) {
            if (tick > 0) {
                val target = (scrollState.value + stepPx).toInt().coerceAtMost(scrollState.maxValue)
                scrollState.animateScrollTo(target)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = outerPadding),
            horizontalArrangement = Arrangement.spacedBy(itemGap)
        ) {
            songs.chunked(12).forEachIndexed { pageIndex, pageSongs ->
                Column(
                    Modifier.width(availableWidth),
                    verticalArrangement = Arrangement.spacedBy(itemGap)
                ) {
                    repeat(3) { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(itemGap)
                        ) {
                            repeat(4) { col ->
                                val indexInPage = row * 4 + col
                                val song = pageSongs.getOrNull(indexInPage)
                                Box(Modifier.width(cardWidth)) {
                                    if (song != null) {
                                        SongTile(
                                            song = song,
                                            index = pageIndex * 12 + indexInPage,
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
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        artists.take(15).forEach { artist ->
            Column(
                Modifier.width(64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(60.dp)
                        .background(XmoRed.copy(0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        artist.name.firstOrNull()?.uppercase() ?: "?",
                        color = XmoRed,
                        fontFamily = XmoFont.bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    artist.name,
                    color = c.text,
                    fontFamily = XmoFont.medium,
                    fontSize = 9.sp,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
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
        Modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        songs.chunked(4).forEachIndexed { rowIndex, rowSongs ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(4) { colIndex ->
                    val song = rowSongs.getOrNull(colIndex)
                    Box(Modifier.weight(1f)) {
                        if (song != null) {
                            SongTile(
                                song = song,
                                index = rowIndex * 4 + colIndex,
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
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = c.sub, fontFamily = XmoFont.normal, fontSize = 12.sp)
    }
}
