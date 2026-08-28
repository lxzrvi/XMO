package com.xmo.music.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
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
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.LyricLine
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.blur.glassBorder
import com.xmo.music.ui.blur.liveBlur
import com.xmo.music.ui.blur.liveBlurStrong
import com.xmo.music.ui.blur.liveBlurSource
import com.xmo.music.ui.blur.rememberLiveBlurState
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
    liked: Boolean,
    lyricsUri: String?,
    onOpened: () -> Unit,
    refreshPosition: () -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    previousItem: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,
    toggleLike: () -> Unit,
    toggleShuffle: () -> Unit,
    cycleRepeat: () -> Unit,
    setSleepTimer: (Long) -> Unit,
    cancelSleepTimer: () -> Unit,
    saveLyricsUri: (String?) -> Unit,
    dismiss: () -> Unit
) {
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val c =
        homeColors(
            theme
        )

    val accent =
        LocalXmoAccent.current

    val hazeState =
        rememberLiveBlurState()

    val sheetY =
        remember {
            Animatable(
                0f
            )
        }

    val entranceY =
        remember {
            Animatable(
                1f
            )
        }

    var screenHeight by
        remember {
            mutableFloatStateOf(
                1f
            )
        }

    var dismissing by
        remember {
            mutableStateOf(
                false
            )
        }

    var menuOpen by
        remember {
            mutableStateOf(
                false
            )
        }

    var fullLyrics by
        remember {
            mutableStateOf(
                false
            )
        }

    var sleepMenu by
        remember {
            mutableStateOf(
                false
            )
        }

    val index =
        state.currentIndex
            .takeIf {
                it in
                    queue.indices
            }
            ?: queue.indexOfFirst {
                it.id ==
                    state.currentSongId
            }

    val currentSong =
        queue.getOrNull(
            index
        )

    val previousSong =
        queue.getOrNull(
            index -
                1
        )

    val nextSong =
        queue.getOrNull(
            index +
                1
        )

    /*
     * =========================================================
     * ENTRANCE
     * =========================================================
     */

    LaunchedEffect(Unit) {
        entranceY.animateTo(
            0f,
            tween(
                420
            )
        )

        onOpened()
    }

    /*
     * =========================================================
     * ARTWORK COLOR
     * =========================================================
     */

    var dominant by
        remember {
            mutableStateOf(
                Color(
                    0xFF35353A
                )
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
            Artwork.cached(
                uri
            )
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
                tween(
                    460
                ),

            label =
                "playerColor"
        )

    val warm =
        Color(
            ColorUtils.blendARGB(
                animatedDominant
                    .toArgb(),

                accent
                    .toArgb(),

                .13f
            )
        )

    val deep =
        Artwork.end(
            animatedDominant,
            theme
        )

    /*
     * =========================================================
     * REAL POSITION POLLING
     * =========================================================
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

    /*
     * =========================================================
     * LYRICS
     * =========================================================
     */

    var externalLyrics by
        remember(
            state.currentSongId,
            lyricsUri
        ) {
            mutableStateOf<SongLyrics?>(
                null
            )
        }

    LaunchedEffect(
        lyricsUri,
        state.currentSongId
    ) {
        externalLyrics =
            lyricsUri
                ?.let {
                    readLyrics(
                        context,
                        Uri.parse(
                            it
                        )
                    )
                }
    }

    val lyrics =
        externalLyrics
            ?: currentSong
                ?.embeddedLyrics

    val lyricPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .OpenDocument()
        ) { uri ->

            if (
                uri != null
            ) {
                runCatching {
                    context
                        .contentResolver
                        .takePersistableUriPermission(
                            uri,
                            android.content.Intent
                                .FLAG_GRANT_READ_URI_PERMISSION
                        )
                }

                saveLyricsUri(
                    uri.toString()
                )
            }
        }

    /*
     * =========================================================
     * DISMISS
     * =========================================================
     */

    suspend fun closePlayer() {
        if (
            dismissing
        ) {
            return
        }

        dismissing =
            true

        sheetY.animateTo(
            screenHeight,
            tween(
                360
            )
        )

        dismiss()
    }

    BackHandler {
        when {
            fullLyrics ->
                fullLyrics =
                    false

            menuOpen ->
                menuOpen =
                    false

            sleepMenu ->
                sleepMenu =
                    false

            else ->
                scope.launch {
                    closePlayer()
                }
        }
    }

    /*
     * =========================================================
     * ROOT
     * =========================================================
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
                    sheetY.value +
                        entranceY.value *
                        screenHeight
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        (
                            38f *
                                (
                                    sheetY.value /
                                        screenHeight
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp,

                    topEnd =
                        (
                            38f *
                                (
                                    sheetY.value /
                                        screenHeight
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp
                )
            )
            .background(
                c.bg
            )
            .pointerInput(
                screenHeight
            ) {
                detectVerticalDragGestures(
                    onVerticalDrag = {
                            change,
                            dragAmount ->

                        if (
                            dragAmount >
                            0f ||
                            sheetY.value >
                            0f
                        ) {
                            change.consume()

                            scope.launch {
                                sheetY.snapTo(
                                    (
                                        sheetY.value +
                                            dragAmount
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
                                screenHeight *
                                    .13f
                            ) {
                                dismissing =
                                    true

                                sheetY.animateTo(
                                    screenHeight,
                                    tween(
                                        320
                                    )
                                )

                                dismiss()
                            } else {
                                sheetY.animateTo(
                                    0f,

                                    spring(
                                        dampingRatio =
                                            .80f,

                                        stiffness =
                                            380f
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
                                    dampingRatio =
                                        .82f,

                                    stiffness =
                                        390f
                                )
                            )
                        }
                    }
                )
            }
    ) {
        /*
         * =====================================================
         * FULL-SCREEN COLOR FIELD
         * =====================================================
         */

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            animatedDominant,
                            warm,
                            deep,
                            c.bg
                        )
                    )
                )
                .liveBlurSource(
                    hazeState
                )
        )

        /*
         * Wider scattered / distorted-looking artwork field.
         */
        Canvas(
            Modifier.fillMaxSize()
        ) {
            drawRect(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                warm.copy(
                                    alpha = .82f
                                ),
                                Color.Transparent
                            ),

                        center =
                            Offset(
                                size.width *
                                    -.05f,

                                size.height *
                                    .14f
                            ),

                        radius =
                            size.width *
                                1.08f
                    )
            )

            drawRect(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                animatedDominant.copy(
                                    alpha = .72f
                                ),
                                Color.Transparent
                            ),

                        center =
                            Offset(
                                size.width *
                                    1.05f,

                                size.height *
                                    .38f
                            ),

                        radius =
                            size.width *
                                1.15f
                    )
            )

            drawRect(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                warm.copy(
                                    alpha = .44f
                                ),
                                Color.Transparent
                            ),

                        center =
                            Offset(
                                size.width *
                                    .16f,

                                size.height *
                                    .58f
                            ),

                        radius =
                            size.width *
                                .90f
                    )
            )

            drawRect(
                brush =
                    Brush.radialGradient(
                        colors =
                            listOf(
                                deep.copy(
                                    alpha = .95f
                                ),
                                Color.Transparent
                            ),

                        center =
                            Offset(
                                size.width *
                                    .84f,

                                size.height *
                                    .82f
                            ),

                        radius =
                            size.width *
                                1.25f
                    )
            )
        }

        /*
         * =====================================================
         * CONTENT
         * =====================================================
         */

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .statusBarsPadding()
        ) {
            /*
             * HEADER
             */
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        68.dp
                    )
                    .padding(
                        horizontal =
                            14.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                PlayerGlassButton(
                    hazeState =
                        hazeState,

                    theme =
                        theme
                ) {
                    scope.launch {
                        closePlayer()
                    }
                } content@{
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
                        .weight(
                            1f
                        )
                        .padding(
                            horizontal =
                                10.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
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
                            11.sp,

                        letterSpacing =
                            1.1.sp
                    )

                    Text(
                        text =
                            source,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            15.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                PlayerGlassButton(
                    hazeState =
                        hazeState,

                    theme =
                        theme,

                    click = {
                        menuOpen =
                            true
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.MoreVert,

                        contentDescription =
                            "Options",

                        tint =
                            c.icon
                    )
                }
            }

            Spacer(
                Modifier.height(
                    54.dp
                )
            )

            /*
             * ARTWORK CAROUSEL
             *
             * Host itself is full screen width, therefore adjacent
             * covers enter from the SCREEN edge rather than the
             * visible current cover's rounded box.
             */
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
                    state.hasPrevious,

                canNext =
                    state.hasNext,

                c =
                    c,

                previousSong =
                    previousItem,

                nextSong =
                    next
            )

            Spacer(
                Modifier.height(
                    31.dp
                )
            )

            /*
             * =================================================
             * PLAYER PANEL
             * =================================================
             */

            Column(
                Modifier
                    .padding(
                        horizontal =
                            14.dp
                    )
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .liveBlurStrong(
                        hazeState,
                        theme
                    )
                    .drawBehind {
                        val stroke =
                            .7.dp.toPx()

                        val radius =
                            28.dp.toPx()

                        val border =
                            glassBorder(
                                theme
                            )

                        drawLine(
                            color =
                                border,

                            start =
                                Offset(
                                    stroke /
                                        2f,

                                    radius
                                ),

                            end =
                                Offset(
                                    stroke /
                                        2f,

                                    size.height
                                ),

                            strokeWidth =
                                stroke
                        )

                        drawLine(
                            color =
                                border,

                            start =
                                Offset(
                                    size.width -
                                        stroke /
                                        2f,

                                    radius
                                ),

                            end =
                                Offset(
                                    size.width -
                                        stroke /
                                        2f,

                                    size.height
                                ),

                            strokeWidth =
                                stroke
                        )

                        val path =
                            Path().apply {
                                moveTo(
                                    0f,
                                    radius
                                )

                                quadraticTo(
                                    0f,
                                    0f,
                                    radius,
                                    0f
                                )

                                lineTo(
                                    size.width -
                                        radius,
                                    0f
                                )

                                quadraticTo(
                                    size.width,
                                    0f,
                                    size.width,
                                    radius
                                )
                            }

                        drawPath(
                            path =
                                path,

                            color =
                                border,

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
                        horizontal = 18.dp,
                        vertical = 24.dp
                    )
            ) {
                /*
                 * SONG + LIKE / CATEGORY BUTTON
                 */
                Row(
                    Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(
                            1f
                        )
                    ) {
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
                                TextOverflow.Ellipsis
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
                                TextOverflow.Ellipsis
                        )
                    }

                    PlayerPillButton(
                        active =
                            liked,

                        c =
                            c,

                        click =
                            toggleLike
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

                    Spacer(
                        Modifier.width(
                            7.dp
                        )
                    )

                    /*
                     * Opens common song/category options.
                     */
                    PlayerPillButton(
                        active =
                            false,

                        c =
                            c,

                        click = {
                            menuOpen =
                                true
                        }
                    ) {
                        XmoIcon(
                            icon =
                                R.drawable.ic_xmo_add,

                            tint =
                                accent,

                            modifier =
                                Modifier.size(
                                    16.dp
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
                 * THIN PROGRESS — no thumb / beam.
                 */
                PlayerSeekBar(
                    position =
                        state.position,

                    duration =
                        state.duration,

                    active =
                        accent,

                    inactive =
                        glassBorder(
                            theme
                        ),

                    seekTo =
                        seekTo
                )

                Spacer(
                    Modifier.height(
                        7.dp
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

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            12.sp
                    )

                    Text(
                        playerTime(
                            state.duration
                        ),

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            12.sp
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
                    androidx.compose.material3.IconButton(
                        onClick =
                            previous,

                        enabled =
                            state.currentSongId !=
                                null
                    ) {
                        PreviousPlayerIcon(
                            color =
                                c.text,

                            modifier =
                                Modifier.size(
                                    29.dp
                                )
                        )
                    }

                    Box(
                        Modifier
                            .size(
                                62.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                accent
                            )
                            .clickable(
                                enabled =
                                    state.currentSongId !=
                                        null,

                                onClick =
                                    togglePlay
                            ),

                        contentAlignment =
                            Alignment.Center
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
                                    Icons.Default.PlayArrow,

                                contentDescription =
                                    "Play",

                                tint =
                                    Color.White,

                                modifier =
                                    Modifier.size(
                                        32.dp
                                    )
                            )
                        }
                    }

                    androidx.compose.material3.IconButton(
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
                                    c.sub.copy(
                                        alpha = .4f
                                    )
                                },

                            modifier =
                                Modifier.size(
                                    29.dp
                                )
                        )
                    }
                }

                /*
                 * EXTRA REAL CONTROLS
                 */
                Spacer(
                    Modifier.height(
                        22.dp
                    )
                )

                Row(
                    Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceEvenly
                ) {
                    SmallPlayerControl(
                        label =
                            "Shuffle",

                        active =
                            state.shuffleEnabled,

                        c =
                            c,

                        click =
                            toggleShuffle
                    )

                    SmallPlayerControl(
                        label =
                            when (
                                state.repeatMode
                            ) {
                                androidx.media3.common.Player
                                    .REPEAT_MODE_ONE ->
                                    "Repeat 1"

                                androidx.media3.common.Player
                                    .REPEAT_MODE_ALL ->
                                    "Repeat All"

                                else ->
                                    "Repeat"
                            },

                        active =
                            state.repeatMode !=
                                androidx.media3.common.Player
                                    .REPEAT_MODE_OFF,

                        c =
                            c,

                        click =
                            cycleRepeat
                    )

                    SmallPlayerControl(
                        label =
                            "Queue",

                        active =
                            false,

                        c =
                            c
                    ) {
                        menuOpen =
                            true
                    }

                    SmallPlayerControl(
                        label =
                            if (
                                state.sleepTimerRemainingMs >
                                0L
                            ) {
                                playerTime(
                                    state.sleepTimerRemainingMs
                                )
                            } else {
                                "Sleep"
                            },

                        active =
                            state.sleepTimerRemainingMs >
                                0L,

                        c =
                            c
                    ) {
                        sleepMenu =
                            true
                    }
                }

                /*
                 * =================================================
                 * LYRICS
                 * =================================================
                 */

                Spacer(
                    Modifier.height(
                        36.dp
                    )
                )

                BorderSection(
                    c =
                        c,

                    title =
                        "LYRICS",

                    right = {
                        PlayerPillButton(
                            active =
                                false,

                            c =
                                c
                        ) {
                            lyricPicker.launch(
                                arrayOf(
                                    "text/plain",
                                    "application/octet-stream",
                                    "application/x-subrip"
                                )
                            )
                        } content@{
                            Text(
                                "+",

                                color =
                                    accent,

                                fontFamily =
                                    XmoFont.medium,

                                fontSize =
                                    20.sp
                            )
                        }

                        Spacer(
                            Modifier.width(
                                7.dp
                            )
                        )

                        PlayerPillButton(
                            active =
                                false,

                            c =
                                c,

                            click = {
                                fullLyrics =
                                    true
                            }
                        ) {
                            Text(
                                "□",

                                color =
                                    c.text,

                                fontFamily =
                                    XmoFont.medium,

                                fontSize =
                                    17.sp
                            )
                        }
                    }
                ) {
                    LyricsPreview(
                        lyrics =
                            lyrics,

                        position =
                            state.position,

                        c =
                            c,

                        accent =
                            accent
                    )
                }

                /*
                 * =================================================
                 * ARTIST
                 * =================================================
                 */

                Spacer(
                    Modifier.height(
                        22.dp
                    )
                )

                BorderSection(
                    c =
                        c,

                    title =
                        "ARTIST"
                ) {
                    Row(
                        Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            /*
                             * Only genuine local artwork is used.
                             * No fake/network artist image.
                             */
                            model =
                                currentSong?.artwork,

                            contentDescription =
                                null,

                            modifier =
                                Modifier
                                    .size(
                                        58.dp
                                    )
                                    .clip(
                                        CircleShape
                                    )
                                    .background(
                                        c.button
                                    ),

                            contentScale =
                                ContentScale.Crop
                        )

                        Column(
                            Modifier.padding(
                                start =
                                    12.dp
                            )
                        ) {
                            Text(
                                state.artist
                                    .ifBlank {
                                        "Unknown artist"
                                    },

                                color =
                                    c.text,

                                fontFamily =
                                    XmoFont.bold,

                                fontSize =
                                    14.sp
                            )

                            currentSong
                                ?.albumArtist
                                ?.takeIf {
                                    it.isNotBlank() &&
                                        it !=
                                        state.artist
                                }
                                ?.let {
                                    Text(
                                        it,

                                        color =
                                            c.sub,

                                        fontFamily =
                                            XmoFont.thin,

                                        fontSize =
                                            10.sp
                                    )
                                }
                        }
                    }
                }

                /*
                 * =================================================
                 * DETAILS
                 * =================================================
                 */

                Spacer(
                    Modifier.height(
                        22.dp
                    )
                )

                BorderSection(
                    c =
                        c,

                    title =
                        "SONG DETAILS"
                ) {
                    DetailRow(
                        "Album",
                        state.album,
                        c
                    )

                    currentSong
                        ?.metadata
                        ?.let { metadata ->

                            metadata.genre
                                ?.let {
                                    DetailRow(
                                        "Genre",
                                        it,
                                        c
                                    )
                                }

                            metadata.year
                                ?.let {
                                    DetailRow(
                                        "Year",
                                        it.toString(),
                                        c
                                    )
                                }

                            metadata.trackNumber
                                ?.let {
                                    DetailRow(
                                        "Track",
                                        it.toString(),
                                        c
                                    )
                                }

                            metadata.discNumber
                                ?.let {
                                    DetailRow(
                                        "Disc",
                                        it.toString(),
                                        c
                                    )
                                }

                            metadata.composer
                                ?.let {
                                    DetailRow(
                                        "Composer",
                                        it,
                                        c
                                    )
                                }

                            metadata.writer
                                ?.let {
                                    DetailRow(
                                        "Writer",
                                        it,
                                        c
                                    )
                                }

                            metadata.bitrate
                                ?.let {
                                    DetailRow(
                                        "Bitrate",
                                        "${it / 1000} kbps",
                                        c
                                    )
                                }

                            metadata.sampleRate
                                ?.let {
                                    DetailRow(
                                        "Sample rate",
                                        "$it Hz",
                                        c
                                    )
                                }

                            metadata.channelCount
                                ?.let {
                                    DetailRow(
                                        "Channels",
                                        it.toString(),
                                        c
                                    )
                                }

                            metadata.mimeType
                                ?.let {
                                    DetailRow(
                                        "Type",
                                        it,
                                        c
                                    )
                                }

                            metadata.fileName
                                ?.let {
                                    DetailRow(
                                        "File",
                                        it,
                                        c
                                    )
                                }

                            metadata.sizeBytes
                                ?.let {
                                    DetailRow(
                                        "Size",
                                        formatBytes(
                                            it
                                        ),
                                        c
                                    )
                                }
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

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.logo,

                        fontSize =
                            19.sp
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
                            20.dp
                        )
                )
            }
        }

        /*
         * =====================================================
         * PLAYER OPTIONS
         * =====================================================
         */

        if (
            menuOpen
        ) {
            PlayerMenu(
                queue =
                    queue,

                currentId =
                    state.currentSongId,

                c =
                    c,

                close = {
                    menuOpen =
                        false
                },

                select = {
                    /*
                     * Queue rows show actual queue. Existing player
                     * previous/next controls remain authoritative;
                     * direct queue jump wiring comes via App later.
                     */
                    menuOpen =
                        false
                },

                toggleLike = {
                    toggleLike()
                },

                liked =
                    liked,

                removeLyrics = {
                    saveLyricsUri(
                        null
                    )

                    menuOpen =
                        false
                }
            )
        }

        /*
         * =====================================================
         * SLEEP TIMER
         * =====================================================
         */

        if (
            sleepMenu
        ) {
            SleepTimerDialog(
                c =
                    c,

                active =
                    state.sleepTimerRemainingMs >
                        0L,

                close = {
                    sleepMenu =
                        false
                },

                choose = {
                    setSleepTimer(
                        it
                    )

                    sleepMenu =
                        false
                },

                cancel = {
                    cancelSleepTimer()

                    sleepMenu =
                        false
                }
            )
        }

        /*
         * =====================================================
         * FULL-SCREEN LYRICS
         * =====================================================
         */

        if (
            fullLyrics
        ) {
            FullLyrics(
                lyrics =
                    lyrics,

                position =
                    state.position,

                title =
                    state.title,

                artist =
                    state.artist,

                c =
                    c,

                accent =
                    accent,

                close = {
                    fullLyrics =
                        false
                }
            )
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
    c: HomeColors,
    previousSong: () -> Unit,
    nextSong: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val drag =
        remember {
            Animatable(
                0f
            )
        }

    var pending by
        remember {
            mutableIntStateOf(
                0
            )
        }

    var oldId by
        remember {
            mutableStateOf<Long?>(
                null
            )
        }

    LaunchedEffect(
        currentId
    ) {
        if (
            pending !=
            0 &&
            oldId !=
            null &&
            currentId !=
            oldId
        ) {
            drag.snapTo(
                0f
            )

            pending =
                0

            oldId =
                null
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(
                390.dp
            )
    ) {
        val width =
            constraints
                .maxWidth
                .toFloat()

        val coverWidth =
            width *
                .82f

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

                            if (
                                pending !=
                                0
                            ) {
                                return@detectHorizontalDragGestures
                            }

                            change.consume()

                            scope.launch {
                                var target =
                                    drag.value +
                                        amount

                                if (
                                    target <
                                    0f &&
                                    !canNext
                                ) {
                                    target *=
                                        .24f
                                }

                                if (
                                    target >
                                    0f &&
                                    !canPrevious
                                ) {
                                    target *=
                                        .24f
                                }

                                drag.snapTo(
                                    target.coerceIn(
                                        -width,
                                        width
                                    )
                                )
                            }
                        },

                        onDragEnd = {
                            scope.launch {
                                when {
                                    drag.value <
                                        -width *
                                        .16f &&
                                        canNext -> {

                                        oldId =
                                            currentId

                                        pending =
                                            1

                                        drag.animateTo(
                                            -width,
                                            tween(
                                                260
                                            )
                                        )

                                        nextSong()
                                    }

                                    drag.value >
                                        width *
                                        .16f &&
                                        canPrevious -> {

                                        oldId =
                                            currentId

                                        pending =
                                            -1

                                        drag.animateTo(
                                            width,
                                            tween(
                                                260
                                            )
                                        )

                                        previousSong()
                                    }

                                    else -> {
                                        drag.animateTo(
                                            0f,

                                            spring(
                                                dampingRatio =
                                                    .80f,

                                                stiffness =
                                                    420f
                                            )
                                        )
                                    }
                                }
                            }
                        },

                        onDragCancel = {
                            scope.launch {
                                if (
                                    pending ==
                                    0
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
            previous?.let {
                PlayerCover(
                    uri =
                        it,

                    c =
                        c,

                    modifier =
                        Modifier
                            .width(
                                (coverWidth /
                                    width *
                                    maxWidth.value).dp
                            )
                            .aspectRatio(
                                1f
                            )
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    drag.value -
                                        width
                            }
                )
            }

            PlayerCover(
                uri =
                    current,

                c =
                    c,

                modifier =
                    Modifier
                        .fillMaxWidth(
                            .82f
                        )
                        .aspectRatio(
                            1f
                        )
                        .align(
                            Alignment.Center
                        )
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
                                    .035f

                            scaleY =
                                scaleX
                        }
            )

            next?.let {
                PlayerCover(
                    uri =
                        it,

                    c =
                        c,

                    modifier =
                        Modifier
                            .fillMaxWidth(
                                .82f
                            )
                            .aspectRatio(
                                1f
                            )
                            .align(
                                Alignment.Center
                            )
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
    c: HomeColors,
    modifier: Modifier
) {
    Box(
        modifier
            .clip(
                RoundedCornerShape(
                    25.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .7.dp,
                c.border,
                RoundedCornerShape(
                    25.dp
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
            uri ==
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
                        LocalXmoAccent.current,

                    fontFamily =
                        XmoFont.logo,

                    fontSize =
                        32.sp
                )
            }
        }
    }
}

/*
 * =============================================================
 * SEEK BAR
 * =============================================================
 */

@Composable
private fun PlayerSeekBar(
    position: Long,
    duration: Long,
    active: Color,
    inactive: Color,
    seekTo: (Long) -> Unit
) {
    val progress =
        if (
            duration >
            0L
        ) {
            (
                position.toFloat() /
                    duration.toFloat()
                )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            0f
        }

    Box(
        Modifier
            .fillMaxWidth()
            .height(
                22.dp
            )
            .pointerInput(
                duration
            ) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (
                            duration >
                            0L
                        ) {
                            seekTo(
                                (
                                    it.x /
                                        size.width
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                                    .let { value ->
                                        (
                                            duration *
                                                value
                                            ).toLong()
                                    }
                            )
                        }
                    },

                    onHorizontalDrag = {
                            change,
                            _ ->

                        if (
                            duration >
                            0L
                        ) {
                            change.consume()

                            val value =
                                (
                                    change.position.x /
                                        size.width
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )

                            seekTo(
                                (
                                    duration *
                                        value
                                    ).toLong()
                            )
                        }
                    }
                )
            },

        contentAlignment =
            Alignment.CenterStart
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    2.dp
                )
                .clip(
                    RoundedCornerShape(
                        1.dp
                    )
                )
                .background(
                    inactive
                )
        )

        Box(
            Modifier
                .fillMaxWidth(
                    progress
                )
                .height(
                    2.dp
                )
                .clip(
                    RoundedCornerShape(
                        1.dp
                    )
                )
                .background(
                    active
                )
        )
    }
}

