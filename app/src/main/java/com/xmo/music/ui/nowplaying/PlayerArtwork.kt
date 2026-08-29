package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
     * These are visual snapshots, not playback state.
     *
     * They intentionally remain on the old song until a carousel
     * transaction reaches the point where the new artwork should
     * become visible.
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

    var previousKnownId by remember {
        mutableStateOf(currentId)
    }

    var previousKnownIndex by remember {
        mutableStateOf(currentIndex)
    }

    /*
     * =========================================================
     * REAL MEDIA3 SONG CHANGE
     * =========================================================
     *
     * Covers:
     * - natural track completion
     * - notification controls
     * - Bluetooth/headset commands
     * - external MediaController changes
     *
     * Manual artwork swipes are handled separately below and do
     * not receive a second animation after Media3 confirms them.
     */

    LaunchedEffect(
        currentId
    ) {
        val oldId =
            previousKnownId

        val oldIndex =
            previousKnownIndex

        if (
            currentId == null ||
            oldId == null ||
            currentId == oldId
        ) {
            previousKnownId =
                currentId

            previousKnownIndex =
                currentIndex

            return@LaunchedEffect
        }

        /*
         * Manual transaction confirmation.
         *
         * The outgoing cover is already at ±width because the
         * user's swipe completed before nextSong/previousSong was
         * sent to Media3.
         */
        if (
            carousel.manualDirection != 0 &&
            carousel.manualSongId == oldId
        ) {
            carousel.x.snapTo(0f)

            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next

            carousel.manualDirection =
                0

            carousel.manualSongId =
                null

            previousKnownId =
                currentId

            previousKnownIndex =
                currentIndex

            return@LaunchedEffect
        }

        /*
         * A real song change occurred outside an active manual
         * artwork transaction.
         */
        if (
            !carousel.transactionActive
        ) {
            carousel.autoAnimating =
                true

            val width =
                carousel.width
                    .coerceAtLeast(1f)

            /*
             * Queue moved forward:
             * old cover exits left.
             *
             * Queue moved backward:
             * old cover exits right.
             */
            val exitDirection =
                if (
                    currentIndex <
                    oldIndex
                ) {
                    1f
                } else {
                    -1f
                }

            carousel.x.snapTo(0f)

            carousel.x.animateTo(
                targetValue =
                    exitDirection *
                        width,
                animationSpec =
                    tween(250)
            )

            /*
             * Adopt the new Media3-confirmed visual window only
             * while the old cover is outside the viewport.
             */
            snapshotCurrent =
                current

            snapshotPrevious =
                previous

            snapshotNext =
                next

            /*
             * New cover enters from the opposite edge.
             */
            carousel.x.snapTo(
                -exitDirection *
                    width
            )

            carousel.x.animateTo(
                targetValue = 0f,
                animationSpec =
                    tween(285)
            )

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

        previousKnownId =
            currentId

        previousKnownIndex =
            currentIndex
    }

    /*
     * Neighbour artwork can change while current song remains the
     * same, e.g. queue mutation. Do not replace visual snapshots
     * during a carousel transaction.
     */
    LaunchedEffect(
        current,
        previous,
        next,
        currentId
    ) {
        if (
            !carousel.transactionActive
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
                    /*
                     * Lyrics own touch/vertical scrolling while
                     * that side of the card is visible.
                     */
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

                                    /*
                                     * Edge resistance.
                                     */
                                    if (
                                        target < 0f &&
                                        !canNext
                                    ) {
                                        target =
                                            carousel.x.value +
                                                amount.x *
                                                    .17f
                                    }

                                    if (
                                        target > 0f &&
                                        !canPrevious
                                    ) {
                                        target =
                                            carousel.x.value +
                                                amount.x *
                                                    .17f
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
                                                tween(220)
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
                                                tween(220)
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
                                                        430f
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
             * Artwork <-> cover-size lyrics.
             *
             * Small scaling range avoids the old visible clipping
             * where the artwork appeared to be cut during the
             * transition.
             */
            AnimatedContent(
                targetState =
                    showLyrics,
                modifier =
                    Modifier.fillMaxSize(),
                transitionSpec = {
                    (
                        fadeIn(
                            animationSpec =
                                tween(300)
                        ) +
                            scaleIn(
                                initialScale =
                                    .985f,
                                animationSpec =
                                    tween(330)
                            )
                        )
                        .togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(250)
                            ) +
                                scaleOut(
                                    targetScale =
                                        1.015f,
                                    animationSpec =
                                        tween(290)
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
                                Modifier
                                    .padding(
                                        horizontal =
                                            17.dp
                                    )
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
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
        previous?.let { uri ->
            Cover(
                uri = uri,
                modifier =
                    coverModifier()
                        .align(
                            Alignment.Center
                        )
                        .graphicsLayer {
                            translationX =
                                x - width
                        }
            )
        }

        val interaction =
            remember {
                MutableInteractionSource()
            }

        Cover(
            uri = current,
            modifier =
                coverModifier()
                    .align(
                        Alignment.Center
                    )
                    .graphicsLayer {
                        translationX = x
                    }
                    .clickable(
                        interactionSource =
                            interaction,
                        indication = null,
                        onClick =
                            toggleLyrics
                    )
        )

        next?.let { uri ->
            Cover(
                uri = uri,
                modifier =
                    coverModifier()
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

private fun coverModifier(): Modifier =
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
                RoundedCornerShape(24.dp)
            )
            .background(
                Color.Black.copy(
                    alpha = .07f
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
