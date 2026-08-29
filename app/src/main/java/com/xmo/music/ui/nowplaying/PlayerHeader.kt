package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.EllipsisVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Share2
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

    Box(
        Modifier
            .fillMaxWidth()
            .height(68.dp)
            .padding(
                horizontal = 14.dp
            )
            .headerDownGesture(
                y = playerY,
                height =
                    screenHeight,
                dismiss =
                    dismissAfterDrag
            )
    ) {
        PremiumCircle(
            size = 40.dp,
            background =
                foreground.copy(
                    alpha = .10f
                ),
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
            Icon(
                imageVector =
                    Lucide.ChevronDown,
                contentDescription =
                    "Close",
                tint = foreground,
                modifier =
                    Modifier.size(22.dp)
            )
        }

        Column(
            Modifier
                .align(
                    Alignment.Center
                )
                .width(180.dp),
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
                    foreground.copy(
                        alpha = .68f
                    ),
                fontFamily =
                    XmoFont.medium,
                fontSize = 12.sp,
                textAlign =
                    TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = source,
                color = foreground,
                fontFamily =
                    XmoFont.bold,
                fontSize = 16.sp,
                textAlign =
                    TextAlign.Center,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        XmoCapsule(
            background =
                foreground.copy(
                    alpha = .10f
                ),
            modifier =
                Modifier.align(
                    Alignment.CenterEnd
                )
        ) {
            CapsuleButton(
                size = 40.dp,
                onClick = share
            ) {
                Icon(
                    imageVector =
                        Lucide.Share2,
                    contentDescription =
                        "Share",
                    tint = foreground,
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 40.dp,
                onClick = options
            ) {
                Icon(
                    imageVector =
                        Lucide.EllipsisVertical,
                    contentDescription =
                        "Options",
                    tint = foreground,
                    modifier =
                        Modifier.size(19.dp)
                )
            }
        }
    }
}

private fun Modifier.headerDownGesture(
    y: Animatable<Float, *>,
    height: Float,
    dismiss: () -> Unit
): Modifier =
    pointerInput(height) {
        coroutineScope {
            detectDragGestures(
                onDrag = { change, amount ->
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
                            height * .13f
                        ) {
                            y.animateTo(
                                targetValue =
                                    height,
                                animationSpec =
                                    tween(300)
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
                                tween(180)
                        )
                    }
                }
            )
        }
    }
