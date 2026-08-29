package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.xmo.music.XmoTheme

@Composable
internal fun PlayerBackground(
    dominant: Color,
    deep: Color,
    theme: XmoTheme
) {
    /*
     * The neutral base itself is deliberately lighter than the
     * previous implementation. Artwork contributes tint, not a
     * dark solid backdrop.
     */
    val base =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF4F6F9)

            XmoTheme.Dark ->
                Color(0xFF24262B)

            XmoTheme.Amoled ->
                Color(0xFF090A0C)
        }

    val primary =
        when (theme) {
            XmoTheme.Light ->
                .30f

            XmoTheme.Dark ->
                .30f

            XmoTheme.Amoled ->
                .25f
        }

    val secondary =
        primary * .55f

    /*
     * Mix the raw deep color back towards dominant. This stops a
     * very dark Palette-derived "deep" field from muddying the
     * screen.
     */
    val softDeep =
        mixColor(
            from = deep,
            to = dominant,
            fraction = .58f
        )

    Canvas(
        Modifier
            .fillMaxSize()
            .background(base)
    ) {
        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant.copy(
                            alpha = primary
                        ),
                        dominant.copy(
                            alpha =
                                primary *
                                    .10f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .12f,
                        size.height * .12f
                    ),
                radius =
                    size.width * 1.22f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        softDeep.copy(
                            alpha =
                                secondary
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .90f,
                        size.height * .25f
                    ),
                radius =
                    size.width * 1.30f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant.copy(
                            alpha =
                                primary *
                                    .72f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .10f,
                        size.height * .58f
                    ),
                radius =
                    size.width * 1.34f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        softDeep.copy(
                            alpha =
                                secondary *
                                    .64f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .88f,
                        size.height * .78f
                    ),
                radius =
                    size.width * 1.38f
            )
        )

        /*
         * Gentle light veil rather than a dark veil.
         */
        when (theme) {
            XmoTheme.Light -> {
                drawRect(
                    Brush.verticalGradient(
                        colors =
                            listOf(
                                Color.White.copy(
                                    alpha = .05f
                                ),
                                Color.White.copy(
                                    alpha = .13f
                                )
                            )
                    )
                )
            }

            XmoTheme.Dark -> {
                drawRect(
                    Color.White.copy(
                        alpha = .018f
                    )
                )
            }

            XmoTheme.Amoled -> {
                /*
                 * AMOLED remains black-based, but artwork fields
                 * stay visible rather than being covered by an
                 * additional dark gradient.
                 */
            }
        }
    }
}
