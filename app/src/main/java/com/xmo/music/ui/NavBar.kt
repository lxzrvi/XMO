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

private val W = 222.dp
private val H = 64.dp
private val SW = 74.dp
private val SH = 54.dp

@Composable
fun BoxScope.NavBar(selected: Int, select: (Int) -> Unit) {
    var pos by remember { mutableFloatStateOf(selected.toFloat()) }
    var pressed by remember { mutableStateOf(false) }
    var held by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        if (!pressed) pos = selected.toFloat()
    }

    val x by animateFloatAsState(
        pos, spring(.76f, 1000f), label = "x"
    )
    val selectorScale by animateFloatAsState(
        when {
            held -> 1.24f
            pressed -> 1.08f
            else -> 1f
        },
        spring(.66f, 800f),
        label = "selector"
    )
    val parentScale by animateFloatAsState(
        when {
            held -> 1.045f
            pressed -> 1.018f
            else -> 1f
        },
        spring(.72f, 900f),
        label = "parent"
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
            .padding(bottom = 32.dp)
            .size(W, H)
            .graphicsLayer {
                scaleX = parentScale
                scaleY = parentScale
            }
            .pointerInput(selected) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val start = selected
                    val startX = down.position.x
                    val startTime = down.uptimeMillis

                    var dx = 0f
                    var lastTime = startTime
                    var change = down

                    pressed = true
                    held = false

                    while (change.pressed) {
                        val event = awaitPointerEvent()
                        change = event.changes.first()
                        lastTime = change.uptimeMillis
                        dx = change.position.x - startX

                        held = lastTime - startTime >= 170 &&
                            abs(dx) < 12f

                        if (abs(dx) > 3f) {
                            val slot = size.width / 3f

                            pos = (
                                start + dx / slot
                            ).coerceIn(
                                (start - 1).coerceAtLeast(0).toFloat(),
                                (start + 1).coerceAtMost(2).toFloat()
                            )

                            change.consume()
                        }
                    }

                    val slot = size.width / 3f

                    val target = when {
                        abs(dx) > slot * .18f ->
                            if (dx > 0)
                                (start + 1).coerceAtMost(2)
                            else
                                (start - 1).coerceAtLeast(0)

                        abs(dx) <= 6f ->
                            (down.position.x / slot)
                                .toInt()
                                .coerceIn(0, 2)

                        else -> start
                    }

                    pressed = false
                    held = false
                    pos = target.toFloat()

                    if (target != selected) select(target)
                }
            }
    ) {
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer {
                    shape = RoundedCornerShape(32.dp)
                    clip = true
                }
                .background(Color(0x9A302B30))
                .border(
                    .8.dp,
                    Color(0x34FFFFFF),
                    RoundedCornerShape(32.dp)
                )
        )

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = ((W - SW) / 2f) * x)
                .size(SW, SH)
                .graphicsLayer {
                    scaleX = selectorScale
                    scaleY = selectorScale
                    shape = RoundedCornerShape(24.dp)
                    clip = true
                }
                .background(Color(0x48FFFFFF))
                .border(
                    .8.dp,
                    Color(0x50FFFFFF),
                    RoundedCornerShape(24.dp)
                )
        )

        Row(Modifier.fillMaxSize()) {
            icons.forEachIndexed { index, icon ->
                val active = abs(x - index) < .48f

                val iconScale by animateFloatAsState(
                    when {
                        active && held -> 1.14f
                        active && pressed -> 1.07f
                        else -> 1f
                    },
                    spring(.7f, 950f),
                    label = "i$index"
                )

                Box(
                    Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (active)
                            Color.White
                        else Color(0xFF887D82),
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
