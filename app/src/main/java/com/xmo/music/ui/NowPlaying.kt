package com.xmo.music.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
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
        state.currentIndex.takeIf { it in queue.indices }
            ?: queue.indexOfFirst { it.id == state.currentSongId }

    val currentSong = queue.getOrNull(index)
    val previousSong = queue.getOrNull(index - 1)
    val nextSong = queue.getOrNull(index + 1)

    var dominant by remember {
        mutableStateOf(Color(0xFF35353A))
    }

    LaunchedEffect(
        currentSong?.artwork,
        state.artworkUri
    ) {
        val uri =
            currentSong?.artwork
                ?: state.artworkUri?.let(Uri::parse)

        dominant =
            Artwork.cached(uri)
                ?: Artwork.color(context, uri)
    }

    val liveColor by animateColorAsState(
        targetValue = dominant,
        animationSpec = tween(420),
        label = "playerArtworkColor"
    )

    val deep = Artwork.deep(liveColor, theme)

    val overlayText =
        if (liveColor.luminance() > .56f) {
            Color(0xFF101010)
        } else {
            Color.White
        }

    /*
     * Main player is now deliberately much more transparent.
     */
    val panelBackground =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .22f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .25f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .40f)
        }

    val sectionBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(alpha = .14f)

            XmoTheme.Dark ->
                Color.White.copy(alpha = .15f)

            XmoTheme.Amoled ->
                Color.White.copy(alpha = .21f)
        }

    /*
     * =========================================================
     * REAL POSITION
     * =========================================================
     */

    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (true) {
            refreshPosition()

            delay(
                if (state.isPlaying) 250L
                else 500L
            )
        }
    }

    /*
     * =========================================================
     * LYRICS
     * =========================================================
     */

    var externalLyrics by remember(
        state.currentSongId,
        lyricsUri
    ) {
        mutableStateOf<SongLyrics?>(null)
    }

    LaunchedEffect(
        state.currentSongId,
        lyricsUri
    ) {
        externalLyrics =
            lyricsUri?.let {
                readLyrics(context, Uri.parse(it))
            }
    }

    val lyrics =
        externalLyrics
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

                saveLyricsUri(uri.toString())
            }
        }

    /*
     * =========================================================
     * OVERLAYS
     * =========================================================
     */

    var fullLyrics by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var queueOpen by remember { mutableStateOf(false) }
    var sleepOpen by remember { mutableStateOf(false) }

    /*
     * =========================================================
     * OPEN / DISMISS
     * =========================================================
     */

    val entrance = remember { Animatable(1f) }
    val dragY = remember { Animatable(0f) }

    var screenHeight by remember {
        mutableFloatStateOf(1f)
    }

    var dismissing by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        entrance.animateTo(0f, tween(410))
        onOpened()
    }

    suspend fun closePlayer() {
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
            fullLyrics -> fullLyrics = false
            menuOpen -> menuOpen = false
            queueOpen -> queueOpen = false
            sleepOpen -> sleepOpen = false

            else -> scope.launch {
                closePlayer()
            }
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .onSizeChanged {
                screenHeight =
                    it.height.toFloat()
                        .coerceAtLeast(1f)
            }
            .graphicsLayer {
                translationY =
                    dragY.value +
                        entrance.value * screenHeight
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        (
                            42f *
                                (
                                    dragY.value /
                                        screenHeight
                                    ).coerceIn(0f, 1f)
                            ).dp,
                    topEnd =
                        (
                            42f *
                                (
                                    dragY.value /
                                        screenHeight
                                    ).coerceIn(0f, 1f)
                            ).dp
                )
            )
    ) {
        ArtworkBackground(
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
             * HEADER
             * =================================================
             */

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(horizontal = 14.dp)
                    .playerDownGesture(
                        dragY = dragY,
                        screenHeight = screenHeight,
                        scope = scope,
                        dismiss = dismiss
                    )
            ) {
                PlayerCircle(
                    size = 39.dp,
                    background =
                        overlayText.copy(alpha = .10f),
                    modifier =
                        Modifier.align(
                            Alignment.CenterStart
                        ),
                    click = {
                        scope.launch {
                            closePlayer()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.KeyboardArrowDown,
                        contentDescription =
                            "Close",
                        tint = overlayText,
                        modifier =
                            Modifier.size(23.dp)
                    )
                }

                Column(
                    Modifier
                        .align(Alignment.Center)
                        .width(180.dp),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        if (sourceIsCategory) {
                            "PLAYING FROM CATEGORY"
                        } else {
                            "PLAYING FROM"
                        },
                        color =
                            overlayText.copy(alpha = .68f),
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp,
                        textAlign =
                            TextAlign.Center,
                        maxLines = 1
                    )

                    Text(
                        source,
                        color = overlayText,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 16.sp,
                        textAlign =
                            TextAlign.Center,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                /*
                 * No border. No separator.
                 */
                XmoCapsule(
                    background =
                        overlayText.copy(alpha = .10f),
                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd
                        )
                ) {
                    CapsuleButton(
                        click = {
                            currentSong?.let {
                                shareSong(context, it)
                            }
                        }
                    ) {
                        ShareIcon(
                            overlayText,
                            Modifier.size(18.dp)
                        )
                    }

                    CapsuleButton(
                        click = {
                            menuOpen = true
                        }
                    ) {
                        Icon(
                            imageVector =
                                Icons.Default.MoreVert,
                            contentDescription =
                                "More",
                            tint = overlayText,
                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }
            }

            /*
             * Cover intentionally lower.
             */
            Spacer(
                Modifier.height(68.dp)
            )

            ArtworkCarousel(
                currentId =
                    state.currentSongId,
                current =
                    currentSong?.artwork
                        ?: state.artworkUri?.let(Uri::parse),
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
                    next
            )

            /*
             * Finger-sized separation between artwork and panel.
             */
            Spacer(
                Modifier.height(52.dp)
            )

            /*
             * =================================================
             * BORDERLESS TRANSPARENT PANEL
             * =================================================
             */

            Column(
                Modifier
                    .padding(horizontal = 11.dp)
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .background(panelBackground)
                    .padding(
                        start = 18.dp,
                        top = 25.dp,
                        end = 18.dp,
                        bottom = 24.dp
                    )
            ) {
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
                    }

                    /*
                     * No border, no middle line.
                     */
                    XmoCapsule(
                        background = c.button
                    ) {
                        CapsuleButton(
                            click = toggleLike
                        ) {
                            XmoIcon(
                                icon =
                                    R.drawable.ic_xmo_heart,
                                tint =
                                    if (liked) accent
                                    else c.icon,
                                modifier =
                                    Modifier.size(17.dp)
                            )
                        }

                        CapsuleButton(
                            click = {
                                menuOpen = true
                            }
                        ) {
                            XmoIcon(
                                icon =
                                    R.drawable.ic_xmo_add,
                                tint = accent,
                                modifier =
                                    Modifier.size(16.dp)
                            )
                        }
                    }
                }

                /*
                 * More space between metadata and seek line.
                 */
                Spacer(
                    Modifier.height(30.dp)
                )

                SeekLine(
                    position = state.position,
                    duration = state.duration,
                    active = accent,
                    inactive = sectionBorder,
                    seekTo = seekTo
                )

                Spacer(
                    Modifier.height(3.dp)
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        playerTime(state.position),
                        color = c.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 12.sp
                    )

                    Text(
                        playerTime(state.duration),
                        color = c.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 12.sp
                    )
                }

                Spacer(
                    Modifier.height(24.dp)
                )

                /*
                 * =================================================
                 * CONTROLS
                 * =================================================
                 */

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    SmallControl(
                        active =
                            state.sleepTimerRemainingMs > 0L,
                        c = c,
                        click = {
                            sleepOpen = true
                        }
                    ) {
                        ClockIcon(
                            if (
                                state.sleepTimerRemainingMs > 0L
                            ) accent else c.icon,
                            Modifier.size(16.dp)
                        )
                    }

                    SmallControl(
                        active =
                            state.shuffleEnabled,
                        c = c,
                        click =
                            toggleShuffle
                    ) {
                        ShuffleIcon(
                            if (
                                state.shuffleEnabled
                            ) accent else c.icon,
                            Modifier.size(17.dp)
                        )
                    }

                    PlayerCircle(
                        size = 37.dp,
                        background = c.button,
                        click = previous
                    ) {
                        PreviousIcon(
                            c.text,
                            Modifier.size(19.dp)
                        )
                    }

                    PlayerCircle(
                        size = 48.dp,
                        background = accent,
                        click = togglePlay
                    ) {
                        if (state.isPlaying) {
                            PauseIcon(
                                Color.White,
                                Modifier.size(22.dp)
                            )
                        } else {
                            Icon(
                                imageVector =
                                    Icons.Default.PlayArrow,
                                contentDescription =
                                    "Play",
                                tint = Color.White,
                                modifier =
                                    Modifier.size(25.dp)
                            )
                        }
                    }

                    PlayerCircle(
                        size = 37.dp,
                        background = c.button,
                        enabled =
                            state.hasNext,
                        click = next
                    ) {
                        NextIcon(
                            if (state.hasNext) {
                                c.text
                            } else {
                                c.sub.copy(alpha = .35f)
                            },
                            Modifier.size(19.dp)
                        )
                    }

                    SmallControl(
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
                                ) accent else c.icon,
                            one =
                                state.repeatMode ==
                                    androidx.media3.common.Player
                                        .REPEAT_MODE_ONE,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    SmallControl(
                        active = false,
                        c = c,
                        click = {
                            queueOpen = true
                        }
                    ) {
                        QueueIcon(
                            c.icon,
                            Modifier.size(17.dp)
                        )
                    }
                }

                /*
                 * Larger controls -> lyrics separation.
                 */
                Spacer(
                    Modifier.height(48.dp)
                )

                /*
                 * =================================================
                 * LYRICS
                 * =================================================
                 */

                BorderSection(
                    title = "LYRICS",
                    c = c,
                    border = sectionBorder,
                    actions = {
                        XmoCapsule(
                            background = c.button
                        ) {
                            CapsuleButton(
                                click = {
                                    lyricPicker.launch(
                                        arrayOf("*/*")
                                    )
                                }
                            ) {
                                Text(
                                    "+",
                                    color = accent,
                                    fontFamily =
                                        XmoFont.medium,
                                    fontSize = 20.sp,
                                    textAlign =
                                        TextAlign.Center
                                )
                            }

                            CapsuleButton(
                                click = {
                                    fullLyrics = true
                                }
                            ) {
                                FullscreenIcon(
                                    c.text,
                                    Modifier.size(17.dp)
                                )
                            }
                        }
                    }
                ) {
                    MovingLyrics(
                        lyrics = lyrics,
                        position = state.position,
                        c = c,
                        accent = accent
                    )
                }

                Spacer(
                    Modifier.height(20.dp)
                )

                BorderSection(
                    title = "ARTIST",
                    c = c,
                    border = sectionBorder
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
                                    .clip(CircleShape)
                                    .background(c.button),
                            contentScale =
                                ContentScale.Crop
                        )

                        Column(
                            Modifier.padding(start = 12.dp)
                        ) {
                            Text(
                                state.artist.ifBlank {
                                    "Unknown artist"
                                },
                                color = c.text,
                                fontFamily =
                                    XmoFont.bold,
                                fontSize = 14.sp
                            )

                            currentSong
                                ?.albumArtist
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
                                        fontSize = 10.sp
                                    )
                                }
                        }
                    }
                }

                Spacer(
                    Modifier.height(20.dp)
                )

                BorderSection(
                    title = "SONG DETAILS",
                    c = c,
                    border = sectionBorder
                ) {
                    DetailRow(
                        "Album",
                        state.album,
                        c
                    )

                    currentSong?.metadata?.let { meta ->
                        meta.genre?.let {
                            DetailRow("Genre", it, c)
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
                        fontSize = 19.sp
                    )

                    Text(
                        "lxzrvi • copyright © 2026",
                        color = c.sub,
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 9.sp
                    )
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(18.dp)
                )
            }
        }

        if (queueOpen) {
            QueueOverlay(
                queue = queue,
                currentId =
                    state.currentSongId,
                c = c,
                close = {
                    queueOpen = false
                }
            )
        }

        if (sleepOpen) {
            SleepOverlay(
                c = c,
                active =
                    state.sleepTimerRemainingMs > 0L,
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

        if (menuOpen) {
            MoreOverlay(
                c = c,
                song = currentSong,
                liked = liked,
                close = {
                    menuOpen = false
                },
                toggleLike = {
                    toggleLike()
                    menuOpen = false
                },
                share = {
                    currentSong?.let {
                        shareSong(context, it)
                    }
                    menuOpen = false
                },
                removeLyrics = {
                    saveLyricsUri(null)
                    menuOpen = false
                }
            )
        }

        if (fullLyrics) {
            FullLyrics(
                lyrics = lyrics,
                position = state.position,
                title = state.title,
                artist = state.artist,
                artwork =
                    currentSong?.artwork,
                dominant = liveColor,
                deep = deep,
                theme = theme,
                c = c,
                accent = accent,
                isPlaying = state.isPlaying,
                togglePlay = togglePlay,
                previous = previous,
                next = next,
                close = {
                    fullLyrics = false
                }
            )
        }
    }
}

