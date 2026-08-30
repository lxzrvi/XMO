package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeCategories(
    songs: List<Song>,
    categories: List<UserCategory>,
    c: HomeColors,
    back: () -> Unit,
    create: () -> Unit,
    open: (UserCategory) -> Unit
) {
    BackHandler(onBack = back)

    val songsById =
        remember(songs) {
            songs.associateBy { it.id }
        }

    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    end = 12.dp,
                    top = 10.dp,
                    bottom = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    text = "Categories",
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 21.sp
                )

                Text(
                    text = "${categories.size} categories",
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 9.sp
                )
            }

            Box(
                Modifier
                    .background(
                        LocalXmoAccent.current.copy(
                            alpha = .14f
                        ),
                        CircleShape
                    )
            ) {
                IconButton(onClick = create) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Create category",
                        tint = LocalXmoAccent.current
                    )
                }
            }
        }

        if (categories.isEmpty()) {
            HomeEmpty(
                "Create your first category",
                c
            )
            return
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding =
                PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 190.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = categories,
                key = { it.id }
            ) { category ->
                val covers =
                    category.songIds
                        .mapNotNull(songsById::get)
                        .take(4)

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            open(category)
                        }
                ) {
                    CategoryCollage(
                        covers = covers,
                        c = c
                    )

                    Text(
                        text = category.name,
                        color = c.text,
                        fontFamily = XmoFont.medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier = Modifier.padding(
                            top = 6.dp,
                            start = 3.dp,
                            end = 3.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryCollage(
    covers: List<Song>,
    c: HomeColors
) {
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(c.button)
    ) {
        val half = maxWidth / 2

        Box(
            Modifier
                .fillMaxWidth()
                .height(maxWidth)
        ) {
            covers.forEachIndexed { index, song ->
                AsyncImage(
                    model = song.artwork,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(
                            start =
                                if (index % 2 == 0) {
                                    0.dp
                                } else {
                                    half
                                },
                            top =
                                if (index < 2) {
                                    0.dp
                                } else {
                                    half
                                }
                        )
                        .height(half)
                        .fillMaxWidth(.5f)
                )
            }
        }
    }
}
