package com.xmo.music.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val BarW = 246.dp
private val BarH = 64.dp

private val RestW = 78.dp
private val RestH = 56.dp

private val GrowX = 12.dp
private val GrowY = 12.dp
private val Travel = 164.dp // HTML: 246 - 8 (pad) - 74 = 164dp

@Composable
fun BoxScope.NavBar(
    selected: Int,
    select: (Int) -> Unit
) {
    var pos by remember { mutableFloatStateOf(selected.toFloat()) }
    var down by remember { mutableStateOf(false) }
    var velocity by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(selected) {
        if (!down) {
            pos = selected.toFloat()
        }
    }

    val settle by animateFloatAsState(
        targetValue = pos,
        animationSpec = spring(dampingRatio = 0.68f, stiffness = 750f),
        label = "selectorPosition"
    )

    val grow by animateFloatAsState(
        targetValue = if (down) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 700f),
        label = "selectorGrow"
    )

    val barScale by animateFloatAsState(
        targetValue = if (down) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 750f),
        label = "barScale"
    )

    val icons = listOf(
        Icons.Rounded.Home,
        Icons.Rounded.Search,
        Icons.Rounded.Settings
    )

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 52.dp)
            .size(BarW, 96.dp)
            .pointerInput(selected) {
                awaitEachGesture {
                    val first = awaitFirstDown(requireUnconsumed = false)
                    val startX = first.position.x
                    val start = selected
                    val slot = size.width / 3f

                    var lastX = startX
                    var total = 0f
                    var change = first

                    down = true
                    velocity = 0f

                    while (change.pressed) {
                        val event = awaitPointerEvent()
                        change = event.changes.first()

                        if (change.pressed) {
                            val currentX = change.position.x
                            val dx = currentX - lastX
                            total = currentX - startX
                            lastX = currentX

                            velocity = velocity * 0.62f + dx * 0.38f
                            pos = (start + total / slot).coerceIn(0f, 2f)

                            if (abs(total) > 2f) {
                                change.consume()
                            }
                        }
                    }

                    val target = when {
                        abs(total) <= 7f -> (first.position.x / slot).toInt().coerceIn(0, 2)
                        total > slot * 0.17f -> (start + 1).coerceAtMost(2)
                        total < -slot * 0.17f -> (start - 1).coerceAtLeast(0)
                        else -> start
                    }

                    down = false
                    velocity = 0f
                    pos = target.toFloat()

                    if (target != selected) {
                        select(target)
                    }
                }
            }
    ) {

        /*
         * MAIN BAR CONTAINER (Centered 246x64)
         */
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .graphicsLayer {
                    scaleX = barScale
                    scaleY = barScale
                    clip = false
                }
        ) {
            // HTML Glass Dock Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(33.dp))
                    .background(Color(0x24FFFFFF)) // HTML rgba(255, 255, 255, 0.14)
                    .border(
                        width = 0.5.dp,
                        color = Color(0x45FFFFFF), // HTML #ffffff45
                        shape = RoundedCornerShape(33.dp)
                    )
            )

            /*
             * LIQUID SELECTOR PILL (Directly inside 64dp Container)
             * Rest: 78x56 (top 4px, left 4px)
             * Hold: 102x80 (top -8px, left -8px)
             */
            val selectorW = RestW + 24.dp * grow
            val selectorH = RestH + 24.dp * grow

            // Left offset matching HTML: 4.px - GrowX(12dp) * grow
            val selectorX = 4.dp + Travel * (settle / 2f) - (GrowX * grow)

            // Top offset matching HTML: (64-56)/2 = 4dp rest, -8dp when expanded
            val selectorY = 4.dp - (GrowY * grow)

            val stretch = if (down) (abs(velocity) / 19f).coerceIn(0f, 1f) else 0f
            val liquidScaleX = 1f + stretch * 0.20f
            val liquidScaleY = 1f - stretch * 0.09f
            val skew = (velocity * 0.32f).coerceIn(-6f, 6f)
            val rotation = (velocity * 0.09f).coerceIn(-1.7f, 1.7f)

            Box(
                modifier = Modifier
                    .offset(x = selectorX, y = selectorY)
                    .size(width = selectorW, height = selectorH)
                    .graphicsLayer {
                        scaleX = liquidScaleX
                        scaleY = liquidScaleY
                        rotationZ = rotation
                        rotationY = skew * 0.20f
                        cameraDistance = 16f * density
                        clip = false
                    }
                    .clip(RoundedCornerShape(if (down) 42.dp else 29.dp))
                    .background(if (down) Color(0x08FFFFFF) else Color(0x1AFFFFFF))
                    .border(
                        width = 0.55.dp,
                        color = if (down) Color(0x52FFFFFF) else Color(0x45FFFFFF),
                        shape = RoundedCornerShape(if (down) 42.dp else 29.dp)
                    )
            )

            /*
             * ICON ROW LAYER
             * Positioned exactly over the bar slots: 78dp per item
             */
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icons.forEachIndexed { index, icon ->
                    val active = abs(settle - index) < 0.5f

                    val iconScale by animateFloatAsState(
                        targetValue = if (active && down) 1.10f else 1f,
                        animationSpec = spring(dampingRatio = 0.70f, stiffness = 850f),
                        label = "iconScale$index"
                    )

                    Box(
                        modifier = Modifier
                            .width(78.dp)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (active) Color.White else Color(0x3DFFFFFF), // HTML #ffffff3d
                            modifier = Modifier
                                .size(25.dp)
                                .graphicsLayer(
                                    scaleX = iconScale,
                                    scaleY = iconScale
                                )
                        )
                    }
                }
            }
        }
    }
}
