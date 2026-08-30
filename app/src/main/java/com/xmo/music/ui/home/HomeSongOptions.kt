package com.xmo.music.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSongOptions(
    song: Song,
    liked: Boolean,
    categories: List<UserCategory>,
    c: HomeColors,
    dismiss: () -> Unit,
    toggleLike: () -> Unit,
    setCategory: (String, Boolean) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = dismiss,
        containerColor = c.surface,
        contentColor = c.text
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.artwork,
                contentDescription = null,
                modifier = Modifier
                    .size(58.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
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
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${song.artist} • ${song.album}",
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        HomeMaterialOption(
            title = if (liked) {
                "Remove from Liked Songs"
            } else {
                "Add to Liked Songs"
            },
            active = liked,
            icon = if (liked) {
                Icons.Rounded.Favorite
            } else {
                Icons.Rounded.FavoriteBorder
            },
            c = c,
            click = toggleLike
        )

        if (categories.isNotEmpty()) {
            Text(
                text = "CATEGORIES",
                color = LocalXmoAccent.current,
                fontFamily = XmoFont.bold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(
                    start = 22.dp,
                    top = 14.dp,
                    bottom = 5.dp
                )
            )

            categories.forEach { category ->
                val added = song.id in category.songIds

                HomeMaterialOption(
                    title = category.name,
                    active = added,
                    icon = Icons.Rounded.Add,
                    c = c
                ) {
                    setCategory(category.id, !added)
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HomeMaterialOption(
    title: String,
    active: Boolean,
    icon: ImageVector,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = click)
            .padding(
                horizontal = 20.dp,
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) {
                LocalXmoAccent.current
            } else {
                c.icon
            },
            modifier = Modifier.size(19.dp)
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
