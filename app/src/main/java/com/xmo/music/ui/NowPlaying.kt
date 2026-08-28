package com.xmo.music.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
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
    queue: List<Song>,
    refreshPosition: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,
    dismiss: () -> Unit
) {
    val c =
        homeColors(theme)

    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    var visible by remember {
        mutableStateOf(false)
    }

    val sheetY =
        remember {
            Animatable(0f)
        }

    var screenHeight by remember {
        mutableFloatStateOf(1f)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    /*
     * Current Song object from real playback queue.
     */
    val queueIndex =
        state.currentIndex
            .takeIf {
                it in queue.indices
            }
            ?: queue.indexOfFirst {
                it.id ==
                    state.currentSongId
            }

    val currentSong =
        queue.getOrNull(
            queueIndex
        )

    val previousSong =
        queue.getOrNull(
            queueIndex - 1
        )

    val nextSong =
        queue.getOrNull(
            queueIndex + 1
        )

    /*
     * =========================================================
     * ARTWORK DOMINANT BACKGROUND
     * =========================================================
     */
    var dominant by remember {
        mutableStateOf(
            Color(0xFF35353A)
        )
    }

    LaunchedEffect(
        currentSong?.artwork,
        state.artworkUri
    ) {
        dominant =
            if (
                currentSong !=
                null
            ) {
                Artwork.cached(
                    currentSong.artwork
                )
                    ?: Artwork.color(
                        context,
                        currentSong.artwork
                    )
            } else {
                state.artworkUri
                    ?.let(
                        Uri::parse
                    )
                    ?.let {
                        Artwork.cached(it)
                            ?: Artwork.color(
                                context,
                                it
                            )
                    }
                    ?: Color(
                        0xFF35353A
                    )
            }
    }

    val animatedDominant by
        animateColorAsState(
            targetValue =
                dominant,
            animationSpec =
                tween(
                    durationMillis =
                        430
                ),
            label =
                "playerBackground"
        )

    val gradientEnd =
        Artwork.end(
            animatedDominant,
            theme
        )

    val backgroundBrush =
        Brush.verticalGradient(
            colors =
                when (theme) {
                    XmoTheme.Light ->
                        listOf(
                            animatedDominant
                                .copy(
                                    alpha =
                                        .35f
                                ),
                            gradientEnd,
                            c.bg
                        )

                    XmoTheme.Dark ->
                        listOf(
                            animatedDominant
                                .copy(
                                    alpha =
                                        .50f
                                ),
                            gradientEnd,
                            c.bg
                        )

                    XmoTheme.Amoled ->
                        listOf(
                            animatedDominant
                                .copy(
                                    alpha =
                                        .38f
                                ),
                            gradientEnd,
                            Color.Black
                        )
                }
        )

    /*
     * Real playback progress polling.
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

    suspend fun closePlayer() {
        visible = false

        delay(370)

        dismiss()
    }

    BackHandler(
        enabled = visible
    ) {
        scope.launch {
            closePlayer()
        }
    }

    AnimatedVisibility(
        visible =
            visible,

        enter =
            slideInVertically(
                initialOffsetY = {
                    it
                },
                animationSpec =
                    tween(420)
            ),

        exit =
            slideOutVertically(
                targetOffsetY = {
                    it
                },
                animationSpec =
                    tween(360)
            ),

        modifier =
            Modifier.fillMaxSize()
    ) {
        val closeProgress =
            (
                sheetY.value /
                    screenHeight
                )
                .coerceIn(
                    0f,
                    1f
                )

        val radius =
            30.dp *
                closeProgress

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
                        sheetY.value
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
                    backgroundBrush
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
                 * HEADER / CLOSE DRAG
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

                                    scope.launch {
                                        sheetY.snapTo(
                                            (
                                                sheetY.value +
                                                    amount
                                                )
                                                .coerceIn(
                                                    0f,
                                                    screenHeight
                                                )
                                        )
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        if (
                                            sheetY.value >
                                            screenHeight *
                                                .18f
                                        ) {
                                            /*
                                             * Full finger-follow exit.
                                             *
                                             * MiniPlayer doesn't appear
                                             * until dismiss() afterwards.
                                             */
                                            sheetY.animateTo(
                                                screenHeight,
                                                tween(250)
                                            )

                                            dismiss()
                                        } else {
                                            sheetY.animateTo(
                                                0f,
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
                                        sheetY.animateTo(
                                            0f
                                        )
                                    }
                                }
                            )
                        },
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                closePlayer()
                            }
                        },
                        modifier =
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
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
                            Icons.Default
                                .KeyboardArrowDown,
                            "Close",
                            tint =
                                c.text
                        )
                    }

                    Column(
                        Modifier
                            .weight(1f)
                            .padding(
                                horizontal =
                                    8.dp
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
                                1.sp
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

                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
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
                            Icons.Default
                                .MoreVert,
                            null,
                            tint =
                                c.icon
                        )
                    }
                }

                /*
                 * Cover intentionally lower than previous version.
                 */
                Spacer(
                    Modifier.height(
                        42.dp
                    )
                )

                /*
                 * =================================================
                 * ARTWORK CAROUSEL
                 * =================================================
                 */
                ArtworkCarousel(
                    current =
                        currentSong
                            ?.artwork
                            ?: state.artworkUri
                                ?.let(
                                    Uri::parse
                                ),

                    previous =
                        previousSong
                            ?.artwork,

                    next =
                        nextSong
                            ?.artwork,

                    border =
                        c.border,

                    surface =
                        c.surface,

                    canPrevious =
                        previousSong !=
                            null,

                    canNext =
                        nextSong !=
                            null,

                    previousSong = {
                        previous()
                    },

                    nextSong = {
                        next()
                    }
                )

                Spacer(
                    Modifier.height(
                        28.dp
                    )
                )

                /*
                 * =================================================
                 * PLAYER PANEL
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
                            c.surface.copy(
                                alpha =
                                    .93f
                            )
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

                    val duration =
                        state.duration
                            .coerceAtLeast(
                                1L
                            )

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
                                    duration
                                        .toDouble() *
                                        it
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
                            playerTime(
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
                            playerTime(
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
                                state.hasPrevious
                        ) {
                            PreviousIcon(
                                if (
                                    state.hasPrevious
                                )
                                    c.text
                                else
                                    c.sub,
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
                                    .size(60.dp)
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
                                PlayerPauseIcon(
                                    Color.White,
                                    Modifier.size(
                                        30.dp
                                    )
                                )
                            } else {
                                Icon(
                                    Icons.Default
                                        .PlayArrow,
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
                                if (
                                    state.hasNext
                                )
                                    c.text
                                else
                                    c.sub,
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
                                .fillMaxWidth()
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
                                        .weight(1f)
                                        .padding(
                                            start =
                                                18.dp
                                        )
                            )
                        }
                    }

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
                            .height(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtworkCarousel(
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    border: Color,
    surface: Color,
    canPrevious: Boolean,
    canNext: Boolean,
    previousSong: () -> Unit,
    nextSong: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val drag =
        remember {
            Animatable(0f)
        }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val width =
            constraints
                .maxWidth
                .toFloat()

        val gap =
            16.dp.value *
                androidx.compose.ui.platform
                    .LocalDensity.current
                    .density

        val step =
            width +
                gap

        /*
         * Artwork drag layer.
         */
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(
                    current,
                    canPrevious,
                    canNext
                ) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = {
                                change,
                                amount ->

                            change.consume()

                            scope.launch {
                                val target =
                                    drag.value +
                                        amount

                                drag.snapTo(
                                    when {
                                        target > 0f &&
                                            !canPrevious ->
                                            target * .25f

                                        target < 0f &&
                                            !canNext ->
                                            target * .25f

                                        else ->
                                            target
                                    }
                                )
                            }
                        },

                        onDragEnd = {
                            scope.launch {
                                /*
                                 * LEFT -> NEXT
                                 */
                                if (
                                    drag.value <
                                    -width *
                                        .20f &&
                                    canNext
                                ) {
                                    drag.animateTo(
                                        -step,
                                        tween(280)
                                    )

                                    nextSong()

                                    delay(90)

                                    drag.snapTo(
                                        0f
                                    )

                                /*
                                 * RIGHT -> PREVIOUS
                                 */
                                } else if (
                                    drag.value >
                                    width *
                                        .20f &&
                                    canPrevious
                                ) {
                                    drag.animateTo(
                                        step,
                                        tween(280)
                                    )

                                    previousSong()

                                    delay(90)

                                    drag.snapTo(
                                        0f
                                    )

                                } else {
                                    drag.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio =
                                                .8f,
                                            stiffness =
                                                420f
                                        )
                                    )
                                }
                            }
                        },

                        onDragCancel = {
                            scope.launch {
                                drag.animateTo(
                                    0f
                                )
                            }
                        }
                    )
                }
        ) {
            /*
             * Previous card
             */
            if (
                previous !=
                null
            ) {
                PlayerArtwork(
                    uri =
                        previous,
                    border =
                        border,
                    surface =
                        surface,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX =
                                    drag.value -
                                        step
                            }
                )
            }

            /*
             * Current
             */
            PlayerArtwork(
                uri =
                    current,
                border =
                    border,
                surface =
                    surface,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                drag.value

                            val p =
                                (
                                    abs(
                                        drag.value
                                    ) /
                                        width
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )

                            scaleX =
                                1f -
                                    p *
                                    .06f

                            scaleY =
                                scaleX
                        }
            )

            /*
             * Next
             */
            if (
                next !=
                null
            ) {
                PlayerArtwork(
                    uri =
                        next,
                    border =
                        border,
                    surface =
                        surface,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationX =
                                    drag.value +
                                        step
                            }
                )
            }
        }
    }
}

