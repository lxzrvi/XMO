package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
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
    playIndex: (Int) -> Unit,
    dismiss: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val sheetY =
        remember {
            Animatable(0f)
        }

    var sheetHeightPx by remember {
        mutableFloatStateOf(1f)
    }

    var menuIndex by remember {
        mutableStateOf<Int?>(
            null
        )
    }

    val sheetHeight =
        LocalConfiguration.current
            .screenHeightDp.dp *
            .72f

    Box(
        Modifier.fillMaxSize()
    ) {
        /*
         * Backdrop closes the queue.
         *
         * No Material indication/press effect.
         */
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = .10f
                    )
                )
                .simpleTap(
                    dismiss
                )
        )

        Column(
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .fillMaxWidth()
                .height(sheetHeight)
                .onSizeChanged {
                    sheetHeightPx =
                        it.height
                            .toFloat()
                            .coerceAtLeast(
                                1f
                            )
                }
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
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .pointerInput(
                        sheetHeightPx
                    ) {
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
                                                .coerceIn(
                                                    0f,
                                                    sheetHeightPx
                                                )
                                        )
                                    }
                                }
                            },

                            onDragEnd = {
                                scope.launch {
                                    if (
                                        sheetY.value >
                                        sheetHeightPx *
                                            .13f
                                    ) {
                                        sheetY.animateTo(
                                            targetValue =
                                                sheetHeightPx,
                                            animationSpec =
                                                tween(280)
                                        )

                                        dismiss()
                                    } else {
                                        sheetY.animateTo(
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
                                                    430f
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
                        .width(54.dp)
                        .height(5.dp)
                        .clip(
                            RoundedCornerShape(
                                3.dp
                            )
                        )
                        .background(
                            colors.sub.copy(
                                alpha = .28f
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
                        top = 1.dp,
                        bottom = 10.dp
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
                itemsIndexed(
                    items = queue,
                    key = {
                            _,
                            song ->

                        song.id
                    }
                ) {
                        index,
                        song ->

                    QueueRow(
                        song = song,
                        active =
                            song.id ==
                                currentSongId,
                        colors = colors,
                        onClick = {
                            playIndex(index)
                        },
                        onLongClick = {
                            menuIndex =
                                index
                        }
                    )
                }
            }
        }

        menuIndex?.let { index ->
            queue.getOrNull(index)
                ?.let { song ->
                    QueueActionMenu(
                        song = song,
                        colors = colors,
                        play = {
                            playIndex(index)
                            menuIndex = null
                        },
                        dismiss = {
                            menuIndex = null
                        }
                    )
                }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    active: Boolean,
    colors: HomeColors,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    val interaction =
        remember {
            MutableInteractionSource()
        }

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
            .combinedClickable(
                interactionSource =
                    interaction,
                indication = null,
                onClick =
                    onClick,
                onLongClick =
                    onLongClick
            )
            .padding(5.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model =
                song.artwork,
            contentDescription =
                null,
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
                text =
                    song.title,
                color =
                    if (active) {
                        accent
                    } else {
                        colors.text
                    },
                fontFamily =
                    XmoFont.bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    song.artist,
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 10.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QueueActionMenu(
    song: Song,
    colors: HomeColors,
    play: () -> Unit,
    dismiss: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .simpleTap(
                dismiss
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 42.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(
                    colors.surface
                )
                .padding(14.dp)
        ) {
            Text(
                text = song.title,
                color = colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 10.sp,
                maxLines = 1
            )

            Spacer(
                Modifier.height(10.dp)
            )

            PressButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                onClick = play
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                13.dp
                            )
                        )
                        .background(
                            colors.button
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text = "Play",
                        color =
                            colors.text,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
