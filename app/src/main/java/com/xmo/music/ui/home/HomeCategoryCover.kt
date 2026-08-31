package com.xmo.music.ui.home

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song

@Composable
internal fun HomeCategoryCover(
    songs: List<Song>,
    cover: String?,
    c: HomeColors,
    modifier: Modifier = Modifier
) {
    val singleSongId =
        cover
            ?.takeIf {
                it.startsWith("song:")
            }
            ?.removePrefix("song:")
            ?.toLongOrNull()

    val customUri =
        cover
            ?.takeIf {
                it.startsWith("uri:")
            }
            ?.removePrefix("uri:")
            ?.let(Uri::parse)

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(c.button)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(maxWidth)
                .clip(
                    RoundedCornerShape(16.dp)
                )
        ) {
            when {
                customUri != null -> {
                    AsyncImage(
                        model = customUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                singleSongId != null -> {
                    val song =
                        songs.firstOrNull {
                            it.id == singleSongId
                        }

                    AsyncImage(
                        model = song?.artwork,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {
                    DefaultCategoryCollage(
                        songs = songs,
                        c = c
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultCategoryCollage(
    songs: List<Song>,
    c: HomeColors
) {
    BoxWithConstraints(
        Modifier.fillMaxSize()
    ) {
        val half =
            maxWidth / 2

        repeat(4) { index ->
            val song =
                songs.getOrNull(index)

            Box(
                Modifier
                    .offset(
                        x =
                            if (index % 2 == 0) {
                                0.dp
                            } else {
                                half
                            },
                        y =
                            if (index < 2) {
                                0.dp
                            } else {
                                half
                            }
                    )
                    .size(half)
                    .background(c.button)
            ) {
                if (song != null) {
                    AsyncImage(
                        model = song.artwork,
                        contentDescription = null,
                        modifier =
                            Modifier.fillMaxSize(),
                        contentScale =
                            ContentScale.Crop
                    )
                }
            }
        }
    }
}
