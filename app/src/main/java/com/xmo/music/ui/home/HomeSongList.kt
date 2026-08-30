package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.xmo.music.data.Song
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeFullSongList(
    model: HomeLayer.SongList,
    c: HomeColors,
    close: () -> Unit,
    play: (Song, String, Boolean, List<Song>) -> Unit,
    options: (Song) -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(500f)
            .background(c.bg)
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(66.dp)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = close,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            c.button,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = c.text,
                        modifier = Modifier.size(25.dp)
                    )
                }

                Column(
                    Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = model.title,
                        color = c.text,
                        fontFamily = XmoFont.bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "${model.songs.size} songs",
                        color = c.sub,
                        fontFamily = XmoFont.normal,
                        fontSize = 9.sp
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = 190.dp
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = model.songs,
                    key = { it.id }
                ) { song ->
                    HomeSongRow(
                        song = song,
                        c = c,
                        play = {
                            play(
                                song,
                                model.source,
                                model.category,
                                model.songs
                            )
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
