package com.xmo.music.ui.nowplaying

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
            Animatable(
                0f
            )
        }

    var sheetHeightPx by
        remember {
            mutableFloatStateOf(
                1f
            )
        }

    var menuIndex by
        remember {
            mutableStateOf<Int?>(
                null
            )
        }

    val sheetHeight =
        LocalConfiguration
            .current
            .screenHeightDp
            .dp *
            .72f

    /*
     * Back closes the long-hold menu first. Otherwise it closes
     * QueueSheet. This handler is inside the queue overlay, so it
     * takes priority while the sheet is present.
     */
    BackHandler {
        if (
            menuIndex != null
        ) {
            menuIndex =
                null
        } else {
            dismiss()
        }
    }

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        Box(
            modifier =
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
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .height(
                        sheetHeight
                    )
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
                            topStart =
                                30.dp,
                            topEnd =
                                30.dp
                        )
                    )
                    .background(
                        colors.surface
                    )
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            38.dp
                        )
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
                                                    tween(
                                                        durationMillis =
                                                            280
                                                    )
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
                    modifier =
                        Modifier
                            .width(
                                54.dp
                            )
                            .height(
                                5.dp
                            )
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
                text =
                    "Queue",
                color =
                    colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    19.sp,
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
                    items =
                        queue,
                    key = {
                            _,
                            song ->

                        song.id
                    }
                ) {
                        index,
                        song ->

                    val active =
                        song.id ==
                            currentSongId

                    QueueRow(
                        song =
                            song,
                        active =
                            active,
                        colors =
                            colors,
                        onClick = {
                            /*
                             * Real queue-index playback. Queue
                             * deliberately remains open.
                             */
                            playIndex(
                                index
                            )
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
            queue
                .getOrNull(
                    index
                )
                ?.let { song ->
                    QueueActionMenu(
                        song =
                            song,
                        active =
                            song.id ==
                                currentSongId,
                        colors =
                            colors,
                        play = {
                            playIndex(
                                index
                            )

                            menuIndex =
                                null
                        },
                        dismiss = {
                            menuIndex =
                                null
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
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    58.dp
                )
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    if (
                        active
                    ) {
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
                    indication =
                        null,
                    onClick =
                        onClick,
                    onLongClick =
                        onLongClick
                )
                .padding(
                    5.dp
                ),
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
                    .size(
                        48.dp
                    )
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
            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        horizontal =
                            10.dp
                    )
        ) {
            Text(
                text =
                    song.title,
                color =
                    if (
                        active
                    ) {
                        accent
                    } else {
                        colors.text
                    },
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    12.sp,
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
                fontSize =
                    10.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        /*
         * Dedicated current-song mark instead of an unrelated
         * generic icon. Filled rounded bars fit the XMO transport
         * family.
         */
        if (
            active
        ) {
            QueuePlayingMark(
                color =
                    accent,
                modifier =
                    Modifier
                        .padding(
                            end = 10.dp
                        )
                        .size(
                            19.dp
                        )
            )
        }
    }
}

@Composable
private fun QueuePlayingMark(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier =
            modifier
    ) {
        val barWidth =
            size.width *
                .16f

        val radius =
            barWidth / 2f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    x =
                        size.width *
                            .18f,
                    y =
                        size.height *
                            .39f
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
                        size.height *
                            .38f
                ),
            cornerRadius =
                CornerRadius(
                    radius,
                    radius
                )
        )

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    x =
                        size.width *
                            .42f,
                    y =
                        size.height *
                            .22f
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
                        size.height *
                            .55f
                ),
            cornerRadius =
                CornerRadius(
                    radius,
                    radius
                )
        )

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    x =
                        size.width *
                            .66f,
                    y =
                        size.height *
                            .31f
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
                        size.height *
                            .46f
                ),
            cornerRadius =
                CornerRadius(
                    radius,
                    radius
                )
        )
    }
}

@Composable
private fun QueueActionMenu(
    song: Song,
    active: Boolean,
    colors: HomeColors,
    play: () -> Unit,
    dismiss: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = .13f
                    )
                )
                .simpleTap(
                    dismiss
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            modifier =
                Modifier
                    .padding(
                        horizontal =
                            42.dp
                    )
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            24.dp
                        )
                    )
                    .background(
                        colors.surface
                    )
                    .padding(
                        15.dp
                    )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
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
                            .size(
                                46.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    12.dp
                                )
                            )
                            .background(
                                colors.button
                            ),
                    contentScale =
                        ContentScale.Crop
                )

                Column(
                    modifier =
                        Modifier
                            .weight(
                                1f
                            )
                            .padding(
                                start = 11.dp
                            )
                ) {
                    Text(
                        text =
                            song.title,
                        color =
                            colors.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            14.sp,
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
                        fontSize =
                            10.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        12.dp
                    )
            )

            PressButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            46.dp
                        ),
                enabled =
                    !active,
                onClick =
                    play
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clip(
                                RoundedCornerShape(
                                    15.dp
                                )
                            )
                            .background(
                                colors.button
                            ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        text =
                            if (
                                active
                            ) {
                                "Currently Playing"
                            } else {
                                "Play"
                            },
                        color =
                            colors.text.copy(
                                alpha =
                                    if (
                                        active
                                    ) {
                                        .48f
                                    } else {
                                        1f
                                    }
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize =
                            12.sp
                    )
                }
            }

            /*
             * Play Next / Remove / Reorder are intentionally not
             * exposed. QueueSheet currently receives only the real
             * playIndex API, so displaying those actions would be
             * a fake backend control.
             */
        }
    }
}
