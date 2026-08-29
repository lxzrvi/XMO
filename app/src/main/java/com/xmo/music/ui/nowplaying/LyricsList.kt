package com.xmo.music.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            contentAlignment = Alignment.Center
        ) {
            Text(
                text =
                    "No local lyrics found.\n" +
                        "Tap + to choose an LRC file.",
                color = colors.sub,
                fontFamily = XmoFont.medium,
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
                textAlign = TextAlign.Center,
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
            lyrics = lyrics,
            position = position
        )

    val listState =
        rememberLazyListState()

    var userBrowsing by
        remember {
            mutableStateOf(false)
        }

    var interactionToken by
        remember {
            mutableLongStateOf(0L)
        }

    LaunchedEffect(
        listState.isScrollInProgress
    ) {
        if (
            listState.isScrollInProgress
        ) {
            userBrowsing = true
            interactionToken++
        } else if (userBrowsing) {
            val token =
                ++interactionToken

            delay(4_000L)

            if (
                token == interactionToken &&
                !listState.isScrollInProgress
            ) {
                userBrowsing = false
            }
        }
    }

    BoxWithConstraints(
        modifier =
            modifier.fillMaxSize()
    ) {
        /*
         * Half the real viewport on both ends lets even the first
         * and last lyric reach the physical viewport centre.
         */
        val centrePadding =
            maxHeight / 2

        LazyColumn(
            state = listState,
            modifier =
                Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = centrePadding,
                    bottom = centrePadding
                ),
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
            itemsIndexed(
                items = lyrics.lines,
                key = { index, line ->
                    "$index:${line.timeMs}:${line.text}"
                }
            ) { index, line ->

                val selected =
                    lyrics.synced &&
                        index == active

                val targetColor =
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
                    }

                val lineColor by
                    animateColorAsState(
                        targetValue =
                            targetColor,
                        animationSpec =
                            tween(
                                durationMillis = 280
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
                    /*
                     * No graphicsLayer scaling. Font metrics own
                     * their real layout size, so multiline lyrics
                     * cannot get visually clipped by an unscaled
                     * LazyColumn item.
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
        }

        LaunchedEffect(
            active,
            userBrowsing,
            fullscreen,
            maxHeight,
            lyrics
        ) {
            if (
                active < 0 ||
                !lyrics.synced ||
                userBrowsing
            ) {
                return@LaunchedEffect
            }

            withFrameNanos { }

            centerLyricExactly(
                state = listState,
                index = active
            )
        }
    }
}

private suspend fun centerLyricExactly(
    state: LazyListState,
    index: Int
) {
    if (index < 0) {
        return
    }

    var target =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index == index
            }

    if (target == null) {
        state.scrollToItem(
            index = index
        )

        withFrameNanos { }

        target =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.index == index
                }
                ?: return
    }

    val layout =
        state.layoutInfo

    val viewportCenter =
        (
            layout.viewportStartOffset +
                layout.viewportEndOffset
            ) / 2f

    val itemCenter =
        target.offset +
            target.size / 2f

    val delta =
        itemCenter -
            viewportCenter

    if (abs(delta) > .5f) {
        state.animateScrollBy(
            value = delta,
            animationSpec =
                tween(
                    durationMillis = 430
                )
        )
    }

    /*
     * Correct any remaining rounding or multiline remeasurement.
     */
    withFrameNanos { }

    val corrected =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index == index
            }
            ?: return

    val correctedLayout =
        state.layoutInfo

    val correctedViewportCenter =
        (
            correctedLayout.viewportStartOffset +
                correctedLayout.viewportEndOffset
            ) / 2f

    val correctedItemCenter =
        corrected.offset +
            corrected.size / 2f

    val correction =
        correctedItemCenter -
            correctedViewportCenter

    if (abs(correction) > 1f) {
        state.scrollBy(
            correction
        )
    }
}
