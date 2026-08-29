package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player

@Composable
internal fun PlayerControls(
    isPlaying: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    foreground: Color,
    accent: Color,
    playBackground: Color,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    toggleShuffle: () -> Unit,
    cycleRepeat: () -> Unit
) {
    Row(
        modifier =
            Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.Center
    ) {
        BarePlayerButton(
            size = 45.dp,
            onClick =
                toggleShuffle
        ) {
            XmoShuffleIcon(
                color =
                    if (
                        shuffleEnabled
                    ) {
                        accent
                    } else {
                        foreground
                    },
                modifier =
                    Modifier.size(
                        24.dp
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.size(
                    12.dp
                )
        )

        BarePlayerButton(
            size = 47.dp,
            enabled =
                hasPrevious,
            onClick =
                previous
        ) {
            XmoPreviousIcon(
                color =
                    foreground.copy(
                        alpha =
                            if (
                                hasPrevious
                            ) {
                                1f
                            } else {
                                .28f
                            }
                    ),
                modifier =
                    Modifier.size(
                        31.dp
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.size(
                    4.dp
                )
        )

        BarePlayerButton(
            size = 68.dp,
            onClick =
                togglePlay
        ) {
            Box(
                modifier =
                    Modifier
                        .size(
                            58.dp
                        )
                        .background(
                            color =
                                playBackground,
                            shape =
                                CircleShape
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                if (
                    isPlaying
                ) {
                    XmoPauseIcon(
                        color =
                            foreground,
                        modifier =
                            Modifier.size(
                                36.dp
                            )
                    )
                } else {
                    XmoPlayIcon(
                        color =
                            foreground,
                        modifier =
                            Modifier.size(
                                37.dp
                            )
                    )
                }
            }
        }

        Spacer(
            modifier =
                Modifier.size(
                    4.dp
                )
        )

        BarePlayerButton(
            size = 47.dp,
            enabled =
                hasNext,
            onClick =
                next
        ) {
            XmoNextIcon(
                color =
                    foreground.copy(
                        alpha =
                            if (
                                hasNext
                            ) {
                                1f
                            } else {
                                .28f
                            }
                    ),
                modifier =
                    Modifier.size(
                        31.dp
                    )
            )
        }

        Spacer(
            modifier =
                Modifier.size(
                    12.dp
                )
        )

        BarePlayerButton(
            size = 45.dp,
            onClick =
                cycleRepeat
        ) {
            XmoRepeatIcon(
                repeatOne =
                    repeatMode ==
                        Player.REPEAT_MODE_ONE,
                color =
                    if (
                        repeatMode ==
                        Player.REPEAT_MODE_OFF
                    ) {
                        foreground
                    } else {
                        accent
                    },
                modifier =
                    Modifier.size(
                        25.dp
                    )
            )
        }
    }
}
