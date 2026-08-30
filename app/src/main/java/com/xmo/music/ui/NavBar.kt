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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/*
 * =============================================================
 * APPROVED XMO NAVBAR GEOMETRY
 * =============================================================
 *
 * Do not alter these values when tuning visual styling.
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

    /*
     * External selection / Android Back.
     */
    LaunchedEffect(selected) {
        if (!down) {
            pos =
                selected.toFloat()
        }
    }

    /*
     * At rest, the original spring remains.
     *
     * While dragging, using raw pos avoids placing a spring
     * between the user's finger and selector on every pointer
     * frame.
     */
    val settledX by
        animateFloatAsState(
            targetValue = pos,
            animationSpec =
                spring(
                    dampingRatio = .76f,
                    stiffness = 1050f
                ),
            label =
                "navPosition"
        )

    val x =
        if (down) {
            pos
        } else {
            settledX
        }

    /*
     * Original hold/drag expansion.
     */
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
                    dampingRatio = .80f,
                    stiffness = 920f
                ),
            label =
                "navGrow"
        )

    /*
     * Original parent response.
     */
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
                    dampingRatio = .80f,
                    stiffness = 980f
                ),
            label =
                "navBarScale"
        )

    /*
     * =========================================================
     * THEME MATERIAL
     * =========================================================
     *
     * No Haze.
     * No backdrop blur.
     * No shadow.
     *
     * Tiny transparency is retained so it does not read as a
     * completely dead block.
     */

    val parentBackground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF9F9FA)
                    .copy(
                        alpha = .965f
                    )

            XmoTheme.Dark ->
                Color(0xFF181819)
                    .copy(
                        alpha = .965f
                    )

            XmoTheme.Amoled ->
                Color(0xFF080808)
                    .copy(
                        alpha = .975f
                    )
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

    val parentTopReflection =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .54f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .075f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .065f
                )
        }

    /*
     * Selector is neutral and slightly separated from its parent.
     *
     * No blue-grey tint.
     */
    val selector =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFE8E8EA)
                    .copy(
                        alpha = .91f
                    )

            XmoTheme.Dark ->
                Color(0xFF29292B)
                    .copy(
                        alpha = .93f
                    )

            XmoTheme.Amoled ->
                Color(0xFF1C1C1E)
                    .copy(
                        alpha = .94f
                    )
        }

    val selectorBorder =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .14f
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

    /*
     * Optical glass reflections.
     *
     * These do not sample/blur the background. Their uneven
     * positions merely give the selector a refracted glass-edge
     * character while keeping rendering lightweight.
     */
    val selectorReflection =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .58f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .18f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .15f
                )
        }

    val selectorSoftReflection =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .24f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .075f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .065f
                )
        }

    val selectorDarkRefraction =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .075f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .25f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .34f
                )
        }

    val inactive =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .50f
                )

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .46f
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

    /*
     * =========================================================
     * ORIGINAL 96dp GESTURE HOST
     * =========================================================
     *
     * Only placement changed:
     *
     * 35dp -> 29dp
     *
     * The complete NavBar therefore sits 6dp lower.
     */

    Box(
        modifier =
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .navigationBarsPadding()
                .padding(
                    bottom = 29.dp
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

                        while (change.pressed) {
                            val event =
                                awaitPointerEvent()

                            change =
                                event.changes
                                    .first()

                            if (change.pressed) {
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
                                 * Original finger-follow math.
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
                                    abs(total) >
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

                        /*
                         * Screen switch is dispatched immediately
                         * on resolved release. Selector spring does
                         * not block the navigation callback.
                         */
                        if (
                            target !=
                            selected
                        ) {
                            select(target)
                        }

                        down = false
                    }
                }
    ) {
        /*
         * =====================================================
         * PARENT — EXACT 246 × 64
         * =====================================================
         */

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
                    .background(
                        parentBackground
                    )
                    .drawBehind {
                        /*
                         * Very subtle upper material reflection.
                         * Not blur and not shadow.
                         */
                        drawRoundRect(
                            brush =
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to
                                                parentTopReflection,
                                            .22f to
                                                parentTopReflection.copy(
                                                    alpha =
                                                        parentTopReflection.alpha *
                                                            .32f
                                                ),
                                            .55f to
                                                Color.Transparent
                                        )
                                ),
                            cornerRadius =
                                CornerRadius(
                                    33.dp.toPx()
                                )
                        )
                    }
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

        /*
         * =====================================================
         * SELECTOR — ORIGINAL GEOMETRY
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
                        /*
                         * Original velocity deformation preserved.
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

                        clip = true
                    }
                    .background(
                        selector
                    )
                    .drawBehind {
                        val corner =
                            CornerRadius(
                                radius.toPx()
                            )

                        /*
                         * Broad top reflection.
                         */
                        drawRoundRect(
                            brush =
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to
                                                selectorReflection,
                                            .20f to
                                                selectorSoftReflection,
                                            .54f to
                                                Color.Transparent,
                                            1f to
                                                selectorDarkRefraction
                                        )
                                ),
                            cornerRadius =
                                corner
                        )

                        /*
                         * Left refracted edge.
                         *
                         * Intentionally short and uneven instead
                         * of outlining the entire pill.
                         */
                        drawLine(
                            color =
                                selectorReflection.copy(
                                    alpha =
                                        selectorReflection.alpha *
                                            .72f
                                ),
                            start =
                                Offset(
                                    x =
                                        2.2.dp.toPx(),
                                    y =
                                        size.height *
                                            .27f
                                ),
                            end =
                                Offset(
                                    x =
                                        2.2.dp.toPx(),
                                    y =
                                        size.height *
                                            .63f
                                ),
                            strokeWidth =
                                1.1.dp.toPx(),
                            cap =
                                StrokeCap.Round
                        )

                        /*
                         * Broken/shattered-style top-right
                         * reflection. Purely optical and cheap.
                         */
                        drawLine(
                            color =
                                selectorReflection.copy(
                                    alpha =
                                        selectorReflection.alpha *
                                            .58f
                                ),
                            start =
                                Offset(
                                    x =
                                        size.width *
                                            .68f,
                                    y =
                                        1.8.dp.toPx()
                                ),
                            end =
                                Offset(
                                    x =
                                        size.width *
                                            .81f,
                                    y =
                                        1.1.dp.toPx()
                                ),
                            strokeWidth =
                                .9.dp.toPx(),
                            cap =
                                StrokeCap.Round
                        )

                        drawLine(
                            color =
                                selectorSoftReflection,
                            start =
                                Offset(
                                    x =
                                        size.width *
                                            .84f,
                                    y =
                                        2.0.dp.toPx()
                                ),
                            end =
                                Offset(
                                    x =
                                        size.width *
                                            .91f,
                                    y =
                                        3.5.dp.toPx()
                                ),
                            strokeWidth =
                                .75.dp.toPx(),
                            cap =
                                StrokeCap.Round
                        )

                        /*
                         * Subtle internal refraction on the lower
                         * opposite edge.
                         */
                        drawArc(
                            color =
                                selectorDarkRefraction,
                            startAngle = 18f,
                            sweepAngle = 64f,
                            useCenter = false,
                            topLeft =
                                Offset(
                                    size.width *
                                        .49f,
                                    size.height *
                                        .43f
                                ),
                            size =
                                androidx.compose.ui.geometry.Size(
                                    size.width *
                                        .48f,
                                    size.height *
                                        .49f
                                ),
                            style =
                                Stroke(
                                    width =
                                        .75.dp.toPx()
                                )
                        )
                    }
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

        /*
         * =====================================================
         * ICON ROW — ORIGINAL GEOMETRY
         * =====================================================
         */

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
                                dampingRatio = .78f,
                                stiffness = 1050f
                            ),
                        label =
                            "navIcon$index"
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
                                .size(25.dp)
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
