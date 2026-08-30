package com.xmo.music.ui.home

import androidx.compose.ui.graphics.Color
import com.xmo.music.XmoTheme

data class HomeColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val sub: Color,
    val button: Color,
    val icon: Color,
    val border: Color
)

internal data class HomeTopColors(
    val background: Color,
    val border: Color,
    val selector: Color,
    val selectorBorder: Color,
    val inactive: Color,
    val active: Color
)

fun homeColors(theme: XmoTheme): HomeColors =
    when (theme) {
        XmoTheme.Light -> HomeColors(
            bg = Color(0xFFF4F6F9),
            surface = Color.White,
            text = Color(0xFF121417),
            sub = Color(0xA6121417),
            button = Color.Black.copy(alpha = .055f),
            icon = Color(0xFF55575B),
            border = Color.Black.copy(alpha = .12f)
        )

        XmoTheme.Dark -> HomeColors(
            bg = Color(0xFF121212),
            surface = Color(0xFF181818),
            text = Color.White,
            sub = Color.White.copy(alpha = .65f),
            button = Color.White.copy(alpha = .09f),
            icon = Color(0xFFB8B8B8),
            border = Color.White.copy(alpha = .11f)
        )

        XmoTheme.Amoled -> HomeColors(
            bg = Color.Black,
            surface = Color(0xFF0B0B0B),
            text = Color.White,
            sub = Color.White.copy(alpha = .60f),
            button = Color.White.copy(alpha = .075f),
            icon = Color(0xFF999999),
            border = Color.White.copy(alpha = .19f)
        )
    }

internal fun homeTopColors(
    theme: XmoTheme
): HomeTopColors =
    when (theme) {
        XmoTheme.Light -> HomeTopColors(
            background =
                Color(0xFFF9F9FA)
                    .copy(alpha = .965f),
            border =
                Color.Black.copy(alpha = .085f),
            selector =
                Color(0xFFEAEAEC),
            selectorBorder =
                Color.Black.copy(alpha = .13f),
            inactive =
                Color.Black.copy(alpha = .46f),
            active =
                Color(0xFF161616)
        )

        XmoTheme.Dark -> HomeTopColors(
            background =
                Color(0xFF181819)
                    .copy(alpha = .965f),
            border =
                Color.White.copy(alpha = .10f),
            selector =
                Color(0xFF303031),
            selectorBorder =
                Color.White.copy(alpha = .155f),
            inactive =
                Color.White.copy(alpha = .42f),
            active =
                Color.White
        )

        XmoTheme.Amoled -> HomeTopColors(
            background =
                Color(0xFF080808)
                    .copy(alpha = .975f),
            border =
                Color.White.copy(alpha = .13f),
            selector =
                Color(0xFF292929),
            selectorBorder =
                Color.White.copy(alpha = .18f),
            inactive =
                Color.White.copy(alpha = .42f),
            active =
                Color.White
        )
    }
