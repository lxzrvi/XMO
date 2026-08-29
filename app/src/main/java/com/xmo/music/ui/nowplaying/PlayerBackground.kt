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
    val bottom =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(alpha = .20f)

            XmoTheme.Dark ->
                Color.Black.copy(alpha = .28f)

            XmoTheme.Amoled ->
                Color.Black.copy(alpha = .45f)
        }

    Canvas(
        Modifier
            .fillMaxSize()
            .background(dominant)
    ) {
        drawRect(
            Brush.radialGradient(
                colors = listOf(
                    dominant,
                    Color.Transparent
                ),
                center = Offset(
                    size.width * .10f,
                    size.height * .08f
                ),
                radius = size.width * 1.20f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors = listOf(
                    deep.copy(alpha = .50f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * .92f,
                    size.height * .30f
                ),
                radius = size.width * 1.15f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors = listOf(
                    dominant.copy(alpha = .76f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * .06f,
                    size.height * .62f
                ),
                radius = size.width * 1.22f
            )
        )

        drawRect(
            Brush.radialGradient(
                colors = listOf(
                    deep.copy(alpha = .54f),
                    Color.Transparent
                ),
                center = Offset(
                    size.width * .94f,
                    size.height * .83f
                ),
                radius = size.width * 1.25f
            )
        )

        drawRect(
            Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    bottom
                ),
                startY = size.height * .55f,
                endY = size.height
            )
        )
    }
}

internal fun liftArtworkColor(
    color: Color
): Color {
    val minimum = .22f

    val maximum =
        maxOf(
            color.red,
            color.green,
            color.blue
        )

    if (maximum >= minimum) {
        return color
    }

    if (maximum <= .001f) {
        return Color(0xFF4A4D55)
    }

    val multiplier =
        minimum / maximum

    return Color(
        red =
            (color.red * multiplier)
                .coerceIn(0f, 1f),
        green =
            (color.green * multiplier)
                .coerceIn(0f, 1f),
        blue =
            (color.blue * multiplier)
                .coerceIn(0f, 1f),
        alpha = 1f
    )
}

internal fun mixColor(
    from: Color,
    to: Color,
    fraction: Float
): Color {
    val f =
        fraction.coerceIn(0f, 1f)

    return Color(
        red =
            from.red +
                (to.red - from.red) * f,
        green =
            from.green +
                (to.green - from.green) * f,
        blue =
            from.blue +
                (to.blue - from.blue) * f,
        alpha = 1f
    )
}
