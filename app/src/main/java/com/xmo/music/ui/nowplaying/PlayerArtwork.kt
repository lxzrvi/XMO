package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.SongLyrics
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
internal fun PlayerArtwork(
    currentId: Long?,
    currentIndex: Int,
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    canPrevious: Boolean,
    canNext: Boolean,
    carousel: PlayerCarouselState,
    showLyrics: Boolean,
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
    accent: Color,
    theme: XmoTheme,
    previousSong: () -> Unit,
    nextSong: () -> Unit,
    toggleLyrics: () -> Unit,
    pickLyrics: () -> Unit,
    fullscreenLyrics: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    var rawX by
        remember {
            mutableFloatStateOf(0f)
        }

    /*
     * =========================================================
     * FROZEN VISUAL QUEUE WINDOW
     * =========================================================
     *
     * The artwork visible during a transaction is intentionally
     * independent from the live recomposed queue window.
     */
    var snapshotCurrent by
        remember {
            mutableStateOf(current)
        }

    var snapshotPrevious by
        remember {
            mutableStateOf(previous)
        }

    var snapshotNext by
        remember {
            mutableStateOf(next)
        }

    var knownId by
        remember {
            mutableStateOf(currentId)
        }

    var knownIndex by
        remember {
            mutableStateOf(currentIndex)
        }

    /*
     * =========================================================
     * MEDIA3 CONFIRMATION / AUTOMATIC CHANGE
     * =========================================================
     */

    LaunchedEffect(
        currentId
    ) {
        val oldId =
            knownId

        val oldIndex =
            knownIndex

        /*
         * Nothing changed yet.
         */
        if (
            currentId == null ||
            oldId == null ||
            currentId == oldId
        ) {
            knownId =
                currentId

            knownIndex =
                currentIndex

            return@LaunchedEffect
        }

        val width =
            carousel.width
                .coerceAtLeast(1f)

        /*
         * =====================================================
         * MANUAL SWIPE CONFIRMATION
         * =====================================================
         *
         * The user has already moved the old visual window one
         * complete cover width.
         *
         * Media3 changing away from manualSongId is the real
         * confirmation.
         */
        if (
            carousel.manualDirection != 0 &&
            carousel.manualSongId ==
            oldId
        ) {
            carousel.confirmManualTarget(
                currentId
            )

            /*
             * At +/- width the incoming cover is already exactly
             * centered. Adopt the confirmed real queue window and
             * reset displacement without doing another animation.
             */
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next

            carousel.x.snapTo(
                0f
            )

            carousel.finishManual()

            knownId =
                currentId

            knownIndex =
                currentIndex

            return@LaunchedEffect
        }

        /*
         * =====================================================
         * NATURAL / BUTTON / EXTERNAL MEDIA3 CHANGE
         * =====================================================
         *
         * Keep old visual snapshots while the adjacent old cover
         * slides into the center.
         */
        if (
            !carousel.transactionActive
        ) {
            /*
             * Higher queue index = next.
             * Lower queue index = previous.
             */
            val direction =
                if (
                    currentIndex <
                    oldIndex
                ) {
                    -1
                } else {
                    1
                }

            carousel.beginAutomatic(
                fromSongId =
                    oldId,
                toSongId =
                    currentId,
                direction =
                    direction
            )

            val target =
                if (
                    direction < 0
                ) {
                    width
                } else {
                    -width
                }

            carousel.x.snapTo(
                0f
            )

            /*
             * ONE continuous visual movement.
             */
            carousel.x.animateTo(
                targetValue =
                    target,
                animationSpec =
                    tween(
                        durationMillis =
                            330
                    )
            )

            /*
             * Incoming old neighbor is centered now.
             *
             * Adopt the Media3-confirmed window and reset x.
             */
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next

            carousel.x.snapTo(
                0f
            )

            carousel.finishAutomatic()
        }

        knownId =
            currentId

        knownIndex =
            currentIndex
    }

    /*
     * Queue neighbors can update without current song changing.
     *
     * Only accept those changes while the visual carousel is
     * fully idle.
     */
    LaunchedEffect(
        current,
        previous,
        next
    ) {
        if (
            !carousel.transactionActive &&
            currentId ==
            knownId
        ) {
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next
        }
    }

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    382.dp
                )
    ) {
        val density =
            LocalDensity.current

        /*
         * coverSizeModifier has 17dp on each horizontal side.
         *
         * Therefore carousel distance must be:
         *
         * container width - 34dp
         *
         * not the complete parent width.
         */
        val horizontalInsetPx =
            with(density) {
                34.dp.toPx()
            }

        val coverWidth =
            (
                constraints.maxWidth
                    .toFloat() -
                    horizontalInsetPx
                )
                .coerceAtLeast(
                    1f
                )

        LaunchedEffect(
            coverWidth
        ) {
            carousel.width =
                coverWidth
        }

        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(
                        currentId,
                        canPrevious,
                        canNext,
                        showLyrics
                    ) {
                        /*
                         * Lyrics surface does not horizontally
                         * navigate the artwork carousel.
                         */
                        if (
                            showLyrics
                        ) {
                            return@pointerInput
                        }

                        detectDragGestures(
                            onDragStart = {
                                if (
                                    carousel
                                        .transactionActive
                                ) {
                                    return@detectDragGestures
                                }

                                rawX =
                                    0f
                            },

                            onDrag = {
                                    change,
                                    amount ->

                                if (
                                    carousel
                                        .transactionActive
                                ) {
                                    return@detectDragGestures
                                }

                                rawX +=
                                    amount.x

                                if (
                                    abs(rawX) >
                                    7f
                                ) {
                                    change.consume()

                                    scope.launch {
                                        var target =
                                            carousel.x.value +
                                                amount.x

                                        /*
                                         * Resistance at queue
                                         * boundaries.
                                         */
                                        if (
                                            target < 0f &&
                                            !canNext
                                        ) {
                                            target =
                                                carousel.x.value +
                                                    amount.x *
                                                    .16f
                                        }

                                        if (
                                            target > 0f &&
                                            !canPrevious
                                        ) {
                                            target =
                                                carousel.x.value +
                                                    amount.x *
                                                    .16f
                                        }

                                        carousel.x.snapTo(
                                            target.coerceIn(
                                                -coverWidth,
                                                coverWidth
                                            )
                                        )
                                    }
                                }
                            },

                            onDragEnd = {
                                if (
                                    carousel
                                        .transactionActive
                                ) {
                                    return@detectDragGestures
                                }

                                scope.launch {
                                    when {
                                        /*
                                         * NEXT
                                         */
                                        carousel.x.value <
                                            -coverWidth *
                                            .15f &&
                                            canNext -> {

                                            carousel.beginManual(
                                                direction =
                                                    1,
                                                currentSongId =
                                                    currentId
                                            )

                                            carousel.x.animateTo(
                                                targetValue =
                                                    -coverWidth,
                                                animationSpec =
                                                    tween(
                                                        durationMillis =
                                                            245
                                                    )
                                            )

                                            /*
                                             * Real playback call.
                                             *
                                             * We intentionally do
                                             * not adopt the queue
                                             * window until Media3
                                             * confirms currentId.
                                             */
                                            nextSong()
                                        }

                                        /*
                                         * PREVIOUS
                                         */
                                        carousel.x.value >
                                            coverWidth *
                                            .15f &&
                                            canPrevious -> {

                                            carousel.beginManual(
                                                direction =
                                                    -1,
                                                currentSongId =
                                                    currentId
                                            )

                                            carousel.x.animateTo(
                                                targetValue =
                                                    coverWidth,
                                                animationSpec =
                                                    tween(
                                                        durationMillis =
                                                            245
                                                    )
                                            )

                                            previousSong()
                                        }

                                        /*
                                         * CANCEL / RETURN
                                         */
                                        else -> {
                                            carousel.x.animateTo(
                                                targetValue =
                                                    0f,
                                                animationSpec =
                                                    spring(
                                                        dampingRatio =
                                                            .84f,
                                                        stiffness =
                                                            420f
                                                    )
                                            )
                                        }
                                    }

                                    rawX =
                                        0f
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    if (
                                        !carousel
                                            .transactionActive
                                    ) {
                                        carousel.x.animateTo(
                                            targetValue =
                                                0f,
                                            animationSpec =
                                                tween(
                                                    durationMillis =
                                                        180
                                                )
                                        )
                                    }

                                    rawX =
                                        0f
                                }
                            }
                        )
                    }
        ) {
            /*
             * =================================================
             * ARTWORK <-> SMALL LYRICS
             * =================================================
             *
             * showLyrics is deliberately NOT keyed to currentId.
             *
             * Therefore:
             *
             * Song A lyrics open
             * -> next song
             * -> Song B stays in lyrics-card mode
             *
             * provided NowPlaying itself does not reset
             * showLyrics on song change.
             */
            AnimatedContent(
                targetState =
                    showLyrics,
                modifier =
                    Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    320
                            )
                    )
                        .togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            260
                                    )
                            )
                        )
                },
                label =
                    "artworkLyrics"
            ) { lyricsVisible ->

                if (
                    lyricsVisible
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        ArtworkLyrics(
                            lyrics =
                                lyrics,
                            position =
                                position,
                            colors =
                                colors,
                            accent =
                                accent,
                            theme =
                                theme,
                            pickLyrics =
                                pickLyrics,
                            fullscreenLyrics =
                                fullscreenLyrics,
                            showArtwork =
                                toggleLyrics,
                            modifier =
                                coverSizeModifier()
                        )
                    }
                } else {
                    ArtworkCarousel(
                        current =
                            snapshotCurrent,
                        previous =
                            snapshotPrevious,
                        next =
                            snapshotNext,
                        x =
                            carousel.x.value,
                        width =
                            coverWidth,
                        toggleLyrics =
                            toggleLyrics
                    )
                }
            }
        }
    }
}

