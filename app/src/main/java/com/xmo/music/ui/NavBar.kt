package com.xmo.music.ui

import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch
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

    val scope =
        rememberCoroutineScope()

    /*
     * Single animation state for the selector.
     * No backdrop capture, blur or expensive effect.
     */
    val position =
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

    /*
     * Programmatic navigation / Android Back.
     */
    LaunchedEffect(
        selected
    ) {
        if (
            !dragging &&
            abs(
                position.value -
                    selected
            ) >
            .001f
        ) {
            position.animateTo(
                selected.toFloat(),

                spring(
                    dampingRatio = .78f,
                    stiffness = 650f
                )
            )
        }
    }

    val background =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .88f
                )

            XmoTheme.Dark ->
                Color(
                    0xFF17181B
                ).copy(
                    alpha = .90f
                )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .90f
                )
        }

    val border =
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

    val reflection =
        when (
            theme
        ) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .76f
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
                    0xFF151515
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
                    alpha = .40f
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
     * 96dp APPROVED OVERFLOW / GESTURE HOST
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
                    val down =
                        awaitFirstDown(
                            requireUnconsumed =
                                false
                        )

                    val slot =
                        size.width /
                            3f

                    val startPosition =
                        selected
                            .coerceIn(
                                0,
                                2
                            )
                            .toFloat()

                    val startX =
                        down.position.x

                    var lastX =
                        startX

                    var totalX =
                        0f

                    var change =
                        down

                    dragging =
                        true

                    velocity =
                        0f

                    /*
                     * All Animatable suspension happens in the
                     * normal Compose coroutine scope, not the
                     * restricted pointer coroutine.
                     */
                    scope.launch {
                        position.stop()
                    }

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

                        lastX =
                            currentX

                        totalX =
                            currentX -
                                startX

                        velocity =
                            velocity *
                                .68f +
                                dx *
                                .32f

                        val raw =
                            startPosition +
                                totalX /
                                slot

                        /*
                         * Slight resistance at both ends.
                         */
                        val target =
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
                                .coerceIn(
                                    -.08f,
                                    2.08f
                                )

                        scope.launch {
                            position.snapTo(
                                target
                            )
                        }

                        if (
                            abs(
                                totalX
                            ) >
                            2f
                        ) {
                            change.consume()
                        }
                    }

                    val targetTab =
                        if (
                            abs(
                                totalX
                            ) <=
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
                            (
                                position.value +
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

                    velocity =
                        0f

                    /*
                     * Destination content switches immediately.
                     * Selector settling is independent of expensive
                     * destination composition.
                     */
                    if (
                        targetTab !=
                        selected
                    ) {
                        select(
                            targetTab
                        )
                    }

                    scope.launch {
                        position.animateTo(
                            targetTab.toFloat(),

                            spring(
                                dampingRatio = .76f,
                                stiffness = 700f
                            )
                        )
                    }
                }
            }
    ) {
        /*
         * =====================================================
         * THEME-AWARE TRANSLUCENT NAVBAR
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
                    shape =
                        RoundedCornerShape(
                            33.dp
                        )

                    clip =
                        true
                }
                .background(
                    background
                )
                .drawBehind {
                    val radius =
                        33.dp.toPx()

                    /*
                     * Top reflection.
                     */
                    drawRoundRect(
                        brush =
                            Brush.verticalGradient(
                                listOf(
                                    reflection,
                                    Color.Transparent,
                                    Color.Transparent
                                ),

                                endY =
                                    size.height *
                                        .70f
                            ),

                        cornerRadius =
                            CornerRadius(
                                radius
                            )
                    )

                    /*
                     * Very subtle accent light.
                     */
                    drawRoundRect(
                        brush =
                            Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    accent.copy(
                                        alpha = .025f
                                    ),
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
                    border,
                    RoundedCornerShape(
                        33.dp
                    )
                )
        )

        /*
         * =====================================================
         * CLEAR REFRACTION PILL
         * =====================================================
         */

        val grow =
            if (
                dragging
            ) {
                1f
            } else {
                0f
            }

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

        val selectorWidth =
            RestWidth +
                32.dp *
                grow

        val selectorHeight =
            RestHeight +
                24.dp *
                grow

        val selectorRadius =
            29.dp +
                13.dp *
                grow

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
                                position.value /
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
                            .15f

                    scaleY =
                        1f -
                            stretch *
                            .055f

                    shape =
                        RoundedCornerShape(
                            selectorRadius
                        )

                    clip =
                        true
                }
                /*
                 * Nearly clear body.
                 */
                .background(
                    when (
                        theme
                    ) {
                        XmoTheme.Light ->
                            Color.White.copy(
                                alpha = .16f
                            )

                        XmoTheme.Dark ->
                            Color.White.copy(
                                alpha = .065f
                            )

                        XmoTheme.Amoled ->
                            Color.White.copy(
                                alpha = .055f
                            )
                    }
                )
                .drawBehind {
                    val radius =
                        selectorRadius.toPx()

                    /*
                     * Curved reflection/refraction illusion.
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
                                                .60f
                                            } else {
                                                .24f
                                            }
                                    ),
                                    Color.White.copy(
                                        alpha = .04f
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
                                        alpha = .10f
                                    ),
                                    Color.Transparent,
                                    accent.copy(
                                        alpha = .09f
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
                    horizontal = 4.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            icons.forEachIndexed {
                    index,
                    icon ->

                val distance =
                    abs(
                        position.value -
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
                                                .02f
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
