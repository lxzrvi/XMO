package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.SongLyrics
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.delay

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
                    TextAlign.Center,
                modifier =
                    Modifier.padding(
                        horizontal = 24.dp
                    )
            )
        }

        return
    }

    val currentIndex =
        remember(
            lyrics,
            position
        ) {
            currentLyricIndex(
                lyrics,
                position
            )
        }

    val listState =
        rememberLazyListState()

    var userScrolling by remember {
        mutableStateOf(false)
    }

    var lastUserInteraction by remember {
        mutableLongStateOf(0L)
    }

    val returnDelay =
        4_000L

    /*
     * Playback changes active line.
     *
     * This intentionally follows the interaction pattern from
     * your reference: one animateScrollToItem operation using
     * half of the actual current viewport instead of
     * scrollToItem -> frame -> corrective animateScrollBy.
     */
    LaunchedEffect(
        currentIndex,
        userScrolling,
        fullscreen
    ) {
        if (
            currentIndex < 0 ||
            userScrolling
        ) {
            return@LaunchedEffect
        }

        /*
         * Let LazyColumn publish its viewport before calculating
         * the offset. This does not create the previous two-step
         * visual correction.
         */
        delay(80L)

        val viewportHeight =
            listState.layoutInfo
                .viewportEndOffset -
                listState.layoutInfo
                    .viewportStartOffset

        listState.animateScrollToItem(
            index =
                currentIndex,
            scrollOffset =
                -(viewportHeight / 2)
        )
    }

    /*
     * User can freely browse lyrics.
     * Four seconds after the last touch we return to current.
     */
    LaunchedEffect(
        lastUserInteraction
    ) {
        if (
            lastUserInteraction ==
            0L
        ) {
            return@LaunchedEffect
        }

        delay(returnDelay)

        userScrolling = false

        if (currentIndex >= 0) {
            val viewportHeight =
                listState.layoutInfo
                    .viewportEndOffset -
                    listState.layoutInfo
                        .viewportStartOffset

            listState.animateScrollToItem(
                index =
                    currentIndex,
                scrollOffset =
                    -(viewportHeight / 2)
            )
        }
    }

    Box(
        modifier
            .pointerInput(
                currentIndex
            ) {
                detectDragGestures(
                    onDragStart = {
                        userScrolling = true

                        lastUserInteraction =
                            System.currentTimeMillis()
                    },

                    onDragEnd = {
                        lastUserInteraction =
                            System.currentTimeMillis()
                    },

                    onDragCancel = {
                        lastUserInteraction =
                            System.currentTimeMillis()
                    },

                    onDrag = {
                            _,
                            _ ->

                        /*
                         * LazyColumn itself performs the actual
                         * scrolling. We only track interaction.
                         *
                         * Do not consume here, otherwise the list
                         * cannot receive its normal scroll.
                         */
                        lastUserInteraction =
                            System.currentTimeMillis()
                    }
                )
            }
    ) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top =
                        if (fullscreen) {
                            280.dp
                        } else {
                            130.dp
                        },
                    bottom =
                        if (fullscreen) {
                            280.dp
                        } else {
                            130.dp
                        }
                ),
            verticalArrangement =
                Arrangement.spacedBy(
                    if (fullscreen) {
                        7.dp
                    } else {
                        5.dp
                    }
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            itemsIndexed(
                items = lyrics.lines,
                key = {
                        index,
                        line ->

                    "$index:${line.timeMs}:${line.text}"
                }
            ) {
                    index,
                    line ->

                val current =
                    index ==
                        currentIndex

                val scale by
                    animateFloatAsState(
                        targetValue =
                            if (current) {
                                1.15f
                            } else {
                                1f
                            },
                        animationSpec =
                            tween(
                                durationMillis =
                                    450
                            ),
                        label =
                            "lyricScale$index"
                    )

                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal =
                                if (fullscreen) {
                                    40.dp
                                } else {
                                    24.dp
                                },
                            vertical = 9.dp
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = line.text,
                        fontFamily =
                            if (current) {
                                XmoFont.bold
                            } else {
                                XmoFont.medium
                            },
                        fontSize =
                            if (current) {
                                if (fullscreen) {
                                    24.sp
                                } else {
                                    22.sp
                                }
                            } else {
                                if (fullscreen) {
                                    18.sp
                                } else {
                                    17.sp
                                }
                            },
                        lineHeight =
                            if (current) {
                                31.sp
                            } else {
                                25.sp
                            },
                        color =
                            if (current) {
                                accent
                            } else {
                                colors.text.copy(
                                    alpha = .38f
                                )
                            },
                        textAlign =
                            TextAlign.Center,
                        softWrap = true,
                        overflow =
                            TextOverflow.Visible,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                }
                    )
                }
            }
        }
    }
}
