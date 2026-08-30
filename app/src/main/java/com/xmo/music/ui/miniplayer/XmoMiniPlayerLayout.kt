package com.xmo.music.ui.miniplayer

import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.player.PlaybackState
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont

internal fun xmoMiniSurface(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color(0xFFF8F8FA)
                .copy(alpha = .97f)

        XmoTheme.Dark ->
            Color(0xFF202126)
                .copy(alpha = .97f)

        XmoTheme.Amoled ->
            Color(0xFF090A0C)
                .copy(alpha = .98f)
    }

internal fun xmoMiniControlSurface(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color(0xFFE8E9EC)

        XmoTheme.Dark ->
            Color(0xFF34363C)

        XmoTheme.Amoled ->
            Color(0xFF25262A)
    }

internal fun xmoMiniBorder(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color.Black.copy(
                alpha = .08f
            )

        XmoTheme.Dark,
        XmoTheme.Amoled ->
            Color.White.copy(
                alpha = .12f
            )
    }

@Composable
internal fun XmoMiniPlayerCard(
    state: PlaybackState,
    theme: XmoTheme,
    colors: HomeColors,
    accent: Color,
    liked: Boolean,
    closeMode: Boolean,
    moved: Boolean,
    opening: Boolean,
    togglePlay: () -> Unit,
    toggleLike: () -> Unit,
    toggleCloseMode: () -> Unit,
    close: () -> Unit,
    open: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape =
        RoundedCornerShape(
            topStart = 15.dp,
            bottomStart = 15.dp,
            topEnd = 30.dp,
            bottomEnd = 30.dp
        )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(cardShape)
                .background(
                    xmoMiniSurface(theme)
                )
                .border(
                    width = .7.dp,
                    color =
                        xmoMiniBorder(theme),
                    shape =
                        cardShape
                )
    ) {
        val progress =
            if (state.duration > 0L) {
                (
                    state.position.toFloat() /
                        state.duration.toFloat()
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                0f
            }

        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .align(
                        Alignment.TopStart
                    )
        ) {
            drawRect(
                color = accent,
                size =
                    Size(
                        size.width *
                            progress,
                        size.height
                    )
            )
        }

        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(
                        start = 4.dp,
                        top = 4.dp,
                        end = 94.dp,
                        bottom = 4.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            AsyncImage(
                model =
                    state.artworkUri
                        ?.let(Uri::parse),
                contentDescription = null,
                modifier =
                    Modifier
                        .size(50.dp)
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
                        .weight(1f)
                        .padding(
                            start = 9.dp,
                            end = 5.dp
                        )
            ) {
                Text(
                    text =
                        state.title,
                    color =
                        colors.text,
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
                        state.artist,
                    color =
                        colors.sub,
                    fontFamily =
                        XmoFont.normal,
                    fontSize =
                        9.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }

        /*
         * Artwork/title area owns tap + long press.
         *
         * Long press only changes controls into close mode.
         * It never opens Now Playing.
         */
        Box(
            modifier =
                Modifier
                    .align(
                        Alignment.CenterStart
                    )
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(
                        end = 94.dp
                    )
                    .pointerInput(
                        state.currentSongId,
                        moved,
                        opening,
                        closeMode
                    ) {
                        if (opening) {
                            return@pointerInput
                        }

                        detectTapGestures(
                            onTap = {
                                if (
                                    !moved &&
                                    !closeMode
                                ) {
                                    open()
                                }
                            },
                            onLongPress = {
                                if (!moved) {
                                    toggleCloseMode()
                                }
                            }
                        )
                    }
        )

        AnimatedContent(
            targetState =
                closeMode,
            modifier =
                Modifier.align(
                    Alignment.CenterEnd
                ),
            transitionSpec = {
                fadeIn(
                    tween(140)
                )
                    .togetherWith(
                        fadeOut(
                            tween(110)
                        )
                    )
            },
            label =
                "xmoMiniControls"
        ) { showingClose ->

            if (showingClose) {
                Box(
                    modifier =
                        Modifier
                            .padding(
                                end = 7.dp
                            )
                            .size(44.dp)
                            .clip(
                                RoundedCornerShape(
                                    22.dp
                                )
                            )
                            .background(
                                xmoMiniControlSurface(
                                    theme
                                )
                            )
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    close()
                                }
                            },
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,
                        contentDescription =
                            "Close player",
                        tint =
                            colors.text,
                        modifier =
                            Modifier.size(
                                22.dp
                            )
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .padding(
                                end = 6.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    24.dp
                                )
                            )
                            .background(
                                xmoMiniControlSurface(
                                    theme
                                )
                            )
                            .border(
                                width = .6.dp,
                                color =
                                    xmoMiniBorder(
                                        theme
                                    ),
                                shape =
                                    RoundedCornerShape(
                                        24.dp
                                    )
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(38.dp)
                                .pointerInput(
                                    liked
                                ) {
                                    detectTapGestures {
                                        toggleLike()
                                    }
                                },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        Icon(
                            imageVector =
                                Icons.Rounded.Favorite,
                            contentDescription =
                                if (liked) {
                                    "Unlike"
                                } else {
                                    "Like"
                                },
                            tint =
                                if (liked) {
                                    accent
                                } else {
                                    colors.icon
                                },
                            modifier =
                                Modifier.size(
                                    18.dp
                                )
                        )
                    }

                    Box(
                        modifier =
                            Modifier
                                .size(42.dp)
                                .pointerInput(
                                    state.isPlaying
                                ) {
                                    detectTapGestures {
                                        togglePlay()
                                    }
                                },
                        contentAlignment =
                            Alignment.Center
                    ) {
                        if (state.isPlaying) {
                            XmoMiniPauseIcon(
                                color =
                                    colors.text,
                                modifier =
                                    Modifier.size(
                                        18.dp
                                    )
                            )
                        } else {
                            XmoMiniPlayIcon(
                                color =
                                    colors.text,
                                modifier =
                                    Modifier.size(
                                        19.dp
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun XmoMiniPauseIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val barWidth =
            size.width * .19f

        val barHeight =
            size.height * .68f

        val top =
            (
                size.height -
                    barHeight
                ) / 2f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .25f,
                    top
                ),
            size =
                Size(
                    barWidth,
                    barHeight
                ),
            cornerRadius =
                CornerRadius(
                    barWidth / 2f
                )
        )

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .56f,
                    top
                ),
            size =
                Size(
                    barWidth,
                    barHeight
                ),
            cornerRadius =
                CornerRadius(
                    barWidth / 2f
                )
        )
    }
}

@Composable
private fun XmoMiniPlayIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier) {
        val path =
            androidx.compose.ui.graphics.Path()

        path.moveTo(
            size.width * .30f,
            size.height * .20f
        )

        path.lineTo(
            size.width * .78f,
            size.height * .50f
        )

        path.lineTo(
            size.width * .30f,
            size.height * .80f
        )

        path.close()

        drawPath(
            path = path,
            color = color
        )
    }
}
