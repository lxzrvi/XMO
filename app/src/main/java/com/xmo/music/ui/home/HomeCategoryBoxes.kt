package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Collections
import androidx.compose.material.icons.rounded.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeRenameCategoryBox(
    value: String,
    c: HomeColors,
    change: (String) -> Unit,
    dismiss: () -> Unit,
    save: () -> Unit
) {
    XmoBox(
        title = "XMO",
        c = c,
        dismiss = dismiss
    ) {
        BasicTextField(
            value = value,
            onValueChange = change,
            singleLine = true,
            textStyle =
                androidx.compose.ui.text.TextStyle(
                    color = c.text,
                    fontFamily = XmoFont.normal,
                    fontSize = 14.sp
                ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    c.button,
                    RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp)
        )

        androidx.compose.foundation.layout.Spacer(
            Modifier.height(12.dp)
        )

        HomeDialogAction(
            text = "Save",
            enabled = value.trim().isNotEmpty(),
            click = save
        )
    }
}

@Composable
internal fun HomeCategoryCoverList(
    category: UserCategory,
    songs: List<Song>,
    c: HomeColors,
    dismiss: () -> Unit,
    default: () -> Unit,
    songCover: (Song) -> Unit,
    custom: () -> Unit
) {
    XmoList(
        c = c,
        dismiss = dismiss
    ) {
        XmoListAction(
            title = "Default 4 Covers",
            icon = Icons.Rounded.Collections,
            c = c,
            click = default
        )

        XmoListAction(
            title = "Custom Gallery Image",
            icon = Icons.Rounded.Image,
            c = c,
            click = custom
        )

        if (songs.isNotEmpty()) {
            androidx.compose.material3.Text(
                text = "SONG COVER",
                color =
                    com.xmo.music.ui
                        .LocalXmoAccent.current,
                fontFamily = XmoFont.bold,
                fontSize = 9.sp,
                modifier =
                    Modifier.padding(
                        start = 18.dp,
                        top = 12.dp,
                        bottom = 5.dp
                    )
            )

            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        rememberScrollState()
                    )
            ) {
                songs.forEach {
                    XmoListAction(
                        title = it.title,
                        icon = Icons.Rounded.Image,
                        c = c
                    ) {
                        songCover(it)
                    }
                }
            }
        }
    }
}
