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
import com.xmo.music.XmoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

private val BarW = 246.dp
private val BarH = 64.dp
private val RestW = 78.dp
private val RestH = 56.dp

@Composable
fun BoxScope.NavBar(
    selected: Int,
    theme: XmoTheme,
    select: (Int) -> Unit
) {
    var pos by remember {
        mutableFloatStateOf(
            selected.toFloat()
        )
    }

    var down by remember {
        mutableStateOf(false)
    }

    var velocity by remember {
        mutableFloatStateOf(0f)
    }

    LaunchedEffect(selected) {
        if (!down) {
            pos = selected.toFloat()
        }
    }

    val x by animateFloatAsState(
        pos,
        spring(.72f, 900f),
        label = "position"
    )

    val grow by animateFloatAsState(
        if (down) 1f else 0f,
        spring(.78f, 850f),
        label = "grow"
    )

    val barScale by animateFloatAsState(
        if (down) 1.04f else 1f,
        spring(.78f, 900f),
        label = "bar"
    )

    val parent = when (theme) {
        XmoTheme.Dark ->
            Color(0xB5262628)

        XmoTheme.Amoled ->
            Color(0xC20A0A0A)

        XmoTheme.Light ->
            Color(0xE8FFFFFF)
    }

    val parentBorder =
        when (theme) {
            XmoTheme.Dark ->
                XmoRed.copy(.30f)

            XmoTheme.Amoled ->
                XmoRed.copy(.34f)

            XmoTheme.Light ->
                XmoRed.copy(.28f)
        }

    val inactive =
        when (theme) {
            XmoTheme.Light ->
                Color(0x80000000)

            else ->
                Color(0x70FFFFFF)
        }

    val active =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF161616)

            else ->
                Color.White
        }

    val icons = listOf(
        Icons.Rounded.Home,
        Icons.Rounded.Search,
        Icons.Rounded.Settings
    )

    Box(
        Modifier
            .align(
                Alignment.BottomCenter
            )
            .navigationBarsPadding()
            .padding(bottom = 35.dp)
            .size(BarW, 96.dp)
            .pointerInput(selected) {
                awaitEachGesture {
                    val first =
                        awaitFirstDown()

                    val startX =
                        first.position.x

                    val start =
                        selected

                    val slot =
                        size.width / 3f

                    var lastX =
                        startX

                    var total =
                        0f

                    var change =
                        first

                    down = true
                    velocity = 0f

                    while (
                        change.pressed
                    ) {
                        val event =
                            awaitPointerEvent()

                        change =
                            event.changes.first()

                        if (
                            change.pressed
                        ) {
                            val dx =
                                change
                                    .position.x -
                                    lastX

                            total =
                                change
                                    .position.x -
                                    startX

                            lastX =
                                change.position.x

                            velocity =
                                velocity * .62f +
                                    dx * .38f

                            pos = (
                                start +
                                    total / slot
                                ).coerceIn(
                                0f,
                                2f
                            )

                            if (
                                abs(total) >
                                2f
                            ) {
                                change.consume()
                            }
                        }
                    }

                    val target =
                        if (
                            abs(total) <=
                            7f
                        ) {
                            (
                                first.position.x /
                                    slot
                                )
                                .toInt()
                                .coerceIn(
                                    0,
                                    2
                                )
                        } else {
                            pos
                                .roundToInt()
                                .coerceIn(
                                    0,
                                    2
                                )
                        }

                    velocity = 0f
                    pos =
                        target.toFloat()

                    if (
                        target != selected
                    ) {
                        select(target)
                    }

                    down = false
                }
            }
    ) {
        Box(
            Modifier
                .align(
                    Alignment.Center
                )
                .size(
                    BarW,
                    BarH
                )
                .graphicsLayer {
                    scaleX =
                        barScale

                    scaleY =
                        barScale

                    shape =
                        RoundedCornerShape(
                            33.dp
                        )

                    clip = true
                }
                .background(parent)
                .border(
                    .6.dp,
                    parentBorder,
                    RoundedCornerShape(
                        33.dp
                    )
                )
        )

        val selectorW =
            RestW +
                32.dp * grow

        val selectorH =
            RestH +
                24.dp * grow

        val radius =
            29.dp +
                13.dp * grow

        val stretch =
            if (down) {
                (
                    abs(velocity) /
                        19f
                    ).coerceIn(
                    0f,
                    1f
                )
            } else 0f

        val selector =
            when (theme) {
                XmoTheme.Dark ->
                    XmoRed.copy(.26f)

                XmoTheme.Amoled ->
                    XmoRed.copy(.29f)

                XmoTheme.Light ->
                    XmoRed.copy(.24f)
            }

        Box(
            Modifier
                .align(
                    Alignment.CenterStart
                )
                .offset(
                    x =
                        4.dp +
                            160.dp *
                            (x / 2f) -
                            16.dp *
                            grow
                )
                .size(
                    selectorW,
                    selectorH
                )
                .graphicsLayer {
                    val skew =
                        (
                            velocity *
                                .32f
                            ).coerceIn(
                            -6f,
                            6f
                        )

                    scaleX =
                        1f +
                            stretch *
                            .20f

                    scaleY =
                        1f -
                            stretch *
                            .09f

                    rotationZ =
                        (
                            velocity *
                                .09f
                            ).coerceIn(
                            -1.7f,
                            1.7f
                        )

                    cameraDistance =
                        16f * density

                    rotationY =
                        skew * .20f
                }
                .graphicsLayer {
                    shape =
                        RoundedCornerShape(
                            radius
                        )

                    clip = true
                }
                .background(selector)
                .border(
                    .65.dp,
                    XmoRed.copy(.42f),
                    RoundedCornerShape(
                        radius
                    )
                )
        )

        Row(
            Modifier
                .align(
                    Alignment.Center
                )
                .size(
                    BarW,
                    BarH
                )
                .padding(
                    horizontal =
                        4.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            icons.forEachIndexed {
                    index,
                    icon ->

                val chosen =
                    abs(x - index) <
                        .5f

                val scale by
                    animateFloatAsState(
                        if (
                            chosen &&
                            down
                        ) 1.10f
                        else 1f,
                        spring(
                            .75f,
                            900f
                        ),
                        label =
                            "icon$index"
                    )

                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        icon,
                        null,
                        tint =
                            if (chosen)
                                active
                            else
                                inactive,
                        modifier =
                            Modifier
                                .offset(
                                    x =
                                        if (
                                            index ==
                                            2
                                        )
                                            2.dp
                                        else
                                            0.dp
                                )
                                .size(
                                    25.dp
                                )
                                .graphicsLayer {
                                    scaleX =
                                        scale

                                    scaleY =
                                        scale
                                }
                    )
                }
            }
        }
    }
}
