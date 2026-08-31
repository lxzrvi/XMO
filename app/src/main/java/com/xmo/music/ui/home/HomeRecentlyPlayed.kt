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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeRecentlyPlayed(
    songs: List<Song>,
    c: HomeColors,
    currentSongId: Long?,
    isPlaying: Boolean,
    play: (Song) -> Unit,
    togglePlay: () -> Unit,
    options: (Song) -> Unit
) {
    if (songs.isEmpty()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(153.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Nothing played yet",
                color = c.sub,
                fontFamily = XmoFont.normal,
                fontSize = 12.sp
            )
        }
        return
    }

    val state =
        rememberLazyListState()

    val fling =
        rememberSnapFlingBehavior(state)

    var currentIndex by
        remember {
            mutableIntStateOf(0)
        }

    LaunchedEffect(songs.first().id) {
        state.animateScrollToItem(0)
    }

    LaunchedEffect(state) {
        snapshotFlow {
            state.firstVisibleItemIndex
        }.collect {
            currentIndex =
                it.coerceIn(
                    0,
                    songs.lastIndex
                )
        }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .height(153.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        LazyRow(
            state = state,
            flingBehavior = fling,
            contentPadding =
                PaddingValues(horizontal = 12.dp)
        ) {
            items(
                items = songs,
                key = { it.id }
            ) { song ->
                BoxWithConstraints(
                    Modifier.fillParentMaxWidth()
                ) {
                    RecentCard(
                        song = song,
                        current =
                            currentSongId ==
                                song.id,
                        playing =
                            currentSongId ==
                                song.id &&
                                isPlaying,
                        c = c,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(122.dp),
                        click = {
                            if (
                                currentSongId ==
                                song.id
                            ) {
                                togglePlay()
                            } else {
                                play(song)
                            }
                        },
                        longClick = {
                            options(song)
                        }
                    )
                }
            }
        }

        val dots =
            minOf(6, songs.size)

        val selected =
            if (songs.size <= 1) {
                0
            } else {
                (
                    currentIndex.toFloat() /
                        (songs.size - 1) *
                        (dots - 1)
                    ).roundToInt()
                    .coerceIn(0, dots - 1)
            }

        Row(
            Modifier.padding(top = 9.dp),
            horizontalArrangement =
                Arrangement.spacedBy(6.dp)
        ) {
            repeat(dots) { index ->
                Box(
                    Modifier
                        .size(
                            if (selected == index) {
                                7.dp
                            } else {
                                5.dp
                            }
                        )
                        .background(
                            if (selected == index) {
                                LocalXmoAccent.current
                            } else {
                                c.sub.copy(alpha = .30f)
                            },
                            CircleShape
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RecentCard(
    song: Song,
    current: Boolean,
    playing: Boolean,
    c: HomeColors,
    modifier: Modifier,
    click: () -> Unit,
    longClick: () -> Unit
) {
    Box(
        modifier
            .background(
                c.surface,
                RoundedCornerShape(18.dp)
            )
            .border(
                .5.dp,
                c.border,
                RoundedCornerShape(18.dp)
            )
            .combinedClickable(
                indication = null,
                interactionSource =
                    remember {
                        androidx.compose.foundation.interaction
                            .MutableInteractionSource()
                    },
                onClick = click,
                onLongClick = longClick
            )
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .padding(.5.dp),
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
            fontSize = 15.sp,
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
                .background(
                    Color.Black.copy(alpha = .52f),
                    if (current) {
                        RoundedCornerShape(18.dp)
                    } else {
                        CircleShape
                    }
                )
                .padding(
                    horizontal =
                        if (current) {
                            10.dp
                        } else {
                            7.dp
                        }
                ),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = current to playing,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "recentState"
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
