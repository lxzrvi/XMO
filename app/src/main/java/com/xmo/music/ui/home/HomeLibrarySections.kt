package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Album
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeAlbums(
    albums: List<Album>,
    c: HomeColors,
    open: (Album) -> Unit
) {
    if (albums.isEmpty()) {
        HomeEmpty("No albums found", c)
        return
    }

    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        albums.take(20).forEach { album ->
            Column(
                Modifier
                    .width(106.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .clickable {
                        open(album)
                    }
                    .padding(4.dp)
            ) {
                AsyncImage(
                    model = album.artwork,
                    contentDescription = album.name,
                    modifier = Modifier
                        .size(98.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(c.button),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = album.name,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 5.dp)
                )

                Text(
                    text = album.artist,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun HomeArtists(
    songs: List<Song>,
    c: HomeColors
) {
    val artists = remember(songs) {
        Library.artists(songs)
    }

    if (artists.isEmpty()) {
        HomeEmpty("No artists found", c)
        return
    }

    Row(
        Modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        artists.take(20).forEach { artist ->
            Column(
                Modifier.width(82.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(78.dp)
                        .clip(CircleShape)
                        .background(
                            LocalXmoAccent.current.copy(alpha = .14f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (artist.artwork != null) {
                        AsyncImage(
                            model = artist.artwork,
                            contentDescription = artist.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = artist.name
                                .firstOrNull()
                                ?.uppercase()
                                ?: "?",
                            color = LocalXmoAccent.current,
                            fontFamily = XmoFont.bold,
                            fontSize = 21.sp
                        )
                    }
                }

                Text(
                    text = artist.name,
                    color = c.text,
                    fontFamily = XmoFont.medium,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}
