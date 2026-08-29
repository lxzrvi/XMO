package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp

@Composable
internal fun XmoPlayIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val path =
            Path().apply {
                moveTo(
                    w * .31f,
                    h * .19f
                )

                cubicTo(
                    w * .31f,
                    h * .13f,
                    w * .38f,
                    h * .10f,
                    w * .44f,
                    h * .14f
                )

                lineTo(
                    w * .80f,
                    h * .42f
                )

                cubicTo(
                    w * .87f,
                    h * .47f,
                    w * .87f,
                    h * .55f,
                    w * .80f,
                    h * .60f
                )

                lineTo(
                    w * .44f,
                    h * .87f
                )

                cubicTo(
                    w * .38f,
                    h * .92f,
                    w * .31f,
                    h * .88f,
                    w * .31f,
                    h * .81f
                )

                close()
            }

        drawPath(
            path = path,
            color = color,
            style = Fill
        )
    }
}

@Composable
internal fun XmoPauseIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val barWidth =
            size.width * .21f

        val barHeight =
            size.height * .62f

        val top =
            size.height *
                .19f

        val radius =
            barWidth *
                .42f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .25f,
                    top
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
            color = color,
            topLeft =
                Offset(
                    size.width * .54f,
                    top
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
        modifier = modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val barWidth =
            w * .12f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    w * .20f,
                    h * .22f
                ),
            size =
                Size(
                    barWidth,
                    h * .56f
                ),
            cornerRadius =
                CornerRadius(
                    barWidth / 2f,
                    barWidth / 2f
                )
        )

        val first =
            Path().apply {
                moveTo(
                    w * .67f,
                    h * .23f
                )

                cubicTo(
                    w * .72f,
                    h * .19f,
                    w * .77f,
                    h * .23f,
                    w * .77f,
                    h * .30f
                )

                lineTo(
                    w * .77f,
                    h * .70f
                )

                cubicTo(
                    w * .77f,
                    h * .77f,
                    w * .72f,
                    h * .81f,
                    w * .67f,
                    h * .77f
                )

                lineTo(
                    w * .36f,
                    h * .56f
                )

                cubicTo(
                    w * .28f,
                    h * .51f,
                    w * .28f,
                    h * .47f,
                    w * .36f,
                    h * .42f
                )

                close()
            }

        drawPath(
            path = first,
            color = color,
            style = Fill
        )
    }
}

@Composable
internal fun XmoNextIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val barWidth =
            w * .12f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    w * .68f,
                    h * .22f
                ),
            size =
                Size(
                    barWidth,
                    h * .56f
                ),
            cornerRadius =
                CornerRadius(
                    barWidth / 2f,
                    barWidth / 2f
                )
        )

        val path =
            Path().apply {
                moveTo(
                    w * .33f,
                    h * .23f
                )

                cubicTo(
                    w * .28f,
                    h * .19f,
                    w * .23f,
                    h * .23f,
                    w * .23f,
                    h * .30f
                )

                lineTo(
                    w * .23f,
                    h * .70f
                )

                cubicTo(
                    w * .23f,
                    h * .77f,
                    w * .28f,
                    h * .81f,
                    w * .33f,
                    h * .77f
                )

                lineTo(
                    w * .64f,
                    h * .56f
                )

                cubicTo(
                    w * .72f,
                    h * .51f,
                    w * .72f,
                    h * .47f,
                    w * .64f,
                    h * .42f
                )

                close()
            }

        drawPath(
            path = path,
            color = color,
            style = Fill
        )
    }
}
