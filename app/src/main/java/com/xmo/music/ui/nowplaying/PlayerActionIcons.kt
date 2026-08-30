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
        Modifier.size(
            21.dp
        )
    ) {
        val w =
            size.width

        val h =
            size.height

        val path =
            Path().apply {
                moveTo(
                    w * .50f,
                    h * .88f
                )

                cubicTo(
                    w * .43f,
                    h * .82f,
                    w * .12f,
                    h * .63f,
                    w * .12f,
                    h * .36f
                )

                cubicTo(
                    w * .12f,
                    h * .19f,
                    w * .25f,
                    h * .10f,
                    w * .37f,
                    h * .10f
                )

                cubicTo(
                    w * .44f,
                    h * .10f,
                    w * .49f,
                    h * .15f,
                    w * .50f,
                    h * .23f
                )

                cubicTo(
                    w * .52f,
                    h * .15f,
                    w * .57f,
                    h * .10f,
                    w * .64f,
                    h * .10f
                )

                cubicTo(
                    w * .78f,
                    h * .10f,
                    w * .88f,
                    h * .20f,
                    w * .88f,
                    h * .36f
                )

                cubicTo(
                    w * .88f,
                    h * .63f,
                    w * .57f,
                    h * .82f,
                    w * .50f,
                    h * .88f
                )

                close()
            }

        /*
         * Heart is deliberately filled in both states.
         *
         * PlayerInfo supplies neutral/white for inactive and
         * XMO accent red for active.
         */
        drawPath(
            path = path,
            color = color
        )
    }
}

@Composable
internal fun FilledStar(
    filled: Boolean,
    color: Color
) {
    Canvas(
        Modifier.size(
            19.dp
        )
    ) {
        val center =
            Offset(
                size.width / 2f,
                size.height / 2f
            )

        val outer =
            size.minDimension *
                .43f

        val inner =
            outer *
                .53f

        val path =
            Path()

        repeat(10) { index ->
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
                        index * 36.0
                    ) *
                    Math.PI /
                    180.0

            val point =
                Offset(
                    x =
                        center.x +
                            (
                                cos(angle) *
                                    radius
                                ).toFloat(),
                    y =
                        center.y +
                            (
                                sin(angle) *
                                    radius
                                ).toFloat()
                )

            if (index == 0) {
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

        if (filled) {
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
