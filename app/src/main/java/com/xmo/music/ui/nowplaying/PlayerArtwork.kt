package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * Frozen visual queue window.
     */
    var snapshotCurrent by
        remember {
            mutableStateOf(
                current
            )
        }

    var snapshotPrevious by
        remember {
            mutableStateOf(
                previous
            )
        }

    var snapshotNext by
        remember {
            mutableStateOf(
                next
            )
        }

    var knownId by
        remember {
            mutableStateOf(
                currentId
            )
        }

    var knownIndex by
        remember {
            mutableStateOf(
                currentIndex
            )
        }

    /*
     * Real Media3 confirmation / external transition.
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

        val coverWidth =
            carousel.width
                .coerceAtLeast(
                    1f
                )

        /*
         * Manual transaction has already moved the incoming
         * neighbour into the centre. Media3 ID change confirms it.
         */
        if (
            carousel.manualDirection != 0 &&
            carousel.manualSongId == oldId
        ) {
            carousel.confirmManualTarget(
                currentId
            )

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
         * Natural next, transport-button next/previous, or other
         * Media3-driven song change.
         */
        if (
            !carousel.transactionActive
        ) {
            val direction =
                when {
                    currentIndex < oldIndex ->
                        -1

                    currentIndex > oldIndex ->
                        1

                    else ->
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
                if (direction < 0) {
                    coverWidth
                } else {
                    -coverWidth
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
                            330
                    )
            )

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
     * Live neighbours may recompose independently. Only adopt
     * them while no frozen visual transaction owns the carousel.
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
         * coverSizeModifier uses 17dp on both horizontal sides.
         * Carousel travel therefore uses the real square-cover
         * width rather than the wider host width.
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
                                        carousel.x.value <
                                            -coverWidth *
                                            .15f &&
                                            canNext -> {

                                            carousel.beginManual(
                                                direction = 1,
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

                                            nextSong()
                                        }

                                        carousel.x.value >
                                            coverWidth *
                                            .15f &&
                                            canPrevious -> {

                                            carousel.beginManual(
                                                direction = -1,
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
                    },
            contentAlignment =
                Alignment.Center
        ) {
            /*
             * Both layers retain identical host bounds.
             * AnimatedContent is deliberately not used so it
             * cannot resize/crop the artwork-sized lyrics card.
             */
            AnimatedVisibility(
                visible =
                    !showLyrics,
                modifier =
                    Modifier.fillMaxSize(),
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    300
                            )
                    ) +
                        scaleIn(
                            initialScale =
                                .985f,
                            animationSpec =
                                tween(
                                    durationMillis =
                                        340
                                )
                        ),
                exit =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    220
                            )
                    ) +
                        scaleOut(
                            targetScale =
                                .985f,
                            animationSpec =
                                tween(
                                    durationMillis =
                                        260
                                )
                        )
            ) {
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

            AnimatedVisibility(
                visible =
                    showLyrics,
                modifier =
                    Modifier.fillMaxSize(),
                enter =
                    fadeIn(
                        animationSpec =
                            tween(
                                durationMillis =
                                    340
                            )
                    ) +
                        scaleIn(
                            initialScale =
                                .975f,
                            animationSpec =
                                tween(
                                    durationMillis =
                                        380
                                )
                        ),
                exit =
                    fadeOut(
                        animationSpec =
                            tween(
                                durationMillis =
                                    230
                            )
                    ) +
                        scaleOut(
                            targetScale =
                                .985f,
                            animationSpec =
                                tween(
                                    durationMillis =
                                        280
                                )
                        )
            ) {
                Box(
                    modifier =
                        Modifier.fillMaxSize(),
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
                            coverSizeModifier()
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
                        alpha = .06f
                    )
                )
    ) {
        AsyncImage(
            model = uri,
            contentDescription =
                null,
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
