package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Expand
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.X
import com.xmo.music.XmoTheme
import com.xmo.music.data.SongLyrics
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.delay

/*
 * =============================================================
 * COVER-SIZED LYRICS
 * =============================================================
 */

@Composable
internal fun ArtworkLyrics(
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
    accent: Color,
    pickLyrics: () -> Unit,
    fullscreenLyrics: () -> Unit,
    showArtwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(
                Color.Black.copy(
                    alpha = .18f
                )
            )
    ) {
        FollowLyrics(
            lyrics = lyrics,
            position = position,
            colors = colors,
            accent = accent,
            fullscreen = false,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        top = 45.dp,
                        bottom = 30.dp
                    )
        )

        /*
         * Local LRC + fullscreen controls remain inside the
         * artwork footprint.
         */
        XmoCapsule(
            background =
                colors.text.copy(
                    alpha = .10f
                ),
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(10.dp)
        ) {
            CapsuleButton(
                size = 38.dp,
                onClick = pickLyrics
            ) {
                Icon(
                    imageVector =
                        Lucide.Plus,
                    contentDescription =
                        "Choose local lyrics",
                    tint = colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 38.dp,
                onClick =
                    fullscreenLyrics
            ) {
                Icon(
                    imageVector =
                        Lucide.Expand,
                    contentDescription =
                        "Fullscreen lyrics",
                    tint = colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }
        }

        /*
         * Explicitly returns to artwork. Scrolling/touching the
         * lyrics themselves does not close this surface.
         */
        Text(
            text = "ARTWORK",
            color =
                colors.text.copy(
                    alpha = .48f
                ),
            fontFamily =
                XmoFont.medium,
            fontSize = 9.sp,
            letterSpacing = .7.sp,
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .clip(
                        RoundedCornerShape(10.dp)
                    )
                    .clickable(
                        onClick = showArtwork
                    )
                    .padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    )
        )
    }
}

/*
 * =============================================================
 * FOLLOWING LYRICS
 * =============================================================
 */

@Composable
internal fun FollowLyrics(
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
    accent: Color,
    fullscreen: Boolean,
    modifier: Modifier = Modifier
) {
    if (
        lyrics == null ||
        lyrics.lines.isEmpty()
    ) {
        Box(
            modifier = modifier,
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "No local lyrics found.\n" +
                        "Tap + to choose an LRC file.",
                color = colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    if (fullscreen) {
                        18.sp
                    } else {
                        15.sp
                    },
                lineHeight =
                    if (fullscreen) {
                        28.sp
                    } else {
                        23.sp
                    },
                textAlign =
                    TextAlign.Center
            )
        }

        return
    }

    val active =
        currentLyricIndex(
            lyrics = lyrics,
            position = position
        )

    val listState =
        rememberLazyListState()

    /*
     * User scrolling temporarily owns positioning. After three
     * seconds without manual scrolling, synced lyrics return to
     * following actual playback position.
     */
    var browsing by remember {
        mutableStateOf(false)
    }

    var generation by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(
        listState.isScrollInProgress
    ) {
        if (
            listState.isScrollInProgress
        ) {
            browsing = true
            generation++
        } else if (browsing) {
            val token =
                ++generation

            delay(3_000L)

            if (
                generation == token &&
                !listState.isScrollInProgress
            ) {
                browsing = false
            }
        }
    }

    LaunchedEffect(
        active,
        browsing
    ) {
        if (
            lyrics.synced &&
            active >= 0 &&
            !browsing
        ) {
            centerLyric(
                state = listState,
                index = active,
                fullscreen = fullscreen
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding =
            PaddingValues(
                top =
                    if (fullscreen) {
                        245.dp
                    } else {
                        110.dp
                    },
                bottom =
                    if (fullscreen) {
                        285.dp
                    } else {
                        110.dp
                    }
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullscreen) {
                    21.dp
                } else {
                    13.dp
                }
            )
    ) {
        itemsIndexed(
            items = lyrics.lines,
            key = { index, line ->
                "$index:${line.timeMs}:${line.text}"
            }
        ) { index, line ->

            val selected =
                lyrics.synced &&
                    index == active

            val scale by
                animateFloatAsState(
                    targetValue =
                        when {
                            fullscreen &&
                                selected ->
                                1.08f

                            selected ->
                                1.055f

                            else ->
                                1f
                        },
                    animationSpec =
                        spring(
                            dampingRatio = .76f,
                            stiffness = 420f
                        ),
                    label =
                        "lyricScale$index"
                )

            Text(
                text = line.text,
                color =
                    if (selected) {
                        accent
                    } else {
                        colors.text.copy(
                            alpha =
                                if (fullscreen) {
                                    .46f
                                } else {
                                    .52f
                                }
                        )
                    },
                fontFamily =
                    if (selected) {
                        XmoFont.bold
                    } else {
                        XmoFont.normal
                    },
                fontSize =
                    when {
                        fullscreen &&
                            selected ->
                            28.sp

                        fullscreen ->
                            20.sp

                        selected ->
                            21.sp

                        else ->
                            16.sp
                    },
                lineHeight =
                    when {
                        fullscreen &&
                            selected ->
                            36.sp

                        fullscreen ->
                            29.sp

                        selected ->
                            28.sp

                        else ->
                            24.sp
                    },
                textAlign =
                    TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .animateItem()
            )
        }
    }
}

