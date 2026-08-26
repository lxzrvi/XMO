package com.xmo.music.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val SLOT = 68f

@Composable
fun BoxScope.NavBar(selected: Int, select: (Int) -> Unit) {
    var drag by remember { mutableFloatStateOf(selected * SLOT) }
    var held by remember { mutableStateOf(false) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(selected) {
        if (!dragging) drag = selected * SLOT
    }

    val x by animateDpAsState(
        drag.dp,
        spring(dampingRatio = .72f, stiffness = 850f),
        label = "x"
    )
    val scale by animateFloatAsState(
        if (held) 1.20f else 1f,
        spring(dampingRatio = .65f, stiffness = 700f),
        label = "scale"
    )

    Box(
        Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
            .size(220.dp, 68.dp)
            .clip(CircleShape)
            .background(Color(0xB81B1B1B))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { p ->
                        select(
                            (p.x / (size.width / 3f))
                                .toInt()
                                .coerceIn(0, 2)
                        )
                    }
                )
            }
            .pointerInput(selected) {
                detectDragGesturesAfterLongPress(
                    onDragStart = {
                        held = true
                        dragging = true
                        drag = selected * SLOT
                    },
                    onDrag = { change, amount ->
                        change.consume()
                        drag = (drag + amount.x / density)
                            .coerceIn(0f, SLOT * 2)
                    },
                    onDragCancel = {
                        held = false
                        dragging = false
                        drag = selected * SLOT
                    },
                    onDragEnd = {
                        val target = (drag / SLOT)
                            .roundToInt()
                            .coerceIn(0, 2)

                        held = false
                        dragging = false
                        drag = target * SLOT
                        select(target)
                    }
                )
            }
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                Icons.Rounded.Home,
                Icons.Rounded.Search,
                Icons.Rounded.Settings
            ).forEach { icon ->
                Box(
                    Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint = Color(0xFFD5D5D5),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.CenterStart)
                .offset(x = 8.dp + x)
                .size(52.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(Color(0x35FFFFFF))
        )
    }
}
