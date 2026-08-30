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
import kotlin.math.max

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
     * KEYBOARD POSITION
     * =========================================================
     *
     * Important rule:
     *
     * MiniPlayer may move UP with the keyboard,
     * but it can never move BELOW its normal resting position
     * while the keyboard closes.
     *
     * This removes:
     *
     * keyboard closing
     * -> MiniPlayer falls toward NavBar
     * -> MiniPlayer jumps/rises back into place
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
                128.dp.toPx()
            }

    val keyboardBottomPx =
        imeBottomPx +
            with(density) {
                6.dp.toPx()
            }

    /*
     * Direct system-IME following.
     *
     * During keyboard close:
     * keyboardBottomPx decreases frame-by-frame until it reaches
     * normalBottomPx, then stops there.
     *
     * It therefore never dips behind/into the NavBar.
     */
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
     * NOW PLAYING -> MINIPLAYER
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
     * Real distance below the physical screen.
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
         * Search keyboard cannot remain over Now Playing.
         */
        keyboardController?.hide()

        x.snapTo(0f)

        /*
         * Do NOT return y to zero.
         *
         * Up-swipe release continues from the exact stretched
         * position directly downward.
         */
        y.animateTo(
            targetValue =
                screenExitY,
            animationSpec =
                XmoMiniPlayerAnimation
                    .openExitSpec
        )

        /*
         * MiniPlayer is completely hidden first.
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
         * Down swipe also continues from its current stretched
         * position instead of returning home first.
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
                                                targetValue = 0f,
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
