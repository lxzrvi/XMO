package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
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
     * Neutral/theme base stays neutral.
     *
     * Artwork is painted as large overlapping fields instead of
     * turning the entire screen into one solid dominant color.
     */
    val base =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF5F6F8)

            XmoTheme.Dark ->
                Color(0xFF191A1E)

            XmoTheme.Amoled ->
                Color(0xFF020203)
        }

    val bright =
        brightenSplash(
            dominant,
            when (theme) {
                XmoTheme.Light ->
                    .17f

                XmoTheme.Dark ->
                    .11f

                XmoTheme.Amoled ->
                    .08f
            }
        )

    val second =
        mixColor(
            from = deep,
            to = bright,
            fraction = .64f
        )

    val soft =
        mixColor(
            from = bright,
            to = Color.White,
            fraction =
                when (theme) {
                    XmoTheme.Light ->
                        .20f

                    XmoTheme.Dark ->
                        .09f

                    XmoTheme.Amoled ->
                        .045f
                }
        )

    Canvas(
        modifier =
            Modifier.fillMaxSize()
    ) {
        drawRect(
            color =
                base
        )

        /*
         * Upper-left cloud.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            bright.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .44f

                                        XmoTheme.Dark ->
                                            .47f

                                        XmoTheme.Amoled ->
                                            .38f
                                    }
                            ),
                            bright.copy(
                                alpha = .20f
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .03f,
                            y =
                                size.height *
                                    .07f
                        ),
                    radius =
                        size.width *
                            1.23f
                )
        )

        /*
         * Upper-right secondary cloud.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            second.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .33f

                                        XmoTheme.Dark ->
                                            .38f

                                        XmoTheme.Amoled ->
                                            .29f
                                    }
                            ),
                            second.copy(
                                alpha = .12f
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    1.02f,
                            y =
                                size.height *
                                    .25f
                        ),
                    radius =
                        size.width *
                            1.08f
                )
        )

        /*
         * Broad middle field spreads artwork hue instead of
         * leaving one obvious radial circle.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            soft.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .30f

                                        XmoTheme.Dark ->
                                            .31f

                                        XmoTheme.Amoled ->
                                            .24f
                                    }
                            ),
                            soft.copy(
                                alpha = .11f
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .37f,
                            y =
                                size.height *
                                    .52f
                        ),
                    radius =
                        size.width *
                            1.48f
                )
        )

        /*
         * Lower-left field remains visible through the translucent
         * information/transport panel.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            second.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .26f

                                        XmoTheme.Dark ->
                                            .31f

                                        XmoTheme.Amoled ->
                                            .25f
                                    }
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                -size.width *
                                    .06f,
                            y =
                                size.height *
                                    .82f
                        ),
                    radius =
                        size.width *
                            1.20f
                )
        )

        /*
         * Lower-right balancing field.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            bright.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .25f

                                        XmoTheme.Dark ->
                                            .28f

                                        XmoTheme.Amoled ->
                                            .21f
                                    }
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    1.04f,
                            y =
                                size.height *
                                    .93f
                        ),
                    radius =
                        size.width *
                            1.32f
                )
        )

        /*
         * Very light finishing veil. It never replaces artwork
         * color with a solid theme block.
         */
        when (theme) {
            XmoTheme.Light -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(
                                    alpha = .04f
                                ),
                                Color.Transparent,
                                Color.White.copy(
                                    alpha = .07f
                                )
                            )
                        )
                )
            }

            XmoTheme.Dark -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(
                                    alpha = .012f
                                ),
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = .035f
                                )
                            )
                        )
                )
            }

            XmoTheme.Amoled -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = .035f
                                ),
                                Color.Black.copy(
                                    alpha = .075f
                                )
                            )
                        )
                )
            }
        }
    }
}

private fun brightenSplash(
    color: Color,
    amount: Float
): Color {
    val value =
        amount.coerceIn(
            0f,
            1f
        )

    return Color(
        red =
            color.red +
                (
                    1f -
                        color.red
                    ) *
                value,
        green =
            color.green +
                (
                    1f -
                        color.green
                    ) *
                value,
        blue =
            color.blue +
                (
                    1f -
                        color.blue
                    ) *
                value,
        alpha = 1f
    )
}