/*
 * =============================================================
 * LYRICS
 * =============================================================
 */

@Composable
private fun LyricsPreview(
    lyrics: SongLyrics?,
    position: Long,
    c: HomeColors,
    accent: Color
) {
    if (
        lyrics == null ||
        lyrics.lines.isEmpty()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    190.dp
                ),

            contentAlignment =
                Alignment.CenterStart
        ) {
            Text(
                "No local lyrics found.\nUse + to attach an LRC or text file.",

                color =
                    c.sub,

                fontFamily =
                    XmoFont.normal,

                fontSize =
                    13.sp,

                lineHeight =
                    21.sp
            )
        }

        return
    }

    val active =
        currentLyricIndex(
            lyrics,
            position
        )

    Column(
        Modifier
            .fillMaxWidth()
            .height(
                210.dp
            )
            .verticalScroll(
                rememberScrollState()
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                8.dp
            )
    ) {
        lyrics.lines
            .take(30)
            .forEachIndexed {
                    index,
                    line ->

                Text(
                    line.text,

                    color =
                        if (
                            index ==
                            active
                        ) {
                            accent
                        } else {
                            c.text.copy(
                                alpha = .68f
                            )
                        },

                    fontFamily =
                        if (
                            index ==
                            active
                        ) {
                            XmoFont.bold
                        } else {
                            XmoFont.normal
                        },

                    fontSize =
                        14.sp,

                    lineHeight =
                        22.sp
                )
            }
    }
}

