package com.xmo.music.ui.nowplaying

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.homeColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@Composable
fun NowPlaying(
    state: PlaybackState,
    theme: XmoTheme,
    source: String,
    sourceIsCategory: Boolean,
    queue: List<Song>,
    songs: List<Song>,
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
     * QUEUE WINDOW
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

    val inCategory =
        currentSong?.let { song ->
            categories.any {
                song.id in
                    it.songIds
            }
        } == true

    val artistTrackCount =
        remember(
            songs,
            currentSong?.artist
        ) {
            val artist =
                currentSong
                    ?.artist
                    ?.trim()
                    .orEmpty()

            if (artist.isBlank()) {
                0
            } else {
                songs.count {
                    it.artist
                        .trim()
                        .equals(
                            artist,
                            ignoreCase = true
                        )
                }
            }
        }

    /*
     * =========================================================
     * ARTWORK / COLORS
     * =========================================================
     */

    val coverX =
        remember {
            Animatable(0f)
        }

    var coverWidth by remember {
        mutableFloatStateOf(1f)
    }

    val fallbackArtwork =
        state.artworkUri
            ?.let(Uri::parse)

    val artworkColors =
        rememberPlayerColors(
            context = context,
            currentArtwork =
                currentSong?.artwork,
            fallbackArtwork =
                fallbackArtwork,
            previousArtwork =
                previousSong?.artwork,
            nextArtwork =
                nextSong?.artwork,
            currentSongId =
                state.currentSongId,
            coverX = coverX
        )

    val displayColor =
        playerDisplayColor(
            colors = artworkColors,
            coverX = coverX.value,
            coverWidth = coverWidth
        )

    val deep =
        com.xmo.music.ui.Artwork.deep(
            displayColor,
            theme
        )

    val themeColors =
        playerThemeColors(
            theme = theme,
            displayColor =
                displayColor
        )

    var sleepTotalMs by remember {
        mutableStateOf<Long?>(null)
    }
    /*
     * =========================================================
     * POSITION
     * =========================================================
     */

    LaunchedEffect(
        state.sleepTimerRemainingMs
    ) {
        if (
            state.sleepTimerRemainingMs <=
            0L
        ) {
            sleepTotalMs = null
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
     * UI STATE
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
     * PLAYER MOTION
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

    LaunchedEffect(pop?.key) {
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
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
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
                        entrance.value *
                        screenHeight
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

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            PlayerHeader(
                source = source,
                sourceIsCategory =
                    sourceIsCategory,
                foreground =
                    themeColors.overlayText,
                playerY = playerY,
                screenHeight =
                    screenHeight,
                close = {
                    closePlayer()
                },
                dismissAfterDrag = {
                    if (!dismissing) {
                        dismissing = true
                        dismiss()
                    }
                },
                share = {
                    currentSong?.let {
                        shareSong(
                            context,
                            it
                        )
                    }
                },
                options = {
                    overlay =
                        PlayerOverlay.Options
                }
            )

            /*
             * Lower artwork position retained.
             */
            Spacer(
                Modifier.height(93.dp)
            )

            PlayerArtwork(
                currentId =
                    state.currentSongId,
                current =
                    currentSong?.artwork
                        ?: fallbackArtwork,
                previous =
                    previousSong?.artwork,
                next =
                    nextSong?.artwork,
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
                theme = theme,
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
             * Requested artwork -> panel gap.
             */
            Spacer(
                Modifier.height(95.dp)
            )

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
                    .background(
                        themeColors.panel
                    )
                    .padding(
                        start = 12.dp,
                        top = 15.dp,
                        end = 12.dp,
                        bottom = 4.dp
                    )
            ) {
                /*
                 * Capsule stays anchored at top of PlayerInfo.
                 * Title/artist live independently lower.
                 */
                PlayerInfo(
                    title = state.title,
                    artist =
                        state.artist,
                    liked = liked,
                    inCategory =
                        inCategory,
                    sleepRemainingMs =
                        state.sleepTimerRemainingMs,
                    sleepTotalMs =
                        sleepTotalMs,
                    colors = colors,
                    accent = accent,
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
                    openCategories = {
                        overlay =
                            PlayerOverlay.Options
                    },
                    openSleep = {
                        overlay =
                            PlayerOverlay.Sleep
                    },
                    openQueue = {
                        overlay =
                            PlayerOverlay.Queue
                    },
                    openDetails = {
                        overlay =
                            PlayerOverlay.Details
                    },
                    openArtist = {
                        overlay =
                            PlayerOverlay.Artist
                    }
                )

                Spacer(
                    Modifier.weight(5.dp)
                )

                PlayerBody(
                    position =
                        state.position,
                    duration =
                        state.duration,
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
                    colors = colors,
                    accent = accent,
                    border =
                        themeColors.border,
                    controlForeground =
                        themeColors.controls,
                    playBackground =
                        themeColors.playBackground,
                    seekTo = seekTo,
                    togglePlay =
                        togglePlay,
                    previous = previous,
                    next = next,
                    toggleShuffle =
                        toggleShuffle,
                    cycleRepeat =
                        cycleRepeat
                )
            }
        }

        /*
         * =====================================================
         * OVERLAYS
         * =====================================================
         */

        when (overlay) {
            PlayerOverlay.Queue -> {
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

            PlayerOverlay.Options -> {
                SongOptionsBox(
                    song =
                        currentSong,
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

                        if (
                            created != null
                        ) {
                            pop =
                                PopMessage(
                                    "Created ${created.name}"
                                )
                        }

                        created != null
                    }
                )
            }

            PlayerOverlay.Sleep -> {
                SleepTimerBox(
                    colors = colors,
                    active =
                        state.sleepTimerRemainingMs >
                            0L,
                    dismiss = {
                        overlay = null
                    },
                    setTimer = { duration, label ->
                        sleepTotalMs = duration
                        setSleepTimer(duration)
                    
                        overlay = null
                    
                        pop =
                            PopMessage(
                                "Sleep timer set for $label"
                            )
                    },
                    cancel = {
                        sleepTotalMs = null
                        cancelSleepTimer()
                    
                        overlay = null
                    
                        pop =
                            PopMessage(
                                "Sleep timer cancelled"
                            )
                    }
                )
            }

            PlayerOverlay.Details -> {
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

            PlayerOverlay.Artist -> {
                ArtistInfoBox(
                    artist =
                        state.artist,
                    trackCount =
                        artistTrackCount,
                    colors = colors,
                    close = {
                        overlay = null
                    }
                )
            }

            null -> Unit
        }

        /*
         * =====================================================
         * FULL LYRICS
         * =====================================================
         *
         * Scale + fade gives both entry and exit. It remains a
         * state transition back to the cover-sized lyrics surface.
         */

        AnimatedVisibility(
            visible = fullLyrics,
            enter =
                fadeIn(
                    tween(260)
                ) +
                    scaleIn(
                        initialScale = .94f,
                        animationSpec =
                            spring(
                                dampingRatio = .86f,
                                stiffness = 330f
                            )
                    ),
            exit =
                fadeOut(
                    tween(210)
                ) +
                    scaleOut(
                        targetScale = .94f,
                        animationSpec =
                            tween(250)
                    )
        ) {
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
                    currentSong?.artwork,
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
                    artworkLyrics = true
                }
            )
        }

        pop?.let {
            XmoPop(
                message = it.text,
                theme = theme,
                modifier =
                    Modifier
                        .align(
                            androidx.compose.ui.Alignment.TopCenter
                        )
                        .statusBarsPadding()
                        .padding(
                            top = 72.dp
                        )
            )
        }
    }
}

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
