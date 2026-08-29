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
        modifier = modifier
    ) {
        val barWidth =
            size.width *
                .19f

        val barHeight =
            size.height *
                .56f

        val top =
            size.height *
                .22f

        val radius =
            barWidth *
                .46f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    x =
                        size.width *
                            .28f,
                    y =
                        top
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
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
                    x =
                        size.width *
                            .53f,
                    y =
                        top
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
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
            w *
                .115f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    x =
                        w * .20f,
                    y =
                        h * .25f
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
                        h * .50f
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
        modifier = modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val barWidth =
            w *
                .115f

        drawRoundRect(
            color = color,
            topLeft =
                Offset(
                    x =
                        w * .685f,
                    y =
                        h * .25f
                ),
            size =
                Size(
                    width =
                        barWidth,
                    height =
                        h * .50f
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
            path = path,
            color = color
        )
    }
}

@Composable
internal fun XmoShuffleIcon(
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

        val stroke =
            w *
                .095f

        val style =
            Stroke(
                width = stroke,
                cap = StrokeCap.Round
            )

        /*
         * Upper crossing route.
         */
        val upper =
            Path().apply {
                moveTo(
                    w * .16f,
                    h * .30f
                )

                cubicTo(
                    w * .29f,
                    h * .30f,
                    w * .34f,
                    h * .32f,
                    w * .42f,
                    h * .43f
                )

                cubicTo(
                    w * .53f,
                    h * .59f,
                    w * .60f,
                    h * .70f,
                    w * .76f,
                    h * .70f
                )
            }

        drawPath(
            path = upper,
            color = color,
            style = style
        )

        /*
         * Lower crossing route.
         */
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
                    w * .76f,
                    h * .30f
                )
            }

        drawPath(
            path = lower,
            color = color,
            style = style
        )

        /*
         * Soft filled arrow heads.
         */
        val topArrow =
            Path().apply {
                moveTo(
                    w * .72f,
                    h * .19f
                )

                cubicTo(
                    w * .72f,
                    h * .16f,
                    w * .76f,
                    h * .15f,
                    w * .79f,
                    h * .18f
                )

                lineTo(
                    w * .90f,
                    h * .27f
                )

                cubicTo(
                    w * .93f,
                    h * .30f,
                    w * .93f,
                    h * .33f,
                    w * .90f,
                    h * .36f
                )

                lineTo(
                    w * .79f,
                    h * .45f
                )

                cubicTo(
                    w * .76f,
                    h * .48f,
                    w * .72f,
                    h * .46f,
                    w * .72f,
                    h * .42f
                )

                close()
            }

        drawPath(
            path =
                topArrow,
            color =
                color
        )

        val bottomArrow =
            Path().apply {
                moveTo(
                    w * .72f,
                    h * .59f
                )

                cubicTo(
                    w * .72f,
                    h * .55f,
                    w * .76f,
                    h * .53f,
                    w * .79f,
                    h * .56f
                )

                lineTo(
                    w * .90f,
                    h * .66f
                )

                cubicTo(
                    w * .93f,
                    h * .69f,
                    w * .93f,
                    h * .72f,
                    w * .90f,
                    h * .75f
                )

                lineTo(
                    w * .79f,
                    h * .84f
                )

                cubicTo(
                    w * .76f,
                    h * .87f,
                    w * .72f,
                    h * .85f,
                    w * .72f,
                    h * .81f
                )

                close()
            }

        drawPath(
            path =
                bottomArrow,
            color =
                color
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
        modifier = modifier
    ) {
        val w =
            size.width

        val h =
            size.height

        val stroke =
            w *
                .085f

        val style =
            Stroke(
                width = stroke,
                cap = StrokeCap.Round
            )

        /*
         * Top route.
         */
        val top =
            Path().apply {
                moveTo(
                    w * .18f,
                    h * .39f
                )

                cubicTo(
                    w * .18f,
                    h * .29f,
                    w * .26f,
                    h * .25f,
                    w * .38f,
                    h * .25f
                )

                lineTo(
                    w * .76f,
                    h * .25f
                )
            }

        drawPath(
            path = top,
            color = color,
            style = style
        )

        /*
         * Bottom route.
         */
        val bottom =
            Path().apply {
                moveTo(
                    w * .82f,
                    h * .61f
                )

                cubicTo(
                    w * .82f,
                    h * .71f,
                    w * .74f,
                    h * .75f,
                    w * .62f,
                    h * .75f
                )

                lineTo(
                    w * .24f,
                    h * .75f
                )
            }

        drawPath(
            path = bottom,
            color = color,
            style = style
        )

        /*
         * Matching rounded arrow heads.
         */
        val rightArrow =
            Path().apply {
                moveTo(
                    w * .72f,
                    h * .13f
                )

                cubicTo(
                    w * .72f,
                    h * .09f,
                    w * .77f,
                    h * .08f,
                    w * .80f,
                    h * .11f
                )

                lineTo(
                    w * .91f,
                    h * .21f
                )

                cubicTo(
                    w * .94f,
                    h * .24f,
                    w * .94f,
                    h * .27f,
                    w * .91f,
                    h * .30f
                )

                lineTo(
                    w * .80f,
                    h * .40f
                )

                cubicTo(
                    w * .77f,
                    h * .43f,
                    w * .72f,
                    h * .41f,
                    w * .72f,
                    h * .37f
                )

                close()
            }

        drawPath(
            path = rightArrow,
            color = color
        )

        val leftArrow =
            Path().apply {
                moveTo(
                    w * .28f,
                    h * .63f
                )

                cubicTo(
                    w * .28f,
                    h * .59f,
                    w * .23f,
                    h * .57f,
                    w * .20f,
                    h * .60f
                )

                lineTo(
                    w * .09f,
                    h * .70f
                )

                cubicTo(
                    w * .06f,
                    h * .73f,
                    w * .06f,
                    h * .76f,
                    w * .09f,
                    h * .79f
                )

                lineTo(
                    w * .20f,
                    h * .89f
                )

                cubicTo(
                    w * .23f,
                    h * .92f,
                    w * .28f,
                    h * .91f,
                    w * .28f,
                    h * .87f
                )

                close()
            }

        drawPath(
            path = leftArrow,
            color = color
        )

        if (repeatOne) {
            /*
             * Filled soft "1". It remains inside the same repeat
             * family instead of switching to a different icon.
             */
            drawRoundRect(
                color = color,
                topLeft =
                    Offset(
                        x =
                            w * .49f,
                        y =
                            h * .40f
                    ),
                size =
                    Size(
                        width =
                            w * .075f,
                        height =
                            h * .22f
                    ),
                cornerRadius =
                    CornerRadius(
                        w * .04f,
                        w * .04f
                    )
            )

            drawLine(
                color = color,
                start =
                    Offset(
                        x =
                            w * .45f,
                        y =
                            h * .45f
                    ),
                end =
                    Offset(
                        x =
                            w * .52f,
                        y =
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
