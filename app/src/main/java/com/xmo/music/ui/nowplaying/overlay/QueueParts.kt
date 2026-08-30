package com.xmo.music.ui.nowplaying

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.data.Song
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun QueueHandle(
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    Box(
        modifier =
            modifier,
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
                            alpha =
                                .28f
                        )
                    )
        )
    }
}

@Composable
internal fun QueueContent(
    queue: List<Song>,
    currentSongId: Long?,
    colors: HomeColors,
    playIndex: (Int) -> Unit,
    openMenu: (Int) -> Unit
) {
    Column(
        Modifier.fillMaxSize()
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start =
                            18.dp,
                        end =
                            17.dp,
                        top =
                            1.dp,
                        bottom =
                            10.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
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
                    Modifier.weight(
                        1f
                    )
            )

            Text(
                text =
                    "${queue.size} tracks",
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    10.sp
            )
        }

        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start =
                        12.dp,
                    end =
                        12.dp,
                    bottom =
                        30.dp
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

                QueueRow(
                    song =
                        song,
                    active =
                        song.id ==
                            currentSongId,
                    colors =
                        colors,
                    onClick = {
                        playIndex(
                            index
                        )
                    },
                    onLongClick = {
                        openMenu(
                            index
                        )
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
                    60.dp
                )
                .clip(
                    RoundedCornerShape(
                        15.dp
                    )
                )
                .background(
                    if (
                        active
                    ) {
                        accent.copy(
                            alpha =
                                .105f
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
                        50.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            11.dp
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
                        start =
                            10.dp,
                        end =
                            8.dp
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
                maxLines =
                    1,
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
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        if (
            active
        ) {
            QueuePlayingMark(
                color =
                    accent,
                modifier =
                    Modifier
                        .padding(
                            end =
                                10.dp
                        )
                        .size(
                            20.dp
                        )
            )
        }
    }
}

@Composable
internal fun QueueActionMenu(
    song: Song,
    active: Boolean,
    colors: HomeColors,
    play: () -> Unit,
    dismiss: () -> Unit
) {
    /*
     * AnimatedVisibility is kept local so this file owns menu
     * presentation rather than making QueueSheet large again.
     */
    AnimatedVisibility(
        visible =
            true,
        enter =
            fadeIn() +
                scaleIn(
                    initialScale =
                        .94f,
                    animationSpec =
                        spring(
                            dampingRatio =
                                .82f,
                            stiffness =
                                430f
                        )
                ),
        exit =
            fadeOut() +
                scaleOut(
                    targetScale =
                        .96f
                )
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha =
                                .15f
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
                                40.dp
                        )
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                25.dp
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
                                        12.dp
                                    )
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
                                    start =
                                        11.dp
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
                            maxLines =
                                1,
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
                            maxLines =
                                1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(
                    Modifier.height(
                        13.dp
                    )
                )

                PressButton(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                47.dp
                            ),
                    enabled =
                        !active,
                    onClick =
                        play
                ) {
                    Row(
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
                                )
                                .padding(
                                    horizontal =
                                        14.dp
                                ),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        QueuePlayIcon(
                            color =
                                colors.text.copy(
                                    alpha =
                                        if (
                                            active
                                        ) {
                                            .38f
                                        } else {
                                            1f
                                        }
                                ),
                            modifier =
                                Modifier.size(
                                    19.dp
                                )
                        )

                        Spacer(
                            Modifier.width(
                                11.dp
                            )
                        )

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
                                            .46f
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
            }
        }
    }
}

@Composable
private fun QueuePlayingMark(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val barWidth =
            size.width *
                .15f

        val radius =
            barWidth /
                2f

        listOf(
            Triple(
                .17f,
                .40f,
                .36f
            ),
            Triple(
                .425f,
                .21f,
                .55f
            ),
            Triple(
                .68f,
                .31f,
                .45f
            )
        ).forEach {
                item ->

            drawRoundRect(
                color =
                    color,
                topLeft =
                    Offset(
                        size.width *
                            item.first,
                        size.height *
                            item.second
                    ),
                size =
                    Size(
                        barWidth,
                        size.height *
                            item.third
                    ),
                cornerRadius =
                    CornerRadius(
                        radius,
                        radius
                    )
            )
        }
    }
}

@Composable
private fun QueuePlayIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val path =
            Path().apply {
                moveTo(
                    size.width *
                        .30f,
                    size.height *
                        .23f
                )

                cubicTo(
                    size.width *
                        .30f,
                    size.height *
                        .18f,
                    size.width *
                        .36f,
                    size.height *
                        .16f,
                    size.width *
                        .42f,
                    size.height *
                        .20f
                )

                lineTo(
                    size.width *
                        .76f,
                    size.height *
                        .43f
                )

                cubicTo(
                    size.width *
                        .83f,
                    size.height *
                        .48f,
                    size.width *
                        .83f,
                    size.height *
                        .52f,
                    size.width *
                        .76f,
                    size.height *
                        .57f
                )

                lineTo(
                    size.width *
                        .42f,
                    size.height *
                        .80f
                )

                cubicTo(
                    size.width *
                        .36f,
                    size.height *
                        .84f,
                    size.width *
                        .30f,
                    size.height *
                        .82f,
                    size.width *
                        .30f,
                    size.height *
                        .77f
                )

                close()
            }

        drawPath(
            path =
                path,
            color =
                color
        )
    }
}
