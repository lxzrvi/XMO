package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeSongRow(
    song: Song,
    c: HomeColors,
    playing: Boolean = false,
    play: () -> Unit,
    options: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(c.surface)
            .border(
                .5.dp,
                c.border,
                RoundedCornerShape(13.dp)
            )
            .combinedClickable(
                onClick = play,
                onLongClick = options
            )
            .padding(5.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(c.button),
            contentScale = ContentScale.Crop
        )

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
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
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text =
                    if (playing) {
                        "Playing • ${song.artist}"
                    } else {
                        song.artist
                    },
                color =
                    if (playing) {
                        LocalXmoAccent.current
                            .copy(alpha = .8f)
                    } else {
                        c.sub
                    },
                fontFamily = XmoFont.normal,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = options,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription = "Options",
                tint = c.sub,
                modifier = Modifier.size(19.dp)
            )
        }
    }
}
