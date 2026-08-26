package com.xmo.music.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private val Slot = 72.dp
private const val Slots = 3

@Composable
fun BoxScope.NavBar(selected: Int, select: (Int) -> Unit) {
    var drag by remember { mutableFloatStateOf(selected.toFloat()) }
    var held by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        if (!dragging) drag = selected.toFloat()
    }

    val position by animateFloatAsState(
        drag,
        spring(.72f, 900f),
        label = "position"
    )
    val scale by animateFloatAsState(
        if (held) 1.22f else 1f,
        spring(.62f, 650f),
        label = "scale"
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
            .padding(bottom = 18.dp)
            .width(Slot * Slots)
            .height(Slot)
            .pointerInput(Unit) {
                detectTapGestures { p ->
                    select(
                        (p.x / (size.width / Slots))
                            .toInt()
                            .coerceIn(0, 2)
                    )
                }
            }
            .pointerInput(selected) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        held = true
                        dragging = true
                        drag = selected.toFloat()
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        drag = (
                            drag + amount.x / (size.width / Slots)
                        ).coerceIn(0f, 2f)
                    },
                    onDragCancel = {
                        held = false
                        dragging = false
                        drag = selected.toFloat()
                    },
                    onDragEnd = {
                        val target = drag.roundToInt().coerceIn(0, 2)
                        held = false
                        dragging = false
                        drag = target.toFloat()
                        select(target)
                    }
                )
            }
    ) {
        // Parent glass capsule
        Box(
            Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(Color(0xB52B292D))
        )

        // Sliding capsule
        Box(
            Modifier
                .offset(x = Slot * position)
                .size(Slot)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(Color(0x3DFFFFFF))
                .border(1.dp, Color(0x28FFFFFF), CircleShape)
        )

        // Fixed icon slots
        Row(Modifier.fillMaxSize()) {
            icons.forEachIndexed { index, icon ->
                val active = (position - index).let { kotlin.math.abs(it) < .45f }

                Box(
                    Modifier.size(Slot),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = if (active)
                            Color.White
                        else
                            Color(0xFF8B858A),
                        modifier = Modifier.size(25.dp)
                    )
                }
            }
        }
    }
}
