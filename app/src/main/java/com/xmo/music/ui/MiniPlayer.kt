package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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

@Composable
fun MiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,
    visible: Boolean,
    openPlayer: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit
) {
    if (
        state.currentSongId == null
    ) {
        return
    }

    val c =
        homeColors(theme)

    val scope =
        rememberCoroutineScope()

    val x =
        remember {
            Animatable(0f)
        }

    val y =
        remember {
            Animatable(0f)
        }

    var dragged by remember {
        mutableStateOf(false)
    }

    var totalX by remember {
        mutableFloatStateOf(0f)
    }

    var totalY by remember {
        mutableFloatStateOf(0f)
    }

    AnimatedVisibility(
        visible = visible,

        enter =
            slideInVertically(
                initialOffsetY = {
                    it + 36
                }
            ) + fadeIn(),

        exit =
            slideOutVertically(
                targetOffsetY = {
                    it + 36
                }
            ) + fadeOut(),

        modifier =
            Modifier.fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                /*
                 * Approved NavBar:
                 *
                 * bottom ~35dp
                 * expanded selector ~80dp
                 *
                 * MiniPlayer stays safely above it.
                 */
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 128.dp
                ),
            contentAlignment =
                Alignment.BottomCenter
        ) {
            BoxWithConstraints {
                val widthPx =
                    constraints
                        .maxWidth
                        .toFloat()

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer {
                            translationX =
                                x.value

                            translationY =
                                y.value
                        }
                        .clip(
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .background(
                            c.surface
                        )
                        .border(
                            .7.dp,
                            c.border,
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .pointerInput(
                            state.currentSongId
                        ) {
                            detectDragGestures(
                                onDragStart = {
                                    dragged = true
                                    totalX = 0f
                                    totalY = 0f
                                },

                                onDrag = {
                                        change,
                                        amount ->

                                    change.consume()

                                    totalX +=
                                        amount.x

                                    totalY +=
                                        amount.y

                                    scope.launch {
                                        x.snapTo(
                                            totalX
                                        )

                                        /*
                                         * Up follows normally.
                                         * Down gets limited.
                                         */
                                        y.snapTo(
                                            totalY.coerceAtMost(
                                                20f
                                            )
                                        )
                                    }
                                },

                                onDragEnd = {
                                    val horizontal =
                                        abs(totalX) >
                                            abs(totalY)

                                    scope.launch {

                                        /*
                                         * Requirement:
                                         *
                                         * LEFT = PREVIOUS
                                         */
                                        if (
                                            horizontal &&
                                            totalX <
                                            -70f
                                        ) {
                                            x.animateTo(
                                                -widthPx,
                                                spring(
                                                    dampingRatio = 1f,
                                                    stiffness = 500f
                                                )
                                            )

                                            previous()

                                            x.snapTo(
                                                widthPx
                                            )

                                            x.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = .88f,
                                                    stiffness = 420f
                                                )
                                            )

                                            y.animateTo(0f)

                                        /*
                                         * RIGHT = NEXT
                                         */
                                        } else if (
                                            horizontal &&
                                            totalX >
                                            70f
                                        ) {
                                            x.animateTo(
                                                widthPx,
                                                spring(
                                                    dampingRatio = 1f,
                                                    stiffness = 500f
                                                )
                                            )

                                            next()

                                            x.snapTo(
                                                -widthPx
                                            )

                                            x.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = .88f,
                                                    stiffness = 420f
                                                )
                                            )

                                            y.animateTo(0f)

                                        /*
                                         * UP -> settle first,
                                         * THEN player opens.
                                         */
                                        } else if (
                                            !horizontal &&
                                            totalY <
                                            -50f
                                        ) {
                                            x.animateTo(0f)

                                            y.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = .76f,
                                                    stiffness = 430f
                                                )
                                            )

                                            openPlayer()

                                        } else {
                                            x.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = .8f,
                                                    stiffness = 430f
                                                )
                                            )

                                            y.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = .8f,
                                                    stiffness = 430f
                                                )
                                            )
                                        }

                                        totalX = 0f
                                        totalY = 0f
                                        dragged = false
                                    }
                                },

                                onDragCancel = {
                                    scope.launch {
                                        x.animateTo(0f)
                                        y.animateTo(0f)

                                        totalX = 0f
                                        totalY = 0f
                                        dragged = false
                                    }
                                }
                            )
                        }
                        .clickable(
                            enabled =
                                !dragged
                        ) {
                            openPlayer()
                        }
                ) {
                    /*
                     * Real playback progress.
                     */
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

                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .align(
                                Alignment.TopStart
                            )
                    ) {
                        drawRect(
                            color = XmoRed,

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
                                    .size(54.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            12.dp
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
                                        11.dp
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
                                    TextOverflow
                                        .Ellipsis
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
                                    TextOverflow
                                        .Ellipsis
                            )
                        }

                        /*
                         * Wide Play/Pause pill.
                         */
                        Box(
                            Modifier
                                .width(72.dp)
                                .height(44.dp)
                                .clip(
                                    RoundedCornerShape(
                                        20.dp
                                    )
                                )
                                .background(
                                    c.button
                                )
                                .border(
                                    .6.dp,
                                    c.border,
                                    RoundedCornerShape(
                                        20.dp
                                    )
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

        val y =
            (
                size.height -
                    bh
                ) / 2f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .25f,
                    y
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
                    y
                ),
            size =
                Size(
                    bw,
                    bh
                )
        )
    }
}
