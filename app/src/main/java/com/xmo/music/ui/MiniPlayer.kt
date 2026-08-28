package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.blur.glassBorder
import com.xmo.music.ui.blur.glassHighlight
import com.xmo.music.ui.blur.liveBlur
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private enum class MiniAxis {
    None,
    Horizontal,
    Vertical
}

private fun miniHorizontalResistance(
    value: Float
): Float {
    val free =
        76f

    val distance =
        abs(value)

    if (
        distance <=
        free
    ) {
        return value
    }

    return (
        free +
            (
                distance -
                    free
                ) *
            .07f
        ) *
        sign(
            value
        )
}

private fun miniVerticalResistance(
    value: Float
): Float {
    if (
        value >=
        0f
    ) {
        return 0f
    }

    val distance =
        -value

    val free =
        45f

    if (
        distance <=
        free
    ) {
        return -distance
    }

    return -(
        free +
            (
                distance -
                    free
                ) *
            .065f
        )
}

@Composable
fun MiniPlayer(
    state: PlaybackState,
    theme: XmoTheme,
    hazeState: HazeState,
    riseKey: Int,
    liked: Boolean,
    openPlayer: () -> Unit,
    togglePlay: () -> Unit,
    toggleLike: () -> Unit,

    /*
     * XMO's intentional MiniPlayer direction:
     *
     * LEFT  -> previous
     * RIGHT -> next
     */
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
        homeColors(
            theme
        )

    val accent =
        LocalXmoAccent.current

    val scope =
        rememberCoroutineScope()

    val density =
        LocalDensity.current

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
     * When NowPlaying has fully disappeared, the newly composed
     * MiniPlayer rises from behind the NavBar.
     */
    val entranceY =
        remember(
            riseKey
        ) {
            Animatable(
                if (
                    riseKey >
                    0
                ) {
                    with(
                        density
                    ) {
                        150.dp.toPx()
                    }
                } else {
                    0f
                }
            )
        }

    LaunchedEffect(
        riseKey
    ) {
        if (
            entranceY.value !=
            0f
        ) {
            entranceY.animateTo(
                0f,

                spring(
                    dampingRatio =
                        .86f,

                    stiffness =
                        320f
                )
            )
        }
    }

    /*
     * Tap opening has an ordered transition:
     *
     * MiniPlayer -> below screen -> callback -> NowPlaying.
     */
    var opening by
        remember {
            mutableStateOf(
                false
            )
        }

    suspend fun openOrdered() {
        if (
            opening
        ) {
            return
        }

        opening =
            true

        x.snapTo(
            0f
        )

        y.animateTo(
            with(
                density
            ) {
                150.dp.toPx()
            },

            animationSpec =
                spring(
                    dampingRatio =
                        .92f,

                    stiffness =
                        430f
                )
        )

        openPlayer()
    }

    var axis by
        remember {
            mutableStateOf(
                MiniAxis.None
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

    var moved by
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

    /*
     * IME inset is applied before NavBar spacing. When a keyboard
     * is visible the player therefore sits above it, while all
     * existing card gestures remain attached to the same card.
     */
    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(
                WindowInsets.ime
            )
            .navigationBarsPadding()
            .padding(
                start = 14.dp,
                end = 14.dp,
                bottom = 128.dp
            ),

        contentAlignment =
            Alignment.BottomCenter
    ) {
        BoxWithConstraints(
            Modifier.fillMaxWidth()
        ) {
            /*
             * The gesture host is deliberately taller than the
             * visible 60dp player. Its transparent area above the
             * card makes upward swipe acquisition easier.
             */
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(
                        86.dp
                    )
                    .align(
                        Alignment.BottomCenter
                    )
                    .pointerInput(
                        state.currentSongId,
                        opening
                    ) {
                        if (
                            opening
                        ) {
                            return@pointerInput
                        }

                        detectDragGestures(
                            onDragStart = {
                                axis =
                                    MiniAxis.None

                                rawX =
                                    0f

                                rawY =
                                    0f

                                moved =
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

                                if (
                                    axis ==
                                    MiniAxis.None &&
                                    (
                                        abs(rawX) >
                                            9f ||
                                            abs(rawY) >
                                            9f
                                        )
                                ) {
                                    moved =
                                        true

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
                                    when (
                                        axis
                                    ) {
                                        MiniAxis.Horizontal -> {
                                            y.snapTo(
                                                0f
                                            )

                                            x.snapTo(
                                                miniHorizontalResistance(
                                                    rawX
                                                )
                                            )
                                        }

                                        MiniAxis.Vertical -> {
                                            x.snapTo(
                                                0f
                                            )

                                            y.snapTo(
                                                miniVerticalResistance(
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
                                        MiniAxis.Horizontal -> {
                                            y.snapTo(
                                                0f
                                            )

                                            val goPrevious =
                                                finalX <
                                                    -48f

                                            val goNext =
                                                finalX >
                                                    48f

                                            x.animateTo(
                                                0f,

                                                spring(
                                                    dampingRatio =
                                                        .80f,

                                                    stiffness =
                                                        470f
                                                )
                                            )

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

                                        MiniAxis.Vertical -> {
                                            x.snapTo(
                                                0f
                                            )

                                            val shouldOpen =
                                                finalY <
                                                    -38f

                                            y.animateTo(
                                                0f,

                                                spring(
                                                    dampingRatio =
                                                        .80f,

                                                    stiffness =
                                                        450f
                                                )
                                            )

                                            if (
                                                shouldOpen
                                            ) {
                                                /*
                                                 * Up-swipe returns to its
                                                 * rest position first, then
                                                 * performs the same ordered
                                                 * exit used by a tap.
                                                 */
                                                openOrdered()
                                            }
                                        }

                                        MiniAxis.None -> {
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
                                        MiniAxis.None

                                    moved =
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
                                        MiniAxis.None

                                    moved =
                                        false
                                }
                            }
                        )
                    }
            ) {
                /*
                 * Small visible grab region at the top of the
                 * enlarged gesture host.
                 */
                Box(
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .padding(
                            top = 7.dp
                        )
                        .width(
                            48.dp
                        )
                        .height(
                            4.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                2.dp
                            )
                        )
                        .background(
                            c.sub.copy(
                                alpha = .26f
                            )
                        )
                )

                Box(
                    Modifier
                        .align(
                            Alignment.BottomCenter
                        )
                        .fillMaxWidth()
                        .height(
                            60.dp
                        )
                        .graphicsLayer {
                            translationX =
                                x.value

                            translationY =
                                y.value +
                                    entranceY.value
                        }
                        .clip(
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .liveBlur(
                            hazeState,
                            theme
                        )
                        .drawBehind {
                            val radius =
                                15.dp.toPx()

                            drawRoundRect(
                                brush =
                                    Brush.verticalGradient(
                                        listOf(
                                            glassHighlight(
                                                theme
                                            ),
                                            Color.Transparent,
                                            Color.Transparent
                                        )
                                    ),

                                cornerRadius =
                                    CornerRadius(
                                        radius
                                    )
                            )

                            drawRoundRect(
                                color =
                                    glassBorder(
                                        theme
                                    ),

                                cornerRadius =
                                    CornerRadius(
                                        radius
                                    ),

                                style =
                                    Stroke(
                                        .4.dp.toPx()
                                    )
                            )
                        }
                        .border(
                            .65.dp,
                            glassBorder(
                                theme
                            ),
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                ) {
                    /*
                     * Real playback progress.
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
                                accent,

                            size =
                                Size(
                                    size.width *
                                        progress,

                                    size.height
                                )
                        )
                    }

                    /*
                     * Card tap region excludes interactive right
                     * buttons, preventing accidental opening.
                     */
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(
                                start = 4.dp,
                                top = 4.dp,
                                end = 94.dp,
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
                                .weight(
                                    1f
                                )
                                .padding(
                                    start = 9.dp,
                                    end = 5.dp
                                )
                        ) {
                            Text(
                                state.title,

                                color =
                                    c.text,

                                fontFamily =
                                    XmoFont.bold,

                                fontSize =
                                    12.sp,

                                maxLines =
                                    1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Text(
                                state.artist,

                                color =
                                    c.sub,

                                fontFamily =
                                    XmoFont.thin,

                                fontSize =
                                    9.sp,

                                maxLines =
                                    1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        }
                    }

                    /*
                     * Transparent tap layer over artwork/text only.
                     */
                    Box(
                        Modifier
                            .align(
                                Alignment.CenterStart
                            )
                            .fillMaxWidth()
                            .height(
                                58.dp
                            )
                            .padding(
                                end =
                                    94.dp
                            )
                            .pointerInput(
                                state.currentSongId,
                                moved,
                                opening
                            ) {
                                if (
                                    opening
                                ) {
                                    return@pointerInput
                                }

                                detectTapGestures(
                                    onTap = {
                                        if (
                                            !moved
                                        ) {
                                            scope.launch {
                                                openOrdered()
                                            }
                                        }
                                    }
                                )
                            }
                    )

                    /*
                     * Like + play controls use the same pill/glass
                     * language as Home refresh/hamburger.
                     */
                    Row(
                        Modifier
                            .align(
                                Alignment.CenterEnd
                            )
                            .padding(
                                end = 6.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    22.dp
                                )
                            )
                            .background(
                                c.button
                            )
                            .border(
                                .6.dp,
                                glassBorder(
                                    theme
                                ),
                                RoundedCornerShape(
                                    22.dp
                                )
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(
                                    38.dp
                                )
                                .pointerInput(
                                    liked
                                ) {
                                    detectTapGestures {
                                        toggleLike()
                                    }
                                },

                            contentAlignment =
                                Alignment.Center
                        ) {
                            XmoIcon(
                                icon =
                                    R.drawable.ic_xmo_heart,

                                tint =
                                    if (
                                        liked
                                    ) {
                                        accent
                                    } else {
                                        c.icon
                                    },

                                modifier =
                                    Modifier.size(
                                        17.dp
                                    )
                            )
                        }

                        Box(
                            Modifier
                                .size(
                                    42.dp
                                )
                                .pointerInput(
                                    state.isPlaying
                                ) {
                                    detectTapGestures {
                                        togglePlay()
                                    }
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
                                        Icons.Default.PlayArrow,

                                    contentDescription =
                                        "Play",

                                    tint =
                                        c.text,

                                    modifier =
                                        Modifier.size(
                                            22.dp
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
                ),

            cornerRadius =
                CornerRadius(
                    barWidth /
                        2f
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
                ),

            cornerRadius =
                CornerRadius(
                    barWidth /
                        2f
                )
        )
    }
}
