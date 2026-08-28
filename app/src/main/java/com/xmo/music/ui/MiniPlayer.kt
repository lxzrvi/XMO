package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private enum class MiniAxis {
    None,
    Horizontal,
    Vertical
}

private fun resistHorizontal(
    value: Float
): Float {
    val free = 76f
    val distance = abs(value)

    if (distance <= free) {
        return value
    }

    return sign(value) *
        (
            free +
                (distance - free) * .10f
            )
}

private fun resistUp(
    value: Float
): Float {
    /*
     * MiniPlayer cannot be pulled downward.
     */
    if (value >= 0f) {
        return 0f
    }

    val distance = -value
    val free = 46f

    return if (distance <= free) {
        -distance
    } else {
        -(
            free +
                (distance - free) * .09f
            )
    }
}

@Composable
fun MiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,
    enterFromBottom: Boolean,
    openPlayer: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit
) {
    if (state.currentSongId == null) {
        return
    }

    val c = homeColors(theme)
    val scope = rememberCoroutineScope()

    val x = remember {
        Animatable(0f)
    }

    val y = remember {
        Animatable(0f)
    }

    /*
     * Used ONLY when MiniPlayer reappears after closing the
     * full player.
     *
     * Tapping MiniPlayer does not play an exit animation;
     * App removes it immediately.
     */
    val entranceY = remember {
        Animatable(
            if (enterFromBottom)
                100f
            else
                0f
        )
    }

    val entranceAlpha = remember {
        Animatable(
            if (enterFromBottom)
                0f
            else
                1f
        )
    }

    LaunchedEffect(Unit) {
        if (enterFromBottom) {
            launch {
                entranceY.animateTo(
                    0f,
                    spring(
                        dampingRatio = .86f,
                        stiffness = 360f
                    )
                )
            }

            launch {
                entranceAlpha.animateTo(
                    1f
                )
            }
        }
    }

    var axis by remember {
        mutableStateOf(
            MiniAxis.None
        )
    }

    var totalX by remember {
        mutableFloatStateOf(0f)
    }

    var totalY by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Distinguishes a tap from actual drag.
     */
    var didDrag by remember {
        mutableStateOf(false)
    }

    val progress =
        if (state.duration > 0L) {
            (
                state.position.toFloat() /
                    state.duration.toFloat()
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
                start = 14.dp,
                end = 14.dp,

                /*
                 * Safe above approved expanding NavBar.
                 */
                bottom = 128.dp
            ),
        contentAlignment =
            Alignment.BottomCenter
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth()
        ) {
            val widthPx =
                constraints.maxWidth
                    .toFloat()

            Box(
                Modifier
                    .fillMaxWidth()

                    /*
                     * 4dp lower than old 64dp.
                     */
                    .height(60.dp)

                    .graphicsLayer {
                        translationX =
                            x.value

                        translationY =
                            y.value +
                                entranceY.value

                        alpha =
                            entranceAlpha.value
                    }

                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )

                    .background(
                        c.surface
                    )

                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(
                            14.dp
                        )
                    )

                    /*
                     * Tap is handled separately.
                     */
                    .clickable {
                        if (!didDrag) {
                            /*
                             * No MiniPlayer exit animation.
                             */
                            openPlayer()
                        }
                    }

                    .pointerInput(
                        state.currentSongId
                    ) {
                        detectDragGestures(
                            onDragStart = {
                                axis =
                                    MiniAxis.None

                                totalX = 0f
                                totalY = 0f
                                didDrag = false
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                totalX += amount.x
                                totalY += amount.y

                                if (
                                    axis ==
                                    MiniAxis.None
                                ) {
                                    if (
                                        abs(totalX) > 10f ||
                                        abs(totalY) > 10f
                                    ) {
                                        didDrag = true

                                        axis =
                                            if (
                                                abs(totalX) >
                                                abs(totalY)
                                            ) {
                                                MiniAxis.Horizontal
                                            } else {
                                                MiniAxis.Vertical
                                            }
                                    }
                                }

                                scope.launch {
                                    when (axis) {
                                        MiniAxis.Horizontal -> {
                                            /*
                                             * HORIZONTAL LOCK:
                                             * Y never moves.
                                             */
                                            y.snapTo(0f)

                                            x.snapTo(
                                                resistHorizontal(
                                                    totalX
                                                )
                                            )
                                        }

                                        MiniAxis.Vertical -> {
                                            /*
                                             * VERTICAL LOCK:
                                             * X never moves.
                                             */
                                            x.snapTo(0f)

                                            y.snapTo(
                                                resistUp(
                                                    totalY
                                                )
                                            )
                                        }

                                        MiniAxis.None ->
                                            Unit
                                    }
                                }
                            },

                            onDragEnd = {
                                val finalAxis =
                                    axis

                                val dx = totalX
                                val dy = totalY

                                scope.launch {
                                    when (finalAxis) {
                                        MiniAxis.Horizontal -> {

                                            /*
                                             * User requirement:
                                             *
                                             * LEFT -> PREVIOUS
                                             */
                                            if (dx < -45f) {
                                                x.animateTo(
                                                    -widthPx,
                                                    spring(
                                                        dampingRatio = 1f,
                                                        stiffness = 520f
                                                    )
                                                )

                                                previous()

                                                /*
                                                 * New song enters from right.
                                                 */
                                                x.snapTo(
                                                    widthPx
                                                )

                                                x.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .88f,
                                                        stiffness = 430f
                                                    )
                                                )

                                            /*
                                             * RIGHT -> NEXT
                                             */
                                            } else if (
                                                dx > 45f
                                            ) {
                                                x.animateTo(
                                                    widthPx,
                                                    spring(
                                                        dampingRatio = 1f,
                                                        stiffness = 520f
                                                    )
                                                )

                                                next()

                                                /*
                                                 * New song enters from left.
                                                 */
                                                x.snapTo(
                                                    -widthPx
                                                )

                                                x.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .88f,
                                                        stiffness = 430f
                                                    )
                                                )
                                            } else {
                                                x.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .8f,
                                                        stiffness = 450f
                                                    )
                                                )
                                            }

                                            y.snapTo(0f)
                                        }

                                        MiniAxis.Vertical -> {
                                            x.snapTo(0f)

                                            if (dy < -36f) {
                                                /*
                                                 * First settle exactly back into
                                                 * its normal location.
                                                 */
                                                y.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .76f,
                                                        stiffness = 440f
                                                    )
                                                )

                                                /*
                                                 * Only AFTER release + settle.
                                                 */
                                                openPlayer()
                                            } else {
                                                y.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .78f,
                                                        stiffness = 450f
                                                    )
                                                )
                                            }
                                        }

                                        MiniAxis.None -> {
                                            x.snapTo(0f)
                                            y.snapTo(0f)
                                        }
                                    }

                                    totalX = 0f
                                    totalY = 0f
                                    axis = MiniAxis.None

                                    /*
                                     * Delay reset one frame so the pointer-up
                                     * isn't interpreted as a click after drag.
                                     */
                                    withFrameNanos { }
                                    didDrag = false
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    x.animateTo(0f)
                                    y.animateTo(0f)

                                    totalX = 0f
                                    totalY = 0f
                                    axis = MiniAxis.None

                                    withFrameNanos { }
                                    didDrag = false
                                }
                            }
                        )
                    }
            ) {
                /*
                 * Thin real progress line.
                 */
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .align(
                            Alignment.TopStart
                        )
                ) {
                    drawRect(
                        color =
                            XmoRed,

                        size =
                            Size(
                                size.width *
                                    progress,

                                size.height
                            )
                    )
                }

                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = 4.dp,
                            top = 4.dp,
                            end = 8.dp,
                            bottom = 4.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    /*
                     * 50dp artwork for 60dp player.
                     */
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
                                .size(50.dp)
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
                            .weight(1f)
                            .padding(
                                horizontal = 10.dp
                            )
                    ) {
                        Text(
                            state.title,
                            color = c.text,
                            fontFamily =
                                XmoFont.bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Text(
                            state.artist,
                            color = c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    /*
                     * Circular play/pause.
                     */
                    Box(
                        Modifier
                            .size(42.dp)
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
                        if (state.isPlaying) {
                            MiniPauseIcon(
                                color = c.text,
                                modifier =
                                    Modifier.size(
                                        18.dp
                                    )
                            )
                        } else {
                            Icon(
                                Icons.Default
                                    .PlayArrow,
                                null,
                                tint = c.text,
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
}

@Composable
private fun MiniPauseIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(modifier) {
        val bw =
            size.width * .19f

        val bh =
            size.height * .68f

        val top =
            (
                size.height -
                    bh
                ) / 2f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .25f,
                    top
                ),
            size =
                Size(
                    bw,
                    bh
                )
        )

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .56f,
                    top
                ),
            size =
                Size(
                    bw,
                    bh
                )
        )
    }
}
