package com.xmo.music.ui.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
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
    playback: PlaybackState,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (!allowed) {
        HomeEmpty(
            "Music access required",
            c
        )
        return
    }

    if (songs.isEmpty()) {
        HomeEmpty(
            "No local music found",
            c
        )
        return
    }

    val grid =
        rememberLazyGridState()

    val scope =
        rememberCoroutineScope()

    val arrow =
        remember {
            HomeSongScroller()
        }

    val slots =
        remember(songs.size) {
            ((songs.size + 11) / 12) * 12
        }

    LaunchedEffect(arrow.click) {
        if (arrow.click <= 0) {
            return@LaunchedEffect
        }

        val column =
            grid.firstVisibleItemIndex / 3

        val maxColumn =
            slots / 3 - 1

        val target =
            (column + 1)
                .coerceAtMost(maxColumn)

        if (target > column) {
            grid.animateScrollToItem(
                target * 3
            )
        }
    }

    LaunchedEffect(arrow.hold) {
        while (
            arrow.hold &&
            isActive
        ) {
            val consumed =
                grid.scrollBy(19f)

            if (abs(consumed) < .1f) {
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

        val cardWidth =
            (
                maxWidth -
                    edge * 2 -
                    gap * 3
                ) / 4

        val gridHeight =
            (
                cardWidth +
                    37.dp
                ) * 3 +
                gap * 2

        LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            state = grid,
            modifier = Modifier
                .fillMaxWidth()
                .height(gridHeight),
            contentPadding =
                PaddingValues(
                    horizontal = edge
                ),
            horizontalArrangement =
                Arrangement.spacedBy(gap),
            verticalArrangement =
                Arrangement.spacedBy(gap)
        ) {
            items(
                count = slots,
                key = {
                    "all_song_slot_$it"
                },
                contentType = {
                    "home_song_slot"
                }
            ) { slot ->
                val page =
                    slot / 12

                val local =
                    slot % 12

                val row =
                    local % 3

                val column =
                    local / 3

                val sourceIndex =
                    page * 12 +
                        row * 4 +
                        column

                Box(
                    Modifier.width(cardWidth)
                ) {
                    val song =
                        songs.getOrNull(
                            sourceIndex
                        )

                    if (song != null) {
                        HomeSongTile(
                            song = song,
                            c = c,
                            theme = theme,
                            playing =
                                playback.currentSongId ==
                                    song.id,
                            active =
                                playback.currentSongId ==
                                    song.id &&
                                    playback.isPlaying,
                            modifier =
                                Modifier.width(
                                    cardWidth
                                ),
                            onClick = {
                                if (
                                    playback.currentSongId !=
                                    song.id
                                ) {
                                    play(song)
                                }
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
                .size(31.dp)
                .clip(CircleShape)
                .background(
                    LocalXmoAccent.current
                        .copy(alpha = .20f)
                )
                .pointerInput(arrow) {
                    detectTapGestures(
                        onPress = {
                            var held =
                                false

                            val job =
                                scope.launch {
                                    delay(250L)

                                    held = true
                                    arrow.begin()
                                }

                            val released =
                                tryAwaitRelease()

                            job.cancel()
                            arrow.stop()

                            if (
                                released &&
                                !held
                            ) {
                                arrow.tap()
                            }
                        }
                    )
                },
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.ArrowForwardIos,
                contentDescription =
                    "Next songs",
                tint =
                    LocalXmoAccent.current,
                modifier =
                    Modifier.size(14.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeSongTile(
    song: Song,
    c: HomeColors,
    theme: XmoTheme,
    playing: Boolean,
    active: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
    onOptions: () -> Unit
) {
    val context =
        LocalContext.current

    val artworkRequest =
        remember(
            song.artwork
        ) {
            ImageRequest.Builder(context)
                .data(song.artwork)
                .size(192, 192)
                .memoryCachePolicy(
                    CachePolicy.ENABLED
                )
                .diskCachePolicy(
                    CachePolicy.ENABLED
                )
                .networkCachePolicy(
                    CachePolicy.DISABLED
                )
                .build()
        }

    val cardBackground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF9F9FA)

            XmoTheme.Dark ->
                Color(0xFF181819)

            XmoTheme.Amoled ->
                Color(0xFF080808)
        }

    val cardBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .06f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .06f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .085f
                )
        }

    Column(
        modifier
            .clip(
                RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onOptions
            )
            .background(cardBackground)
            .border(
                .45.dp,
                cardBorder,
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
                    .clip(
                        RoundedCornerShape(6.dp)
                    )
                    .background(c.button)
            ) {
                AsyncImage(
                    model = artworkRequest,
                    contentDescription = song.title,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )

                if (song.artwork == null) {
                    Text(
                        text =
                            song.title
                                .firstOrNull()
                                ?.uppercase()
                                ?: "X",
                        color =
                            c.text.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 17.sp,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                if (playing) {
                    HomePlayingWave(
                        active = active,
                        modifier = Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .padding(4.dp)
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    color = c.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    color = c.sub,
                    fontFamily =
                        XmoFont.normal,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
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
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.MoreHoriz,
                    contentDescription =
                        "Song options",
                    tint = c.sub,
                    modifier =
                        Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun HomePlayingWave(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    if (!active) {
        Icon(
            imageVector =
                Icons.Rounded.GraphicEq,
            contentDescription = "Paused",
            tint = LocalXmoAccent.current,
            modifier =
                modifier.size(19.dp)
        )

        return
    }

    val animation =
        rememberInfiniteTransition(
            label = "activeSongWave"
        )

    val scale by
        animation.animateFloat(
            initialValue = .72f,
            targetValue = 1.18f,
            animationSpec =
                infiniteRepeatable(
                    animation =
                        tween(390),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label = "activeSongWaveScale"
        )

    Icon(
        imageVector =
            Icons.Rounded.GraphicEq,
        contentDescription = "Playing",
        tint = LocalXmoAccent.current,
        modifier = modifier
            .size(19.dp)
            .graphicsLayer {
                scaleY = scale
            }
    )
}
