package com.xmo.music.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val BarW = 246.dp
private val BarH = 64.dp
private val RestW = 78.dp
private val RestH = 56.dp

@Composable
fun BoxScope.NavBar(selected: Int, select: (Int) -> Unit) {
    var pos by remember { mutableFloatStateOf(selected.toFloat()) }
    var down by remember { mutableStateOf(false) }
    var velocity by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(selected) {
        if (!down) pos = selected.toFloat()
    }

    val settle by animateFloatAsState(
        pos, spring(.68f, 750f), label = "position"
    )

    val grow by animateFloatAsState(
        if (down) 1f else 0f,
        spring(.72f, 700f), label = "grow"
    )

    val parentGrow by animateFloatAsState(
        if (down) 1.04f else 1f,
        spring(.75f, 750f), label = "bar"
    )

    val icons = listOf(
        Icons.Rounded.Home,
        Icons.Rounded.Search,
        Icons.Rounded.Settings
    )

    // 96dp outer area allows selector to escape vertically.
    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 52.dp)
            .size(BarW, 96.dp)
            .pointerInput(selected) {
                awaitEachGesture {
                    val first = awaitFirstDown()
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
                            val dx = change.position.x - lastX
                            total = change.position.x - startX
                            lastX = change.position.x

                            velocity = velocity * .62f + dx * .38f

                            pos = (
                                start + total / slot
                            ).coerceIn(0f, 2f)

                            if (abs(total) > 2f)
                                change.consume()
                        }
                    }

                    val target = when {
                        abs(total) <= 7f ->
                            (first.position.x / slot)
                                .toInt()
                                .coerceIn(0, 2)

                        total > slot * .17f ->
                            (start + 1).coerceAtMost(2)

                        total < -slot * .17f ->
                            (start - 1).coerceAtLeast(0)

                        else -> start
                    }

                    down = false
                    velocity = 0f
                    pos = target.toFloat()

                    if (target != selected)
                        select(target)
                }
            }
    ) {
        // Actual 246 × 64 parent stays centered.
        Box(
            Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .graphicsLayer {
                    scaleX = parentGrow
                    scaleY = parentGrow
                    shape = RoundedCornerShape(33.dp)
                    clip = true
                }
                .background(Color(0x2EFFFFFF))
                .border(
                    .5.dp,
                    Color(0x45FFFFFF),
                    RoundedCornerShape(33.dp)
                )
        )

        val activeW = RestW + 24.dp * grow
        val activeH = RestH + 36.dp * grow
        val travel = 160.dp

        val stretch =
            if (down)
                (abs(velocity) / 19f).coerceIn(0f, 1f)
            else 0f

        // Selector now has real vertical space to overflow.
        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(
                    x = 4.dp +
                        travel * (settle / 2f) -
                        12.dp * grow
                )
                .size(activeW, activeH)
                .graphicsLayer {
                    val direction =
                        (velocity * .32f).coerceIn(-6f, 6f)

                    scaleX = 1f + stretch * .20f
                    scaleY = 1f - stretch * .09f

                    rotationZ =
                        (velocity * .09f)
                            .coerceIn(-1.7f, 1.7f)

                    cameraDistance = 16f * density
                    rotationY = direction * .20f
                }
                .graphicsLayer {
                    shape = RoundedCornerShape(
                        if (down) 46.dp else 29.dp
                    )
                    clip = true
                }
                .background(Color(0x12FFFFFF))
                .border(
                    .55.dp,
                    if (down)
                        Color(0x52FFFFFF)
                    else Color(0x45FFFFFF),
                    RoundedCornerShape(
                        if (down) 46.dp else 29.dp
                    )
                )
        )

        // Icons remain aligned to the actual 64dp parent.
        Row(
            Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .padding(horizontal = 4.dp)
        ) {
            icons.forEachIndexed { index, icon ->
                val active = abs(settle - index) < .5f

                val iconScale by animateFloatAsState(
                    if (active && down) 1.10f else 1f,
                    spring(.7f, 850f),
                    label = "icon$index"
                )

                Box(
                    Modifier
                        .width(78.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (active)
                            Color.White
                        else Color(0x62FFFFFF),
                        modifier = Modifier
                            .size(25.dp)
                            .graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                    )
                }
            }
        }
    }
}
