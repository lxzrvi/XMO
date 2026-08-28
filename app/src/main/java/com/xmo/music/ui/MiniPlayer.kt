package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
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

private fun miniHorizontalResistance(
    raw: Float
): Float {
    val free = 68f
    val amount = abs(raw)

    if (amount <= free) {
        return raw
    }

    return sign(raw) *
        (
            free +
                (amount - free) * .08f
            )
}

private fun miniUpResistance(
    raw: Float
): Float {
    /*
     * Down drag MiniPlayer par allow nahi.
     */
    if (raw >= 0f) {
        return 0f
    }

    val amount = -raw
    val free = 42f

    return if (amount <= free) {
        -amount
    } else {
        -(
            free +
                (amount - free) * .075f
            )
    }
}

@Composable
fun MiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,

    /*
     * Increment hone par MiniPlayer NavBar ke neeche se
     * apni resting position tak rise karega.
     *
     * 0 = no entrance animation.
     */
    riseKey: Int,

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

    /*
     * Entire MiniPlayer only Y axis par move hota hai for
     * swipe-up interaction / initial rise.
     *
     * Horizontal song change outer box ko move nahi karta.
     */
    val cardY = remember {
        Animatable(0f)
    }

    /*
     * Sirf internal content horizontal slide karta hai.
     */
    val contentX = remember {
        Animatable(0f)
    }

    var axis by remember {
        mutableStateOf(
            MiniAxis.None
        )
    }

    var rawX by remember {
        mutableFloatStateOf(0f)
    }

    var rawY by remember {
        mutableFloatStateOf(0f)
    }

    var dragged by remember {
        mutableStateOf(false)
    }

    /*
     * MiniPlayer post-NowPlaying close entrance.
     *
     * 120px bottom offset roughly NavBar ke peeche se start
     * karata hai, actual final position NavBar ke upar hai.
     */
    val entranceY = remember {
        Animatable(0f)
    }

    var lastRiseKey by remember {
        mutableStateOf(riseKey)
    }

    LaunchedEffect(riseKey) {
        if (
            riseKey > 0 &&
            riseKey != lastRiseKey
        ) {
            lastRiseKey = riseKey

            entranceY.snapTo(
                120f
            )

            entranceY.animateTo(
                0f,
                spring(
                    dampingRatio = .86f,
                    stiffness = 340f
                )
            )
        }
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
                 * Approved NavBar expanded selector se clear.
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
                    .height(60.dp)
                    .graphicsLayer {
                        translationY =
                            cardY.value +
                                entranceY.value
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
                    .pointerInput(
                        state.currentSongId
                    ) {
                        detectDragGestures(
                            onDragStart = {
                                axis =
                                    MiniAxis.None

                                rawX = 0f
                                rawY = 0f
                                dragged = false
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                rawX += amount.x
                                rawY += amount.y

                                /*
                                 * Direction lock only after small slop.
                                 */
                                if (
                                    axis ==
                                    MiniAxis.None &&
                                    (
                                        abs(rawX) > 10f ||
                                            abs(rawY) > 10f
                                        )
                                ) {
                                    dragged = true

                                    axis =
                                        if (
                                            abs(rawX) >
                                            abs(rawY)
                                        ) {
                                            MiniAxis.Horizontal
                                        } else {
                                            MiniAxis.Vertical
                                        }
                                }

                                scope.launch {
                                    when (axis) {
                                        MiniAxis.Horizontal -> {
                                            /*
                                             * Outer card remains stationary.
                                             */
                                            cardY.snapTo(0f)

                                            contentX.snapTo(
                                                miniHorizontalResistance(
                                                    rawX
                                                )
                                            )
                                        }

                                        MiniAxis.Vertical -> {
                                            /*
                                             * Horizontal content stays fixed.
                                             */
                                            contentX.snapTo(0f)

                                            cardY.snapTo(
                                                miniUpResistance(
                                                    rawY
                                                )
                                            )
                                        }

                                        MiniAxis.None ->
                                            Unit
                                    }
                                }
                            },

                            onDragEnd = {
                                val finalAxis = axis
                                val dx = rawX
                                val dy = rawY

                                scope.launch {
                                    when (finalAxis) {
                                        MiniAxis.Horizontal -> {
                                            cardY.snapTo(0f)

                                            /*
                                             * XMO requested mapping:
                                             *
                                             * LEFT -> PREVIOUS
                                             */
                                            if (dx < -42f) {
                                                contentX.animateTo(
                                                    -widthPx,
                                                    spring(
                                                        dampingRatio = 1f,
                                                        stiffness = 520f
                                                    )
                                                )

                                                previous()

                                                /*
                                                 * New internal content comes
                                                 * from opposite side.
                                                 */
                                                contentX.snapTo(
                                                    widthPx
                                                )

                                                contentX.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .88f,
                                                        stiffness = 420f
                                                    )
                                                )

                                            /*
                                             * RIGHT -> NEXT
                                             */
                                            } else if (
                                                dx > 42f
                                            ) {
                                                contentX.animateTo(
                                                    widthPx,
                                                    spring(
                                                        dampingRatio = 1f,
                                                        stiffness = 520f
                                                    )
                                                )

                                                next()

                                                contentX.snapTo(
                                                    -widthPx
                                                )

                                                contentX.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .88f,
                                                        stiffness = 420f
                                                    )
                                                )
                                            } else {
                                                contentX.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .80f,
                                                        stiffness = 450f
                                                    )
                                                )
                                            }
                                        }

                                        MiniAxis.Vertical -> {
                                            contentX.snapTo(0f)

                                            /*
                                             * Enough upward intent:
                                             * first MiniPlayer settles,
                                             * THEN NowPlaying is requested.
                                             */
                                            if (dy < -34f) {
                                                cardY.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .76f,
                                                        stiffness = 440f
                                                    )
                                                )

                                                openPlayer()
                                            } else {
                                                cardY.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio = .80f,
                                                        stiffness = 450f
                                                    )
                                                )
                                            }
                                        }

                                        MiniAxis.None -> {
                                            /*
                                             * No drag = tap.
                                             *
                                             * MiniPlayer itself does not animate.
                                             */
                                            openPlayer()
                                        }
                                    }

                                    rawX = 0f
                                    rawY = 0f
                                    axis = MiniAxis.None

                                    withFrameNanos { }
                                    dragged = false
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    contentX.animateTo(0f)
                                    cardY.animateTo(0f)

                                    rawX = 0f
                                    rawY = 0f
                                    axis = MiniAxis.None

                                    withFrameNanos { }
                                    dragged = false
                                }
                            }
                        )
                    }
            ) {
                /*
                 * Real playback progress line.
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

                /*
                 * Only THIS content translates horizontally.
                 * Outer MiniPlayer frame stays fixed.
                 */
                Row(
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                contentX.value
                        }
                        .padding(
                            start = 4.dp,
                            top = 4.dp,
                            end = 8.dp,
                            bottom = 4.dp
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
                                horizontal =
                                    10.dp
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
                }

                /*
                 * Play button intentionally OUTSIDE translated content,
                 * so outer card + control remain steady.
                 */
                Box(
                    Modifier
                        .align(
                            Alignment.CenterEnd
                        )
                        .padding(
                            end = 8.dp
                        )
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
                        .pointerInput(
                            state.isPlaying
                        ) {
                            /*
                             * Parent drag detector owns main card gesture.
                             * Keeping control visually stable.
                             */
                        },
                    contentAlignment =
                        Alignment.Center
                ) {
                    /*
                     * Clickable overlay separate from translated content.
                     */
                    Box(
                        Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .clickable {
                                togglePlay()
                            },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        if (state.isPlaying) {
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
