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
    val base =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF4F5F7)

            XmoTheme.Dark ->
                Color(0xFF17181C)

            XmoTheme.Amoled ->
                Color(0xFF010102)
        }

    val bright =
        brightenSplash(
            color = dominant,
            amount =
                when (theme) {
                    XmoTheme.Light -> .19f
                    XmoTheme.Dark -> .13f
                    XmoTheme.Amoled -> .08f
                }
        )

    val rich =
        mixColor(
            from = deep,
            to = dominant,
            fraction = .58f
        )

    val middle =
        mixColor(
            from = dominant,
            to = bright,
            fraction = .46f
        )

    val soft =
        mixColor(
            from = bright,
            to = Color.White,
            fraction =
                when (theme) {
                    XmoTheme.Light -> .23f
                    XmoTheme.Dark -> .08f
                    XmoTheme.Amoled -> .035f
                }
        )

    Canvas(
        modifier =
            Modifier.fillMaxSize()
    ) {
        drawRect(
            color = base
        )

        /*
         * Oversized fields deliberately extend well beyond the
         * viewport. They read as distributed artwork light rather
         * than individual circular gradient spots.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                bright.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .50f
                                            XmoTheme.Dark -> .50f
                                            XmoTheme.Amoled -> .36f
                                        }
                                ),
                            .38f to
                                bright.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .30f
                                            XmoTheme.Dark -> .29f
                                            XmoTheme.Amoled -> .20f
                                        }
                                ),
                            .72f to
                                bright.copy(
                                    alpha = .09f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    -.10f,
                            y =
                                size.height *
                                    .05f
                        ),
                    radius =
                        size.width *
                            1.48f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                rich.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .38f
                                            XmoTheme.Dark -> .43f
                                            XmoTheme.Amoled -> .29f
                                        }
                                ),
                            .48f to
                                rich.copy(
                                    alpha = .18f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    1.13f,
                            y =
                                size.height *
                                    .25f
                        ),
                    radius =
                        size.width *
                            1.37f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                soft.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .35f
                                            XmoTheme.Dark -> .30f
                                            XmoTheme.Amoled -> .20f
                                        }
                                ),
                            .42f to
                                middle.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .22f
                                            XmoTheme.Dark -> .21f
                                            XmoTheme.Amoled -> .15f
                                        }
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .43f,
                            y =
                                size.height *
                                    .49f
                        ),
                    radius =
                        size.width *
                            1.72f
                )
        )

        /*
         * Lower fields stay visible through the glass player
         * panel so the panel does not become a flat solid block.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                rich.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .34f
                                            XmoTheme.Dark -> .37f
                                            XmoTheme.Amoled -> .27f
                                        }
                                ),
                            .50f to
                                rich.copy(
                                    alpha = .14f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    -.16f,
                            y =
                                size.height *
                                    .84f
                        ),
                    radius =
                        size.width *
                            1.50f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                middle.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .32f
                                            XmoTheme.Dark -> .34f
                                            XmoTheme.Amoled -> .23f
                                        }
                                ),
                            .54f to
                                middle.copy(
                                    alpha = .13f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    1.12f,
                            y =
                                size.height *
                                    .96f
                        ),
                    radius =
                        size.width *
                            1.58f
                )
        )

        /*
         * Neutral veil preserves the identity of each theme.
         */
        when (theme) {
            XmoTheme.Light -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0f to
                                        Color.White.copy(
                                            alpha = .05f
                                        ),
                                    .50f to
                                        Color.Transparent,
                                    1f to
                                        Color.White.copy(
                                            alpha = .08f
                                        )
                                )
                        )
                )
            }

            XmoTheme.Dark -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0f to
                                        Color.White.copy(
                                            alpha = .012f
                                        ),
                                    .55f to
                                        Color.Transparent,
                                    1f to
                                        Color.Black.copy(
                                            alpha = .05f
                                        )
                                )
                        )
                )
            }

            XmoTheme.Amoled -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colorStops =
                                arrayOf(
                                    0f to
                                        Color.Black.copy(
                                            alpha = .02f
                                        ),
                                    .52f to
                                        Color.Transparent,
                                    1f to
                                        Color.Black.copy(
                                            alpha = .11f
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
                (1f - color.red) *
                value,
        green =
            color.green +
                (1f - color.green) *
                value,
        blue =
            color.blue +
                (1f - color.blue) *
                value,
        alpha = 1f
    )
}