private suspend fun centerLyric(
    state: LazyListState,
    index: Int,
    fullscreen: Boolean
) {
    state.animateScrollToItem(
        index = index,
        scrollOffset =
            if (fullscreen) {
                -410
            } else {
                -110
            }
    )
}

/*
 * =============================================================
 * FULLSCREEN LYRICS
 * =============================================================
 */

@Composable
internal fun FullLyrics(
    lyrics: SongLyrics?,
    position: Long,
    duration: Long,
    title: String,
    artist: String,
    artwork: Uri?,
    dominant: Color,
    deep: Color,
    theme: XmoTheme,
    isPlaying: Boolean,
    canPrevious: Boolean,
    canNext: Boolean,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,
    close: () -> Unit
) {
    val foreground =
        if (
            dominant.luminance() >
            .58f
        ) {
            Color(0xFF111214)
        } else {
            Color.White
        }

    val lyricColors =
        HomeColors(
            bg =
                Color.Transparent,
            surface =
                Color.Transparent,
            text =
                foreground,
            sub =
                foreground.copy(
                    alpha = .60f
                ),
            button =
                foreground.copy(
                    alpha = .10f
                ),
            icon =
                foreground.copy(
                    alpha = .78f
                ),
            border =
                foreground.copy(
                    alpha = .16f
                )
        )

    Box(
        Modifier.fillMaxSize()
    ) {
        PlayerBackground(
            dominant = dominant,
            deep = deep,
            theme = theme
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            /*
             * =================================================
             * HEADER
             * =================================================
             */

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(
                        start = 15.dp,
                        end = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = artwork,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(
                                RoundedCornerShape(
                                    9.dp
                                )
                            )
                            .background(
                                foreground.copy(
                                    alpha = .08f
                                )
                            ),
                    contentScale =
                        ContentScale.Crop
                )

                Column(
                    Modifier
                        .weight(1f)
                        .padding(
                            start = 10.dp
                        )
                ) {
                    Text(
                        text =
                            title.ifBlank {
                                "Unknown song"
                            },
                        color = foreground,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text =
                            artist.ifBlank {
                                "Unknown artist"
                            },
                        color =
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                /*
                 * Requested order:
                 *
                 * Play -> Previous -> Next -> Close
                 */
                XmoCapsule(
                    background =
                        foreground.copy(
                            alpha = .10f
                        )
                ) {
                    CapsuleButton(
                        size = 39.dp,
                        onClick =
                            togglePlay
                    ) {
                        Icon(
                            imageVector =
                                if (isPlaying) {
                                    Lucide.Pause
                                } else {
                                    Lucide.Play
                                },
                            contentDescription =
                                if (isPlaying) {
                                    "Pause"
                                } else {
                                    "Play"
                                },
                            tint = foreground,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    CapsuleButton(
                        size = 39.dp,
                        enabled =
                            canPrevious,
                        onClick =
                            previous
                    ) {
                        Icon(
                            imageVector =
                                Lucide.SkipBack,
                            contentDescription =
                                "Previous",
                            tint =
                                foreground.copy(
                                    alpha =
                                        if (
                                            canPrevious
                                        ) {
                                            1f
                                        } else {
                                            .30f
                                        }
                                ),
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    CapsuleButton(
                        size = 39.dp,
                        enabled =
                            canNext,
                        onClick =
                            next
                    ) {
                        Icon(
                            imageVector =
                                Lucide.SkipForward,
                            contentDescription =
                                "Next",
                            tint =
                                foreground.copy(
                                    alpha =
                                        if (canNext) {
                                            1f
                                        } else {
                                            .30f
                                        }
                                ),
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    CapsuleButton(
                        size = 39.dp,
                        onClick = close
                    ) {
                        Icon(
                            imageVector =
                                Lucide.X,
                            contentDescription =
                                "Close",
                            tint = foreground,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }
                }
            }

            /*
             * =================================================
             * REAL DRAGGABLE PROGRESS
             * =================================================
             */

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 17.dp,
                        end = 17.dp,
                        top = 2.dp
                    )
            ) {
                RoundedSeekBar(
                    position = position,
                    duration = duration,
                    active = foreground,
                    inactive =
                        foreground.copy(
                            alpha = .22f
                        ),
                    seekTo = seekTo
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        text =
                            playerTime(position),
                        color =
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )

                    Text(
                        text =
                            playerTime(duration),
                        color =
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(
                Modifier.height(4.dp)
            )

            /*
             * No fixed screen-height calculation. Weight gives
             * lyrics exactly the remaining safe viewport.
             */
            FollowLyrics(
                lyrics = lyrics,
                position = position,
                colors = lyricColors,
                accent = foreground,
                fullscreen = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            )
        }
    }
}
