package com.xmo.music.ui

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.LyricLine
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.player.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val c = homeColors(theme)
    val accent = LocalXmoAccent.current

    val index =
        state.currentIndex.takeIf {
            it in queue.indices
        } ?: queue.indexOfFirst {
            it.id == state.currentSongId
        }

    val currentSong = queue.getOrNull(index)
    val previousSong = queue.getOrNull(index - 1)
    val nextSong = queue.getOrNull(index + 1)

    /*
     * =========================================================
     * ARTWORK COLOR
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
                ?: state.artworkUri?.let(
                    Uri::parse
                )

        dominant =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context,
                    uri
                )
    }

    val liveColor by animateColorAsState(
        targetValue = dominant,
        animationSpec = tween(420),
        label = "dominant"
    )

    val deep =
        Artwork.deep(
            liveColor,
            theme
        )

    /*
     * Text sitting directly over artwork always maintains
     * luminance contrast.
     */
    val headerForeground =
        if (
            liveColor.luminance() > .56f
        ) {
            Color(0xFF111111)
        } else {
            Color.White
        }

    val headerSecondary =
        headerForeground.copy(
            alpha = .68f
        )

    /*
     * =========================================================
     * PLAYER PANEL MATERIAL
     * =========================================================
     */

    val panelBackground =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .70f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .60f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .72f
                )
        }

    val panelBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .15f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .16f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .22f
                )
        }

    /*
     * =========================================================
     * POSITION
     * =========================================================
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
     * =========================================================
     * LYRICS
     * =========================================================
     */

    var localLyrics by remember(
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
        localLyrics =
            lyricsUri?.let {
                readLyrics(
                    context,
                    Uri.parse(it)
                )
            }
    }

    val lyrics =
        localLyrics
            ?: currentSong?.embeddedLyrics

    val lyricPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri != null) {
                runCatching {
                    context.contentResolver
                        .takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                }

                /*
                 * Accept real .lrc files even on file managers
                 * which expose them as application/octet-stream.
                 */
                saveLyricsUri(
                    uri.toString()
                )
            }
        }

    /*
     * =========================================================
     * OVERLAYS
     * =========================================================
     */

    var queueOpen by remember {
        mutableStateOf(false)
    }

    var sleepOpen by remember {
        mutableStateOf(false)
    }

    var menuOpen by remember {
        mutableStateOf(false)
    }

    var fullLyrics by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * ENTRANCE / DISMISS
     * =========================================================
     */

    val entrance =
        remember {
            Animatable(1f)
        }

    val dragY =
        remember {
            Animatable(0f)
        }

    var screenHeight by remember {
        mutableFloatStateOf(1f)
    }

    var dismissing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        entrance.animateTo(
            0f,
            tween(410)
        )

        onOpened()
    }

    suspend fun close() {
        if (dismissing) return

        dismissing = true

        dragY.animateTo(
            screenHeight,
            tween(330)
        )

        dismiss()
    }

    BackHandler {
        when {
            fullLyrics ->
                fullLyrics = false

            menuOpen ->
                menuOpen = false

            queueOpen ->
                queueOpen = false

            sleepOpen ->
                sleepOpen = false

            else ->
                scope.launch {
                    close()
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
                        .coerceAtLeast(1f)
            }
            .graphicsLayer {
                translationY =
                    dragY.value +
                        entrance.value *
                        screenHeight
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        (
                            42f *
                                (
                                    dragY.value /
                                        screenHeight
                                    ).coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp,

                    topEnd =
                        (
                            42f *
                                (
                                    dragY.value /
                                        screenHeight
                                    ).coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp
                )
            )
            .background(
                deep
            )
    ) {
        /*
         * =====================================================
         * ARTWORK-DERIVED SCATTERED BACKGROUND
         * =====================================================
         */

        PlayerBackground(
            dominant = liveColor,
            deep = deep,
            theme = theme
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    rememberScrollState()
                )
                .statusBarsPadding()
        ) {
            /*
             * =================================================
             * DRAG + HEADER
             * =================================================
             *
             * Drag is intentionally acquired here instead of the
             * full scrolling body so horizontal artwork and
             * vertical page scroll are not constantly competing.
             */

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(
                        horizontal = 14.dp
                    )
                    .pointerInput(
                        screenHeight
                    ) {
                        detectVerticalDragGestures(
                            onVerticalDrag = {
                                    change,
                                    amount ->

                                if (
                                    amount > 0f ||
                                    dragY.value > 0f
                                ) {
                                    change.consume()

                                    scope.launch {
                                        dragY.snapTo(
                                            (
                                                dragY.value +
                                                    amount
                                                ).coerceIn(
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
                                        dragY.value >
                                        screenHeight * .13f
                                    ) {
                                        dismissing = true

                                        dragY.animateTo(
                                            screenHeight,
                                            tween(300)
                                        )

                                        dismiss()
                                    } else {
                                        dragY.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = .82f,
                                                stiffness = 390f
                                            )
                                        )
                                    }
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    dragY.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = .82f,
                                            stiffness = 390f
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
                 * Bigger close button.
                 */
                XmoCircleControl(
                    size = 46.dp,
                    foreground = headerForeground,
                    background =
                        headerForeground.copy(
                            alpha = .10f
                        ),
                    border =
                        headerForeground.copy(
                            alpha = .20f
                        ),
                    click = {
                        scope.launch {
                            close()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.KeyboardArrowDown,

                        contentDescription =
                            "Close",

                        tint =
                            headerForeground,

                        modifier =
                            Modifier.size(27.dp)
                    )
                }

                Column(
                    Modifier
                        .weight(1f)
                        .padding(
                            horizontal = 10.dp
                        ),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text =
                            if (sourceIsCategory) {
                                "PLAYING FROM CATEGORY"
                            } else {
                                "PLAYING FROM"
                            },

                        color =
                            headerSecondary,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            12.sp,

                        letterSpacing =
                            1.sp,

                        maxLines =
                            1
                    )

                    Text(
                        text = source,

                        color =
                            headerForeground,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            16.sp,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                /*
                 * XmoCapsule:
                 * Share + More.
                 */
                XmoCapsule(
                    background =
                        headerForeground.copy(
                            alpha = .10f
                        ),

                    border =
                        headerForeground.copy(
                            alpha = .20f
                        )
                ) {
                    XmoCapsuleButton(
                        click = {
                            currentSong?.let {
                                shareSong(
                                    context,
                                    it
                                )
                            }
                        }
                    ) {
                        ShareIcon(
                            color =
                                headerForeground,

                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    XmoCapsuleDivider(
                        headerForeground.copy(
                            alpha = .16f
                        )
                    )

                    XmoCapsuleButton(
                        click = {
                            menuOpen = true
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.MoreVert,

                            contentDescription =
                                "More",

                            tint =
                                headerForeground,

                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(48.dp)
            )

            /*
             * =================================================
             * COVER
             *
             * Same horizontal width as player panel.
             * Adjacent covers enter from full screen edge.
             * =================================================
             */

            PlayerArtworkCarousel(
                currentId =
                    state.currentSongId,

                current =
                    currentSong?.artwork
                        ?: state.artworkUri?.let(
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

                previousSong =
                    previousItem,

                nextSong =
                    next,

                surface =
                    c.surface,

                border =
                    panelBorder
            )

            Spacer(
                Modifier.height(28.dp)
            )

            /*
             * =================================================
             * PLAYER PANEL
             * =================================================
             */

            Column(
                Modifier
                    .padding(
                        horizontal = 14.dp
                    )
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .background(
                        panelBackground
                    )
                    .drawBehind {
                        /*
                         * No bottom border.
                         */
                        val stroke =
                            .75.dp.toPx()

                        val r =
                            28.dp.toPx()

                        drawLine(
                            color =
                                panelBorder,

                            start =
                                Offset(
                                    stroke / 2f,
                                    r
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
                                panelBorder,

                            start =
                                Offset(
                                    size.width -
                                        stroke / 2f,
                                    r
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

                        val top =
                            Path().apply {
                                moveTo(
                                    0f,
                                    r
                                )

                                quadraticTo(
                                    0f,
                                    0f,
                                    r,
                                    0f
                                )

                                lineTo(
                                    size.width - r,
                                    0f
                                )

                                quadraticTo(
                                    size.width,
                                    0f,
                                    size.width,
                                    r
                                )
                            }

                        drawPath(
                            path = top,
                            color = panelBorder,
                            style =
                                Stroke(
                                    width = stroke,
                                    cap =
                                        StrokeCap.Round
                                )
                        )
                    }
                    .padding(
                        start = 18.dp,
                        top = 23.dp,
                        end = 18.dp,
                        bottom = 24.dp
                    )
            ) {
                /*
                 * TITLE + LIKE/ADD XmoCapsule
                 */
                Row(
                    Modifier.fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        Modifier.weight(1f)
                    ) {
                        Text(
                            state.title.ifBlank {
                                "Unknown song"
                            },

                            color = c.text,

                            fontFamily =
                                XmoFont.bold,

                            fontSize =
                                21.sp,

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

                            fontSize =
                                13.sp,

                            maxLines = 1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    XmoCapsule(
                        background =
                            c.button,

                        border =
                            panelBorder
                    ) {
                        XmoCapsuleButton(
                            click =
                                toggleLike
                        ) {
                            XmoIcon(
                                icon =
                                    R.drawable.ic_xmo_heart,

                                tint =
                                    if (liked) {
                                        accent
                                    } else {
                                        c.icon
                                    },

                                modifier =
                                    Modifier.size(17.dp)
                            )
                        }

                        XmoCapsuleDivider(
                            panelBorder
                        )

                        XmoCapsuleButton(
                            click = {
                                menuOpen = true
                            }
                        ) {
                            XmoIcon(
                                icon =
                                    R.drawable.ic_xmo_add,

                                tint =
                                    accent,

                                modifier =
                                    Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(
                    Modifier.height(21.dp)
                )

                /*
                 * THIN PROGRESS / NO THUMB
                 */
                XmoSeekBar(
                    position =
                        state.position,

                    duration =
                        state.duration,

                    active =
                        accent,

                    inactive =
                        panelBorder,

                    seekTo =
                        seekTo
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
                            XmoFont.medium,

                        fontSize =
                            12.sp
                    )

                    Text(
                        playerTime(
                            state.duration
                        ),

                        color = c.sub,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            12.sp
                    )
                }

                Spacer(
                    Modifier.height(19.dp)
                )

                /*
                 * =================================================
                 * ONE CONTROL ROW
                 *
                 * Sleep | Shuffle | Prev | Play | Next | Repeat | Queue
                 * =================================================
                 */
                Row(
                    Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    XmoSmallControl(
                        active =
                            state.sleepTimerRemainingMs >
                                0L,

                        c = c,

                        click = {
                            sleepOpen = true
                        }
                    ) {
                        ClockIcon(
                            color =
                                if (
                                    state.sleepTimerRemainingMs >
                                    0L
                                ) {
                                    accent
                                } else {
                                    c.icon
                                },

                            modifier =
                                Modifier.size(16.dp)
                        )
                    }

                    XmoSmallControl(
                        active =
                            state.shuffleEnabled,

                        c = c,

                        click =
                            toggleShuffle
                    ) {
                        ShuffleIcon(
                            color =
                                if (
                                    state.shuffleEnabled
                                ) {
                                    accent
                                } else {
                                    c.icon
                                },

                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    XmoCircleControl(
                        size = 37.dp,
                        foreground = c.text,
                        background = c.button,
                        border = panelBorder,
                        click = previous
                    ) {
                        PreviousIcon(
                            color = c.text,
                            modifier =
                                Modifier.size(19.dp)
                        )
                    }

                    /*
                     * Smaller play circle.
                     */
                    XmoCircleControl(
                        size = 48.dp,
                        foreground = Color.White,
                        background = accent,
                        border =
                            accent.copy(
                                alpha = .65f
                            ),
                        click =
                            togglePlay
                    ) {
                        if (
                            state.isPlaying
                        ) {
                            PauseIcon(
                                color =
                                    Color.White,

                                modifier =
                                    Modifier.size(22.dp)
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
                                    Modifier.size(25.dp)
                            )
                        }
                    }

                    XmoCircleControl(
                        size = 37.dp,
                        foreground = c.text,
                        background = c.button,
                        border = panelBorder,
                        enabled =
                            state.hasNext,
                        click = next
                    ) {
                        NextIcon(
                            color =
                                if (
                                    state.hasNext
                                ) {
                                    c.text
                                } else {
                                    c.sub.copy(
                                        alpha = .35f
                                    )
                                },

                            modifier =
                                Modifier.size(19.dp)
                        )
                    }

                    XmoSmallControl(
                        active =
                            state.repeatMode !=
                                androidx.media3.common.Player
                                    .REPEAT_MODE_OFF,

                        c = c,

                        click =
                            cycleRepeat
                    ) {
                        RepeatIcon(
                            color =
                                if (
                                    state.repeatMode !=
                                    androidx.media3.common.Player
                                        .REPEAT_MODE_OFF
                                ) {
                                    accent
                                } else {
                                    c.icon
                                },

                            one =
                                state.repeatMode ==
                                    androidx.media3.common.Player
                                        .REPEAT_MODE_ONE,

                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    XmoSmallControl(
                        active = false,
                        c = c,
                        click = {
                            queueOpen = true
                        }
                    ) {
                        QueueIcon(
                            color = c.icon,
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }
                }

                /*
                 * =================================================
                 * LYRICS
                 * =================================================
                 */

                Spacer(
                    Modifier.height(35.dp)
                )

                PlayerBorderSection(
                    c = c,
                    border = panelBorder,
                    title = "LYRICS",
                    modifier =
                        Modifier.padding(
                            horizontal = 3.dp
                        ),
                    actions = {
                        XmoCapsule(
                            background =
                                c.button,

                            border =
                                panelBorder
                        ) {
                            XmoCapsuleButton(
                                click = {
                                    lyricPicker.launch(
                                        arrayOf(
                                            "text/plain",
                                            "application/octet-stream",
                                            "application/x-subrip",
                                            "*/*"
                                        )
                                    )
                                }
                            ) {
                                Text(
                                    "+",

                                    color = accent,

                                    fontFamily =
                                        XmoFont.medium,

                                    fontSize =
                                        20.sp
                                )
                            }

                            XmoCapsuleDivider(
                                panelBorder
                            )

                            XmoCapsuleButton(
                                click = {
                                    fullLyrics = true
                                }
                            ) {
                                FullscreenIcon(
                                    color = c.text,
                                    modifier =
                                        Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                ) {
                    LyricsBody(
                        lyrics = lyrics,
                        position =
                            state.position,
                        c = c,
                        accent = accent
                    )
                }

                /*
                 * =================================================
                 * ARTIST
                 * =================================================
                 */

                Spacer(
                    Modifier.height(20.dp)
                )

                PlayerBorderSection(
                    c = c,
                    border = panelBorder,
                    title = "ARTIST",
                    modifier =
                        Modifier.padding(
                            horizontal = 3.dp
                        )
                ) {
                    Row(
                        Modifier.fillMaxWidth(),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model =
                                currentSong?.artwork,

                            contentDescription =
                                null,

                            modifier =
                                Modifier
                                    .size(58.dp)
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
                                start = 12.dp
                            )
                        ) {
                            Text(
                                state.artist.ifBlank {
                                    "Unknown artist"
                                },

                                color = c.text,

                                fontFamily =
                                    XmoFont.bold,

                                fontSize =
                                    14.sp
                            )

                            currentSong?.albumArtist
                                ?.takeIf {
                                    it.isNotBlank() &&
                                        it != state.artist
                                }
                                ?.let {
                                    Text(
                                        it,

                                        color = c.sub,

                                        fontFamily =
                                            XmoFont.normal,

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
                    Modifier.height(20.dp)
                )

                PlayerBorderSection(
                    c = c,
                    border = panelBorder,
                    title =
                        "SONG DETAILS",
                    modifier =
                        Modifier.padding(
                            horizontal = 3.dp
                        )
                ) {
                    DetailRow(
                        "Album",
                        state.album,
                        c
                    )

                    currentSong?.metadata?.let {
                        meta ->

                        meta.genre?.let {
                            DetailRow(
                                "Genre",
                                it,
                                c
                            )
                        }

                        meta.year?.let {
                            DetailRow(
                                "Year",
                                it.toString(),
                                c
                            )
                        }

                        meta.trackNumber?.let {
                            DetailRow(
                                "Track",
                                it.toString(),
                                c
                            )
                        }

                        meta.discNumber?.let {
                            DetailRow(
                                "Disc",
                                it.toString(),
                                c
                            )
                        }

                        meta.composer?.let {
                            DetailRow(
                                "Composer",
                                it,
                                c
                            )
                        }

                        meta.writer?.let {
                            DetailRow(
                                "Writer",
                                it,
                                c
                            )
                        }

                        meta.bitrate?.let {
                            DetailRow(
                                "Bitrate",
                                "${it / 1000} kbps",
                                c
                            )
                        }

                        meta.sampleRate?.let {
                            DetailRow(
                                "Sample rate",
                                "$it Hz",
                                c
                            )
                        }

                        meta.channelCount?.let {
                            DetailRow(
                                "Channels",
                                it.toString(),
                                c
                            )
                        }

                        meta.mimeType?.let {
                            DetailRow(
                                "Type",
                                it,
                                c
                            )
                        }

                        meta.fileName?.let {
                            DetailRow(
                                "File",
                                it,
                                c
                            )
                        }

                        meta.sizeBytes?.let {
                            DetailRow(
                                "Size",
                                formatBytes(it),
                                c
                            )
                        }
                    }
                }

                Spacer(
                    Modifier.height(80.dp)
                )

                Column(
                    Modifier.fillMaxWidth(),

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        "XMO",

                        color = c.text,

                        fontFamily =
                            XmoFont.logo,

                        fontSize =
                            19.sp
                    )

                    Text(
                        "lxzrvi • copyright © 2026",

                        color = c.sub,

                        fontFamily =
                            XmoFont.normal,

                        fontSize =
                            9.sp
                    )
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(18.dp)
                )
            }
        }

        /*
         * =====================================================
         * QUEUE
         * =====================================================
         */

        if (
            queueOpen
        ) {
            QueueOverlay(
                queue = queue,
                currentId =
                    state.currentSongId,
                c = c,
                backgroundColor =
                    liveColor,
                close = {
                    queueOpen = false
                }
            )
        }

        /*
         * =====================================================
         * SLEEP TIMER
         * =====================================================
         */

        if (
            sleepOpen
        ) {
            SleepOverlay(
                c = c,

                active =
                    state.sleepTimerRemainingMs >
                        0L,

                close = {
                    sleepOpen = false
                },

                select = {
                    setSleepTimer(it)
                    sleepOpen = false
                },

                cancel = {
                    cancelSleepTimer()
                    sleepOpen = false
                }
            )
        }

        /*
         * =====================================================
         * MORE
         * =====================================================
         */

        if (
            menuOpen
        ) {
            MoreOverlay(
                c = c,
                song =
                    currentSong,
                liked =
                    liked,
                close = {
                    menuOpen = false
                },
                toggleLike = {
                    toggleLike()
                    menuOpen = false
                },
                share = {
                    currentSong?.let {
                        shareSong(
                            context,
                            it
                        )
                    }

                    menuOpen = false
                },
                removeLyrics = {
                    saveLyricsUri(null)
                    menuOpen = false
                }
            )
        }

        /*
         * =====================================================
         * FULLSCREEN LYRICS
         * =====================================================
         */

        if (
            fullLyrics
        ) {
            FullLyrics(
                lyrics = lyrics,

                position =
                    state.position,

                title =
                    state.title,

                artist =
                    state.artist,

                artwork =
                    currentSong?.artwork,

                dominant =
                    liveColor,

                deep =
                    deep,

                c = c,

                accent =
                    accent,

                close = {
                    fullLyrics = false
                }
            )
        }
    }
}

/*
 * =============================================================
 * BACKGROUND
 * =============================================================
 */

@Composable
private fun PlayerBackground(
    dominant: Color,
    deep: Color,
    theme: XmoTheme
) {
    val base =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF4F6F9)

            XmoTheme.Dark ->
                Color(0xFF111113)

            XmoTheme.Amoled ->
                Color.Black
        }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        dominant,
                        deep,
                        base
                    )
                )
            )
    ) {
        /*
         * Uneven/scattered fields. Every field is derived only
         * from current artwork color/deep color.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            dominant.copy(
                                alpha = .92f
                            ),
                            Color.Transparent
                        ),

                    center =
                        Offset(
                            size.width * -.08f,
                            size.height * .12f
                        ),

                    radius =
                        size.width * 1.08f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            dominant.copy(
                                alpha = .66f
                            ),
                            Color.Transparent
                        ),

                    center =
                        Offset(
                            size.width * 1.08f,
                            size.height * .38f
                        ),

                    radius =
                        size.width * .98f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            deep.copy(
                                alpha = .92f
                            ),
                            Color.Transparent
                        ),

                    center =
                        Offset(
                            size.width * .05f,
                            size.height * .72f
                        ),

                    radius =
                        size.width * 1.18f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            dominant.copy(
                                alpha = .27f
                            ),
                            Color.Transparent
                        ),

                    center =
                        Offset(
                            size.width * .92f,
                            size.height * .88f
                        ),

                    radius =
                        size.width * .80f
                )
        )
    }
}

/*
 * =============================================================
 * ARTWORK
 * =============================================================
 */

@Composable
private fun PlayerArtworkCarousel(
    currentId: Long?,
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    canPrevious: Boolean,
    canNext: Boolean,
    previousSong: () -> Unit,
    nextSong: () -> Unit,
    surface: Color,
    border: Color
) {
    val scope =
        rememberCoroutineScope()

    val drag =
        remember {
            Animatable(0f)
        }

    var oldId by remember {
        mutableStateOf<Long?>(
            null
        )
    }

    var pending by remember {
        mutableIntStateOf(0)
    }

    /*
     * Reset only when Media3 really reports the next item.
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
            .height(382.dp)
    ) {
        val screenWidth =
            constraints.maxWidth
                .toFloat()

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
                                pending != 0
                            ) {
                                return@detectHorizontalDragGestures
                            }

                            change.consume()

                            scope.launch {
                                var target =
                                    drag.value +
                                        amount

                                if (
                                    target < 0f &&
                                    !canNext
                                ) {
                                    target =
                                        drag.value +
                                            amount * .18f
                                }

                                if (
                                    target > 0f &&
                                    !canPrevious
                                ) {
                                    target =
                                        drag.value +
                                            amount * .18f
                                }

                                drag.snapTo(
                                    target.coerceIn(
                                        -screenWidth,
                                        screenWidth
                                    )
                                )
                            }
                        },

                        onDragEnd = {
                            scope.launch {
                                when {
                                    drag.value <
                                        -screenWidth * .15f &&
                                        canNext -> {

                                        oldId =
                                            currentId

                                        pending = 1

                                        drag.animateTo(
                                            -screenWidth,
                                            tween(230)
                                        )

                                        /*
                                         * Real Media3 command.
                                         * UI updates from currentSongId.
                                         */
                                        nextSong()
                                    }

                                    drag.value >
                                        screenWidth * .15f &&
                                        canPrevious -> {

                                        oldId =
                                            currentId

                                        pending = -1

                                        drag.animateTo(
                                            screenWidth,
                                            tween(230)
                                        )

                                        previousSong()
                                    }

                                    else -> {
                                        drag.animateTo(
                                            0f,
                                            spring(
                                                dampingRatio = .80f,
                                                stiffness = 430f
                                            )
                                        )
                                    }
                                }
                            }
                        },

                        onDragCancel = {
                            scope.launch {
                                if (
                                    pending == 0
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
            /*
             * Adjacent cards use screen width translation;
             * they arrive from actual screen edge.
             */
            if (
                previous != null
            ) {
                ArtworkCard(
                    uri = previous,
                    surface = surface,
                    border = border,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = 14.dp
                            )
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    drag.value -
                                        screenWidth
                            }
                )
            }

            ArtworkCard(
                uri = current,
                surface = surface,
                border = border,
                modifier =
                    Modifier
                        .padding(
                            horizontal = 14.dp
                        )
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer {
                            translationX =
                                drag.value
                        }
            )

            if (
                next != null
            ) {
                ArtworkCard(
                    uri = next,
                    surface = surface,
                    border = border,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = 14.dp
                            )
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    drag.value +
                                        screenWidth
                            }
                )
            }
        }
    }
}

@Composable
private fun ArtworkCard(
    uri: Uri?,
    surface: Color,
    border: Color,
    modifier: Modifier
) {
    Box(
        modifier
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(surface)
            .border(
                .7.dp,
                border,
                RoundedCornerShape(24.dp)
            )
    ) {
        AsyncImage(
            model = uri,
            contentDescription = null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )

        if (
            uri == null
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
                        31.sp
                )
            }
        }
    }
}

/*
 * =============================================================
 * XMO CAPSULE
 *
 * Shared name for:
 * Home Refresh + Menu
 * Player Like + Add
 * Player Share + More
 * Lyrics Add + Fullscreen
 * =============================================================
 */

@Composable
private fun XmoCapsule(
    background: Color,
    border: Color,
    content:
        @Composable RowScope.() -> Unit
) {
    Row(
        Modifier
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(background)
            .border(
                .65.dp,
                border,
                RoundedCornerShape(22.dp)
            )
            .padding(
                horizontal = 2.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically,

        content = content
    )
}

@Composable
private fun XmoCapsuleButton(
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(39.dp)
            .clip(CircleShape)
            .clickable(
                onClick = click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun XmoCapsuleDivider(
    color: Color
) {
    Box(
        Modifier
            .width(.6.dp)
            .height(18.dp)
            .background(color)
    )
}

/*
 * =============================================================
 * PLAYER CONTROL
 * =============================================================
 */

@Composable
private fun XmoCircleControl(
    size: androidx.compose.ui.unit.Dp,
    foreground: Color,
    background: Color,
    border: Color,
    enabled: Boolean = true,
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .border(
                .65.dp,
                border,
                CircleShape
            )
            .clickable(
                enabled = enabled,
                onClick = click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun XmoSmallControl(
    active: Boolean,
    c: HomeColors,
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(31.dp)
            .clip(CircleShape)
            .background(
                if (active) {
                    LocalXmoAccent.current.copy(
                        alpha = .13f
                    )
                } else {
                    c.button
                }
            )
            .clickable(
                onClick = click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

/*
 * =============================================================
 * SEEK
 * =============================================================
 */

@Composable
private fun XmoSeekBar(
    position: Long,
    duration: Long,
    active: Color,
    inactive: Color,
    seekTo: (Long) -> Unit
) {
    val progress =
        if (
            duration > 0L
        ) {
            (
                position.toFloat() /
                    duration.toFloat()
                ).coerceIn(
                0f,
                1f
            )
        } else {
            0f
        }

    Box(
        Modifier
            .fillMaxWidth()
            .height(20.dp)
            .pointerInput(
                duration
            ) {
                detectTapGestures {
                    if (
                        duration > 0L
                    ) {
                        val fraction =
                            (
                                it.x /
                                    size.width
                                ).coerceIn(
                                0f,
                                1f
                            )

                        seekTo(
                            (
                                duration *
                                    fraction
                                ).toLong()
                        )
                    }
                }
            },

        contentAlignment =
            Alignment.CenterStart
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(
                    RoundedCornerShape(1.dp)
                )
                .background(inactive)
        )

        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(2.dp)
                .clip(
                    RoundedCornerShape(1.dp)
                )
                .background(active)
        )
    }
}

/*
 * =============================================================
 * BORDER-ONLY SECTIONS
 * =============================================================
 */

@Composable
private fun PlayerBorderSection(
    c: HomeColors,
    border: Color,
    title: String,
    modifier: Modifier = Modifier,
    actions:
        @Composable RowScope.() -> Unit = {},
    content:
        @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier
            .fillMaxWidth()
            .border(
                .7.dp,
                border,
                RoundedCornerShape(22.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                title,
                color =
                    LocalXmoAccent.current,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    11.sp,
                letterSpacing =
                    1.1.sp,
                modifier =
                    Modifier.weight(1f)
            )

            actions()
        }

        Spacer(
            Modifier.height(12.dp)
        )

        content()
    }
}

/*
 * =============================================================
 * LYRICS
 * =============================================================
 */

@Composable
private fun LyricsBody(
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
                .height(210.dp),

            contentAlignment =
                Alignment.CenterStart
        ) {
            Text(
                "No local lyrics found.\nTap + to select an LRC or text file.",
                color = c.sub,
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

    val scroll =
        rememberScrollState()

    Column(
        Modifier
            .fillMaxWidth()
            .height(230.dp)
            .verticalScroll(scroll),

        verticalArrangement =
            Arrangement.spacedBy(9.dp)
    ) {
        lyrics.lines.forEachIndexed {
                index,
                line ->

            Text(
                line.text,
                color =
                    if (index == active) {
                        accent
                    } else {
                        c.text.copy(
                            alpha = .68f
                        )
                    },
                fontFamily =
                    if (index == active) {
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
    artwork: Uri?,
    dominant: Color,
    deep: Color,
    c: HomeColors,
    accent: Color,
    close: () -> Unit
) {
    val list =
        rememberLazyListState()

    val active =
        lyrics?.let {
            currentLyricIndex(
                it,
                position
            )
        } ?: -1

    LaunchedEffect(
        active
    ) {
        if (
            active >= 0
        ) {
            list.animateScrollToItem(
                active
            )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(deep)
    ) {
        PlayerBackground(
            dominant = dominant,
            deep = deep,
            theme =
                when {
                    c.bg == Color.Black ->
                        XmoTheme.Amoled

                    c.text == Color.White ->
                        XmoTheme.Dark

                    else ->
                        XmoTheme.Light
                }
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(
                        horizontal = 16.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = artwork,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(
                                RoundedCornerShape(9.dp)
                            ),
                    contentScale =
                        ContentScale.Crop
                )

                Column(
                    Modifier
                        .weight(1f)
                        .padding(
                            start = 11.dp
                        )
                ) {
                    Text(
                        title,
                        color = c.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            15.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        artist,
                        color = c.sub,
                        fontFamily =
                            XmoFont.normal,
                        fontSize =
                            10.sp,
                        maxLines = 1
                    )
                }

                XmoCircleControl(
                    size = 40.dp,
                    foreground = c.text,
                    background = c.button,
                    border = c.border,
                    click = close
                ) {
                    Text(
                        "×",
                        color = c.text,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 20.sp
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
                        color = c.sub,
                        fontFamily =
                            XmoFont.normal,
                        fontSize =
                            14.sp
                    )
                }
            } else {
                LazyColumn(
                    state = list,
                    contentPadding =
                        PaddingValues(
                            start = 22.dp,
                            end = 22.dp,
                            top = 80.dp,
                            bottom = 240.dp
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
                                    index == active
                                ) {
                                    accent
                                } else {
                                    c.text.copy(
                                        alpha = .50f
                                    )
                                },
                            fontFamily =
                                if (
                                    index == active
                                ) {
                                    XmoFont.bold
                                } else {
                                    XmoFont.normal
                                },
                            fontSize =
                                if (
                                    index == active
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
 * DETAILS
 * =============================================================
 */

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
                vertical = 5.dp
            )
    ) {
        /*
         * Normal — Thin deliberately removed.
         */
        Text(
            label,
            color = c.sub,
            fontFamily =
                XmoFont.normal,
            fontSize =
                11.sp,
            modifier =
                Modifier.width(92.dp)
        )

        Text(
            value,
            color = c.text,
            fontFamily =
                XmoFont.medium,
            fontSize =
                11.sp,
            textAlign =
                TextAlign.End,
            maxLines = 2,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.weight(1f)
        )
    }
}

/*
 * =============================================================
 * QUEUE
 * =============================================================
 */

@Composable
private fun QueueOverlay(
    queue: List<Song>,
    currentId: Long?,
    c: HomeColors,
    backgroundColor: Color,
    close: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .58f
                )
            )
            .clickable(
                onClick = close
            ),
        contentAlignment =
            Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(470.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
                .background(c.surface)
                .border(
                    .7.dp,
                    c.border,
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
                .clickable {}
                .padding(
                    start = 14.dp,
                    top = 16.dp,
                    end = 14.dp
                )
        ) {
            Text(
                "QUEUE",
                color =
                    LocalXmoAccent.current,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    11.sp,
                letterSpacing =
                    1.sp
            )

            Spacer(
                Modifier.height(12.dp)
            )

            LazyColumn(
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(
                    queue,
                    key = {
                            _,
                            song ->
                        song.id
                    }
                ) {
                        _,
                        song ->

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (
                                    song.id ==
                                    currentId
                                ) {
                                    LocalXmoAccent
                                        .current.copy(
                                            alpha = .12f
                                        )
                                } else {
                                    c.button
                                }
                            )
                            .padding(5.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model =
                                song.artwork,
                            contentDescription =
                                null,
                            modifier =
                                Modifier
                                    .size(46.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            8.dp
                                        )
                                    ),
                            contentScale =
                                ContentScale.Crop
                        )

                        Column(
                            Modifier
                                .weight(1f)
                                .padding(
                                    start = 10.dp
                                )
                        ) {
                            Text(
                                song.title,
                                color =
                                    if (
                                        song.id ==
                                        currentId
                                    ) {
                                        LocalXmoAccent
                                            .current
                                    } else {
                                        c.text
                                    },
                                fontFamily =
                                    XmoFont.bold,
                                fontSize =
                                    11.sp,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Text(
                                song.artist,
                                color = c.sub,
                                fontFamily =
                                    XmoFont.normal,
                                fontSize =
                                    9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/*
 * =============================================================
 * SLEEP
 * =============================================================
 */

@Composable
private fun SleepOverlay(
    c: HomeColors,
    active: Boolean,
    close: () -> Unit,
    select: (Long) -> Unit,
    cancel: () -> Unit
) {
    XmoPopup(
        title = "Sleep Timer",
        c = c,
        close = close
    ) {
        listOf(
            15L to "15 minutes",
            30L to "30 minutes",
            45L to "45 minutes",
            60L to "1 hour"
        ).forEach {
                (minutes, text) ->

            PopupRow(
                text,
                c
            ) {
                select(
                    minutes *
                        60_000L
                )
            }
        }

        if (active) {
            PopupRow(
                "Cancel timer",
                c,
                accent = true,
                click = cancel
            )
        }
    }
}

/*
 * =============================================================
 * MORE
 * =============================================================
 */

@Composable
private fun MoreOverlay(
    c: HomeColors,
    song: Song?,
    liked: Boolean,
    close: () -> Unit,
    toggleLike: () -> Unit,
    share: () -> Unit,
    removeLyrics: () -> Unit
) {
    XmoPopup(
        title =
            song?.title
                ?: "Song",

        c = c,

        close = close
    ) {
        PopupRow(
            if (liked) {
                "Remove from Liked Songs"
            } else {
                "Add to Liked Songs"
            },
            c,
            accent = liked,
            click = toggleLike
        )

        PopupRow(
            "Share local song",
            c,
            click = share
        )

        PopupRow(
            "Remove attached lyrics",
            c,
            click = removeLyrics
        )
    }
}

@Composable
private fun XmoPopup(
    title: String,
    c: HomeColors,
    close: () -> Unit,
    content:
        @Composable ColumnScope.() -> Unit
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
                onClick = close
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 28.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(22.dp)
                )
                .background(c.surface)
                .border(
                    .7.dp,
                    c.border,
                    RoundedCornerShape(22.dp)
                )
                .clickable {}
                .padding(17.dp)
        ) {
            Text(
                title,
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    16.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Spacer(
                Modifier.height(10.dp)
            )

            content()
        }
    }
}

@Composable
private fun PopupRow(
    text: String,
    c: HomeColors,
    accent: Boolean = false,
    click: () -> Unit
) {
    Text(
        text,
        color =
            if (accent) {
                LocalXmoAccent.current
            } else {
                c.text
            },
        fontFamily =
            XmoFont.medium,
        fontSize =
            12.sp,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(11.dp)
                )
                .clickable(
                    onClick = click
                )
                .padding(
                    vertical = 11.dp,
                    horizontal = 8.dp
                )
    )
}

/*
 * =============================================================
 * SHARE
 * =============================================================
 */

private fun shareSong(
    context: Context,
    song: Song
) {
    val intent =
        Intent(
            Intent.ACTION_SEND
        ).apply {
            type =
                song.metadata.mimeType
                    ?: "audio/*"

            putExtra(
                Intent.EXTRA_STREAM,
                song.uri
            )

            putExtra(
                Intent.EXTRA_TITLE,
                song.title
            )

            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

    context.startActivity(
        Intent.createChooser(
            intent,
            "Share ${song.title}"
        )
    )
}

/*
 * =============================================================
 * LRC
 * =============================================================
 */

private suspend fun readLyrics(
    context: Context,
    uri: Uri
): SongLyrics? =
    withContext(
        Dispatchers.IO
    ) {
        runCatching {
            val text =
                context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }
                    ?: return@runCatching null

            parseLyrics(text)
        }.getOrNull()
    }

private fun parseLyrics(
    source: String
): SongLyrics {
    val output =
        mutableListOf<LyricLine>()

    /*
     * Supports:
     * [01:23]
     * [01:23.45]
     * [01:23.456]
     * Multiple timestamps per line.
     */
    val timestamp =
        Regex(
            """\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]"""
        )

    source.lineSequence()
        .forEach { raw ->

            val matches =
                timestamp
                    .findAll(raw)
                    .toList()

            if (
                matches.isNotEmpty()
            ) {
                val text =
                    timestamp
                        .replace(
                            raw,
                            ""
                        )
                        .trim()

                if (
                    text.isNotEmpty()
                ) {
                    matches.forEach {
                            match ->

                        val minutes =
                            match.groupValues[1]
                                .toLongOrNull()
                                ?: 0L

                        val seconds =
                            match.groupValues[2]
                                .toLongOrNull()
                                ?: 0L

                        val fractionRaw =
                            match.groupValues[3]

                        val fraction =
                            when (
                                fractionRaw.length
                            ) {
                                1 ->
                                    (
                                        fractionRaw
                                            .toLongOrNull()
                                            ?: 0L
                                        ) *
                                        100L

                                2 ->
                                    (
                                        fractionRaw
                                            .toLongOrNull()
                                            ?: 0L
                                        ) *
                                        10L

                                3 ->
                                    fractionRaw
                                        .toLongOrNull()
                                        ?: 0L

                                else ->
                                    0L
                            }

                        output +=
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
                 * Ignore LRC metadata tags.
                 */
                val metadata =
                    clean.matches(
                        Regex(
                            """\[(ar|ti|al|by|offset|re|ve):.*]""",
                            RegexOption.IGNORE_CASE
                        )
                    )

                if (
                    clean.isNotEmpty() &&
                    !metadata
                ) {
                    output +=
                        LyricLine(
                            timeMs = null,
                            text = clean
                        )
                }
            }
        }

    val synced =
        output.any {
            it.timeMs != null
        }

    return SongLyrics(
        lines =
            if (synced) {
                output.sortedWith(
                    compareBy {
                        it.timeMs
                            ?: Long.MAX_VALUE
                    }
                )
            } else {
                output
            },

        synced = synced,
        source = "Local"
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

    var result = -1

    lyrics.lines.forEachIndexed {
            index,
            line ->

        val time =
            line.timeMs
                ?: return@forEachIndexed

        if (
            time <= position
        ) {
            result = index
        }
    }

    return result
}

/*
 * =============================================================
 * CANVAS ICONS
 * =============================================================
 */

@Composable
private fun PreviousIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val path =
            Path().apply {
                moveTo(
                    size.width * .72f,
                    size.height * .18f
                )

                lineTo(
                    size.width * .34f,
                    size.height * .50f
                )

                lineTo(
                    size.width * .72f,
                    size.height * .82f
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
                    size.width * .20f,
                    size.height * .20f
                ),
            size =
                Size(
                    size.width * .10f,
                    size.height * .60f
                )
        )
    }
}

@Composable
private fun NextIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val path =
            Path().apply {
                moveTo(
                    size.width * .28f,
                    size.height * .18f
                )

                lineTo(
                    size.width * .66f,
                    size.height * .50f
                )

                lineTo(
                    size.width * .28f,
                    size.height * .82f
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
                    size.width * .70f,
                    size.height * .20f
                ),
            size =
                Size(
                    size.width * .10f,
                    size.height * .60f
                )
        )
    }
}

@Composable
private fun PauseIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val width =
            size.width * .18f

        val height =
            size.height * .62f

        val top =
            (
                size.height -
                    height
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
                    width,
                    height
                ),
            cornerRadius =
                CornerRadius(
                    width / 2f
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
                    width,
                    height
                ),
            cornerRadius =
                CornerRadius(
                    width / 2f
                )
        )
    }
}

@Composable
private fun ShuffleIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.width * .09f

        drawLine(
            color,
            Offset(
                size.width * .13f,
                size.height * .27f
            ),
            Offset(
                size.width * .38f,
                size.height * .27f
            ),
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .38f,
                size.height * .27f
            ),
            Offset(
                size.width * .70f,
                size.height * .70f
            ),
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .13f,
                size.height * .73f
            ),
            Offset(
                size.width * .38f,
                size.height * .73f
            ),
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .38f,
                size.height * .73f
            ),
            Offset(
                size.width * .70f,
                size.height * .30f
            ),
            stroke,
            StrokeCap.Round
        )

        val top =
            Path().apply {
                moveTo(
                    size.width * .68f,
                    size.height * .17f
                )
                lineTo(
                    size.width * .88f,
                    size.height * .30f
                )
                lineTo(
                    size.width * .68f,
                    size.height * .43f
                )
                close()
            }

        val bottom =
            Path().apply {
                moveTo(
                    size.width * .68f,
                    size.height * .57f
                )
                lineTo(
                    size.width * .88f,
                    size.height * .70f
                )
                lineTo(
                    size.width * .68f,
                    size.height * .83f
                )
                close()
            }

        drawPath(top, color)
        drawPath(bottom, color)
    }
}

@Composable
private fun RepeatIcon(
    color: Color,
    one: Boolean,
    modifier: Modifier
) {
    Box(
        modifier,
        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            Modifier.fillMaxSize()
        ) {
            val stroke =
                size.width * .08f

            drawLine(
                color,
                Offset(
                    size.width * .23f,
                    size.height * .30f
                ),
                Offset(
                    size.width * .73f,
                    size.height * .30f
                ),
                stroke,
                StrokeCap.Round
            )

            drawLine(
                color,
                Offset(
                    size.width * .77f,
                    size.height * .70f
                ),
                Offset(
                    size.width * .27f,
                    size.height * .70f
                ),
                stroke,
                StrokeCap.Round
            )

            val topArrow =
                Path().apply {
                    moveTo(
                        size.width * .70f,
                        size.height * .17f
                    )
                    lineTo(
                        size.width * .88f,
                        size.height * .30f
                    )
                    lineTo(
                        size.width * .70f,
                        size.height * .43f
                    )
                    close()
                }

            val bottomArrow =
                Path().apply {
                    moveTo(
                        size.width * .30f,
                        size.height * .57f
                    )
                    lineTo(
                        size.width * .12f,
                        size.height * .70f
                    )
                    lineTo(
                        size.width * .30f,
                        size.height * .83f
                    )
                    close()
                }

            drawPath(topArrow, color)
            drawPath(bottomArrow, color)
        }

        if (one) {
            Text(
                "1",
                color = color,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    7.sp
            )
        }
    }
}

@Composable
private fun QueueIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.height * .08f

        repeat(3) {
                i ->

            val y =
                size.height *
                    (
                        .27f +
                            i * .23f
                        )

            drawLine(
                color,
                Offset(
                    size.width * .16f,
                    y
                ),
                Offset(
                    size.width * .76f,
                    y
                ),
                stroke,
                StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ClockIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.width * .08f

        drawCircle(
            color = color,
            radius =
                size.minDimension * .37f,
            style =
                Stroke(stroke)
        )

        drawLine(
            color,
            center,
            Offset(
                center.x,
                center.y -
                    size.height * .20f
            ),
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            center,
            Offset(
                center.x +
                    size.width * .16f,
                center.y
            ),
            stroke,
            StrokeCap.Round
        )
    }
}

@Composable
private fun ShareIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.width * .075f

        val a =
            Offset(
                size.width * .27f,
                size.height * .50f
            )

        val b =
            Offset(
                size.width * .70f,
                size.height * .25f
            )

        val d =
            Offset(
                size.width * .70f,
                size.height * .75f
            )

        drawLine(
            color,
            a,
            b,
            stroke
        )

        drawLine(
            color,
            a,
            d,
            stroke
        )

        drawCircle(
            color,
            size.width * .10f,
            a
        )

        drawCircle(
            color,
            size.width * .10f,
            b
        )

        drawCircle(
            color,
            size.width * .10f,
            d
        )
    }
}

@Composable
private fun FullscreenIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.width * .08f

        val l =
            size.width * .24f

        drawLine(
            color,
            Offset(
                size.width * .18f,
                size.height * .38f
            ),
            Offset(
                size.width * .18f,
                size.height * .18f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .18f,
                size.height * .18f
            ),
            Offset(
                size.width * .38f,
                size.height * .18f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .62f,
                size.height * .18f
            ),
            Offset(
                size.width * .82f,
                size.height * .18f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .82f,
                size.height * .18f
            ),
            Offset(
                size.width * .82f,
                size.height * .38f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .18f,
                size.height * .62f
            ),
            Offset(
                size.width * .18f,
                size.height * .82f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .18f,
                size.height * .82f
            ),
            Offset(
                size.width * .38f,
                size.height * .82f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .62f,
                size.height * .82f
            ),
            Offset(
                size.width * .82f,
                size.height * .82f
            ),
            stroke
        )

        drawLine(
            color,
            Offset(
                size.width * .82f,
                size.height * .82f
            ),
            Offset(
                size.width * .82f,
                size.height * .62f
            ),
            stroke
        )
    }
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
            .coerceAtLeast(0L) /
            1000L

    val hours =
        total / 3600L

    val minutes =
        (
            total % 3600L
            ) / 60L

    val seconds =
        total % 60L

    return if (
        hours > 0L
    ) {
        "$hours:${
            minutes.toString()
                .padStart(2, '0')
        }:${
            seconds.toString()
                .padStart(2, '0')
        }"
    } else {
        "$minutes:${
            seconds.toString()
                .padStart(2, '0')
        }"
    }
}

private fun formatBytes(
    bytes: Long
): String =
    when {
        bytes >=
            1024L * 1024L * 1024L ->
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
            1024L * 1024L ->
            String.format(
                "%.1f MB",
                bytes.toDouble() /
                    (
                        1024.0 *
                            1024.0
                        )
            )

        bytes >= 1024L ->
            String.format(
                "%.1f KB",
                bytes.toDouble() /
                    1024.0
            )

        else ->
            "$bytes B"
    }
