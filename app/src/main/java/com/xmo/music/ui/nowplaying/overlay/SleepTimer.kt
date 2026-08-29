package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.TimerReset
import com.composables.icons.lucide.X
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun SleepTimerBox(
    colors: HomeColors,
    active: Boolean,
    dismiss: () -> Unit,
    setTimer: (
        Long,
        String
    ) -> Unit,
    cancel: () -> Unit
) {
    var customMinutes by remember {
        mutableStateOf("")
    }

    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .30f
                )
            )
            .clickable(
                onClick = dismiss
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 30.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(17.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "Sleep Timer",
                    color = colors.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 17.sp,
                    modifier =
                        Modifier.weight(1f)
                )

                PremiumCircle(
                    size = 36.dp,
                    background =
                        colors.button,
                    onClick = dismiss
                ) {
                    Icon(
                        imageVector =
                            Lucide.X,
                        contentDescription =
                            "Close",
                        tint = colors.text,
                        modifier =
                            Modifier.size(
                                16.dp
                            )
                    )
                }
            }

            Spacer(
                Modifier.height(9.dp)
            )

            listOf(
                15L to "15 minutes",
                30L to "30 minutes",
                45L to "45 minutes",
                60L to "1 hour"
            ).forEach {
                    (minutes, label) ->

                OverlayAction(
                    icon =
                        Lucide.Clock3,
                    title = label,
                    colors = colors
                ) {
                    setTimer(
                        minutes *
                            60_000L,
                        label
                    )
                }
            }

            Spacer(
                Modifier.height(5.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(
                        RoundedCornerShape(
                            14.dp
                        )
                    )
                    .background(
                        colors.button
                    )
                    .padding(
                        horizontal = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                BasicTextField(
                    value =
                        customMinutes,
                    onValueChange = {
                        customMinutes =
                            it
                                .filter(
                                    Char::isDigit
                                )
                                .take(4)
                    },
                    singleLine = true,
                    textStyle =
                        TextStyle(
                            color =
                                colors.text,
                            fontFamily =
                                XmoFont.normal,
                            fontSize =
                                12.sp
                        ),
                    modifier =
                        Modifier.weight(1f),
                    decorationBox = {
                        Box {
                            if (
                                customMinutes
                                    .isBlank()
                            ) {
                                Text(
                                    text =
                                        "Custom minutes",
                                    color =
                                        colors.sub,
                                    fontFamily =
                                        XmoFont.normal,
                                    fontSize =
                                        11.sp
                                )
                            }

                            it()
                        }
                    }
                )

                PremiumCircle(
                    size = 34.dp,
                    background =
                        accent.copy(
                            alpha = .16f
                        ),
                    enabled =
                        (
                            customMinutes
                                .toLongOrNull()
                                ?: 0L
                            ) > 0L,
                    onClick = {
                        val minutes =
                            customMinutes
                                .toLongOrNull()
                                ?: return@PremiumCircle

                        setTimer(
                            minutes *
                                60_000L,
                            "$minutes min"
                        )
                    }
                ) {
                    Icon(
                        imageVector =
                            Lucide.TimerReset,
                        contentDescription =
                            "Set custom timer",
                        tint = accent,
                        modifier =
                            Modifier.size(
                                17.dp
                            )
                    )
                }
            }

            if (active) {
                Spacer(
                    Modifier.height(8.dp)
                )

                OverlayAction(
                    icon = Lucide.X,
                    title =
                        "Cancel Timer",
                    active = true,
                    colors = colors,
                    click = cancel
                )
            }
        }
    }
}
