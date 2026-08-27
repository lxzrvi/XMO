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
                modifier = Modifier.padding(18.dp, 12.dp, 18.dp, 22.dp)
            )

            SectionTitle(
                "All Songs",
                if (allowed) "All songs: ${songs.size}" else "Music access required",
                Icons.Rounded.Album,
                c
            )

            if (!allowed) {
                Text(
                    "Allow music access to show songs on this device.",
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(18.dp, 12.dp, 18.dp, 26.dp)
                )
            } else if (songs.isEmpty()) {
                Text(
                    "No local music found",
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(18.dp, 12.dp, 18.dp, 26.dp)
                )
            } else {
                SongPages(songs, c)
            }

            val albums = Library.albums(songs)
            SectionTitle(
                "Albums",
                "${albums.size} albums",
                Icons.Rounded.LibraryMusic,
                c
            )

            SectionTitle(
                "Liked Songs",
                "0 favorites",
                Icons.Rounded.Favorite,
                c
            )

            Text(
                "No liked songs yet",
                color = c.sub,
                fontFamily = XmoFont.normal,
                fontSize = 13.sp,
                modifier = Modifier.padding(18.dp, 10.dp, 18.dp, 22.dp)
            )

            val artists = Library.artists(songs)
            SectionTitle(
                "Top Artists",
                "${artists.size} artists",
                Icons.Rounded.People,
                c
            )

            if (artists.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    artists.take(12).forEach { artist ->
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
                                    artist.name.take(1).uppercase(),
                                    color = XmoRed,
                                    fontFamily = XmoFont.bold,
                                    fontSize = 18.sp
                                )
                            }
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

            Spacer(Modifier.height(32.dp))

            Text(
                "XMO",
                color = c.text,
                fontFamily = XmoFont.logo,
                fontSize = 25.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
        }
    }
}

@Composable
private fun SongPages(songs: List<Song>, c: HomeColors) {
    val pages = songs.chunked(12)
    val scroll = rememberScrollState()

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        pages.forEachIndexed { pageIndex, page ->
            Column(
                Modifier
                    .fillParentMaxWidth()
                    .padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { row ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(4) { col ->
                            val local = row * 4 + col
                            val song = page.getOrNull(local)

                            Box(Modifier.weight(1f)) {
                                if (song != null)
                                    SongTile(song, pageIndex * 12 + local, c)
                            }
                        }
                    }
                }
            }
        }
    }
}
