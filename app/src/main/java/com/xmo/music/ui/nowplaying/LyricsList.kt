package com.xmo.music.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.SongLyrics
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
internal fun FollowLyrics(
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
    accent: Color,
    fullscreen: Boolean,
    modifier: Modifier = Modifier,
    pickLyrics: (() -> Unit)? = null
) {
    if (
        lyrics == null ||
        lyrics.lines.isEmpty()
    ) {
        NoLyricsState(
            colors = colors,
            accent = accent,
            fullscreen = fullscreen,
            pickLyrics = pickLyrics,
            modifier = modifier
        )

        return
    }

    val active =
        currentLyricIndex(
            lyrics,
            position
        )

    val state =
        rememberLazyListState()

    val density =
        LocalDensity.current

    var viewportHeightPx by
        remember {
            mutableIntStateOf(0)
        }

    var userBrowsing by
        remember {
            mutableStateOf(false)
        }

    var autoFollowing by
        remember {
            mutableStateOf(false)
        }

    var interactionToken by
        remember {
            mutableLongStateOf(0L)
        }

    LaunchedEffect(
        state.isScrollInProgress,
        autoFollowing
    ) {
        if (
            state.isScrollInProgress &&
            !autoFollowing
        ) {
            userBrowsing = true
            interactionToken++
        } else if (
            !state.isScrollInProgress &&
            userBrowsing &&
            !autoFollowing
        ) {
            val token =
                ++interactionToken

            delay(4_000L)

            if (
                token == interactionToken &&
                !state.isScrollInProgress
            ) {
                userBrowsing = false
            }
        }
    }

    val boundarySpace =
        with(density) {
            (
                viewportHeightPx /
                    2f
                )
                .toDp()
        }

    LazyColumn(
        state = state,
        modifier =
            modifier
                .fillMaxSize()
                .onSizeChanged {
                    viewportHeightPx =
                        it.height
                }
                .graphicsLayer {
                    compositingStrategy =
                        CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()

                    /*
                     * Fullscreen gets a much broader edge fade.
                     *
                     * This gives the top/bottom the requested
                     * soft, hazy disappearance without blurring
                     * the centered active lyric itself.
                     *
                     * Small lyrics retain more usable visible
                     * area while still getting softer edges.
                     */
                    val stops =
                        if (fullscreen) {
                            arrayOf(
                                0.00f to
                                    Color.Transparent,
                                0.055f to
                                    Color.White.copy(
                                        alpha = .035f
                                    ),
                                0.12f to
                                    Color.White.copy(
                                        alpha = .10f
                                    ),
                                0.20f to
                                    Color.White.copy(
                                        alpha = .25f
                                    ),
                                0.30f to
                                    Color.White.copy(
                                        alpha = .54f
                                    ),
                                0.40f to
                                    Color.White.copy(
                                        alpha = .86f
                                    ),
                                0.46f to
                                    Color.White,
                                0.54f to
                                    Color.White,
                                0.60f to
                                    Color.White.copy(
                                        alpha = .86f
                                    ),
                                0.70f to
                                    Color.White.copy(
                                        alpha = .54f
                                    ),
                                0.80f to
                                    Color.White.copy(
                                        alpha = .25f
                                    ),
                                0.88f to
                                    Color.White.copy(
                                        alpha = .10f
                                    ),
                                0.945f to
                                    Color.White.copy(
                                        alpha = .035f
                                    ),
                                1.00f to
                                    Color.Transparent
                            )
                        } else {
                            arrayOf(
                                0.00f to
                                    Color.White.copy(
                                        alpha = .025f
                                    ),
                                0.08f to
                                    Color.White.copy(
                                        alpha = .10f
                                    ),
                                0.18f to
                                    Color.White.copy(
                                        alpha = .30f
                                    ),
                                0.31f to
                                    Color.White.copy(
                                        alpha = .67f
                                    ),
                                0.43f to
                                    Color.White,
                                0.57f to
                                    Color.White,
                                0.69f to
                                    Color.White.copy(
                                        alpha = .67f
                                    ),
                                0.82f to
                                    Color.White.copy(
                                        alpha = .30f
                                    ),
                                0.92f to
                                    Color.White.copy(
                                        alpha = .10f
                                    ),
                                1.00f to
                                    Color.White.copy(
                                        alpha = .025f
                                    )
                            )
                        }

                    drawRect(
                        brush =
                            Brush.verticalGradient(
                                colorStops = stops
                            ),
                        blendMode =
                            BlendMode.DstIn
                    )
                },
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullscreen) {
                    8.dp
                } else {
                    5.dp
                }
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        item(
            key = "lyricsTopBoundary"
        ) {
            Spacer(
                Modifier.height(
                    boundarySpace
                )
            )
        }

        itemsIndexed(
            items = lyrics.lines,
            key = { index, line ->
                "$index:${line.timeMs}:${line.text}"
            }
        ) { index, line ->

            val selected =
                lyrics.synced &&
                    index == active

            val inactiveAlpha =
                if (!lyrics.synced) {
                    .82f
                } else if (fullscreen) {
                    .48f
                } else {
                    .44f
                }

            val lineColor by
                animateColorAsState(
                    targetValue =
                        if (selected) {
                            accent
                        } else {
                            colors.text.copy(
                                alpha =
                                    inactiveAlpha
                            )
                        },
                    animationSpec =
                        tween(
                            durationMillis =
                                260
                        ),
                    label =
                        "lyricColor$index"
                )

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                if (fullscreen) {
                                    32.dp
                                } else {
                                    22.dp
                                },
                            vertical =
                                if (selected) {
                                    10.dp
                                } else {
                                    7.dp
                                }
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = line.text,
                    color = lineColor,
                    fontFamily =
                        if (selected) {
                            XmoFont.bold
                        } else {
                            XmoFont.medium
                        },
                    fontSize =
                        when {
                            fullscreen &&
                                selected ->
                                25.sp

                            fullscreen ->
                                18.sp

                            selected ->
                                21.sp

                            else ->
                                16.sp
                        },
                    lineHeight =
                        when {
                            fullscreen &&
                                selected ->
                                33.sp

                            fullscreen ->
                                26.sp

                            selected ->
                                29.sp

                            else ->
                                23.sp
                        },
                    textAlign =
                        TextAlign.Center,
                    softWrap = true,
                    overflow =
                        TextOverflow.Visible,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }

        item(
            key = "lyricsBottomBoundary"
        ) {
            Spacer(
                Modifier.height(
                    boundarySpace
                )
            )
        }
    }

    LaunchedEffect(
        active,
        userBrowsing,
        viewportHeightPx,
        lyrics
    ) {
        if (
            !lyrics.synced ||
            active < 0 ||
            viewportHeightPx <= 0 ||
            userBrowsing
        ) {
            return@LaunchedEffect
        }

        autoFollowing = true

        try {
            withFrameNanos { }

            centerLyricExactly(
                state = state,
                lazyIndex = active + 1
            )
        } finally {
            autoFollowing = false
        }
    }
}

