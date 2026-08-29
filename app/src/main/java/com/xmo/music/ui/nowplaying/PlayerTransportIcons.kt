package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
internal fun XmoPlayIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val path =
            Path().apply {
                moveTo(
                    w * .31f,
                    h * .24f
                )

                cubicTo(
                    w * .31f,
                    h * .18f,
                    w * .37f,
                    h * .15f,
                    w * .43f,
                    h * .19f
                )

                lineTo(
                    w * .77f,
                    h * .43f
                )

                cubicTo(
                    w * .84f,
                    h * .48f,
                    w * .84f,
                    h * .52f,
                    w * .77f,
                    h * .57f
                )

                lineTo(
                    w * .43f,
                    h * .81f
                )

                cubicTo(
                    w * .37f,
                    h * .85f,
                    w * .31f,
                    h * .82f,
                    w * .31f,
                    h * .76f
                )

                close()
            }

        drawPath(
            path =
                path,
            color =
                color
        )
    }
}

@Composable
internal fun XmoPauseIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
    ) {
        val barWidth =
            size.width *
                .19f

        val barHeight =
            size.height *
                .56f

        val radius =
            barWidth *
                .47f

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    size.width *
                        .28f,
                    size.height *
                        .22f
                ),
            size =
                Size(
                    barWidth,
                    barHeight
                ),
            cornerRadius =
                CornerRadius(
                    radius,
                    radius
                )
        )

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    size.width *
                        .53f,
                    size.height *
                        .22f
                ),
            size =
                Size(
                    barWidth,
                    barHeight
                ),
            cornerRadius =
                CornerRadius(
                    radius,
                    radius
                )
        )
    }
}

@Composable
internal fun XmoPreviousIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .115f

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    w * .20f,
                    h * .25f
                ),
            size =
                Size(
                    bar,
                    h * .50f
                ),
            cornerRadius =
                CornerRadius(
                    bar / 2f,
                    bar / 2f
                )
        )

        val path =
            Path().apply {
                moveTo(
                    w * .69f,
                    h * .24f
                )

                cubicTo(
                    w * .75f,
                    h * .20f,
                    w * .79f,
                    h * .24f,
                    w * .79f,
                    h * .31f
                )

                lineTo(
                    w * .79f,
                    h * .69f
                )

                cubicTo(
                    w * .79f,
                    h * .76f,
                    w * .75f,
                    h * .80f,
                    w * .69f,
                    h * .76f
                )

                lineTo(
                    w * .37f,
                    h * .56f
                )

                cubicTo(
                    w * .29f,
                    h * .51f,
                    w * .29f,
                    h * .49f,
                    w * .37f,
                    h * .44f
                )

                close()
            }

        drawPath(
            path =
                path,
            color =
                color
        )
    }
}

@Composable
internal fun XmoNextIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .115f

        drawRoundRect(
            color =
                color,
            topLeft =
                Offset(
                    w * .685f,
                    h * .25f
                ),
            size =
                Size(
                    bar,
                    h * .50f
                ),
            cornerRadius =
                CornerRadius(
                    bar / 2f,
                    bar / 2f
                )
        )

        val path =
            Path().apply {
                moveTo(
                    w * .31f,
                    h * .24f
                )

                cubicTo(
                    w * .25f,
                    h * .20f,
                    w * .21f,
                    h * .24f,
                    w * .21f,
                    h * .31f
                )

                lineTo(
                    w * .21f,
                    h * .69f
                )

                cubicTo(
                    w * .21f,
                    h * .76f,
                    w * .25f,
                    h * .80f,
                    w * .31f,
                    h * .76f
                )

                lineTo(
                    w * .63f,
                    h * .56f
                )

                cubicTo(
                    w * .71f,
                    h * .51f,
                    w * .71f,
                    h * .49f,
                    w * .63f,
                    h * .44f
                )

                close()
            }

        drawPath(
            path =
                path,
            color =
                color
        )
    }
}

