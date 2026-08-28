package com.xmo.music.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.xmo.music.XmoTheme
import kotlin.math.abs
import kotlin.math.roundToInt

private val BarWidth = 246.dp
private val BarHeight = 64.dp

private val RestWidth = 78.dp
private val RestHeight = 56.dp

@Composable
fun BoxScope.NavBar(
    selected: Int,
    theme: XmoTheme,
    select: (Int) -> Unit
) {
    val accent =
        LocalXmoAccent.current

    /*
     * Selector position is intentionally isolated from screen
     * composition. Heavy Home/Search/Settings work therefore
     * doesn't rebuild this gesture state every pointer pixel.
     */
    val selector =
        remember {
            Animatable(
                selected.toFloat()
            )
        }

    var dragging by
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

    var fingerStretch by
        remember {
            mutableFloatStateOf(
                0f
            )
        }

    /*
     * External navigation / Android Back.
     */
    LaunchedEffect(
        selected
    ) {
        if (
            !dragging &&
            abs(
                selector.value -
                    selected
            ) >
            .001f
        ) {
            selector.animateTo(
                targetValue =
                    selected.toFloat(),

                animationSpec =
                    spring(
                        dampingRatio =
                            .79f,

                        stiffness =
                            Spring.StiffnessMediumLow
                    )
            )
        }
    }

    val parentColor =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .84f
                )

            XmoTheme.Dark ->
                Color(
                    0xFF17181B
                ).copy(
                    alpha = .88f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .88f
                )
        }

    val parentBorder =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .09f
                )

            else ->
                Color.White.copy(
                    alpha = .12f
                )
        }

    val parentHighlight =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .80f
                )

            else ->
                Color.White.copy(
                    alpha = .12f
                )
        }

    val activeIcon =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color(
                    0xFF111214
                )

            else ->
                Color.White
        }

    val inactiveIcon =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .42f
                )

            else ->
                Color.White.copy(
                    alpha = .40f
                )
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
     * APPROVED 96dp OVERFLOW HOST
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
                BarWidth,
                96.dp
            )
            .pointerInput(
                selected
            ) {
                awaitEachGesture {
                    val down =
                        awaitFirstDown(
                            requireUnconsumed =
                                false
                        )

                    val slot =
                        size.width /
                            3f

                    val startSelection =
                        selected
                            .coerceIn(
                                0,
                                2
                            )
                            .toFloat()

                    val startX =
                        down.position.x

                    var previousX =
                        startX

                    var totalX =
                        0f

                    var change =
                        down

                    dragging =
                        true

                    velocity =
                        0f

                    fingerStretch =
                        1f

                    /*
                     * Kill any old settle before direct
                     * finger-follow begins.
                     */
                    selector.stop()

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

                        val x =
                            change.position.x

                        val dx =
                            x -
                                previousX

                        previousX =
                            x

                        totalX =
                            x -
                                startX

                        /*
                         * Lightweight velocity smoothing.
                         */
                        velocity =
                            velocity *
                                .67f +
                                dx *
                                .33f

                        val raw =
                            startSelection +
                                totalX /
                                slot

                        /*
                         * Tiny resistance at absolute ends rather
                         * than hard visual clipping.
                         */
                        val resisted =
                            when {
                                raw <
                                    0f ->
                                    raw *
                                        .10f

                                raw >
                                    2f ->
                                    2f +
                                        (
                                            raw -
                                                2f
                                            ) *
                                        .10f

                                else ->
                                    raw
                            }

                        selector.snapTo(
                            resisted.coerceIn(
                                -.08f,
                                2.08f
                            )
                        )

                        if (
                            abs(
                                totalX
                            ) >
                            2f
                        ) {
                            change.consume()
                        }
                    }

                    val target =
                        if (
                            abs(
                                totalX
                            ) <
                            7f
                        ) {
                            (
                                down.position.x /
                                    slot
                                )
                                .toInt()
                                .coerceIn(
                                    0,
                                    2
                                )
                        } else {
                            /*
                             * Small projected velocity makes quick
                             * flicks settle naturally.
                             */
                            (
                                selector.value +
                                    velocity /
                                    85f
                                )
                                .roundToInt()
                                .coerceIn(
                                    0,
                                    2
                                )
                        }

                    dragging =
                        false

                    fingerStretch =
                        0f

                    velocity =
                        0f

                    /*
                     * Selector animation begins before the heavier
                     * destination screen is requested.
                     */
                    launch {
                        selector.animateTo(
                            targetValue =
                                target.toFloat(),

                            animationSpec =
                                spring(
                                    dampingRatio =
                                        .76f,

                                    stiffness =
                                        620f
                                )
                        )
                    }

                    /*
                     * Give Compose one frame to render the navbar's
                     * release state first. This prevents the screen
                     * change from visually swallowing the release.
                     */
                    withFrameNanos { }

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
         * THEME-AWARE TRANSLUCENT PARENT
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
                    /*
                     * Render as its own inexpensive layer.
                     */
                    shape =
                        RoundedCornerShape(
                            33.dp
                        )

                    clip =
                        true
                }
                .background(
                    parentColor
                )
                .drawBehind {
                    val radius =
                        33.dp.toPx()

                    /*
                     * Upper glass-like reflection. No backdrop
                     * sampling or blur dependency involved.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        parentHighlight,
                                        Color.Transparent,
                                        Color.Transparent
                                    ),

                                startY =
                                    0f,

                                endY =
                                    size.height *
                                        .74f
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
                            )
                    )

                    /*
                     * Slight accent spread near lower-right.
                     */
                    drawRoundRect(
                        brush =
                            Brush.radialGradient(
                                colors =
                                    listOf(
                                        accent.copy(
                                            alpha = .055f
                                        ),
                                        Color.Transparent
                                    ),

                                center =
                                    Offset(
                                        size.width *
                                            .86f,

                                        size.height *
                                            1.15f
                                    ),

                                radius =
                                    size.width *
                                        .48f
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
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
         * CLEAR REFRACTION-STYLE SELECTOR
         *
         * There is deliberately no opaque fill.
         * =====================================================
         */
        val pressedProgress =
            if (
                dragging
            ) {
                1f
            } else {
                fingerStretch
            }

        val selectorWidth =
            RestWidth +
                32.dp *
                pressedProgress

        val selectorHeight =
            RestHeight +
                24.dp *
                pressedProgress

        val selectorRadius =
            29.dp +
                13.dp *
                pressedProgress

        val stretch =
            if (
                dragging
            ) {
                (
                    abs(
                        velocity
                    ) /
                        18f
                    )
                    .coerceIn(
                        0f,
                        1f
                    )
            } else {
                0f
            }

        val selectorX =
            4.dp +
                160.dp *
                (
                    selector.value /
                        2f
                    ) -
                16.dp *
                pressedProgress

        Box(
            Modifier
                .align(
                    Alignment.CenterStart
                )
                .offset(
                    x =
                        selectorX
                )
                .size(
                    selectorWidth,
                    selectorHeight
                )
                .graphicsLayer {
                    scaleX =
                        1f +
                            stretch *
                            .15f

                    scaleY =
                        1f -
                            stretch *
                            .055f

                    rotationZ =
                        (
                            velocity *
                                .055f
                            )
                            .coerceIn(
                                -1.1f,
                                1.1f
                            )

                    shape =
                        RoundedCornerShape(
                            selectorRadius
                        )

                    clip =
                        true
                }
                /*
                 * Almost clear body.
                 */
                .background(
                    when (
                        theme
                    ) {
                        XmoTheme.Light ->
                            Color.White.copy(
                                alpha = .15f
                            )

                        else ->
                            Color.White.copy(
                                alpha = .055f
                            )
                    }
                )
                .drawBehind {
                    val radius =
                        selectorRadius.toPx()

                    /*
                     * Refraction approximation:
                     * bright curved upper edge + dim lower edge.
                     * No blur or captured backdrop required.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            if (
                                                theme ==
                                                XmoTheme.Light
                                            ) {
                                                .68f
                                            } else {
                                                .28f
                                            }
                                    ),
                                    Color.White.copy(
                                        alpha = .055f
                                    ),
                                    Color.Transparent
                                )
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
                            )
                    )

                    drawRoundRect(
                        brush =
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(
                                        alpha = .13f
                                    ),
                                    Color.Transparent,
                                    accent.copy(
                                        alpha = .10f
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
                    .7.dp,
                    accent.copy(
                        alpha = .38f
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

                /*
                 * No per-icon animation state. Selection is derived
                 * directly from one already-running Animatable.
                 */
                val distance =
                    abs(
                        selector.value -
                            index
                    )

                val influence =
                    (
                        1f -
                            distance
                        )
                        .coerceIn(
                            0f,
                            1f
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
                                influence >
                                .5f
                            ) {
                                activeIcon
                            } else {
                                inactiveIcon
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
                                    val scale =
                                        1f +
                                            influence *
                                            if (
                                                dragging
                                            ) {
                                                .07f
                                            } else {
                                                .025f
                                            }

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
