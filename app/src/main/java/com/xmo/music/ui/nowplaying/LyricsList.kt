package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
                        horizontal = 22.dp
                    )
            )
        }

        return
    }

    val activeIndex =
        currentLyricIndex(
            lyrics = lyrics,
            position = position
        )

    val listState =
        rememberLazyListState()

    var manualBrowsing by remember {
        mutableStateOf(false)
    }

    var browseGeneration by remember {
        mutableLongStateOf(0L)
    }

    /*
     * Manual scroll temporarily owns the list. When the user
     * stops interacting, wait 3 seconds before following the
     * actual playback position again.
     */
    LaunchedEffect(
        listState.isScrollInProgress
    ) {
        if (
            listState.isScrollInProgress
        ) {
            manualBrowsing = true
            browseGeneration++
        } else if (manualBrowsing) {
            val token =
                ++browseGeneration

            delay(3_000L)

            if (
                browseGeneration == token &&
                !listState.isScrollInProgress
            ) {
                manualBrowsing = false
            }
        }
    }

    /*
     * No scrollToItem + delayed correction.
     *
     * That old sequence caused:
     * jump -> frame -> correction -> visible jitter.
     *
     * animateScrollToItem receives an offset calculated from the
     * actual viewport and currently measured item. Once the item
     * is visible we perform one smooth center correction.
     */
    LaunchedEffect(
        activeIndex,
        manualBrowsing,
        fullscreen
    ) {
        if (
            !lyrics.synced ||
            activeIndex < 0 ||
            manualBrowsing
        ) {
            return@LaunchedEffect
        }

        val visible =
            listState.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.index == activeIndex
                }

        if (visible == null) {
            /*
             * Bring it near center in one animated operation.
             * Content padding on both ends allows first/last
             * lyrics to reach center too.
             */
            val viewport =
                listState.layoutInfo

            val estimatedOffset =
                -(
                    (
                        viewport.viewportEndOffset -
                            viewport.viewportStartOffset
                        ) / 2
                    )

            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset =
                    estimatedOffset
            )
        }

        /*
         * Read the layout after the animation/composition pass.
         */
        withFrameNanos { }

        val layout =
            listState.layoutInfo

        val item =
            layout.visibleItemsInfo
                .firstOrNull {
                    it.index == activeIndex
                }
                ?: return@LaunchedEffect

        val viewportCenter =
            (
                layout.viewportStartOffset +
                    layout.viewportEndOffset
                ) / 2f

        val itemCenter =
            item.offset +
                item.size / 2f

        val correction =
            itemCenter -
                viewportCenter

        if (
            kotlin.math.abs(correction) >
            1f
        ) {
            listState.animateScrollBy(
                correction
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier =
            modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                /*
                 * Large symmetric padding lets the first and last
                 * line reach the actual visual center.
                 */
                top =
                    if (fullscreen) {
                        280.dp
                    } else {
                        150.dp
                    },
                bottom =
                    if (fullscreen) {
                        280.dp
                    } else {
                        150.dp
                    },
                start =
                    if (fullscreen) {
                        20.dp
                    } else {
                        12.dp
                    },
                end =
                    if (fullscreen) {
                        20.dp
                    } else {
                        12.dp
                    }
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
                    index == activeIndex

            val scale by
                animateFloatAsState(
                    targetValue =
                        when {
                            fullscreen &&
                                selected ->
                                1.065f

                            selected ->
                                1.04f

                            else ->
                                1f
                        },
                    animationSpec =
                        spring(
                            dampingRatio = .82f,
                            stiffness = 360f
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
                                    .47f
                                } else {
                                    .54f
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
                            27.sp

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
                            35.sp

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
                                    12.dp
                                } else {
                                    6.dp
                                }
                        )
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
            )
        }
    }
}
