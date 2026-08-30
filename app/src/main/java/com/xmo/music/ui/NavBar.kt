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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

/*
 * Approved XMO NavBar geometry.
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

    LaunchedEffect(selected) {
        if (!down) {
            pos =
                selected.toFloat()
        }
    }

    /*
     * Original selector physics.
     *
     * Spring remains between pos and rendered x during drag as
     * well as settle.
     */
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
                    dampingRatio = .78f,
                    stiffness = 850f
                ),
            label = "grow"
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
                    dampingRatio = .78f,
                    stiffness = 900f
                ),
            label = "bar"
        )

    /*
     * =========================================================
     * THEME SURFACES
     * =========================================================
     *
     * No Haze.
     * No blur.
     * No shadow.
     * Neutral blacks rather than blue-grey.
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

    val parentReflection =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .46f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .07f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .055f
                )
        }

    /*
     * More transparent selector.
     *
     * Parent surface can subtly show through it, giving it a
     * glass-like layer without actual backdrop blur.
     */
    val selector =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF4F4F5)
                    .copy(
                        alpha = .54f
                    )

            XmoTheme.Dark ->
                Color(0xFF303031)
                    .copy(
                        alpha = .52f
                    )

            XmoTheme.Amoled ->
                Color(0xFF292929)
                    .copy(
                        alpha = .48f
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
                    alpha = .16f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .19f
                )
        }

    val selectorReflection =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .52f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .15f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .12f
                )
        }

    val selectorMidReflection =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .16f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .055f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .045f
                )
        }

    val selectorLowerShade =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .045f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .12f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .20f
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

    /*
     * =========================================================
     * ORIGINAL GESTURE HOST
     * =========================================================
     *
     * Geometry is unchanged.
     * Only whole-host placement is lower:
     *
     * original 35dp
     * previous 29dp
     * now      24dp
     */

    Box(
        modifier =
            Modifier
                .align(
                    Alignment.BottomCenter
                )
                .navigationBarsPadding()
                .padding(
                    bottom = 24.dp
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

                                /*
                                 * Original finger-follow input.
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
         * PARENT — 246 × 64
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
                    .drawBehind {
                        drawRoundRect(
                            brush =
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to
                                                parentReflection,
                                            .23f to
                                                parentReflection.copy(
                                                    alpha =
                                                        parentReflection.alpha *
                                                            .27f
                                                ),
                                            .54f to
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
                        color = parentBorder,
                        shape =
                            RoundedCornerShape(
                                33.dp
                            )
                    )
        )

        /*
         * =====================================================
         * SELECTOR — ORIGINAL GEOMETRY + PHYSICS
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
                        val cornerRadius =
                            CornerRadius(
                                radius.toPx()
                            )

                        /*
                         * Soft glass-like internal reflection.
                         *
                         * No left vertical line: the tiny white
                         * artifact from the previous revision is
                         * intentionally gone.
                         */
                        drawRoundRect(
                            brush =
                                Brush.verticalGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to
                                                selectorReflection,
                                            .18f to
                                                selectorMidReflection,
                                            .54f to
                                                Color.Transparent,
                                            1f to
                                                selectorLowerShade
                                        )
                                ),
                            cornerRadius =
                                cornerRadius
                        )

                        /*
                         * A broad, low-alpha diagonal light field
                         * avoids a hard straight reflection edge.
                         */
                        drawRoundRect(
                            brush =
                                Brush.linearGradient(
                                    colorStops =
                                        arrayOf(
                                            0f to
                                                Color.Transparent,
                                            .38f to
                                                selectorMidReflection,
                                            .55f to
                                                Color.Transparent,
                                            1f to
                                                Color.Transparent
                                        ),
                                    start =
                                        Offset(
                                            x = 0f,
                                            y =
                                                size.height
                                        ),
                                    end =
                                        Offset(
                                            x =
                                                size.width,
                                            y = 0f
                                        )
                                ),
                            cornerRadius =
                                cornerRadius
                        )

                        /*
                         * Very soft lower refraction. It stays
                         * inside the selector rather than drawing
                         * a disconnected border fragment.
                         */
                        drawArc(
                            color =
                                selectorLowerShade,
                            startAngle = 25f,
                            sweepAngle = 52f,
                            useCenter = false,
                            topLeft =
                                Offset(
                                    x =
                                        size.width *
                                            .52f,
                                    y =
                                        size.height *
                                            .52f
                                ),
                            size =
                                Size(
                                    width =
                                        size.width *
                                            .39f,
                                    height =
                                        size.height *
                                            .35f
                                ),
                            style =
                                Stroke(
                                    width =
                                        .55.dp.toPx()
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
         * ICONS
         * =====================================================
         *
         * Activation follows selector distance continuously.
         * No abrupt selected/unselected color switch.
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

                /*
                 * 1 when selector is exactly centered on icon,
                 * smoothly falls to 0 by one full slot away.
                 */
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

                /*
                 * While held, only the icon actually under the
                 * selector receives the original extra zoom,
                 * but the value fades continuously as selector
                 * leaves it.
                 */
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
                        from =
                            inactive,
                        to =
                            active,
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

                                    /*
                                     * Tiny optical fade reinforces
                                     * the selector moving away.
                                     */
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
