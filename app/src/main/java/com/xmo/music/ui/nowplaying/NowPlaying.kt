package com.xmo.music.ui.nowplaying

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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.Artwork
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import com.xmo.music.ui.homeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

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
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val colors =
        homeColors(theme)

    val accent =
        LocalXmoAccent.current

    /*
     * =========================================================
     * CURRENT QUEUE WINDOW
     * =========================================================
     */

    val currentIndex =
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
            currentIndex
        )

    val previousSong =
        queue.getOrNull(
            currentIndex - 1
        )

    val nextSong =
        queue.getOrNull(
            currentIndex + 1
        )

    val songIsInCategory =
        currentSong?.let { song ->
            categories.any { category ->
                song.id in
                    category.songIds
            }
        } == true

    /*
     * =========================================================
     * ARTWORK COLORS
     * =========================================================
     */

    var currentColor by remember {
        mutableStateOf(
            Color(0xFF52545A)
        )
    }

    var previousColor by remember {
        mutableStateOf(
            Color(0xFF52545A)
        )
    }

    var nextColor by remember {
        mutableStateOf(
            Color(0xFF52545A)
        )
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
                    ?: state.artworkUri
                        ?.let(Uri::parse)
            )
    }

    LaunchedEffect(
        previousSong?.artwork
    ) {
        previousColor =
            previousSong
                ?.artwork
                ?.let {
                    extractBright(it)
                }
                ?: currentColor
    }

    LaunchedEffect(
        nextSong?.artwork
    ) {
        nextColor =
            nextSong
                ?.artwork
                ?.let {
                    extractBright(it)
                }
                ?: currentColor
    }

    val animatedCurrentColor by
        animateColorAsState(
            targetValue =
                currentColor,
            animationSpec =
                tween(390),
            label =
                "playerDominant"
        )

    /*
     * =========================================================
     * ARTWORK TRANSACTION
     * =========================================================
     */

    val coverX =
        remember {
            Animatable(0f)
        }

    var coverWidth by remember {
        mutableFloatStateOf(1f)
    }

    /*
     * This color is deliberately frozen while the cover is away
     * from its resting point. Otherwise currentColor changing
     * after Media3 confirmation can visually snap the background.
     */
    var transactionBaseColor by remember {
        mutableStateOf(
            currentColor
        )
    }

    var lastRestingSongId by remember {
        mutableStateOf<Long?>(null)
    }

    val coverFraction =
        (
            coverX.value /
                coverWidth
            )
            .coerceIn(
                -1f,
                1f
            )

    LaunchedEffect(
        state.currentSongId,
        coverX.value,
        animatedCurrentColor
    ) {
        if (
            abs(coverX.value) <
            1f
        ) {
            transactionBaseColor =
                animatedCurrentColor

            lastRestingSongId =
                state.currentSongId
        }
    }

    val colorBase =
        if (
            abs(coverFraction) >
            .001f
        ) {
            transactionBaseColor
        } else {
            animatedCurrentColor
        }

    val colorDestination =
        when {
            coverFraction < 0f ->
                nextColor

            coverFraction > 0f ->
                previousColor

            else ->
                colorBase
        }

    val displayColor =
        mixColor(
            from = colorBase,
            to = colorDestination,
            fraction =
                abs(coverFraction)
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

    /*
     * Main playback controls remain white/light as requested.
     * They no longer receive circular surfaces.
     */
    val controlForeground =
        if (
            theme ==
            XmoTheme.Light &&
            displayColor.luminance() >
            .72f
        ) {
            Color(0xFF171719)
        } else {
            Color.White
        }

    val panel =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .40f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .27f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .45f
                )
        }

    val border =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .14f
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
     * REAL PLAYBACK POSITION POLLING
     * =========================================================
     */

    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (isActive) {
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
     * LOCAL LYRICS
     * =========================================================
     */

    var fileLyrics by remember(
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
            ?: currentSong
                ?.embeddedLyrics

    val lyricPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .OpenDocument()
        ) { uri ->
            if (uri != null) {
                runCatching {
                    context
                        .contentResolver
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
     * SCREEN UI STATE
     * =========================================================
     */

    var overlay by remember {
        mutableStateOf<PlayerOverlay?>(
            null
        )
    }

    var artworkLyrics by remember(
        state.currentSongId
    ) {
        mutableStateOf(false)
    }

    var fullLyrics by remember {
        mutableStateOf(false)
    }

    var pop by remember {
        mutableStateOf<PopMessage?>(
            null
        )
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
            targetValue = 0f,
            animationSpec =
                tween(410)
        )

        onOpened()
    }

    LaunchedEffect(
        pop?.key
    ) {
        if (pop != null) {
            delay(1_900L)

            pop = null
        }
    }

    suspend fun closePlayer() {
        if (dismissing) {
            return
        }

        dismissing = true

        playerY.animateTo(
            targetValue =
                screenHeight,
            animationSpec =
                tween(330)
        )

        dismiss()
    }

    /*
     * Back chain:
     *
     * fullscreen lyrics
     * -> cover-size lyrics
     * -> artwork
     * -> player close
     */
    BackHandler {
        when {
            fullLyrics -> {
                fullLyrics = false
                artworkLyrics = true
            }

            overlay != null -> {
                overlay = null
            }

            artworkLyrics -> {
                artworkLyrics = false
            }

            else -> {
                scope.launch {
                    closePlayer()
                }
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
                    playerY.value +
                        (
                            entrance.value *
                                screenHeight
                            )
            }
            .clip(
                RoundedCornerShape(
                    topStart =
                        (
                            88f *
                                (
                                    playerY.value /
                                        screenHeight
                                    )
                                    .coerceIn(
                                        0f,
                                        1f
                                    )
                            ).dp,
                    topEnd =
                        (
                            88f *
                                (
                                    playerY.value /
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
        PlayerBackground(
            dominant =
                displayColor,
            deep = deep,
            theme = theme
        )

        /*
         * There is deliberately NO verticalScroll here.
         *
         * Main Now Playing is now a fixed screen.
         */
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            /*
             * =================================================
             * HEADER
             * =================================================
             *
             * Player dismissal drag lives here only.
             * Artwork cannot dismiss the player anymore.
             */

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .padding(
                        horizontal = 14.dp
                    )
                    .headerDownGesture(
                        y = playerY,
                        height =
                            screenHeight,
                        dismiss = {
                            if (
                                !dismissing
                            ) {
                                dismissing =
                                    true

                                dismiss()
                            }
                        }
                    )
            ) {
                PremiumCircle(
                    size = 40.dp,
                    background =
                        overlayText.copy(
                            alpha = .10f
                        ),
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
                        tint =
                            overlayText,
                        modifier =
                            Modifier.size(
                                22.dp
                            )
                    )
                }

                Column(
                    Modifier
                        .align(
                            Alignment.Center
                        )
                        .width(180.dp),
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
                            overlayText.copy(
                                alpha = .68f
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 12.sp,
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
                        overlayText.copy(
                            alpha = .10f
                        ),
                    modifier =
                        Modifier.align(
                            Alignment.CenterEnd
                        )
                ) {
                    CapsuleButton(
                        onClick = {
                            currentSong
                                ?.let {
                                    shareSong(
                                        context,
                                        it
                                    )
                                }
                        }
                    ) {
                        Icon(
                            imageVector =
                                Lucide.Share2,
                            contentDescription =
                                "Share",
                            tint =
                                overlayText,
                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )
                    }

                    CapsuleButton(
                        onClick = {
                            overlay =
                                PlayerOverlay.Options
                        }
                    ) {
                        Icon(
                            imageVector =
                                Lucide.EllipsisVertical,
                            contentDescription =
                                "Options",
                            tint =
                                overlayText,
                            modifier =
                                Modifier.size(
                                    19.dp
                                )
                        )
                    }
                }
            }

            /*
             * Artwork was 68dp below header.
             *
             * Requested +25dp:
             * 68 + 25 = 93dp.
             */
            Spacer(
                Modifier.height(93.dp)
            )

            /*
             * =================================================
             * ARTWORK / COVER-SIZE LYRICS
             * =================================================
             */

            PlayerArtwork(
                currentId =
                    state.currentSongId,
                current =
                    currentSong
                        ?.artwork
                        ?: state
                            .artworkUri
                            ?.let(
                                Uri::parse
                            ),
                previous =
                    previousSong
                        ?.artwork,
                next =
                    nextSong
                        ?.artwork,
                canPrevious =
                    state.hasPrevious,
                canNext =
                    state.hasNext,
                x = coverX,
                setWidth = {
                    coverWidth =
                        it.coerceAtLeast(
                            1f
                        )
                },
                showLyrics =
                    artworkLyrics,
                lyrics = lyrics,
                position =
                    state.position,
                colors = colors,
                accent = accent,
                previousSong =
                    previousItem,
                nextSong = next,
                toggleLyrics = {
                    artworkLyrics =
                        !artworkLyrics
                },
                pickLyrics = {
                    lyricPicker.launch(
                        arrayOf("*/*")
                    )
                },
                fullscreenLyrics = {
                    fullLyrics = true
                }
            )

            /*
             * Old gap was 104dp. The artwork itself has already
             * moved 25dp down, therefore we don't compensate by
             * pulling the body upward.
             *
             * Reduced only enough to fit the fixed viewport.
             */
            Spacer(
                Modifier.height(34.dp)
            )

            /*
             * =================================================
             * FIXED PLAYER PANEL
             * =================================================
             */

            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 28.dp,
                            topEnd = 28.dp
                        )
                    )
                    .background(panel)
                    .padding(
                        start = 12.dp,
                        top = 22.dp,
                        end = 12.dp,
                        bottom = 6.dp
                    )
            ) {
                /*
                 * =================================================
                 * TITLE + FIVE BUTTON CAPSULE
                 * =================================================
                 */

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 4.dp
                        ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(
                                end = 7.dp
                            )
                    ) {
                        Text(
                            text =
                                state.title
                                    .ifBlank {
                                        "Unknown song"
                                    },
                            color =
                                colors.text,
                            fontFamily =
                                XmoFont.bold,
                            fontSize = 19.sp,
                            maxLines = 1,
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
                                colors.sub,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    /*
                     * Requested one connected capsule:
                     *
                     * Like / Category / Timer / Queue / Details
                     */
                    XmoCapsule(
                        background =
                            colors.button
                    ) {
                        CapsuleButton(
                            size = 35.dp,
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
                            FilledHeart(
                                filled = liked,
                                color =
                                    if (liked) {
                                        accent
                                    } else {
                                        colors.icon
                                    }
                            )
                        }

                        CapsuleButton(
                            size = 35.dp,
                            onClick = {
                                overlay =
                                    PlayerOverlay.Options
                            }
                        ) {
                            FilledStar(
                                filled =
                                    songIsInCategory,
                                color =
                                    if (
                                        songIsInCategory
                                    ) {
                                        accent
                                    } else {
                                        colors.icon
                                    }
                            )
                        }

                        CapsuleButton(
                            size = 35.dp,
                            onClick = {
                                overlay =
                                    PlayerOverlay.Sleep
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Lucide.Clock3,
                                contentDescription =
                                    "Sleep timer",
                                tint =
                                    if (
                                        state.sleepTimerRemainingMs >
                                        0L
                                    ) {
                                        accent
                                    } else {
                                        colors.icon
                                    },
                                modifier =
                                    Modifier.size(
                                        17.dp
                                    )
                            )
                        }

                        CapsuleButton(
                            size = 35.dp,
                            onClick = {
                                overlay =
                                    PlayerOverlay.Queue
                            }
                        ) {
                            Icon(
                                imageVector =
                                    Lucide.ListMusic,
                                contentDescription =
                                    "Queue",
                                tint =
                                    colors.icon,
                                modifier =
                                    Modifier.size(
                                        17.dp
                                    )
                            )
                        }

                        CapsuleButton(
                            size = 35.dp,
                            onClick = {
                                overlay =
                                    PlayerOverlay.Details
                            }
                        ) {
                            Text(
                                text = "?",
                                color =
                                    colors.icon,
                                fontFamily =
                                    XmoFont.bold,
                                fontSize = 17.sp
                            )
                        }
                    }
                }

                /*
                 * Title/artist are now much closer to progress.
                 */
                Spacer(
                    Modifier.height(8.dp)
                )

                /*
                 * =================================================
                 * DRAGGABLE PROGRESS
                 * =================================================
                 *
                 * RoundedSeekBar itself contains the requested
                 * additional 4dp top displacement.
                 */

                RoundedSeekBar(
                    position =
                        state.position,
                    duration =
                        state.duration,
                    active = accent,
                    inactive = border,
                    seekTo = seekTo
                )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = 4.dp
                        ),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        text =
                            playerTime(
                                state.position
                            ),
                        color =
                            colors.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )

                    Text(
                        text =
                            playerTime(
                                state.duration
                            ),
                        color =
                            colors.sub,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )
                }

                Spacer(
                    Modifier.height(8.dp)
                )

                /*
                 * =================================================
                 * FIVE MAIN CONTROLS
                 * =================================================
                 */

                PlayerControls(
                    isPlaying =
                        state.isPlaying,
                    hasPrevious =
                        state.hasPrevious,
                    hasNext =
                        state.hasNext,
                    shuffleEnabled =
                        state.shuffleEnabled,
                    repeatMode =
                        state.repeatMode,
                    foreground =
                        controlForeground,
                    accent = accent,
                    togglePlay =
                        togglePlay,
                    previous =
                        previous,
                    next = next,
                    toggleShuffle =
                        toggleShuffle,
                    cycleRepeat =
                        cycleRepeat
                )

                /*
                 * No Lyrics/Artist/Details scrolling sections.
                 */
                Spacer(
                    Modifier.weight(1f)
                )

                /*
                 * Tiny footer requested.
                 */
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "XMO",
                        color =
                            colors.text.copy(
                                alpha = .65f
                            ),
                        fontFamily =
                            XmoFont.logo,
                        fontSize = 11.sp
                    )

                    Text(
                        text =
                            "lxzrvi • © 2026",
                        color =
                            colors.sub.copy(
                                alpha = .52f
                            ),
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 6.sp
                    )
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(5.dp)
                )
            }
        }

        /*
         * =====================================================
         * QUEUE
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Queue
        ) {
            QueueSheet(
                queue = queue,
                currentSongId =
                    state.currentSongId,
                colors = colors,
                dismiss = {
                    overlay = null
                }
            )
        }

        /*
         * =====================================================
         * OPTIONS
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Options
        ) {
            SongOptionsBox(
                song = currentSong,
                categories =
                    categories,
                colors = colors,
                liked = liked,
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
                        shareSong(
                            context,
                            it
                        )
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
                createCategory = {
                        name ->

                    val created =
                        createCategory(name)

                    if (
                        created != null
                    ) {
                        /*
                         * Existing App contract decides whether
                         * the newly created category automatically
                         * contains this song.
                         */
                        pop =
                            PopMessage(
                                "Created ${created.name}"
                            )
                    }

                    created != null
                }
            )
        }

        /*
         * =====================================================
         * SLEEP TIMER
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Sleep
        ) {
            SleepTimerBox(
                colors = colors,
                active =
                    state.sleepTimerRemainingMs >
                        0L,
                dismiss = {
                    overlay = null
                },
                setTimer = {
                        duration,
                        label ->

                    setSleepTimer(
                        duration
                    )

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
         * DETAILS
         * =====================================================
         */

        if (
            overlay ==
            PlayerOverlay.Details
        ) {
            SongDetailsBox(
                song =
                    currentSong,
                album =
                    state.album,
                colors = colors,
                close = {
                    overlay = null
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
                lyrics = lyrics,
                position =
                    state.position,
                duration =
                    state.duration,
                title =
                    state.title,
                artist =
                    state.artist,
                artwork =
                    currentSong
                        ?.artwork,
                dominant =
                    displayColor,
                deep = deep,
                theme = theme,
                isPlaying =
                    state.isPlaying,
                canPrevious =
                    state.hasPrevious,
                canNext =
                    state.hasNext,
                togglePlay =
                    togglePlay,
                previous =
                    previous,
                next = next,
                seekTo = seekTo,
                close = {
                    fullLyrics = false

                    /*
                     * Fullscreen collapses logically back to the
                     * artwork-sized lyrics surface.
                     */
                    artworkLyrics = true
                }
            )
        }

        /*
         * =====================================================
         * POP
         * =====================================================
         */

        pop?.let {
            XmoPop(
                message = it.text,
                theme = theme,
                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .statusBarsPadding()
                        .padding(
                            top = 72.dp
                        )
            )
        }
    }
}

