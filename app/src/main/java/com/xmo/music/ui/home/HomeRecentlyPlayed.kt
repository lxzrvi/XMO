package com.xmo.music.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeRecentlyPlayed(
    songs: List<Song>,
    c: HomeColors,
    playback: PlaybackState,
    play: (Song) -> Unit,
    togglePlay: () -> Unit,
    options: (Song) -> Unit
) {
    if (songs.isEmpty()) return

    val state = rememberLazyListState()
    val fling =
        rememberSnapFlingBehavior(state)

    var current by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(state) {
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            current = it.coerceIn(
                0,
                songs.lastIndex
            )
        }
    }

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        LazyRow(
            state = state,
            flingBehavior = fling,
            contentPadding =
                PaddingValues(horizontal = 12.dp),
            horizontalArrangement =
                Arrangement.spacedBy(24.dp)
        ) {
            itemsIndexed(
                items = songs,
                key = { _, song ->
                    song.id
                }
            ) { _, song ->
                androidx.compose.foundation.layout.BoxWithConstraints(
                    Modifier.fillParentMaxWidth()
                ) {
                    RecentCard(
                        song = song,
                        c = c,
                        width = maxWidth - 24.dp,
                        current =
                            playback.currentSongId ==
                                song.id,
                        playing =
                            playback.currentSongId ==
                                song.id &&
                                playback.isPlaying,
                        play = {
                            if (
                                playback.currentSongId ==
                                song.id
                            ) {
                                togglePlay()
                            } else {
                                play(song)
                            }
                        },
                        options = {
                            options(song)
                        }
                    )
                }
            }
        }

        val dots =
            minOf(6, songs.size)

        val active =
            if (songs.size <= 1) {
                0
            } else {
                (
                    current.toFloat() /
                        (songs.size - 1) *
                        (dots - 1)
                    )
                    .roundToInt()
                    .coerceIn(0, dots - 1)
            }

        Row(
            Modifier.padding(top = 8.dp),
            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            repeat(dots) { index ->
                Box(
                    Modifier
                        .size(
                            if (index == active) {
                                7.dp
                            } else {
                                5.dp
                            }
                        )
                        .clip(CircleShape)
                        .background(
                            if (index == active) {
                                LocalXmoAccent.current
                            } else {
                                c.sub.copy(alpha = .30f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun RecentCard(
    song: Song,
    c: HomeColors,
    width: androidx.compose.ui.unit.Dp,
    current: Boolean,
    playing: Boolean,
    play: () -> Unit,
    options: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(122.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = play,
                onLongClick = options
            )
            .background(c.surface)
            .border(
                .5.dp,
                c.border,
                RoundedCornerShape(18.dp)
            )
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = song.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = .84f)
                        )
                    )
                )
        )

        Text(
            text = song.title,
            color = Color.White,
            fontFamily = XmoFont.bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(.72f)
                .padding(12.dp)
        )

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .height(34.dp)
                .clip(
                    if (current) {
                        RoundedCornerShape(18.dp)
                    } else {
                        CircleShape
                    }
                )
                .background(
                    Color.Black.copy(alpha = .52f)
                )
                .combinedClickable(
                    onClick = play,
                    onLongClick = options
                )
                .padding(
                    horizontal =
                        if (current) 10.dp else 7.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = current to playing,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "recentControl"
            ) { value ->
                if (value.first) {
                    Row(
                        verticalAlignment =
                            Alignment.CenterVertically,
                        horizontalArrangement =
                            Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector =
                                if (value.second) {
                                    Icons.Rounded.Pause
                                } else {
                                    Icons.Rounded.PlayArrow
                                },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )

                        Text(
                            text =
                                if (value.second) {
                                    "Playing"
                                } else {
                                    "Paused"
                                },
                            color = Color.White,
                            fontFamily = XmoFont.medium,
                            fontSize = 9.sp
                        )
                    }
                } else {
                    Icon(
                        imageVector =
                            Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
