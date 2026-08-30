package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.launch

@Composable
internal fun SleepTimerBox(
    colors: HomeColors,
    active: Boolean,
    remainingMs: Long,
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

    var closing by
        remember {
            mutableStateOf(false)
        }

    val scope =
        rememberCoroutineScope()

    val accent =
        LocalXmoAccent.current

    val reveal =
        remember {
            Animatable(0f)
        }

    LaunchedEffect(Unit) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec =
                XmoPlayerAnimation
                    .overlayRevealSpec
        )
    }

    suspend fun closeAnimated(
        action: (() -> Unit)? = null
    ) {
        if (closing) {
            return
        }

        closing = true

        reveal.animateTo(
            targetValue = 0f,
            animationSpec =
                XmoPlayerAnimation
                    .overlayHideSpec
        )

        action?.invoke()
            ?: dismiss()
    }

    val minutes =
        customMinutes
            .toLongOrNull()
            ?: 0L

    val seconds =
        customSeconds
            .toLongOrNull()
            ?: 0L

    val validCustom =
        (
            minutes > 0L ||
                seconds > 0L
            ) &&
            seconds in 0L..59L

    val customDurationMs =
        (
            minutes *
                60L +
                seconds
            ) *
            1_000L

    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        /*
         * Invisible outside-tap target.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .simpleTap {
                        scope.launch {
                            closeAnimated()
                        }
                    }
        )

        Column(
            modifier =
                Modifier
                    .padding(
                        horizontal = 30.dp
                    )
                    .fillMaxWidth()
                    .graphicsLayer {
                        with(
                            XmoPlayerAnimation
                        ) {
                            centerOverlay(
                                reveal.value
                            )
                        }
                    }
                    .clip(
                        RoundedCornerShape(
                            25.dp
                        )
                    )
                    .background(
                        colors.surface
                    )
                    .simpleTap {}
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
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sleep Timer",
                        color =
                            colors.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            17.sp
                    )

                    if (
                        active &&
                        remainingMs > 0L
                    ) {
                        Text(
                            text =
                                "${sleepRemainingTime(remainingMs)} remaining",
                            color =
                                accent,
                            fontFamily =
                                XmoFont.medium,
                            fontSize =
                                10.sp
                        )
                    }
                }

                PremiumCircle(
                    size = 36.dp,
                    background =
                        colors.button,
                    onClick = {
                        scope.launch {
                            closeAnimated()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,
                        contentDescription =
                            "Close",
                        tint =
                            colors.text,
                        modifier =
                            Modifier.size(
                                19.dp
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
                    (
                        presetMinutes,
                        label
                    ) ->

                OverlayAction(
                    icon =
                        Icons.Rounded.Schedule,
                    title = label,
                    colors = colors
                ) {
                    scope.launch {
                        closeAnimated {
                            setTimer(
                                presetMinutes *
                                    60_000L,
                                label
                            )
                        }
                    }
                }
            }

            Spacer(
                Modifier.height(8.dp)
            )

            Text(
                text = "Custom timer",
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    10.sp,
                modifier =
                    Modifier.padding(
                        start = 2.dp,
                        bottom = 6.dp
                    )
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                TimerNumberBox(
                    value =
                        customMinutes,
                    placeholder = "00",
                    label = "MIN",
                    colors = colors,
                    modifier =
                        Modifier.weight(1f),
                    onValueChange = {
                        customMinutes =
                            it
                                .filter(
                                    Char::isDigit
                                )
                                .take(4)
                    }
                )

                Text(
                    text = ":",
                    color =
                        colors.text.copy(
                            alpha = .64f
                        ),
                    fontFamily =
                        XmoFont.bold,
                    fontSize =
                        17.sp,
                    textAlign =
                        TextAlign.Center,
                    modifier =
                        Modifier.width(20.dp)
                )

                TimerNumberBox(
                    value =
                        customSeconds,
                    placeholder = "00",
                    label = "SEC",
                    colors = colors,
                    modifier =
                        Modifier.weight(1f),
                    onValueChange = {
                        val digits =
                            it
                                .filter(
                                    Char::isDigit
                                )
                                .take(2)

                        customSeconds =
                            if (digits.isBlank()) {
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
                    Modifier.width(9.dp)
                )

                PremiumCircle(
                    size = 42.dp,
                    background =
                        if (validCustom) {
                            accent.copy(
                                alpha = .18f
                            )
                        } else {
                            colors.button
                        },
                    enabled =
                        validCustom,
                    onClick = {
                        if (
                            validCustom &&
                            customDurationMs >
                            0L
                        ) {
                            scope.launch {
                                closeAnimated {
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
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Schedule,
                        contentDescription =
                            "Set timer",
                        tint =
                            if (validCustom) {
                                accent
                            } else {
                                colors.sub
                            },
                        modifier =
                            Modifier.size(21.dp)
                    )
                }
            }

            if (validCustom) {
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
                            alpha = .88f
                        ),
                    fontFamily =
                        XmoFont.medium,
                    fontSize =
                        10.sp,
                    modifier =
                        Modifier.padding(
                            start = 2.dp,
                            top = 5.dp
                        )
                )
            }

            if (active) {
                Spacer(
                    Modifier.height(9.dp)
                )

                OverlayAction(
                    icon =
                        Icons.Rounded.Close,
                    title =
                        "Cancel Timer",
                    active = true,
                    colors = colors,
                    click = {
                        scope.launch {
                            closeAnimated {
                                cancel()
                            }
                        }
                    }
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
                .height(52.dp)
                .clip(
                    RoundedCornerShape(
                        14.dp
                    )
                )
                .background(
                    colors.button
                )
                .padding(
                    horizontal = 10.dp
                ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = value,
                onValueChange =
                    onValueChange,
                singleLine = true,
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
                        if (value.isBlank()) {
                            Text(
                                text =
                                    placeholder,
                                color =
                                    colors.sub.copy(
                                        alpha = .62f
                                    ),
                                fontFamily =
                                    XmoFont.bold,
                                fontSize =
                                    15.sp
                            )
                        }

                        inner()
                    }
                }
            )

            Text(
                text = label,
                color =
                    colors.sub.copy(
                        alpha = .72f
                    ),
                fontFamily =
                    XmoFont.medium,
                fontSize = 7.sp
            )
        }
    }
}

private fun sleepRemainingTime(
    milliseconds: Long
): String {
    val totalSeconds =
        (
            milliseconds
                .coerceAtLeast(0L) +
                999L
            ) /
            1_000L

    val hours =
        totalSeconds /
            3_600L

    val minutes =
        (
            totalSeconds %
                3_600L
            ) /
            60L

    val seconds =
        totalSeconds %
            60L

    return if (hours > 0L) {
        "%d:%02d:%02d".format(
            hours,
            minutes,
            seconds
        )
    } else {
        "%02d:%02d".format(
            minutes,
            seconds
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
