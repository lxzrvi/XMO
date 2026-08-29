package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
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

@Composable
internal fun ArtworkLyrics(
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
    accent: Color,
    theme: XmoTheme,
    pickLyrics: () -> Unit,
    fullscreenLyrics: () -> Unit,
    showArtwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .82f
                )

            XmoTheme.Dark ->
                Color(0xFF171719)
                    .copy(
                        alpha = .82f
                    )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .88f
                )
        }

    Box(
        modifier
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(surface)
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
                        horizontal = 14.dp
                    )
        )

        XmoCapsule(
            background =
                colors.button,
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(10.dp)
        ) {
            CapsuleButton(
                size = 38.dp,
                onClick =
                    pickLyrics
            ) {
                Icon(
                    imageVector =
                        Lucide.Plus,
                    contentDescription =
                        "Choose local lyrics",
                    tint =
                        colors.text,
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
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 38.dp,
                onClick =
                    showArtwork
            ) {
                Icon(
                    imageVector =
                        Lucide.X,
                    contentDescription =
                        "Show artwork",
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }
        }
    }
}

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
            modifier,
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "No local lyrics found.\n" +
                        "Tap + to choose an LRC file.",
                color =
                    colors.sub,
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
                    TextAlign.Center,
                modifier =
                    Modifier.padding(
                        horizontal = 18.dp
                    )
            )
        }

        return
    }

    val active =
        currentLyricIndex(
            lyrics,
            position
        )

    val state =
        rememberLazyListState()

    var browsing by remember {
        mutableStateOf(false)
    }

    var generation by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(
        state.isScrollInProgress
    ) {
        if (
            state.isScrollInProgress
        ) {
            browsing = true
            generation++
        } else if (browsing) {
            val token =
                ++generation

            delay(3_000L)

            if (
                generation == token &&
                !state.isScrollInProgress
            ) {
                browsing = false
            }
        }
    }

    /*
     * First make sure active item exists in the visible layout,
     * then calculate its REAL pixel center against the current
     * viewport. No density/screen-specific magic offset.
     */
    LaunchedEffect(
        active,
        browsing,
        fullscreen
    ) {
        if (
            !lyrics.synced ||
            active < 0 ||
            browsing
        ) {
            return@LaunchedEffect
        }

        centerActualLyric(
            state = state,
            index = active
        )
    }

    LazyColumn(
        state = state,
        modifier = modifier,
        contentPadding =
            PaddingValues(
                top =
                    if (fullscreen) {
                        220.dp
                    } else {
                        145.dp
                    },
                bottom =
                    if (fullscreen) {
                        220.dp
                    } else {
                        145.dp
                    },
                start = 4.dp,
                end = 4.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullscreen) {
                    20.dp
                } else {
                    13.dp
                }
            )
    ) {
        itemsIndexed(
            items =
                lyrics.lines,
            key = {
                    index,
                    line ->

                "$index:${line.timeMs}:${line.text}"
            }
        ) {
                index,
                line ->

            val selected =
                lyrics.synced &&
                    index == active

            val scale by
                animateFloatAsState(
                    targetValue =
                        when {
                            fullscreen &&
                                selected ->
                                1.07f

                            selected ->
                                1.045f

                            else ->
                                1f
                        },
                    animationSpec =
                        spring(
                            dampingRatio = .78f,
                            stiffness = 400f
                        ),
                    label =
                        "lyric$index"
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
                                    .48f
                                } else {
                                    .55f
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
                            20.sp

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
                            27.sp

                        else ->
                            24.sp
                    },
                textAlign =
                    TextAlign.Center,
                softWrap = true,
                overflow =
                    TextOverflow.Visible,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                if (fullscreen) {
                                    18.dp
                                } else {
                                    8.dp
                                }
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .animateItem()
            )
        }
    }
}

private suspend fun centerActualLyric(
    state: LazyListState,
    index: Int
) {
    /*
     * Bring item into layout first. This isn't the final
     * position.
     */
    state.scrollToItem(index)

    /*
     * Wait until LazyColumn reports the new visible layout.
     */
    kotlinx.coroutines.delay(16L)

    val layout =
        state.layoutInfo

    val item =
        layout.visibleItemsInfo
            .firstOrNull {
                it.index == index
            }
            ?: return

    val viewportCenter =
        (
            layout.viewportStartOffset +
                layout.viewportEndOffset
            ) / 2f

    val itemCenter =
        item.offset +
            item.size / 2f

    val delta =
        itemCenter -
            viewportCenter

    state.animateScrollBy(delta)
}

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
            bg = Color.Transparent,
            surface = Color.Transparent,
            text = foreground,
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
                        text = title,
                        color = foreground,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text = artist,
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

                XmoCapsule(
                    background =
                        foreground.copy(
                            alpha = .10f
                        )
                ) {
                    CapsuleButton(
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
                                "Play pause",
                            tint = foreground,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    CapsuleButton(
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
                            tint = foreground,
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    CapsuleButton(
                        enabled =
                            canNext,
                        onClick = next
                    ) {
                        Icon(
                            imageVector =
                                Lucide.SkipForward,
                            contentDescription =
                                "Next",
                            tint = foreground,
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    CapsuleButton(
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

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 17.dp
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
                        .padding(
                            horizontal = 8.dp
                        )
            )
        }
    }
}
