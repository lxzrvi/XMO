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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.isActive
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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

private enum class ArtworkAxis {
    None,
    Horizontal,
    Vertical
}

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
        state.currentIndex
            .takeIf { it in queue.indices }
            ?: queue.indexOfFirst {
                it.id == state.currentSongId
            }

    val currentSong = queue.getOrNull(index)
    val previousSong = queue.getOrNull(index - 1)
    val nextSong = queue.getOrNull(index + 1)

    /*
     * =========================================================
     * CURRENT / ADJACENT ARTWORK COLORS
     * =========================================================
     */

    var currentColor by remember {
        mutableStateOf(Color(0xFF35353A))
    }

    var previousColor by remember {
        mutableStateOf(Color(0xFF35353A))
    }

    var nextColor by remember {
        mutableStateOf(Color(0xFF35353A))
    }

    LaunchedEffect(
        currentSong?.artwork,
        state.artworkUri
    ) {
        val uri =
            currentSong?.artwork
                ?: state.artworkUri?.let(Uri::parse)

        currentColor =
            Artwork.cached(uri)
                ?: Artwork.color(context, uri)
    }

    LaunchedEffect(previousSong?.artwork) {
        previousColor =
            previousSong?.artwork?.let { uri ->
                Artwork.cached(uri)
                    ?: Artwork.color(context, uri)
            } ?: currentColor
    }

    LaunchedEffect(nextSong?.artwork) {
        nextColor =
            nextSong?.artwork?.let { uri ->
                Artwork.cached(uri)
                    ?: Artwork.color(context, uri)
            } ?: currentColor
    }

    val animatedCurrent by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(380),
        label = "currentArtworkColor"
    )

    /*
     * Shared horizontal artwork displacement.
     *
     * Background reads this same value, so background transitions
     * during the swipe rather than changing afterwards.
     */
    val artworkX = remember {
        Animatable(0f)
    }

    var artworkWidth by remember {
        mutableFloatStateOf(1f)
    }

    val swipeFraction =
        (
            artworkX.value /
                artworkWidth
            ).coerceIn(-1f, 1f)

    val swipeTargetColor =
        when {
            swipeFraction < 0f ->
                nextColor

            swipeFraction > 0f ->
                previousColor

            else ->
                animatedCurrent
        }

    val swipeMix =
        abs(swipeFraction)
            .coerceIn(0f, 1f)

    val liveBackgroundColor =
        blendColor(
            animatedCurrent,
            swipeTargetColor,
            swipeMix
        )

    val deepColor =
        Artwork.deep(
            liveBackgroundColor,
            theme
        )

    val overlayText =
        if (
            liveBackgroundColor.luminance() >
            .56f
        ) {
            Color(0xFF101010)
        } else {
            Color.White
        }

    /*
     * =========================================================
     * PANEL
     * =========================================================
     */

    val panelBackground =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .34f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .25f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .42f
                )
        }

    val sectionBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .14f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .15f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .21f
                )
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
        while (isActive) {
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

    var externalLyrics by remember(
        state.currentSongId,
        lyricsUri
    ) {
        mutableStateOf<SongLyrics?>(
            null
        )
    }

    LaunchedEffect(
        state.currentSongId,
        lyricsUri
    ) {
        externalLyrics =
            lyricsUri?.let {
                readLyrics(
                    context,
                    Uri.parse(it)
                )
            }
    }

    val lyrics =
        externalLyrics
            ?: currentSong?.embeddedLyrics

    val lyricPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .OpenDocument()
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

    /*
     * =========================================================
     * OVERLAYS
     * =========================================================
     */

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
     * PLAYER OPEN / CLOSE
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

    suspend fun finishDismiss() {
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
                    finishDismiss()
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
                        entrance.value *
                        screenHeight
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        (
                            56f *
                                (
                                    dragY.value /
                                        screenHeight
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp,

                    topEnd =
                        (
                            56f *
                                (
                                    dragY.value /
                                        screenHeight
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp
                )
            )
    ) {
        ArtworkBackground(
            dominant =
                liveBackgroundColor,
            deep =
                deepColor,
            theme =
                theme
        )

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(
                    androidx.compose.foundation
                        .rememberScrollState()
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
                    .padding(
                        horizontal = 14.dp
                    )
                    .downGesture(
                        dragY =
                            dragY,
                        screenHeight =
                            screenHeight,
                        scope =
                            scope,
                        dismiss = {
                            if (!dismissing) {
                                dismissing = true
                                dismiss()
                            }
                        }
                    )
            ) {
                PlayerCircle(
                    size = 39.dp,
                    background =
                        overlayText.copy(
                            alpha = .10f
                        ),
                    modifier =
                        Modifier.align(
                            Alignment.CenterStart
                        ),
                    click = {
                        scope.launch {
                            finishDismiss()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Default
                                .KeyboardArrowDown,
                        contentDescription =
                            "Close",
                        tint =
                            overlayText,
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
                            overlayText.copy(
                                alpha = .68f
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize =
                            12.sp,
                        letterSpacing =
                            1.sp,
                        textAlign =
                            TextAlign.Center,
                        maxLines = 1
                    )

                    Text(
                        text = source,
                        color =
                            overlayText,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            16.sp,
                        textAlign =
                            TextAlign.Center,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                XmoCapsule(
                    background =
                        overlayText.copy(
                            alpha = .10f
                        ),
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
                            tint =
                                overlayText,
                            modifier =
                                Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(68.dp)
            )

            /*
             * =================================================
             * COVER
             *
             * One axis-lock gesture controls both horizontal
             * carousel and vertical NowPlaying dismissal.
             * =================================================
             */

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
                    state.hasPrevious,
                canNext =
                    state.hasNext,
                x =
                    artworkX,
                setWidth = {
                    artworkWidth =
                        it.coerceAtLeast(1f)
                },
                dragY =
                    dragY,
                screenHeight =
                    screenHeight,
                previousSong =
                    previousItem,
                nextSong =
                    next,
                dismiss = {
                    if (!dismissing) {
                        dismissing = true
                        dismiss()
                    }
                }
            )

            /*
             * Double-sized artwork → panel gap.
             */
            Spacer(
                Modifier.height(104.dp)
            )

            /*
             * =================================================
             * EDGE-TO-EDGE LOWER PANEL
             *
             * No outer side gap.
             * =================================================
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
                        panelBackground
                    )
                    .padding(
                        start = 16.dp,
                        top = 25.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    )
            ) {
                /*
                 * =================================================
                 * TITLE
                 * =================================================
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
                            color =
                                c.text,
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
                            color =
                                c.sub,
                            fontFamily =
                                XmoFont.normal,
                            fontSize =
                                13.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    /*
                     * Larger aligned like + white star.
                     */
                    XmoCapsule(
                        background =
                            c.button
                    ) {
                        CapsuleButton(
                            size = 44.dp,
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
                                    Modifier.size(19.dp)
                            )
                        }

                        CapsuleButton(
                            size = 44.dp,
                            click = {
                                menuOpen = true
                            }
                        ) {
                            XmoIcon(
                                icon =
                                    R.drawable.ic_xmo_star,
                                tint =
                                    Color.White,
                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(
                    Modifier.height(31.dp)
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

                Spacer(
                    Modifier.height(3.dp)
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
                        fontSize = 12.sp
                    )

                    Text(
                        playerTime(
                            state.duration
                        ),
                        color = c.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 12.sp
                    )
                }

                Spacer(
                    Modifier.height(25.dp)
                )

                /*
                 * =================================================
                 * SAME-SIZE CONTROL ROW
                 * =================================================
                 */

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    PlayerCircle(
                        size = 42.dp,
                        background = c.button,
                        click = {
                            sleepOpen = true
                        }
                    ) {
                        ClockIcon(
                            if (
                                state.sleepTimerRemainingMs >
                                0L
                            ) {
                                accent
                            } else {
                                c.icon
                            },
                            Modifier.size(18.dp)
                        )
                    }

                    PlayerCircle(
                        size = 42.dp,
                        background = c.button,
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
                            Modifier.size(19.dp)
                        )
                    }

                    PlayerCircle(
                        size = 42.dp,
                        background = c.button,
                        click =
                            previous
                    ) {
                        PreviousIcon(
                            c.text,
                            Modifier.size(20.dp)
                        )
                    }

                    PlayerCircle(
                        size = 44.dp,
                        background = accent,
                        click =
                            togglePlay
                    ) {
                        if (state.isPlaying) {
                            PauseIcon(
                                Color.White,
                                Modifier.size(21.dp)
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
                                    Modifier.size(24.dp)
                            )
                        }
                    }

                    PlayerCircle(
                        size = 42.dp,
                        background = c.button,
                        enabled =
                            state.hasNext,
                        click =
                            next
                    ) {
                        NextIcon(
                            if (state.hasNext) {
                                c.text
                            } else {
                                c.sub.copy(
                                    alpha = .35f
                                )
                            },
                            Modifier.size(20.dp)
                        )
                    }

                    PlayerCircle(
                        size = 42.dp,
                        background = c.button,
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
                                Modifier.size(19.dp)
                        )
                    }

                    PlayerCircle(
                        size = 42.dp,
                        background = c.button,
                        click = {
                            queueOpen = true
                        }
                    ) {
                        QueueIcon(
                            c.icon,
                            Modifier.size(19.dp)
                        )
                    }
                }

                Spacer(
                    Modifier.height(49.dp)
                )

                /*
                 * =================================================
                 * LYRICS
                 * =================================================
                 */

                BorderSection(
                    title =
                        "LYRICS",
                    c =
                        c,
                    border =
                        sectionBorder,
                    actions = {
                        /*
                         * Separate circles, no capsule.
                         */
                        PlayerCircle(
                            size = 38.dp,
                            background =
                                c.button,
                            click = {
                                lyricPicker.launch(
                                    arrayOf("*/*")
                                )
                            }
                        ) {
                            Text(
                                "+",
                                color =
                                    accent,
                                fontFamily =
                                    XmoFont.medium,
                                fontSize =
                                    20.sp,
                                textAlign =
                                    TextAlign.Center
                            )
                        }

                        Spacer(
                            Modifier.width(7.dp)
                        )

                        PlayerCircle(
                            size = 38.dp,
                            background =
                                c.button,
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
                ) {
                    FollowLyricsList(
                        lyrics =
                            lyrics,
                        position =
                            state.position,
                        c =
                            c,
                        accent =
                            accent,
                        height =
                            280.dp,
                        fullscreen =
                            false
                    )
                }

                Spacer(
                    Modifier.height(18.dp)
                )

                /*
                 * =================================================
                 * ARTIST
                 * =================================================
                 */

                BorderSection(
                    title =
                        "ARTIST",
                    c =
                        c,
                    border =
                        sectionBorder
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
                                        it != state.artist
                                }
                                ?.let {
                                    Text(
                                        it,
                                        color =
                                            c.sub,
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
                    Modifier.height(18.dp)
                )

                /*
                 * =================================================
                 * DETAILS
                 * =================================================
                 */

                BorderSection(
                    title =
                        "SONG DETAILS",
                    c =
                        c,
                    border =
                        sectionBorder
                ) {
                    DetailRow(
                        "Album",
                        state.album,
                        c
                    )

                    currentSong?.metadata
                        ?.let { meta ->

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
         * OVERLAYS
         * =====================================================
         */

        if (queueOpen) {
            QueueOverlay(
                queue =
                    queue,
                currentId =
                    state.currentSongId,
                c =
                    c,
                close = {
                    queueOpen = false
                }
            )
        }

        if (sleepOpen) {
            SleepOverlay(
                c =
                    c,
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

        if (menuOpen) {
            MoreOverlay(
                c =
                    c,
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
                lyrics =
                    lyrics,
                position =
                    state.position,
                title =
                    state.title,
                artist =
                    state.artist,
                artwork =
                    currentSong?.artwork,
                dominant =
                    liveBackgroundColor,
                deep =
                    deepColor,
                theme =
                    theme,
                isPlaying =
                    state.isPlaying,
                togglePlay =
                    togglePlay,
                previous =
                    previous,
                next =
                    next,
                close = {
                    fullLyrics = false
                }
            )
        }
    }
}

/*
 * =============================================================
 * ARTWORK BACKGROUND
 * =============================================================
 */

@Composable
private fun ArtworkBackground(
    dominant: Color,
    deep: Color,
    theme: XmoTheme
) {
    val veil =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .07f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .07f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .13f
                )
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
                        size.width * .06f,
                        size.height * .08f
                    ),
                radius =
                    size.width * 1.20f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        deep.copy(
                            alpha = .62f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .96f,
                        size.height * .29f
                    ),
                radius =
                    size.width * 1.18f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant.copy(
                            alpha = .78f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .08f,
                        size.height * .64f
                    ),
                radius =
                    size.width * 1.17f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        deep.copy(
                            alpha = .60f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .93f,
                        size.height * .89f
                    ),
                radius =
                    size.width * 1.18f
            )
        )

        drawRect(veil)
    }
}

/*
 * =============================================================
 * ONE AXIS-LOCKED ARTWORK GESTURE
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
    x: Animatable<Float, *>,
    setWidth: (Float) -> Unit,
    dragY: Animatable<Float, *>,
    screenHeight: Float,
    previousSong: () -> Unit,
    nextSong: () -> Unit,
    dismiss: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    var axis by remember {
        mutableStateOf(ArtworkAxis.None)
    }

    var rawX by remember {
        mutableFloatStateOf(0f)
    }

    var rawY by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Stable transaction snapshots.
     */
    var transactionId by remember {
        mutableStateOf<Long?>(null)
    }

    var pendingDirection by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(currentId) {
        if (
            pendingDirection != 0 &&
            transactionId != null &&
            currentId != transactionId
        ) {
            /*
             * Real Media3 item confirmed.
             * Reset exactly once against new queue state.
             */
            x.snapTo(0f)

            pendingDirection = 0
            transactionId = null
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(382.dp)
    ) {
        val width =
            constraints.maxWidth
                .toFloat()
                .coerceAtLeast(1f)

        LaunchedEffect(width) {
            setWidth(width)
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(
                    currentId,
                    canPrevious,
                    canNext
                ) {
                    detectDragGestures(
                        onDragStart = {
                            axis =
                                ArtworkAxis.None

                            rawX = 0f
                            rawY = 0f
                        },

                        onDrag = {
                                change,
                                amount ->

                            if (
                                pendingDirection !=
                                0
                            ) {
                                return@detectDragGestures
                            }

                            rawX += amount.x
                            rawY += amount.y

                            if (
                                axis ==
                                ArtworkAxis.None &&
                                (
                                    abs(rawX) > 9f ||
                                    abs(rawY) > 9f
                                )
                            ) {
                                axis =
                                    if (
                                        abs(rawX) >
                                        abs(rawY)
                                    ) {
                                        ArtworkAxis.Horizontal
                                    } else {
                                        ArtworkAxis.Vertical
                                    }
                            }

                            when (axis) {
                                ArtworkAxis.Horizontal -> {
                                    change.consume()

                                    scope.launch {
                                        dragY.snapTo(0f)

                                        var target =
                                            x.value +
                                                amount.x

                                        if (
                                            target < 0f &&
                                            !canNext
                                        ) {
                                            target =
                                                x.value +
                                                    amount.x *
                                                    .18f
                                        }

                                        if (
                                            target > 0f &&
                                            !canPrevious
                                        ) {
                                            target =
                                                x.value +
                                                    amount.x *
                                                    .18f
                                        }

                                        x.snapTo(
                                            target.coerceIn(
                                                -width,
                                                width
                                            )
                                        )
                                    }
                                }

                                ArtworkAxis.Vertical -> {
                                    if (
                                        rawY > 0f ||
                                        dragY.value > 0f
                                    ) {
                                        change.consume()

                                        scope.launch {
                                            x.snapTo(0f)

                                            dragY.snapTo(
                                                (
                                                    dragY.value +
                                                        amount.y
                                                    )
                                                    .coerceIn(
                                                        0f,
                                                        screenHeight
                                                    )
                                            )
                                        }
                                    }
                                }

                                ArtworkAxis.None ->
                                    Unit
                            }
                        },

                        onDragEnd = {
                            val finalAxis =
                                axis

                            scope.launch {
                                when (finalAxis) {
                                    ArtworkAxis.Horizontal -> {
                                        when {
                                            x.value <
                                                -width *
                                                .15f &&
                                                canNext -> {

                                                transactionId =
                                                    currentId

                                                pendingDirection =
                                                    1

                                                /*
                                                 * Real playback request starts
                                                 * immediately. The carousel is
                                                 * not timer-reset.
                                                 */
                                                nextSong()

                                                x.animateTo(
                                                    -width,
                                                    tween(240)
                                                )
                                            }

                                            x.value >
                                                width *
                                                .15f &&
                                                canPrevious -> {

                                                transactionId =
                                                    currentId

                                                pendingDirection =
                                                    -1

                                                previousSong()

                                                x.animateTo(
                                                    width,
                                                    tween(240)
                                                )
                                            }

                                            else -> {
                                                x.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio =
                                                            .82f,
                                                        stiffness =
                                                            430f
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    ArtworkAxis.Vertical -> {
                                        if (
                                            dragY.value >
                                            screenHeight *
                                                .13f
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
                                                    dampingRatio =
                                                        .82f,
                                                    stiffness =
                                                        390f
                                                )
                                            )
                                        }
                                    }

                                    ArtworkAxis.None ->
                                        Unit
                                }

                                rawX = 0f
                                rawY = 0f
                                axis =
                                    ArtworkAxis.None
                            }
                        },

                        onDragCancel = {
                            scope.launch {
                                if (
                                    pendingDirection ==
                                    0
                                ) {
                                    x.animateTo(
                                        0f,
                                        tween(180)
                                    )
                                }

                                dragY.animateTo(
                                    0f,
                                    tween(180)
                                )

                                rawX = 0f
                                rawY = 0f
                                axis =
                                    ArtworkAxis.None
                            }
                        }
                    )
                }
        ) {
            previous?.let {
                Cover(
                    uri = it,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = 17.dp
                            )
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    x.value -
                                        width
                            }
                )
            }

            Cover(
                uri = current,
                modifier =
                    Modifier
                        .padding(
                            horizontal = 17.dp
                        )
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer {
                            translationX =
                                x.value
                        }
            )

            next?.let {
                Cover(
                    uri = it,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = 17.dp
                            )
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    x.value +
                                        width
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
                Color.Black.copy(
                    alpha = .08f
                )
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
 * HEADER DOWN GESTURE
 * =============================================================
 */

private fun Modifier.downGesture(
    dragY: Animatable<Float, *>,
    screenHeight: Float,
    scope: kotlinx.coroutines.CoroutineScope,
    dismiss: () -> Unit
): Modifier =
    pointerInput(screenHeight) {
        detectDragGestures(
            onDrag = {
                    change,
                    amount ->

                if (
                    amount.y > 0f ||
                    dragY.value > 0f
                ) {
                    change.consume()

                    scope.launch {
                        dragY.snapTo(
                            (
                                dragY.value +
                                    amount.y
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
                        dragY.value >
                        screenHeight *
                            .13f
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
                    dragY.animateTo(
                        0f,
                        tween(180)
                    )
                }
            }
        )
    }

/*
 * =============================================================
 * XMO CAPSULE
 * =============================================================
 */

@Composable
private fun XmoCapsule(
    background: Color,
    modifier: Modifier = Modifier,
    content:
        @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    Row(
        modifier
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(background)
            .padding(horizontal = 2.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun CapsuleButton(
    size: Dp = 39.dp,
    click: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .size(size)
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
private fun PlayerCircle(
    size: Dp,
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
                )
                .coerceIn(0f, 1f)
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
                        val fraction =
                            (
                                it.x /
                                    size.width
                                )
                                .coerceIn(0f, 1f)

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
 * BORDER SECTIONS
 * =============================================================
 */

@Composable
private fun BorderSection(
    title: String,
    c: HomeColors,
    border: Color,
    actions:
        @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {},
    content:
        @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp)
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
 * FOLLOWING + MANUALLY SCROLLABLE LYRICS
 *
 * During manual scroll:
 * - list stays where user puts it.
 *
 * 3 seconds after scrolling stops:
 * - active timestamp smoothly returns to center.
 * =============================================================
 */

@Composable
private fun FollowLyricsList(
    lyrics: SongLyrics?,
    position: Long,
    c: HomeColors,
    accent: Color,
    height: Dp,
    fullscreen: Boolean
) {
    if (
        lyrics == null ||
        lyrics.lines.isEmpty()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(height),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                "No local lyrics found.\nTap + to select an LRC file.",
                color = c.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    if (fullscreen) {
                        18.sp
                    } else {
                        16.sp
                    },
                lineHeight =
                    if (fullscreen) {
                        27.sp
                    } else {
                        24.sp
                    },
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

    val listState =
        rememberLazyListState()

    var userBrowsing by remember {
        mutableStateOf(false)
    }

    var browseVersion by remember {
        mutableLongStateOf(0L)
    }

    /*
     * Detect real user scroll.
     */
    LaunchedEffect(
        listState.isScrollInProgress
    ) {
        if (listState.isScrollInProgress) {
            userBrowsing = true
            browseVersion++
        } else if (userBrowsing) {
            val version =
                ++browseVersion

            delay(3_000L)

            if (
                version == browseVersion &&
                !listState.isScrollInProgress
            ) {
                userBrowsing = false
            }
        }
    }

    /*
     * Follow active timestamp whenever manual browse mode is off.
     */
    LaunchedEffect(
        active,
        userBrowsing
    ) {
        if (
            lyrics.synced &&
            active >= 0 &&
            !userBrowsing
        ) {
            listState.animateScrollToItem(
                index = active,
                scrollOffset =
                    if (fullscreen) {
                        -430
                    } else {
                        -115
                    }
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height),
        contentPadding =
            PaddingValues(
                top =
                    if (fullscreen) {
                        230.dp
                    } else {
                        112.dp
                    },
                bottom =
                    if (fullscreen) {
                        260.dp
                    } else {
                        122.dp
                    }
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullscreen) {
                    21.dp
                } else {
                    14.dp
                }
            )
    ) {
        itemsIndexed(
            items = lyrics.lines
        ) {
                index,
                line ->

            val selected =
                lyrics.synced &&
                    index == active

            /*
             * Item placement moves naturally with LazyColumn.
             * Active line zooms/focuses while the next timestamp
             * enters upward from below as list follows.
             */
            val scale by
                androidx.compose.animation.core
                    .animateFloatAsState(
                        targetValue =
                            if (selected) {
                                if (fullscreen) {
                                    1.08f
                                } else {
                                    1.06f
                                }
                            } else {
                                1f
                            },
                        animationSpec =
                            tween(260),
                        label =
                            "lyricScale$index"
                    )

            val textColor =
                when {
                    selected ->
                        accent

                    fullscreen ->
                        c.text.copy(
                            alpha = .50f
                        )

                    else ->
                        c.text.copy(
                            alpha = .55f
                        )
                }

            Text(
                text = line.text,
                color = textColor,
                fontFamily =
                    if (selected) {
                        XmoFont.bold
                    } else {
                        XmoFont.normal
                    },
                fontSize =
                    when {
                        selected &&
                            fullscreen ->
                            28.sp

                        selected ->
                            22.sp

                        fullscreen ->
                            20.sp

                        else ->
                            17.sp
                    },
                lineHeight =
                    when {
                        selected &&
                            fullscreen ->
                            36.sp

                        selected ->
                            29.sp

                        fullscreen ->
                            29.sp

                        else ->
                            25.sp
                    },
                textAlign =
                    TextAlign.Center,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .animateItem()
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
    isPlaying: Boolean,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    close: () -> Unit
) {
    val foreground =
        if (
            dominant.luminance() >
            .56f
        ) {
            Color(0xFF101010)
        } else {
            Color.White
        }

    val localColors =
        HomeColors(
            bg = Color.Transparent,
            surface = Color.Transparent,
            text = foreground,
            sub =
                foreground.copy(
                    alpha = .60f
                ),
            button =
                foreground.copy(
                    alpha = .10f
                ),
            icon =
                foreground.copy(
                    alpha = .74f
                ),
            border =
                foreground.copy(
                    alpha = .16f
                )
        )

    Box(
        Modifier.fillMaxSize()
    ) {
        ArtworkBackground(
            dominant =
                dominant,
            deep =
                deep,
            theme =
                theme
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
                    .height(70.dp)
                    .padding(
                        start = 16.dp,
                        end = 14.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                AsyncImage(
                    model =
                        artwork,
                    contentDescription =
                        null,
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(
                                RoundedCornerShape(
                                    9.dp
                                )
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
                        color =
                            foreground,
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
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                XmoLongCap(
                    isPlaying =
                        isPlaying,
                    foreground =
                        foreground,
                    background =
                        foreground.copy(
                            alpha = .10f
                        ),
                    togglePlay =
                        togglePlay,
                    next =
                        next,
                    previous =
                        previous,
                    close =
                        close
                )
            }

            /*
             * All lyrics are present and manually scrollable.
             * Auto-follow resumes after 3 seconds idle.
             */
            FollowLyricsList(
                lyrics =
                    lyrics,
                position =
                    position,
                c =
                    localColors,
                accent =
                    foreground,
                height =
                    androidx.compose.ui.platform
                        .LocalConfiguration.current
                        .screenHeightDp.dp -
                        90.dp,
                fullscreen =
                    true
            )
        }
    }
}

/*
 * =============================================================
 * XMO LONG CAP
 *
 * Play/Pause → Next → Previous → Close
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
            .clip(
                RoundedCornerShape(24.dp)
            )
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
                    tint =
                        foreground,
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
                tint =
                    foreground,
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
            color =
                c.sub,
            fontFamily =
                XmoFont.normal,
            fontSize = 11.sp,
            modifier =
                Modifier.width(92.dp)
        )

        Text(
            value,
            color =
                c.text,
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
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                itemsIndexed(
                    items = queue,
                    key = { _, song ->
                        song.id
                    }
                ) { _, song ->

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(
                                RoundedCornerShape(
                                    12.dp
                                )
                            )
                            .background(
                                if (
                                    song.id ==
                                    currentId
                                ) {
                                    LocalXmoAccent.current
                                        .copy(
                                            alpha = .13f
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
                                color =
                                    c.sub,
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
        title =
            "Sleep Timer",
        c =
            c,
        close =
            close
    ) {
        listOf(
            15L to "15 minutes",
            30L to "30 minutes",
            45L to "45 minutes",
            60L to "1 hour"
        ).forEach {
                (minutes, label) ->

            PopupRow(
                label,
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
        c =
            c,
        close =
            close
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
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(c.surface)
                .clickable {}
                .padding(17.dp)
        ) {
            Text(
                title,
                color =
                    c.text,
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
                .clickable(
                    onClick = click
                )
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

    source
        .lineSequence()
        .forEach { raw ->

            val stamps =
                timestamp
                    .findAll(raw)
                    .toList()

            if (stamps.isNotEmpty()) {
                val text =
                    timestamp
                        .replace(
                            raw,
                            ""
                        )
                        .trim()

                if (text.isNotEmpty()) {
                    stamps.forEach { match ->

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

                if (
                    clean.isNotEmpty() &&
                    !metadata.matches(clean)
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
                output.sortedBy {
                    it.timeMs
                        ?: Long.MAX_VALUE
                }
            } else {
                output
            },
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
    if (!lyrics.synced) {
        return -1
    }

    var active = -1

    lyrics.lines
        .forEachIndexed {
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
        val path =
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

        drawPath(
            path,
            color
        )

        drawRoundRect(
            color =
                color,
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

        drawPath(
            path,
            color
        )

        drawRoundRect(
            color =
                color,
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
                ) /
                2f

        drawRoundRect(
            color,
            Offset(
                size.width * .27f,
                top
            ),
            Size(
                width,
                height
            ),
            CornerRadius(
                width / 2f
            )
        )

        drawRoundRect(
            color,
            Offset(
                size.width * .55f,
                top
            ),
            Size(
                width,
                height
            ),
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
                size.width * .82f,
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
                size.width * .82f,
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
                    size.width * .18f,
                    size.height * .30f
                ),
                Offset(
                    size.width * .82f,
                    size.height * .30f
                ),
                stroke,
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
                stroke,
                StrokeCap.Round
            )
        }

        if (one) {
            Text(
                "1",
                color =
                    color,
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
        repeat(3) { index ->

            val y =
                size.height *
                    (
                        .27f +
                            index *
                            .23f
                        )

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
            color =
                color,
            radius =
                size.minDimension *
                    .37f,
            style =
                Stroke(stroke)
        )

        drawLine(
            color,
            center,
            Offset(
                center.x,
                center.y -
                    size.height *
                    .19f
            ),
            stroke,
            StrokeCap.Round
        )

        drawLine(
            color,
            center,
            Offset(
                center.x +
                    size.width *
                    .15f,
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
                stroke,
                StrokeCap.Round
            )
        }

        line(
            .18f,
            .38f,
            .18f,
            .18f
        )

        line(
            .18f,
            .18f,
            .38f,
            .18f
        )

        line(
            .62f,
            .18f,
            .82f,
            .18f
        )

        line(
            .82f,
            .18f,
            .82f,
            .38f
        )

        line(
            .18f,
            .62f,
            .18f,
            .82f
        )

        line(
            .18f,
            .82f,
            .38f,
            .82f
        )

        line(
            .62f,
            .82f,
            .82f,
            .82f
        )

        line(
            .82f,
            .82f,
            .82f,
            .62f
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

    return if (hours > 0L) {
        "$hours:${
            minutes
                .toString()
                .padStart(
                    2,
                    '0'
                )
        }:${
            seconds
                .toString()
                .padStart(
                    2,
                    '0'
                )
        }"
    } else {
        "$minutes:${
            seconds
                .toString()
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

private fun blendColor(
    from: Color,
    to: Color,
    amount: Float
): Color {
    val t =
        amount.coerceIn(
            0f,
            1f
        )

    return Color(
        red =
            from.red +
                (
                    to.red -
                        from.red
                    ) *
                t,
        green =
            from.green +
                (
                    to.green -
                        from.green
                    ) *
                t,
        blue =
            from.blue +
                (
                    to.blue -
                        from.blue
                    ) *
                t,
        alpha =
            from.alpha +
                (
                    to.alpha -
                        from.alpha
                    ) *
                t
    )
}
