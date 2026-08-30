package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeCategoryDetail(
    category: UserCategory,
    songs: List<Song>,
    playback: PlaybackState,
    c: HomeColors,
    back: () -> Unit,
    add: () -> Unit,
    delete: () -> Unit,
    shuffle: (List<Song>) -> Unit,
    play: (Song, List<Song>) -> Unit,
    options: (Song) -> Unit
) {
    BackHandler(onBack = back)

    val categorySongs =
        songs.filter {
            it.id in category.songIds
        }

    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 7.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IconButton(onClick = back) {
                Icon(
                    imageVector =
                        Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = c.text
                )
            }

            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 19.sp
                )

                Text(
                    text =
                        "${categorySongs.size} songs",
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 9.sp
                )
            }

            IconButton(
                enabled = categorySongs.isNotEmpty(),
                onClick = {
                    shuffle(categorySongs)
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = "Shuffle",
                    tint = LocalXmoAccent.current
                )
            }

            IconButton(onClick = add) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Songs",
                    tint = LocalXmoAccent.current
                )
            }

            IconButton(onClick = delete) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete",
                    tint = c.sub
                )
            }
        }

        if (categorySongs.isEmpty()) {
            HomeEmpty(
                "No songs in this category",
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
                items = categorySongs,
                key = { it.id }
            ) { song ->
                HomeSongRow(
                    song = song,
                    c = c,
                    playing =
                        playback.currentSongId ==
                            song.id,
                    play = {
                        if (
                            playback.currentSongId !=
                            song.id
                        ) {
                            play(
                                song,
                                categorySongs
                            )
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
