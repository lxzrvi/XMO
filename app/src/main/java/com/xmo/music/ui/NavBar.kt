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

    val x by animateFloatAsState(
        pos, spring(.68f, 750f), label = "position"
    )
    val grow by animateFloatAsState(
        if (down) 1f else 0f,
        spring(.72f, 700f), label = "grow"
    )
    val barScale by animateFloatAsState(
        if (down) 1.04f else 1f,
        spring(.75f, 750f), label = "bar"
    )

    val icons = listOf(
        Icons.Rounded.Home,
        Icons.Rounded.Search,
        Icons.Rounded.Settings
    )

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
                            pos = (start + total / slot)
                                .coerceIn(0f, 2f)

                            if (abs(total) > 2f) change.consume()
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

                    if (target != selected) select(target)
                }
            }
    ) {
        // Parent
        Box(
            Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .graphicsLayer {
                    scaleX = barScale
                    scaleY = barScale
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

        // Rest 78x56
        // Press 110x80
        val selectorW = RestW + 32.dp * grow
        val selectorH = RestH + 24.dp * grow
        val travel = 160.dp

        val stretch = if (down)
            (abs(velocity) / 19f).coerceIn(0f, 1f)
        else 0f

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(
                    x = 4.dp +
                        travel * (x / 2f) -
                        16.dp * grow
                )
                .size(selectorW, selectorH)
                .graphicsLayer {
                    val skew =
                        (velocity * .32f).coerceIn(-6f, 6f)

                    scaleX = 1f + stretch * .20f
                    scaleY = 1f - stretch * .09f

                    rotationZ =
                        (velocity * .09f)
                            .coerceIn(-1.7f, 1.7f)

                    cameraDistance = 16f * density
                    rotationY = skew * .20f
                }
                .graphicsLayer {
                    shape = RoundedCornerShape(
                        if (down) 42.dp else 29.dp
                    )
                    clip = true
                }
                .background(
                    if (down)
                        Color(0x03FFFFFF)
                    else
                        Color(0x12FFFFFF)
                )
                .border(
                    .55.dp,
                    if (down)
                        Color(0x52FFFFFF)
                    else
                        Color(0x45FFFFFF),
                    RoundedCornerShape(
                        if (down) 42.dp else 29.dp
                    )
                )
        )

        // Exact three equal icon slots
        Row(
            Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icons.forEachIndexed { index, icon ->
                val active = abs(x - index) < .5f

                val iconScale by animateFloatAsState(
                    if (active && down) 1.10f else 1f,
                    spring(.7f, 850f),
                    label = "icon$index"
                )

                Box(
                    Modifier
                        .weight(1f)
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