@Composable
private fun FullLyrics(
    lyrics: SongLyrics?,
    position: Long,
    title: String,
    artist: String,
    c: HomeColors,
    accent: Color,
    close: () -> Unit
) {
    val active =
        if (
            lyrics !=
            null
        ) {
            currentLyricIndex(
                lyrics,
                position
            )
        } else {
            -1
        }

    val listState =
        androidx.compose.foundation.lazy
            .rememberLazyListState()

    LaunchedEffect(
        active
    ) {
        if (
            active >=
            0
        ) {
            listState.animateScrollToItem(
                active.coerceAtLeast(
                    0
                )
            )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        64.dp
                    )
                    .padding(
                        horizontal =
                            16.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(
                        1f
                    )
                ) {
                    Text(
                        title,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            17.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        artist,

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.thin,

                        fontSize =
                            10.sp
                    )
                }

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
                        .clickable(
                            onClick =
                                close
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Close,

                        contentDescription =
                            "Close lyrics",

                        tint =
                            c.text
                    )
                }
            }

            if (
                lyrics == null ||
                lyrics.lines.isEmpty()
            ) {
                Box(
                    Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "No lyrics",

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.normal,

                        fontSize =
                            14.sp
                    )
                }
            } else {
                LazyColumn(
                    state =
                        listState,

                    contentPadding =
                        PaddingValues(
                            start = 22.dp,
                            end = 22.dp,
                            top = 70.dp,
                            bottom = 220.dp
                        ),

                    verticalArrangement =
                        Arrangement.spacedBy(
                            18.dp
                        )
                ) {
                    itemsIndexed(
                        lyrics.lines
                    ) {
                            index,
                            line ->

                        Text(
                            line.text,

                            color =
                                if (
                                    index ==
                                    active
                                ) {
                                    accent
                                } else {
                                    c.text.copy(
                                        alpha = .48f
                                    )
                                },

                            fontFamily =
                                if (
                                    index ==
                                    active
                                ) {
                                    XmoFont.bold
                                } else {
                                    XmoFont.normal
                                },

                            fontSize =
                                if (
                                    index ==
                                    active
                                ) {
                                    23.sp
                                } else {
                                    19.sp
                                },

                            lineHeight =
                                30.sp
                        )
                    }
                }
            }
        }
    }
}