/*
 * =============================================================
 * DOWN GESTURE
 * =============================================================
 */

private fun Modifier.playerDownGesture(
    dragY: Animatable<Float, *>,
    screenHeight: Float,
    scope: kotlinx.coroutines.CoroutineScope,
    dismiss: () -> Unit
): Modifier =
    pointerInput(screenHeight) {
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
                                dragY.value + amount
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
                        dragY.animateTo(
                            screenHeight,
                            tween(300)
                        )
                        dismiss()
                    } else {
                        dragY.animateTo(
                            0f,
                            tween(220)
                        )
                    }
                }
            },
            onDragCancel = {
                scope.launch {
                    dragY.animateTo(
                        0f,
                        tween(220)
                    )
                }
            }
        )
    }

/*
 * =============================================================
 * EVEN ARTWORK BACKGROUND
 * =============================================================
 */

@Composable
private fun ArtworkBackground(
    dominant: Color,
    deep: Color,
    theme: XmoTheme
) {
    val neutral =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .08f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .08f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .13f)
        }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(dominant)
    ) {
        drawRect(
            Brush.radialGradient(
                listOf(
                    dominant,
                    Color.Transparent
                ),
                Offset(
                    size.width * .08f,
                    size.height * .10f
                ),
                size.width * 1.20f
            )
        )

        drawRect(
            Brush.radialGradient(
                listOf(
                    deep.copy(alpha = .64f),
                    Color.Transparent
                ),
                Offset(
                    size.width * .95f,
                    size.height * .31f
                ),
                size.width * 1.16f
            )
        )

        drawRect(
            Brush.radialGradient(
                listOf(
                    dominant.copy(alpha = .78f),
                    Color.Transparent
                ),
                Offset(
                    size.width * .10f,
                    size.height * .64f
                ),
                size.width * 1.18f
            )
        )

        drawRect(
            Brush.radialGradient(
                listOf(
                    deep.copy(alpha = .62f),
                    Color.Transparent
                ),
                Offset(
                    size.width * .92f,
                    size.height * .88f
                ),
                size.width * 1.17f
            )
        )

        drawRect(
            neutral
        )
    }
}

