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

    var screenHeight by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * Screen displacement.
     */
    val offset =
        remember {
            Animatable(
                0f
            )
        }

    var entranceStarted by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * ---------------------------------------------------------
     * ENTRANCE
     * ---------------------------------------------------------
     */
    LaunchedEffect(
        screenHeight
    ) {
        if (
            screenHeight >
                0f &&
            !entranceStarted
        ) {
            entranceStarted =
                true

            /*
             * Start below display.
             */
            offset.snapTo(
                screenHeight
            )

            /*
             * Ease/spring into screen.
             */
            offset.animateTo(
                targetValue =
                    0f,

                animationSpec =
                    spring(
                        dampingRatio =
                            .88f,

                        stiffness =
                            300f
                    )
            )
        }
    }

    /*
     * ---------------------------------------------------------
     * REAL PLAYBACK POSITION
     * ---------------------------------------------------------
     */
    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (true) {
            refreshPosition()

            delay(
                if (
                    state.isPlaying
                ) {
                    250L
                } else {
                    500L
                }
            )
        }
    }

    suspend fun close() {
        if (
            screenHeight <=
            0f
        ) {
            dismiss()
            return
        }

        offset.animateTo(
            targetValue =
                screenHeight,

            animationSpec =
                spring(
                    dampingRatio =
                        1f,

                    stiffness =
                        270f
                )
        )

        dismiss()
    }

    BackHandler {
        scope.launch {
            close()
        }
    }

    val dismissProgress =
        if (
            screenHeight >
            0f
        ) {
            (
                offset.value /
                    screenHeight
                )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    /*
     * Full open:
     * 0dp radius.
     *
     * Moving down:
     * progressively round.
     */
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
                    offset.value
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        topRadius,

                    topEnd =
                        topRadius
                )
            )
            .background(
                c.bg
            )
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
                    horizontal =
                        14.dp
                )
        ) {
            /*
             * =================================================
             * HEADER
             * =================================================
             */
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        62.dp
                    )
                    /*
                     * Player-sheet drag begins here.
                     *
                     * Keeps Slider and vertical content gestures
                     * independent.
                     */
                    .pointerInput(
                        screenHeight
                    ) {
                        detectVerticalDragGestures(
                            onVerticalDrag = {
                                    change,
                                    delta ->

                                change.consume()

                                val target =
                                    (
                                        offset.value +
                                            delta
                                        )
                                        .coerceIn(
                                            0f,

                                            screenHeight
                                                .coerceAtLeast(
                                                    0f
                                                )
                                        )

                                scope.launch {
                                    offset.snapTo(
                                        target
                                    )
                                }
                            },

                            onDragEnd = {
                                scope.launch {
                                    /*
                                     * Down far enough:
                                     * dismiss.
                                     */
                                    if (
                                        screenHeight >
                                        0f &&
                                        offset.value >
                                        screenHeight *
                                            .18f
                                    ) {
                                        close()
                                    } else {
                                        /*
                                         * Not enough:
                                         * restore.
                                         */
                                        offset.animateTo(
                                            0f,

                                            animationSpec =
                                                spring(
                                                    dampingRatio =
                                                        .82f,

                                                    stiffness =
                                                        400f
                                                )
                                        )
                                    }
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    offset.animateTo(
                                        0f
                                    )
                                }
                            }
                        )
                    },

                verticalAlignment =
                    Alignment
                        .CenterVertically
            ) {
                /*
                 * DOWN
                 */
                IconButton(
                    onClick = {
                        scope.launch {
                            close()
                        }
                    },

                    modifier =
                        Modifier
                            .size(
                                40.dp
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
                 * PLAYING FROM
                 */
                Column(
                    Modifier
                        .weight(
                            1f
                        )
                        .padding(
                            horizontal =
                                8.dp
                        ),

                    horizontalAlignment =
                        Alignment
                            .CenterHorizontally
                ) {
                    Text(
                        text =
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
                            1.sp,

                        maxLines =
                            1
                    )

                    Text(
                        text =
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
                 * MENU SHELL
                 */
                Box(
                    Modifier
                        .size(
                            40.dp
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
             * COVER
             * =================================================
             */
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(
                        1f
                    )
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
                        Modifier
                            .fillMaxSize(),

                    contentScale =
                        ContentScale.Crop
                )

                if (
                    state.artworkUri ==
                    null
                ) {
                    Box(
                        Modifier
                            .fillMaxSize(),

                        contentAlignment =
                            Alignment.Center
                    ) {
                        Text(
                            "XMO",

                            color =
                                XmoRed.copy(
                                    alpha =
                                        .72f
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
             * PLAYER CARD
             * =================================================
             */
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart =
                                28.dp,

                            topEnd =
                                28.dp,

                            bottomStart =
                                10.dp,

                            bottomEnd =
                                10.dp
                        )
                    )
                    .background(
                        c.surface
                    )
                    .border(
                        .7.dp,

                        c.border,

                        RoundedCornerShape(
                            topStart =
                                28.dp,

                            topEnd =
                                28.dp,

                            bottomStart =
                                10.dp,

                            bottomEnd =
                                10.dp
                        )
                    )
                    .padding(
                        20.dp
                    )
            ) {
                /*
                 * TITLE
                 */
                Text(
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

                Text(
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
                 * SEEK
                 * =================================================
                 */
                val duration =
                    state.duration
                        .coerceAtLeast(
                            1L
                        )

                val progress =
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
                        progress,

                    onValueChange = {
                            fraction ->

                        seekTo(
                            (
                                duration
                                    .toDouble() *
                                    fraction
                                )
                                .toLong()
                        )
                    },

                    colors =
                        SliderDefaults
                            .colors(
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
                 * CONTROLS
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
                        PreviousIcon(
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
                            PauseIcon(
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
                        NextIcon(
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
                            58.dp
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
                                            18.dp
                                    )
                        )
                    }
                }

                /*
                 * =================================================
                 * FOOTER
                 * =================================================
                 */
                Spacer(
                    Modifier.height(
                        90.dp
                    )
                )

                Column(
                    Modifier
                        .fillMaxWidth(),

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
 * LIGHTWEIGHT PLAYER ICONS
 *
 * No material-icons-extended.
 * =============================================================
 */

@Composable
private fun PreviousIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .1f

        drawRoundRect(
            color =
                color,

            topLeft =
                Offset(
                    w * .2f,
                    h * .2f
                ),

            size =
                Size(
                    bar,
                    h * .6f
                ),

            cornerRadius =
                CornerRadius(
                    bar / 2f
                )
        )

        val path =
            Path().apply {
                moveTo(
                    w * .73f,
                    h * .17f
                )

                lineTo(
                    w * .32f,
                    h * .5f
                )

                lineTo(
                    w * .73f,
                    h * .83f
                )

                close()
            }

        drawPath(
            path =
                path,

            color =
                color
        )
    }
}

@Composable
private fun NextIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .1f

        val path =
            Path().apply {
                moveTo(
                    w * .27f,
                    h * .17f
                )

                lineTo(
                    w * .68f,
                    h * .5f
                )

                lineTo(
                    w * .27f,
                    h * .83f
                )

                close()
            }

        drawPath(
            path =
                path,

            color =
                color
        )

        drawRoundRect(
            color =
                color,

            topLeft =
                Offset(
                    w * .7f,
                    h * .2f
                ),

            size =
                Size(
                    bar,
                    h * .6f
                ),

            cornerRadius =
                CornerRadius(
                    bar / 2f
                )
        )
    }
}

@Composable
private fun PauseIcon(
    color: Color,
    modifier: Modifier =
        Modifier
) {
    Canvas(
        modifier
    ) {
        val width =
            size.width *
                .18f

        val height =
            size.height *
                .62f

        val top =
            (
                size.height -
                    height
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
                    width,
                    height
                ),

            cornerRadius =
                CornerRadius(
                    width *
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
                    width,
                    height
                ),

            cornerRadius =
                CornerRadius(
                    width *
                        .22f
                )
        )
    }
}

private fun formatPlayerTime(
    milliseconds: Long
): String {
    val totalSeconds =
        milliseconds
            .coerceAtLeast(
                0L
            ) /
            1000L

    val minutes =
        totalSeconds /
            60L

    val seconds =
        totalSeconds %
            60L

    return "$minutes:${
        seconds
            .toString()
            .padStart(
                2,
                '0'
            )
    }"
}
