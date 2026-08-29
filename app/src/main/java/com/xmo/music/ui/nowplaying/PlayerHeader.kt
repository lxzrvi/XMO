package com.xmo.music.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
internal fun PlayerHeader(
    source: String,
    sourceIsCategory: Boolean,
    foreground: Color,
    playerY: Animatable<Float, *>,
    screenHeight: Float,
    close: suspend () -> Unit,
    dismissAfterDrag: () -> Unit,
    share: () -> Unit,
    options: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val animatedForeground by
        animateColorAsState(
            targetValue =
                foreground,
            animationSpec =
                tween(
                    durationMillis =
                        480
                ),
            label =
                "headerForeground"
        )

    val surface =
        animatedForeground.copy(
            alpha = .10f
        )

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    68.dp
                )
                .padding(
                    horizontal =
                        14.dp
                )
                .headerDownGesture(
                    y =
                        playerY,
                    height =
                        screenHeight,
                    dismiss =
                        dismissAfterDrag
                )
    ) {
        PremiumCircle(
            size =
                40.dp,
            background =
                surface,
            modifier =
                Modifier.align(
                    Alignment.CenterStart
                ),
            onClick = {
                scope.launch {
                    close()
                }
            }
        ) {
            HeaderDownIcon(
                color =
                    animatedForeground,
                modifier =
                    Modifier.size(
                        22.dp
                    )
            )
        }

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .width(
                        180.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    if (
                        sourceIsCategory
                    ) {
                        "PLAYING FROM CATEGORY"
                    } else {
                        "PLAYING FROM"
                    },
                color =
                    animatedForeground.copy(
                        alpha = .70f
                    ),
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    12.sp,
                textAlign =
                    TextAlign.Center,
                maxLines =
                    1
            )

            Text(
                text =
                    source,
                color =
                    animatedForeground,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    16.sp,
                textAlign =
                    TextAlign.Center,
                maxLines =
                    1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        XmoCapsule(
            background =
                surface,
            modifier =
                Modifier.align(
                    Alignment.CenterEnd
                )
        ) {
            CapsuleButton(
                size =
                    40.dp,
                onClick =
                    share
            ) {
                HeaderShareIcon(
                    color =
                        animatedForeground,
                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )
            }

            CapsuleButton(
                size =
                    40.dp,
                onClick =
                    options
            ) {
                HeaderMoreIcon(
                    color =
                        animatedForeground,
                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun HeaderDownIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val stroke =
            size.minDimension *
                .105f

        val path =
            Path().apply {
                moveTo(
                    size.width *
                        .23f,
                    size.height *
                        .39f
                )

                cubicTo(
                    size.width *
                        .25f,
                    size.height *
                        .36f,
                    size.width *
                        .29f,
                    size.height *
                        .36f,
                    size.width *
                        .32f,
                    size.height *
                        .39f
                )

                lineTo(
                    size.width *
                        .50f,
                    size.height *
                        .57f
                )

                lineTo(
                    size.width *
                        .68f,
                    size.height *
                        .39f
                )

                cubicTo(
                    size.width *
                        .71f,
                    size.height *
                        .36f,
                    size.width *
                        .75f,
                    size.height *
                        .36f,
                    size.width *
                        .77f,
                    size.height *
                        .39f
                )
            }

        drawPath(
            path =
                path,
            color =
                color,
            style =
                Stroke(
                    width =
                        stroke,
                    cap =
                        StrokeCap.Round
                )
        )
    }
}

@Composable
private fun HeaderShareIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val stroke =
            size.minDimension *
                .095f

        val radius =
            size.minDimension *
                .105f

        val left =
            Offset(
                size.width *
                    .27f,
                size.height *
                    .50f
            )

        val top =
            Offset(
                size.width *
                    .70f,
                size.height *
                    .27f
            )

        val bottom =
            Offset(
                size.width *
                    .70f,
                size.height *
                    .73f
            )

        drawLine(
            color =
                color,
            start =
                left,
            end =
                top,
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )

        drawLine(
            color =
                color,
            start =
                left,
            end =
                bottom,
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )

        drawCircle(
            color =
                color,
            radius =
                radius,
            center =
                left
        )

        drawCircle(
            color =
                color,
            radius =
                radius,
            center =
                top
        )

        drawCircle(
            color =
                color,
            radius =
                radius,
            center =
                bottom
        )
    }
}

@Composable
private fun HeaderMoreIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val radius =
            size.minDimension *
                .105f

        drawCircle(
            color =
                color,
            radius =
                radius,
            center =
                Offset(
                    size.width *
                        .24f,
                    size.height *
                        .50f
                )
        )

        drawCircle(
            color =
                color,
            radius =
                radius,
            center =
                Offset(
                    size.width *
                        .50f,
                    size.height *
                        .50f
                )
        )

        drawCircle(
            color =
                color,
            radius =
                radius,
            center =
                Offset(
                    size.width *
                        .76f,
                    size.height *
                        .50f
                )
        )
    }
}

private fun Modifier.headerDownGesture(
    y: Animatable<Float, *>,
    height: Float,
    dismiss: () -> Unit
): Modifier =
    pointerInput(
        height
    ) {
        coroutineScope {
            detectDragGestures(
                onDrag = {
                        change,
                        amount ->

                    if (
                        amount.y > 0f ||
                        y.value > 0f
                    ) {
                        change.consume()

                        launch {
                            y.snapTo(
                                (
                                    y.value +
                                        amount.y
                                    )
                                    .coerceIn(
                                        0f,
                                        height
                                    )
                            )
                        }
                    }
                },

                onDragEnd = {
                    launch {
                        if (
                            y.value >
                            height *
                            .13f
                        ) {
                            y.animateTo(
                                targetValue =
                                    height,
                                animationSpec =
                                    tween(
                                        durationMillis =
                                            300
                                    )
                            )

                            dismiss()
                        } else {
                            y.animateTo(
                                targetValue =
                                    0f,
                                animationSpec =
                                    spring(
                                        dampingRatio =
                                            .84f,
                                        stiffness =
                                            390f
                                    )
                            )
                        }
                    }
                },

                onDragCancel = {
                    launch {
                        y.animateTo(
                            targetValue =
                                0f,
                            animationSpec =
                                tween(
                                    durationMillis =
                                        180
                                )
                        )
                    }
                }
            )
        }
    }
