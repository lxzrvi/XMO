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
    val base =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF1F3F6)

            XmoTheme.Dark ->
                Color(0xFF151518)

            XmoTheme.Amoled ->
                Color.Black
        }

    val artworkStrength =
        when (theme) {
            XmoTheme.Light ->
                .19f

            XmoTheme.Dark ->
                .22f

            XmoTheme.Amoled ->
                .18f
        }

    val secondaryStrength =
        artworkStrength * .72f

    Canvas(
        Modifier
            .fillMaxSize()
            .background(base)
    ) {
        /*
         * Wide, low-opacity color fields. No solid dominant fill.
         */
        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant.copy(
                            alpha =
                                artworkStrength
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .12f,
                        size.height * .13f
                    ),
                radius =
                    size.width * 1.18f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        deep.copy(
                            alpha =
                                secondaryStrength
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .88f,
                        size.height * .28f
                    ),
                radius =
                    size.width * 1.22f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        dominant.copy(
                            alpha =
                                artworkStrength *
                                    .72f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .17f,
                        size.height * .63f
                    ),
                radius =
                    size.width * 1.25f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors =
                    listOf(
                        deep.copy(
                            alpha =
                                secondaryStrength *
                                    .75f
                        ),
                        Color.Transparent
                    ),
                center =
                    Offset(
                        size.width * .86f,
                        size.height * .83f
                    ),
                radius =
                    size.width * 1.28f
            )
        )

        /*
         * Tiny neutral integration layer keeps all four fields
         * visually joined rather than looking like circles.
         */
        drawRect(
            Brush.verticalGradient(
                colors =
                    when (theme) {
                        XmoTheme.Light ->
                            listOf(
                                Color.White.copy(
                                    alpha = .025f
                                ),
                                Color.White.copy(
                                    alpha = .10f
                                )
                            )

                        XmoTheme.Dark ->
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = .08f
                                )
                            )

                        XmoTheme.Amoled ->
                            listOf(
                                Color.Transparent,
                                Color.Black.copy(
                                    alpha = .20f
                                )
                            )
                    }
            )
        )
    }
}
