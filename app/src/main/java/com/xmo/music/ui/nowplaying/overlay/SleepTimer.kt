package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Lucide
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
    var customMinutes by
        remember {
            mutableStateOf("")
        }

    var customSeconds by
        remember {
            mutableStateOf("")
        }

    val accent =
        LocalXmoAccent.current

    val minutes =
        customMinutes
            .toLongOrNull()
            ?: 0L

    val seconds =
        customSeconds
            .toLongOrNull()
            ?: 0L

    /*
     * Seconds input is normalized to 0..59.
     * Minutes can be 0..9999.
     */
    val validCustom =
        (
            minutes > 0L ||
                seconds > 0L
            ) &&
            seconds in
                0L..59L

    val customDurationMs =
        (
            minutes *
                60L +
                seconds
            ) *
            1_000L

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Color.Black.copy(
                        alpha = .30f
                    )
                )
                .clickable(
                    interactionSource =
                        remember {
                            MutableInteractionSource()
                        },
                    indication = null,
                    onClick =
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
                            30.dp
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
                    .clickable(
                        interactionSource =
                            remember {
                                MutableInteractionSource()
                            },
                        indication = null,
                        onClick = {}
                    )
                    .padding(
                        17.dp
                    )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "Sleep Timer",
                    color =
                        colors.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize =
                        17.sp,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                PremiumCircle(
                    size =
                        36.dp,
                    background =
                        colors.button,
                    onClick =
                        dismiss
                ) {
                    SleepCloseIcon(
                        color =
                            colors.text,
                        modifier =
                            Modifier.size(
                                17.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        9.dp
                    )
            )

            listOf(
                15L to
                    "15 minutes",
                30L to
                    "30 minutes",
                45L to
                    "45 minutes",
                60L to
                    "1 hour"
            ).forEach {
                    (
                        presetMinutes,
                        label
                    ) ->

                OverlayAction(
                    icon =
                        Lucide.Clock3,
                    title =
                        label,
                    colors =
                        colors
                ) {
                    setTimer(
                        presetMinutes *
                            60_000L,
                        label
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        8.dp
                    )
            )

            Text(
                text =
                    "Custom timer",
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    10.sp,
                modifier =
                    Modifier.padding(
                        start =
                            2.dp,
                        bottom =
                            6.dp
                    )
            )

            /*
             * Two independent fields:
             *
             * MINUTES | SECONDS | SET
             */
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(
                            52.dp
                        ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TimerNumberBox(
                    value =
                        customMinutes,
                    placeholder =
                        "00",
                    label =
                        "MIN",
                    colors =
                        colors,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onValueChange = {
                        customMinutes =
                            it
                                .filter(
                                    Char::isDigit
                                )
                                .take(
                                    4
                                )
                    }
                )

                Text(
                    text =
                        ":",
                    color =
                        colors.text.copy(
                            alpha =
                                .64f
                        ),
                    fontFamily =
                        XmoFont.bold,
                    fontSize =
                        17.sp,
                    textAlign =
                        TextAlign.Center,
                    modifier =
                        Modifier.width(
                            20.dp
                        )
                )

                TimerNumberBox(
                    value =
                        customSeconds,
                    placeholder =
                        "00",
                    label =
                        "SEC",
                    colors =
                        colors,
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    onValueChange = {
                        val digits =
                            it
                                .filter(
                                    Char::isDigit
                                )
                                .take(
                                    2
                                )

                        /*
                         * Keep the seconds field immediately valid.
                         * Typing 99 becomes 59 instead of silently
                         * creating 99 seconds.
                         */
                        customSeconds =
                            if (
                                digits.isBlank()
                            ) {
                                ""
                            } else {
                                digits
                                    .toIntOrNull()
                                    ?.coerceIn(
                                        0,
                                        59
                                    )
                                    ?.toString()
                                    ?: ""
                            }
                    }
                )

                Spacer(
                    modifier =
                        Modifier.width(
                            9.dp
                        )
                )

                PremiumCircle(
                    size =
                        42.dp,
                    background =
                        if (
                            validCustom
                        ) {
                            accent.copy(
                                alpha =
                                    .18f
                            )
                        } else {
                            colors.button
                        },
                    enabled =
                        validCustom,
                    onClick = {
                        if (
                            !validCustom ||
                            customDurationMs <=
                            0L
                        ) {
                            return@PremiumCircle
                        }

                        setTimer(
                            customDurationMs,
                            customTimerLabel(
                                minutes =
                                    minutes,
                                seconds =
                                    seconds
                            )
                        )
                    }
                ) {
                    SleepTimerSetIcon(
                        color =
                            if (
                                validCustom
                            ) {
                                accent
                            } else {
                                colors.sub
                            },
                        modifier =
                            Modifier.size(
                                20.dp
                            )
                    )
                }
            }

            if (
                validCustom
            ) {
                Text(
                    text =
                        customTimerLabel(
                            minutes =
                                minutes,
                            seconds =
                                seconds
                        ),
                    color =
                        accent.copy(
                            alpha =
                                .88f
                        ),
                    fontFamily =
                        XmoFont.medium,
                    fontSize =
                        10.sp,
                    modifier =
                        Modifier.padding(
                            start =
                                2.dp,
                            top =
                                5.dp
                        )
                )
            }

            if (
                active
            ) {
                Spacer(
                    modifier =
                        Modifier.height(
                            9.dp
                        )
                )

                OverlayAction(
                    icon =
                        Lucide.X,
                    title =
                        "Cancel Timer",
                    active =
                        true,
                    colors =
                        colors,
                    click =
                        cancel
                )
            }
        }
    }
}

