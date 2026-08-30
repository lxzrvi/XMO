package com.xmo.music.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.ui.XmoFont

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

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val edge = 8.dp
        val gap = 8.dp
        val cardWidth =
            (maxWidth - edge * 2 - gap * 3) / 4

        val cardHeight =
            cardWidth + 37.dp

        val height =
            cardHeight * 3 +
                gap * 2

        val scroll =
            rememberScrollState()

        Row(
            Modifier
                .fillMaxWidth()
                .height(height)
                .horizontalScroll(scroll)
                .padding(horizontal = edge),
            horizontalArrangement =
                Arrangement.spacedBy(gap)
        ) {
            songs
                .chunked(3)
                .forEach { column ->
                    Column(
                        Modifier.width(cardWidth),
                        verticalArrangement =
                            Arrangement.spacedBy(gap)
                    ) {
                        column.forEach { song ->
                            key(song.id) {
                                HomeSongTile(
                                    song = song,
                                    c = c,
                                    theme = theme,
                                    modifier = Modifier
                                        .width(cardWidth)
                                        .height(cardHeight),
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
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeSongTile(
    song: Song,
    c: HomeColors,
    theme: XmoTheme,
    modifier: Modifier,
    onClick: () -> Unit,
    onOptions: () -> Unit
) {
    val border =
        when (theme) {
            XmoTheme.Light ->
                androidx.compose.ui.graphics.Color
                    .Black.copy(alpha = .07f)

            XmoTheme.Dark ->
                androidx.compose.ui.graphics.Color
                    .White.copy(alpha = .065f)

            XmoTheme.Amoled ->
                androidx.compose.ui.graphics.Color
                    .White.copy(alpha = .09f)
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

    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onOptions
            )
            .background(surface)
            .border(
                .45.dp,
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
                        text =
                            song.title
                                .firstOrNull()
                                ?.uppercase()
                                ?: "X",
                        color =
                            c.text.copy(alpha = .65f),
                        fontFamily = XmoFont.bold,
                        fontSize = 17.sp,
                        modifier =
                            Modifier.align(
                                Alignment.Center
                            )
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
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