/*
 * =============================================================
 * BORDER-ONLY SECTIONS
 * =============================================================
 */

@Composable
private fun BorderSection(
    c: HomeColors,
    title: String,
    right: @Composable Row.() -> Unit = {},
    content: @Composable Column.() -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Column(
        Modifier
            .fillMaxWidth()
            .border(
                .7.dp,
                c.border,
                RoundedCornerShape(
                    22.dp
                )
            )
            .padding(
                16.dp
            )
    ) {
        Row(
            Modifier.fillMaxWidth(),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                title,

                color =
                    accent,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    11.sp,

                letterSpacing =
                    1.1.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            right()
        }

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    c: HomeColors
) {
    if (
        value.isBlank()
    ) {
        return
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                vertical =
                    5.dp
            )
    ) {
        Text(
            label,

            color =
                c.sub,

            fontFamily =
                XmoFont.thin,

            fontSize =
                11.sp,

            modifier =
                Modifier.width(
                    90.dp
                )
        )

        Text(
            value,

            color =
                c.text,

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp,

            textAlign =
                TextAlign.End,

            maxLines =
                2,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}

/*
 * =============================================================
 * MENUS
 * =============================================================
 */

@Composable
private fun PlayerMenu(
    queue: List<Song>,
    currentId: Long?,
    c: HomeColors,
    liked: Boolean,
    close: () -> Unit,
    select: (Song) -> Unit,
    toggleLike: () -> Unit,
    removeLyrics: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .52f
                )
            )
            .clickable(
                onClick =
                    close
            ),

        contentAlignment =
            Alignment.TopEnd
    ) {
        Column(
            Modifier
                .statusBarsPadding()
                .padding(
                    top = 62.dp,
                    end = 14.dp
                )
                .width(
                    270.dp
                )
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(
                    c.surface
                )
                .border(
                    .7.dp,
                    c.border,
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .clickable {}
                .padding(
                    12.dp
                )
        ) {
            PlayerMenuRow(
                if (
                    liked
                ) {
                    "Unlike song"
                } else {
                    "Like song"
                },
                c,
                toggleLike
            )

            PlayerMenuRow(
                "Remove attached lyrics",
                c,
                removeLyrics
            )

            Text(
                "QUEUE",

                color =
                    LocalXmoAccent.current,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    9.sp,

                letterSpacing =
                    1.sp,

                modifier =
                    Modifier.padding(
                        start = 7.dp,
                        top = 13.dp,
                        bottom = 6.dp
                    )
            )

            queue
                .take(6)
                .forEach { song ->

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    11.dp
                                )
                            )
                            .background(
                                if (
                                    song.id ==
                                    currentId
                                ) {
                                    LocalXmoAccent.current.copy(
                                        alpha = .12f
                                    )
                                } else {
                                    Color.Transparent
                                }
                            )
                            .clickable {
                                select(
                                    song
                                )
                            }
                            .padding(
                                8.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            song.title,

                            color =
                                if (
                                    song.id ==
                                    currentId
                                ) {
                                    LocalXmoAccent.current
                                } else {
                                    c.text
                                },

                            fontFamily =
                                XmoFont.medium,

                            fontSize =
                                10.sp,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }
        }
    }
}

