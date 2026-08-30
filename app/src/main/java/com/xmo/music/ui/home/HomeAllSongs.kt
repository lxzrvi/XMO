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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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

    BoxWithConstraints(
            Modifier
                .fillMaxWidth()
                .height(330.dp)
        ) {
            val gap = 7.dp
        
            LazyHorizontalGrid(
            rows = GridCells.Fixed(3),
            state = rememberLazyGridState(),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 8.dp,
                end = 8.dp,
                bottom = 4.dp
            ),
            horizontalArrangement =
                Arrangement.spacedBy(gap),
            verticalArrangement =
                Arrangement.spacedBy(gap)
        ) {
            items(
                items = songs,
                key = { it.id },
                contentType = { "song" }
            ) { song ->
                SongGridTile(
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongGridTile(
    song: Song,
    c: HomeColors,
    theme: XmoTheme,
    playing: Boolean,
    active: Boolean,
    play: () -> Unit,
    options: () -> Unit
) {
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
                androidx.compose.ui.graphics.Color.Black
                    .copy(alpha = .065f)

            XmoTheme.Dark ->
                androidx.compose.ui.graphics.Color.White
                    .copy(alpha = .06f)

            XmoTheme.Amoled ->
                androidx.compose.ui.graphics.Color.White
                    .copy(alpha = .085f)
        }

    Column(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(surface)
            .border(
                .4.dp,
                border,
                RoundedCornerShape(10.dp)
            )
            .combinedClickable(
                onClick = play,
                onLongClick = options
            )
            .padding(4.dp)
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
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (playing) {
                    PlayingWave(
                        active = active,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(5.dp)
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 3.dp),
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
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text = song.artist,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 7.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .size(22.dp)
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
                    contentDescription = "Options",
                    tint = c.sub,
                    modifier = Modifier.size(15.dp)
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
            label = "playingWave"
        )

    val scale by
        transition.animateFloat(
            initialValue = .78f,
            targetValue = 1.15f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(430),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label = "waveScale"
        )

    Box(
        modifier
            .size(25.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                androidx.compose.ui.graphics.Color.Black
                    .copy(alpha = .58f)
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.GraphicEq,
            contentDescription = "Playing",
            tint = LocalXmoAccent.current,
            modifier = Modifier
                .size(17.dp)
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
}
