package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeCreateCategory(
    name: String,
    selected: Set<Long>,
    songs: List<Song>,
    c: HomeColors,
    changeName: (String) -> Unit,
    toggle: (Song) -> Unit,
    create: () -> Unit,
    dismiss: () -> Unit
) {
    XmoBox(
        title = "New Category",
        c = c,
        dismiss = dismiss
    ) {
        BasicTextField(
            value = name,
            onValueChange = {
                changeName(it.take(24))
            },
            singleLine = true,
            textStyle =
                androidx.compose.ui.text.TextStyle(
                    color = c.text,
                    fontFamily = XmoFont.normal,
                    fontSize = 14.sp
                ),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .background(
                    c.button,
                    androidx.compose.foundation.shape
                        .RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 12.dp),
            decorationBox = { field ->
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    if (name.isBlank()) {
                        Text(
                            text = "Category name",
                            color = c.sub,
                            fontFamily = XmoFont.normal,
                            fontSize = 12.sp
                        )
                    }
                    field()
                }
            }
        )

        Text(
            text =
                "${selected.size} songs added",
            color =
                if (selected.isNotEmpty()) {
                    Color(0xFF34C759)
                } else {
                    c.sub
                },
            fontFamily = XmoFont.medium,
            fontSize = 10.sp,
            modifier = Modifier.padding(
                top = 10.dp,
                bottom = 6.dp
            )
        )

        Column(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .verticalScroll(
                    rememberScrollState()
                ),
            verticalArrangement =
                Arrangement.spacedBy(2.dp)
        ) {
            songs.forEach { song ->
                val added =
                    song.id in selected

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(start = 4.dp)
                    ) {
                        Text(
                            text = song.title,
                            color = c.text,
                            fontFamily = XmoFont.medium,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        Text(
                            text = song.artist,
                            color = c.sub,
                            fontFamily = XmoFont.normal,
                            fontSize = 8.sp,
                            maxLines = 1
                        )
                    }

                    IconButton(
                        onClick = {
                            toggle(song)
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (added) {
                                    Icons.Rounded.Check
                                } else {
                                    Icons.Rounded.Add
                                },
                            contentDescription = null,
                            tint =
                                if (added) {
                                    Color(0xFF34C759)
                                } else {
                                    c.icon
                                },
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }

        HomeDialogAction(
            text = "Create",
            enabled =
                name.trim().isNotEmpty() &&
                    selected.isNotEmpty(),
            click = create
        )
    }
}