@Composable
private fun PlayerMenuRow(
    text: String,
    c: HomeColors,
    click: () -> Unit
) {
    Text(
        text,

        color =
            c.text,

        fontFamily =
            XmoFont.medium,

        fontSize =
            11.sp,

        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        11.dp
                    )
                )
                .clickable(
                    onClick =
                        click
                )
                .padding(
                    10.dp
                )
    )
}

@Composable
private fun SleepTimerDialog(
    c: HomeColors,
    active: Boolean,
    close: () -> Unit,
    choose: (Long) -> Unit,
    cancel: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .56f
                )
            )
            .clickable(
                onClick =
                    close
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal =
                        28.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(
                    c.surface
                )
                .border(
                    .7.dp,
                    c.border,
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .clickable {}
                .padding(
                    18.dp
                )
        ) {
            Text(
                "Sleep Timer",

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    17.sp
            )

            listOf(
                15 to
                    "15 minutes",

                30 to
                    "30 minutes",

                45 to
                    "45 minutes",

                60 to
                    "1 hour"
            ).forEach {
                    (minutes, title) ->

                Text(
                    title,

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.medium,

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                choose(
                                    minutes *
                                        60_000L
                                )
                            }
                            .padding(
                                vertical =
                                    11.dp
                            )
                )
            }

            if (
                active
            ) {
                Text(
                    "Cancel timer",

                    color =
                        LocalXmoAccent.current,

                    fontFamily =
                        XmoFont.medium,

                    fontSize =
                        12.sp,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable(
                                onClick =
                                    cancel
                            )
                            .padding(
                                vertical =
                                    11.dp
                            )
                )
            }
        }
    }
}

