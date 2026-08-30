package com.xmo.music.ui.nowplaying

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.Artwork
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
    playQueueIndex: (Int) -> Unit,
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

    val configuration =
        LocalConfiguration.current

    val density =
        LocalDensity.current

    val scope =
        rememberCoroutineScope()

    val colors =
        homeColors(
            theme
        )

    val accent =
        LocalXmoAccent.current

    /*
     * Real screen height exists on the first composition.
     *
     * This replaces the previous 1px / measurement-gated setup
     * that could make Now Playing never render.
     */
    val screenHeight =
        with(density) {
            configuration
                .screenHeightDp
                .dp
                .toPx()
        }
        .coerceAtLeast(
            1f
        )

    /*
     * =========================================================
     * REAL QUEUE WINDOW
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

    val fallbackArtwork =
        state.artworkUri
            ?.let(
                Uri::parse
            )

    /*
     * =========================================================
     * REAL CURRENT SONG DATA
     * =========================================================
     */

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

            if (
                artist.isBlank()
            ) {
                0
            } else {
                songs.count {
                    it.artist
                        .trim()
                        .equals(
                            artist,
                            ignoreCase =
                                true
                        )
                }
            }
        }

    /*
     * =========================================================
     * CAROUSEL
     * =========================================================
     */

    val carousel =
        remember {
            PlayerCarouselState(
                initialId =
                    state.currentSongId,
                initialIndex =
                    currentIndex,
                initialCurrent =
                    currentSong?.artwork
                        ?: fallbackArtwork,
                initialPrevious =
                    previousSong?.artwork,
                initialNext =
                    nextSong?.artwork
            )
        }

    LaunchedEffect(
        state.currentSongId,
        currentIndex,
        currentSong?.artwork,
        previousSong?.artwork,
        nextSong?.artwork,
        fallbackArtwork
    ) {
        carousel.initializeIfEmpty(
            id =
                state.currentSongId,
            index =
                currentIndex,
            current =
                currentSong?.artwork
                    ?: fallbackArtwork,
            previous =
                previousSong?.artwork,
            next =
                nextSong?.artwork
        )

        carousel.updateIdleWindow(
            id =
                state.currentSongId,
            index =
                currentIndex,
            current =
                currentSong?.artwork
                    ?: fallbackArtwork,
            previous =
                previousSong?.artwork,
            next =
                nextSong?.artwork
        )
    }

    /*
     * =========================================================
     * ARTWORK COLOR
     * =========================================================
     */

    val artworkColors =
        rememberPlayerColors(
            context =
                context,
            carousel =
                carousel
        )

    val displayColor =
        playerDisplayColor(
            colors =
                artworkColors,
            coverX =
                carousel.x.value,
            coverWidth =
                carousel.width
        )

    val deep =
        Artwork.deep(
            displayColor,
            theme
        )

    val themeColors =
        playerThemeColors(
            theme =
                theme,
            displayColor =
                displayColor
        )

    /*
     * =========================================================
     * POSITION
     * =========================================================
     */

    LaunchedEffect(
        state.currentSongId,
        state.isPlaying
    ) {
        while (
            isActive
        ) {
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

    var fileLyrics by
        remember(
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
                    Uri.parse(
                        it
                    )
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
            if (
                uri != null
            ) {
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
     * PLAYER UI STATE
     * =========================================================
     */

    var overlay by
        remember {
            mutableStateOf<PlayerOverlay?>(
                null
            )
        }

    /*
     * Deliberately independent from current song.
     */
    var artworkLyrics by
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

    var pop by
        remember {
            mutableStateOf<PopMessage?>(
                null
            )
        }

    var sleepTotalMs by
        remember {
            mutableStateOf<Long?>(
                null
            )
        }

    LaunchedEffect(
        state.sleepTimerRemainingMs
    ) {
        if (
            state.sleepTimerRemainingMs <=
            0L
        ) {
            sleepTotalMs =
                null
        }
    }

    /*
     * =========================================================
     * PLAYER ENTRANCE / CLOSE
     * =========================================================
     *
     * entrance starts at 1:
     *
     * translation =
     * 1 * real screen height
     *
     * Therefore the very first visible frame is already below
     * screen. No flash before slide-in.
     */

    val entrance =
        remember {
            Animatable(
                1f
            )
        }

    val playerY =
        remember {
            Animatable(
                0f
            )
        }

    var dismissing by
        remember {
            mutableStateOf(
                false
            )
        }

    LaunchedEffect(
        Unit
    ) {
        entrance.snapTo(
            1f
        )

        entrance.animateTo(
            targetValue =
                0f,
            animationSpec =
                tween(
                    durationMillis =
                        410
                )
        )

        onOpened()
    }

    suspend fun closePlayer() {
        if (
            dismissing
        ) {
            return
        }

        dismissing =
            true

        playerY.animateTo(
            targetValue =
                screenHeight,
            animationSpec =
                tween(
                    durationMillis =
                        330
                )
        )

        dismiss()
    }

    /*
     * =========================================================
     * POP
     * =========================================================
     */

    LaunchedEffect(
        pop?.key
    ) {
        if (
            pop != null
        ) {
            delay(
                1_900L
            )

            pop =
                null
        }
    }

    /*
     * =========================================================
     * BACK
     * =========================================================
     */

    BackHandler {
        when {
            fullLyrics -> {
                fullLyrics =
                    false

                artworkLyrics =
                    true
            }

            overlay != null -> {
                overlay =
                    null
            }

            artworkLyrics -> {
                artworkLyrics =
                    false
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
     * RENDER
     * =========================================================
     */

    NowPlayingContent(
        state =
            state,
        theme =
            theme,
        source =
            source,
        sourceIsCategory =
            sourceIsCategory,
        queue =
            queue,
        categories =
            categories,
        liked =
            liked,

        currentIndex =
            currentIndex,
        currentSong =
            currentSong,
        previousSong =
            previousSong,
        nextSong =
            nextSong,
        fallbackArtwork =
            fallbackArtwork,

        artistTrackCount =
            artistTrackCount,
        inCategory =
            inCategory,

        lyrics =
            lyrics,

        colors =
            colors,
        accent =
            accent,

        displayColor =
            displayColor,
        deep =
            deep,
        themeColors =
            themeColors,

        carousel =
            carousel,

        overlay =
            overlay,
        artworkLyrics =
            artworkLyrics,
        fullLyrics =
            fullLyrics,

        pop =
            pop,
        sleepTotalMs =
            sleepTotalMs,

        entrance =
            entrance,
        playerY =
            playerY,
        screenHeight =
            screenHeight,

        /*
         * Kept because NowPlayingContent already has this callback
         * in its stable rendering signature. Screen height is now
         * configuration-owned and does not need to mutate here.
         */
        updateScreenHeight = {
            Unit
        },

        closePlayer = {
            closePlayer()
        },

        dismissAfterDrag = {
            if (
                !dismissing
            ) {
                dismissing =
                    true

                dismiss()
            }
        },

        setOverlay = {
            overlay =
                it
        },

        setArtworkLyrics = {
            artworkLyrics =
                it
        },

        setFullLyrics = {
            fullLyrics =
                it
        },

        setPop = {
            pop =
                it
        },

        setSleepTotalMs = {
            sleepTotalMs =
                it
        },

        pickLyrics = {
            lyricPicker.launch(
                arrayOf(
                    "*/*"
                )
            )
        },

        shareCurrentSong = {
            currentSong?.let {
                shareSong(
                    context,
                    it
                )
            }
        },

        togglePlay =
            togglePlay,
        previous =
            previous,
        previousItem =
            previousItem,
        next =
            next,
        playQueueIndex =
            playQueueIndex,
        seekTo =
            seekTo,
        toggleLike =
            toggleLike,
        toggleShuffle =
            toggleShuffle,
        cycleRepeat =
            cycleRepeat,
        setSleepTimer =
            setSleepTimer,
        cancelSleepTimer =
            cancelSleepTimer,
        setSongInCategory =
            setSongInCategory,
        createCategory =
            createCategory
    )
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
