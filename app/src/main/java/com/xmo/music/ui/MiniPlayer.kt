package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
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

    val c = homeColors(theme)
    val scope = rememberCoroutineScope()

    val x = remember {
        Animatable(0f)
    }

    val y = remember {
        Animatable(0f)
    }

    var dragging by remember {
        mutableStateOf(false)
    }

    /*
     * Mini player sits above:
     *
     * NavBar bottom position: 35dp + navigation inset
     * NavBar overflow: up to ~80dp
     *
     * 35 + 80 + safety spacing = 127dp.
     */
    AnimatedVisibility(
        visible = visible,

        enter =
            slideInVertically(
                initialOffsetY = {
                    it + 40
                }
            ) +
                fadeIn(),

        exit =
            slideOutVertically(
                targetOffsetY = {
                    it + 40
                }
            ) +
                fadeOut(),

        modifier =
            Modifier.fillMaxSize()
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(
                    start = 14.dp,
                    end = 14.dp,
                    bottom = 126.dp
                ),
            contentAlignment =
                Alignment.BottomCenter
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .graphicsLayer {
                        translationX = x.value
                        translationY = y.value
                    }
                    .clip(
                        RoundedCornerShape(15.dp)
                    )
                    .background(c.surface)
                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(15.dp)
                    )
                    .pointerInput(
                        state.currentSongId
                    ) {
                        detectDragGestures(
                            onDragStart = {
                                dragging = true
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                scope.launch {
                                    x.snapTo(
                                        x.value +
                                            amount.x
                                    )

                                    y.snapTo(
                                        (
                                            y.value +
                                                amount.y
                                            )
                                            /*
                                             * Up allowed.
                                             * Down has resistance.
                                             */
                                            .coerceAtMost(
                                                18f
                                            )
                                    )
                                }
                            },

                            onDragEnd = {
                                dragging = false

                                scope.launch {
                                    val horizontal =
                                        abs(x.value) >
                                            abs(y.value)

                                    /*
                                     * LEFT -> next
                                     */
                                    if (
                                        horizontal &&
                                        x.value <
                                        -70f
                                    ) {
                                        next()

                                        x.animateTo(
                                            -size.width
                                                .toFloat(),
                                            spring(
                                                dampingRatio = 1f,
                                                stiffness = 500f
                                            )
                                        )

                                        x.snapTo(
                                            size.width
                                                .toFloat()
                                        )

                                        x.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = .9f,
                                                stiffness = 420f
                                            )
                                        )

                                        y.animateTo(0f)

                                        return@launch
                                    }

                                    /*
                                     * RIGHT -> previous
                                     */
                                    if (
                                        horizontal &&
                                        x.value >
                                        70f
                                    ) {
                                        previous()

                                        x.animateTo(
                                            size.width
                                                .toFloat(),
                                            spring(
                                                dampingRatio = 1f,
                                                stiffness = 500f
                                            )
                                        )

                                        x.snapTo(
                                            -size.width
                                                .toFloat()
                                        )

                                        x.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = .9f,
                                                stiffness = 420f
                                            )
                                        )

                                        y.animateTo(0f)

                                        return@launch
                                    }

                                    /*
                                     * UP:
                                     *
                                     * Must first return to its resting
                                     * position. Player opens only after
                                     * release + settle.
                                     */
                                    if (
                                        !horizontal &&
                                        y.value <
                                        -55f
                                    ) {
                                        x.animateTo(0f)

                                        y.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = .78f,
                                                stiffness = 430f
                                            )
                                        )

                                        openPlayer()

                                        return@launch
                                    }

                                    x.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = .8f,
                                            stiffness = 450f
                                        )
                                    )

                                    y.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = .8f,
                                            stiffness = 450f
                                        )
                                    )
                                }
                            },

                            onDragCancel = {
                                dragging = false

                                scope.launch {
                                    x.animateTo(0f)
                                    y.animateTo(0f)
                                }
                            }
                        )
                    }
                    .clickable(
                        enabled = !dragging
                    ) {
                        openPlayer()
                    }
            ) {
                /*
                 * ---------------------------------------------
                 * REAL PROGRESS — TOP BORDER
                 * ---------------------------------------------
                 */
                val progress =
                    if (
                        state.duration >
                        0
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
                    /*
                     * COVER
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

                    /*
                     * TEXT
                     */
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(
                                horizontal = 11.dp
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
                     * PLAY / PAUSE CAPSULE
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
                                c.text,
                                Modifier.size(
                                    18.dp
                                )
                            )
                        } else {
                            androidx.compose.material3.Icon(
                                Icons.Default.PlayArrow,
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
        val w =
            size.width

        val h =
            size.height

        val bw =
            w * .20f

        val bh =
            h * .70f

        val top =
            (h - bh) /
                2f

        drawRoundRect(
            color,
            topLeft =
                Offset(
                    w * .24f,
                    top
                ),
            size =
                Size(
                    bw,
                    bh
                )
        )

        drawRoundRect(
            color,
            topLeft =
                Offset(
                    w * .56f,
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
