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
    /*
     * No lyrics state.
     */
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

    /*
     * Current lyric index according to the
     * current playback position.
     */
    val active =
        currentLyricIndex(
            lyrics = lyrics,
            position = position
        )

    /*
     * One persistent LazyColumn state.
     */
    val listState =
        rememberLazyListState()

    /*
     * When the user manually scrolls lyrics,
     * automatic centering is temporarily disabled.
     */
    var userBrowsing by
        remember {
            mutableStateOf(false)
        }

    /*
     * Used to restart the 4 second timeout whenever
     * the user interacts with the lyric list.
     */
    var interactionToken by
        remember {
            mutableLongStateOf(0L)
        }

    /*
     * Detect manual scrolling.
     */
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
         * Half of the real container height.
         *
         * This gives the first and last lyrics enough
         * empty space to also reach the physical center.
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

                /*
                 * Only the currently playing lyric is selected.
                 */
                val selected =
                    lyrics.synced &&
                        index == active

                /*
                 * Keep your existing accent color.
                 *
                 * Nothing is hard-coded to red.
                 */
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

                /*
                 * Smooth color transition.
                 */
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

                /*
                 * Full-width lyric item.
                 *
                 * The Text itself is centered inside this box.
                 */
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
        }

        /*
         * Automatically center the currently playing lyric.
         *
         * This runs whenever:
         *
         * - active lyric changes
         * - user browsing ends
         * - fullscreen changes
         * - lyrics container size changes
         */
        LaunchedEffect(
            active,
            userBrowsing,
            fullscreen,
            maxHeight,
            lyrics
        ) {

            /*
             * Nothing to center.
             */
            if (
                active < 0 ||
                !lyrics.synced ||
                userBrowsing
            ) {
                return@LaunchedEffect
            }

            /*
             * Give LazyColumn one frame to measure
             * the current lyric.
             */
            withFrameNanos { }

            centerLyricExactly(
                state = listState,
                index = active
            )
        }
    }
}


/*
 * ============================================================
 * EXACT LYRIC CENTERING
 * ============================================================
 *
 * Centers the complete active lyric item against the
 * physical center of the LazyColumn.
 *
 * Works with:
 *
 * - Normal player
 * - Fullscreen
 * - First lyric
 * - Middle lyric
 * - Last lyric
 * - Multiline lyrics
 * - Different selected/unselected font sizes
 * - Different vertical padding
 */
private suspend fun centerLyricExactly(
    state: LazyListState,
    index: Int
) {
    if (index < 0) {
        return
    }

    /*
     * Try to get the target lyric from currently
     * measured items.
     */
    var target =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index == index
            }

    /*
     * If the lyric isn't currently measured,
     * move the list near it first.
     *
     * This is NOT the final centering.
     */
    if (target == null) {

        state.scrollToItem(
            index = index
        )

        /*
         * Wait until LazyColumn lays out the item.
         */
        withFrameNanos { }

        target =
            state.layoutInfo
                .visibleItemsInfo
                .firstOrNull {
                    it.index == index
                }
                ?: return
    }

    /*
     * Get the latest layout information.
     */
    val layout =
        state.layoutInfo

    /*
     * IMPORTANT FIX
     *
     * Do NOT use:
     *
     * viewportStartOffset + viewportEndOffset
     *
     * because contentPadding affects those coordinates.
     *
     * viewportSize.height represents the actual
     * LazyColumn viewport height.
     */
    val viewportCenter =
        layout.viewportSize.height / 2f

    /*
     * Calculate the center of the COMPLETE item.
     *
     * target.size includes:
     *
     * - text height
     * - selected padding
     * - unselected padding
     */
    val itemCenter =
        target.offset +
            target.size / 2f

    /*
     * Difference between lyric center and physical
     * viewport center.
     */
    val delta =
        itemCenter -
            viewportCenter

    /*
     * Only move if there is an actual difference.
     */
    if (abs(delta) > 0.5f) {

        state.animateScrollBy(
            value = delta,
            animationSpec =
                tween(
                    durationMillis = 430
                )
        )
    }

    /*
     * Wait for the layout after scrolling.
     */
    withFrameNanos { }

    /*
     * Re-read the target because the item can have
     * changed size after being selected.
     *
     * This is particularly important for:
     *
     * normal lyric -> active lyric
     *
     * because font size changes from 16sp to 21sp,
     * or 18sp to 25sp in fullscreen.
     */
    val corrected =
        state.layoutInfo
            .visibleItemsInfo
            .firstOrNull {
                it.index == index
            }
            ?: return

    /*
     * Recalculate actual item center.
     */
    val correctedItemCenter =
        corrected.offset +
            corrected.size / 2f

    /*
     * Recalculate actual physical viewport center.
     */
    val correctedViewportCenter =
        state.layoutInfo
            .viewportSize
            .height / 2f

    /*
     * Final difference.
     */
    val correction =
        correctedItemCenter -
            correctedViewportCenter

    /*
     * Tiny final correction.
     *
     * Handles:
     *
     * - pixel rounding
     * - multiline remeasurement
     * - font-size change
     * - padding change
     */
    if (abs(correction) > 1f) {

        state.scrollBy(
            value = correction
        )
    }
}