@Composable
private fun ArtworkCarousel(
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    x: Float,
    width: Float,
    toggleLyrics: () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        previous
            ?.let {
                Cover(
                    uri =
                        it,
                    modifier =
                        coverSizeModifier()
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    x -
                                        width
                            }
                )
            }

        Cover(
            uri =
                current,
            modifier =
                coverSizeModifier()
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer {
                        translationX =
                            x
                    }
                    .clickable(
                        interactionSource =
                            remember {
                                MutableInteractionSource()
                            },
                        indication =
                            null,
                        onClick =
                            toggleLyrics
                    )
        )

        next
            ?.let {
                Cover(
                    uri =
                        it,
                    modifier =
                        coverSizeModifier()
                            .align(
                                Alignment.Center
                            )
                            .graphicsLayer {
                                translationX =
                                    x +
                                        width
                            }
                )
            }
    }
}

private fun coverSizeModifier(): Modifier =
    Modifier
        .padding(
            horizontal =
                17.dp
        )
        .fillMaxWidth()
        .aspectRatio(
            1f
        )

@Composable
private fun Cover(
    uri: Uri?,
    modifier: Modifier
) {
    Box(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .background(
                    Color.Black.copy(
                        alpha =
                            .06f
                    )
                )
    ) {
        AsyncImage(
            model =
                uri,
            contentDescription =
                null,
            modifier =
                Modifier.fillMaxSize(),
            contentScale =
                ContentScale.Crop
        )

        if (
            uri == null
        ) {
            Box(
                modifier =
                    Modifier.fillMaxSize(),
                contentAlignment =
                    Alignment.Center
            ) {
                Text(
                    text =
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
