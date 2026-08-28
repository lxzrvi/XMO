package com.xmo.music.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.AlignmentLine
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
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
import androidx.compose.ui.geometry.Offset
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

    /*
     * ONE App-level shared HazeState.
     *
     * NavBar does not create its own backdrop.
     */
    hazeState: HazeState,

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

    /*
     * External tab selection keeps selector in sync.
     */
    LaunchedEffect(
        selected
    ) {
        if (!down) {
            pos =
                selected.toFloat()
        }
    }

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

    val grow by
        animateFloatAsState(
            targetValue =
                if (down)
                    1f
                else
                    0f,

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

    val barScale by
        animateFloatAsState(
            targetValue =
                if (down)
                    1.04f
                else
                    1f,

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
     * ---------------------------------------------------------
     * GLASS COLORS
     * ---------------------------------------------------------
     *
     * Blur itself comes from LiveBlur.
     * These are cheap overlays only.
     */
    val parentBorder =
        when (theme) {
            XmoTheme.Dark ->
                XmoRed.copy(
                    alpha = .30f
                )

            XmoTheme.Amoled ->
                XmoRed.copy(
                    alpha = .34f
                )

            XmoTheme.Light ->
                XmoRed.copy(
                    alpha = .28f
                )
        }

    val glassEdge =
        glassBorder(
            theme
        )

    val highlight =
        glassHighlight(
            theme
        )

    val inactive =
        when (theme) {
            XmoTheme.Light ->
                Color(
                    0x80000000
                )

            else ->
                Color(
                    0x70FFFFFF
                )
        }

    val active =
        when (theme) {
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
     * =========================================================
     *
     * Approved geometry unchanged.
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
         * GLASS PARENT — 246 × 64
         * =====================================================
         *
         * ONLY this whole parent gets Haze blur.
         *
         * Selector/icons do NOT create another blur.
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

                /*
                 * Real shared Haze.
                 */
                .liveBlur(
                    state =
                        hazeState,

                    theme =
                        theme
                )

                /*
                 * Subtle upper glass reflection.
                 *
                 * Cheap gradient only.
                 */
                .drawBehind {
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
                            androidx.compose.ui.geometry
                                .CornerRadius(
                                    33.dp.toPx()
                                )
                    )

                    /*
                     * Very subtle neutral glass edge under
                     * the branded red border.
                     */
                    drawRoundRect(
                        color =
                            glassEdge,

                        cornerRadius =
                            androidx.compose.ui.geometry
                                .CornerRadius(
                                    33.dp.toPx()
                                ),

                        style =
                            androidx.compose.ui.graphics.drawscope
                                .Stroke(
                                    width =
                                        .35.dp.toPx()
                                )
                    )
                }

                /*
                 * Existing XMO-red parent border stays.
                 */
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
         * SELECTOR
         * =====================================================
         *
         * Geometry and interaction unchanged.
         *
         * It is intentionally NOT independently blurred.
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

        val stretch =
            if (down) {
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

        val selector =
            when (theme) {
                XmoTheme.Dark ->
                    XmoRed.copy(
                        alpha = .26f
                    )

                XmoTheme.Amoled ->
                    XmoRed.copy(
                        alpha = .29f
                    )

                XmoTheme.Light ->
                    XmoRed.copy(
                        alpha = .24f
                    )
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
                .border(
                    .65.dp,
                    XmoRed.copy(
                        alpha = .42f
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
                            null,

                        tint =
                            if (chosen) {
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
