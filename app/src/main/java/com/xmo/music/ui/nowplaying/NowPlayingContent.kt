package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.data.SongLyrics
import com.xmo.music.data.UserCategory
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.HomeColors

@Composable
internal fun NowPlayingContent(
    state: PlaybackState,
    theme: XmoTheme,
    source: String,
    sourceIsCategory: Boolean,
    queue: List<Song>,
    categories: List<UserCategory>,
    liked: Boolean,

    currentIndex: Int,
    currentSong: Song?,
    previousSong: Song?,
    nextSong: Song?,
    fallbackArtwork: Uri?,

    artistTrackCount: Int,
    inCategory: Boolean,
    lyrics: SongLyrics?,

    colors: HomeColors,
    accent: Color,

    displayColor: Color,
    deep: Color,
    themeColors: PlayerThemeColors,

    carousel: PlayerCarouselState,

    overlay: PlayerOverlay?,
    artworkLyrics: Boolean,
    fullLyrics: Boolean,

    pop: PopMessage?,
    sleepTotalMs: Long?,

    entrance: Animatable<Float, *>,
    playerY: Animatable<Float, *>,
    screenHeight: Float,

    updateScreenHeight: (Float) -> Unit,

    closePlayer: suspend () -> Unit,
    dismissAfterDrag: () -> Unit,

    setOverlay: (PlayerOverlay?) -> Unit,
    setArtworkLyrics: (Boolean) -> Unit,
    setFullLyrics: (Boolean) -> Unit,
    setPop: (PopMessage?) -> Unit,
    setSleepTotalMs: (Long?) -> Unit,

    pickLyrics: () -> Unit,
    shareCurrentSong: () -> Unit,

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

    setSongInCategory: (
        categoryId: String,
        added: Boolean
    ) -> Unit,

    createCategory: (String) -> UserCategory?
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            awaitPointerEvent()
                        }
                    }
                }
                .onSizeChanged {
                    updateScreenHeight(
                        it.height
                            .toFloat()
                            .coerceAtLeast(1f)
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
            deep =
                deep,
            theme =
                theme
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
        ) {
            PlayerHeader(
                source =
                    source,
                sourceIsCategory =
                    sourceIsCategory,
                foreground =
                    themeColors.overlayText,
                playerY =
                    playerY,
                screenHeight =
                    screenHeight,
                close =
                    closePlayer,
                dismissAfterDrag =
                    dismissAfterDrag,
                share =
                    shareCurrentSong,
                options = {
                    setOverlay(
                        PlayerOverlay.Options
                    )
                }
            )

            Spacer(
                Modifier.height(
                    93.dp
                )
            )

            PlayerArtwork(
                currentId =
                    state.currentSongId,
                currentIndex =
                    currentIndex,
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
                carousel =
                    carousel,
                showLyrics =
                    artworkLyrics,
                lyrics =
                    lyrics,
                position =
                    state.position,
                colors =
                    colors,
                accent =
                    accent,
                theme =
                    theme,
                previousSong =
                    previousItem,
                nextSong =
                    next,
                toggleLyrics = {
                    setArtworkLyrics(
                        !artworkLyrics
                    )
                },
                pickLyrics =
                    pickLyrics,
                fullscreenLyrics = {
                    setArtworkLyrics(
                        true
                    )

                    setFullLyrics(
                        true
                    )
                }
            )

            /*
             * Panel brought upward from 95dp gap.
             *
             * Artwork still breathes, but the lower section has
             * enough usable height for XMO/footer.
             */
            Spacer(
                Modifier.height(
                    76.dp
                )
            )

            Column(
                modifier =
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
                            top = 8.dp,
                            end = 12.dp,
                            bottom = 1.dp
                        )
            ) {
                PlayerInfo(
                    title =
                        state.title,
                    artist =
                        state.artist,
                    liked =
                        liked,
                    inCategory =
                        inCategory,
                    sleepRemainingMs =
                        state.sleepTimerRemainingMs,
                    sleepTotalMs =
                        sleepTotalMs,
                    colors =
                        colors,
                    accent =
                        accent,
                    softButton =
                        themeColors.softButton,
                    theme =
                        theme,
                    toggleLike = {
                        toggleLike()

                        setPop(
                            PopMessage(
                                if (liked) {
                                    "Removed from Liked Songs"
                                } else {
                                    "Added to Liked Songs"
                                }
                            )
                        )
                    },
                    openCategories = {
                        setOverlay(
                            PlayerOverlay.Options
                        )
                    },
                    openSleep = {
                        setOverlay(
                            PlayerOverlay.Sleep
                        )
                    },
                    openQueue = {
                        setOverlay(
                            PlayerOverlay.Queue
                        )
                    },
                    openDetails = {
                        setOverlay(
                            PlayerOverlay.Details
                        )
                    },
                    openArtist = {
                        setOverlay(
                            PlayerOverlay.Artist
                        )
                    }
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
                    colors =
                        colors,
                    accent =
                        accent,
                    border =
                        themeColors.border,
                    controlForeground =
                        themeColors.controls,
                    playBackground =
                        themeColors.playBackground,
                    seekTo =
                        seekTo,
                    togglePlay =
                        togglePlay,
                    previous =
                        previous,
                    next =
                        next,
                    toggleShuffle =
                        toggleShuffle,
                    cycleRepeat =
                        cycleRepeat
                )
            }
        }

        when (overlay) {
            PlayerOverlay.Queue -> {
                QueueSheet(
                    queue =
                        queue,
                    currentSongId =
                        state.currentSongId,
                    colors =
                        colors,
                    playIndex =
                        playQueueIndex,
                    dismiss = {
                        setOverlay(null)
                    }
                )
            }

            PlayerOverlay.Options -> {
                SongOptionsBox(
                    song =
                        currentSong,
                    categories =
                        categories,
                    colors =
                        colors,
                    liked =
                        liked,
                    close = {
                        setOverlay(null)
                    },
                    toggleLike = {
                        toggleLike()
                    },
                    share = {
                        shareCurrentSong()
                        setOverlay(null)
                    },
                    setCategory = {
                            category,
                            add ->

                        setSongInCategory(
                            category.id,
                            add
                        )
                    },
                    createCategory = {
                            name ->

                        createCategory(name) != null
                    }
                )
            }

            PlayerOverlay.Sleep -> {
                SleepTimerBox(
                    colors =
                        colors,
                    active =
                        state.sleepTimerRemainingMs >
                            0L,
                    dismiss = {
                        setOverlay(null)
                    },
                    setTimer = {
                            duration,
                            label ->

                        setSleepTotalMs(
                            duration
                        )

                        setSleepTimer(
                            duration
                        )

                        setOverlay(null)

                        setPop(
                            PopMessage(
                                "Sleep timer set for $label"
                            )
                        )
                    },
                    cancel = {
                        setSleepTotalMs(null)
                        cancelSleepTimer()
                        setOverlay(null)

                        setPop(
                            PopMessage(
                                "Sleep timer cancelled"
                            )
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
                    colors =
                        colors,
                    close = {
                        setOverlay(null)
                    }
                )
            }

            PlayerOverlay.Artist -> {
                ArtistInfoBox(
                    artist =
                        state.artist,
                    trackCount =
                        artistTrackCount,
                    colors =
                        colors,
                    close = {
                        setOverlay(null)
                    }
                )
            }

            null ->
                Unit
        }

        /*
         * Working fullscreen animation intentionally preserved.
         */
        AnimatedVisibility(
            visible =
                fullLyrics,
            enter =
                fadeIn(
                    tween(310)
                ) +
                    scaleIn(
                        initialScale = .94f,
                        animationSpec =
                            tween(390)
                    ),
            exit =
                fadeOut(
                    tween(250)
                ) +
                    scaleOut(
                        targetScale = .95f,
                        animationSpec =
                            tween(330)
                    )
        ) {
            FullLyrics(
                lyrics =
                    lyrics,
                position =
                    state.position,
                duration =
                    state.duration,
                title =
                    state.title,
                artist =
                    state.artist,
                artwork =
                    currentSong?.artwork
                        ?: fallbackArtwork,
                dominant =
                    displayColor,
                deep =
                    deep,
                theme =
                    theme,
                accent =
                    accent,
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
                next =
                    next,
                seekTo =
                    seekTo,
                close = {
                    setFullLyrics(false)
                    setArtworkLyrics(true)
                }
            )
        }

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
                        .padding(
                            top = 72.dp
                        )
            )
        }
    }
}
