package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont

@Composable
internal fun PlayerBody(
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    hasPrevious: Boolean,
    hasNext: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    colors: HomeColors,
    accent: Color,
    border: Color,
    controlForeground: Color,
    playBackground: Color,
    seekTo: (Long) -> Unit,
    togglePlay: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    toggleShuffle: () -> Unit,
    cycleRepeat: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth()
    ) {
        RoundedSeekBar(
            position = position,
            duration = duration,
            active = accent,
            inactive = border,
            seekTo = seekTo
        )

        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 4.dp
                ),
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Text(
                text =
                    playerTime(position),
                color = colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize = 10.sp
            )

            Text(
                text =
                    playerTime(duration),
                color = colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize = 10.sp
            )
        }

        Spacer(
            Modifier.height(9.dp)
        )

        PlayerControls(
            isPlaying = isPlaying,
            hasPrevious = hasPrevious,
            hasNext = hasNext,
            shuffleEnabled =
                shuffleEnabled,
            repeatMode = repeatMode,
            foreground =
                controlForeground,
            accent = accent,
            playBackground =
                playBackground,
            togglePlay =
                togglePlay,
            previous = previous,
            next = next,
            toggleShuffle =
                toggleShuffle,
            cycleRepeat =
                cycleRepeat
        )

        /*
         * Exact requested visual breathing room above branding.
         */
        Spacer(
            Modifier.height(15.dp)
        )

        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "XMO",
                color =
                    colors.text.copy(
                        alpha = .65f
                    ),
                fontFamily =
                    XmoFont.logo,
                fontSize = 11.sp
            )

            Text(
                text =
                    "lxzrvi • © 2026",
                color =
                    colors.sub.copy(
                        alpha = .52f
                    ),
                fontFamily =
                    XmoFont.normal,
                fontSize = 6.sp
            )
        }

        Spacer(
            Modifier
                .navigationBarsPadding()
                .height(5.dp)
        )
    }
}
