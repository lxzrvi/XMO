package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.blur.glassBorder
import com.xmo.music.ui.blur.glassHighlight
import com.xmo.music.ui.blur.liveBlur
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private enum class MiniDragAxis {
    None,
    Horizontal,
    Vertical
}

/*
 * Horizontal:
 *
 * first 60px = normal
 * after that = strong resistance
 */
private fun horizontalResistance(
    raw: Float
): Float {
    val free =
        60f

    val distance =
        abs(raw)

    if (
        distance <= free
    ) {
        return raw
    }

    val result =
        free +
            (
                distance -
                    free
                ) *
            .08f

    return result *
        sign(raw)
}

/*
 * Vertical:
 *
 * downward drag not allowed
 *
 * first ~40px upward = normal
 * after that = very strong resistance
 */
private fun upwardResistance(
    raw: Float
): Float {
    if (
        raw >= 0f
    ) {
        return 0f
    }

    val distance =
        -raw

    val free =
        40f

    if (
        distance <= free
    ) {
        return -distance
    }

    return -(
        free +
            (
                distance -
                    free
                ) *
            .07f
        )
}

@Composable
fun MiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,
    hazeState: HazeState,

    /*
     * 0:
     * normal composition
     *
     * >0:
     * this MiniPlayer was created after NowPlaying completely
     * closed, so it rises from behind NavBar.
     */
    riseKey: Int,

    openPlayer: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit
) {
    if (
        state.currentSongId ==
        null
    ) {
        return
    }

    val c =
        homeColors(theme)

    val scope =
        rememberCoroutineScope()

    /*
     * Main card gesture displacement.
     *
     * Horizontal swipe:
     * x changes, y = 0
     *
     * Up swipe:
     * y changes, x = 0
     */
    val x =
        remember {
            Animatable(
                0f
            )
        }

    val y =
        remember {
            Animatable(
                0f
            )
        }

    /*
     * Post-NowPlaying entrance.
     *
     * Because MiniPlayer is newly composed after full player
     * dismissal, initial value itself can start below NavBar.
     */
    val entranceY =
        remember {
            Animatable(
                if (
                    riseKey >
                    0
                ) {
                    150f
                } else {
                    0f
                }
            )
        }

    LaunchedEffect(Unit) {
        if (
            riseKey >
            0
        ) {
            entranceY.animateTo(
                targetValue =
                    0f,

                animationSpec =
                    spring(
                        dampingRatio =
                            .86f,

                        stiffness =
                            320f
                    )
            )
        }
    }

    var axis by
        remember {
            mutableStateOf(
                MiniDragAxis.None
            )
        }

    var rawX by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    var rawY by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * Stops tap callback after an actual drag.
     */
    var dragging by
        remember {
            mutableStateOf(
                false
            )
        }

    val progress =
        if (
            state.duration >
            0L
        ) {
            (
                state.position
                    .toFloat() /
                    state.duration
                        .toFloat()
                )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    Box(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(
                start =
                    14.dp,

                end =
                    14.dp,

                /*
                 * Final location safely above approved NavBar,
                 * including its expanded selector.
                 */
                bottom =
                    128.dp
            ),

        contentAlignment =
            Alignment.BottomCenter
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth()
        ) {
            /*
             * Card itself.
             */
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(
                        60.dp
                    )

                    /*
                     * Entire box follows finger.
                     */
                    .graphicsLayer {
                        translationX =
                            x.value

                        translationY =
                            y.value +
                                entranceY.value
                    }

                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )

                    /*
                     * ONE shared real Haze blur.
                     */
                    .liveBlur(
                        state =
                            hazeState,

                        theme =
                            theme
                    )

                    /*
                     * Cheap reflection / edge.
                     */
                    .drawBehind {
                        val corner =
                            14.dp.toPx()

                        drawRoundRect(
                            brush =
                                Brush.verticalGradient(
                                    colors =
                                        listOf(
                                            glassHighlight(
                                                theme
                                            ),

                                            Color.Transparent,

                                            Color.Transparent
                                        ),

                                    startY =
                                        0f,

                                    endY =
                                        size.height *
                                            .72f
                                ),

                            cornerRadius =
                                CornerRadius(
                                    corner
                                )
                        )

                        drawRoundRect(
                            color =
                                glassBorder(
                                    theme
                                ),

                            cornerRadius =
                                CornerRadius(
                                    corner
                                ),

                            style =
                                Stroke(
                                    width =
                                        .35.dp.toPx()
                                )
                        )
                    }

                    .border(
                        width =
                            .65.dp,

                        color =
                            c.border,

                        shape =
                            RoundedCornerShape(
                                14.dp
                            )
                    )

                    /*
                     * Tap:
                     *
                     * no MiniPlayer animation.
                     * NowPlaying simply rises above it.
                     */
                    .clickable(
                        enabled =
                            !dragging
                    ) {
                        openPlayer()
                    }

                    /*
                     * Swipe gesture.
                     */
                    .pointerInput(
                        state.currentSongId
                    ) {
                        detectDragGestures(
                            onDragStart = {
                                axis =
                                    MiniDragAxis.None

                                rawX =
                                    0f

                                rawY =
                                    0f

                                dragging =
                                    false
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                rawX +=
                                    amount.x

                                rawY +=
                                    amount.y

                                /*
                                 * Wait until direction is clear,
                                 * then lock it.
                                 */
                                if (
                                    axis ==
                                    MiniDragAxis.None &&
                                    (
                                        abs(rawX) >
                                            10f ||
                                            abs(rawY) >
                                            10f
                                        )
                                ) {
                                    dragging =
                                        true

                                    axis =
                                        if (
                                            abs(rawX) >
                                            abs(rawY)
                                        ) {
                                            MiniDragAxis.Horizontal
                                        } else {
                                            MiniDragAxis.Vertical
                                        }
                                }

                                scope.launch {
                                    when (
                                        axis
                                    ) {
                                        MiniDragAxis.Horizontal -> {
                                            /*
                                             * Horizontal gesture cannot
                                             * move vertically.
                                             */
                                            y.snapTo(
                                                0f
                                            )

                                            x.snapTo(
                                                horizontalResistance(
                                                    rawX
                                                )
                                            )
                                        }

                                        MiniDragAxis.Vertical -> {
                                            /*
                                             * Up gesture cannot move
                                             * horizontally.
                                             */
                                            x.snapTo(
                                                0f
                                            )

                                            y.snapTo(
                                                upwardResistance(
                                                    rawY
                                                )
                                            )
                                        }

                                        MiniDragAxis.None ->
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

                                scope.launch {
                                    when (
                                        finalAxis
                                    ) {
                                        /*
                                         * =================================
                                         * LEFT / RIGHT
                                         * =================================
                                         *
                                         * Requirement:
                                         *
                                         * box moves a little
                                         *        ↓
                                         * release
                                         *        ↓
                                         * FIRST restore box
                                         *        ↓
                                         * THEN change song/details
                                         */
                                        MiniDragAxis.Horizontal -> {

                                            y.snapTo(
                                                0f
                                            )

                                            val goPrevious =
                                                finalX <
                                                    -40f

                                            val goNext =
                                                finalX >
                                                    40f

                                            /*
                                             * Box comes home first.
                                             */
                                            x.animateTo(
                                                targetValue =
                                                    0f,

                                                animationSpec =
                                                    spring(
                                                        dampingRatio =
                                                            .78f,

                                                        stiffness =
                                                            440f
                                                    )
                                            )

                                            /*
                                             * Details only change AFTER
                                             * exact resting position.
                                             */
                                            if (
                                                goPrevious
                                            ) {
                                                previous()
                                            } else if (
                                                goNext
                                            ) {
                                                next()
                                            }
                                        }

                                        /*
                                         * =================================
                                         * SWIPE UP
                                         * =================================
                                         */
                                        MiniDragAxis.Vertical -> {

                                            x.snapTo(
                                                0f
                                            )

                                            val shouldOpen =
                                                finalY <
                                                    -32f

                                            /*
                                             * Card returns to its exact
                                             * normal position first.
                                             */
                                            y.animateTo(
                                                targetValue =
                                                    0f,

                                                animationSpec =
                                                    spring(
                                                        dampingRatio =
                                                            .76f,

                                                        stiffness =
                                                            440f
                                                    )
                                            )

                                            /*
                                             * Only after settle.
                                             */
                                            if (
                                                shouldOpen
                                            ) {
                                                openPlayer()
                                            }
                                        }

                                        MiniDragAxis.None -> {
                                            /*
                                             * Tap is owned by clickable.
                                             */
                                            x.snapTo(
                                                0f
                                            )

                                            y.snapTo(
                                                0f
                                            )
                                        }
                                    }

                                    rawX =
                                        0f

                                    rawY =
                                        0f

                                    axis =
                                        MiniDragAxis.None

                                    /*
                                     * Re-enable tap after gesture has
                                     * completely ended.
                                     */
                                    withFrameNanos { }

                                    dragging =
                                        false
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    x.animateTo(
                                        0f
                                    )

                                    y.animateTo(
                                        0f
                                    )

                                    rawX =
                                        0f

                                    rawY =
                                        0f

                                    axis =
                                        MiniDragAxis.None

                                    withFrameNanos { }

                                    dragging =
                                        false
                                }
                            }
                        )
                    }
            ) {
                /*
                 * =================================================
                 * TOP PLAYBACK PROGRESS
                 * =================================================
                 */
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            1.5.dp
                        )
                        .align(
                            Alignment.TopStart
                        )
                ) {
                    drawRect(
                        color =
                            XmoRed,

                        size =
                            Size(
                                width =
                                    size.width *
                                        progress,

                                height =
                                    size.height
                            )
                    )
                }

                /*
                 * =================================================
                 * CONTENT
                 * =================================================
                 *
                 * Content itself does not independently slide.
                 */
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start =
                                4.dp,

                            top =
                                4.dp,

                            end =
                                56.dp,

                            bottom =
                                4.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model =
                            state.artworkUri
                                ?.let(
                                    Uri::parse
                                ),

                        contentDescription =
                            null,

                        modifier =
                            Modifier
                                .size(
                                    50.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        11.dp
                                    )
                                )
                                .background(
                                    c.button
                                ),

                        contentScale =
                            ContentScale.Crop
                    )

                    Column(
                        Modifier
                            .padding(
                                start =
                                    10.dp,

                                end =
                                    6.dp
                            )
                    ) {
                        Text(
                            text =
                                state.title,

                            color =
                                c.text,

                            fontFamily =
                                XmoFont.bold,

                            fontSize =
                                13.sp,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Text(
                            text =
                                state.artist,

                            color =
                                c.sub,

                            fontFamily =
                                XmoFont.thin,

                            fontSize =
                                10.sp,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }

                /*
                 * =================================================
                 * PLAY / PAUSE CIRCLE
                 * =================================================
                 */
                Box(
                    Modifier
                        .align(
                            Alignment.CenterEnd
                        )
                        .padding(
                            end =
                                7.dp
                        )
                        .size(
                            42.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            c.button
                        )
                        .border(
                            .6.dp,
                            c.border,
                            CircleShape
                        )
                        .clickable {
                            togglePlay()
                        },

                    contentAlignment =
                        Alignment.Center
                ) {
                    if (
                        state.isPlaying
                    ) {
                        MiniPauseIcon(
                            color =
                                c.text,

                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.Default
                                    .PlayArrow,

                            contentDescription =
                                null,

                            tint =
                                c.text,

                            modifier =
                                Modifier.size(
                                    21.dp
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniPauseIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(
        modifier
    ) {
        val barWidth =
            size.width *
                .19f

        val barHeight =
            size.height *
                .68f

        val top =
            (
                size.height -
                    barHeight
                ) /
                2f

        drawRoundRect(
            color =
                color,

            topLeft =
                Offset(
                    size.width *
                        .25f,

                    top
                ),

            size =
                Size(
                    barWidth,
                    barHeight
                )
        )

        drawRoundRect(
            color =
                color,

            topLeft =
                Offset(
                    size.width *
                        .56f,

                    top
                ),

            size =
                Size(
                    barWidth,
                    barHeight
                )
        )
    }
}
