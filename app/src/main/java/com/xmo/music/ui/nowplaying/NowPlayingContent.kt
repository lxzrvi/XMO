package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
    val dismissFraction =
        (
            playerY.value /
                screenHeight.coerceAtLeast(1f)
            )
            .coerceIn(
                0f,
                1f
            )

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
                                30f *
                                    dismissFraction
                                ).dp,
                        topEnd =
                            (
                                30f *
                                    dismissFraction
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
            modifier =
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
                close = closePlayer,
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
                carousel = carousel,
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
                    setArtworkLyrics(
                        !artworkLyrics
                    )
                },
                pickLyrics =
                    pickLyrics,
                fullscreenLyrics = {
                    setArtworkLyrics(true)
                    setFullLyrics(true)
                }
            )

            /*
             * Cover is untouched.
             *
             * Only the panel surface starts 8dp higher.
             */
            Spacer(
                Modifier.height(
                    52.dp
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
                        /*
                         * Extra 8dp internal top padding
                         * compensates for the earlier panel edge.
                         * Title/progress/transport therefore do
                         * not get pulled upward with the panel.
                         */
                        .padding(
                            start = 12.dp,
                            top = 16.dp,
                            end = 12.dp,
                            bottom = 1.dp
                        )
            ) {
                PlayerInfo(
                    title = state.title,
                    artist = state.artist,
                    liked = liked,
                    inCategory =
                        inCategory,
                    sleepRemainingMs =
                        state.sleepTimerRemainingMs,
                    sleepTotalMs =
                        sleepTotalMs,
                    colors = colors,
                    accent = accent,
                    softButton =
                        themeColors.softButton,
                    theme = theme,
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

                Box(
                    modifier =
                        Modifier.offset(
                            y = 16.dp
                        )
                ) {
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
        }

        AnimatedContent(
            targetState = overlay,
            modifier =
                Modifier.fillMaxSize(),
            transitionSpec = {
                if (
                    targetState != null &&
                    initialState == null
                ) {
                    (
                        fadeIn(
                            animationSpec =
                                tween(
                                    durationMillis = 220
                                )
                        ) +
                            scaleIn(
                                initialScale = .965f,
                                animationSpec =
                                    spring(
                                        dampingRatio = .86f,
                                        stiffness = 430f
                                    )
                            )
                        )
                        .togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = 130
                                    )
                            )
                        )
                } else if (
                    targetState == null
                ) {
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis = 120
                            )
                    )
                        .togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis = 190
                                    )
                            ) +
                                scaleOut(
                                    targetScale = .975f,
                                    animationSpec =
                                        tween(
                                            durationMillis = 190
                                        )
                                )
                        )
                } else {
                    fadeIn(
                        tween(180)
                    )
                        .togetherWith(
                            fadeOut(
                                tween(150)
                            )
                        )
                }
            },
            label =
                "playerOverlay"
        ) { visibleOverlay ->

            when (visibleOverlay) {
                PlayerOverlay.Queue -> {
                    QueueSheet(
                        queue = queue,
                        currentSongId =
                            state.currentSongId,
                        colors = colors,
                        playIndex =
                            playQueueIndex,
                        dismiss = {
                            setOverlay(null)
                        }
                    )
                }

                PlayerOverlay.Options -> {
                    SongOptionsBox(
                        song = currentSong,
                        categories =
                            categories,
                        colors = colors,
                        liked = liked,
                        close = {
                            setOverlay(null)
                        },
                        toggleLike =
                            toggleLike,
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

                            createCategory(name) !=
                                null
                        }
                    )
                }

                PlayerOverlay.Sleep -> {
                    SleepTimerBox(
                        colors = colors,
                        active =
                            state
                                .sleepTimerRemainingMs >
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
                        song = currentSong,
                        album = state.album,
                        colors = colors,
                        close = {
                            setOverlay(null)
                        }
                    )
                }

                PlayerOverlay.Artist -> {
                    ArtistInfoBox(
                        artist = state.artist,
                        trackCount =
                            artistTrackCount,
                        colors = colors,
                        close = {
                            setOverlay(null)
                        }
                    )
                }

                null -> Unit
            }
        }

        AnimatedVisibility(
            visible = fullLyrics,
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
                lyrics = lyrics,
                position = state.position,
                duration = state.duration,
                title = state.title,
                artist = state.artist,
                artwork =
                    currentSong?.artwork
                        ?: fallbackArtwork,
                dominant =
                    displayColor,
                deep = deep,
                theme = theme,
                accent = accent,
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
                },
                pickLyrics =
                    pickLyrics
            )
        }

        AnimatedVisibility(
            visible = pop != null,
            modifier =
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .statusBarsPadding()
                    .padding(
                        top = 72.dp
                    ),
            enter =
                fadeIn(
                    tween(210)
                ) +
                    scaleIn(
                        initialScale = .94f,
                        animationSpec =
                            tween(
                                durationMillis = 250,
                                easing =
                                    FastOutSlowInEasing
                            )
                    ) +
                    slideInVertically(
                        initialOffsetY = {
                            -it / 5
                        },
                        animationSpec =
                            tween(250)
                    ),
            exit =
                fadeOut(
                    tween(190)
                ) +
                    scaleOut(
                        targetScale = .96f,
                        animationSpec =
                            tween(190)
                    ) +
                    slideOutVertically(
                        targetOffsetY = {
                            -it / 7
                        },
                        animationSpec =
                            tween(190)
                    )
        ) {
            pop?.let {
                XmoPop(
                    message = it.text,
                    theme = theme
                )
            }
        }
    }
}
