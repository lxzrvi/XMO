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
import com.xmo.music.ui.blur.glassBorder
import com.xmo.music.ui.blur.glassHighlight
import com.xmo.music.ui.blur.liveBlur
import dev.chrisbanes.haze.HazeState
import kotlin.math.abs
import kotlin.math.roundToInt

/*
 * Approved XMO geometry.
 */
private val BarW =
    246.dp

private val BarH =
    64.dp

private val RestW =
    78.dp

private val RestH =
    56.dp

@Composable
fun BoxScope.NavBar(
    selected: Int,
    theme: XmoTheme,
    hazeState: HazeState,
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

    /*
     * Keep selector synchronized with programmatic/back navigation.
     */
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

    val animatedPosition by
        animateFloatAsState(
            targetValue =
                position,

            animationSpec =
                spring(
                    dampingRatio =
                        .72f,

                    stiffness =
                        900f
                ),

            label =
                "xmoNavPosition"
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
                    dampingRatio =
                        .78f,

                    stiffness =
                        850f
                ),

            label =
                "xmoNavGrow"
        )

    val parentScale by
        animateFloatAsState(
            targetValue =
                if (
                    pressed
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
                "xmoNavParent"
        )

    val parentBorder =
        accent.copy(
            alpha =
                when (
                    theme
                ) {
                    XmoTheme.Light ->
                        .28f

                    XmoTheme.Dark ->
                        .30f

                    XmoTheme.Amoled ->
                        .35f
                }
        )

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
     * 96dp INVISIBLE OVERFLOW / GESTURE HOST
     *
     * Approved positioning is intentionally retained.
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
                            !change.pressed
                        ) {
                            continue
                        }

                        val currentX =
                            change.position.x

                        val dx =
                            currentX -
                                lastX

                        total =
                            currentX -
                                startX

                        lastX =
                            currentX

                        /*
                         * Low-pass velocity gives selector stretch
                         * without noisy single-frame spikes.
                         */
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

                    /*
                     * Settle selector first; selected tab updates
                     * immediately through App.
                     */
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
         * PARENT GLASS — 246 × 64
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
                        parentScale

                    scaleY =
                        parentScale

                    shape =
                        RoundedCornerShape(
                            33.dp
                        )

                    clip =
                        true
                }
                .liveBlur(
                    state =
                        hazeState,

                    theme =
                        theme
                )
                .drawBehind {
                    val radius =
                        33.dp.toPx()

                    /*
                     * Subtle top reflection.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        glassHighlight(
                                            theme
                                        ),
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
                                radius
                            )
                    )

                    /*
                     * Neutral glass edge.
                     */
                    drawRoundRect(
                        color =
                            glassBorder(
                                theme
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
                            ),

                        style =
                            androidx.compose.ui.graphics
                                .drawscope
                                .Stroke(
                                    width =
                                        .35.dp.toPx()
                                )
                    )
                }
                .border(
                    width =
                        .6.dp,

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
         * MOVING SELECTOR
         *
         * Rest     78 × 56
         * Expanded 110 × 80
         * =====================================================
         */
        val selectorWidth =
            RestW +
                32.dp *
                grow

        val selectorHeight =
            RestH +
                24.dp *
                grow

        val selectorRadius =
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

        val selectorColor =
            accent.copy(
                alpha =
                    when (
                        theme
                    ) {
                        XmoTheme.Light ->
                            .24f

                        XmoTheme.Dark ->
                            .26f

                        XmoTheme.Amoled ->
                            .30f
                    }
            )

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
                                animatedPosition /
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
                            selectorRadius
                        )

                    clip =
                        true
                }
                .background(
                    selectorColor
                )
                .border(
                    .65.dp,
                    accent.copy(
                        alpha = .44f
                    ),
                    RoundedCornerShape(
                        selectorRadius
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

                val selectedVisual =
                    abs(
                        animatedPosition -
                            index
                    ) <
                        .5f

                val scale by
                    animateFloatAsState(
                        targetValue =
                            if (
                                selectedVisual &&
                                pressed
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
                            "xmoNavIcon$index"
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
                                selectedVisual
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
