package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.launch

@Composable
internal fun QueueSheet(
    queue: List<Song>,
    currentSongId: Long?,
    colors: HomeColors,
    dismiss: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val sheetY =
        remember {
            Animatable(0f)
        }

    val configuration =
        LocalConfiguration.current

    val sheetHeightDp =
        configuration.screenHeightDp *
            .72f

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .18f
                )
            )
            /*
             * No clickable/ripple backdrop.
             * This is intentionally a passive touch blocker.
             */
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        contentAlignment =
            Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(
                    sheetHeightDp.dp
                )
                .graphicsLayer {
                    translationY =
                        sheetY.value
                }
                .clip(
                    RoundedCornerShape(
                        topStart = 30.dp,
                        topEnd = 30.dp
                    )
                )
                .background(
                    colors.surface
                )
        ) {
            /*
             * Dedicated swipe handle.
             *
             * Queue list itself remains free for normal scrolling.
             */
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = {
                                    change,
                                    amount ->

                                if (
                                    amount.y > 0f ||
                                    sheetY.value >
                                    0f
                                ) {
                                    change.consume()

                                    scope.launch {
                                        sheetY.snapTo(
                                            (
                                                sheetY.value +
                                                    amount.y
                                                )
                                                .coerceAtLeast(
                                                    0f
                                                )
                                        )
                                    }
                                }
                            },

                            onDragEnd = {
                                scope.launch {
                                    if (
                                        sheetY.value >
                                        90f
                                    ) {
                                        sheetY.animateTo(
                                            targetValue =
                                                1_000f,
                                            animationSpec =
                                                tween(250)
                                        )

                                        dismiss()
                                    } else {
                                        sheetY.animateTo(
                                            targetValue =
                                                0f,
                                            animationSpec =
                                                spring(
                                                    dampingRatio =
                                                        .82f,
                                                    stiffness =
                                                        430f
                                                )
                                        )
                                    }
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    sheetY.animateTo(
                                        targetValue =
                                            0f,
                                        animationSpec =
                                            spring(
                                                dampingRatio =
                                                    .84f,
                                                stiffness =
                                                    450f
                                            )
                                    )
                                }
                            }
                        )
                    },
                contentAlignment =
                    Alignment.Center
            ) {
                Box(
                    Modifier
                        .width(52.dp)
                        .height(5.dp)
                        .clip(
                            RoundedCornerShape(
                                3.dp
                            )
                        )
                        .background(
                            colors.sub.copy(
                                alpha = .30f
                            )
                        )
                )
            }

            Text(
                text = "Queue",
                color = colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 19.sp,
                modifier =
                    Modifier.padding(
                        start = 18.dp,
                        top = 2.dp,
                        bottom = 11.dp
                    )
            )

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 30.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        6.dp
                    )
            ) {
                items(
                    items = queue,
                    key = { song ->
                        song.id
                    }
                ) { song ->
                    QueueRow(
                        song = song,
                        active =
                            song.id ==
                                currentSongId,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    active: Boolean,
    colors: HomeColors
) {
    val accent =
        LocalXmoAccent.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(
                RoundedCornerShape(
                    14.dp
                )
            )
            .background(
                if (active) {
                    accent.copy(
                        alpha = .10f
                    )
                } else {
                    colors.button
                }
            )
            .padding(5.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(
                        RoundedCornerShape(
                            10.dp
                        )
                    )
                    .background(
                        colors.button
                    ),
            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(1f)
                .padding(
                    horizontal = 10.dp
                )
        ) {
            Text(
                text = song.title,
                color =
                    if (active) {
                        accent
                    } else {
                        colors.text
                    },
                fontFamily =
                    XmoFont.bold,
                fontSize = 12.sp,
                maxLines = 1
            )

            Text(
                text = song.artist,
                color = colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}
