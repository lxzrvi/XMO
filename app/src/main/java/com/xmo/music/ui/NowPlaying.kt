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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
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

    /*
     * Called after entrance finishes.
     * App then removes MiniPlayer from background.
     */
    onOpened: () -> Unit,

    refreshPosition: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,

    /*
     * Called only after player has fully exited.
     */
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

    /*
     * Separate drag displacement after initial entrance.
     */
    val sheetY =
        remember {
            Animatable(0f)
        }

    var screenHeight by remember {
        mutableFloatStateOf(1f)
    }

    /*
     * Initial bottom entrance.
     */
    LaunchedEffect(Unit) {
        visible = true

        /*
         * Match entrance duration before background MiniPlayer
         * is removed. By then sheet fully covers it.
         */
        delay(430L)

        onOpened()
    }

    /*
     * Current queue items.
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
     * ARTWORK DOMINANT COLOR
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
                    ?.let(Uri::parse)

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
                "nowPlayingDominant"
        )

    val hot =
        Color(
            ColorUtils.blendARGB(
                animatedDominant.toArgb(),
                XmoRed.toArgb(),
                .10f
            )
        )

    val deep =
        Artwork.end(
            animatedDominant,
            theme
        )

    /*
     * Real playback progress from MediaController.
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

    suspend fun buttonClose() {
        /*
         * MiniPlayer is still absent during this exit.
         */
        visible = false

        delay(370L)

        dismiss()
    }

    BackHandler(
        enabled = visible
    ) {
        scope.launch {
            buttonClose()
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

        val topRadius =
            30.dp *
                closeProgress

        /*
         * Gesture is applied at root so downward drag can begin
         * from almost anywhere.
         *
         * Artwork has its own horizontal detector and Slider owns
         * horizontal seek, but vertical drags can still reach root.
         */
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
                            topRadius,
                        topEnd =
                            topRadius
                    )
                )
                .background(c.bg)
                .pointerInput(
                    screenHeight
                ) {
                    detectVerticalDragGestures(
                        onVerticalDrag = {
                                change,
                                amount ->

                            /*
                             * Only downward sheet movement.
                             */
                            if (
                                amount > 0f ||
                                sheetY.value > 0f
                            ) {
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
                            }
                        },

                        onDragEnd = {
                            scope.launch {
                                if (
                                    sheetY.value >
                                    screenHeight * .14f
                                ) {
                                    /*
                                     * Finish completely below display.
                                     */
                                    sheetY.animateTo(
                                        screenHeight,
                                        tween(250)
                                    )

                                    /*
                                     * ONLY NOW App can show MiniPlayer.
                                     */
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
                                    0f,
                                    spring(
                                        dampingRatio = .84f,
                                        stiffness = 390f
                                    )
                                )
                            }
                        }
                    )
                }
        ) {
            /*
             * =================================================
             * FULL OPAQUE ARTWORK COLOR FIELD
             * =================================================
             */
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    animatedDominant,
                                    hot,
                                    deep,
                                    c.bg
                                )
                        )
                    )
            )

            /*
             * Scattered color areas.
             * No Haze / no fake backdrop blur.
             */
            Canvas(
                Modifier.fillMaxSize()
            ) {
                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    hot.copy(
                                        alpha = .76f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    size.width * .10f,
                                    size.height * .22f
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
                                    animatedDominant.copy(
                                        alpha = .60f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    size.width * .98f,
                                    size.height * .45f
                                ),
                            radius =
                                size.width * .95f
                        )
                )

                drawRect(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    deep.copy(
                                        alpha = .90f
                                    ),
                                    Color.Transparent
                                ),
                            center =
                                Offset(
                                    size.width * .22f,
                                    size.height * .82f
                                ),
                            radius =
                                size.width * 1.1f
                        )
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(
                        rememberScrollState()
                    )
                    .statusBarsPadding()
                    .padding(
                        horizontal =
                            14.dp
                    )
            ) {
                /*
                 * HEADER
                 */
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(62.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                buttonClose()
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
                            imageVector =
                                Icons.Default
                                    .KeyboardArrowDown,
                            contentDescription =
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
                            text =
                                if (sourceIsCategory)
                                    "PLAYING FROM CATEGORY"
                                else
                                    "PLAYING FROM",
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
                            imageVector =
                                Icons.Default
                                    .MoreVert,
                            contentDescription =
                                null,
                            tint =
                                c.icon
                        )
                    }
                }

                /*
                 * Cover lower than old layout.
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
                                ?.let(Uri::parse),

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
                 * Visible breathing room.
                 */
                Spacer(
                    Modifier.height(
                        36.dp
                    )
                )

                /*
                 * =================================================
                 * TRANSLUCENT PLAYER PANEL
                 * =================================================
                 *
                 * This is translucent, NOT fake blur.
                 * Artwork color field remains visible through it.
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
                            when (theme) {
                                XmoTheme.Dark ->
                                    Color(0xB8141518)

                                XmoTheme.Light ->
                                    Color(0xD9FFFFFF)

                                XmoTheme.Amoled ->
                                    Color(0xC9000000)
                            }
                        )

                        /*
                         * Top + side border.
                         * No bottom border.
                         */
                        .drawBehind {
                            val stroke =
                                .7.dp.toPx()

                            val corner =
                                28.dp.toPx()

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

                            val topPath =
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
                                path =
                                    topPath,
                                color =
                                    c.border,
                                style =
                                    Stroke(
                                        width =
                                            stroke,
                                        cap =
                                            StrokeCap.Round
                                    )
                            )
                        }
                        .padding(
                            horizontal =
                                20.dp,
                            vertical =
                                24.dp
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

                    Spacer(
                        Modifier.height(
                            3.dp
                        )
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
                            .coerceAtLeast(
                                1L
                            )

                    Slider(
                        value =
                            (
                                state.position
                                    .toFloat() /
                                    duration.toFloat()
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
                            PreviousPlayerIcon(
                                color =
                                    if (state.hasPrevious)
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
                            enabled =
                                state.currentSongId !=
                                    null,
                            modifier =
                                Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        XmoRed
                                    )
                        ) {
                            if (state.isPlaying) {
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
                                    if (state.hasNext)
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
                        state.album.isNotBlank()
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
                                    TextOverflow.Ellipsis,
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
                        Modifier.fillMaxWidth(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally
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
                            "lxzrvi • copyright © 2026",
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

    var pending by remember {
        mutableIntStateOf(0)
    }

    var oldId by remember {
        mutableStateOf<Long?>(
            null
        )
    }

    /*
     * Real Media3 item switch complete hua tab reset.
     */
    LaunchedEffect(
        currentId
    ) {
        if (
            pending != 0 &&
            oldId != null &&
            currentId != oldId
        ) {
            drag.snapTo(0f)
            pending = 0
            oldId = null
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(
                RoundedCornerShape(
                    24.dp
                )
            )
    ) {
        val width =
            constraints.maxWidth
                .toFloat()

        val resistanceStart =
            width * .32f

        fun resisted(
            raw: Float
        ): Float {
            val d =
                abs(raw)

            if (
                d <= resistanceStart
            ) {
                return raw
            }

            val value =
                resistanceStart +
                    (
                        d -
                            resistanceStart
                        ) * .08f

            return if (raw < 0f)
                -value
            else
                value
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(
                    currentId,
                    canPrevious,
                    canNext
                ) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = {
                                change,
                                amount ->

                            if (pending != 0) {
                                return@detectHorizontalDragGestures
                            }

                            change.consume()

                            val raw =
                                drag.value +
                                    amount

                            scope.launch {
                                drag.snapTo(
                                    when {
                                        raw < 0f &&
                                            !canNext ->
                                            resisted(
                                                raw * .28f
                                            )

                                        raw > 0f &&
                                            !canPrevious ->
                                            resisted(
                                                raw * .28f
                                            )

                                        else ->
                                            resisted(raw)
                                    }
                                )
                            }
                        },

                        onDragEnd = {
                            if (pending != 0) {
                                return@detectHorizontalDragGestures
                            }

                            scope.launch {
                                /*
                                 * LEFT -> NEXT
                                 */
                                if (
                                    drag.value <
                                    -width * .16f &&
                                    canNext
                                ) {
                                    oldId =
                                        currentId

                                    pending = 1

                                    drag.animateTo(
                                        -width,
                                        tween(250)
                                    )

                                    nextSong()

                                /*
                                 * RIGHT -> PREVIOUS
                                 */
                                } else if (
                                    drag.value >
                                    width * .16f &&
                                    canPrevious
                                ) {
                                    oldId =
                                        currentId

                                    pending = -1

                                    drag.animateTo(
                                        width,
                                        tween(250)
                                    )

                                    previousSong()
                                } else {
                                    drag.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio =
                                                .80f,
                                            stiffness =
                                                430f
                                        )
                                    )
                                }
                            }
                        },

                        onDragCancel = {
                            scope.launch {
                                if (pending == 0) {
                                    drag.animateTo(
                                        0f
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            /*
             * Previous.
             */
            if (previous != null) {
                PlayerCover(
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
                                        width
                            }
                )
            }

            /*
             * Current.
             */
            PlayerCover(
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
                                    p * .045f

                            scaleY =
                                scaleX
                        }
            )

            /*
             * Next.
             */
            if (next != null) {
                PlayerCover(
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
            model =
                uri,
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
private fun PreviousPlayerIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bar = w * .10f

        drawRoundRect(
            color = color,
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
            path,
            color
        )
    }
}

@Composable
private fun NextPlayerIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val bar = w * .10f

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
            path,
            color
        )

        drawRoundRect(
            color = color,
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
    modifier: Modifier
) {
    Canvas(modifier) {
        val bw =
            size.width * .18f

        val bh =
            size.height * .62f

        val top =
            (
                size.height -
                    bh
                ) / 2f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .27f,
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
                    size.width * .55f,
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
    milliseconds: Long
): String {
    val total =
        milliseconds
            .coerceAtLeast(0L) /
            1000L

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
