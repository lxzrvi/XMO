package com.xmo.music.ui.home

import coil3.request.CachePolicy
import coil3.request.ImageRequest
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

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

    val slots =
        ((songs.size + 11) / 12) * 12

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(330.dp)
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
                    "home_song_slot_$it"
                },
                contentType = {
                    "home_song"
                }
            ) { slot ->
                val page = slot / 12
                val local = slot % 12
                val row = local % 3
                val column = local / 3

                val sourceIndex =
                    page * 12 +
                        row * 4 +
                        column

                Box(
                    Modifier.width(cardWidth)
                ) {
                    songs
                        .getOrNull(sourceIndex)
                        ?.let { song ->
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
                                play = {
                                    if (
                                        playback.currentSongId !=
                                        song.id
                                    ) {
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
    play: () -> Unit,
    options: () -> Unit
) {
    val context = LocalContext.current

    val imageRequest =
        remember(song.artwork) {
            ImageRequest.Builder(context)
                .data(song.artwork)
                .size(320, 320)
                .memoryCachePolicy(
                    CachePolicy.ENABLED
                )
                .diskCachePolicy(
                    CachePolicy.ENABLED
                )
                .build()
        }

    val surface =
        when (theme) {
            XmoTheme.Light ->
                androidx.compose.ui.graphics.Color(
                    0xFFF9F9FA
                )

            XmoTheme.Dark ->
                androidx.compose.ui.graphics.Color(
                    0xFF181819
                )

            XmoTheme.Amoled ->
                androidx.compose.ui.graphics.Color(
                    0xFF080808
                )
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                androidx.compose.ui.graphics.Color
                    .Black.copy(alpha = .06f)

            XmoTheme.Dark ->
                androidx.compose.ui.graphics.Color
                    .White.copy(alpha = .06f)

            XmoTheme.Amoled ->
                androidx.compose.ui.graphics.Color
                    .White.copy(alpha = .08f)
        }

    Column(
        modifier
            .clip(
                RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = play,
                onLongClick = options
            )
            .background(surface)
            .border(
                .4.dp,
                border,
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
                    model = imageRequest,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (song.artwork == null) {
                    Text(
                        text =
                            song.title
                                .firstOrNull()
                                ?.uppercase()
                                ?: "X",
                        color =
                            c.text.copy(alpha = .60f),
                        fontFamily = XmoFont.bold,
                        fontSize = 17.sp,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                if (playing) {
                    PlayingWave(
                        active = active,
                        modifier = Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                            .padding(5.dp)
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
                    color =
                        if (playing) {
                            LocalXmoAccent.current
                        } else {
                            c.text
                        },
                    fontFamily = XmoFont.bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .size(25.dp)
                    .combinedClickable(
                        onClick = options,
                        onLongClick = options
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
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun PlayingWave(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val transition =
        rememberInfiniteTransition(
            label = "homePlaying"
        )

    val scale by
        transition.animateFloat(
            initialValue = .72f,
            targetValue = 1.15f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(420),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label = "homePlayingScale"
        )

    Icon(
        imageVector = Icons.Rounded.GraphicEq,
        contentDescription = "Playing",
        tint = LocalXmoAccent.current,
        modifier = modifier
            .size(20.dp)
            .graphicsLayer {
                scaleY =
                    if (active) {
                        scale
                    } else {
                        1f
                    }
            }
    )
}