@Composable
private fun PlayerArtwork(
    uri: Uri?,
    border: Color,
    surface: Color,
    modifier: Modifier =
        Modifier
) {
    Box(
        modifier
            .clip(
                RoundedCornerShape(
                    24.dp
                )
            )
            .background(
                surface
            )
            .border(
                .7.dp,
                border,
                RoundedCornerShape(
                    24.dp
                )
            )
    ) {
        AsyncImage(
            model =
                uri,
            contentDescription =
                null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )

        if (
            uri == null
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
                        XmoRed,
                    fontFamily =
                        XmoFont.logo,
                    fontSize =
                        32.sp
                )
            }
        }
    }
}

@Composable
private fun PreviousIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bar = w * .1f

        drawRoundRect(
            color,
            Offset(
                w * .2f,
                h * .2f
            ),
            Size(
                bar,
                h * .6f
            ),
            CornerRadius(
                bar / 2
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
            path,
            color
        )
    }
}

@Composable
private fun NextIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bar = w * .1f

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
            path,
            color
        )

        drawRoundRect(
            color,
            Offset(
                w * .7f,
                h * .2f
            ),
            Size(
                bar,
                h * .6f
            ),
            CornerRadius(
                bar / 2
            )
        )
    }
}

@Composable
private fun PlayerPauseIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val bw =
            size.width *
                .18f

        val bh =
            size.height *
                .62f

        val top =
            (
                size.height -
                    bh
                ) / 2

        drawRoundRect(
            color,
            Offset(
                size.width *
                    .27f,
                top
            ),
            Size(
                bw,
                bh
            )
        )

        drawRoundRect(
            color,
            Offset(
                size.width *
                    .55f,
                top
            ),
            Size(
                bw,
                bh
            )
        )
    }
}

private fun playerTime(
    ms: Long
): String {
    val total =
        ms.coerceAtLeast(
            0
        ) / 1000

    val min =
        total / 60

    val sec =
        total % 60

    return "$min:${
        sec.toString()
            .padStart(
                2,
                '0'
            )
    }"
}
