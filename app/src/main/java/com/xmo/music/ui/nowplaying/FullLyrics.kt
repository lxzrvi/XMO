package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward
import com.composables.icons.lucide.X
import com.xmo.music.XmoTheme
import com.xmo.music.data.SongLyrics
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont

@Composable
internal fun FullLyrics(
    lyrics: SongLyrics?,
    position: Long,
    duration: Long,
    title: String,
    artist: String,
    artwork: Uri?,
    dominant: Color,
    deep: Color,
    theme: XmoTheme,
    isPlaying: Boolean,
    canPrevious: Boolean,
    canNext: Boolean,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,
    close: () -> Unit
) {
    val foreground =
        if (
            dominant.luminance() >
            .58f
        ) {
            Color(0xFF111214)
        } else {
            Color.White
        }

    val lyricColors =
        HomeColors(
            bg = Color.Transparent,
            surface = Color.Transparent,
            text = foreground,
            sub =
                foreground.copy(
                    alpha = .60f
                ),
            button =
                foreground.copy(
                    alpha = .10f
                ),
            icon =
                foreground.copy(
                    alpha = .78f
                ),
            border =
                foreground.copy(
                    alpha = .16f
                )
        )

    Box(
        Modifier.fillMaxSize()
    ) {
        PlayerBackground(
            dominant = dominant,
            deep = deep,
            theme = theme
        )

        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(
                        start = 15.dp,
                        end = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = artwork,
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(42.dp)
                            .clip(
                                RoundedCornerShape(
                                    9.dp
                                )
                            ),
                    contentScale =
                        ContentScale.Crop
                )

                Column(
                    Modifier
                        .weight(1f)
                        .padding(
                            start = 10.dp
                        )
                ) {
                    Text(
                        text = title,
                        color = foreground,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    Text(
                        text = artist,
                        color =
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.normal,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )
                }

                XmoCapsule(
                    background =
                        foreground.copy(
                            alpha = .10f
                        )
                ) {
                    CapsuleButton(
                        onClick =
                            togglePlay
                    ) {
                        Icon(
                            imageVector =
                                if (isPlaying) {
                                    Lucide.Pause
                                } else {
                                    Lucide.Play
                                },
                            contentDescription =
                                "Play pause",
                            tint = foreground,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }

                    CapsuleButton(
                        enabled =
                            canPrevious,
                        onClick =
                            previous
                    ) {
                        Icon(
                            imageVector =
                                Lucide.SkipBack,
                            contentDescription =
                                "Previous",
                            tint = foreground,
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    CapsuleButton(
                        enabled =
                            canNext,
                        onClick = next
                    ) {
                        Icon(
                            imageVector =
                                Lucide.SkipForward,
                            contentDescription =
                                "Next",
                            tint = foreground,
                            modifier =
                                Modifier.size(17.dp)
                        )
                    }

                    CapsuleButton(
                        onClick = close
                    ) {
                        Icon(
                            imageVector =
                                Lucide.X,
                            contentDescription =
                                "Close",
                            tint = foreground,
                            modifier =
                                Modifier.size(18.dp)
                        )
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 17.dp
                    )
            ) {
                RoundedSeekBar(
                    position = position,
                    duration = duration,
                    active = foreground,
                    inactive =
                        foreground.copy(
                            alpha = .22f
                        ),
                    seekTo = seekTo
                )

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {
                    Text(
                        text =
                            playerTime(position),
                        color =
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )

                    Text(
                        text =
                            playerTime(duration),
                        color =
                            foreground.copy(
                                alpha = .60f
                            ),
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(
                Modifier.height(4.dp)
            )

            FollowLyrics(
                lyrics = lyrics,
                position = position,
                colors = lyricColors,
                accent = foreground,
                fullscreen = true,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
            )
        }
    }
}
