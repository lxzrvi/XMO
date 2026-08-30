package com.xmo.music.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

@Stable
private class HomeSongScroller {
    var click by mutableIntStateOf(0)
        private set

    var hold by mutableStateOf(false)
        private set

    fun tap() {
        click++
    }

    fun begin() {
        hold = true
    }

    fun stop() {
        hold = false
    }
}

@Composable
internal fun HomeAllSongs(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (!allowed) {
        HomeEmpty("Music access required", c)
        return
    }

    if (songs.isEmpty()) {
        HomeEmpty("No local music found", c)
        return
    }

    val arrow = remember {
        HomeSongScroller()
    }

    val grid = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    val slots = ((songs.size + 11) / 12) * 12

    LaunchedEffect(arrow.click) {
        if (arrow.click > 0) {
            val column = grid.firstVisibleItemIndex / 3
            val maxColumn = slots / 3 - 1
            val target = (column + 1).coerceAtMost(maxColumn)

            if (target > column) {
                grid.animateScrollToItem(target * 3)
            }
        }
    }

    LaunchedEffect(arrow.hold) {
        while (arrow.hold && isActive) {
            if (abs(grid.scrollBy(19f)) < .1f) {
                break
            }

            delay(16L)
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .background(c.bg)
    ) {
        val edge = 8.dp
        val gap = 8.dp
        val cardWidth = (maxWidth - edge * 2 - gap * 3) / 4
        val gridHeight = (cardWidth + 37.dp) * 3 + gap * 2

        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            state = grid,
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            contentPadding = PaddingValues(horizontal = edge),
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalArrangement = Arrangement.spacedBy(gap)
        ) {
            items(
                count = slots,
                key = { "all_song_slot_$it" }
            ) { slot ->
                val page = slot / 12
                val local = slot % 12
                val row = local % 3
                val column = local / 3
                val sourceIndex = page * 12 + row * 4 + column

                Box(
                    Modifier.width(cardWidth)
                ) {
                    songs.getOrNull(sourceIndex)?.let { song ->
                        HomeSongTile(
                            song = song,
                            c = c,
                            modifier = Modifier.width(cardWidth),
                            onClick = {
                                play(song)
                            },
                            onOptions = {
                                options(song)
                            }
                        )
                    }
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 9.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    LocalXmoAccent.current.copy(alpha = .18f)
                )
                .border(
                    .6.dp,
                    LocalXmoAccent.current.copy(alpha = .28f),
                    CircleShape
                )
                .pointerInput(arrow) {
                    detectTapGestures(
                        onPress = {
                            var held = false

                            val job = scope.launch {
                                delay(250L)
                                held = true
                                arrow.begin()
                            }

                            val released = tryAwaitRelease()
                            job.cancel()
                            arrow.stop()

                            if (released && !held) {
                                arrow.tap()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowForwardIos,
                contentDescription = "Next songs",
                tint = LocalXmoAccent.current,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeSongTile(
    song: Song,
    c: HomeColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onOptions: () -> Unit
) {
    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onOptions
            )
            .background(c.surface)
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(10.dp)
            )
            .padding(5.dp)
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(maxWidth)
                    .clip(RoundedCornerShape(7.dp))
                    .background(c.button)
            ) {
                AsyncImage(
                    model = song.artwork,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (song.artwork == null) {
                    Text(
                        text = song.title
                            .firstOrNull()
                            ?.uppercase()
                            ?: "X",
                        color = c.text.copy(alpha = .65f),
                        fontFamily = XmoFont.bold,
                        fontSize = 17.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .size(25.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = onOptions,
                        onLongClick = onOptions
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreHoriz,
                    contentDescription = "Song options",
                    tint = c.sub,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
