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
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * Keep neighbour metadata fresh only while this is still the
     * visual-current song.
     */
    LaunchedEffect(
        currentId,
        currentIndex,
        current,
        previous,
        next
    ) {
        carousel.updateIdleWindow(
            id =
                currentId,
            index =
                currentIndex,
            current =
                current,
            previous =
                previous,
            next =
                next
        )
    }

    /*
     * Media3 current ID is the real confirmation signal.
     */
    LaunchedEffect(
        currentId
    ) {
        val oldId =
            carousel.visualSongId

        if (
            currentId == null ||
            oldId == null ||
            currentId == oldId
        ) {
            return@LaunchedEffect
        }

        val pageWidth =
            carousel.width
                .coerceAtLeast(
                    1f
                )

        /*
         * Manual swipe:
         *
         * cover is already centered at +/-pageWidth and remains
         * there until this real Media3 confirmation arrives.
         */
        if (
            carousel.manualDirection != 0 &&
            carousel.manualSongId == oldId
        ) {
            carousel.finishManual(
                id =
                    currentId,
                index =
                    currentIndex,
                current =
                    current,
                previous =
                    previous,
                next =
                    next
            )

            /*
             * PlayerColors already committed destination when x
             * reached the page edge. Reset cannot reveal old color.
             */
            carousel.x.snapTo(
                0f
            )

            return@LaunchedEffect
        }

        /*
         * Button / natural / external Media3 transition.
         *
         * The old frozen queue window is still intact.
         */
        if (
            !carousel.transactionActive
        ) {
            val direction =
                when {
                    currentIndex <
                        carousel.visualIndex ->
                        -1

                    currentIndex >
                        carousel.visualIndex ->
                        1

                    else ->
                        1
                }

            val available =
                if (
                    direction > 0
                ) {
                    carousel.visualNext !=
                        null
                } else {
                    carousel.visualPrevious !=
                        null
                }

            /*
             * Normally a real adjacent cover exists. If an
             * external queue replacement jumps outside that
             * window, don't animate a fake neighbour.
             */
            if (
                available
            ) {
                carousel.beginAutomatic(
                    direction
                )

                val target =
                    if (
                        direction > 0
                    ) {
                        -pageWidth
                    } else {
                        pageWidth
                    }

                carousel.x.snapTo(
                    0f
                )

                carousel.x.animateTo(
                    targetValue =
                        target,
                    animationSpec =
                        tween(
                            durationMillis =
                                340
                        )
                )
            }

            /*
             * Adopt only after old visual transaction completes.
             */
            carousel.finishAutomatic(
                id =
                    currentId,
                index =
                    currentIndex,
                current =
                    current,
                previous =
                    previous,
                next =
                    next
            )

            carousel.x.snapTo(
                0f
            )
        }
    }

    /*
     * Exact square visual host.
     *
     * There is no fixed 382dp rectangle around a differently
     * sized square anymore. Artwork and small lyrics own exactly
     * the same measured bounds.
     */
    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal =
                        17.dp
                )
                .aspectRatio(
                    1f
                )
    ) {
        /*
         * Restore original page geometry:
         *
         * one visual page == complete square host width.
         *
         * This fixes previous/current/next covers appearing stuck
         * together after subtracting artwork padding twice.
         */
        val pageWidth =
            constraints.maxWidth
                .toFloat()
                .coerceAtLeast(
                    1f
                )

        LaunchedEffect(
            pageWidth
        ) {
            carousel.width =
                pageWidth
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

                                rawDragX =
                                    0f
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

                                    val resisted =
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
                                            resisted.coerceIn(
                                                -pageWidth,
                                                pageWidth
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
                                            -pageWidth *
                                            .16f &&
                                            canNext -> {

                                            carousel.beginManual(
                                                direction =
                                                    1
                                            )

                                            carousel.x.animateTo(
                                                targetValue =
                                                    -pageWidth,
                                                animationSpec =
                                                    tween(
                                                        durationMillis =
                                                            250
                                                    )
                                            )

                                            nextSong()
                                        }

                                        carousel.x.value >=
                                            pageWidth *
                                            .16f &&
                                            canPrevious -> {

                                            carousel.beginManual(
                                                direction =
                                                    -1
                                            )

                                            carousel.x.animateTo(
                                                targetValue =
                                                    pageWidth,
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

                                    rawDragX =
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

                                    rawDragX =
                                        0f
                                }
                            }
                        )
                    }
        ) {
            /*
             * Carousel and lyrics remain composed in exactly the
             * same square. Only alpha/scale transition changes.
             *
             * No AnimatedContent size transform -> no clipped card.
             */
            val transition =
                remember {
                    Animatable(
                        if (
                            showLyrics
                        ) {
                            1f
                        } else {
                            0f
                        }
                    )
                }

            LaunchedEffect(
                showLyrics
            ) {
                transition.animateTo(
                    targetValue =
                        if (
                            showLyrics
                        ) {
                            1f
                        } else {
                            0f
                        },
                    animationSpec =
                        tween(
                            durationMillis =
                                340
                        )
                )
            }

            val lyricsFraction =
                transition.value
                    .coerceIn(
                        0f,
                        1f
                    )

            /*
             * Artwork carousel.
             */
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

                            scaleX =
                                scale

                            scaleY =
                                scale
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
                    width =
                        pageWidth,
                    enabled =
                        !showLyrics,
                    toggleLyrics =
                        toggleLyrics
                )
            }

            /*
             * Small lyrics surface.
             *
             * The card itself is the square. Nothing outside this
             * layer clips it during animation.
             */
            if (
                showLyrics ||
                lyricsFraction >
                .001f
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

                                scaleX =
                                    scale

                                scaleY =
                                    scale
                            },
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
    width: Float,
    enabled: Boolean,
    toggleLyrics: () -> Unit
) {
    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        previous?.let {
            Cover(
                uri =
                    it,
                modifier =
                    Modifier
                        .fillMaxSize()
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
                Modifier
                    .fillMaxSize()
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
                        enabled =
                            enabled,
                        onClick =
                            toggleLyrics
                    )
        )

        next?.let {
            Cover(
                uri =
                    it,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX =
                                x +
                                    width
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
