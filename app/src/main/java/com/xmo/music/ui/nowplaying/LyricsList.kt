package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
                        horizontal = 24.dp
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

    var userBrowsing by remember {
        mutableStateOf(false)
    }

    var interactionToken by remember {
        mutableLongStateOf(0L)
    }

    /*
     * LazyList's own isScrollInProgress is the source of truth
     * for manual browsing. It doesn't steal the list's pointer
     * input.
     */
    LaunchedEffect(
        state.isScrollInProgress
    ) {
        if (
            state.isScrollInProgress
        ) {
            userBrowsing = true
            interactionToken++
        } else if (userBrowsing) {
            val token =
                ++interactionToken

            delay(4_000L)

            if (
                interactionToken == token &&
                !state.isScrollInProgress
            ) {
                userBrowsing = false
            }
        }
    }

    LaunchedEffect(
        active,
        userBrowsing,
        fullscreen
    ) {
        if (
            active < 0 ||
            !lyrics.synced ||
            userBrowsing
        ) {
            return@LaunchedEffect
        }

        centerLyricExactly(
            state = state,
            index = active
        )
    }

    LazyColumn(
        state = state,
        modifier =
            modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                top =
                    if (fullscreen) {
                        300.dp
                    } else {
                        155.dp
                    },

                bottom =
                    if (fullscreen) {
                        300.dp
                    } else {
                        155.dp
                    }
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullscreen) {
                    8.dp
                } else {
                    6.dp
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

            val selected =
                lyrics.synced &&
                    index == active

            val scale by
                animateFloatAsState(
                    targetValue =
                        if (selected) {
                            1.12f
                        } else {
                            1f
                        },
                    animationSpec =
                        tween(420),
                    label =
                        "lyricScale$index"
                )

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal =
                            if (fullscreen) {
                                36.dp
                            } else {
                                20.dp
                            },

                        vertical =
                            if (selected) {
                                11.dp
                            } else {
                                8.dp
                            }
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text = line.text,
                    color =
                        if (selected) {
                            accent
                        } else {
                            colors.text.copy(
                                alpha = .38f
                            )
                        },
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
                                22.sp

                            else ->
                                17.sp
                        },
                    lineHeight =
                        when {
                            fullscreen &&
                                selected ->
                                33.sp

                            fullscreen ->
                                26.sp

                            selected ->
                                30.sp

                            else ->
                                25.sp
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

private suspend fun centerLyricExactly(
    state: LazyListState,
    index: Int
) {
    /*
     * If already visible, there is no preliminary jump.
     */
    var item =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index == index
            }

    if (item == null) {
        val viewport =
            state.layoutInfo

        val halfViewport =
            (
                viewport.viewportEndOffset -
                    viewport.viewportStartOffset
                ) / 2

        /*
         * Bring near the target in one animated operation.
         */
        state.animateScrollToItem(
            index = index,
            scrollOffset =
                -halfViewport
        )

        withFrameNanos { }

        item =
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
        item.offset +
            item.size / 2f

    val delta =
        itemCenter -
            viewportCenter

    if (
        abs(delta) >
        1f
    ) {
        state.animateScrollBy(
            delta
        )
    }
}