/*
 * =============================================================
 * FILLED LIKE
 * =============================================================
 *
 * This is a real filled shape when active rather than merely
 * changing the tint of an outline icon.
 */

@Composable
private fun FilledHeart(
    filled: Boolean,
    color: Color
) {
    Canvas(
        Modifier.size(19.dp)
    ) {
        val path =
            Path()

        val w =
            size.width

        val h =
            size.height

        path.moveTo(
            w * .50f,
            h * .88f
        )

        path.cubicTo(
            w * .43f,
            h * .80f,
            w * .10f,
            h * .59f,
            w * .10f,
            h * .32f
        )

        path.cubicTo(
            w * .10f,
            h * .14f,
            w * .23f,
            h * .07f,
            w * .35f,
            h * .07f
        )

        path.cubicTo(
            w * .43f,
            h * .07f,
            w * .48f,
            h * .12f,
            w * .50f,
            h * .18f
        )

        path.cubicTo(
            w * .53f,
            h * .12f,
            w * .58f,
            h * .07f,
            w * .66f,
            h * .07f
        )

        path.cubicTo(
            w * .80f,
            h * .07f,
            w * .90f,
            h * .17f,
            w * .90f,
            h * .32f
        )

        path.cubicTo(
            w * .90f,
            h * .59f,
            w * .57f,
            h * .80f,
            w * .50f,
            h * .88f
        )

        path.close()

        if (filled) {
            drawPath(
                path = path,
                color = color
            )
        } else {
            drawPath(
                path = path,
                color = color,
                style =
                    androidx.compose.ui.graphics
                        .drawscope.Stroke(
                            width =
                                1.8.dp.toPx()
                        )
            )
        }
    }
}

