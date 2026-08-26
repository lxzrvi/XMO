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
            .size(BarW, 104.dp)
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

        /*
         * Pressed parent after 1.04 scale:
         * one tab width 82dp x height 66.56dp.
         *
         * Selector = 106 x 90.56
         *
         * Therefore selector extends exactly:
         * 12dp left
         * 12dp right
         * 12dp top
         * 12dp bottom
         */
        val pressedW = 106.dp
        val pressedH = 90.56.dp

        val selectorW = RestW + (pressedW - RestW) * grow
        val selectorH = RestH + (pressedH - RestH) * grow

        // Resting centers:
        // Home 43, Search 123, Settings 203
        val centerTravel = 160.dp

        val stretch =
            if (down)
                (abs(velocity) / 30f).coerceIn(0f, 1f)
            else 0f

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(
                    x = 4.dp +
                        centerTravel * (x / 2f) -
                        ((pressedW - RestW) / 2f) * grow
                )
                .size(selectorW, selectorH)
                .graphicsLayer {
                    // Very subtle liquid deformation.
                    scaleX = 1f + stretch * .035f
                    scaleY = 1f - stretch * .015f

                    rotationZ =
                        (velocity * .04f)
                            .coerceIn(-1f, 1f)
                }
                .graphicsLayer {
                    shape = RoundedCornerShape(46.dp)
                    clip = true
                }
                .background(Color(0x12FFFFFF))
                .border(
                    .55.dp,
                    if (down)
                        Color(0x52FFFFFF)
                    else Color(0x45FFFFFF),
                    RoundedCornerShape(46.dp)
                )
        )

        Row(
            Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .padding(horizontal = 4.dp)
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