/*
 * =============================================================
 * COVER CAROUSEL
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
    previousSong: () -> Unit,
    nextSong: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val x = remember { Animatable(0f) }

    var pending by remember {
        mutableIntStateOf(0)
    }

    var oldId by remember {
        mutableStateOf<Long?>(null)
    }

    LaunchedEffect(currentId) {
        if (
            pending != 0 &&
            oldId != null &&
            currentId != oldId
        ) {
            x.snapTo(0f)
            pending = 0
            oldId = null
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(382.dp)
    ) {
        val width =
            constraints.maxWidth.toFloat()

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

                            scope.launch {
                                var target =
                                    x.value + amount

                                if (
                                    target < 0f &&
                                    !canNext
                                ) {
                                    target =
                                        x.value +
                                            amount * .18f
                                }

                                if (
                                    target > 0f &&
                                    !canPrevious
                                ) {
                                    target =
                                        x.value +
                                            amount * .18f
                                }

                                x.snapTo(
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
                                    x.value <
                                        -width * .15f &&
                                        canNext -> {

                                        oldId = currentId
                                        pending = 1
                                        nextSong()

                                        x.animateTo(
                                            -width,
                                            tween(230)
                                        )
                                    }

                                    x.value >
                                        width * .15f &&
                                        canPrevious -> {

                                        oldId = currentId
                                        pending = -1
                                        previousSong()

                                        x.animateTo(
                                            width,
                                            tween(230)
                                        )
                                    }

                                    else ->
                                        x.animateTo(
                                            0f,
                                            tween(190)
                                        )
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                if (pending == 0) {
                                    x.animateTo(
                                        0f,
                                        tween(180)
                                    )
                                }
                            }
                        }
                    )
                }
        ) {
            previous?.let {
                Cover(
                    it,
                    Modifier
                        .padding(horizontal = 17.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationX =
                                x.value - width
                        }
                )
            }

            Cover(
                current,
                Modifier
                    .padding(horizontal = 17.dp)
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationX = x.value
                    }
            )

            next?.let {
                Cover(
                    it,
                    Modifier
                        .padding(horizontal = 17.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationX =
                                x.value + width
                        }
                )
            }
        }
    }
}

@Composable
private fun Cover(
    uri: Uri?,
    modifier: Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                Color.Black.copy(alpha = .08f)
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

        if (uri == null) {
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
                    fontSize = 31.sp
                )
            }
        }
    }
}

/*
 * =============================================================
 * XMO CAPSULE — NO BORDER / NO DIVIDER
 * =============================================================
 */

