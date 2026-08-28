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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/*
 * Original approved XMO NavBar geometry.
 */
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
    /*
     * Original interaction model.
     */
    var pos by
        remember {
            mutableFloatStateOf(
                selected.toFloat()
            )
        }

    var down by
        remember {
            mutableStateOf(
                false
            )
        }

    var velocity by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * External selection / Android Back.
     */
    LaunchedEffect(
        selected
    ) {
        if (
            !down
        ) {
            pos =
                selected.toFloat()
        }
    }

    /*
     * Original spring selector movement.
     */
    val x by
        animateFloatAsState(
            targetValue =
                pos,

            animationSpec =
                spring(
                    dampingRatio =
                        .72f,

                    stiffness =
                        900f
                ),

            label =
                "position"
        )

    /*
     * Original hold/drag expansion.
     *
     * 78 × 56
     * ->
     * 110 × 80
     */
    val grow by
        animateFloatAsState(
            targetValue =
                if (
                    down
                ) {
                    1f
                } else {
                    0f
                },

            animationSpec =
                spring(
                    dampingRatio =
                        .78f,

                    stiffness =
                        850f
                ),

            label =
                "grow"
        )

    /*
     * Original parent response.
     */
    val barScale by
        animateFloatAsState(
            targetValue =
                if (
                    down
                ) {
                    1.04f
                } else {
                    1f
                },

            animationSpec =
                spring(
                    dampingRatio =
                        .78f,

                    stiffness =
                        900f
                ),

            label =
                "bar"
        )

    /*
     * =========================================================
     * THEME-BASED NAVBAR MATERIAL
     *
     * No Haze.
     * No Miuix.
     * No blur.
     * No accent dependency.
     * =========================================================
     */

    val parentBackground =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .84f
                )

            XmoTheme.Dark ->
                Color(
                    0xFF1B1B1D
                ).copy(
                    alpha = .88f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .90f
                )
        }

    val parentBorder =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .10f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .13f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .17f
                )
        }

    val highlight =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .66f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .10f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .08f
                )
        }

    /*
     * Selector itself is neutral/theme-based.
     */
    val selector =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .08f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .10f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .12f
                )
        }

    val selectorBorder =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .15f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .17f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .20f
                )
        }

    val selectorHighlight =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .38f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .11f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .09f
                )
        }

    val inactive =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .50f
                )

            else ->
                Color.White.copy(
                    alpha = .44f
                )
        }

    val active =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color(
                    0xFF161616
                )

            else ->
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

    /*
     * =========================================================
     * ORIGINAL 96dp OVERFLOW / GESTURE HOST
     * =========================================================
     */

    Box(
        Modifier
            .align(
                Alignment.BottomCenter
            )
            .navigationBarsPadding()
            .padding(
                bottom =
                    35.dp
            )
            .size(
                BarW,
                96.dp
            )
            .pointerInput(
                selected
            ) {
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

                    down =
                        true

                    velocity =
                        0f

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

                            /*
                             * Original direct finger-follow.
                             */
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
                                abs(
                                    total
                                ) >
                                2f
                            ) {
                                change.consume()
                            }
                        }
                    }

                    /*
                     * Original tap / drag resolution.
                     */
                    val target =
                        if (
                            abs(
                                total
                            ) <=
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

                    velocity =
                        0f

                    pos =
                        target.toFloat()

                    if (
                        target !=
                        selected
                    ) {
                        select(
                            target
                        )
                    }

                    down =
                        false
                }
            }
    ) {
        /*
         * =====================================================
         * PARENT — ORIGINAL 246 × 64
         * =====================================================
         */

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

                    clip =
                        true
                }
                .background(
                    parentBackground
                )
                .drawBehind {
                    /*
                     * Lightweight surface depth.
                     *
                     * This is only a gradient draw and does not
                     * sample/capture content behind the NavBar.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        highlight,
                                        Color.Transparent,
                                        Color.Transparent
                                    ),

                                startY =
                                    0f,

                                endY =
                                    size.height *
                                        .72f
                            ),

                        cornerRadius =
                            CornerRadius(
                                33.dp.toPx()
                            )
                    )
                }
                .border(
                    width =
                        .65.dp,

                    color =
                        parentBorder,

                    shape =
                        RoundedCornerShape(
                            33.dp
                        )
                )
        )

        /*
         * =====================================================
         * ORIGINAL SELECTOR GEOMETRY
         * =====================================================
         */

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

        /*
         * Original velocity stretch.
         */
        val stretch =
            if (
                down
            ) {
                (
                    abs(
                        velocity
                    ) /
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
                    /*
                     * Original selector deformation.
                     */
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

                    clip =
                        true
                }
                .background(
                    selector
                )
                .drawBehind {
                    /*
                     * Clearer top edge gives the neutral pill
                     * separation without accent coloring.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        selectorHighlight,
                                        Color.Transparent,
                                        Color.Transparent
                                    ),

                                endY =
                                    size.height *
                                        .68f
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius.toPx()
                            )
                    )
                }
                .border(
                    width =
                        .65.dp,

                    color =
                        selectorBorder,

                    shape =
                        RoundedCornerShape(
                            radius
                        )
                )
        )

        /*
         * =====================================================
         * ORIGINAL ICON ROW
         * =====================================================
         */

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
                    abs(
                        x -
                            index
                    ) <
                        .5f

                val scale by
                    animateFloatAsState(
                        targetValue =
                            if (
                                chosen &&
                                down
                            ) {
                                1.10f
                            } else {
                                1f
                            },

                        animationSpec =
                            spring(
                                dampingRatio =
                                    .75f,

                                stiffness =
                                    900f
                            ),

                        label =
                            "icon$index"
                    )

                Box(
                    Modifier
                        .weight(
                            1f
                        )
                        .fillMaxHeight(),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            icon,

                        contentDescription =
                            when (
                                index
                            ) {
                                0 ->
                                    "Home"

                                1 ->
                                    "Search"

                                else ->
                                    "Settings"
                            },

                        tint =
                            if (
                                chosen
                            ) {
                                active
                            } else {
                                inactive
                            },

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
