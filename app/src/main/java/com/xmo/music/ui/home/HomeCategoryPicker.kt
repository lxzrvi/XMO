package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeCategoryPicker(
    category: UserCategory,
    songs: List<Song>,
    c: HomeColors,
    back: () -> Unit,
    setMembership: (
        Song,
        Boolean
    ) -> Unit
) {
    BackHandler(onBack = back)

    val ordered =
        remember(
            songs,
            category.songIds
        ) {
            val indexes =
                songs.mapIndexed {
                        index,
                        song ->
                    song.id to index
                }.toMap()

            songs.sortedWith(
                compareByDescending<Song> {
                    it.id in category.songIds
                }.thenBy {
                    indexes[it.id] ?: Int.MAX_VALUE
                }
            )
        }

    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 8.dp
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

            Column {
                Text(
                    text = "Add Songs",
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 19.sp
                )

                Text(
                    text = category.name,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 9.sp
                )
            }
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
                items = ordered,
                key = { it.id }
            ) { song ->
                val added =
                    song.id in category.songIds

                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            c.surface,
                            androidx.compose.foundation.shape
                                .RoundedCornerShape(13.dp)
                        )
                        .padding(
                            start = 8.dp,
                            top = 6.dp,
                            bottom = 6.dp,
                            end = 4.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                    ) {
                        Text(
                            text = song.title,
                            color = c.text,
                            fontFamily = XmoFont.bold,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        Text(
                            text = song.artist,
                            color = c.sub,
                            fontFamily = XmoFont.normal,
                            fontSize = 9.sp,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = {
                            setMembership(
                                song,
                                !added
                            )
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (added) {
                                    Icons.Rounded.Check
                                } else {
                                    Icons.Rounded.Add
                                },
                            contentDescription =
                                if (added) {
                                    "Remove"
                                } else {
                                    "Add"
                                },
                            tint =
                                if (added) {
                                    Color(0xFF34C759)
                                } else {
                                    c.icon
                                },
                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
