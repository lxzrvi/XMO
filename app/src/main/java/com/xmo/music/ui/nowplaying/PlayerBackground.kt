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
     * The neutral base owns overall readability.
     * Artwork color supplies atmosphere rather than becoming the
     * screen itself.
     */
    val base =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFE9EAED)

            XmoTheme.Dark ->
                Color(0xFF1B1C21)

            XmoTheme.Amoled ->
                Color(0xFF030304)
        }

    /*
     * Moderate extreme artwork colors before distributing them.
     *
     * Light stays colorful without becoming near-white.
     * Dark/AMOLED retain artwork hue without creating giant
     * luminous areas behind white controls.
     */
    val artwork =
        when (theme) {
            XmoTheme.Light ->
                mixColor(
                    from = dominant,
                    to = Color(0xFF777A80),
                    fraction = .20f
                )

            XmoTheme.Dark ->
                mixColor(
                    from = dominant,
                    to = Color(0xFF555860),
                    fraction = .24f
                )

            XmoTheme.Amoled ->
                mixColor(
                    from = dominant,
                    to = Color(0xFF3E4148),
                    fraction = .30f
                )
        }

    val artworkDeep =
        when (theme) {
            XmoTheme.Light ->
                mixColor(
                    from = deep,
                    to = artwork,
                    fraction = .62f
                )

            XmoTheme.Dark ->
                mixColor(
                    from = deep,
                    to = artwork,
                    fraction = .55f
                )

            XmoTheme.Amoled ->
                mixColor(
                    from = deep,
                    to = artwork,
                    fraction = .48f
                )
        }

    val soft =
        mixColor(
            from = artwork,
            to =
                when (theme) {
                    XmoTheme.Light ->
                        Color(0xFFD8DADE)

                    XmoTheme.Dark ->
                        Color(0xFF858891)

                    XmoTheme.Amoled ->
                        Color(0xFF686B73)
                },
            fraction =
                when (theme) {
                    XmoTheme.Light -> .18f
                    XmoTheme.Dark -> .14f
                    XmoTheme.Amoled -> .11f
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
         * Top artwork field.
         *
         * Very oversized radius prevents a visible "circle".
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                artwork.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .52f
                                            XmoTheme.Dark -> .42f
                                            XmoTheme.Amoled -> .31f
                                        }
                                ),
                            .40f to
                                artwork.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .32f
                                            XmoTheme.Dark -> .24f
                                            XmoTheme.Amoled -> .17f
                                        }
                                ),
                            .76f to
                                artwork.copy(
                                    alpha = .08f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .08f,
                            y =
                                size.height *
                                    .04f
                        ),
                    radius =
                        size.width *
                            1.55f
                )
        )

        /*
         * Opposite upper field breaks uniform dominant fill.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                artworkDeep.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .36f
                                            XmoTheme.Dark -> .35f
                                            XmoTheme.Amoled -> .25f
                                        }
                                ),
                            .48f to
                                artworkDeep.copy(
                                    alpha = .15f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    1.10f,
                            y =
                                size.height *
                                    .29f
                        ),
                    radius =
                        size.width *
                            1.42f
                )
        )

        /*
         * Broad center wash carries the artwork hue between the
         * upper artwork area and lower glass panel.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                soft.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .27f
                                            XmoTheme.Dark -> .24f
                                            XmoTheme.Amoled -> .16f
                                        }
                                ),
                            .52f to
                                artwork.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .15f
                                            XmoTheme.Dark -> .13f
                                            XmoTheme.Amoled -> .09f
                                        }
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                size.width *
                                    .48f,
                            y =
                                size.height *
                                    .52f
                        ),
                    radius =
                        size.width *
                            1.74f
                )
        )

        /*
         * Color remains visible through the translucent lower
         * panel on both sides.
         */
        drawRect(
            brush =
                Brush.radialGradient(
                    colorStops =
                        arrayOf(
                            0f to
                                artworkDeep.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .30f
                                            XmoTheme.Dark -> .28f
                                            XmoTheme.Amoled -> .20f
                                        }
                                ),
                            .58f to
                                artworkDeep.copy(
                                    alpha = .10f
                                ),
                            1f to
                                Color.Transparent
                        ),
                    center =
                        Offset(
                            x =
                                -size.width *
                                    .12f,
                            y =
                                size.height *
                                    .88f
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
                                artwork.copy(
                                    alpha =
                                        when (theme) {
                                            XmoTheme.Light -> .27f
                                            XmoTheme.Dark -> .25f
                                            XmoTheme.Amoled -> .17f
                                        }
                                ),
                            .60f to
                                artwork.copy(
                                    alpha = .09f
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
                            1.52f
                )
        )

        /*
         * Final theme veil keeps Light/Dark/AMOLED visually
         * distinct without hiding the artwork color.
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
                                            alpha = .04f
                                        ),
                                    .46f to
                                        Color.Transparent,
                                    1f to
                                        Color.White.copy(
                                            alpha = .06f
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
                                        Color.Black.copy(
                                            alpha = .025f
                                        ),
                                    .52f to
                                        Color.Transparent,
                                    1f to
                                        Color.Black.copy(
                                            alpha = .075f
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
                                            alpha = .08f
                                        ),
                                    .45f to
                                        Color.Transparent,
                                    1f to
                                        Color.Black.copy(
                                            alpha = .18f
                                        )
                                )
                        )
                )
            }
        }
    }
}
