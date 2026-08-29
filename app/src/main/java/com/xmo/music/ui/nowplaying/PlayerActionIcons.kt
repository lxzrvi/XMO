package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun FilledHeart(
    filled: Boolean,
    color: Color
) {
    Canvas(
        Modifier.size(19.dp)
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
                    h * .80f,
                    w * .10f,
                    h * .59f,
                    w * .10f,
                    h * .32f
                )

                cubicTo(
                    w * .10f,
                    h * .14f,
                    w * .23f,
                    h * .07f,
                    w * .35f,
                    h * .07f
                )

                cubicTo(
                    w * .43f,
                    h * .07f,
                    w * .48f,
                    h * .12f,
                    w * .50f,
                    h * .18f
                )

                cubicTo(
                    w * .53f,
                    h * .12f,
                    w * .58f,
                    h * .07f,
                    w * .66f,
                    h * .07f
                )

                cubicTo(
                    w * .80f,
                    h * .07f,
                    w * .90f,
                    h * .17f,
                    w * .90f,
                    h * .32f
                )

                cubicTo(
                    w * .90f,
                    h * .59f,
                    w * .57f,
                    h * .80f,
                    w * .50f,
                    h * .88f
                )

                close()
            }

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
                            1.8.dp.toPx()
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
        Modifier.size(18.dp)
    ) {
        val center =
            Offset(
                size.width / 2f,
                size.height / 2f
            )

        val outer =
            size.minDimension *
                .47f

        val inner =
            outer * .43f

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
                            1.7.dp.toPx()
                    )
            )
        }
    }
}
