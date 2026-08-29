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

    var rawX by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Visual queue window.
     *
     * It deliberately does not immediately follow recomposition
     * while a carousel transaction is active.
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

    var knownId by remember {
        mutableStateOf(currentId)
    }

    var knownIndex by remember {
        mutableStateOf(currentIndex)
    }

    /*
     * =========================================================
     * MEDIA3 CONFIRMATION / AUTO CHANGE
     * =========================================================
     */

    LaunchedEffect(
        currentId
    ) {
        val oldId =
            knownId

        val oldIndex =
            knownIndex

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
         * -----------------------------------------------------
         * MANUAL SWIPE CONFIRMED
         * -----------------------------------------------------
         *
         * User has already moved the old/current and neighbor
         * together by one complete width. No second animation.
         */
        if (
            carousel.manualDirection != 0 &&
            carousel.manualSongId == oldId
        ) {
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next

            /*
             * At ±width the incoming neighbor is already exactly
             * in the center. Swap visual window and reset x in the
             * same snapshot transaction; no second visible slide.
             */
            carousel.x.snapTo(0f)

            carousel.manualDirection =
                0

            carousel.manualSongId =
                null

            knownId =
                currentId

            knownIndex =
                currentIndex

            return@LaunchedEffect
        }

        /*
         * -----------------------------------------------------
         * AUTO / EXTERNAL MEDIA3 CHANGE
         * -----------------------------------------------------
         *
         * ExoPlayer may already report the new song before this
         * visual animation starts. We retain old snapshots and
         * use the real old neighbour as the incoming cover.
         */
        if (
            !carousel.transactionActive
        ) {
            carousel.autoAnimating =
                true

            /*
             * Natural next / higher queue index:
             * move carousel left.
             *
             * Previous / lower queue index:
             * move carousel right.
             */
            val target =
                if (
                    currentIndex <
                    oldIndex
                ) {
                    width
                } else {
                    -width
                }

            /*
             * ONE continuous adjacent-cover movement.
             *
             * snapshotCurrent remains old.
             * snapshotNext/Previous is already beside it.
             */
            carousel.x.snapTo(0f)

            carousel.x.animateTo(
                targetValue = target,
                animationSpec =
                    tween(330)
            )

            /*
             * Incoming neighbour is now exactly centered.
             * Adopt Media3-confirmed window and reset x atomically.
             */
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next

            carousel.x.snapTo(0f)

            carousel.autoAnimating =
                false
        } else {
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next
        }

        knownId =
            currentId

        knownIndex =
            currentIndex
    }

    /*
     * Queue neighbor metadata may update independently.
     */
    LaunchedEffect(
        current,
        previous,
        next
    ) {
        if (
            !carousel.transactionActive &&
            currentId == knownId
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
        Modifier
            .fillMaxWidth()
            .height(382.dp)
    ) {
        val width =
            constraints.maxWidth
                .toFloat()
                .coerceAtLeast(1f)

        LaunchedEffect(width) {
            carousel.width =
                width
        }

        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(
                    currentId,
                    canPrevious,
                    canNext,
                    showLyrics
                ) {
                    if (showLyrics) {
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

                            rawX = 0f
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

                            rawX += amount.x

                            if (
                                abs(rawX) >
                                7f
                            ) {
                                change.consume()

                                scope.launch {
                                    var target =
                                        carousel.x.value +
                                            amount.x

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
                                            -width,
                                            width
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
                                    carousel.x.value <
                                        -width * .15f &&
                                        canNext -> {

                                        carousel.manualSongId =
                                            currentId

                                        carousel.manualDirection =
                                            1

                                        carousel.x.animateTo(
                                            targetValue =
                                                -width,
                                            animationSpec =
                                                tween(245)
                                        )

                                        nextSong()
                                    }

                                    carousel.x.value >
                                        width * .15f &&
                                        canPrevious -> {

                                        carousel.manualSongId =
                                            currentId

                                        carousel.manualDirection =
                                            -1

                                        carousel.x.animateTo(
                                            targetValue =
                                                width,
                                            animationSpec =
                                                tween(245)
                                        )

                                        previousSong()
                                    }

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

                                rawX = 0f
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
                                            tween(180)
                                    )
                                }

                                rawX = 0f
                            }
                        }
                    )
                }
        ) {
            /*
             * =================================================
             * ARTWORK <-> LYRICS CARD
             * =================================================
             *
             * Fade only. Scaling the clipped cover itself was the
             * source of the visible artwork cropping during this
             * transition.
             */
            AnimatedContent(
                targetState =
                    showLyrics,
                modifier =
                    Modifier.fillMaxSize(),
                transitionSpec = {
                    fadeIn(
                        animationSpec =
                            tween(320)
                    )
                        .togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(280)
                            )
                        )
                },
                label =
                    "artworkLyrics"
            ) { lyricsVisible ->

                if (lyricsVisible) {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        ArtworkLyrics(
                            lyrics = lyrics,
                            position =
                                position,
                            colors = colors,
                            accent = accent,
                            theme = theme,
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
                        width = width,
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
        Modifier.fillMaxSize()
    ) {
        previous?.let {
            Cover(
                uri = it,
                modifier =
                    coverSizeModifier()
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer {
                            translationX =
                                x - width
                        }
            )
        }

        Cover(
            uri = current,
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
                        indication = null,
                        onClick =
                            toggleLyrics
                    )
        )

        next?.let {
            Cover(
                uri = it,
                modifier =
                    coverSizeModifier()
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer {
                            translationX =
                                x + width
                        }
            )
        }
    }
}

private fun coverSizeModifier(): Modifier =
    Modifier
        .padding(
            horizontal = 17.dp
        )
        .fillMaxWidth()
        .aspectRatio(1f)

@Composable
private fun Cover(
    uri: Uri?,
    modifier: Modifier
) {
    Box(
        modifier
            .clip(
                RoundedCornerShape(
                    24.dp
                )
            )
            .background(
                Color.Black.copy(
                    alpha = .06f
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
                    text = "XMO",
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
