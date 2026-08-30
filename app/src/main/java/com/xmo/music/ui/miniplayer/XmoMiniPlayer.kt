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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.homeColors
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun XmoMiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,
    riseKey: Int,
    liked: Boolean,
    openPlayer: () -> Unit,
    closePlayer: () -> Unit,
    togglePlay: () -> Unit,
    toggleLike: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit
) {
    if (
        state.currentSongId ==
        null
    ) {
        return
    }

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

    val view =
        LocalView.current

    val x =
        remember {
            Animatable(0f)
        }

    val y =
        remember {
            Animatable(0f)
        }

    /*
     * =========================================================
     * SYSTEM INSETS
     * =========================================================
     *
     * There is deliberately NO Compose tween around the IME
     * inset. Android already animates this value.
     *
     * MiniPlayer therefore follows the keyboard's actual
     * frame-by-frame movement instead of arriving late.
     */

    val navigationBottomPx =
        WindowInsets.navigationBars
            .getBottom(density)

    val imeBottomPx =
        WindowInsets.ime
            .getBottom(density)

    val keyboardVisible =
        imeBottomPx >
            navigationBottomPx

    val navigationBottomDp =
        with(density) {
            navigationBottomPx.toDp()
        }

    val imeBottomDp =
        with(density) {
            imeBottomPx.toDp()
        }

    val bottomPadding =
        if (keyboardVisible) {
            /*
             * Keyboard's current animated top.
             *
             * As IME opens/closes, this value follows it directly.
             */
            imeBottomDp +
                6.dp
        } else {
            /*
             * Existing normal MiniPlayer position.
             */
            navigationBottomDp +
                128.dp
        }

    /*
     * =========================================================
     * RETURN FROM NOW PLAYING
     * =========================================================
     *
     * This is separate from keyboard positioning.
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
            entranceY.value !=
            0f
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

    /*
     * =========================================================
     * FULL SCREEN EXIT DISTANCE
     * =========================================================
     *
     * Uses the actual Android view height.
     *
     * This guarantees that MiniPlayer really reaches below the
     * physical screen instead of stopping at an arbitrary 96dp /
     * 220dp offset.
     */

    val screenExitY =
        view.height
            .toFloat()
            .takeIf {
                it > 0f
            }
            ?: with(density) {
                900.dp.toPx()
            }

    /*
     * =========================================================
     * OPEN NOW PLAYING
     * =========================================================
     */

    suspend fun openOrdered() {
        if (
            opening ||
            closing
        ) {
            return
        }

        opening = true

        /*
         * Search keyboard must not survive into Now Playing.
         *
         * We do not wait for its animation to complete.
         */
        keyboardController?.hide()

        x.snapTo(0f)

        /*
         * IMPORTANT:
         *
         * Do not reset y to zero.
         *
         * If user released the card while raised, its current
         * dragged position is the start of this direct downward
         * exit.
         */
        y.animateTo(
            targetValue =
                screenExitY,
            animationSpec =
                XmoMiniPlayerAnimation
                    .openExitSpec
        )

        /*
         * Only now, after MiniPlayer is physically below the
         * screen, create Now Playing.
         */
        openPlayer()
    }

    /*
     * =========================================================
     * CLOSE PLAYBACK
     * =========================================================
     */

    suspend fun closeOrdered() {
        if (
            opening ||
            closing
        ) {
            return
        }

        closing = true

        x.snapTo(0f)

        /*
         * Same behavior:
         * current downward stretch -> directly below screen.
         */
        y.animateTo(
            targetValue =
                screenExitY,
            animationSpec =
                XmoMiniPlayerAnimation
                    .closeExitSpec
        )

        closePlayer()
    }

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
        /*
         * Invisible gesture host.
         */
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
                        closing
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

                                rawX +=
                                    amount.x

                                rawY +=
                                    amount.y

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

                                            val goPrevious =
                                                finalX <
                                                    -XmoMiniPlayerAnimation
                                                        .horizontalThresholdPx

                                            val goNext =
                                                finalX >
                                                    XmoMiniPlayerAnimation
                                                        .horizontalThresholdPx

                                            x.animateTo(
                                                targetValue =
                                                    0f,
                                                animationSpec =
                                                    XmoMiniPlayerAnimation
                                                        .horizontalReturnSpec
                                            )

                                            when {
                                                goPrevious ->
                                                    previous()

                                                goNext ->
                                                    next()
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
                                                        targetValue =
                                                            0f,
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
                                        targetValue =
                                            0f,
                                        animationSpec =
                                            XmoMiniPlayerAnimation
                                                .horizontalReturnSpec
                                    )

                                    y.animateTo(
                                        targetValue =
                                            0f,
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
                state = state,
                theme = theme,
                colors = colors,
                accent = accent,
                liked = liked,
                moved = moved,
                opening = opening,
                togglePlay =
                    togglePlay,
                toggleLike =
                    toggleLike,
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