@Composable
private fun NoLyricsState(
    colors: HomeColors,
    accent: Color,
    fullscreen: Boolean,
    pickLyrics: (() -> Unit)?,
    modifier: Modifier
) {
    Box(
        modifier =
            modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            if (pickLyrics != null) {
                PremiumCircle(
                    size =
                        if (fullscreen) {
                            58.dp
                        } else {
                            52.dp
                        },
                    background =
                        accent.copy(
                            alpha = .16f
                        ),
                    onClick =
                        pickLyrics
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Add,
                        contentDescription =
                            "Add local lyrics",
                        tint = accent,
                        modifier =
                            Modifier.size(
                                if (fullscreen) {
                                    31.dp
                                } else {
                                    28.dp
                                }
                            )
                    )
                }

                Spacer(
                    Modifier.height(
                        13.dp
                    )
                )
            }

            Text(
                text =
                    "No local lyrics",
                color =
                    colors.text.copy(
                        alpha = .88f
                    ),
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    if (fullscreen) {
                        19.sp
                    } else {
                        16.sp
                    },
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                Modifier.height(
                    4.dp
                )
            )

            Text(
                text =
                    if (pickLyrics != null) {
                        "Add an LRC file"
                    } else {
                        "No lyrics attached"
                    },
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    if (fullscreen) {
                        12.sp
                    } else {
                        10.sp
                    },
                textAlign =
                    TextAlign.Center
            )
        }
    }
}

private suspend fun centerLyricExactly(
    state: LazyListState,
    lazyIndex: Int
) {
    var target =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index == lazyIndex
            }

    if (target == null) {
        state.scrollToItem(
            index = lazyIndex
        )

        withFrameNanos { }

        target =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.index == lazyIndex
                }
                ?: return
    }

    fun measuredCorrection(): Float? {
        val layout =
            state.layoutInfo

        val item =
            layout
                .visibleItemsInfo
                .firstOrNull {
                    it.index == lazyIndex
                }
                ?: return null

        val viewportCenter =
            (
                layout.viewportStartOffset +
                    layout.viewportEndOffset
                ) / 2f

        val itemCenter =
            item.offset +
                item.size / 2f

        return itemCenter -
            viewportCenter
    }

    val first =
        measuredCorrection()
            ?: return

    if (
        abs(first) > .5f
    ) {
        state.animateScrollBy(
            value = first,
            animationSpec =
                tween(
                    durationMillis = 390
                )
        )
    }

    withFrameNanos { }

    val final =
        measuredCorrection()
            ?: return

    if (
        abs(final) > .75f
    ) {
        state.scrollBy(
            value = final
        )
    }
}