/*
 * =============================================================
 * BUTTONS
 * =============================================================
 */

@Composable
private fun PlayerGlassButton(
    hazeState:
        dev.chrisbanes.haze.HazeState,
    theme: XmoTheme,
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(
                41.dp
            )
            .clip(
                CircleShape
            )
            .liveBlur(
                hazeState,
                theme
            )
            .border(
                .6.dp,
                glassBorder(
                    theme
                ),
                CircleShape
            )
            .clickable(
                onClick =
                    click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun PlayerPillButton(
    active: Boolean,
    c: HomeColors,
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .size(
                40.dp
            )
            .clip(
                CircleShape
            )
            .background(
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .15f
                    )
                } else {
                    c.button
                }
            )
            .border(
                .6.dp,
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .34f
                    )
                } else {
                    c.border
                },
                CircleShape
            )
            .clickable(
                onClick =
                    click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SmallPlayerControl(
    label: String,
    active: Boolean,
    c: HomeColors,
    click: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .clip(
                RoundedCornerShape(
                    18.dp
                )
            )
            .background(
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .15f
                    )
                } else {
                    c.button
                }
            )
            .border(
                .6.dp,
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .32f
                    )
                } else {
                    c.border
                },
                RoundedCornerShape(
                    18.dp
                )
            )
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 10.dp,
                vertical = 7.dp
            )
    ) {
        Text(
            label,

            color =
                if (
                    active
                ) {
                    accent
                } else {
                    c.text
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                8.sp,

            maxLines =
                1
        )
    }
}

