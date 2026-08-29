package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

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

        /*
         * Same visual vertical bounds as Pause: ~22%..78%.
         */
        val path =
            Path().apply {
                moveTo(
                    w * .32f,
                    h * .22f
                )

                cubicTo(
                    w * .32f,
                    h * .17f,
                    w * .38f,
                    h * .15f,
                    w * .43f,
                    h * .18f
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
                    h * .82f
                )

                cubicTo(
                    w * .38f,
                    h * .85f,
                    w * .32f,
                    h * .82f,
                    w * .32f,
                    h * .78f
                )

                close()
            }

        drawPath(
            path = path,
            color = color
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
            size.width * .20f

        val barHeight =
            size.height * .56f

        val top =
            size.height * .22f

        val radius =
            barWidth * .43f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    size.width * .27f,
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
                    size.width * .53f,
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
        modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val bar =
            w * .12f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    w * .20f,
                    h * .24f
                ),
            size =
                Size(
                    bar,
                    h * .52f
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
                    w * .70f,
                    h * .24f
                )

                cubicTo(
                    w * .74f,
                    h * .21f,
                    w * .78f,
                    h * .24f,
                    w * .78f,
                    h * .30f
                )

                lineTo(
                    w * .78f,
                    h * .70f
                )

                cubicTo(
                    w * .78f,
                    h * .76f,
                    w * .74f,
                    h * .79f,
                    w * .70f,
                    h * .76f
                )

                lineTo(
                    w * .37f,
                    h * .55f
                )

                cubicTo(
                    w * .30f,
                    h * .51f,
                    w * .30f,
                    h * .49f,
                    w * .37f,
                    h * .45f
                )

                close()
            }

        drawPath(
            path = path,
            color = color
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
            w * .12f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    w * .68f,
                    h * .24f
                ),
            size =
                Size(
                    bar,
                    h * .52f
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
                    w * .30f,
                    h * .24f
                )

                cubicTo(
                    w * .26f,
                    h * .21f,
                    w * .22f,
                    h * .24f,
                    w * .22f,
                    h * .30f
                )

                lineTo(
                    w * .22f,
                    h * .70f
                )

                cubicTo(
                    w * .22f,
                    h * .76f,
                    w * .26f,
                    h * .79f,
                    w * .30f,
                    h * .76f
                )

                lineTo(
                    w * .63f,
                    h * .55f
                )

                cubicTo(
                    w * .70f,
                    h * .51f,
                    w * .70f,
                    h * .49f,
                    w * .63f,
                    h * .45f
                )

                close()
            }

        drawPath(
            path = path,
            color = color
        )
    }
}
