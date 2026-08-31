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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeAllSongs(
    songs: List<Song>,
    allowed: Boolean,
    c: HomeColors,
    theme: XmoTheme,
    currentSongId: Long?,
    isPlaying: Boolean,
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
        remember(songs.size) {
            ((songs.size + 19) / 20) * 20
        }

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge = 8.dp
        val gap = 6.dp

        val cardWidth =
            (
                maxWidth -
                    edge * 2 -
                    gap * 4
                ) / 5

        val cardHeight =
            cardWidth + 34.dp

        val gridHeight =
            cardHeight * 4 +
                gap * 3

        LazyHorizontalGrid(
            rows = GridCells.Fixed(4),
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
                    "song"
                }
            ) { slot ->
                val page = slot / 20
                val local = slot % 20
                val row = local % 4
                val column = local / 4

                val sourceIndex =
                    page * 20 +
                        row * 5 +
                        column

                Box(
                    Modifier
                        .width(cardWidth)
                        .height(cardHeight)
                ) {
                    songs
                        .getOrNull(sourceIndex)
                        ?.let { song ->
                            HomeSongTile(
                                song = song,
                                c = c,
                                theme = theme,
                                current =
                                    currentSongId ==
                                        song.id,
                                playing =
                                    currentSongId ==
                                        song.id &&
                                        isPlaying,
                                play = {
                                    if (
                                        currentSongId !=
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
    current: Boolean,
    playing: Boolean,
    play: () -> Unit,
    options: () -> Unit
) {
    val context =
        LocalContext.current

    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val request =
        remember(song.artwork) {
            ImageRequest.Builder(context)
                .data(song.artwork)
                .size(160, 160)
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

    val surface =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF9F9FA)

            XmoTheme.Dark ->
                Color(0xFF181819)

            XmoTheme.Amoled ->
                Color(0xFF080808)
        }

    val normalBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(alpha = .06f)

            XmoTheme.Dark ->
                Color.White.copy(alpha = .06f)

            XmoTheme.Amoled ->
                Color.White.copy(alpha = .085f)
        }

    val border =
        if (current) {
            LocalXmoAccent.current
                .copy(alpha = .82f)
        } else {
            normalBorder
        }

    Column(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                val scale =
                    if (pressed) {
                        .955f
                    } else {
                        1f
                    }

                scaleX = scale
                scaleY = scale
            }
            .background(
                surface,
                RoundedCornerShape(9.dp)
            )
            .border(
                if (current) {
                    .9.dp
                } else {
                    .4.dp
                },
                border,
                RoundedCornerShape(9.dp)
            )
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
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
                    .background(
                        c.button,
                        RoundedCornerShape(6.dp)
                    )
            ) {
                AsyncImage(
                    model = request,
                    contentDescription = null,
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
                        fontSize = 15.sp,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
                    )
                }

                if (current) {
                    HomePlayingWave(
                        active = playing,
                        modifier = Modifier
                            .align(
                                Alignment.BottomEnd
                            )
                    )
                }
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = song.title,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 8.5.sp,
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
                modifier.size(18.dp)
        )

        return
    }

    val transition =
        rememberInfiniteTransition(
            label = "playingWave"
        )

    val scale by
        transition.animateFloat(
            initialValue = .68f,
            targetValue = 1.16f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(360),
                    repeatMode =
                        RepeatMode.Reverse
                ),
            label = "playingWaveScale"
        )

    Icon(
        imageVector =
            Icons.Rounded.GraphicEq,
        contentDescription = "Playing",
        tint = LocalXmoAccent.current,
        modifier = modifier
            .size(18.dp)
            .graphicsLayer {
                scaleY = scale
            }
    )
}
