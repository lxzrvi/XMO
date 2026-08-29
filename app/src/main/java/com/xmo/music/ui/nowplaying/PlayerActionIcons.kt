package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun FilledHeart(
    filled: Boolean,
    color: Color
) {
    Canvas(
        modifier =
            Modifier.size(
                20.dp
            )
    ) {
        val w =
            size.width

        val h =
            size.height

        /*
         * Softer heart with broader curved lobes and a less
         * needle-like bottom point.
         */
        val path =
            Path().apply {
                moveTo(
                    w * .50f,
                    h * .87f
                )

                cubicTo(
                    w * .44f,
                    h * .81f,
                    w * .13f,
                    h * .63f,
                    w * .13f,
                    h * .36f
                )

                cubicTo(
                    w * .13f,
                    h * .20f,
                    w * .25f,
                    h * .11f,
                    w * .37f,
                    h * .11f
                )

                cubicTo(
                    w * .44f,
                    h * .11f,
                    w * .49f,
                    h * .16f,
                    w * .50f,
                    h * .23f
                )

                cubicTo(
                    w * .52f,
                    h * .16f,
                    w * .57f,
                    h * .11f,
                    w * .64f,
                    h * .11f
                )

                cubicTo(
                    w * .77f,
                    h * .11f,
                    w * .87f,
                    h * .20f,
                    w * .87f,
                    h * .36f
                )

                cubicTo(
                    w * .87f,
                    h * .63f,
                    w * .56f,
                    h * .81f,
                    w * .50f,
                    h * .87f
                )

                close()
            }

        if (
            filled
        ) {
            drawPath(
                path = path,
                color = color
            )
        } else {
            drawPath(
                path = path,
                color = color,
                style =
                    Stroke(
                        width =
                            1.9.dp.toPx(),
                        cap =
                            StrokeCap.Round,
                        join =
                            StrokeJoin.Round
                    )
            )
        }
    }
}

@Composable
internal fun FilledStar(
    filled: Boolean,
    color: Color
) {
    Canvas(
        modifier =
            Modifier.size(
                19.dp
            )
    ) {
        val center =
            Offset(
                x =
                    size.width / 2f,
                y =
                    size.height / 2f
            )

        /*
         * Slightly shallower inner radius makes the category star
         * fuller and less sharp/pin-like.
         */
        val outer =
            size.minDimension *
                .45f

        val inner =
            outer *
                .50f

        val path =
            Path()

        repeat(
            10
        ) { index ->
            val radius =
                if (
                    index % 2 == 0
                ) {
                    outer
                } else {
                    inner
                }

            val angle =
                (
                    -90.0 +
                        index *
                        36.0
                    ) *
                    Math.PI /
                    180.0

            val point =
                Offset(
                    x =
                        center.x +
                            (
                                cos(
                                    angle
                                ) *
                                    radius
                                )
                                .toFloat(),
                    y =
                        center.y +
                            (
                                sin(
                                    angle
                                ) *
                                    radius
                                )
                                .toFloat()
                )

            if (
                index == 0
            ) {
                path.moveTo(
                    point.x,
                    point.y
                )
            } else {
                path.lineTo(
                    point.x,
                    point.y
                )
            }
        }

        path.close()

        if (
            filled
        ) {
            drawPath(
                path = path,
                color = color
            )
        } else {
            drawPath(
                path = path,
                color = color,
                style =
                    Stroke(
                        width =
                            1.8.dp.toPx(),
                        cap =
                            StrokeCap.Round,
                        join =
                            StrokeJoin.Round
                    )
            )
        }
    }
}
