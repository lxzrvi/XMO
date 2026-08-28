package com.xmo.music.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
fun NowPlaying(
    state: PlaybackState,
    theme: XmoTheme,
    source: String,
    sourceIsCategory: Boolean,
    refreshPosition: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,
    dismiss: () -> Unit
) {
    val c =
        homeColors(theme)

    val scope =
        rememberCoroutineScope()

    var screenHeight by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Complete screen offset.
     *
     * 0 = fully open
     * height = below display
     */
    val offsetY =
        remember {
            Animatable(0f)
        }

    var entranceDone by remember {
        mutableStateOf(false)
    }

    /*
     * Bottom -> top entrance.
     */
    LaunchedEffect(screenHeight) {
        if (
            screenHeight > 0f &&
            !entranceDone
        ) {
            entranceDone = true

            offsetY.snapTo(
                screenHeight
            )

            offsetY.animateTo(
                0f,
                animationSpec =
                    spring(
                        dampingRatio = .88f,
                        stiffness = 300f
                    )
            )
        }
    }

    /*
     * Real MediaController position refresh.
     *
     * This does not invent progress.
     */
    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (true) {
            refreshPosition()

            delay(
                if (state.isPlaying)
                    250L
                else
                    500L
            )
        }
    }

    suspend fun closePlayer() {
        if (
            screenHeight <= 0f
        ) {
            dismiss()
            return
        }

        offsetY.animateTo(
            screenHeight,
            animationSpec =
                spring(
                    dampingRatio = 1f,
                    stiffness = 260f
                )
        )

        dismiss()
    }

    BackHandler {
        scope.launch {
            closePlayer()
        }
    }

    /*
     * As player moves towards bottom:
     *
     * 0 -> square screen
     * 1 -> rounded sheet
     */
    val dismissProgress =
        if (
            screenHeight >
            0f
        ) {
            (
                offsetY.value /
                    screenHeight
                )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    val topRadius =
        30.dp *
            dismissProgress

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                screenHeight =
                    it.height
                        .toFloat()
            }
            .graphicsLayer {
                translationY =
                    offsetY.value
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        topRadius,
                    topEnd =
                        topRadius
                )
            )
            .background(c.bg)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
                .padding(
                    horizontal = 14.dp
                )
        ) {
            /*
             * =================================================
             * HEADER / DRAG AREA
             * =================================================
             *
             * Drag gesture is on header rather than whole player,
             * so seek/artwork/vertical content don't fight it.
             */
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .pointerInput(
                        screenHeight
                    ) {
                        detectVerticalDragGestures(
                            onVerticalDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                /*
                                 * Downward only.
                                 *
                                 * Upward drag while fully open
                                 * doesn't move the player.
                                 */
                                val target =
                                    (
                                        offsetY.value +
                                            amount
                                        )
                                        .coerceIn(
                                            0f,
                                            screenHeight
                                                .coerceAtLeast(
                                                    0f
                                                )
                                        )

                                scope.launch {
                                    offsetY.snapTo(
                                        target
                                    )
                                }
                            },

                            onDragEnd = {
                                scope.launch {
                                    /*
                                     * 18% gives quick sheet-like dismissal.
                                     */
                                    if (
                                        screenHeight >
                                        0f &&
                                        offsetY.value >
                                        screenHeight *
                                            .18f
                                    ) {
                                        closePlayer()
                                    } else {
                                        offsetY.animateTo(
                                            0f,
                                            animationSpec =
                                                spring(
                                                    dampingRatio =
                                                        .82f,
                                                    stiffness =
                                                        390f
                                                )
                                        )
                                    }
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    offsetY.animateTo(
                                        0f,
                                        animationSpec =
                                            spring(
                                                dampingRatio =
                                                    .85f,
                                                stiffness =
                                                    380f
                                            )
                                    )
                                }
                            }
                        )
                    },

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                /*
                 * DOWN / BACK
                 */
                IconButton(
                    onClick = {
                        scope.launch {
                            closePlayer()
                        }
                    },
                    modifier =
                        Modifier
                            .size(40.dp)
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
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .KeyboardArrowDown,
                        contentDescription =
                            "Close player",
                        tint =
                            c.text,
                        modifier =
                            Modifier.size(
                                25.dp
                            )
                    )
                }

                /*
                 * SOURCE
                 */
                Column(
                    Modifier
                        .weight(1f)
                        .padding(
                            horizontal =
                                10.dp
                        ),
                    horizontalAlignment =
                        Alignment
                            .CenterHorizontally
                ) {
                    Text(
                        if (
                            sourceIsCategory
                        ) {
                            "PLAYING FROM CATEGORY"
                        } else {
                            "PLAYING FROM"
                        },
                        color =
                            c.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize =
                            9.sp,
                        letterSpacing =
                            1.1.sp,
                        maxLines =
                            1
                    )

                    Text(
                        source,
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
                }

                /*
                 * MENU VISUAL ONLY.
                 *
                 * No fake actions.
                 */
                Box(
                    Modifier
                        .size(40.dp)
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
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .MoreVert,
                        contentDescription =
                            null,
                        tint =
                            c.icon,
                        modifier =
                            Modifier.size(
                                21.dp
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(
                    22.dp
                )
            )

            /*
             * =================================================
             * ARTWORK
             * =================================================
             */
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(
                        RoundedCornerShape(
                            24.dp
                        )
                    )
                    .background(
                        c.surface
                    )
                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(
                            24.dp
                        )
                    )
            ) {
                AsyncImage(
                    model =
                        state.artworkUri
                            ?.let(
                                Uri::parse
                            ),
                    contentDescription =
                        state.title,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )

                /*
                 * Local artwork missing fallback.
                 */
                if (
                    state.artworkUri ==
                    null
                ) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            "XMO",
                            color =
                                XmoRed.copy(
                                    alpha =
                                        .75f
                                ),
                            fontFamily =
                                XmoFont.logo,
                            fontSize =
                                32.sp
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(
                    24.dp
                )
            )

            /*
             * =================================================
             * CONNECTED PLAYER BOX
             * =================================================
             */
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    )
                    .background(
                        c.surface
                    )
                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp,
                            bottomStart = 8.dp,
                            bottomEnd = 8.dp
                        )
                    )
                    .padding(
                        horizontal =
                            20.dp,
                        vertical =
                            24.dp
                    )
            ) {
                /*
                 * SONG TITLE
                 */
                Text(
                    text =
                        state.title
                            .ifBlank {
                                "Unknown song"
                            },
                    color =
                        c.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize =
                        21.sp,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow
                            .Ellipsis
                )

                Spacer(
                    Modifier.height(
                        3.dp
                    )
                )

                Text(
                    text =
                        state.artist
                            .ifBlank {
                                "Unknown artist"
                            },
                    color =
                        c.sub,
                    fontFamily =
                        XmoFont.normal,
                    fontSize =
                        13.sp,
                    maxLines =
                        1,
                    overflow =
                        TextOverflow
                            .Ellipsis
                )

                Spacer(
                    Modifier.height(
                        24.dp
                    )
                )

                /*
                 * =================================================
                 * SEEK BAR
                 * =================================================
                 */
                val duration =
                    state.duration
                        .coerceAtLeast(
                            1L
                        )

                val sliderPosition =
                    (
                        state.position
                            .toFloat() /
                            duration
                                .toFloat()
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

                Slider(
                    value =
                        sliderPosition,

                    onValueChange = {
                            fraction ->

                        val position =
                            (
                                duration
                                    .toDouble() *
                                    fraction
                                )
                                .toLong()

                        seekTo(
                            position
                        )
                    },

                    colors =
                        SliderDefaults.colors(
                            thumbColor =
                                XmoRed,
                            activeTrackColor =
                                XmoRed,
                            inactiveTrackColor =
                                c.border
                        )
                )

                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement
                            .SpaceBetween
                ) {
                    Text(
                        formatPlayerTime(
                            state.position
                        ),
                        color =
                            c.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize =
                            10.sp
                    )

                    Text(
                        formatPlayerTime(
                            state.duration
                        ),
                        color =
                            c.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize =
                            10.sp
                    )
                }

                Spacer(
                    Modifier.height(
                        20.dp
                    )
                )

                /*
                 * =================================================
                 * MAIN CONTROLS
                 * =================================================
                 */
                Row(
                    Modifier
                        .fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement
                            .SpaceEvenly,
                    verticalAlignment =
                        Alignment
                            .CenterVertically
                ) {
                    IconButton(
                        onClick =
                            previous,
                        enabled =
                            state.currentSongId !=
                                null
                    ) {
                        PreviousPlayerIcon(
                            color =
                                if (
                                    state.currentSongId !=
                                    null
                                ) {
                                    c.text
                                } else {
                                    c.sub
                                },
                            modifier =
                                Modifier.size(
                                    30.dp
                                )
                        )
                    }

                    IconButton(
                        onClick =
                            togglePlay,
                        enabled =
                            state.currentSongId !=
                                null,
                        modifier =
                            Modifier
                                .size(
                                    60.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .background(
                                    XmoRed
                                )
                    ) {
                        if (
                            state.isPlaying
                        ) {
                            PausePlayerIcon(
                                color =
                                    Color.White,
                                modifier =
                                    Modifier.size(
                                        30.dp
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
                                    Color.White,
                                modifier =
                                    Modifier.size(
                                        31.dp
                                    )
                            )
                        }
                    }

                    IconButton(
                        onClick =
                            next,
                        enabled =
                            state.hasNext
                    ) {
                        NextPlayerIcon(
                            color =
                                if (
                                    state.hasNext
                                ) {
                                    c.text
                                } else {
                                    c.sub
                                },
                            modifier =
                                Modifier.size(
                                    30.dp
                                )
                        )
                    }
                }

                /*
                 * =================================================
                 * DETAILS
                 * =================================================
                 */
                if (
                    state.album
                        .isNotBlank()
                ) {
                    Spacer(
                        Modifier.height(
                            55.dp
                        )
                    )

                    Text(
                        "SONG DETAILS",
                        color =
                            XmoRed,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            11.sp,
                        letterSpacing =
                            1.sp
                    )

                    Spacer(
                        Modifier.height(
                            12.dp
                        )
                    )

                    Row(
                        Modifier
                            .fillMaxWidth(),
                        verticalAlignment =
                            Alignment
                                .CenterVertically
                    ) {
                        Text(
                            "Album",
                            color =
                                c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize =
                                12.sp
                        )

                        Text(
                            state.album,
                            color =
                                c.text,
                            fontFamily =
                                XmoFont.medium,
                            fontSize =
                                12.sp,
                            maxLines =
                                1,
                            overflow =
                                TextOverflow
                                    .Ellipsis,
                            textAlign =
                                TextAlign.End,
                            modifier =
                                Modifier
                                    .weight(
                                        1f
                                    )
                                    .padding(
                                        start =
                                            20.dp
                                    )
                        )
                    }
                }

                /*
                 * =================================================
                 * BRAND
                 * =================================================
                 */
                Spacer(
                    Modifier.height(
                        90.dp
                    )
                )

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment
                            .CenterHorizontally
                ) {
                    Text(
                        "XMO",
                        color =
                            c.text,
                        fontFamily =
                            XmoFont.logo,
                        fontSize =
                            18.sp
                    )

                    Spacer(
                        Modifier.height(
                            3.dp
                        )
                    )

                    Text(
                        "lxzrvi  •  copyright © 2026",
                        color =
                            c.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize =
                            9.sp
                    )
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(
                            12.dp
                        )
                )
            }
        }
    }
}

