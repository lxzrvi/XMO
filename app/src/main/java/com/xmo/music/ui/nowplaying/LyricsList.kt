package com.xmo.music.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
    accent: androidx.compose.ui.graphics.Color,
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
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    if (fullscreen) {
                        18.sp
                    } else {
                        15.sp
                    },
                lineHeight =
                    if (fullscreen) {
                        27.sp
                    } else {
                        22.sp
                    },
                textAlign =
                    TextAlign.Center,
                modifier =
                    Modifier.padding(
                        horizontal = 28.dp
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

    /*
     * Critical:
     * LazyListState.isScrollInProgress also becomes true while
     * OUR automatic centering animation is running.
     *
     * Without this flag, auto-follow mistakes its own animation
     * for user browsing and disables itself.
     */
    var autoFollowing by
        remember {
            mutableStateOf(false)
        }

    var interactionToken by
        remember {
            mutableLongStateOf(0L)
        }

    /*
     * Only genuine user scrolling pauses following.
     */
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

    /*
     * Real measured viewport boundary space allows first/last
     * lyric to physically reach viewport center.
     */
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

            val lineColor by
                animateColorAsState(
                    targetValue =
                        if (selected) {
                            accent
                        } else {
                            colors.text.copy(
                                alpha =
                                    if (fullscreen) {
                                        .40f
                                    } else {
                                        .36f
                                    }
                            )
                        },
                    animationSpec =
                        tween(260),
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
                /*
                 * No graphicsLayer scale.
                 *
                 * Multiline active lyric's real measured height
                 * is therefore used for exact centering.
                 */
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

            /*
             * +1 = top boundary spacer.
             */
            centerLyricExactly(
                state = state,
                lazyIndex = active + 1
            )
        } finally {
            autoFollowing = false
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

    /*
     * Multiline/font measurement may settle one frame later.
     */
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
