package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pause
import com.composables.icons.lucide.Play
import com.composables.icons.lucide.Repeat1
import com.composables.icons.lucide.Repeat2
import com.composables.icons.lucide.Shuffle
import com.composables.icons.lucide.SkipBack
import com.composables.icons.lucide.SkipForward

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
        Modifier.fillMaxWidth(),
        horizontalArrangement =
            Arrangement.Center,
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        BarePlayerButton(
            size = 45.dp,
            onClick =
                toggleShuffle
        ) {
            Icon(
                imageVector =
                    Lucide.Shuffle,
                contentDescription =
                    "Shuffle",
                tint =
                    if (
                        shuffleEnabled
                    ) {
                        accent
                    } else {
                        foreground
                    },
                modifier =
                    Modifier.size(23.dp)
            )
        }

        androidx.compose.foundation.layout.Spacer(
            Modifier.size(12.dp)
        )

        BarePlayerButton(
            size = 47.dp,
            enabled =
                hasPrevious,
            onClick = previous
        ) {
            Icon(
                imageVector =
                    Lucide.SkipBack,
                contentDescription =
                    "Previous",
                tint =
                    foreground.copy(
                        alpha =
                            if (
                                hasPrevious
                            ) {
                                1f
                            } else {
                                .30f
                            }
                    ),
                modifier =
                    Modifier.size(29.dp)
            )
        }

        androidx.compose.foundation.layout.Spacer(
            Modifier.size(5.dp)
        )

        BarePlayerButton(
            size = 68.dp,
            onClick =
                togglePlay
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier
                    .size(58.dp)
                    .background(
                        color =
                            playBackground,
                        shape =
                            CircleShape
                    ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        if (isPlaying) {
                            Lucide.Pause
                        } else {
                            Lucide.Play
                        },
                    contentDescription =
                        if (isPlaying) {
                            "Pause"
                        } else {
                            "Play"
                        },
                    tint = foreground,
                    modifier =
                        Modifier.size(35.dp)
                )
            }
        }

        androidx.compose.foundation.layout.Spacer(
            Modifier.size(12.dp)
        )

        BarePlayerButton(
            size = 47.dp,
            enabled = hasNext,
            onClick = next
        ) {
            Icon(
                imageVector =
                    Lucide.SkipForward,
                contentDescription =
                    "Next",
                tint =
                    foreground.copy(
                        alpha =
                            if (hasNext) {
                                1f
                            } else {
                                .30f
                            }
                    ),
                modifier =
                    Modifier.size(29.dp)
            )
        }

        androidx.compose.foundation.layout.Spacer(
            Modifier.size(5.dp)
        )

        BarePlayerButton(
            size = 45.dp,
            onClick =
                cycleRepeat
        ) {
            Icon(
                imageVector =
                    if (
                        repeatMode ==
                        Player.REPEAT_MODE_ONE
                    ) {
                        Lucide.Repeat1
                    } else {
                        Lucide.Repeat2
                    },
                contentDescription =
                    when (repeatMode) {
                        Player.REPEAT_MODE_ONE ->
                            "Repeat one"

                        Player.REPEAT_MODE_ALL ->
                            "Repeat all"

                        else ->
                            "Repeat off"
                    },
                tint =
                    if (
                        repeatMode ==
                        Player.REPEAT_MODE_OFF
                    ) {
                        foreground
                    } else {
                        accent
                    },
                modifier =
                    Modifier.size(23.dp)
            )
        }
    }
}
