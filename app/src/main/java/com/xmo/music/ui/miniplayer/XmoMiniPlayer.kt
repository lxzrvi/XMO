package com.xmo.music.ui.miniplayer

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.homeColors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

@Composable
fun XmoMiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,
    queue: List<Song>,
    riseKey: Int,
    likedSongIds: Set<Long>,
    openPlayer: () -> Unit,
    closePlayer: () -> Unit,
    togglePlay: () -> Unit,
    toggleLike: (Long) -> Unit,
    playQueueIndex: (Int) -> Unit
) {
    if (
        state.currentSongId == null ||
        queue.isEmpty()
    ) {
        return
    }

    val realIndex =
        state.currentIndex
            .takeIf {
                it in queue.indices
            }
            ?: queue.indexOfFirst {
                it.id ==
                    state.currentSongId
            }
                .coerceAtLeast(0)

    val colors =
        homeColors(theme)

    val accent =
        LocalXmoAccent.current

    val scope =
        rememberCoroutineScope()

    val density =
        LocalDensity.current

    val keyboardController =
        LocalSoftwareKeyboardController.current

    val x =
        remember {
            Animatable(0f)
        }

    val y =
        remember {
            Animatable(0f)
        }

    /*
     * Preview index is independent from Media3 until debounce
     * commits the final destination.
     */
    var previewIndex by
        remember(
            state.currentSongId,
            queue
        ) {
            mutableIntStateOf(
                realIndex
            )
        }

    var transitionDirection by
        remember {
            mutableIntStateOf(0)
        }

    var previewRevision by
        remember {
            mutableIntStateOf(0)
        }

    /*
     * If Media3 confirms a committed preview, adopt its real
     * index without creating another content transition.
     */
    LaunchedEffect(
        state.currentSongId,
        realIndex
    ) {
        if (
            previewIndex ==
            realIndex
        ) {
            return@LaunchedEffect
        }

        /*
         * Do not overwrite a pending preview. Its revision effect
         * owns the upcoming commit.
         */
        if (previewRevision == 0) {
            previewIndex =
                realIndex

            transitionDirection = 0
        }
    }

    /*
     * One playback command after the user stops rapid swiping.
     */
    LaunchedEffect(
        previewRevision
    ) {
        if (previewRevision <= 0) {
            return@LaunchedEffect
        }

        val revision =
            previewRevision

        delay(
            XmoMiniPlayerAnimation
                .previewCommitDelayMs
        )

        if (
            revision !=
            previewRevision
        ) {
            return@LaunchedEffect
        }

        val target =
            previewIndex

        if (
            target in queue.indices &&
            target != realIndex
        ) {
            playQueueIndex(
                target
            )
        }

        previewRevision = 0
    }

    /*
     * =========================================================
     * POSITION
     * =========================================================
     */

    val navigationBottomPx =
        WindowInsets.navigationBars
            .getBottom(density)

    val imeBottomPx =
        WindowInsets.ime
            .getBottom(density)

    val normalBottomPx =
        navigationBottomPx +
            with(density) {
                144.dp.toPx()
            }

    val keyboardBottomPx =
        imeBottomPx +
            with(density) {
                6.dp.toPx()
            }

    val resolvedBottomPx =
        max(
            normalBottomPx,
            keyboardBottomPx
        )

    val bottomPadding =
        with(density) {
            resolvedBottomPx.toDp()
        }

    /*
     * =========================================================
     * RETURN FROM NOW PLAYING
     * =========================================================
     */

    val riseDistance =
        with(density) {
            150.dp.toPx()
        }

    val entranceY =
        remember(riseKey) {
            Animatable(
                if (riseKey > 0) {
                    riseDistance
                } else {
                    0f
                }
            )
        }

    LaunchedEffect(riseKey) {
        if (
            entranceY.value != 0f
        ) {
            entranceY.animateTo(
                targetValue = 0f,
                animationSpec =
                    XmoMiniPlayerAnimation
                        .riseSpec
            )
        }
    }

    var opening by
        remember {
            mutableStateOf(false)
        }

    var closing by
        remember {
            mutableStateOf(false)
        }

    var axis by
        remember {
            mutableStateOf(
                XmoMiniAxis.None
            )
        }

    var rawX by
        remember {
            mutableFloatStateOf(0f)
        }

    var rawY by
        remember {
            mutableFloatStateOf(0f)
        }

    var moved by
        remember {
            mutableStateOf(false)
        }

    val hiddenThreshold =
        resolvedBottomPx +
            with(density) {
                60.dp.toPx()
            }

    val hiddenTarget =
        hiddenThreshold +
            with(density) {
                110.dp.toPx()
            }

    suspend fun awaitHidden() {
        while (
            y.value <
            hiddenThreshold
        ) {
            withFrameNanos { }
        }
    }

    /*
     * Commit currently previewed song immediately when opening.
     *
     * No 320ms wait should occur after the user explicitly asks
     * for Now Playing.
     */
    fun commitPreviewNow() {
        val target =
            previewIndex

        if (
            target in queue.indices &&
            target != realIndex
        ) {
            playQueueIndex(
                target
            )
        }

        previewRevision = 0
    }

    suspend fun openOrdered() {
        if (
            opening ||
            closing
        ) {
            return
        }

        opening = true

        commitPreviewNow()

        keyboardController?.hide()

        x.snapTo(0f)

        coroutineScope {
            launch {
                y.animateTo(
                    targetValue =
                        hiddenTarget,
                    animationSpec =
                        XmoMiniPlayerAnimation
                            .openExitSpec
                )
            }

            awaitHidden()

            openPlayer()
        }
    }

    suspend fun closeOrdered() {
        if (
            opening ||
            closing
        ) {
            return
        }

        closing = true

        previewRevision = 0

        x.snapTo(0f)

        coroutineScope {
            launch {
                y.animateTo(
                    targetValue =
                        hiddenTarget,
                    animationSpec =
                        XmoMiniPlayerAnimation
                            .closeExitSpec
                )
            }

            awaitHidden()

            closePlayer()
        }
    }

    val visualSong =
        queue.getOrNull(
            previewIndex
        )
            ?: return

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom =
                        bottomPadding
                ),
        contentAlignment =
            Alignment.BottomCenter
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .align(
                        Alignment.BottomCenter
                    )
                    .pointerInput(
                        state.currentSongId,
                        opening,
                        closing,
                        queue
                    ) {
                        if (
                            opening ||
                            closing
                        ) {
                            return@pointerInput
                        }

                        detectDragGestures(
                            onDragStart = {
                                axis =
                                    XmoMiniAxis.None

                                rawX = 0f
                                rawY = 0f
                                moved = false
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                rawX += amount.x
                                rawY += amount.y

                                if (
                                    axis ==
                                    XmoMiniAxis.None &&
                                    (
                                        abs(rawX) >
                                            XmoMiniPlayerAnimation
                                                .axisThresholdPx ||
                                        abs(rawY) >
                                            XmoMiniPlayerAnimation
                                                .axisThresholdPx
                                        )
                                ) {
                                    moved = true

                                    axis =
                                        if (
                                            abs(rawX) >
                                            abs(rawY)
                                        ) {
                                            XmoMiniAxis.Horizontal
                                        } else {
                                            XmoMiniAxis.Vertical
                                        }
                                }

                                scope.launch {
                                    when (axis) {
                                        XmoMiniAxis.Horizontal -> {
                                            y.snapTo(0f)

                                            x.snapTo(
                                                XmoMiniPlayerAnimation
                                                    .horizontalResistance(
                                                        rawX
                                                    )
                                            )
                                        }

                                        XmoMiniAxis.Vertical -> {
                                            x.snapTo(0f)

                                            y.snapTo(
                                                XmoMiniPlayerAnimation
                                                    .verticalResistance(
                                                        rawY
                                                    )
                                            )
                                        }

                                        XmoMiniAxis.None ->
                                            Unit
                                    }
                                }
                            },

                            onDragEnd = {
                                val finalAxis =
                                    axis

                                val finalX =
                                    rawX

                                val finalY =
                                    rawY

                                rawX = 0f
                                rawY = 0f
                                axis =
                                    XmoMiniAxis.None

                                scope.launch {
                                    when (finalAxis) {
                                        XmoMiniAxis.Horizontal -> {
                                            y.snapTo(0f)

                                            /*
                                             * RIGHT -> LEFT = NEXT
                                             */
                                            val goNext =
                                                finalX <
                                                    -XmoMiniPlayerAnimation
                                                        .horizontalThresholdPx

                                            /*
                                             * LEFT -> RIGHT = PREVIOUS
                                             */
                                            val goPrevious =
                                                finalX >
                                                    XmoMiniPlayerAnimation
                                                        .horizontalThresholdPx

                                            when {
                                                goNext &&
                                                    previewIndex <
                                                    queue.lastIndex -> {

                                                    transitionDirection =
                                                        1

                                                    previewIndex++

                                                    previewRevision++
                                                }

                                                goPrevious &&
                                                    previewIndex >
                                                    0 -> {

                                                    transitionDirection =
                                                        -1

                                                    previewIndex--

                                                    previewRevision++
                                                }
                                            }

                                            /*
                                             * Visual card itself always
                                             * returns immediately. Playback
                                             * is not waiting on this spring.
                                             */
                                            launch {
                                                x.animateTo(
                                                    targetValue = 0f,
                                                    animationSpec =
                                                        XmoMiniPlayerAnimation
                                                            .horizontalReturnSpec
                                                )
                                            }

                                            moved = false
                                        }

                                        XmoMiniAxis.Vertical -> {
                                            x.snapTo(0f)

                                            when {
                                                finalY <=
                                                    XmoMiniPlayerAnimation
                                                        .openThresholdPx -> {
                                                    openOrdered()
                                                }

                                                finalY >=
                                                    XmoMiniPlayerAnimation
                                                        .closeThresholdPx -> {
                                                    closeOrdered()
                                                }

                                                else -> {
                                                    y.animateTo(
                                                        targetValue = 0f,
                                                        animationSpec =
                                                            XmoMiniPlayerAnimation
                                                                .verticalReturnSpec
                                                    )

                                                    moved = false
                                                }
                                            }
                                        }

                                        XmoMiniAxis.None -> {
                                            x.snapTo(0f)
                                            y.snapTo(0f)
                                            moved = false
                                        }
                                    }
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    x.animateTo(
                                        targetValue = 0f,
                                        animationSpec =
                                            XmoMiniPlayerAnimation
                                                .horizontalReturnSpec
                                    )

                                    y.animateTo(
                                        targetValue = 0f,
                                        animationSpec =
                                            XmoMiniPlayerAnimation
                                                .verticalReturnSpec
                                    )

                                    rawX = 0f
                                    rawY = 0f
                                    axis =
                                        XmoMiniAxis.None
                                    moved = false
                                }
                            }
                        )
                    }
        ) {
            XmoMiniPlayerCard(
                song =
                    visualSong,
                isPlaying =
                    state.isPlaying,
                position =
                    if (
                        visualSong.id ==
                        state.currentSongId
                    ) {
                        state.position
                    } else {
                        0L
                    },
                duration =
                    if (
                        visualSong.id ==
                        state.currentSongId
                    ) {
                        state.duration
                    } else {
                        visualSong.duration
                    },
                theme = theme,
                colors = colors,
                accent = accent,
                liked =
                    visualSong.id in
                        likedSongIds,
                transitionDirection =
                    transitionDirection,
                moved = moved,
                opening = opening,
                togglePlay =
                    togglePlay,
                toggleLike = {
                    toggleLike(
                        visualSong.id
                    )
                },
                open = {
                    if (!moved) {
                        scope.launch {
                            openOrdered()
                        }
                    }
                },
                modifier =
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .graphicsLayer {
                            translationX =
                                x.value

                            translationY =
                                y.value +
                                    entranceY.value
                        }
            )
        }
    }
}
