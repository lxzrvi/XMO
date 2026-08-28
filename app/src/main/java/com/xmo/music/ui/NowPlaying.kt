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
import androidx.compose.ui.draw.drawBehind
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
import androidx.core.graphics.ColorUtils
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

    var height by remember {
        mutableFloatStateOf(1f)
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    /*
     * Real queue index.
     */
    val index =
        state.currentIndex
            .takeIf {
                it in queue.indices
            }
            ?: queue.indexOfFirst {
                it.id ==
                    state.currentSongId
            }

    val currentSong =
        queue.getOrNull(index)

    val previousSong =
        queue.getOrNull(
            index - 1
        )

    val nextSong =
        queue.getOrNull(
            index + 1
        )

    /*
     * =========================================================
     * DOMINANT COLOR
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
        val uri =
            currentSong?.artwork
                ?: state.artworkUri
                    ?.let(
                        Uri::parse
                    )

        dominant =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context,
                    uri
                )
    }

    val animatedDominant by
        animateColorAsState(
            targetValue =
                dominant,

            animationSpec =
                tween(420),

            label =
                "dominant"
        )

    /*
     * Slight red merge gives XMO identity while remaining
     * artwork-driven.
     */
    val hotColor =
        Color(
            ColorUtils.blendARGB(
                animatedDominant.toArgb(),
                XmoRed.toArgb(),
                .10f
            )
        )

    val deepColor =
        Artwork.end(
            animatedDominant,
            theme
        )

    /*
     * Real playback position.
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

    suspend fun closeWithButton() {
        visible = false
        delay(370)
        dismiss()
    }

    BackHandler(visible) {
        scope.launch {
            closeWithButton()
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
                    height
                )
                .coerceIn(
                    0f,
                    1f
                )

        val radius =
            30.dp *
                closeProgress

        /*
         * Fully opaque base.
         *
         * Gradient layers go ABOVE this.
         * No transparent player wallpaper.
         */
        Box(
            Modifier
                .fillMaxSize()
                .onSizeChanged {
                    height =
                        it.height
                            .toFloat()
                            .coerceAtLeast(1f)
                }
                .graphicsLayer {
                    translationY =
                        sheetY.value
                }
                .clip(
                    RoundedCornerShape(
                        topStart = radius,
                        topEnd = radius
                    )
                )
                .background(c.bg)
        ) {
            /*
             * Main deep vertical colour field.
             */
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                animatedDominant,
                                hotColor,
                                deepColor,
                                c.bg
                            )
                        )
                    )
            )

            /*
             * Scattered/distorted colour fields.
             *
             * Native gradients; no blur / Haze.
             */
            Canvas(
                Modifier.fillMaxSize()
            ) {
                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    hotColor.copy(
                                        alpha = .72f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    size.width * .12f,
                                    size.height * .24f
                                ),
                            radius =
                                size.width * .88f
                        )
                )

                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    animatedDominant
                                        .copy(
                                            alpha = .55f
                                        ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    size.width * .92f,
                                    size.height * .48f
                                ),
                            radius =
                                size.width * .92f
                        )
                )

                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    deepColor
                                        .copy(
                                            alpha = .80f
                                        ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    size.width * .24f,
                                    size.height * .78f
                                ),
                            radius =
                                size.width
                        )
                )
            }

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
                 * HEADER / DISMISS DRAG
                 * =================================================
                 */
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(62.dp)
                        .pointerInput(height) {
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
                                                    height
                                                )
                                        )
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        if (
                                            sheetY.value >
                                            height * .18f
                                        ) {
                                            /*
                                             * Finger-follow close.
                                             * Only after it is fully below
                                             * display is MiniPlayer allowed.
                                             */
                                            sheetY.animateTo(
                                                height,
                                                tween(250)
                                            )

                                            dismiss()
                                        } else {
                                            sheetY.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio = .82f,
                                                    stiffness = 400f
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
                                closeWithButton()
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
                            tint = c.text
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
                            if (sourceIsCategory)
                                "PLAYING FROM CATEGORY"
                            else
                                "PLAYING FROM",
                            color = c.sub,
                            fontFamily =
                                XmoFont.medium,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            maxLines = 1
                        )

                        Text(
                            source,
                            color = c.text,
                            fontFamily =
                                XmoFont.bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
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
                            Icons.Default.MoreVert,
                            null,
                            tint = c.icon
                        )
                    }
                }

                /*
                 * Cover lower.
                 */
                Spacer(
                    Modifier.height(
                        64.dp
                    )
                )

                ArtworkCarousel(
                    currentId =
                        state.currentSongId,

                    current =
                        currentSong?.artwork
                            ?: state.artworkUri
                                ?.let(
                                    Uri::parse
                                ),

                    previous =
                        previousSong?.artwork,

                    next =
                        nextSong?.artwork,

                    canPrevious =
                        previousSong != null,

                    canNext =
                        nextSong != null,

                    border =
                        c.border,

                    surface =
                        c.surface,

                    previousSong =
                        previous,

                    nextSong =
                        next
                )

                /*
                 * More space between cover and connected box.
                 */
                Spacer(
                    Modifier.height(
                        36.dp
                    )
                )

                /*
                 * =================================================
                 * CONNECTED PLAYER PANEL
                 * =================================================
                 *
                 * Top corners + left/right sides.
                 * No bottom border.
                 */
                val panelShape =
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(panelShape)
                        .background(
                            c.surface.copy(
                                alpha = .94f
                            )
                        )
                        .drawBehind {
                            val stroke =
                                .7.dp.toPx()

                            val corner =
                                28.dp.toPx()

                            /*
                             * LEFT
                             */
                            drawLine(
                                color =
                                    c.border,

                                start =
                                    Offset(
                                        stroke / 2f,
                                        corner
                                    ),

                                end =
                                    Offset(
                                        stroke / 2f,
                                        size.height
                                    ),

                                strokeWidth =
                                    stroke
                            )

                            /*
                             * RIGHT
                             */
                            drawLine(
                                color =
                                    c.border,

                                start =
                                    Offset(
                                        size.width -
                                            stroke / 2f,
                                        corner
                                    ),

                                end =
                                    Offset(
                                        size.width -
                                            stroke / 2f,
                                        size.height
                                    ),

                                strokeWidth =
                                    stroke
                            )

                            /*
                             * Rounded top path.
                             */
                            val path =
                                Path().apply {
                                    moveTo(
                                        0f,
                                        corner
                                    )

                                    quadraticTo(
                                        0f,
                                        0f,
                                        corner,
                                        0f
                                    )

                                    lineTo(
                                        size.width -
                                            corner,
                                        0f
                                    )

                                    quadraticTo(
                                        size.width,
                                        0f,
                                        size.width,
                                        corner
                                    )
                                }

                            drawPath(
                                path = path,
                                color = c.border,
                                style =
                                    androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = stroke
                                    )
                            )
                        }
                        .padding(
                            horizontal = 20.dp,
                            vertical = 24.dp
                        )
                ) {
                    Text(
                        state.title.ifBlank {
                            "Unknown song"
                        },
                        color = c.text,
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
                        color = c.sub,
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Spacer(
                        Modifier.height(
                            24.dp
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
                                    duration.toDouble() *
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
                        Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            playerTime(
                                state.position
                            ),
                            color = c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 10.sp
                        )

                        Text(
                            playerTime(
                                state.duration
                            ),
                            color = c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(
                        Modifier.height(
                            20.dp
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
                                state.hasPrevious
                        ) {
                            PreviousIcon(
                                color =
                                    if (
                                        state.hasPrevious
                                    )
                                        c.text
                                    else
                                        c.sub,
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
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        XmoRed
                                    )
                        ) {
                            if (state.isPlaying) {
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
                                color =
                                    if (
                                        state.hasNext
                                    )
                                        c.text
                                    else
                                        c.sub,
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
                            Modifier.fillMaxWidth()
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
                            color = c.text,
                            fontFamily =
                                XmoFont.logo,
                            fontSize = 18.sp
                        )

                        Text(
                            "lxzrvi  •  copyright © 2026",
                            color = c.sub,
                            fontFamily =
                                XmoFont.thin,
                            fontSize = 9.sp
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
 * ARTWORK CAROUSEL
 * =============================================================
 */

@Composable
private fun ArtworkCarousel(
    currentId: Long?,
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    canPrevious: Boolean,
    canNext: Boolean,
    border: Color,
    surface: Color,
    previousSong: () -> Unit,
    nextSong: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val drag =
        remember {
            Animatable(0f)
        }

    /*
     * Waiting for REAL MediaController item change.
     *
     * -1 = previous
     * +1 = next
     * 0 = idle
     */
    var pendingDirection by remember {
        mutableIntStateOf(0)
    }

    var oldId by remember {
        mutableStateOf<Long?>(
            null
        )
    }

    /*
     * Reset only when Media3 confirms song changed.
     */
    LaunchedEffect(
        currentId
    ) {
        if (
            pendingDirection != 0 &&
            oldId != null &&
            currentId != oldId
        ) {
            drag.snapTo(0f)
            pendingDirection = 0
            oldId = null
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val width =
            constraints.maxWidth
                .toFloat()

        val maxDrag =
            width * .34f

        fun resisted(
            value: Float
        ): Float {
            val d =
                abs(value)

            if (d <= maxDrag) {
                return value
            }

            return if (
                value < 0f
            ) {
                -(
                    maxDrag +
                        (d - maxDrag) *
                        .08f
                    )
            } else {
                maxDrag +
                    (d - maxDrag) *
                    .08f
            }
        }

        Box(
            Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .pointerInput(
                    currentId,
                    canPrevious,
                    canNext
                ) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = {
                                change,
                                amount ->

                            if (
                                pendingDirection != 0
                            ) {
                                return@detectHorizontalDragGestures
                            }

                            change.consume()

                            val target =
                                drag.value +
                                    amount

                            scope.launch {
                                drag.snapTo(
                                    when {
                                        target < 0f &&
                                            !canNext ->
                                            resisted(
                                                target *
                                                    .3f
                                            )

                                        target > 0f &&
                                            !canPrevious ->
                                            resisted(
                                                target *
                                                    .3f
                                            )

                                        else ->
                                            resisted(
                                                target
                                            )
                                    }
                                )
                            }
                        },

                        onDragEnd = {
                            if (
                                pendingDirection != 0
                            ) {
                                return@detectHorizontalDragGestures
                            }

                            scope.launch {
                                /*
                                 * LEFT -> NEXT
                                 */
                                if (
                                    drag.value <
                                    -width * .18f &&
                                    canNext
                                ) {
                                    pendingDirection = 1
                                    oldId = currentId

                                    drag.animateTo(
                                        -width,
                                        tween(260)
                                    )

                                    nextSong()

                                /*
                                 * RIGHT -> PREVIOUS
                                 */
                                } else if (
                                    drag.value >
                                    width * .18f &&
                                    canPrevious
                                ) {
                                    pendingDirection = -1
                                    oldId = currentId

                                    drag.animateTo(
                                        width,
                                        tween(260)
                                    )

                                    previousSong()
                                } else {
                                    drag.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = .8f,
                                            stiffness = 430f
                                        )
                                    )
                                }
                            }
                        },

                        onDragCancel = {
                            scope.launch {
                                if (
                                    pendingDirection == 0
                                ) {
                                    drag.animateTo(
                                        0f
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            if (
                previous != null
            ) {
                PlayerCover(
                    previous,
                    border,
                    surface,
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                drag.value -
                                    width
                        }
                )
            }

            PlayerCover(
                current,
                border,
                surface,
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
                                p * .045f

                        scaleY =
                            scaleX
                    }
            )

            if (
                next != null
            ) {
                PlayerCover(
                    next,
                    border,
                    surface,
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                drag.value +
                                    width
                        }
                )
            }
        }
    }
}

@Composable
private fun PlayerCover(
    uri: Uri?,
    border: Color,
    surface: Color,
    modifier: Modifier
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
            model = uri,
            contentDescription =
                null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )

        if (uri == null) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    "XMO",
                    color = XmoRed,
                    fontFamily =
                        XmoFont.logo,
                    fontSize = 32.sp
                )
            }
        }
    }
}

/*
 * =============================================================
 * LIGHTWEIGHT PLAYER ICONS
 * =============================================================
 */

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
            color = color,
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

        val p =
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
            p,
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

        val p =
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
            p,
            color
        )

        drawRoundRect(
            color = color,
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
                ) / 2f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width *
                        .27f,
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
                    size.width *
                        .55f,
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

private fun playerTime(
    ms: Long
): String {
    val total =
        ms.coerceAtLeast(
            0L
        ) / 1000L

    return "${
        total / 60L
    }:${
        (total % 60L)
            .toString()
            .padStart(
                2,
                '0'
            )
    }"
}
