package com.xmo.music.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
    val colors =
        homeColors(theme)

    val scope =
        rememberCoroutineScope()

    val density =
        LocalDensity.current

    var heightPx by remember {
        mutableFloatStateOf(1f)
    }

    val offsetY =
        remember {
            Animatable(1f)
        }

    var opened by remember {
        mutableStateOf(false)
    }

    /*
     * Initial bottom -> top reveal.
     */
    LaunchedEffect(heightPx) {
        if (
            heightPx > 1f &&
            !opened
        ) {
            opened = true

            offsetY.snapTo(
                heightPx
            )

            offsetY.animateTo(
                0f,
                spring(
                    dampingRatio = .88f,
                    stiffness = 320f
                )
            )
        }
    }

    /*
     * Real MediaController position polling.
     * No synthetic playback timer.
     */
    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (true) {
            refreshPosition()

            delay(
                if (state.isPlaying)
                    250
                else
                    500
            )
        }
    }

    suspend fun close() {
        offsetY.animateTo(
            heightPx,
            spring(
                dampingRatio = 1f,
                stiffness = 280f
            )
        )

        dismiss()
    }

    BackHandler {
        scope.launch {
            close()
        }
    }

    /*
     * More downward displacement =
     * more rounded top corners.
     */
    val progress =
        (
            offsetY.value /
                heightPx
            )
            .coerceIn(
                0f,
                1f
            )

    val radius =
        30.dp * progress

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                heightPx =
                    it.height
                        .toFloat()
                        .coerceAtLeast(1f)
            }
            .graphicsLayer {
                translationY =
                    offsetY.value
            }
            .clip(
                RoundedCornerShape(
                    topStart = radius,
                    topEnd = radius
                )
            )
            .background(
                colors.bg
            )
            /*
             * Full-screen downward gesture.
             *
             * Horizontal artwork gestures can be added later.
             */
            .pointerInput(heightPx) {
                detectVerticalDragGestures(
                    onVerticalDrag = {
                            change,
                            dragAmount ->

                        change.consume()

                        if (
                            dragAmount != 0f
                        ) {
                            scope.launch {
                                offsetY.snapTo(
                                    (
                                        offsetY.value +
                                            dragAmount
                                        )
                                        .coerceIn(
                                            0f,
                                            heightPx
                                        )
                                )
                            }
                        }
                    },

                    onDragEnd = {
                        scope.launch {
                            if (
                                offsetY.value >
                                heightPx * .20f
                            ) {
                                close()
                            } else {
                                offsetY.animateTo(
                                    0f,
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
                                0f
                            )
                        }
                    }
                )
            }
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
                    horizontal = 16.dp
                )
        ) {
            /*
             * Header
             */
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            close()
                        }
                    },
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                colors.button
                            )
                ) {
                    Icon(
                        Icons.Default
                            .KeyboardArrowDown,
                        null,
                        tint = colors.text
                    )
                }

                Column(
                    Modifier.weight(1f),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        if (sourceIsCategory)
                            "PLAYING FROM CATEGORY"
                        else
                            "PLAYING FROM",
                        color =
                            colors.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )

                    Text(
                        source,
                        color = colors.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                /*
                 * Menu shell only.
                 * No fake popup options yet.
                 */
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            colors.button
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        null,
                        tint = colors.icon
                    )
                }
            }

            Spacer(
                Modifier.height(
                    24.dp
                )
            )

            /*
             * Artwork
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
                        colors.surface
                    )
                    .border(
                        .7.dp,
                        colors.border,
                        RoundedCornerShape(
                            24.dp
                        )
                    )
            ) {
                AsyncImage(
                    model =
                        state.artworkUri
                            ?.let(Uri::parse),
                    contentDescription =
                        null,
                    modifier =
                        Modifier.fillMaxSize(),
                    contentScale =
                        ContentScale.Crop
                )
            }

            Spacer(
                Modifier.height(
                    24.dp
                )
            )

            /*
             * Connected control surface
             */
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .background(
                        colors.surface
                    )
                    .border(
                        .7.dp,
                        colors.border,
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .padding(
                        20.dp
                    )
            ) {
                Text(
                    state.title.ifBlank {
                        "Unknown song"
                    },
                    color = colors.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 21.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    state.artist.ifBlank {
                        "Unknown artist"
                    },
                    color = colors.sub,
                    fontFamily =
                        XmoFont.normal,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Spacer(
                    Modifier.height(
                        25.dp
                    )
                )

                val duration =
                    state.duration
                        .coerceAtLeast(1L)

                Slider(
                    value =
                        (
                            state.position
                                .toFloat() /
                                duration
                                    .toFloat()
                            )
                            .coerceIn(
                                0f,
                                1f
                            ),

                    onValueChange = {
                        seekTo(
                            (
                                duration *
                                    it
                                )
                                .roundToInt()
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
                                colors.border
                        )
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        formatPlayerTime(
                            state.position
                        ),
                        color = colors.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize = 10.sp
                    )

                    Text(
                        formatPlayerTime(
                            state.duration
                        ),
                        color = colors.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize = 10.sp
                    )
                }

                Spacer(
                    Modifier.height(
                        18.dp
                    )
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick =
                            previous,
                        enabled =
                            state.currentSongId !=
                                null
                    ) {
                        Icon(
                            Icons.Default
                                .SkipPrevious,
                            null,
                            tint = colors.text,
                            modifier =
                                Modifier.size(
                                    30.dp
                                )
                        )
                    }

                    IconButton(
                        onClick =
                            togglePlay,
                        modifier =
                            Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(
                                    XmoRed
                                )
                    ) {
                        Icon(
                            if (state.isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,
                            null,
                            tint = Color.White,
                            modifier =
                                Modifier.size(
                                    30.dp
                                )
                        )
                    }

                    IconButton(
                        onClick =
                            next,
                        enabled =
                            state.hasNext
                    ) {
                        Icon(
                            Icons.Default
                                .SkipNext,
                            null,
                            tint =
                                if (
                                    state.hasNext
                                )
                                    colors.text
                                else
                                    colors.sub,
                            modifier =
                                Modifier.size(
                                    30.dp
                                )
                        )
                    }
                }

                if (
                    state.album
                        .isNotBlank()
                ) {
                    Spacer(
                        Modifier.height(
                            42.dp
                        )
                    )

                    Text(
                        "SONG DETAILS",
                        color = XmoRed,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )

                    Spacer(
                        Modifier.height(
                            10.dp
                        )
                    )

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Album",
                            color = colors.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 12.sp
                        )

                        Text(
                            state.album,
                            color = colors.text,
                            fontFamily =
                                XmoFont.medium,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis,
                            modifier =
                                Modifier
                                    .padding(
                                        start = 20.dp
                                    )
                                    .weight(1f),
                            textAlign =
                                androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                }

                Spacer(
                    Modifier.height(
                        80.dp
                    )
                )

                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "XMO",
                        color = colors.text,
                        fontFamily =
                            XmoFont.logo,
                        fontSize = 18.sp
                    )

                    Text(
                        "lxzrvi  •  copyright © 2026",
                        color = colors.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize = 9.sp
                    )
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(14.dp)
                )
            }
        }
    }
}

private fun formatPlayerTime(
    milliseconds: Long
): String {
    val totalSeconds =
        (
            milliseconds
                .coerceAtLeast(0L) /
                1000L
            )

    val minutes =
        totalSeconds /
            60L

    val seconds =
        totalSeconds %
            60L

    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
