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
     * All fields are calculated from the transaction-controlled
     * display color. PlayerBackground never owns a separate song
     * transition, which avoids Palette/background races.
     */
    val brightDominant =
        brightenBackgroundColor(
            color = dominant,
            amount =
                when (theme) {
                    XmoTheme.Light ->
                        .16f

                    XmoTheme.Dark ->
                        .09f

                    XmoTheme.Amoled ->
                        .05f
                }
        )

    val relatedDeep =
        mixColor(
            from = deep,
            to = brightDominant,
            fraction = .72f
        )

    /*
     * Artwork hue participates in the base itself instead of
     * being only a faint field over an unrelated neutral screen.
     */
    val base =
        when (theme) {
            XmoTheme.Light ->
                mixColor(
                    from =
                        Color(0xFFF8F9FB),
                    to =
                        brightDominant,
                    fraction =
                        .23f
                )

            XmoTheme.Dark ->
                mixColor(
                    from =
                        Color(0xFF17181C),
                    to =
                        brightDominant,
                    fraction =
                        .32f
                )

            XmoTheme.Amoled ->
                mixColor(
                    from =
                        Color(0xFF030405),
                    to =
                        brightDominant,
                    fraction =
                        .21f
                )
        }

    val topColor =
        when (theme) {
            XmoTheme.Light ->
                mixColor(
                    from = base,
                    to =
                        brightDominant,
                    fraction =
                        .48f
                )

            XmoTheme.Dark ->
                mixColor(
                    from = base,
                    to =
                        brightDominant,
                    fraction =
                        .50f
                )

            XmoTheme.Amoled ->
                mixColor(
                    from = base,
                    to =
                        brightDominant,
                    fraction =
                        .44f
                )
        }

    val lowerColor =
        when (theme) {
            XmoTheme.Light ->
                mixColor(
                    from = base,
                    to =
                        brightDominant,
                    fraction =
                        .26f
                )

            XmoTheme.Dark ->
                mixColor(
                    from = base,
                    to =
                        relatedDeep,
                    fraction =
                        .35f
                )

            XmoTheme.Amoled ->
                mixColor(
                    from = base,
                    to =
                        relatedDeep,
                    fraction =
                        .24f
                )
        }

    Canvas(
        modifier =
            Modifier.fillMaxSize()
    ) {
        drawRect(
            color = base
        )

        /*
         * Main artwork-derived field behind header/artwork.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            topColor.copy(
                                alpha = .94f
                            ),
                            topColor.copy(
                                alpha = .48f
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .20f,
                            y =
                                size.height *
                                    .05f
                        ),
                    radius =
                        size.width *
                            1.35f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            relatedDeep.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .24f

                                        XmoTheme.Dark ->
                                            .34f

                                        XmoTheme.Amoled ->
                                            .27f
                                    }
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .98f,
                            y =
                                size.height *
                                    .28f
                        ),
                    radius =
                        size.width *
                            1.18f
                )
        )

        /*
         * Color underneath translucent player panel.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            brightDominant.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .30f

                                        XmoTheme.Dark ->
                                            .35f

                                        XmoTheme.Amoled ->
                                            .28f
                                    }
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .08f,
                            y =
                                size.height *
                                    .68f
                        ),
                    radius =
                        size.width *
                            1.46f
                )
        )

        drawRect(
            brush =
                Brush.radialGradient(
                    colors =
                        listOf(
                            lowerColor.copy(
                                alpha =
                                    when (theme) {
                                        XmoTheme.Light ->
                                            .50f

                                        XmoTheme.Dark ->
                                            .52f

                                        XmoTheme.Amoled ->
                                            .44f
                                    }
                            ),
                            Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .82f,
                            y =
                                size.height *
                                    .94f
                        ),
                    radius =
                        size.width *
                            1.30f
                )
        )

        when (theme) {
            XmoTheme.Light -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            .08f
                                    ),
                                    Color.White.copy(
                                        alpha =
                                            .025f
                                    ),
                                    Color.White.copy(
                                        alpha =
                                            .07f
                                    )
                                )
                        )
                )
            }

            XmoTheme.Dark -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.White.copy(
                                        alpha =
                                            .018f
                                    ),
                                    Color.Transparent,
                                    Color.Black.copy(
                                        alpha =
                                            .045f
                                    )
                                )
                        )
                )
            }

            XmoTheme.Amoled -> {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(
                                        alpha =
                                            .05f
                                    ),
                                    Color.Black.copy(
                                        alpha =
                                            .11f
                                    )
                                )
                        )
                )
            }
        }
    }
}

private fun brightenBackgroundColor(
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
