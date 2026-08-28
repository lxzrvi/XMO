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
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import kotlin.math.abs
import kotlin.math.roundToInt

private val BarWidth = 246.dp
private val BarHeight = 64.dp
private val RestWidth = 78.dp
private val RestHeight = 56.dp

/*
 * backdrop comes from App's rememberLayerBackdrop().
 *
 * Approved XMO geometry is preserved.
 */
@Composable
fun BoxScope.NavBar(
    selected: Int,
    theme: XmoTheme,
    backdrop: LayerBackdrop,
    select: (Int) -> Unit
) {
    val accent =
        LocalXmoAccent.current

    var position by
        remember {
            mutableFloatStateOf(
                selected.toFloat()
            )
        }

    var pressed by
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

    LaunchedEffect(
        selected
    ) {
        if (
            !pressed
        ) {
            position =
                selected
                    .coerceIn(
                        0,
                        2
                    )
                    .toFloat()
        }
    }

    val x by
        animateFloatAsState(
            targetValue =
                position,

            animationSpec =
                spring(
                    dampingRatio = .72f,
                    stiffness = 900f
                ),

            label =
                "navPosition"
        )

    val grow by
        animateFloatAsState(
            targetValue =
                if (
                    pressed
                ) {
                    1f
                } else {
                    0f
                },

            animationSpec =
                spring(
                    dampingRatio = .76f,
                    stiffness = 820f
                ),

            label =
                "navGrow"
        )

    val parentScale by
        animateFloatAsState(
            targetValue =
                if (
                    pressed
                ) {
                    1.035f
                } else {
                    1f
                },

            animationSpec =
                spring(
                    dampingRatio = .78f,
                    stiffness = 900f
                ),

            label =
                "navScale"
        )

    val icons =
        remember {
            listOf(
                Icons.Rounded.Home,
                Icons.Rounded.Search,
                Icons.Rounded.Settings
            )
        }

    val textColor =
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

    val inactive =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .46f
                )

            else ->
                Color.White.copy(
                    alpha = .42f
                )
        }

    val liquidTint =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .20f
                )

            XmoTheme.Dark ->
                Color(
                    0xFF111318
                ).copy(
                    alpha = .23f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .31f
                )
        }

    val edge =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .72f
                )

            else ->
                Color.White.copy(
                    alpha = .17f
                )
        }

    /*
     * =========================================================
     * APPROVED 96dp OVERFLOW / GESTURE HOST
     * =========================================================
     */

    Box(
        Modifier
            .align(
                Alignment.BottomCenter
            )
            .navigationBarsPadding()
            .padding(
                bottom = 35.dp
            )
            .size(
                BarWidth,
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
                            .coerceIn(
                                0,
                                2
                            )

                    val slot =
                        size.width /
                            3f

                    var lastX =
                        startX

                    var total =
                        0f

                    var change =
                        first

                    pressed =
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
                            val current =
                                change.position.x

                            val dx =
                                current -
                                    lastX

                            total =
                                current -
                                    startX

                            lastX =
                                current

                            velocity =
                                velocity *
                                    .64f +
                                    dx *
                                    .36f

                            position =
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
                            position
                                .roundToInt()
                                .coerceIn(
                                    0,
                                    2
                                )
                        }

                    position =
                        target.toFloat()

                    velocity =
                        0f

                    pressed =
                        false

                    if (
                        target !=
                        selected
                    ) {
                        select(
                            target
                        )
                    }
                }
            }
    ) {
        /*
         * =====================================================
         * MIUIX REAL LIQUID BACKDROP
         * =====================================================
         */

        Box(
            Modifier
                .align(
                    Alignment.Center
                )
                .size(
                    BarWidth,
                    BarHeight
                )
                .graphicsLayer {
                    scaleX =
                        parentScale

                    scaleY =
                        parentScale
                }
                .drawBackdrop(
                    backdrop =
                        backdrop,

                    shape = {
                        RoundedCornerShape(
                            33.dp
                        )
                    },

                    effects = {
                        blur(
                            22.dp
                        )
                    }
                )
                /*
                 * Transparent color material above the real
                 * captured backdrop.
                 */
                .background(
                    liquidTint,
                    RoundedCornerShape(
                        33.dp
                    )
                )
                .drawBehind {
                    val radius =
                        33.dp.toPx()

                    /*
                     * Liquid highlight concentrated along top.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(
                                            alpha =
                                                if (
                                                    theme ==
                                                    XmoTheme.Light
                                                ) {
                                                    .54f
                                                } else {
                                                    .15f
                                                }
                                        ),

                                        Color.White.copy(
                                            alpha = .035f
                                        ),

                                        Color.Transparent
                                    )
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
                            )
                    )

                    /*
                     * Soft lateral light.
                     */
                    drawRoundRect(
                        brush =
                            Brush.horizontalGradient(
                                colors =
                                    listOf(
                                        Color.White.copy(
                                            alpha = .08f
                                        ),
                                        Color.Transparent,
                                        accent.copy(
                                            alpha = .055f
                                        )
                                    )
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
                            )
                    )
                }
                .border(
                    .65.dp,
                    accent.copy(
                        alpha = .30f
                    ),
                    RoundedCornerShape(
                        33.dp
                    )
                )
                .border(
                    .35.dp,
                    edge,
                    RoundedCornerShape(
                        33.dp
                    )
                )
        )

        /*
         * =====================================================
         * LIQUID SELECTOR
         * =====================================================
         */

        val selectorWidth =
            RestWidth +
                32.dp *
                grow

        val selectorHeight =
            RestHeight +
                24.dp *
                grow

        val radius =
            29.dp +
                13.dp *
                grow

        val stretch =
            if (
                pressed
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
                    selectorWidth,
                    selectorHeight
                )
                .graphicsLayer {
                    scaleX =
                        1f +
                            stretch *
                            .20f

                    scaleY =
                        1f -
                            stretch *
                            .08f

                    rotationZ =
                        (
                            velocity *
                                .085f
                            )
                            .coerceIn(
                                -1.6f,
                                1.6f
                            )

                    cameraDistance =
                        density *
                            16f

                    rotationY =
                        (
                            velocity *
                                .055f
                            )
                            .coerceIn(
                                -1.5f,
                                1.5f
                            )
                }
                .clip(
                    RoundedCornerShape(
                        radius
                    )
                )
                /*
                 * Selector samples the backdrop too. It produces
                 * the thicker refractive-glass region instead of
                 * a flat red blob.
                 */
                .drawBackdrop(
                    backdrop =
                        backdrop,

                    shape = {
                        RoundedCornerShape(
                            radius
                        )
                    },

                    effects = {
                        blur(
                            15.dp
                        )
                    }
                )
                .background(
                    accent.copy(
                        alpha =
                            when (
                                theme
                            ) {
                                XmoTheme.Light ->
                                    .18f

                                XmoTheme.Dark ->
                                    .22f

                                XmoTheme.Amoled ->
                                    .25f
                            }
                    )
                )
                .drawBehind {
                    val corner =
                        radius.toPx()

                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(
                                        alpha = .18f
                                    ),
                                    Color.Transparent,
                                    accent.copy(
                                        alpha = .06f
                                    )
                                )
                            ),

                        cornerRadius =
                            CornerRadius(
                                corner
                            )
                    )
                }
                .border(
                    .7.dp,
                    accent.copy(
                        alpha = .48f
                    ),
                    RoundedCornerShape(
                        radius
                    )
                )
        )

        /*
         * =====================================================
         * ICONS
         * =====================================================
         */

        Row(
            Modifier
                .align(
                    Alignment.Center
                )
                .size(
                    BarWidth,
                    BarHeight
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

                val iconScale by
                    animateFloatAsState(
                        targetValue =
                            if (
                                chosen &&
                                pressed
                            ) {
                                1.10f
                            } else {
                                1f
                            },

                        animationSpec =
                            spring(
                                dampingRatio =
                                    .74f,

                                stiffness =
                                    900f
                            ),

                        label =
                            "liquidIcon$index"
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
                                textColor
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
                                        iconScale

                                    scaleY =
                                        iconScale
                                }
                    )
                }
            }
        }
    }
}
