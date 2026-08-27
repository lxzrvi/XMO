package com.xmo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.XmoTheme
import com.xmo.music.data.Library
import com.xmo.music.data.Song

@Composable
fun Home(
    songs: List<Song>,
    allowed: Boolean,
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit
) {
    val c = theme.colors()

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(bottom = 150.dp)
        ) {
            HomeHeader(c, theme, setTheme, refresh)
            Categories(c)

            SectionTitle(
                "Recently Played",
                "Nothing played yet",
                Icons.Rounded.History,
                c
            )

            Text(
                "Nothing played yet",
                color = c.sub,
                fontFamily = XmoFont.normal,
                fontSize = 13.sp,
                modifier = Modifier.padding(
                    start = 18.dp,
                    top = 12.dp,
                    bottom = 22.dp
                )
            )

            SectionTitle(
                "All Songs",
                if (allowed)
                    "All songs: ${songs.size}"
                else
                    "Music access required",
                Icons.Rounded.Album,
                c
            )

            when {
                !allowed -> EmptyHomeText(
                    "Allow music access to show songs on this device.",
                    c
                )

                songs.isEmpty() -> EmptyHomeText(
                    "No local music found",
                    c
                )

                else -> SongPages(songs, c)
            }

            val albums = Library.albums(songs)

            SectionTitle(
                "Albums",
                "${albums.size} albums",
                Icons.Rounded.LibraryMusic,
                c
            )

            if (albums.isEmpty()) {
                EmptyHomeText("No albums found", c)
            }

            SectionTitle(
                "Liked Songs",
                "0 favorites",
                Icons.Rounded.Favorite,
                c
            )

            EmptyHomeText("No liked songs yet", c)

            val artists = Library.artists(songs)

            SectionTitle(
                "Top Artists",
                "${artists.size} artists",
                Icons.Rounded.People,
                c
            )

            if (artists.isEmpty()) {
                EmptyHomeText("No artists found", c)
            } else {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    artists.take(15).forEach { artist ->
                        Column(
                            Modifier.width(62.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(58.dp)
                                    .background(
                                        XmoRed.copy(.16f),
                                        androidx.compose.foundation.shape.CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    artist.name
                                        .firstOrNull()
                                        ?.uppercase()
                                        ?: "?",
                                    color = XmoRed,
                                    fontFamily = XmoFont.bold,
                                    fontSize = 18.sp
                                )
                            }

                            Spacer(Modifier.height(5.dp))

                            Text(
                                artist.name,
                                color = c.text,
                                fontFamily = XmoFont.medium,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(35.dp))

            Text(
                "XMO",
                color = c.text,
                fontFamily = XmoFont.logo,
                fontSize = 25.sp,
                modifier = Modifier.align(
                    Alignment.CenterHorizontally
                )
            )

            Text(
                "lxzrvi  •  copyright © 2026",
                color = c.sub,
                fontFamily = XmoFont.thin,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 3.dp)
            )

            Spacer(Modifier.height(15.dp))
        }
    }
}

@Composable
private fun EmptyHomeText(
    text: String,
    c: HomeColors
) {
    Text(
        text,
        color = c.sub,
        fontFamily = XmoFont.normal,
        fontSize = 13.sp,
        modifier = Modifier.padding(
            start = 18.dp,
            end = 18.dp,
            top = 10.dp,
            bottom = 22.dp
        )
    )
}

@Composable
private fun SongPages(
    songs: List<Song>,
    c: HomeColors
) {
    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val pageWidth = maxWidth
        val pages = songs.chunked(12)

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            pages.forEachIndexed { pageIndex, page ->
                Column(
                    Modifier
                        .width(pageWidth)
                        .padding(
                            start = 12.dp,
                            end = 12.dp,
                            top = 4.dp,
                            bottom = 8.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement =
                                Arrangement.spacedBy(8.dp)
                        ) {
                            repeat(4) { col ->
                                val local = row * 4 + col
                                val song = page.getOrNull(local)

                                Box(
                                    Modifier.weight(1f)
                                ) {
                                    if (song != null) {
                                        SongTile(
                                            song,
                                            pageIndex * 12 + local,
                                            c
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
