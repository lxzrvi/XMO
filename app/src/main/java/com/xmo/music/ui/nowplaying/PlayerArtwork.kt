package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
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
    current: Uri?,
    previous: Uri?,
    next: Uri?,
    canPrevious: Boolean,
    canNext: Boolean,
    x: Animatable<Float, *>,
    setWidth: (Float) -> Unit,
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
     * Snapshots remain stable until Media3 confirms the actual
     * song ID change.
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

    var transactionSongId by remember {
        mutableStateOf<Long?>(null)
    }

    /*
     * -1 previous
     *  0 idle
     *  1 next
     */
    var pending by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        currentId,
        pending,
        current,
        previous,
        next
    ) {
        if (
            pending != 0 &&
            transactionSongId != null &&
            currentId != transactionSongId
        ) {
            /*
             * Media3 confirmation arrived.
             *
             * Reset displacement before changing the snapshots so
             * the newly-current artwork never appears translated
             * by the old transaction.
             */
            x.snapTo(0f)

            pending = 0
            transactionSongId = null

            snapshotCurrent = current
            snapshotPrevious = previous
            snapshotNext = next
        } else if (pending == 0) {
            snapshotCurrent = current
            snapshotPrevious = previous
            snapshotNext = next
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
            setWidth(width)
        }

        /*
         * Gesture surface always occupies the artwork viewport.
         *
         * Lyrics get their own vertical scroll when visible, so
         * carousel gestures are disabled on that side.
         */
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
                            rawX = 0f
                        },

                        onDrag = {
                                change,
                                amount ->

                            if (pending != 0) {
                                return@detectDragGestures
                            }

                            /*
                             * Vertical artwork dragging has no
                             * player-close behavior anymore.
                             */
                            rawX += amount.x

                            if (
                                abs(rawX) >
                                7f
                            ) {
                                change.consume()

                                scope.launch {
                                    var target =
                                        x.value +
                                            amount.x

                                    if (
                                        target < 0f &&
                                        !canNext
                                    ) {
                                        target =
                                            x.value +
                                                amount.x *
                                                    .17f
                                    }

                                    if (
                                        target > 0f &&
                                        !canPrevious
                                    ) {
                                        target =
                                            x.value +
                                                amount.x *
                                                    .17f
                                    }

                                    x.snapTo(
                                        target.coerceIn(
                                            -width,
                                            width
                                        )
                                    )
                                }
                            }
                        },

                        onDragEnd = {
                            scope.launch {
                                when {
                                    x.value <
                                        -width * .15f &&
                                        canNext -> {

                                        transactionSongId =
                                            currentId

                                        pending = 1

                                        x.animateTo(
                                            targetValue =
                                                -width,
                                            animationSpec =
                                                tween(220)
                                        )

                                        nextSong()
                                    }

                                    x.value >
                                        width * .15f &&
                                        canPrevious -> {

                                        transactionSongId =
                                            currentId

                                        pending = -1

                                        x.animateTo(
                                            targetValue =
                                                width,
                                            animationSpec =
                                                tween(220)
                                        )

                                        previousSong()
                                    }

                                    else -> {
                                        x.animateTo(
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
                                if (pending == 0) {
                                    x.animateTo(
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
            AnimatedContent(
                targetState =
                    showLyrics,
                modifier =
                    Modifier.fillMaxSize(),
                transitionSpec = {
                    (
                        fadeIn(
                            animationSpec =
                                tween(260)
                        ) +
                            scaleIn(
                                initialScale =
                                    .965f,
                                animationSpec =
                                    tween(300)
                            )
                        )
                        .togetherWith(
                            fadeOut(
                                animationSpec =
                                    tween(210)
                            ) +
                                scaleOut(
                                    targetScale =
                                        1.025f,
                                    animationSpec =
                                        tween(240)
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
                            colors =
                                colors,
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
                        x = x.value,
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
                    Modifier
                        .padding(
                            horizontal = 17.dp
                        )
                        .fillMaxWidth()
                        .aspectRatio(1f)
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
                Modifier
                    .padding(
                        horizontal = 17.dp
                    )
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                    Modifier
                        .padding(
                            horizontal = 17.dp
                        )
                        .fillMaxWidth()
                        .aspectRatio(1f)
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