@Composable
internal fun XmoShuffleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val stroke =
            w * .085f

        val style =
            Stroke(
                width =
                    stroke,
                cap =
                    StrokeCap.Round
            )

        val upper =
            Path().apply {
                moveTo(
                    w * .16f,
                    h * .30f
                )

                cubicTo(
                    w * .31f,
                    h * .30f,
                    w * .35f,
                    h * .34f,
                    w * .44f,
                    h * .47f
                )

                cubicTo(
                    w * .55f,
                    h * .63f,
                    w * .61f,
                    h * .70f,
                    w * .78f,
                    h * .70f
                )
            }

        drawPath(
            path =
                upper,
            color =
                color,
            style =
                style
        )

        val lower =
            Path().apply {
                moveTo(
                    w * .16f,
                    h * .70f
                )

                cubicTo(
                    w * .31f,
                    h * .70f,
                    w * .37f,
                    h * .62f,
                    w * .46f,
                    h * .48f
                )

                cubicTo(
                    w * .56f,
                    h * .33f,
                    w * .62f,
                    h * .30f,
                    w * .78f,
                    h * .30f
                )
            }

        drawPath(
            path =
                lower,
            color =
                color,
            style =
                style
        )

        drawSoftArrowHead(
            color =
                color,
            right =
                true,
            center =
                Offset(
                    w * .80f,
                    h * .30f
                ),
            scale =
                w
        )

        drawSoftArrowHead(
            color =
                color,
            right =
                true,
            center =
                Offset(
                    w * .80f,
                    h * .70f
                ),
            scale =
                w
        )
    }
}

@Composable
internal fun XmoRepeatIcon(
    repeatOne: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val stroke =
            w * .082f

        val style =
            Stroke(
                width =
                    stroke,
                cap =
                    StrokeCap.Round
            )

        val top =
            Path().apply {
                moveTo(
                    w * .18f,
                    h * .39f
                )

                cubicTo(
                    w * .18f,
                    h * .29f,
                    w * .27f,
                    h * .25f,
                    w * .39f,
                    h * .25f
                )

                lineTo(
                    w * .78f,
                    h * .25f
                )
            }

        drawPath(
            path =
                top,
            color =
                color,
            style =
                style
        )

        val bottom =
            Path().apply {
                moveTo(
                    w * .82f,
                    h * .61f
                )

                cubicTo(
                    w * .82f,
                    h * .71f,
                    w * .73f,
                    h * .75f,
                    w * .61f,
                    h * .75f
                )

                lineTo(
                    w * .22f,
                    h * .75f
                )
            }

        drawPath(
            path =
                bottom,
            color =
                color,
            style =
                style
        )

        drawSoftArrowHead(
            color =
                color,
            right =
                true,
            center =
                Offset(
                    w * .79f,
                    h * .25f
                ),
            scale =
                w
        )

        drawSoftArrowHead(
            color =
                color,
            right =
                false,
            center =
                Offset(
                    w * .21f,
                    h * .75f
                ),
            scale =
                w
        )

        if (
            repeatOne
        ) {
            drawRoundRect(
                color =
                    color,
                topLeft =
                    Offset(
                        w * .49f,
                        h * .40f
                    ),
                size =
                    Size(
                        w * .075f,
                        h * .22f
                    ),
                cornerRadius =
                    CornerRadius(
                        w * .04f,
                        w * .04f
                    )
            )

            drawLine(
                color =
                    color,
                start =
                    Offset(
                        w * .45f,
                        h * .45f
                    ),
                end =
                    Offset(
                        w * .52f,
                        h * .40f
                    ),
                strokeWidth =
                    w * .06f,
                cap =
                    StrokeCap.Round
            )
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope
    .drawSoftArrowHead(
        color: Color,
        right: Boolean,
        center: Offset,
        scale: Float
    ) {
    val direction =
        if (
            right
        ) {
            1f
        } else {
            -1f
        }

    val path =
        Path().apply {
            moveTo(
                center.x -
                    direction *
                    scale *
                    .075f,
                center.y -
                    scale *
                    .115f
            )

            cubicTo(
                center.x -
                    direction *
                    scale *
                    .095f,
                center.y -
                    scale *
                    .14f,
                center.x -
                    direction *
                    scale *
                    .125f,
                center.y -
                    scale *
                    .10f,
                center.x -
                    direction *
                    scale *
                    .09f,
                center.y -
                    scale *
                    .07f
            )

            lineTo(
                center.x +
                    direction *
                    scale *
                    .045f,
                center.y
            )

            lineTo(
                center.x -
                    direction *
                    scale *
                    .09f,
                center.y +
                    scale *
                    .07f
            )

            cubicTo(
                center.x -
                    direction *
                    scale *
                    .125f,
                center.y +
                    scale *
                    .10f,
                center.x -
                    direction *
                    scale *
                    .095f,
                center.y +
                    scale *
                    .14f,
                center.x -
                    direction *
                    scale *
                    .075f,
                center.y +
                    scale *
                    .115f
            )

            close()
        }

    drawPath(
        path =
            path,
        color =
            color
    )
}