/*
 * =============================================================
 * CUSTOM PLAYER ICONS
 *
 * material-icons-extended intentionally NOT used.
 * =============================================================
 */

@Composable
private fun PreviousPlayerIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(modifier) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .10f

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    w * .20f,
                    h * .20f
                ),
            size =
                Size(
                    bar,
                    h * .60f
                ),
            cornerRadius =
                CornerRadius(
                    bar / 2f
                )
        )

        val triangle =
            Path().apply {
                moveTo(
                    w * .73f,
                    h * .17f
                )

                lineTo(
                    w * .32f,
                    h * .50f
                )

                lineTo(
                    w * .73f,
                    h * .83f
                )

                close()
            }

        drawPath(
            triangle,
            color
        )
    }
}

@Composable
private fun NextPlayerIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(modifier) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .10f

        val triangle =
            Path().apply {
                moveTo(
                    w * .27f,
                    h * .17f
                )

                lineTo(
                    w * .68f,
                    h * .50f
                )

                lineTo(
                    w * .27f,
                    h * .83f
                )

                close()
            }

        drawPath(
            triangle,
            color
        )

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    w * .70f,
                    h * .20f
                ),
            size =
                Size(
                    bar,
                    h * .60f
                ),
            cornerRadius =
                CornerRadius(
                    bar / 2f
                )
        )
    }
}

@Composable
private fun PausePlayerIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(modifier) {
        val barWidth =
            size.width *
                .18f

        val barHeight =
            size.height *
                .62f

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
                        .27f,
                    top
                ),
            size =
                Size(
                    barWidth,
                    barHeight
                ),
            cornerRadius =
                CornerRadius(
                    barWidth *
                        .22f
                )
        )

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    size.width *
                        .55f,
                    top
                ),
            size =
                Size(
                    barWidth,
                    barHeight
                ),
            cornerRadius =
                CornerRadius(
                    barWidth *
                        .22f
                )
        )
    }
}

private fun formatPlayerTime(
    milliseconds: Long
): String {
    val seconds =
        milliseconds
            .coerceAtLeast(
                0L
            ) /
            1000L

    val minutes =
        seconds /
            60L

    val remaining =
        seconds %
            60L

    return "$minutes:${
        remaining
            .toString()
            .padStart(
                2,
                '0'
            )
    }"
}
