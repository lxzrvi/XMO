package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeLiked(
    songs: List<Song>,
    playback: PlaybackState,
    c: HomeColors,
    play: (Song) -> Unit,
    shuffle: () -> Unit,
    options: (Song) -> Unit
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 12.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = "Liked Songs",
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 21.sp
                )

                Text(
                    text = "${songs.size} songs",
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 10.sp
                )
            }

            Box(
                Modifier
                    .size(42.dp)
                    .background(
                        LocalXmoAccent.current
                            .copy(alpha = .15f),
                        CircleShape
                    )
                    .clickable(
                        enabled = songs.isNotEmpty(),
                        onClick = shuffle
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle All",
                    tint = LocalXmoAccent.current,
                    modifier = Modifier.size(21.dp)
                )
            }
        }

        if (songs.isEmpty()) {
            HomeEmpty(
                "No liked songs yet",
                c
            )
            return
        }

        LazyColumn(
            contentPadding =
                PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 190.dp
                ),
            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {
            items(
                items = songs,
                key = { it.id }
            ) { song ->
                HomeSongRow(
                    song = song,
                    c = c,
                    playing =
                        playback.currentSongId ==
                            song.id,
                    play = {
                        play(song)
                    },
                    options = {
                        options(song)
                    }
                )
            }
        }
    }
}
