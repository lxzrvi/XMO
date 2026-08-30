package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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

    var rawDragX by
        remember {
            mutableFloatStateOf(0f)
        }

    LaunchedEffect(
        currentId,
        currentIndex,
        current,
        previous,
        next
    ) {
        carousel.initializeIfEmpty(
            id = currentId,
            index = currentIndex,
            current = current,
            previous = previous,
            next = next
        )

        carousel.updateIdleWindow(
            id = currentId,
            index = currentIndex,
            current = current,
            previous = previous,
            next = next
        )
    }

    /*
     * Confirmed Media3 transition.
     *
     * Frozen visual neighbors remain on screen until the
     * confirmed destination has completed its travel.
     */
    LaunchedEffect(currentId) {
        val oldId =
            carousel.visualSongId

        if (
            currentId == null ||
            oldId == null ||
            currentId == oldId
        ) {
            return@LaunchedEffect
        }

        val pageDistance =
            carousel.width
                .coerceAtLeast(1f)

        /*
         * Manual drag already moved the real adjacent artwork to
         * the destination. Media3 confirmation only commits the
         * new frozen window.
         */
        if (
            carousel.manualDirection != 0 &&
            carousel.manualSongId == oldId
        ) {
            carousel.finishManual(
                id = currentId,
                index = currentIndex,
                current = current,
                previous = previous,
                next = next
            )

            carousel.x.snapTo(0f)

            return@LaunchedEffect
        }

        if (!carousel.transactionActive) {
            /*
             * Resolve adjacent direction from the actual frozen
             * artwork first. This also handles last -> first and
             * first -> last queue wraps.
             */
            val direction =
                when {
                    current != null &&
                        carousel.visualNext != null &&
                        current ==
                            carousel.visualNext ->
                        1

                    current != null &&
                        carousel.visualPrevious != null &&
                        current ==
                            carousel.visualPrevious ->
                        -1

                    currentIndex ==
                        carousel.visualIndex + 1 ->
                        1

                    currentIndex ==
                        carousel.visualIndex - 1 ->
                        -1

                    else ->
                        0
                }

            val adjacentAvailable =
                when (direction) {
                    1 ->
                        carousel.visualNext != null

                    -1 ->
                        carousel.visualPrevious != null

                    else ->
                        false
                }

            if (adjacentAvailable) {
                carousel.beginAutomatic(
                    direction
                )

                carousel.x.snapTo(0f)

                carousel.x.animateTo(
                    targetValue =
                        if (direction > 0) {
                            -pageDistance
                        } else {
                            pageDistance
                        },
                    animationSpec =
                        tween(
                            durationMillis = 340
                        )
                )

                carousel.finishAutomatic(
                    id = currentId,
                    index = currentIndex,
                    current = current,
                    previous = previous,
                    next = next
                )

                carousel.x.snapTo(0f)
            } else {
                /*
                 * A genuine non-adjacent external jump must never
                 * fake a neighboring cover transition.
                 */
                carousel.adoptExternalWindow(
                    id = currentId,
                    index = currentIndex,
                    current = current,
                    previous = previous,
                    next = next
                )

                carousel.x.snapTo(0f)
            }
        }
    }

    /*
     * This remains the exact approved artwork square.
     *
     * Nothing here changes the centered cover's size or vertical
     * position.
     */
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 17.dp
                )
                .aspectRatio(1f)
    ) {
        val density =
            LocalDensity.current

        val gapPx =
            with(density) {
                18.dp.toPx()
            }

        val coverWidth =
            constraints.maxWidth
                .toFloat()
                .coerceAtLeast(1f)

        /*
         * Complete center-to-center page travel.
         *
         * Neighbor covers live outside the centered square at
         * rest. Since this host does not clip horizontally, they
         * enter from the screen-side area and leave toward the
         * opposite screen edge instead of appearing at the cover
         * boundary.
         */
        val pageDistance =
            coverWidth +
                gapPx

        LaunchedEffect(pageDistance) {
            carousel.width =
                pageDistance
        }

        /*
         * Existing artwork <-> lyrics animation is retained.
         */
        val lyricsTransition =
            remember {
                Animatable(
                    if (showLyrics) {
                        1f
                    } else {
                        0f
                    }
                )
            }

        LaunchedEffect(showLyrics) {
            lyricsTransition.animateTo(
                targetValue =
                    if (showLyrics) {
                        1f
                    } else {
                        0f
                    },
                animationSpec =
                    tween(340)
            )
        }

        val lyricsFraction =
            lyricsTransition.value
                .coerceIn(
                    0f,
                    1f
                )

        /*
         * Intentionally NOT clipped here. Horizontal carousel
         * overflow is allowed to render into the side areas.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .pointerInput(
                        currentId,
                        canPrevious,
                        canNext,
                        showLyrics,
                        pageDistance
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

                                rawDragX = 0f
                            },

                            onDrag = {
                                    change,
                                    drag ->

                                if (
                                    carousel
                                        .transactionActive
                                ) {
                                    return@detectDragGestures
                                }

                                rawDragX +=
                                    drag.x

                                if (
                                    abs(rawDragX) >
                                    6f
                                ) {
                                    change.consume()

                                    val candidate =
                                        carousel.x.value +
                                            drag.x

                                    val target =
                                        when {
                                            candidate < 0f &&
                                                !canNext ->

                                                carousel.x.value +
                                                    drag.x *
                                                    .14f

                                            candidate > 0f &&
                                                !canPrevious ->

                                                carousel.x.value +
                                                    drag.x *
                                                    .14f

                                            else ->
                                                candidate
                                        }

                                    scope.launch {
                                        carousel.x.snapTo(
                                            target.coerceIn(
                                                -pageDistance,
                                                pageDistance
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
                                        carousel.x.value <=
                                            -pageDistance *
                                            .16f &&
                                            canNext -> {

                                            carousel.beginManual(
                                                1
                                            )

                                            carousel.x.animateTo(
                                                targetValue =
                                                    -pageDistance,
                                                animationSpec =
                                                    tween(
                                                        durationMillis =
                                                            250
                                                    )
                                            )

                                            nextSong()
                                        }

                                        carousel.x.value >=
                                            pageDistance *
                                            .16f &&
                                            canPrevious -> {

                                            carousel.beginManual(
                                                -1
                                            )

                                            carousel.x.animateTo(
                                                targetValue =
                                                    pageDistance,
                                                animationSpec =
                                                    tween(
                                                        durationMillis =
                                                            250
                                                    )
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
                                                            .86f,
                                                        stiffness =
                                                            430f
                                                    )
                                            )
                                        }
                                    }

                                    rawDragX = 0f
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    if (
                                        !carousel
                                            .transactionActive
                                    ) {
                                        carousel.x.animateTo(
                                            targetValue = 0f,
                                            animationSpec =
                                                tween(
                                                    durationMillis =
                                                        180
                                                )
                                        )
                                    }

                                    rawDragX = 0f
                                }
                            }
                        )
                    }
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .alpha(
                            1f -
                                lyricsFraction
                        )
                        .graphicsLayer {
                            val scale =
                                1f -
                                    .018f *
                                    lyricsFraction

                            scaleX = scale
                            scaleY = scale

                            /*
                             * Explicitly keep layer clipping off.
                             */
                            clip = false
                        }
            ) {
                ArtworkCarousel(
                    current =
                        carousel.visualCurrent,
                    previous =
                        carousel.visualPrevious,
                    next =
                        carousel.visualNext,
                    x =
                        carousel.x.value,
                    pageDistance =
                        pageDistance,
                    enabled =
                        !showLyrics,
                    toggleLyrics =
                        toggleLyrics
                )
            }

            if (
                showLyrics ||
                lyricsFraction > .001f
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .alpha(
                                lyricsFraction
                            )
                            .graphicsLayer {
                                val scale =
                                    .982f +
                                        .018f *
                                        lyricsFraction

                                scaleX = scale
                                scaleY = scale
                            },
                    contentAlignment =
                        Alignment.Center
                ) {
                    ArtworkLyrics(
                        lyrics = lyrics,
                        position = position,
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
                            Modifier.fillMaxSize()
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
    pageDistance: Float,
    enabled: Boolean,
    toggleLyrics: () -> Unit
) {
    /*
     * No clipping on this container. Each cover remains exactly
     * artwork-size but may render outside the center square while
     * moving.
     */
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    clip = false
                }
    ) {
        previous?.let {
            Cover(
                uri = it,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                x -
                                    pageDistance
                            clip = false
                        }
            )
        }

        Cover(
            uri = current,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = x
                        clip = false
                    }
                    .clickable(
                        interactionSource =
                            remember {
                                MutableInteractionSource()
                            },
                        indication = null,
                        enabled = enabled,
                        onClick =
                            toggleLyrics
                    )
        )

        next?.let {
            Cover(
                uri = it,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                x +
                                    pageDistance
                            clip = false
                        }
            )
        }
    }
}

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
                modifier =
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
                    fontSize =
                        31.sp
                )
            }
        }
    }
}
