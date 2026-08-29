package com.xmo.music.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.isActive
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Expand
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Repeat2
import com.composables.icons.lucide.Repeat1
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Shuffle
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TimerReset
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.xmo.music.XmoTheme
import com.xmo.music.data.LyricLine
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class CoverAxis {
    None,
    Horizontal,
    Vertical
}

private sealed interface PlayerOverlay {
    data object Options : PlayerOverlay
    data object Queue : PlayerOverlay
    data object Sleep : PlayerOverlay
}

private data class PopMessage(
    val text: String,
    val key: Long = System.nanoTime()
)

@Composable
fun NowPlaying(
    state: PlaybackState,
    theme: XmoTheme,
    source: String,
    sourceIsCategory: Boolean,
    queue: List<Song>,
    liked: Boolean,
    lyricsUri: String?,
    categories: List<UserCategory>,
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
    setSongInCategory: (
        categoryId: String,
        added: Boolean
    ) -> Unit,
    createCategory: (String) -> UserCategory?,
    dismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val c = homeColors(theme)
    val accent = LocalXmoAccent.current

    val currentIndex =
        state.currentIndex
            .takeIf { it in queue.indices }
            ?: queue.indexOfFirst {
                it.id == state.currentSongId
            }

    val currentSong =
        queue.getOrNull(currentIndex)

    val previousSong =
        queue.getOrNull(currentIndex - 1)

    val nextSong =
        queue.getOrNull(currentIndex + 1)

    /*
     * =========================================================
     * COLORS
     * =========================================================
     */

    var currentColor by remember {
        mutableStateOf(Color(0xFF52545A))
    }

    var previousColor by remember {
        mutableStateOf(Color(0xFF52545A))
    }

    var nextColor by remember {
        mutableStateOf(Color(0xFF52545A))
    }

    suspend fun extractBright(
        uri: Uri?
    ): Color {
        val raw =
            Artwork.cached(uri)
                ?: Artwork.color(
                    context,
                    uri
                )

        return liftArtworkColor(raw)
    }

    LaunchedEffect(
        currentSong?.artwork,
        state.artworkUri
    ) {
        currentColor =
            extractBright(
                currentSong?.artwork
                    ?: state.artworkUri?.let(
                        Uri::parse
                    )
            )
    }

    LaunchedEffect(previousSong?.artwork) {
        previousColor =
            previousSong?.artwork
                ?.let {
                    extractBright(it)
                }
                ?: currentColor
    }

    LaunchedEffect(nextSong?.artwork) {
        nextColor =
            nextSong?.artwork
                ?.let {
                    extractBright(it)
                }
                ?: currentColor
    }

    val animatedColor by animateColorAsState(
        targetValue = currentColor,
        animationSpec = tween(380),
        label = "dominant"
    )

    /*
     * =========================================================
     * CAROUSEL
     * =========================================================
     */

    val coverX =
        remember {
            Animatable(0f)
        }

    var coverWidth by remember {
        mutableFloatStateOf(1f)
    }

    val fraction =
        (
            coverX.value /
                coverWidth
            )
            .coerceIn(-1f, 1f)

    val targetColor =
        when {
            fraction < 0f ->
                nextColor

            fraction > 0f ->
                previousColor

            else ->
                animatedColor
        }

    val displayColor =
        mix(
            animatedColor,
            targetColor,
            abs(fraction)
        )

    val deep =
        Artwork.deep(
            displayColor,
            theme
        )

    val overlayText =
        if (
            displayColor.luminance() >
            .58f
        ) {
            Color(0xFF111214)
        } else {
            Color.White
        }

    val panel =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .38f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .25f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .43f)
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(alpha = .14f)

            XmoTheme.Dark ->
                Color.White.copy(alpha = .16f)

            XmoTheme.Amoled ->
                Color.White.copy(alpha = .22f)
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
        fileLyrics
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

                saveLyricsUri(
                    uri.toString()
                )
            }
        }

    /*
     * =========================================================
     * UI STATE
     * =========================================================
     */

    var overlay by remember {
        mutableStateOf<PlayerOverlay?>(null)
    }

    var fullLyrics by remember {
        mutableStateOf(false)
    }

    var pop by remember {
        mutableStateOf<PopMessage?>(null)
    }

    val entrance =
        remember {
            Animatable(1f)
        }

    val playerY =
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

    LaunchedEffect(pop?.key) {
        if (pop != null) {
            delay(1_900L)
            pop = null
        }
    }

    suspend fun closePlayer() {
        if (dismissing) return

        dismissing = true

        playerY.animateTo(
            screenHeight,
            tween(330)
        )

        dismiss()
    }

    BackHandler {
        when {
            fullLyrics ->
                fullLyrics = false

            overlay != null ->
                overlay = null

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
                        .coerceAtLeast(1f)
            }
            .graphicsLayer {
                translationY =
                    playerY.value +
                        entrance.value *
                        screenHeight
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        (
                            56f *
                                (
                                    playerY.value /
                                        screenHeight
                                    )
                                    .coerceIn(0f, 1f)
                            ).dp,

                    topEnd =
                        (
                            56f *
                                (
                                    playerY.value /
                                        screenHeight
                                    )
                                    .coerceIn(0f, 1f)
                            ).dp
                )
            )
    ) {
        PlayerBackground(
            dominant = displayColor,
            deep = deep,
            theme = theme
        )

        Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
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
                    .downGesture(
                        y = playerY,
                        height = screenHeight,
                        dismiss = {
                            if (!dismissing) {
                                dismissing = true
                                dismiss()
                            }
                        }
                    )
            ) {
                PremiumCircle(
                    size = 40.dp,
                    background =
                        overlayText.copy(alpha = .10f),
                    modifier =
                        Modifier.align(
                            Alignment.CenterStart
                        ),
                    onClick = {
                        scope.launch {
                            closePlayer()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Lucide.ChevronDown,
                        contentDescription =
                            "Close",
                        tint = overlayText,
                        modifier =
                            Modifier.size(22.dp)
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

                XmoCapsule(
                    background =
                        overlayText.copy(alpha = .10f),
                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd
                        )
                ) {
                    CapsuleButton(
                        onClick = {
                            currentSong?.let {
                                shareSong(context, it)
                            }
                        }
                    ) {
                        Icon(
                            Lucide.Share2,
                            contentDescription =
                                "Share",
                            tint = overlayText,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    CapsuleButton(
                        onClick = {
                            overlay =
                                PlayerOverlay.Options
                        }
                    ) {
                        Icon(
                            Lucide.EllipsisVertical,
                            contentDescription =
                                "Options",
                            tint = overlayText,
                            modifier =
                                Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(
                Modifier.height(68.dp)
            )

            /*
             * =================================================
             * ARTWORK
             * =================================================
             */

            PlayerArtwork(
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

                x =
                    coverX,

                setWidth = {
                    coverWidth =
                        it.coerceAtLeast(1f)
                },

                playerY =
                    playerY,

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

            Spacer(
                Modifier.height(104.dp)
            )

            /*
             * =================================================
             * PLAYER BODY
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
                    .background(panel)
                    .padding(
                        start = 12.dp,
                        top = 25.dp,
                        end = 12.dp,
                        bottom = 24.dp
                    )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
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

                    XmoCapsule(
                        background = c.button
                    ) {
                        CapsuleButton(
                            size = 44.dp,
                            onClick = {
                                toggleLike()

                                pop =
                                    PopMessage(
                                        if (liked) {
                                            "Removed from Liked Songs"
                                        } else {
                                            "Added to Liked Songs"
                                        }
                                    )
                            }
                        ) {
                            Icon(
                                Lucide.Heart,
                                contentDescription =
                                    "Like",
                                tint =
                                    if (liked) {
                                        accent
                                    } else {
                                        c.icon
                                    },
                                modifier =
                                    Modifier.size(20.dp)
                            )
                        }

                        CapsuleButton(
                            size = 44.dp,
                            onClick = {
                                overlay =
                                    PlayerOverlay.Options
                            }
                        ) {
                            Icon(
                                Lucide.Star,
                                contentDescription =
                                    "Song options",
                                tint = c.text,
                                modifier =
                                    Modifier.size(19.dp)
                            )
                        }
                    }
                }

                Spacer(
                    Modifier.height(31.dp)
                )

                RoundedSeekBar(
                    position =
                        state.position,
                    duration =
                        state.duration,
                    active =
                        accent,
                    inactive =
                        border,
                    seekTo =
                        seekTo
                )

                Spacer(
                    Modifier.height(4.dp)
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
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
                 * CONTROLS
                 * =================================================
                 */

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceEvenly,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    SleepControl(
                        active =
                            state.sleepTimerRemainingMs > 0L,
                        remaining =
                            state.sleepTimerRemainingMs,
                        accent =
                            accent,
                        foreground =
                            c.icon,
                        background =
                            c.button,
                        onClick = {
                            overlay =
                                PlayerOverlay.Sleep
                        }
                    )

                    PremiumCircle(
                        44.dp,
                        c.button,
                        onClick =
                            toggleShuffle
                    ) {
                        Icon(
                            Lucide.Shuffle,
                            contentDescription =
                                "Shuffle",
                            tint =
                                if (
                                    state.shuffleEnabled
                                ) {
                                    accent
                                } else {
                                    c.icon
                                },
                            modifier =
                                Modifier.size(20.dp)
                        )
                    }

                    PremiumCircle(
                        44.dp,
                        c.button,
                        onClick =
                            previous
                    ) {
                        Icon(
                            Lucide.SkipBack,
                            contentDescription =
                                "Previous",
                            tint = c.text,
                            modifier =
                                Modifier.size(21.dp)
                        )
                    }

                    PremiumCircle(
                        46.dp,
                        accent,
                        onClick =
                            togglePlay
                    ) {
                        Icon(
                            imageVector =
                                if (state.isPlaying) {
                                    Lucide.Pause
                                } else {
                                    Lucide.Play
                                },
                            contentDescription =
                                if (state.isPlaying) {
                                    "Pause"
                                } else {
                                    "Play"
                                },
                            tint =
                                Color.White,
                            modifier =
                                Modifier.size(22.dp)
                        )
                    }

                    PremiumCircle(
                        44.dp,
                        c.button,
                        enabled =
                            state.hasNext,
                        onClick =
                            next
                    ) {
                        Icon(
                            Lucide.SkipForward,
                            contentDescription =
                                "Next",
                            tint =
                                if (
                                    state.hasNext
                                ) {
                                    c.text
                                } else {
                                    c.sub.copy(alpha = .35f)
                                },
                            modifier =
                                Modifier.size(21.dp)
                        )
                    }

                    PremiumCircle(
                        44.dp,
                        c.button,
                        onClick =
                            cycleRepeat
                    ) {
                        Icon(
                            imageVector =
                                if (
                                    state.repeatMode ==
                                    androidx.media3.common.Player
                                        .REPEAT_MODE_ONE
                                ) {
                                    Lucide.Repeat1
                                } else {
                                    Lucide.Repeat2
                                },
                            contentDescription =
                                "Repeat",
                            tint =
                                if (
                                    state.repeatMode !=
                                    androidx.media3.common.Player
                                        .REPEAT_MODE_OFF
                                ) {
                                    accent
                                } else {
                                    c.icon
                                },
                            modifier =
                                Modifier.size(20.dp)
                        )
                    }

                    PremiumCircle(
                        44.dp,
                        c.button,
                        onClick = {
                            overlay =
                                PlayerOverlay.Queue
                        }
                    ) {
                        Icon(
                            Lucide.ListMusic,
                            contentDescription =
                                "Queue",
                            tint = c.icon,
                            modifier =
                                Modifier.size(20.dp)
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
                    border =
                        border,
                    c =
                        c,
                    actions = {
                        PremiumCircle(
                            39.dp,
                            c.button,
                            onClick = {
                                lyricPicker.launch(
                                    arrayOf("*/*")
                                )
                            }
                        ) {
                            Icon(
                                Lucide.Plus,
                                contentDescription =
                                    "Add lyrics",
                                tint = accent,
                                modifier =
                                    Modifier.size(19.dp)
                            )
                        }

                        Spacer(
                            Modifier.width(7.dp)
                        )

                        PremiumCircle(
                            39.dp,
                            c.button,
                            onClick = {
                                fullLyrics = true
                            }
                        ) {
                            Icon(
                                Lucide.Expand,
                                contentDescription =
                                    "Fullscreen lyrics",
                                tint = c.text,
                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }
                    }
                ) {
                    FollowLyrics(
                        lyrics =
                            lyrics,
                        position =
                            state.position,
                        colors =
                            c,
                        accent =
                            accent,
                        height =
                            286.dp,
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
                    border =
                        border,
                    c =
                        c
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
                    border =
                        border,
                    c =
                        c
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

        /*
         * =====================================================
         * XMO HALF OP
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Queue
        ) {
            XmoHalfOp(
                title =
                    "Queue",
                c =
                    c,
                dismiss = {
                    overlay = null
                }
            ) {
                items(
                    items = queue,
                    key = {
                        it.id
                    }
                ) { song ->

                    QueueRow(
                        song =
                            song,
                        active =
                            song.id ==
                                state.currentSongId,
                        c =
                            c
                    )
                }
            }
        }

        /*
         * =====================================================
         * XMO CENTER BOX
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Options
        ) {
            XmoCenterBox(
                song =
                    currentSong,
                categories =
                    categories,
                c =
                    c,
                liked =
                    liked,
                close = {
                    overlay = null
                },
                toggleLike = {
                    toggleLike()

                    pop =
                        PopMessage(
                            if (liked) {
                                "Removed from Liked Songs"
                            } else {
                                "Added to Liked Songs"
                            }
                        )
                },
                share = {
                    currentSong?.let {
                        shareSong(context, it)
                    }

                    overlay = null
                },
                removeLyrics = {
                    saveLyricsUri(null)
                    pop =
                        PopMessage(
                            "Attached lyrics removed"
                        )
                },
                setCategory = {
                        category,
                        add ->

                    setSongInCategory(
                        category.id,
                        add
                    )

                    pop =
                        PopMessage(
                            if (add) {
                                "Added to ${category.name}"
                            } else {
                                "Removed from ${category.name}"
                            }
                        )
                },
                createCategory = { name ->
                    val created =
                        createCategory(name)

                    if (created != null) {
                        pop =
                            PopMessage(
                                "Added to ${created.name}"
                            )
                    }

                    created != null
                }
            )
        }

        /*
         * =====================================================
         * SLEEP
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Sleep
        ) {
            SleepCenterBox(
                c =
                    c,
                active =
                    state.sleepTimerRemainingMs >
                        0L,
                dismiss = {
                    overlay = null
                },
                setTimer = {
                        duration,
                        label ->

                    setSleepTimer(duration)
                    overlay = null

                    pop =
                        PopMessage(
                            "Sleep timer set for $label"
                        )
                },
                cancel = {
                    cancelSleepTimer()
                    overlay = null

                    pop =
                        PopMessage(
                            "Sleep timer cancelled"
                        )
                }
            )
        }

        /*
         * =====================================================
         * FULLSCREEN LYRICS
         * =====================================================
         */

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
                    displayColor,
                deep =
                    deep,
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

        /*
         * =====================================================
         * XMO POP
         * =====================================================
         */

        pop?.let {
            XmoPop(
                message =
                    it.text,
                theme =
                    theme,
                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .statusBarsPadding()
                        .padding(top = 72.dp)
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
    val veil =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .08f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .06f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .12f)
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
                size.width * 1.25f
            )
        )

        drawRect(
            Brush.radialGradient(
                listOf(
                    deep.copy(alpha = .42f),
                    Color.Transparent
                ),
                Offset(
                    size.width * .93f,
                    size.height * .31f
                ),
                size.width * 1.22f
            )
        )

        drawRect(
            Brush.radialGradient(
                listOf(
                    dominant.copy(alpha = .72f),
                    Color.Transparent
                ),
                Offset(
                    size.width * .08f,
                    size.height * .65f
                ),
                size.width * 1.22f
            )
        )

        drawRect(
            Brush.radialGradient(
                listOf(
                    deep.copy(alpha = .40f),
                    Color.Transparent
                ),
                Offset(
                    size.width * .92f,
                    size.height * .88f
                ),
                size.width * 1.23f
            )
        )

        drawRect(veil)
    }
}

/*
 * =============================================================
 * ARTWORK TRANSACTION
 * =============================================================
 */

@Composable
private fun PlayerArtwork(
    currentId: Long?,
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    canPrevious: Boolean,
    canNext: Boolean,
    x: Animatable<Float, *>,
    setWidth: (Float) -> Unit,
    playerY: Animatable<Float, *>,
    screenHeight: Float,
    previousSong: () -> Unit,
    nextSong: () -> Unit,
    dismiss: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    var axis by remember {
        mutableStateOf(CoverAxis.None)
    }

    var rawX by remember {
        mutableFloatStateOf(0f)
    }

    var rawY by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Keep URI snapshots throughout an active transaction.
     * Queue recomposition cannot replace them halfway through.
     */
    var snapshotCurrent by remember {
        mutableStateOf(current)
    }

    var snapshotPrevious by remember {
        mutableStateOf(previous)
    }

    var snapshotNext by remember {
        mutableStateOf(next)
    }

    var transactionSongId by remember {
        mutableStateOf<Long?>(null)
    }

    var pending by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        currentId,
        pending
    ) {
        if (
            pending != 0 &&
            transactionSongId != null &&
            currentId != transactionSongId
        ) {
            /*
             * Media3 confirmation arrived.
             *
             * First return displacement to zero, then adopt the
             * newly recomposed queue snapshots. This avoids
             * drawing new-current at an old-current translation.
             */
            x.snapTo(0f)

            pending = 0
            transactionSongId = null

            snapshotCurrent = current
            snapshotPrevious = previous
            snapshotNext = next
        } else if (
            pending == 0
        ) {
            snapshotCurrent = current
            snapshotPrevious = previous
            snapshotNext = next
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
                            axis = CoverAxis.None
                            rawX = 0f
                            rawY = 0f
                        },

                        onDrag = {
                                change,
                                amount ->

                            if (pending != 0) {
                                return@detectDragGestures
                            }

                            rawX += amount.x
                            rawY += amount.y

                            if (
                                axis == CoverAxis.None &&
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
                                        CoverAxis.Horizontal
                                    } else {
                                        CoverAxis.Vertical
                                    }
                            }

                            when (axis) {
                                CoverAxis.Horizontal -> {
                                    change.consume()

                                    scope.launch {
                                        playerY.snapTo(0f)

                                        var target =
                                            x.value +
                                                amount.x

                                        if (
                                            target < 0f &&
                                            !canNext
                                        ) {
                                            target =
                                                x.value +
                                                    amount.x * .17f
                                        }

                                        if (
                                            target > 0f &&
                                            !canPrevious
                                        ) {
                                            target =
                                                x.value +
                                                    amount.x * .17f
                                        }

                                        x.snapTo(
                                            target.coerceIn(
                                                -width,
                                                width
                                            )
                                        )
                                    }
                                }

                                CoverAxis.Vertical -> {
                                    if (
                                        rawY > 0f ||
                                        playerY.value > 0f
                                    ) {
                                        change.consume()

                                        scope.launch {
                                            x.snapTo(0f)

                                            playerY.snapTo(
                                                (
                                                    playerY.value +
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

                                CoverAxis.None ->
                                    Unit
                            }
                        },

                        onDragEnd = {
                            val finalAxis =
                                axis

                            scope.launch {
                                when (finalAxis) {
                                    CoverAxis.Horizontal -> {
                                        when {
                                            x.value <
                                                -width * .15f &&
                                                canNext -> {

                                                transactionSongId =
                                                    currentId

                                                pending = 1

                                                /*
                                                 * Keep exactly the finger's
                                                 * released position, then
                                                 * smoothly complete from that
                                                 * point.
                                                 */
                                                x.animateTo(
                                                    -width,
                                                    tween(220)
                                                )

                                                nextSong()
                                            }

                                            x.value >
                                                width * .15f &&
                                                canPrevious -> {

                                                transactionSongId =
                                                    currentId

                                                pending = -1

                                                x.animateTo(
                                                    width,
                                                    tween(220)
                                                )

                                                previousSong()
                                            }

                                            else -> {
                                                x.animateTo(
                                                    0f,
                                                    spring(
                                                        dampingRatio =
                                                            .84f,
                                                        stiffness =
                                                            430f
                                                    )
                                                )
                                            }
                                        }
                                    }

                                    CoverAxis.Vertical -> {
                                        if (
                                            playerY.value >
                                            screenHeight * .13f
                                        ) {
                                            playerY.animateTo(
                                                screenHeight,
                                                tween(300)
                                            )

                                            dismiss()
                                        } else {
                                            playerY.animateTo(
                                                0f,
                                                spring(
                                                    dampingRatio =
                                                        .84f,
                                                    stiffness =
                                                        390f
                                                )
                                            )
                                        }
                                    }

                                    CoverAxis.None ->
                                        Unit
                                }

                                axis = CoverAxis.None
                                rawX = 0f
                                rawY = 0f
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

                                playerY.animateTo(
                                    0f,
                                    tween(180)
                                )

                                axis = CoverAxis.None
                                rawX = 0f
                                rawY = 0f
                            }
                        }
                    )
                }
        ) {
            snapshotPrevious?.let {
                Cover(
                    uri = it,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = 17.dp
                            )
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                translationX =
                                    x.value -
                                        width
                            }
                )
            }

            Cover(
                uri =
                    snapshotCurrent,
                modifier =
                    Modifier
                        .padding(
                            horizontal = 17.dp
                        )
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .align(Alignment.Center)
                        .graphicsLayer {
                            translationX =
                                x.value
                        }
            )

            snapshotNext?.let {
                Cover(
                    uri = it,
                    modifier =
                        Modifier
                            .padding(
                                horizontal = 17.dp
                            )
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .align(Alignment.Center)
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
                Color.Black.copy(alpha = .07f)
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
 * PREMIUM PRESS
 * =============================================================
 */

@Composable
private fun PremiumCircle(
    size: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue =
                if (pressed) {
                    .90f
                } else {
                    1f
                },
            animationSpec =
                spring(
                    dampingRatio = .62f,
                    stiffness = 700f
                ),
            label = "press"
        )

    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha =
                    if (enabled) {
                        if (pressed) .78f else 1f
                    } else {
                        .40f
                    }
            }
            .clip(CircleShape)
            .background(background)
            .clickable(
                interactionSource =
                    interaction,
                indication =
                    null,
                enabled =
                    enabled,
                onClick =
                    onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun XmoCapsule(
    background: Color,
    modifier: Modifier = Modifier,
    content:
        @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .clip(
                RoundedCornerShape(23.dp)
            )
            .background(background)
            .padding(horizontal = 2.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        content =
            content
    )
}

@Composable
private fun CapsuleButton(
    size: Dp = 40.dp,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    PremiumCircle(
        size = size,
        background =
            Color.Transparent,
        onClick =
            onClick,
        content =
            content
    )
}

/*
 * =============================================================
 * SLEEP COUNTDOWN
 * =============================================================
 */

@Composable
private fun SleepControl(
    active: Boolean,
    remaining: Long,
    accent: Color,
    foreground: Color,
    background: Color,
    onClick: () -> Unit
) {
    /*
     * Exact original duration is not exposed by PlaybackState,
     * therefore ring uses a stable nearest selected/custom timer
     * bucket rather than pretending to know unavailable state.
     */
    val reference =
        when {
            remaining >
                45L * 60_000L ->
                60L * 60_000L

            remaining >
                30L * 60_000L ->
                45L * 60_000L

            remaining >
                15L * 60_000L ->
                30L * 60_000L

            else ->
                15L * 60_000L
        }

    val fraction =
        if (
            active &&
            reference > 0L
        ) {
            (
                remaining.toFloat() /
                    reference.toFloat()
                )
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    Box(
        Modifier.size(48.dp),
        contentAlignment =
            Alignment.Center
    ) {
        PremiumCircle(
            size = 44.dp,
            background =
                background,
            onClick =
                onClick
        ) {
            Icon(
                Lucide.Clock3,
                contentDescription =
                    "Sleep timer",
                tint =
                    if (active) {
                        accent
                    } else {
                        foreground
                    },
                modifier =
                    Modifier.size(20.dp)
            )
        }

        if (active) {
            Canvas(
                Modifier.fillMaxSize()
            ) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle =
                        360f * fraction,
                    useCenter = false,
                    style =
                        Stroke(
                            width =
                                2.dp.toPx(),
                            cap =
                                androidx.compose.ui.graphics
                                    .StrokeCap.Round
                        )
                )
            }
        }
    }
}

/*
 * =============================================================
 * ROUNDED SEEK
 * =============================================================
 */

@Composable
private fun RoundedSeekBar(
    position: Long,
    duration: Long,
    active: Color,
    inactive: Color,
    seekTo: (Long) -> Unit
) {
    val fraction =
        if (duration > 0L) {
            (
                position.toFloat() /
                    duration.toFloat()
                )
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    Canvas(
        Modifier
            .fillMaxWidth()
            .height(22.dp)
            .pointerInput(duration) {
                detectTapGestures {
                    if (duration > 0L) {
                        val p =
                            (
                                it.x /
                                    size.width
                                )
                                .coerceIn(
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
            }
    ) {
        val y =
            size.height / 2f

        val stroke =
            2.dp.toPx()

        drawLine(
            color = inactive,
            start =
                Offset(0f, y),
            end =
                Offset(
                    size.width,
                    y
                ),
            strokeWidth =
                stroke,
            cap =
                androidx.compose.ui.graphics
                    .StrokeCap.Round
        )

        if (fraction > 0f) {
            drawLine(
                color = active,
                start =
                    Offset(0f, y),
                end =
                    Offset(
                        size.width * fraction,
                        y
                    ),
                strokeWidth =
                    stroke,
                cap =
                    androidx.compose.ui.graphics
                        .StrokeCap.Round
            )
        }
    }
}

/*
 * =============================================================
 * LYRICS / DETAILS SECTIONS
 * =============================================================
 */

@Composable
private fun BorderSection(
    title: String,
    border: Color,
    c: HomeColors,
    actions:
        @Composable RowScope.() -> Unit = {},
    content:
        @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 1.dp)
            .border(
                .7.dp,
                border,
                RoundedCornerShape(23.dp)
            )
            .padding(15.dp)
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
 * MANUAL SCROLL + 3 SECOND RECENTER
 * =============================================================
 */

@Composable
private fun FollowLyrics(
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
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
                "No local lyrics found.\nTap + to choose an LRC file.",
                color =
                    colors.sub,
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
                        28.sp
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

    val state =
        rememberLazyListState()

    var browsing by remember {
        mutableStateOf(false)
    }

    var generation by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(
        state.isScrollInProgress
    ) {
        if (state.isScrollInProgress) {
            browsing = true
            generation++
        } else if (browsing) {
            val id =
                ++generation

            delay(3_000L)

            if (
                generation == id &&
                !state.isScrollInProgress
            ) {
                browsing = false
            }
        }
    }

    LaunchedEffect(
        active,
        browsing
    ) {
        if (
            lyrics.synced &&
            active >= 0 &&
            !browsing
        ) {
            centerLyric(
                state =
                    state,
                index =
                    active,
                fullscreen =
                    fullscreen
            )
        }
    }

    LazyColumn(
        state = state,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height),
        contentPadding =
            PaddingValues(
                top =
                    if (fullscreen) {
                        260.dp
                    } else {
                        120.dp
                    },
                bottom =
                    if (fullscreen) {
                        300.dp
                    } else {
                        130.dp
                    }
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                if (fullscreen) {
                    22.dp
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

            val scale by
                animateFloatAsState(
                    targetValue =
                        if (selected) {
                            if (fullscreen) {
                                1.09f
                            } else {
                                1.06f
                            }
                        } else {
                            1f
                        },
                    animationSpec =
                        spring(
                            dampingRatio = .76f,
                            stiffness = 420f
                        ),
                    label =
                        "lyric$index"
                )

            Text(
                text =
                    line.text,
                color =
                    if (selected) {
                        accent
                    } else {
                        colors.text.copy(
                            alpha =
                                if (fullscreen) {
                                    .53f
                                } else {
                                    .58f
                                }
                        )
                    },
                fontFamily =
                    if (selected) {
                        XmoFont.bold
                    } else {
                        XmoFont.normal
                    },
                fontSize =
                    when {
                        fullscreen &&
                            selected ->
                            28.sp

                        fullscreen ->
                            20.sp

                        selected ->
                            22.sp

                        else ->
                            17.sp
                    },
                lineHeight =
                    when {
                        fullscreen &&
                            selected ->
                            36.sp

                        fullscreen ->
                            29.sp

                        selected ->
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

private suspend fun centerLyric(
    state: LazyListState,
    index: Int,
    fullscreen: Boolean
) {
    state.animateScrollToItem(
        index =
            index,
        scrollOffset =
            if (fullscreen) {
                -420
            } else {
                -115
            }
    )
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
    isPlaying: Boolean,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    close: () -> Unit
) {
    val foreground =
        if (
            dominant.luminance() >
            .58f
        ) {
            Color(0xFF111214)
        } else {
            Color.White
        }

    val local =
        HomeColors(
            bg =
                Color.Transparent,
            surface =
                Color.Transparent,
            text =
                foreground,
            sub =
                foreground.copy(alpha = .60f),
            button =
                foreground.copy(alpha = .10f),
            icon =
                foreground.copy(alpha = .75f),
            border =
                foreground.copy(alpha = .16f)
        )

    Box(
        Modifier.fillMaxSize()
    ) {
        PlayerBackground(
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
                        start = 15.dp,
                        end = 13.dp
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
                            foreground.copy(alpha = .60f),
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
                        foreground.copy(alpha = .10f),
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

            FollowLyrics(
                lyrics =
                    lyrics,
                position =
                    position,
                colors =
                    local,
                accent =
                    foreground,
                height =
                    androidx.compose.ui.platform
                        .LocalConfiguration.current
                        .screenHeightDp.dp -
                        94.dp,
                fullscreen =
                    true
            )
        }
    }
}

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
            .padding(horizontal = 2.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        CapsuleButton(
            onClick =
                togglePlay
        ) {
            Icon(
                if (isPlaying) {
                    Lucide.Pause
                } else {
                    Lucide.Play
                },
                contentDescription =
                    null,
                tint =
                    foreground,
                modifier =
                    Modifier.size(18.dp)
            )
        }

        CapsuleButton(
            onClick =
                next
        ) {
            Icon(
                Lucide.SkipForward,
                contentDescription =
                    "Next",
                tint =
                    foreground,
                modifier =
                    Modifier.size(17.dp)
            )
        }

        CapsuleButton(
            onClick =
                previous
        ) {
            Icon(
                Lucide.SkipBack,
                contentDescription =
                    "Previous",
                tint =
                    foreground,
                modifier =
                    Modifier.size(17.dp)
            )
        }

        CapsuleButton(
            onClick =
                close
        ) {
            Icon(
                Lucide.X,
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

/*
 * =============================================================
 * XMO HALF OP
 * =============================================================
 */

@Composable
private fun XmoHalfOp(
    title: String,
    c: HomeColors,
    dismiss: () -> Unit,
    content:
        androidx.compose.foundation.lazy
            .LazyListScope.() -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = .44f)
            )
            .clickable(
                onClick = dismiss
            ),
        contentAlignment =
            Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(
                    androidx.compose.ui.platform
                        .LocalConfiguration.current
                        .screenHeightDp.dp *
                        .52f
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
                .background(c.surface)
                .clickable {}
                .padding(
                    top = 12.dp
                )
        ) {
            Box(
                Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .width(42.dp)
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(2.dp)
                    )
                    .background(
                        c.sub.copy(alpha = .30f)
                    )
            )

            Text(
                title,
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 18.sp,
                modifier =
                    Modifier.padding(
                        start = 18.dp,
                        top = 15.dp,
                        bottom = 10.dp
                    )
            )

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 30.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp),
                content =
                    content
            )
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    active: Boolean,
    c: HomeColors
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(
                RoundedCornerShape(13.dp)
            )
            .background(
                if (active) {
                    LocalXmoAccent.current
                        .copy(alpha = .12f)
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
                    .size(48.dp)
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
                    horizontal = 10.dp
                )
        ) {
            Text(
                song.title,
                color =
                    if (active) {
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

        if (active) {
            Icon(
                Lucide.Play,
                contentDescription =
                    null,
                tint =
                    LocalXmoAccent.current,
                modifier =
                    Modifier
                        .padding(end = 10.dp)
                        .size(17.dp)
            )
        }
    }
}

/*
 * =============================================================
 * XMO CENTER BOX
 * =============================================================
 */

@Composable
private fun XmoCenterBox(
    song: Song?,
    categories: List<UserCategory>,
    c: HomeColors,
    liked: Boolean,
    close: () -> Unit,
    toggleLike: () -> Unit,
    share: () -> Unit,
    removeLyrics: () -> Unit,
    setCategory: (
        UserCategory,
        Boolean
    ) -> Unit,
    createCategory: (String) -> Boolean
) {
    var newCategory by remember {
        mutableStateOf("")
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = .48f)
            )
            .clickable(
                onClick = close
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(c.surface)
                .clickable {}
                .padding(16.dp)
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
                        song?.title
                            ?: "Song Options",
                        color = c.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    song?.artist?.let {
                        Text(
                            it,
                            color = c.sub,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                PremiumCircle(
                    38.dp,
                    c.button,
                    onClick =
                        close
                ) {
                    Icon(
                        Lucide.X,
                        contentDescription =
                            "Close",
                        tint = c.text,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }

            Spacer(
                Modifier.height(12.dp)
            )

            CenterAction(
                icon =
                    Lucide.Heart,
                title =
                    if (liked) {
                        "Remove from Liked Songs"
                    } else {
                        "Add to Liked Songs"
                    },
                active =
                    liked,
                c =
                    c,
                click =
                    toggleLike
            )

            CenterAction(
                icon =
                    Lucide.Share2,
                title =
                    "Share Song",
                c =
                    c,
                click =
                    share
            )

            CenterAction(
                icon =
                    Lucide.Trash2,
                title =
                    "Remove Attached Lyrics",
                c =
                    c,
                click =
                    removeLyrics
            )

            Text(
                "CATEGORIES",
                color =
                    LocalXmoAccent.current,
                fontFamily =
                    XmoFont.bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                modifier =
                    Modifier.padding(
                        start = 5.dp,
                        top = 14.dp,
                        bottom = 5.dp
                    )
            )

            categories
                .take(6)
                .forEach { category ->

                    val added =
                        song?.id in
                            category.songIds

                    CenterAction(
                        icon =
                            Lucide.Star,
                        title =
                            category.name,
                        trailing =
                            if (added) {
                                "Added"
                            } else {
                                "Add"
                            },
                        active =
                            added,
                        c =
                            c
                    ) {
                        setCategory(
                            category,
                            !added
                        )
                    }
                }

            Spacer(
                Modifier.height(12.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(c.button)
                    .padding(
                        start = 13.dp,
                        end = 5.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                BasicTextField(
                    value =
                        newCategory,
                    onValueChange = {
                        newCategory =
                            it.take(24)
                    },
                    singleLine =
                        true,
                    textStyle =
                        TextStyle(
                            color = c.text,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 12.sp
                        ),
                    modifier =
                        Modifier.weight(1f),
                    decorationBox = {
                        Box {
                            if (
                                newCategory.isBlank()
                            ) {
                                Text(
                                    "Create category",
                                    color = c.sub,
                                    fontFamily =
                                        XmoFont.normal,
                                    fontSize = 11.sp
                                )
                            }

                            it()
                        }
                    }
                )

                PremiumCircle(
                    36.dp,
                    LocalXmoAccent.current
                        .copy(alpha = .16f),
                    enabled =
                        newCategory
                            .trim()
                            .isNotEmpty(),
                    onClick = {
                        if (
                            createCategory(
                                newCategory
                            )
                        ) {
                            newCategory = ""
                        }
                    }
                ) {
                    Icon(
                        Lucide.Plus,
                        contentDescription =
                            "Create category",
                        tint =
                            LocalXmoAccent.current,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CenterAction(
    icon:
        androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    c: HomeColors,
    trailing: String? = null,
    active: Boolean = false,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(13.dp)
            )
            .clickable(
                onClick = click
            )
            .padding(
                horizontal = 7.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint =
                if (active) {
                    LocalXmoAccent.current
                } else {
                    c.icon
                },
            modifier =
                Modifier.size(18.dp)
        )

        Text(
            title,
            color =
                if (active) {
                    LocalXmoAccent.current
                } else {
                    c.text
                },
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
        )

        trailing?.let {
            Text(
                it,
                color =
                    if (active) {
                        LocalXmoAccent.current
                    } else {
                        c.sub
                    },
                fontFamily =
                    XmoFont.medium,
                fontSize = 9.sp
            )
        }
    }
}

/*
 * =============================================================
 * SLEEP CENTER BOX
 * =============================================================
 */

@Composable
private fun SleepCenterBox(
    c: HomeColors,
    active: Boolean,
    dismiss: () -> Unit,
    setTimer: (
        Long,
        String
    ) -> Unit,
    cancel: () -> Unit
) {
    var customMinutes by remember {
        mutableStateOf("")
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(alpha = .48f)
            )
            .clickable(
                onClick = dismiss
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(horizontal = 30.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(c.surface)
                .clickable {}
                .padding(17.dp)
        ) {
            Text(
                "Sleep Timer",
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 17.sp
            )

            Spacer(
                Modifier.height(9.dp)
            )

            listOf(
                15L to "15 minutes",
                30L to "30 minutes",
                45L to "45 minutes",
                60L to "1 hour"
            ).forEach {
                    (minutes, label) ->

                CenterAction(
                    icon =
                        Lucide.Clock3,
                    title =
                        label,
                    c =
                        c
                ) {
                    setTimer(
                        minutes * 60_000L,
                        label
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(c.button)
                    .padding(
                        horizontal = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                BasicTextField(
                    value =
                        customMinutes,
                    onValueChange = {
                        customMinutes =
                            it.filter(
                                Char::isDigit
                            )
                                .take(4)
                    },
                    singleLine =
                        true,
                    textStyle =
                        TextStyle(
                            color = c.text,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 12.sp
                        ),
                    modifier =
                        Modifier.weight(1f),
                    decorationBox = {
                        Box {
                            if (
                                customMinutes.isBlank()
                            ) {
                                Text(
                                    "Custom minutes",
                                    color = c.sub,
                                    fontFamily =
                                        XmoFont.normal,
                                    fontSize = 11.sp
                                )
                            }

                            it()
                        }
                    }
                )

                PremiumCircle(
                    34.dp,
                    LocalXmoAccent.current
                        .copy(alpha = .16f),
                    enabled =
                        (
                            customMinutes
                                .toLongOrNull()
                                ?: 0L
                            ) >
                            0L,
                    onClick = {
                        val minutes =
                            customMinutes
                                .toLongOrNull()
                                ?: return@PremiumCircle

                        setTimer(
                            minutes * 60_000L,
                            "$minutes min"
                        )
                    }
                ) {
                    Icon(
                        Lucide.TimerReset,
                        contentDescription =
                            "Set custom timer",
                        tint =
                            LocalXmoAccent.current,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }

            if (active) {
                Spacer(
                    Modifier.height(8.dp)
                )

                CenterAction(
                    icon =
                        Lucide.X,
                    title =
                        "Cancel Timer",
                    active =
                        true,
                    c =
                        c,
                    click =
                        cancel
                )
            }
        }
    }
}

/*
 * =============================================================
 * XMO POP
 * =============================================================
 */

@Composable
private fun XmoPop(
    message: String,
    theme: XmoTheme,
    modifier: Modifier = Modifier
) {
    val background =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .94f)

            XmoTheme.Dark ->
                Color(0xFF1C1C1E).copy(
                    alpha = .94f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .96f)
        }

    val foreground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF151515)

            else ->
                Color.White
        }

    Box(
        modifier
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(background)
            .padding(
                horizontal = 17.dp,
                vertical = 11.dp
            )
    ) {
        Text(
            message,
            color = foreground,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
            textAlign =
                TextAlign.Center
        )
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
            maxLines = 2,
            textAlign =
                TextAlign.End,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.weight(1f)
        )
    }
}

/*
 * =============================================================
 * DOWN GESTURE
 * =============================================================
 */

private fun Modifier.downGesture(
    y: Animatable<Float, *>,
    height: Float,
    dismiss: () -> Unit
): Modifier =
    pointerInput(height) {
        coroutineScope {
            detectDragGestures(
                onDrag = { change, amount ->
                    if (
                        amount.y > 0f ||
                        y.value > 0f
                    ) {
                        change.consume()

                        launch {
                            y.snapTo(
                                (y.value + amount.y)
                                    .coerceIn(
                                        0f,
                                        height
                                    )
                            )
                        }
                    }
                },

                onDragEnd = {
                    launch {
                        if (
                            y.value >
                            height * .13f
                        ) {
                            y.animateTo(
                                height,
                                tween(300)
                            )

                            dismiss()
                        } else {
                            y.animateTo(
                                0f,
                                spring(
                                    dampingRatio = .84f,
                                    stiffness = 390f
                                )
                            )
                        }
                    }
                },

                onDragCancel = {
                    launch {
                        y.animateTo(
                            0f,
                            tween(180)
                        )
                    }
                }
            )
        }
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

    val stamp =
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
                stamp
                    .findAll(raw)
                    .toList()

            if (stamps.isNotEmpty()) {
                val text =
                    stamp
                        .replace(raw, "")
                        .trim()

                if (text.isNotEmpty()) {
                    stamps.forEach { match ->

                        val minute =
                            match
                                .groupValues[1]
                                .toLongOrNull()
                                ?: 0L

                        val second =
                            match
                                .groupValues[2]
                                .toLongOrNull()
                                ?: 0L

                        val fractionText =
                            match.groupValues[3]

                        val fraction =
                            when (
                                fractionText.length
                            ) {
                                1 ->
                                    (
                                        fractionText
                                            .toLongOrNull()
                                            ?: 0L
                                        ) *
                                        100L

                                2 ->
                                    (
                                        fractionText
                                            .toLongOrNull()
                                            ?: 0L
                                        ) *
                                        10L

                                3 ->
                                    fractionText
                                        .toLongOrNull()
                                        ?: 0L

                                else ->
                                    0L
                            }

                        output +=
                            LyricLine(
                                timeMs =
                                    minute *
                                        60_000L +
                                        second *
                                        1_000L +
                                        fraction,
                                text =
                                    text
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

    var result = -1

    lyrics.lines
        .forEachIndexed {
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
 * COLORS
 * =============================================================
 */

private fun liftArtworkColor(
    color: Color
): Color {
    /*
     * Avoid near-black / muddy player backgrounds while preserving
     * artwork hue.
     */
    val minimum = .22f

    val max =
        maxOf(
            color.red,
            color.green,
            color.blue
        )

    if (max >= minimum) {
        return color
    }

    if (max <= .001f) {
        return Color(
            0xFF4A4D55
        )
    }

    val multiplier =
        minimum / max

    return Color(
        red =
            (
                color.red *
                    multiplier
                ).coerceIn(0f, 1f),
        green =
            (
                color.green *
                    multiplier
                ).coerceIn(0f, 1f),
        blue =
            (
                color.blue *
                    multiplier
                ).coerceIn(0f, 1f),
        alpha = 1f
    )
}

private fun mix(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val value =
        fraction.coerceIn(0f, 1f)

    return Color(
        red =
            from.red +
                (to.red - from.red) *
                value,
        green =
            from.green +
                (to.green - from.green) *
                value,
        blue =
            from.blue +
                (to.blue - from.blue) *
                value,
        alpha = 1f
    )
}

/*
 * =============================================================
 * TIME / SIZE
 * =============================================================
 */

private fun playerTime(
    milliseconds: Long
): String {
    val seconds =
        milliseconds
            .coerceAtLeast(0L) /
            1000L

    val hours =
        seconds / 3600L

    val minutes =
        (seconds % 3600L) / 60L

    val remainder =
        seconds % 60L

    return if (hours > 0L) {
        "$hours:${
            minutes.toString()
                .padStart(2, '0')
        }:${
            remainder.toString()
                .padStart(2, '0')
        }"
    } else {
        "$minutes:${
            remainder.toString()
                .padStart(2, '0')
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

        bytes >= 1024L ->
            String.format(
                "%.1f KB",
                bytes.toDouble() /
                    1024.0
            )

        else ->
            "$bytes B"
    }
