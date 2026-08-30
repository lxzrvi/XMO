package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs

@Composable
internal fun HomeRecentlyPlayed(
    songs: List<Song>,
    c: HomeColors,
    play: (Song) -> Unit,
    options: (Song) -> Unit
) {
    if (songs.isEmpty()) return

    val middle = remember(songs.size) {
        val center = Int.MAX_VALUE / 2
        center - center % songs.size
    }

    val state = rememberLazyListState(
        initialFirstVisibleItemIndex = middle
    )

    var selected by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(state, songs.size) {
        snapshotFlow {
            val layout = state.layoutInfo
            val center =
                (layout.viewportStartOffset + layout.viewportEndOffset) / 2

            layout.visibleItemsInfo.minByOrNull {
                abs((it.offset + it.size / 2) - center)
            }?.index
        }
            .distinctUntilChanged()
            .collect { index ->
                if (index != null) {
                    selected = Math.floorMod(index, songs.size)
                }
            }
    }

    BoxWithConstraints(
        Modifier.fillMaxWidth()
    ) {
        val cardWidth = 284.dp
        val sidePadding =
            ((maxWidth - cardWidth) / 2).coerceAtLeast(12.dp)

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyRow(
                state = state,
                contentPadding = PaddingValues(horizontal = sidePadding),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    count = Int.MAX_VALUE,
                    key = { it }
                ) { index ->
                    val song =
                        songs[Math.floorMod(index, songs.size)]

                    RecentSongCard(
                        song = song,
                        c = c,
                        play = {
                            play(song)
                        },
                        options = {
                            options(song)
                        }
                    )
                }
            }

            Row(
                Modifier.padding(top = 11.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                songs.forEachIndexed { index, _ ->
                    Box(
                        Modifier
                            .size(
                                if (selected == index) {
                                    7.dp
                                } else {
                                    5.dp
                                }
                            )
                            .clip(CircleShape)
                            .background(
                                if (selected == index) {
                                    LocalXmoAccent.current
                                } else {
                                    c.sub.copy(alpha = .35f)
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentSongCard(
    song: Song,
    c: HomeColors,
    play: () -> Unit,
    options: () -> Unit
) {
    Box(
        Modifier
            .width(284.dp)
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = play,
                onLongClick = options
            )
            .background(c.surface)
            .border(
                .7.dp,
                c.border,
                RoundedCornerShape(18.dp)
            )
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = song.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = .82f)
                        )
                    )
                )
        )

        Text(
            text = song.title,
            color = Color.White,
            fontFamily = XmoFont.bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(.76f)
                .padding(12.dp)
        )

        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(34.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = .46f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}
