package com.xmo.music.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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

    /*
     * AnimatedVisibility controls only full entrance/exit.
     */
    var visible by remember {
        mutableStateOf(false)
    }

    /*
     * Finger-follow offset while already open.
     */
    val dragY =
        remember {
            Animatable(0f)
        }

    var screenHeight by remember {
        mutableFloatStateOf(1f)
    }

    /*
     * Must happen after initial composition.
     *
     * false -> true is what triggers slideInVertically.
     */
    LaunchedEffect(Unit) {
        visible = true
    }

    /*
     * Real MediaController position polling.
     */
    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (true) {
            refreshPosition()

            delay(
                if (state.isPlaying) {
                    250L
                } else {
                    500L
                }
            )
        }
    }

    /*
     * Full animated dismissal.
     *
     * First return drag sheet to zero when needed, then let
     * AnimatedVisibility own the final bottom exit.
     */
    suspend fun close() {
        visible = false

        /*
         * Keep composable alive while slideOut runs.
         */
        delay(370L)

        dismiss()
    }

    BackHandler(
        enabled = visible
    ) {
        scope.launch {
            close()
        }
    }

    AnimatedVisibility(
        visible = visible,

        enter =
            slideInVertically(
                initialOffsetY = {
                    it
                },
                animationSpec =
                    tween(
                        durationMillis = 420
                    )
            ),

        exit =
            slideOutVertically(
                targetOffsetY = {
                    it
                },
                animationSpec =
                    tween(
                        durationMillis = 360
                    )
            ),

        modifier =
            Modifier.fillMaxSize()
    ) {
        /*
         * Drag progress drives corner rounding.
         */
        val dragProgress =
            (
                dragY.value /
                    screenHeight
                )
                .coerceIn(
                    0f,
                    1f
                )

        val radius =
            30.dp *
                dragProgress

        Box(
            Modifier
                .fillMaxSize()
                .onSizeChanged {
                    screenHeight =
                        it.height
                            .toFloat()
                            .coerceAtLeast(
                                1f
                            )
                }
                .graphicsLayer {
                    translationY =
                        dragY.value
                }
                .clip(
                    RoundedCornerShape(
                        topStart =
                            radius,
                        topEnd =
                            radius
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
                        horizontal = 14.dp
                    )
            ) {
                /*
                 * =================================================
                 * HEADER + PLAYER DRAG HANDLE AREA
                 * =================================================
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
                                     * Only downward displacement is
                                     * allowed from the open position.
                                     */
                                    val target =
                                        (
                                            dragY.value +
                                                amount
                                            )
                                            .coerceIn(
                                                0f,
                                                screenHeight
                                            )

                                    scope.launch {
                                        dragY.snapTo(
                                            target
                                        )
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        if (
                                            dragY.value >
                                            screenHeight *
                                                .18f
                                        ) {
                                            /*
                                             * Finish remaining finger
                                             * displacement first.
                                             *
                                             * This gives a continuous
                                             * gesture rather than jumping
                                             * back to top before exit.
                                             */
                                            dragY.animateTo(
                                                screenHeight,
                                                animationSpec =
                                                    tween(
                                                        220
                                                    )
                                            )

                                            dismiss()
                                        } else {
                                            dragY.animateTo(
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
                                        dragY.animateTo(
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
                     * DOWN BUTTON
                     */
                    IconButton(
                        onClick = {
                            scope.launch {
                                close()
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
                                horizontal = 8.dp
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
                     * MENU VISUAL.
                     *
                     * Functional menu is added only when
                     * real actions exist.
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
                                        alpha = .72f
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
                 * CONNECTED PLAYER SURFACE
                 * =================================================
                 */
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 28.dp,
                                topEnd = 28.dp,
                                bottomStart = 10.dp,
                                bottomEnd = 10.dp
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
                                bottomStart = 10.dp,
                                bottomEnd = 10.dp
                            )
                        )
                        .padding(
                            horizontal = 20.dp,
                            vertical = 24.dp
                        )
                ) {
                    /*
                     * SONG
                     */
                    Text(
                        text =
                            state.title.ifBlank {
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
                            state.artist.ifBlank {
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
                                value ->

                            seekTo(
                                (
                                    duration
                                        .toDouble() *
                                        value
                                    )
                                    .toLong()
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
                     * PREVIOUS / PLAY / NEXT
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
                                            start = 18.dp
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
}

/*
 * =============================================================
 * CUSTOM LIGHTWEIGHT ICONS
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

        val path =
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
            path =
                path,

            color =
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

        val path =
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