@Composable
private fun XmoCapsule(
    background: Color,
    modifier: Modifier = Modifier,
    content:
        @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(background)
            .padding(horizontal = 2.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun CapsuleButton(
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(39.dp)
            .clip(CircleShape)
            .clickable(onClick = click),
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

/*
 * =============================================================
 * XMO LONG CAP
 *
 * Play/Pause | Next | Previous | Close
 * =============================================================
 */

@Composable
private fun XmoLongCap(
    isPlaying: Boolean,
    foreground: Color,
    background: Color,
    togglePlay: () -> Unit,
    next: () -> Unit,
    previous: () -> Unit,
    close: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(background)
            .padding(horizontal = 3.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        LongCapButton(togglePlay) {
            if (isPlaying) {
                PauseIcon(
                    foreground,
                    Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector =
                        Icons.Default.PlayArrow,
                    contentDescription =
                        "Play",
                    tint = foreground,
                    modifier =
                        Modifier.size(21.dp)
                )
            }
        }

        LongCapButton(next) {
            NextIcon(
                foreground,
                Modifier.size(17.dp)
            )
        }

        LongCapButton(previous) {
            PreviousIcon(
                foreground,
                Modifier.size(17.dp)
            )
        }

        LongCapButton(close) {
            Icon(
                imageVector =
                    Icons.Default.Close,
                contentDescription =
                    "Close",
                tint = foreground,
                modifier =
                    Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun LongCapButton(
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = click),
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

/*
 * =============================================================
 * BASIC CONTROLS
 * =============================================================
 */

@Composable
private fun PlayerCircle(
    size: androidx.compose.ui.unit.Dp,
    background: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
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
private fun SmallControl(
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
                    LocalXmoAccent.current
                        .copy(alpha = .13f)
                } else {
                    c.button
                }
            )
            .clickable(onClick = click),
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun SeekLine(
    position: Long,
    duration: Long,
    active: Color,
    inactive: Color,
    seekTo: (Long) -> Unit
) {
    val progress =
        if (duration > 0L) {
            (
                position.toFloat() /
                    duration.toFloat()
                ).coerceIn(0f, 1f)
        } else {
            0f
        }

    Box(
        Modifier
            .fillMaxWidth()
            .height(20.dp)
            .pointerInput(duration) {
                detectTapGestures {
                    if (duration > 0L) {
                        val p =
                            (
                                it.x / size.width
                                ).coerceIn(0f, 1f)

                        seekTo(
                            (duration * p).toLong()
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
                .background(inactive)
        )

        Box(
            Modifier
                .fillMaxWidth(progress)
                .height(2.dp)
                .background(active)
        )
    }
}

/*
 * =============================================================
 * LYRICS / ARTIST / DETAILS BORDER
 * =============================================================
 */

@Composable
private fun BorderSection(
    title: String,
    c: HomeColors,
    border: Color,
    actions:
        @Composable RowScope.() -> Unit = {},
    content:
        @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp)
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
                fontSize = 11.sp,
                letterSpacing = 1.1.sp,
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
 *
 * Previous rises.
 * Current zooms/focuses at center.
 * Next enters smoothly from bottom.
 * Only lyric text area scrolls for plain lyrics.
 * =============================================================
 */

@Composable
private fun MovingLyrics(
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
                .height(240.dp),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                "No local lyrics found.\nTap + to select an LRC file.",
                color = c.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                textAlign =
                    TextAlign.Center
            )
        }

        return
    }

    val active =
        currentLyricIndex(
            lyrics,
            position
        )

    if (
        lyrics.synced &&
        active >= 0
    ) {
        val previous =
            lyrics.lines
                .getOrNull(active - 1)
                ?.text
                .orEmpty()

        val current =
            lyrics.lines
                .getOrNull(active)
                ?.text
                .orEmpty()

        val next =
            lyrics.lines
                .getOrNull(active + 1)
                ?.text
                .orEmpty()

        Box(
            Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment =
                Alignment.Center
        ) {
            AnimatedContent(
                targetState = active,
                transitionSpec = {
                    (
                        slideInVertically(
                            animationSpec = tween(330)
                        ) {
                            it / 3
                        } +
                            fadeIn(tween(280))
                        ) togetherWith
                        (
                            slideOutVertically(
                                animationSpec = tween(330)
                            ) {
                                -it / 3
                            } +
                                fadeOut(tween(210))
                            )
                },
                label = "lyricsMotion"
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.Center
                ) {
                    Text(
                        previous,
                        color =
                            c.text.copy(alpha = .38f),
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        textAlign =
                            TextAlign.Center,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis,
                        modifier =
                            Modifier.fillMaxWidth()
                    )

                    Spacer(
                        Modifier.height(19.dp)
                    )

                    /*
                     * CURRENT — bigger, focused, centered.
                     */
                    Text(
                        current,
                        color = accent,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 23.sp,
                        lineHeight = 30.sp,
                        textAlign =
                            TextAlign.Center,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .graphicsLayer {
                                    scaleX = 1.035f
                                    scaleY = 1.035f
                                }
                    )

                    Spacer(
                        Modifier.height(19.dp)
                    )

                    Text(
                        next,
                        color =
                            c.text.copy(alpha = .46f),
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 17.sp,
                        lineHeight = 24.sp,
                        textAlign =
                            TextAlign.Center,
                        maxLines = 2,
                        overflow =
                            TextOverflow.Ellipsis,
                        modifier =
                            Modifier.fillMaxWidth()
                    )
                }
            }
        }

        return
    }

    /*
     * Only text body scrolls.
     */
    Column(
        Modifier
            .fillMaxWidth()
            .height(250.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {
        lyrics.lines.forEach {
            Text(
                it.text,
                color =
                    c.text.copy(alpha = .78f),
                fontFamily =
                    XmoFont.normal,
                fontSize = 17.sp,
                lineHeight = 25.sp,
                textAlign =
                    TextAlign.Center,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }
    }
}

/*
 * =============================================================
 * FULL SCREEN LYRICS
 * =============================================================
 */

@Composable
private fun FullLyrics(
    lyrics: SongLyrics?,
    position: Long,
    title: String,
    artist: String,
    artwork: Uri?,
    dominant: Color,
    deep: Color,
    theme: XmoTheme,
    c: HomeColors,
    accent: Color,
    isPlaying: Boolean,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    close: () -> Unit
) {
    /*
     * Text contrast follows live artwork background.
     */
    val foreground =
        if (dominant.luminance() > .56f) {
            Color(0xFF101010)
        } else {
            Color.White
        }

    val active =
        lyrics?.let {
            currentLyricIndex(
                it,
                position
            )
        } ?: -1

    Box(
        Modifier.fillMaxSize()
    ) {
        ArtworkBackground(
            dominant = dominant,
            deep = deep,
            theme = theme
        )

        /*
         * Header.
         */
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(70.dp)
                .padding(
                    start = 16.dp,
                    end = 14.dp
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
                    .padding(start = 10.dp)
            ) {
                Text(
                    title,
                    color = foreground,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    artist,
                    color =
                        foreground.copy(alpha = .62f),
                    fontFamily =
                        XmoFont.normal,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }

            /*
             * XmoLongCap on right.
             */
            XmoLongCap(
                isPlaying = isPlaying,
                foreground = foreground,
                background =
                    foreground.copy(alpha = .10f),
                togglePlay = togglePlay,
                next = next,
                previous = previous,
                close = close
            )
        }

        /*
         * Main centered lyrics area.
         */
        Box(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    start = 24.dp,
                    top = 100.dp,
                    end = 24.dp,
                    bottom = 36.dp
                ),
            contentAlignment =
                Alignment.Center
        ) {
            if (
                lyrics == null ||
                lyrics.lines.isEmpty()
            ) {
                Text(
                    "No lyrics",
                    color =
                        foreground.copy(alpha = .60f),
                    fontFamily =
                        XmoFont.normal,
                    fontSize = 17.sp
                )
            } else if (
                lyrics.synced &&
                active >= 0
            ) {
                val previousLine =
                    lyrics.lines
                        .getOrNull(active - 1)
                        ?.text
                        .orEmpty()

                val currentLine =
                    lyrics.lines
                        .getOrNull(active)
                        ?.text
                        .orEmpty()

                val nextLine =
                    lyrics.lines
                        .getOrNull(active + 1)
                        ?.text
                        .orEmpty()

                AnimatedContent(
                    targetState = active,
                    transitionSpec = {
                        (
                            slideInVertically(
                                tween(360)
                            ) {
                                it / 3
                            } +
                                fadeIn(tween(300))
                            ) togetherWith
                            (
                                slideOutVertically(
                                    tween(360)
                                ) {
                                    -it / 3
                                } +
                                    fadeOut(tween(230))
                                )
                    },
                    label = "fullscreenLyricMotion"
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment =
                            Alignment.CenterHorizontally,
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Text(
                            previousLine,
                            color =
                                foreground.copy(
                                    alpha = .38f
                                ),
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            textAlign =
                                TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
                        )

                        Spacer(
                            Modifier.height(30.dp)
                        )

                        Text(
                            currentLine,
                            color = foreground,
                            fontFamily =
                                XmoFont.bold,
                            fontSize = 29.sp,
                            lineHeight = 37.sp,
                            textAlign =
                                TextAlign.Center,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        scaleX = 1.045f
                                        scaleY = 1.045f
                                    }
                        )

                        Spacer(
                            Modifier.height(30.dp)
                        )

                        Text(
                            nextLine,
                            color =
                                foreground.copy(
                                    alpha = .48f
                                ),
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            textAlign =
                                TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
                        )
                    }
                }
            } else {
                /*
                 * Untimed full lyrics: only this text body scrolls.
                 */
                Column(
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    horizontalAlignment =
                        Alignment.CenterHorizontally,
                    verticalArrangement =
                        Arrangement.spacedBy(16.dp)
                ) {
                    lyrics.lines.forEach {
                        Text(
                            it.text,
                            color =
                                foreground.copy(
                                    alpha = .78f
                                ),
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 20.sp,
                            lineHeight = 29.sp,
                            textAlign =
                                TextAlign.Center,
                            modifier =
                                Modifier.fillMaxWidth()
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
    if (value.isBlank()) return

    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        Text(
            label,
            color = c.sub,
            fontFamily =
                XmoFont.normal,
            fontSize = 11.sp,
            modifier =
                Modifier.width(92.dp)
        )

        Text(
            value,
            color = c.text,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
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
    close: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = .58f)
            )
            .clickable(onClick = close),
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
                .clickable {}
                .padding(16.dp)
        ) {
            Text(
                "QUEUE",
                color =
                    LocalXmoAccent.current,
                fontFamily =
                    XmoFont.bold,
                fontSize = 11.sp
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
                    key = { _, song ->
                        song.id
                    }
                ) { _, song ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(
                                RoundedCornerShape(12.dp)
                            )
                            .background(
                                if (
                                    song.id == currentId
                                ) {
                                    LocalXmoAccent.current
                                        .copy(alpha = .13f)
                                } else {
                                    c.button
                                }
                            )
                            .padding(5.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = song.artwork,
                            contentDescription = null,
                            modifier =
                                Modifier
                                    .size(46.dp)
                                    .clip(
                                        RoundedCornerShape(8.dp)
                                    ),
                            contentScale =
                                ContentScale.Crop
                        )

                        Column(
                            Modifier
                                .weight(1f)
                                .padding(start = 10.dp)
                        ) {
                            Text(
                                song.title,
                                color =
                                    if (
                                        song.id == currentId
                                    ) {
                                        LocalXmoAccent.current
                                    } else {
                                        c.text
                                    },
                                fontFamily =
                                    XmoFont.bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Text(
                                song.artist,
                                color = c.sub,
                                fontFamily =
                                    XmoFont.normal,
                                fontSize = 9.sp,
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
 * POPUPS
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
    Popup(
        title = "Sleep Timer",
        c = c,
        close = close
    ) {
        listOf(
            15L to "15 minutes",
            30L to "30 minutes",
            45L to "45 minutes",
            60L to "1 hour"
        ).forEach { (minutes, label) ->
            PopupRow(label, c) {
                select(
                    minutes * 60_000L
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
    Popup(
        title =
            song?.title ?: "Song",
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
private fun Popup(
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
                Color.Black.copy(alpha = .56f)
            )
            .clickable(onClick = close),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(22.dp)
                )
                .background(c.surface)
                .clickable {}
                .padding(17.dp)
        ) {
            Text(
                title,
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 16.sp,
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
        fontSize = 12.sp,
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = click)
                .padding(
                    horizontal = 8.dp,
                    vertical = 11.dp
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
        Intent(Intent.ACTION_SEND).apply {
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
    withContext(Dispatchers.IO) {
        runCatching {
            val source =
                context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use { it.readText() }
                    ?: return@runCatching null

            parseLyrics(source)
        }.getOrNull()
    }

private fun parseLyrics(
    source: String
): SongLyrics {
    val output =
        mutableListOf<LyricLine>()

    val timestamp =
        Regex(
            """\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]"""
        )

    val metadata =
        Regex(
            """\[(ar|ti|al|by|offset|re|ve):.*]""",
            RegexOption.IGNORE_CASE
        )

    source.lineSequence().forEach { raw ->
        val stamps =
            timestamp.findAll(raw).toList()

        if (stamps.isNotEmpty()) {
            val text =
                timestamp
                    .replace(raw, "")
                    .trim()

            if (text.isNotEmpty()) {
                stamps.forEach { match ->
                    val min =
                        match.groupValues[1]
                            .toLongOrNull() ?: 0L

                    val sec =
                        match.groupValues[2]
                            .toLongOrNull() ?: 0L

                    val fractionRaw =
                        match.groupValues[3]

                    val fraction =
                        when (fractionRaw.length) {
                            1 ->
                                (
                                    fractionRaw
                                        .toLongOrNull() ?: 0L
                                    ) * 100L

                            2 ->
                                (
                                    fractionRaw
                                        .toLongOrNull() ?: 0L
                                    ) * 10L

                            3 ->
                                fractionRaw
                                    .toLongOrNull() ?: 0L

                            else -> 0L
                        }

                    output +=
                        LyricLine(
                            timeMs =
                                min * 60_000L +
                                    sec * 1_000L +
                                    fraction,
                            text = text
                        )
                }
            }
        } else {
            val text = raw.trim()

            if (
                text.isNotEmpty() &&
                !metadata.matches(text)
            ) {
                output +=
                    LyricLine(
                        timeMs = null,
                        text = text
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
                output.sortedBy {
                    it.timeMs
                        ?: Long.MAX_VALUE
                }
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
    if (!lyrics.synced) return -1

    var active = -1

    lyrics.lines.forEachIndexed {
            index,
            line ->

        val time =
            line.timeMs
                ?: return@forEachIndexed

        if (time <= position) {
            active = index
        }
    }

    return active
}

/*
 * =============================================================
 * ICONS
 * =============================================================
 */

@Composable
private fun PreviousIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val p = Path().apply {
            moveTo(
                size.width * .73f,
                size.height * .17f
            )
            lineTo(
                size.width * .32f,
                size.height * .50f
            )
            lineTo(
                size.width * .73f,
                size.height * .83f
            )
            close()
        }

        drawPath(p, color)

        drawRoundRect(
            color,
            Offset(
                size.width * .20f,
                size.height * .20f
            ),
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
        val p = Path().apply {
            moveTo(
                size.width * .27f,
                size.height * .17f
            )
            lineTo(
                size.width * .68f,
                size.height * .50f
            )
            lineTo(
                size.width * .27f,
                size.height * .83f
            )
            close()
        }

        drawPath(p, color)

        drawRoundRect(
            color,
            Offset(
                size.width * .70f,
                size.height * .20f
            ),
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
        val w = size.width * .18f
        val h = size.height * .62f
        val top = (size.height - h) / 2f

        drawRoundRect(
            color,
            Offset(
                size.width * .27f,
                top
            ),
            Size(w, h),
            CornerRadius(w / 2f)
        )

        drawRoundRect(
            color,
            Offset(
                size.width * .55f,
                top
            ),
            Size(w, h),
            CornerRadius(w / 2f)
        )
    }
}

@Composable
private fun ShuffleIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val s = size.width * .08f

        drawLine(
            color,
            Offset(
                size.width * .14f,
                size.height * .28f
            ),
            Offset(
                size.width * .38f,
                size.height * .28f
            ),
            s,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .38f,
                size.height * .28f
            ),
            Offset(
                size.width * .82f,
                size.height * .72f
            ),
            s,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .14f,
                size.height * .72f
            ),
            Offset(
                size.width * .38f,
                size.height * .72f
            ),
            s,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .38f,
                size.height * .72f
            ),
            Offset(
                size.width * .82f,
                size.height * .28f
            ),
            s,
            StrokeCap.Round
        )
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
            val s = size.width * .08f

            drawLine(
                color,
                Offset(
                    size.width * .18f,
                    size.height * .30f
                ),
                Offset(
                    size.width * .82f,
                    size.height * .30f
                ),
                s,
                StrokeCap.Round
            )

            drawLine(
                color,
                Offset(
                    size.width * .82f,
                    size.height * .70f
                ),
                Offset(
                    size.width * .18f,
                    size.height * .70f
                ),
                s,
                StrokeCap.Round
            )
        }

        if (one) {
            Text(
                "1",
                color = color,
                fontFamily =
                    XmoFont.bold,
                fontSize = 7.sp
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
        repeat(3) { i ->
            val y =
                size.height *
                    (.27f + i * .23f)

            drawLine(
                color,
                Offset(
                    size.width * .16f,
                    y
                ),
                Offset(
                    size.width * .82f,
                    y
                ),
                size.height * .075f,
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
        val s = size.width * .075f

        drawCircle(
            color,
            size.minDimension * .37f,
            style = Stroke(s)
        )

        drawLine(
            color,
            center,
            Offset(
                center.x,
                center.y - size.height * .19f
            ),
            s,
            StrokeCap.Round
        )

        drawLine(
            color,
            center,
            Offset(
                center.x + size.width * .15f,
                center.y
            ),
            s,
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
        val s = size.width * .07f

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

        drawLine(color, a, b, s)
        drawLine(color, a, d, s)

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
        val s = size.width * .075f

        fun line(
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float
        ) {
            drawLine(
                color,
                Offset(
                    size.width * x1,
                    size.height * y1
                ),
                Offset(
                    size.width * x2,
                    size.height * y2
                ),
                s,
                StrokeCap.Round
            )
        }

        line(.18f, .38f, .18f, .18f)
        line(.18f, .18f, .38f, .18f)
        line(.62f, .18f, .82f, .18f)
        line(.82f, .18f, .82f, .38f)
        line(.18f, .62f, .18f, .82f)
        line(.18f, .82f, .38f, .82f)
        line(.62f, .82f, .82f, .82f)
        line(.82f, .82f, .82f, .62f)
    }
}

private fun playerTime(
    milliseconds: Long
): String {
    val total =
        milliseconds.coerceAtLeast(0L) /
            1000L

    val hours = total / 3600L
    val minutes = (total % 3600L) / 60L
    val seconds = total % 60L

    return if (hours > 0L) {
        "$hours:${
            minutes.toString().padStart(2, '0')
        }:${
            seconds.toString().padStart(2, '0')
        }"
    } else {
        "$minutes:${
            seconds.toString().padStart(2, '0')
        }"
    }
}

private fun formatBytes(
    bytes: Long
): String =
    when {
        bytes >= 1024L * 1024L * 1024L ->
            String.format(
                "%.2f GB",
                bytes.toDouble() /
                    (1024.0 * 1024.0 * 1024.0)
            )

        bytes >= 1024L * 1024L ->
            String.format(
                "%.1f MB",
                bytes.toDouble() /
                    (1024.0 * 1024.0)
            )

        bytes >= 1024L ->
            String.format(
                "%.1f KB",
                bytes.toDouble() / 1024.0
            )

        else -> "$bytes B"
    }
