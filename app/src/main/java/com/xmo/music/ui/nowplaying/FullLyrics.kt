package com.xmo.music.ui.nowplaying

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
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
    accent: Color,
    isPlaying: Boolean,
    canPrevious: Boolean,
    canNext: Boolean,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    seekTo: (Long) -> Unit,
    close: () -> Unit
) {
    val foregroundTarget =
        when (theme) {
            XmoTheme.Light ->
                if (
                    dominant.luminance() >
                    .74f
                ) {
                    Color(0xFF15161A)
                } else {
                    Color(0xFF111216)
                }

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                if (
                    dominant.luminance() >
                    .76f
                ) {
                    Color(0xFF15161A)
                } else {
                    Color.White
                }
        }

    val foreground by
        animateColorAsState(
            targetValue =
                foregroundTarget,
            animationSpec =
                tween(
                    durationMillis =
                        460
                ),
            label =
                "fullLyricsForeground"
        )

    val lyricColors =
        HomeColors(
            bg =
                Color.Transparent,
            surface =
                Color.Transparent,
            text =
                foreground,
            sub =
                foreground.copy(
                    alpha = .56f
                ),
            button =
                foreground.copy(
                    alpha = .10f
                ),
            icon =
                foreground.copy(
                    alpha = .86f
                ),
            border =
                foreground.copy(
                    alpha = .15f
                )
        )

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        PlayerBackground(
            dominant =
                dominant,
            deep =
                deep,
            theme =
                theme
        )

        /*
         * FollowLyrics gets the whole safe-area viewport.
         *
         * Header and seek controls are overlays instead of taking
         * vertical layout space. The lyric viewport center is
         * therefore the physical center of the safe screen.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
        ) {
            FollowLyrics(
                lyrics =
                    lyrics,
                position =
                    position,
                colors =
                    lyricColors,
                accent =
                    accent,
                fullscreen =
                    true,
                modifier =
                    Modifier.fillMaxSize()
            )

            Column(
                modifier =
                    Modifier
                        .align(
                            Alignment.TopCenter
                        )
                        .fillMaxWidth()
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(
                                70.dp
                            )
                            .padding(
                                start = 15.dp,
                                end = 12.dp
                            ),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model =
                            artwork,
                        contentDescription =
                            null,
                        modifier =
                            Modifier
                                .size(
                                    42.dp
                                )
                                .clip(
                                    RoundedCornerShape(
                                        11.dp
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
                                    start = 10.dp
                                )
                    ) {
                        Text(
                            text =
                                title.ifBlank {
                                    "Unknown song"
                                },
                            color =
                                foreground,
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
                                artist.ifBlank {
                                    "Unknown artist"
                                },
                            color =
                                foreground.copy(
                                    alpha = .60f
                                ),
                            fontFamily =
                                XmoFont.medium,
                            fontSize =
                                10.sp,
                            maxLines = 1,
                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    FullLyricsControls(
                        isPlaying =
                            isPlaying,
                        canPrevious =
                            canPrevious,
                        canNext =
                            canNext,
                        foreground =
                            foreground,
                        background =
                            foreground.copy(
                                alpha = .10f
                            ),
                        togglePlay =
                            togglePlay,
                        previous =
                            previous,
                        next =
                            next,
                        close =
                            close
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal =
                                    17.dp
                            )
                ) {
                    RoundedSeekBar(
                        position =
                            position,
                        duration =
                            duration,
                        active =
                            accent,
                        inactive =
                            foreground.copy(
                                alpha = .20f
                            ),
                        seekTo =
                            seekTo
                    )

                    Row(
                        modifier =
                            Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {
                        Text(
                            text =
                                playerTime(
                                    position
                                ),
                            color =
                                foreground.copy(
                                    alpha = .58f
                                ),
                            fontFamily =
                                XmoFont.medium,
                            fontSize =
                                10.sp
                        )

                        Text(
                            text =
                                playerTime(
                                    duration
                                ),
                            color =
                                foreground.copy(
                                    alpha = .58f
                                ),
                            fontFamily =
                                XmoFont.medium,
                            fontSize =
                                10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FullLyricsControls(
    isPlaying: Boolean,
    canPrevious: Boolean,
    canNext: Boolean,
    foreground: Color,
    background: Color,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    close: () -> Unit
) {
    XmoCapsule(
        background =
            background
    ) {
        CapsuleButton(
            size = 39.dp,
            onClick =
                togglePlay
        ) {
            if (
                isPlaying
            ) {
                XmoPauseIcon(
                    color =
                        foreground,
                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )
            } else {
                XmoPlayIcon(
                    color =
                        foreground,
                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )
            }
        }

        CapsuleButton(
            size = 39.dp,
            enabled =
                canPrevious,
            onClick =
                previous
        ) {
            XmoPreviousIcon(
                color =
                    foreground.copy(
                        alpha =
                            if (
                                canPrevious
                            ) {
                                1f
                            } else {
                                .26f
                            }
                    ),
                modifier =
                    Modifier.size(
                        20.dp
                    )
            )
        }

        CapsuleButton(
            size = 39.dp,
            enabled =
                canNext,
            onClick =
                next
        ) {
            XmoNextIcon(
                color =
                    foreground.copy(
                        alpha =
                            if (
                                canNext
                            ) {
                                1f
                            } else {
                                .26f
                            }
                    ),
                modifier =
                    Modifier.size(
                        20.dp
                    )
            )
        }

        CapsuleButton(
            size = 39.dp,
            onClick =
                close
        ) {
            SoftCloseIcon(
                color =
                    foreground,
                modifier =
                    Modifier.size(
                        18.dp
                    )
            )
        }
    }
}

@Composable
private fun SoftCloseIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier =
            modifier
    ) {
        val stroke =
            size.minDimension *
                .115f

        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .25f,
                    size.height *
                        .25f
                ),
            end =
                Offset(
                    size.width *
                        .75f,
                    size.height *
                        .75f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )

        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .75f,
                    size.height *
                        .25f
                ),
            end =
                Offset(
                    size.width *
                        .25f,
                    size.height *
                        .75f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )
    }
}
