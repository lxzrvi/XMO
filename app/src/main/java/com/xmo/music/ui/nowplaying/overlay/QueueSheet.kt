package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun QueueSheet(
    queue: List<Song>,
    currentSongId: Long?,
    colors: HomeColors,
    dismiss: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Color.Black
                    .copy(alpha = .28f)
            )
            .clickable(
                onClick = dismiss
            ),
        contentAlignment =
            Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(
                    LocalConfiguration.current
                        .screenHeightDp.dp *
                        .72f
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp
                    )
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(top = 12.dp)
        ) {
            Box(
                Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .width(42.dp)
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(2.dp)
                    )
                    .background(
                        colors.sub.copy(
                            alpha = .28f
                        )
                    )
            )

            Text(
                text = "Queue",
                color = colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 19.sp,
                modifier =
                    Modifier.padding(
                        start = 18.dp,
                        top = 16.dp,
                        bottom = 11.dp
                    )
            )

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 30.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                items(
                    items = queue,
                    key = { it.id }
                ) { song ->
                    QueueRow(
                        song = song,
                        active =
                            song.id ==
                                currentSongId,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    active: Boolean,
    colors: HomeColors
) {
    val accent =
        LocalXmoAccent.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                if (active) {
                    accent.copy(
                        alpha = .10f
                    )
                } else {
                    colors.button
                }
            )
            .padding(5.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(
                        RoundedCornerShape(
                            10.dp
                        )
                    )
                    .background(
                        colors.button
                    ),
            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(1f)
                .padding(
                    horizontal = 10.dp
                )
        ) {
            Text(
                text = song.title,
                color =
                    if (active) {
                        accent
                    } else {
                        colors.text
                    },
                fontFamily =
                    XmoFont.bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 10.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}
