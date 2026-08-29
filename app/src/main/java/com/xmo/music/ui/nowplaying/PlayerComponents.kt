package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PremiumCircle(
    size: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue =
                if (pressed) .90f else 1f,
            animationSpec =
                spring(
                    dampingRatio = .62f,
                    stiffness = 700f
                ),
            label = "premiumPress"
        )

    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale

                alpha =
                    when {
                        !enabled -> .40f
                        pressed -> .78f
                        else -> 1f
                    }
            }
            .background(
                background,
                CircleShape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun BarePlayerButton(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interaction =
        remember {
            MutableInteractionSource()
        }

    val pressed by
        interaction.collectIsPressedAsState()

    val scale by
        animateFloatAsState(
            targetValue =
                if (pressed) .86f else 1f,
            animationSpec =
                spring(
                    dampingRatio = .64f,
                    stiffness = 720f
                ),
            label = "playerPress"
        )

    Box(
        modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale

                alpha =
                    if (enabled) 1f
                    else .30f
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment =
            Alignment.Center
    ) {
        content()
    }
}

@Composable
internal fun XmoCapsule(
    background: Color,
    modifier: Modifier = Modifier,
    content:
        @Composable RowScope.() -> Unit
) {
    Row(
        modifier
            .background(
                background,
                RoundedCornerShape(23.dp)
            )
            .padding(horizontal = 2.dp),
        verticalAlignment =
            Alignment.CenterVertically,
        content = content
    )
}

@Composable
internal fun CapsuleButton(
    size: Dp = 40.dp,
    enabled: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    PremiumCircle(
        size = size,
        background = Color.Transparent,
        enabled = enabled,
        onClick = onClick,
        content = content
    )
}

@Composable
internal fun RoundedSeekBar(
    position: Long,
    duration: Long,
    active: Color,
    inactive: Color,
    modifier: Modifier = Modifier,
    seekTo: (Long) -> Unit
) {
    var dragging by remember {
        mutableStateOf(false)
    }

    var draggedFraction by remember {
        mutableFloatStateOf(0f)
    }

    val actualFraction =
        if (duration > 0L) {
            (
                position.toFloat() /
                    duration.toFloat()
                )
                .coerceIn(0f, 1f)
        } else {
            0f
        }

    val fraction =
        if (dragging) {
            draggedFraction
        } else {
            actualFraction
        }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(26.dp)
            // requested 4dp downward placement
            .padding(top = 4.dp)
            .pointerInput(duration) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (duration <= 0L) {
                            return@detectDragGestures
                        }

                        dragging = true

                        draggedFraction =
                            (
                                offset.x /
                                    size.width
                                )
                                .coerceIn(0f, 1f)

                        seekTo(
                            (
                                duration *
                                    draggedFraction
                                ).toLong()
                        )
                    },

                    onDrag = { change, _ ->
                        if (
                            !dragging ||
                            duration <= 0L
                        ) {
                            return@detectDragGestures
                        }

                        change.consume()

                        draggedFraction =
                            (
                                change.position.x /
                                    size.width
                                )
                                .coerceIn(0f, 1f)

                        seekTo(
                            (
                                duration *
                                    draggedFraction
                                ).toLong()
                        )
                    },

                    onDragEnd = {
                        if (
                            dragging &&
                            duration > 0L
                        ) {
                            seekTo(
                                (
                                    duration *
                                        draggedFraction
                                    ).toLong()
                            )
                        }

                        dragging = false
                    },

                    onDragCancel = {
                        dragging = false
                    }
                )
            }
            .pointerInput(duration) {
                detectTapGestures { offset ->
                    if (duration <= 0L) {
                        return@detectTapGestures
                    }

                    val value =
                        (
                            offset.x /
                                size.width
                            )
                            .coerceIn(0f, 1f)

                    seekTo(
                        (
                            duration *
                                value
                            ).toLong()
                    )
                }
            }
    ) {
        val y =
            size.height / 2f

        val stroke =
            2.dp.toPx()

        drawLine(
            color = inactive,
            start = Offset(0f, y),
            end =
                Offset(
                    size.width,
                    y
                ),
            strokeWidth = stroke,
            cap =
                androidx.compose.ui.graphics
                    .StrokeCap.Round
        )

        if (fraction > 0f) {
            drawLine(
                color = active,
                start = Offset(0f, y),
                end =
                    Offset(
                        size.width *
                            fraction,
                        y
                    ),
                strokeWidth = stroke,
                cap =
                    androidx.compose.ui.graphics
                        .StrokeCap.Round
            )
        }

        if (dragging) {
            drawCircle(
                color = active,
                radius = 5.dp.toPx(),
                center =
                    Offset(
                        size.width *
                            fraction,
                        y
                    )
            )
        }
    }
}

internal fun playerTime(
    milliseconds: Long
): String {
    val seconds =
        milliseconds
            .coerceAtLeast(0L) /
            1_000L

    val hours =
        seconds / 3_600L

    val minutes =
        (seconds % 3_600L) /
            60L

    val remaining =
        seconds % 60L

    return if (hours > 0L) {
        "$hours:${
            minutes
                .toString()
                .padStart(2, '0')
        }:${
            remaining
                .toString()
                .padStart(2, '0')
        }"
    } else {
        "$minutes:${
            remaining
                .toString()
                .padStart(2, '0')
        }"
    }
}

internal fun formatBytes(
    bytes: Long
): String =
    when {
        bytes >=
            1024L * 1024L * 1024L ->

            String.format(
                "%.2f GB",
                bytes.toDouble() /
                    (
                        1024.0 *
                            1024.0 *
                            1024.0
                        )
            )

        bytes >=
            1024L * 1024L ->

            String.format(
                "%.1f MB",
                bytes.toDouble() /
                    (
                        1024.0 *
                            1024.0
                        )
            )

        bytes >= 1024L ->

            String.format(
                "%.1f KB",
                bytes.toDouble() /
                    1024.0
            )

        else ->
            "$bytes B"
    }