@Composable
private fun TimerNumberBox(
    value: String,
    placeholder: String,
    label: String,
    colors: HomeColors,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier =
            modifier
                .height(
                    52.dp
                )
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    colors.button
                )
                .padding(
                    horizontal =
                        10.dp
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value =
                    value,
                onValueChange =
                    onValueChange,
                singleLine =
                    true,
                textStyle =
                    TextStyle(
                        color =
                            colors.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            15.sp,
                        textAlign =
                            TextAlign.Center
                    ),
                modifier =
                    Modifier.fillMaxWidth(),
                decorationBox = {
                        inner ->

                    Box(
                        modifier =
                            Modifier.fillMaxWidth(),
                        contentAlignment =
                            Alignment.Center
                    ) {
                        if (
                            value.isBlank()
                        ) {
                            Text(
                                text =
                                    placeholder,
                                color =
                                    colors.sub.copy(
                                        alpha =
                                            .62f
                                    ),
                                fontFamily =
                                    XmoFont.bold,
                                fontSize =
                                    15.sp,
                                textAlign =
                                    TextAlign.Center
                            )
                        }

                        inner()
                    }
                }
            )

            Text(
                text =
                    label,
                color =
                    colors.sub.copy(
                        alpha =
                            .72f
                    ),
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    7.sp
            )
        }
    }
}

@Composable
private fun SleepCloseIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
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

@Composable
private fun SleepTimerSetIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val stroke =
            size.minDimension *
                .09f

        drawCircle(
            color =
                color,
            radius =
                size.minDimension *
                    .30f,
            center =
                center,
            style =
                androidx.compose.ui.graphics.drawscope.Stroke(
                    width =
                        stroke
                )
        )

        drawLine(
            color =
                color,
            start =
                center,
            end =
                Offset(
                    center.x,
                    size.height *
                        .31f
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
                center,
            end =
                Offset(
                    size.width *
                        .64f,
                    size.height *
                        .56f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )

        /*
         * Small filled confirmation mark.
         */
        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .62f,
                    size.height *
                        .75f
                ),
            end =
                Offset(
                    size.width *
                        .71f,
                    size.height *
                        .83f
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
                        .71f,
                    size.height *
                        .83f
                ),
            end =
                Offset(
                    size.width *
                        .88f,
                    size.height *
                        .65f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )
    }
}

private fun customTimerLabel(
    minutes: Long,
    seconds: Long
): String =
    when {
        minutes > 0L &&
            seconds > 0L ->
            "$minutes min ${seconds}s"

        minutes > 0L ->
            "$minutes min"

        else ->
            "${seconds}s"
    }
