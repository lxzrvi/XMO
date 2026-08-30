package com.xmo.music.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var pos by
        remember {
            mutableFloatStateOf(
                selected.toFloat()
            )
        }

    var down by
        remember {
            mutableStateOf(false)
        }

    var velocity by
        remember {
            mutableFloatStateOf(0f)
        }

    LaunchedEffect(selected) {
        if (!down) {
            pos =
                selected.toFloat()
        }
    }

    val x by
        animateFloatAsState(
            targetValue = pos,
            animationSpec =
                spring(
                    dampingRatio = .72f,
                    stiffness = 900f
                ),
            label = "position"
        )

    val grow by
        animateFloatAsState(
            targetValue =
                if (down) {
                    1f
                } else {
                    0f
                },
            animationSpec =
                spring(
                    dampingRatio = .78f,
                    stiffness = 850f
                ),
            label = "grow"
        )

    val barScale by
        animateFloatAsState(
            targetValue =
                if (down) {
                    1.04f
                } else {
                    1f
                },
            animationSpec =
                spring(
                    dampingRatio = .78f,
                    stiffness = 900f
                ),
            label = "bar"
        )

    val parentBackground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF9F9FA)
                    .copy(alpha = .965f)

            XmoTheme.Dark ->
                Color(0xFF181819)
                    .copy(alpha = .965f)

            XmoTheme.Amoled ->
                Color(0xFF080808)
                    .copy(alpha = .975f)
        }

    val parentBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .085f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .10f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .13f
                )
        }

    val selector =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFEAEAEC)
                    .copy(alpha = .62f)

            XmoTheme.Dark ->
                Color(0xFF303031)
                    .copy(alpha = .58f)

            XmoTheme.Amoled ->
                Color(0xFF292929)
                    .copy(alpha = .54f)
        }

    val selectorBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .13f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .155f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .18f
                )
        }

    val inactive =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .46f
                )

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .42f
                )
        }

    val active =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF161616)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    val icons =
        remember {
            listOf(
                Icons.Rounded.Home,
                Icons.Rounded.Search,
                Icons.Rounded.Settings
            )
        }

    Box(
        modifier =
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .navigationBarsPadding()
                /*
                 * 96dp host contains centered 64dp bar:
                 * 16dp internal bottom area.
                 *
                 * 4 + 16 = 20dp visible bottom gap.
                 */
                .padding(
                    bottom = 4.dp
                )
                .size(
                    BarW,
                    96.dp
                )
                .pointerInput(selected) {
                    awaitEachGesture {
                        val first =
                            awaitFirstDown()

                        val startX =
                            first.position.x

                        val start =
                            selected

                        val slot =
                            size.width /
                                3f

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
                                event.changes
                                    .first()

                            if (
                                change.pressed
                            ) {
                                val dx =
                                    change.position.x -
                                        lastX

                                total =
                                    change.position.x -
                                        startX

                                lastX =
                                    change.position.x

                                velocity =
                                    velocity *
                                        .62f +
                                        dx *
                                        .38f

                                pos =
                                    (
                                        start +
                                            total /
                                            slot
                                        )
                                        .coerceIn(
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
            modifier =
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .size(
                        BarW,
                        BarH
                    )
                    .graphicsLayer {
                        scaleX = barScale
                        scaleY = barScale

                        shape =
                            RoundedCornerShape(
                                33.dp
                            )

                        clip = true
                    }
                    .background(
                        parentBackground
                    )
                    .border(
                        width = .65.dp,
                        color =
                            parentBorder,
                        shape =
                            RoundedCornerShape(
                                33.dp
                            )
                    )
        )

        val selectorW =
            RestW +
                32.dp *
                grow

        val selectorH =
            RestH +
                24.dp *
                grow

        val radius =
            29.dp +
                13.dp *
                grow

        val stretch =
            if (down) {
                (
                    abs(velocity) /
                        19f
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                0f
            }

        Box(
            modifier =
                Modifier
                    .align(
                        Alignment.CenterStart
                    )
                    .offset(
                        x =
                            4.dp +
                                160.dp *
                                (
                                    x /
                                        2f
                                    ) -
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
                                )
                                .coerceIn(
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
                                )
                                .coerceIn(
                                    -1.7f,
                                    1.7f
                                )

                        cameraDistance =
                            16f *
                                density

                        rotationY =
                            skew *
                                .20f
                    }
                    .graphicsLayer {
                        shape =
                            RoundedCornerShape(
                                radius
                            )

                        clip = true
                    }
                    .background(
                        selector
                    )
                    .border(
                        width = .65.dp,
                        color =
                            selectorBorder,
                        shape =
                            RoundedCornerShape(
                                radius
                            )
                    )
        )

        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.Center
                    )
                    .size(
                        BarW,
                        BarH
                    )
                    .padding(
                        horizontal = 4.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            icons.forEachIndexed {
                    index,
                    icon ->

                val proximity =
                    (
                        1f -
                            abs(
                                x -
                                    index.toFloat()
                            )
                        )
                        .coerceIn(
                            0f,
                            1f
                        )

                val iconScale =
                    1f +
                        if (down) {
                            .10f *
                                proximity
                        } else {
                            0f
                        }

                val tint =
                    mixNavColor(
                        from = inactive,
                        to = active,
                        fraction =
                            proximity
                    )

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            icon,
                        contentDescription =
                            when (index) {
                                0 -> "Home"
                                1 -> "Search"
                                else -> "Settings"
                            },
                        tint = tint,
                        modifier =
                            Modifier
                                .offset(
                                    x =
                                        if (
                                            index ==
                                            2
                                        ) {
                                            2.dp
                                        } else {
                                            0.dp
                                        }
                                )
                                .size(25.dp)
                                .graphicsLayer {
                                    scaleX =
                                        iconScale

                                    scaleY =
                                        iconScale

                                    alpha =
                                        .88f +
                                            .12f *
                                            proximity
                                }
                    )
                }
            }
        }
    }
}

private fun mixNavColor(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val value =
        fraction.coerceIn(
            0f,
            1f
        )

    return Color(
        red =
            from.red +
                (
                    to.red -
                        from.red
                    ) *
                value,
        green =
            from.green +
                (
                    to.green -
                        from.green
                    ) *
                value,
        blue =
            from.blue +
                (
                    to.blue -
                        from.blue
                    ) *
                value,
        alpha =
            from.alpha +
                (
                    to.alpha -
                        from.alpha
                    ) *
                value
    )
}