/*
 * =============================================================
 * FILLED CATEGORY STAR
 * =============================================================
 */

@Composable
private fun FilledStar(
    filled: Boolean,
    color: Color
) {
    Canvas(
        Modifier.size(18.dp)
    ) {
        val center =
            Offset(
                size.width / 2f,
                size.height / 2f
            )

        val outer =
            size.minDimension *
                .47f

        val inner =
            outer * .43f

        val path =
            Path()

        repeat(10) { index ->
            val radius =
                if (
                    index % 2 == 0
                ) {
                    outer
                } else {
                    inner
                }

            val angle =
                (
                    -90.0 +
                        index * 36.0
                    ) *
                    Math.PI /
                    180.0

            val point =
                Offset(
                    x =
                        center.x +
                            (
                                cos(angle) *
                                    radius
                                ).toFloat(),
                    y =
                        center.y +
                            (
                                sin(angle) *
                                    radius
                                ).toFloat()
                )

            if (index == 0) {
                path.moveTo(
                    point.x,
                    point.y
                )
            } else {
                path.lineTo(
                    point.x,
                    point.y
                )
            }
        }

        path.close()

        if (filled) {
            drawPath(
                path = path,
                color = color
            )
        } else {
            drawPath(
                path = path,
                color = color,
                style =
                    androidx.compose.ui.graphics
                        .drawscope.Stroke(
                            width =
                                1.7.dp.toPx()
                        )
            )
        }
    }
}

/*
 * =============================================================
 * HEADER-ONLY DISMISS GESTURE
 * =============================================================
 */

private fun Modifier.headerDownGesture(
    y: Animatable<Float, *>,
    height: Float,
    dismiss: () -> Unit
): Modifier =
    pointerInput(height) {
        kotlinx.coroutines.coroutineScope {
            detectDragGestures(
                onDrag = {
                        change,
                        amount ->

                    if (
                        amount.y > 0f ||
                        y.value > 0f
                    ) {
                        change.consume()

                        launch {
                            y.snapTo(
                                (
                                    y.value +
                                        amount.y
                                    )
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
                                targetValue =
                                    height,
                                animationSpec =
                                    tween(300)
                            )

                            dismiss()
                        } else {
                            y.animateTo(
                                targetValue = 0f,
                                animationSpec =
                                    spring(
                                        dampingRatio =
                                            .84f,
                                        stiffness =
                                            390f
                                    )
                            )
                        }
                    }
                },

                onDragCancel = {
                    launch {
                        y.animateTo(
                            targetValue = 0f,
                            animationSpec =
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
                song.metadata
                    ?.mimeType
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
