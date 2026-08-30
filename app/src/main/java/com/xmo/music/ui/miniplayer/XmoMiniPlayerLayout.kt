package com.xmo.music.ui.miniplayer

import android.net.Uri
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
import androidx.compose.ui.graphics.Path
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
            Color(0xFFF9F9FA)

        /*
         * Neutral black/charcoal. No blue tint.
         * Still visibly lighter than AMOLED.
         */
        XmoTheme.Dark ->
            Color(0xFF181819)

        XmoTheme.Amoled ->
            Color(0xFF080808)
    }

internal fun xmoMiniControlSurface(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color(0xFFE9EAEC)

        XmoTheme.Dark ->
            Color(0xFF2A2A2C)

        XmoTheme.Amoled ->
            Color(0xFF1D1D1F)
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
                alpha = .11f
            )
    }

@Composable
internal fun XmoMiniPlayerCard(
    state: PlaybackState,
    theme: XmoTheme,
    colors: HomeColors,
    accent: Color,
    liked: Boolean,
    moved: Boolean,
    opening: Boolean,
    togglePlay: () -> Unit,
    toggleLike: () -> Unit,
    open: () -> Unit,
    modifier: Modifier = Modifier
) {
    /*
     * Left keeps the original MiniPlayer radius.
     * Right follows the rounded controls more closely without
     * turning the whole card into a capsule.
     */
    val cardShape =
        RoundedCornerShape(
            topStart = 15.dp,
            bottomStart = 15.dp,
            topEnd = 22.dp,
            bottomEnd = 22.dp
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
                    width = .65.dp,
                    color =
                        xmoMiniBorder(theme),
                    shape = cardShape
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
                        width =
                            size.width *
                                progress,
                        height =
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
                    fontSize = 12.sp,
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
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }

        /*
         * Tap only.
         *
         * Long press intentionally has no action.
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
                        opening
                    ) {
                        if (opening) {
                            return@pointerInput
                        }

                        detectTapGestures(
                            onTap = {
                                if (!moved) {
                                    open()
                                }
                            }
                        )
                    }
        )

        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.CenterEnd
                    )
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
                            xmoMiniBorder(theme),
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
                        Modifier.size(18.dp)
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
                            Modifier.size(18.dp)
                    )
                } else {
                    XmoMiniPlayIcon(
                        color =
                            colors.text,
                        modifier =
                            Modifier.size(19.dp)
                    )
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
            Path().apply {
                moveTo(
                    size.width * .30f,
                    size.height * .20f
                )

                lineTo(
                    size.width * .78f,
                    size.height * .50f
                )

                lineTo(
                    size.width * .30f,
                    size.height * .80f
                )

                close()
            }

        drawPath(
            path = path,
            color = color
        )
    }
}
