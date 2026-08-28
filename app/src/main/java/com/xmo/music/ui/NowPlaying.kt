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
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
                ?: Artwork.color(
                    context,
                    uri
                )
    }

    val liveColor by animateColorAsState(
        targetValue = dominant,
        animationSpec = tween(400),
        label = "artColor"
    )

    val deep =
        Artwork.deep(
            liveColor,
            theme
        )

    val headerColor =
        if (liveColor.luminance() > .55f) {
            Color(0xFF101010)
        } else {
            Color.White
        }

    val headerSub =
        headerColor.copy(alpha = .68f)

    val panelBackground =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .90f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .90f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .80f)
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

    var fileLyrics by remember(
        state.currentSongId,
        lyricsUri
    ) {
        mutableStateOf<SongLyrics?>(null)
    }

    LaunchedEffect(
        state.currentSongId,
        lyricsUri
    ) {
        fileLyrics =
            lyricsUri?.let {
                readLyrics(
                    context,
                    Uri.parse(it)
                )
            }
    }

    val lyrics =
        fileLyrics ?: currentSong?.embeddedLyrics

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

                saveLyricsUri(
                    uri.toString()
                )
            }
        }

    var fullLyrics by remember {
        mutableStateOf(false)
    }

    var menuOpen by remember {
        mutableStateOf(false)
    }

    var queueOpen by remember {
        mutableStateOf(false)
    }

    var sleepOpen by remember {
        mutableStateOf(false)
    }

    /*
     * =========================================================
     * OPEN / CLOSE
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
                    closePlayer()
                }
        }
    }

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
                    .pointerInput(screenHeight) {
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
                    }
            ) {
                PlayerCircle(
                    size = 39.dp,
                    background =
                        headerColor.copy(alpha = .10f),
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

                        tint =
                            headerColor,

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
                        text =
                            if (sourceIsCategory) {
                                "PLAYING FROM CATEGORY"
                            } else {
                                "PLAYING FROM"
                            },

                        color =
                            headerSub,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            12.sp,

                        letterSpacing =
                            1.sp,

                        textAlign =
                            TextAlign.Center,

                        maxLines =
                            1
                    )

                    Text(
                        text = source,

                        color =
                            headerColor,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            16.sp,

                        textAlign =
                            TextAlign.Center,

                        maxLines =
                            1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                XmoCapsule(
                    background =
                        headerColor.copy(alpha = .10f),

                    border =
                        headerColor.copy(alpha = .19f),

                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd
                        )
                ) {
                    CapsuleButton(
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
                            headerColor,
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

                            tint =
                                headerColor,

                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(64.dp)
            )

            /*
             * =================================================
             * COVER
             * =================================================
             */

            ArtworkCarousel(
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

                dragY =
                    dragY,

                screenHeight =
                    screenHeight,

                dismiss = {
                    dismissing = true
                    dismiss()
                }
            )

            Spacer(
                Modifier.height(36.dp)
            )

            /*
             * =================================================
             * BORDERLESS PLAYER PANEL
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
                    .background(
                        panelBackground
                    )
                    .padding(
                        start = 18.dp,
                        top = 24.dp,
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

                            fontSize =
                                21.sp,

                            maxLines =
                                1,

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

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    XmoCapsule(
                        background = c.button,
                        border = sectionBorder
                    ) {
                        CapsuleButton(
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

                        CapsuleButton(
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
                    Modifier.height(22.dp)
                )

                SeekLine(
                    position =
                        state.position,

                    duration =
                        state.duration,

                    active =
                        accent,

                    inactive =
                        sectionBorder,

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
                 * CONTROL ROW
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
                            ) {
                                accent
                            } else {
                                c.icon
                            },

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
                            ) {
                                accent
                            } else {
                                c.icon
                            },

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
                        if (
                            state.isPlaying
                        ) {
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

                                tint =
                                    Color.White,

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
                            if (
                                state.hasNext
                            ) {
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

                Spacer(
                    Modifier.height(36.dp)
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
                            background = c.button,
                            border = sectionBorder
                        ) {
                            CapsuleButton(
                                click = {
                                    /*
                                     * LRC MIME varies between
                                     * document providers.
                                     */
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

                                    fontSize =
                                        20.sp,

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
                    CenterLyrics(
                        lyrics = lyrics,
                        position =
                            state.position,
                        c = c,
                        accent = accent
                    )
                }

                Spacer(
                    Modifier.height(20.dp)
                )

                /*
                 * =================================================
                 * ARTIST
                 * =================================================
                 */

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

                                        fontSize =
                                            10.sp
                                    )
                                }
                        }
                    }
                }

                Spacer(
                    Modifier.height(20.dp)
                )

                /*
                 * =================================================
                 * DETAILS
                 * =================================================
                 */

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

        if (fullLyrics) {
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
                deep = deep,
                theme = theme,
                c = c,
                accent = accent,
                close = {
                    fullLyrics = false
                }
            )
        }
    }
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
    val bottom =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .18f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .16f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .23f)
        }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(dominant)
    ) {
        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant,
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .04f,
                        size.height * .10f
                    ),
                radius =
                    size.width * 1.18f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        deep.copy(alpha = .68f),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .96f,
                        size.height * .28f
                    ),
                radius =
                    size.width * 1.13f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant.copy(alpha = .78f),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .05f,
                        size.height * .61f
                    ),
                radius =
                    size.width * 1.12f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        deep.copy(alpha = .60f),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .98f,
                        size.height * .82f
                    ),
                radius =
                    size.width * 1.14f
            )
        )

        drawRect(
            Brush.verticalGradient(
                listOf(
                    Color.Transparent,
                    Color.Transparent,
                    bottom
                )
            )
        )
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
    previousSong: () -> Unit,
    nextSong: () -> Unit,
    dragY: Animatable<Float, *>,
    screenHeight: Float,
    dismiss: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val x =
        remember {
            Animatable(0f)
        }

    var oldId by remember {
        mutableStateOf<Long?>(null)
    }

    var pending by remember {
        mutableIntStateOf(0)
    }

    var rawX by remember {
        mutableFloatStateOf(0f)
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
                /*
                 * Horizontal swipe belongs to carousel.
                 */
                .pointerInput(
                    currentId,
                    canPrevious,
                    canNext
                ) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            rawX = 0f
                        },

                        onHorizontalDrag = {
                                change,
                                amount ->

                            if (pending != 0) {
                                return@detectHorizontalDragGestures
                            }

                            change.consume()

                            rawX += amount

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

                                        /*
                                         * Request real song first.
                                         * Background/metadata can begin
                                         * changing while cover leaves.
                                         */
                                        nextSong()

                                        x.animateTo(
                                            -width,
                                            tween(235)
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
                                            tween(235)
                                        )
                                    }

                                    else -> {
                                        x.animateTo(
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
                                if (pending == 0) {
                                    x.animateTo(0f)
                                }
                            }
                        }
                    )
                }
                /*
                 * Downward drag works from artwork too.
                 */
                .pointerInput(screenHeight) {
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
                }
        ) {
            if (previous != null) {
                Cover(
                    uri = previous,
                    modifier =
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
                uri = current,
                modifier =
                    Modifier
                        .padding(horizontal = 17.dp)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationX = x.value
                        }
            )

            if (next != null) {
                Cover(
                    uri = next,
                    modifier =
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
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(
                Color.Black.copy(alpha = .10f)
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
 * =============================================================
 */

@Composable
private fun XmoCapsule(
    background: Color,
    border: Color,
    modifier: Modifier = Modifier,
    content:
        @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(background)
            .border(
                .65.dp,
                border,
                RoundedCornerShape(22.dp)
            )
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
 * CONTROLS
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
                                it.x /
                                    size.width
                                ).coerceIn(
                                0f,
                                1f
                            )

                        seekTo(
                            (
                                duration * p
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
 * BORDER SECTIONS
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
 * CENTER-FOLLOW LYRICS
 * =============================================================
 */

@Composable
private fun CenterLyrics(
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
                .height(230.dp),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                "No local lyrics found.\nTap + to select an LRC file.",
                color = c.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    15.sp,
                lineHeight =
                    23.sp,
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

    /*
     * Synced LRC:
     * current line remains physically centered.
     * Previous and next lines surround it.
     */
    if (lyrics.synced && active >= 0) {
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

        Column(
            Modifier
                .fillMaxWidth()
                .height(240.dp),
            verticalArrangement =
                Arrangement.Center,
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                previous,
                color =
                    c.text.copy(alpha = .36f),
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    16.sp,
                lineHeight =
                    22.sp,
                textAlign =
                    TextAlign.Center,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis,
                modifier =
                    Modifier.fillMaxWidth()
            )

            Spacer(
                Modifier.height(16.dp)
            )

            AnimatedContent(
                targetState = current,
                transitionSpec = {
                    (
                        fadeIn(
                            tween(220)
                        )
                        ) togetherWith
                        fadeOut(
                            tween(150)
                        )
                },
                label =
                    "activeLyric"
            ) { line ->
                Text(
                    line,
                    color = accent,
                    fontFamily =
                        XmoFont.bold,
                    fontSize =
                        22.sp,
                    lineHeight =
                        29.sp,
                    textAlign =
                        TextAlign.Center,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }

            Spacer(
                Modifier.height(16.dp)
            )

            Text(
                next,
                color =
                    c.text.copy(alpha = .42f),
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    16.sp,
                lineHeight =
                    22.sp,
                textAlign =
                    TextAlign.Center,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis,
                modifier =
                    Modifier.fillMaxWidth()
            )
        }

        return
    }

    /*
     * Plain untimed lyrics.
     */
    Column(
        Modifier
            .fillMaxWidth()
            .height(240.dp)
            .verticalScroll(
                rememberScrollState()
            ),
        horizontalAlignment =
            Alignment.CenterHorizontally,
        verticalArrangement =
            Arrangement.spacedBy(11.dp)
    ) {
        lyrics.lines.forEach {
            Text(
                it.text,
                color =
                    c.text.copy(alpha = .76f),
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    16.sp,
                lineHeight =
                    24.sp,
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
 * FULL LYRICS
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
    close: () -> Unit
) {
    val active =
        lyrics?.let {
            currentLyricIndex(
                it,
                position
            )
        } ?: -1

    val list =
        rememberLazyListState()

    LaunchedEffect(active) {
        if (
            active >= 0 &&
            lyrics != null
        ) {
            list.animateScrollToItem(
                active,
                scrollOffset = -420
            )
        }
    }

    Box(
        Modifier.fillMaxSize()
    ) {
        ArtworkBackground(
            dominant = dominant,
            deep = deep,
            theme = theme
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(
                        horizontal = 16.dp
                    )
            ) {
                Row(
                    Modifier
                        .align(
                            Alignment.CenterStart
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
                            .width(220.dp)
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
                }

                PlayerCircle(
                    size = 40.dp,
                    background = c.button,
                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd
                        ),
                    click = close
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Close,
                        contentDescription =
                            "Close lyrics",
                        tint = c.text,
                        modifier =
                            Modifier.size(19.dp)
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
                            16.sp
                    )
                }
            } else {
                LazyColumn(
                    state = list,
                    contentPadding =
                        PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            top = 210.dp,
                            bottom = 320.dp
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(
                        lyrics.lines
                    ) {
                            index,
                            line ->

                        val selected =
                            index == active

                        Text(
                            line.text,
                            color =
                                if (selected) {
                                    accent
                                } else {
                                    c.text.copy(
                                        alpha = .48f
                                    )
                                },
                            fontFamily =
                                if (selected) {
                                    XmoFont.bold
                                } else {
                                    XmoFont.normal
                                },
                            fontSize =
                                if (selected) {
                                    24.sp
                                } else {
                                    18.sp
                                },
                            lineHeight =
                                if (selected) {
                                    31.sp
                                } else {
                                    26.sp
                                },
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
                fontSize = 11.sp,
                letterSpacing = 1.sp
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
                                    song.id ==
                                    currentId
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
                                        song.id ==
                                        currentId
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
        ).forEach {
                (minutes, text) ->

            PopupRow(
                text,
                c
            ) {
                select(
                    minutes * 60_000L
                )
            }
        }

        if (active) {
            PopupRow(
                "Cancel timer",
                c,
                true,
                cancel
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
            liked,
            toggleLike
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
                .clip(
                    RoundedCornerShape(11.dp)
                )
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
 * LRC PARSER
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
                    ?.use {
                        it.readText()
                    }
                    ?: return@runCatching null

            parseLyrics(source)
        }.getOrNull()
    }

private fun parseLyrics(
    source: String
): SongLyrics {
    val result =
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

    source.lineSequence()
        .forEach { raw ->

            val stamps =
                timestamp
                    .findAll(raw)
                    .toList()

            if (stamps.isNotEmpty()) {
                val text =
                    timestamp
                        .replace(raw, "")
                        .trim()

                if (text.isNotEmpty()) {
                    stamps.forEach { match ->
                        val minutes =
                            match.groupValues[1]
                                .toLongOrNull()
                                ?: 0L

                        val seconds =
                            match.groupValues[2]
                                .toLongOrNull()
                                ?: 0L

                        val rawFraction =
                            match.groupValues[3]

                        val fraction =
                            when (
                                rawFraction.length
                            ) {
                                1 ->
                                    (
                                        rawFraction
                                            .toLongOrNull()
                                            ?: 0L
                                        ) * 100L

                                2 ->
                                    (
                                        rawFraction
                                            .toLongOrNull()
                                            ?: 0L
                                        ) * 10L

                                3 ->
                                    rawFraction
                                        .toLongOrNull()
                                        ?: 0L

                                else ->
                                    0L
                            }

                        result +=
                            LyricLine(
                                timeMs =
                                    minutes * 60_000L +
                                        seconds * 1_000L +
                                        fraction,
                                text = text
                            )
                    }
                }
            } else {
                val text =
                    raw.trim()

                if (
                    text.isNotEmpty() &&
                    !metadata.matches(text)
                ) {
                    result +=
                        LyricLine(
                            timeMs = null,
                            text = text
                        )
                }
            }
        }

    val synced =
        result.any {
            it.timeMs != null
        }

    return SongLyrics(
        lines =
            if (synced) {
                result.sortedBy {
                    it.timeMs
                        ?: Long.MAX_VALUE
                }
            } else {
                result
            },
        synced = synced,
        source = "Local"
    )
}

private fun currentLyricIndex(
    lyrics: SongLyrics,
    position: Long
): Int {
    if (!lyrics.synced) {
        return -1
    }

    var result = -1

    lyrics.lines.forEachIndexed {
            index,
            line ->

        val time =
            line.timeMs
                ?: return@forEachIndexed

        if (time <= position) {
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
        val p =
            Path().apply {
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
        val p =
            Path().apply {
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
        val w =
            size.width * .18f

        val h =
            size.height * .62f

        val top =
            (size.height - h) / 2f

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
        val stroke =
            size.width * .08f

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
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .38f,
                size.height * .28f
            ),
            Offset(
                size.width * .78f,
                size.height * .72f
            ),
            stroke,
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
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            Offset(
                size.width * .38f,
                size.height * .72f
            ),
            Offset(
                size.width * .78f,
                size.height * .28f
            ),
            stroke,
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
            val stroke =
                size.width * .08f

            drawLine(
                color,
                Offset(
                    size.width * .20f,
                    size.height * .30f
                ),
                Offset(
                    size.width * .80f,
                    size.height * .30f
                ),
                stroke,
                StrokeCap.Round
            )

            drawLine(
                color,
                Offset(
                    size.width * .80f,
                    size.height * .70f
                ),
                Offset(
                    size.width * .20f,
                    size.height * .70f
                ),
                stroke,
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
        val stroke =
            size.width * .075f

        drawCircle(
            color,
            size.minDimension * .37f,
            style =
                androidx.compose.ui.graphics.drawscope.Stroke(
                    stroke
                )
        )

        drawLine(
            color,
            center,
            Offset(
                center.x,
                center.y -
                    size.height * .19f
            ),
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            center,
            Offset(
                center.x +
                    size.width * .15f,
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
            size.width * .07f

        val left =
            Offset(
                size.width * .27f,
                size.height * .50f
            )

        val top =
            Offset(
                size.width * .70f,
                size.height * .25f
            )

        val bottom =
            Offset(
                size.width * .70f,
                size.height * .75f
            )

        drawLine(
            color,
            left,
            top,
            stroke
        )

        drawLine(
            color,
            left,
            bottom,
            stroke
        )

        drawCircle(
            color,
            size.width * .10f,
            left
        )

        drawCircle(
            color,
            size.width * .10f,
            top
        )

        drawCircle(
            color,
            size.width * .10f,
            bottom
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
            size.width * .075f

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
        (total % 3600L) / 60L

    val seconds =
        total % 60L

    return if (hours > 0L) {
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
