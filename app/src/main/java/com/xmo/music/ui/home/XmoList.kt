package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun XmoList(
    c: HomeColors,
    dismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val maxHeight =
        LocalConfiguration.current
            .screenHeightDp.dp * .70f

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )

    BackHandler(onBack = dismiss)

    ModalBottomSheet(
        onDismissRequest = dismiss,
        sheetState = sheetState,
        containerColor = c.surface,
        contentColor = c.text,
        scrimColor =
            androidx.compose.ui.graphics.Color.Transparent
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = maxHeight)
                .verticalScroll(
                    rememberScrollState()
                )
        ) {
            content()
        }
    }
}

@Composable
internal fun XmoSongList(
    song: Song,
    liked: Boolean,
    recent: Boolean,
    c: HomeColors,
    dismiss: () -> Unit,
    toggleLike: () -> Unit,
    playNext: () -> Unit,
    removeRecent: () -> Unit
) {
    XmoList(
        c = c,
        dismiss = dismiss
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.artwork,
                contentDescription = null,
                modifier = Modifier.size(58.dp)
            )

            Column(
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = song.title,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 14.sp,
                    maxLines = 1
                )

                Text(
                    text = song.artist,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }

        XmoListAction(
            title = if (liked) {
                "Remove from Liked Songs"
            } else {
                "Add to Liked Songs"
            },
            icon = if (liked) {
                Icons.Rounded.Favorite
            } else {
                Icons.Rounded.FavoriteBorder
            },
            active = liked,
            c = c,
            click = toggleLike
        )

        XmoListAction(
            title = "Play Next",
            icon = Icons.Rounded.QueueMusic,
            c = c,
            click = {
                playNext()
                dismiss()
            }
        )

        if (recent) {
            XmoListAction(
                title = "Remove from Recent",
                icon = Icons.Rounded.Delete,
                c = c,
                click = {
                    removeRecent()
                    dismiss()
                }
            )
        }
    }
}

@Composable
internal fun XmoListAction(
    title: String,
    icon: ImageVector,
    c: HomeColors,
    active: Boolean = false,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = click)
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) {
                LocalXmoAccent.current
            } else {
                c.icon
            },
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = title,
            color = if (active) {
                LocalXmoAccent.current
            } else {
                c.text
            },
            fontFamily = XmoFont.medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 14.dp)
        )
    }
}