/*
 * =============================================================
 * CANVAS PLAYER ICONS
 * =============================================================
 */

@Composable
private fun PreviousPlayerIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w *
                .10f

        drawRoundRect(
            color =
                color,

            topLeft =
                Offset(
                    w *
                        .20f,
                    h *
                        .20f
                ),

            size =
                Size(
                    bar,
                    h *
                        .60f
                )
        )

        val path =
            Path().apply {
                moveTo(
                    w *
                        .73f,
                    h *
                        .17f
                )

                lineTo(
                    w *
                        .32f,
                    h *
                        .50f
                )

                lineTo(
                    w *
                        .73f,
                    h *
                        .83f
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
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w *
                .10f

        val path =
            Path().apply {
                moveTo(
                    w *
                        .27f,
                    h *
                        .17f
                )

                lineTo(
                    w *
                        .68f,
                    h *
                        .50f
                )

                lineTo(
                    w *
                        .27f,
                    h *
                        .83f
                )

                close()
            }

        drawPath(
            path,
            color
        )

        drawRoundRect(
            color =
                color,

            topLeft =
                Offset(
                    w *
                        .70f,
                    h *
                        .20f
                ),

            size =
                Size(
                    bar,
                    h *
                        .60f
                )
        )
    }
}

@Composable
private fun PausePlayerIcon(
    color: Color,
    modifier: Modifier
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
                    width /
                        2f
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
                    width /
                        2f
                )
        )
    }
}

