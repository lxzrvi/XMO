package com.xmo.music.ui

import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.dp
import kotlin.math.abs

private val BarW = 246.dp
private val BarH = 64.dp

private val RestW = 78.dp
private val RestH = 56.dp

private val Grow = 12.dp
private val Travel = 160.dp

@Composable
fun BoxScope.NavBar(
    selected: Int,
    select: (Int) -> Unit
) {
    var pos by remember {
        mutableFloatStateOf(selected.toFloat())
    }

    var down by remember {
        mutableStateOf(false)
    }

    var velocity by remember {
        mutableFloatStateOf(0f)
    }

    /*
     * Keep position synchronized with external selection.
     */
    LaunchedEffect(selected) {
        if (!down) {
            pos = selected.toFloat()
        }
    }

    /*
     * Smooth selector position after release.
     */
    val settle by animateFloatAsState(
        targetValue = pos,
        animationSpec = spring(
            dampingRatio = 0.68f,
            stiffness = 750f
        ),
        label = "selectorPosition"
    )

    /*
     * 0 = 78x56
     * 1 = 102x80
     */
    val grow by animateFloatAsState(
        targetValue = if (down) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = 700f
        ),
        label = "selectorGrow"
    )

    /*
     * HTML:
     *
     * .bar.hold {
     *     transform: scale(1.04);
     * }
     */
    val barScale by animateFloatAsState(
        targetValue = if (down) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = 750f
        ),
        label = "barScale"
    )

    val icons = listOf(
        Icons.Rounded.Home,
        Icons.Rounded.Search,
        Icons.Rounded.Settings
    )

    /*
     * Large invisible host.
     *
     * 246 x 96 gives the selector enough room to overflow
     * above and below the 64dp visual bar.
     *
     * IMPORTANT:
     * No clip() here.
     */
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = 52.dp)
            .size(BarW, 96.dp)
            .pointerInput(selected) {

                awaitEachGesture {

                    val first = awaitFirstDown(
                        requireUnconsumed = false
                    )

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

                            /*
                             * Same idea as HTML:
                             *
                             * sv = sv * .62 + d * .38
                             */
                            velocity =
                                velocity * 0.62f +
                                    dx * 0.38f

                            /*
                             * Convert pixel movement into
                             * 0..2 selector position.
                             */
                            pos = (
                                start +
                                    total / slot
                                ).coerceIn(
                                    0f,
                                    2f
                                )

                            if (abs(total) > 2f) {
                                change.consume()
                            }
                        }
                    }

                    /*
                     * Tap:
                     * choose icon directly under finger.
                     *
                     * Drag:
                     * move one slot only when movement
                     * crosses approximately 17%.
                     */
                    val target = when {

                        abs(total) <= 7f -> {
                            (
                                first.position.x / slot
                            )
                                .toInt()
                                .coerceIn(0, 2)
                        }

                        total > slot * 0.17f -> {
                            (start + 1)
                                .coerceAtMost(2)
                        }

                        total < -slot * 0.17f -> {
                            (start - 1)
                                .coerceAtLeast(0)
                        }

                        else -> {
                            start
                        }
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
         * ============================================================
         * VISUAL BAR CONTAINER
         * ============================================================
         *
         * This is the important structural fix.
         *
         * In the HTML version:
         *
         * .bar
         *   ├── .drop
         *   └── .item
         *
         * Therefore when .bar scales to 1.04,
         * the selector scales with it too.
         *
         * Here we reproduce the same hierarchy.
         */
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(BarW, BarH)
                .graphicsLayer {
                    scaleX = barScale
                    scaleY = barScale

                    /*
                     * IMPORTANT:
                     * Do NOT clip.
                     *
                     * The 102x80 selector must overflow
                     * the 246x64 bar.
                     */
                    clip = false
                }
        ) {

            /*
             * ========================================================
             * MAIN GLASS BAR
             * ========================================================
             */
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(
                        RoundedCornerShape(33.dp)
                    )
                    .background(
                        Color(0x2EFFFFFF)
                    )
                    .border(
                        width = 0.5.dp,
                        color = Color(0x45FFFFFF),
                        shape = RoundedCornerShape(33.dp)
                    )
            )

            /*
             * ========================================================
             * LIQUID SELECTOR
             * ========================================================
             *
             * HTML:
             *
             * normal:
             * left: 4px
             * top: 4px
             * width: 78px
             * height: 56px
             *
             * hold:
             * width: 102px
             * height: 80px
             * top: -8px
             *
             * Because this box is centered vertically inside
             * a 64dp parent:
             *
             * normal:
             * (64 - 56) / 2 = 4dp
             *
             * hold:
             * (64 - 80) / 2 = -8dp
             *
             * EXACT HTML behavior.
             */
            val selectorW =
                RestW + 24.dp * grow

            val selectorH =
                RestH + 24.dp * grow

            /*
             * x position:
             *
             * normal:
             * 4dp + travel * position
             *
             * hold:
             * -12dp extra
             *
             * This keeps the selector centered while
             * growing 78 -> 102.
             */
            val selectorX =
                4.dp +
                    Travel * (settle / 2f) -
                    Grow * grow

            /*
             * HTML velocity stretch:
             *
             * f = min(abs(sv)/19, 1)
             */
            val stretch =
                if (down) {
                    (
                        abs(velocity) / 19f
                    ).coerceIn(
                        0f,
                        1f
                    )
                } else {
                    0f
                }

            val scaleX =
                1f + stretch * 0.20f

            val scaleY =
                1f - stretch * 0.09f

            val skew =
                (
                    velocity * 0.32f
                ).coerceIn(
                    -6f,
                    6f
                )

            val rotation =
                (
                    velocity * 0.09f
                ).coerceIn(
                    -1.7f,
                    1.7f
                )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)

                    /*
                     * This gives:
                     *
                     * normal:  x = 4dp
                     * hold:    x = -8dp
                     *
                     * while the selector grows
                     * from 78 to 102.
                     */
                    .offset(
                        x = selectorX
                    )

                    .size(
                        width = selectorW,
                        height = selectorH
                    )

                    .graphicsLayer {

                        /*
                         * Liquid stretch.
                         */
                        scaleX =
                            1f +
                                stretch * 0.20f

                        scaleY =
                            1f -
                                stretch * 0.09f

                        /*
                         * Subtle directional rotation.
                         */
                        rotationZ =
                            rotation

                        /*
                         * Horizontal perspective/wobble.
                         */
                        rotationY =
                            skew * 0.20f

                        cameraDistance =
                            16f * density

                        /*
                         * No clipping.
                         *
                         * The selector must be able
                         * to extend outside the bar.
                         */
                        clip = false
                    }

                    /*
                     * Selector itself is clipped to its
                     * rounded glass shape.
                     */
                    .clip(
                        RoundedCornerShape(
                            if (down) {
                                42.dp
                            } else {
                                29.dp
                            }
                        )
                    )

                    .background(
                        if (down) {
                            Color(0x03FFFFFF)
                        } else {
                            Color(0x12FFFFFF)
                        }
                    )

                    .border(
                        width = 0.55.dp,
                        color = if (down) {
                            Color(0x52FFFFFF)
                        } else {
                            Color(0x45FFFFFF)
                        },
                        shape = RoundedCornerShape(
                            if (down) {
                                42.dp
                            } else {
                                29.dp
                            }
                        )
                    )
            )

            /*
             * ========================================================
             * ICON ROW
             * ========================================================
             *
             * This stays exactly 246 x 64.
             */
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 4.dp
                    )
            ) {

                icons.forEachIndexed { index, icon ->

                    val active =
                        abs(settle - index) < 0.5f

                    val iconScale by animateFloatAsState(
                        targetValue =
                            if (active && down) {
                                1.10f
                            } else {
                                1f
                            },
                        animationSpec = spring(
                            dampingRatio = 0.70f,
                            stiffness = 850f
                        ),
                        label = "iconScale$index"
                    )

                    Box(
                        modifier = Modifier
                            .width(78.dp)
                            .fillMaxHeight(),
                        contentAlignment =
                            Alignment.Center
                    ) {

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint =
                                if (active) {
                                    Color.White
                                } else {
                                    Color(0x62FFFFFF)
                                },
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
}
