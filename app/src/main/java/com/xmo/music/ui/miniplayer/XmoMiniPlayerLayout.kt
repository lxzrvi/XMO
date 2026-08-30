package com.xmo.music.ui.miniplayer

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont
import com.xmo.music.ui.nowplaying.XmoPauseIcon
import com.xmo.music.ui.nowplaying.XmoPlayIcon

internal fun xmoMiniSurface(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color(0xFFF9F9FA)
                .copy(alpha = .98f)

        XmoTheme.Dark ->
            Color(0xFF181819)
                .copy(alpha = .98f)

        XmoTheme.Amoled ->
            Color(0xFF080808)
                .copy(alpha = .985f)
    }

internal fun xmoMiniControlSurface(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color(0xFFE9E9EB)

        XmoTheme.Dark ->
            Color(0xFF29292B)

        XmoTheme.Amoled ->
            Color(0xFF1C1C1E)
    }

internal fun xmoMiniBorder(
    theme: XmoTheme
): Color =
    when (theme) {
        XmoTheme.Light ->
            Color.Black.copy(alpha = .08f)

        XmoTheme.Dark ->
            Color.White.copy(alpha = .105f)

        XmoTheme.Amoled ->
            Color.White.copy(alpha = .13f)
    }

@Composable
internal fun XmoMiniPlayerCard(
    song: Song,
    isPlaying: Boolean,
    position: Long,
    duration: Long,
    theme: XmoTheme,
    colors: HomeColors,
    accent: Color,
    liked: Boolean,
    transitionDirection: Int,
    moved: Boolean,
    opening: Boolean,
    togglePlay: () -> Unit,
    toggleLike: () -> Unit,
    open: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape =
        RoundedCornerShape(15.dp)

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
                    shape =
                        cardShape
                )
    ) {
        val progress =
            if (duration > 0L) {
                (
                    position.toFloat() /
                        duration.toFloat()
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
            /*
             * Artwork position does not move.
             * AnimatedContent is clipped to this exact square.
             */
            Box(
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
                        )
            ) {
                AnimatedContent(
                    targetState = song,
                    modifier =
                        Modifier.fillMaxSize(),
                    transitionSpec = {
                        XmoMiniPlayerAnimation
                            .artworkChange()
                    },
                    label =
                        "miniArtwork"
                ) { visualSong ->

                    AsyncImage(
                        model =
                            visualSong.artwork,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.fillMaxSize(),
                        contentScale =
                            ContentScale.Crop
                    )
                }
            }

            /*
             * Metadata owns the directional transition.
             */
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(
                            RoundedCornerShape(
                                1.dp
                            )
                        )
            ) {
                AnimatedContent(
                    targetState = song,
                    modifier =
                        Modifier.fillMaxSize(),
                    transitionSpec = {
                        XmoMiniPlayerAnimation
                            .metadataChange(
                                transitionDirection
                            )
                    },
                    label =
                        "miniMetadata"
                ) { visualSong ->

                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(
                                    start = 9.dp,
                                    end = 5.dp
                                ),
                        verticalArrangement =
                            Arrangement.Center
                    ) {
                        Text(
                            text =
                                visualSong.title,
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
                                visualSong.artist,
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
            }
        }

        Box(
            modifier =
                Modifier
                    .align(
                        Alignment.CenterStart
                    )
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(end = 94.dp)
                    .pointerInput(
                        song.id,
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
                    .padding(end = 6.dp)
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
                            liked,
                            song.id
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
                            when (theme) {
                                XmoTheme.Light ->
                                    Color(0xFF15161A)

                                XmoTheme.Dark,
                                XmoTheme.Amoled ->
                                    Color.White
                            }
                        },
                    modifier =
                        Modifier.size(19.dp)
                )
            }

            Box(
                modifier =
                    Modifier
                        .size(42.dp)
                        .pointerInput(
                            isPlaying
                        ) {
                            detectTapGestures {
                                togglePlay()
                            }
                        },
                contentAlignment =
                    Alignment.Center
            ) {
                if (isPlaying) {
                    XmoPauseIcon(
                        color = colors.text,
                        modifier =
                            Modifier.size(19.dp)
                    )
                } else {
                    XmoPlayIcon(
                        color = colors.text,
                        modifier =
                            Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
