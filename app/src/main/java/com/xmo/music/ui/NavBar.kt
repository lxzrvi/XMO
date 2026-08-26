package com.xmo.music.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun BoxScope.NavBar(selected: Int, select: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    val slot = 68f
    val x = remember { Animatable(selected * slot) }
    val zoom = remember { Animatable(1f) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        if (!dragging) x.animateTo(selected * slot, spring(1f, 650f))
    }

    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
            .width(220.dp)
            .height(68.dp)
            .clip(CircleShape)
            .background(Color(0xB8222222))
            .pointerInput(selected) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val start = x.value
                    val startX = down.position.x
                    var held = false

                    val hold = scope.launch {
                        delay(150)
                        held = true
                        zoom.animateTo(1.18f, spring(0.7f, 700f))
                    }

                    var change: PointerInputChange? = null
                    do {
                        change = awaitPointerEvent().changes.firstOrNull()
                        if (change != null && change.pressed) {
                            val dx = change.position.x - startX

                            if (kotlin.math.abs(dx) > 8f) {
                                dragging = true
                                hold.cancel()
                                if (!held) zoom.animateTo(1.08f)
                                x.snapTo((start + dx / density).coerceIn(0f, slot * 2))
                                change.consume()
                            }
                        }
                    } while (change?.pressed == true)

                    hold.cancel()

                    val target = if (dragging)
                        (x.value / slot).roundToInt().coerceIn(0, 2)
                    else
                        ((down.position.x / size.width) * 3)
                            .toInt().coerceIn(0, 2)

                    x.animateTo(target * slot, spring(0.65f, 750f))
                    zoom.animateTo(1f, spring(0.55f, 700f))
                    dragging = false
                    if (target != selected) select(target)
                }
            }
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf(Icons.Rounded.Home, Icons.Rounded.Search, Icons.Rounded.Settings).forEach {
                Box(Modifier.size(68.dp), contentAlignment = Alignment.Center) {
                    Icon(it, null, tint = Color(0xFFCCCCCC), modifier = Modifier.size(23.dp))
                }
            }
        }

        Box(
            Modifier
                .offset { IntOffset((8.dp.toPx() + x.value.dp.toPx()).roundToInt(), 0) }
                .align(Alignment.CenterStart)
                .size(52.dp)
                .graphicsLayer {
                    scaleX = zoom.value
                    scaleY = zoom.value
                }
                .clip(CircleShape)
                .background(Color(0x32FFFFFF))
        )
    }
}