/*
 * =============================================================
 * LYRICS PARSER
 * =============================================================
 */

private suspend fun readLyrics(
    context: Context,
    uri: Uri
): SongLyrics? =
    kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO
    ) {
        runCatching {
            val text =
                context
                    .contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }
                    ?: return@runCatching null

            parseLyrics(
                text
            )
        }.getOrNull()
    }

private fun parseLyrics(
    input: String
): SongLyrics {
    val lines =
        mutableListOf<LyricLine>()

    val regex =
        Regex(
            """\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]\s*(.*)"""
        )

    input
        .lineSequence()
        .forEach { raw ->

            val matches =
                regex.findAll(
                    raw
                ).toList()

            if (
                matches.isNotEmpty()
            ) {
                matches.forEach { match ->

                    val minutes =
                        match
                            .groupValues[1]
                            .toLongOrNull()
                            ?: 0L

                    val seconds =
                        match
                            .groupValues[2]
                            .toLongOrNull()
                            ?: 0L

                    val fractionRaw =
                        match
                            .groupValues[3]

                    val fraction =
                        when (
                            fractionRaw.length
                        ) {
                            1 ->
                                (
                                    fractionRaw.toLongOrNull()
                                        ?: 0L
                                    ) *
                                    100L

                            2 ->
                                (
                                    fractionRaw.toLongOrNull()
                                        ?: 0L
                                    ) *
                                    10L

                            3 ->
                                fractionRaw.toLongOrNull()
                                    ?: 0L

                            else ->
                                0L
                        }

                    val text =
                        match
                            .groupValues[4]
                            .trim()

                    if (
                        text.isNotEmpty()
                    ) {
                        lines +=
                            LyricLine(
                                timeMs =
                                    minutes *
                                        60_000L +
                                        seconds *
                                        1_000L +
                                        fraction,

                                text =
                                    text
                            )
                    }
                }
            } else {
                val clean =
                    raw.trim()

                /*
                 * Ignore common LRC metadata tags rather than
                 * presenting them as lyrics.
                 */
                if (
                    clean.isNotEmpty() &&
                    !clean.matches(
                        Regex(
                            """\[(ar|ti|al|by|offset|re|ve):.*]""",
                            RegexOption.IGNORE_CASE
                        )
                    )
                ) {
                    lines +=
                        LyricLine(
                            timeMs =
                                null,

                            text =
                                clean
                        )
                }
            }
        }

    val synced =
        lines.any {
            it.timeMs !=
                null
        }

    val ordered =
        if (
            synced
        ) {
            lines.sortedWith(
                compareBy<LyricLine> {
                    it.timeMs
                        ?: Long.MAX_VALUE
                }
            )
        } else {
            lines
        }

    return SongLyrics(
        lines =
            ordered,

        synced =
            synced,

        source =
            "Local"
    )
}

private fun currentLyricIndex(
    lyrics: SongLyrics,
    position: Long
): Int {
    if (
        !lyrics.synced
    ) {
        return -1
    }

    var result =
        -1

    lyrics.lines
        .forEachIndexed {
                index,
                line ->

            val time =
                line.timeMs
                    ?: return@forEachIndexed

            if (
                time <=
                position
            ) {
                result =
                    index
            } else {
                return@forEachIndexed
            }
        }

    return result
}

/*
 * =============================================================
 * FORMAT
 * =============================================================
 */

private fun playerTime(
    milliseconds: Long
): String {
    val total =
        milliseconds
            .coerceAtLeast(
                0L
            ) /
            1000L

    val hours =
        total /
            3600L

    val minutes =
        (
            total %
                3600L
            ) /
            60L

    val seconds =
        total %
            60L

    return if (
        hours >
        0L
    ) {
        "$hours:${
            minutes.toString()
                .padStart(
                    2,
                    '0'
                )
        }:${
            seconds.toString()
                .padStart(
                    2,
                    '0'
                )
        }"
    } else {
        "$minutes:${
            seconds.toString()
                .padStart(
                    2,
                    '0'
                )
        }"
    }
}

private fun formatBytes(
    bytes: Long
): String =
    when {
        bytes >=
            1024L *
            1024L *
            1024L ->
            String.format(
                "%.2f GB",
                bytes.toDouble() /
                    (
                        1024.0 *
                            1024.0 *
                            1024.0
                        )
            )

        bytes >=
            1024L *
            1024L ->
            String.format(
                "%.1f MB",
                bytes.toDouble() /
                    (
                        1024.0 *
                            1024.0
                        )
            )

        bytes >=
            1024L ->
            String.format(
                "%.1f KB",
                bytes.toDouble() /
                    1024.0
            )

        else ->
            "$bytes B"
    }
