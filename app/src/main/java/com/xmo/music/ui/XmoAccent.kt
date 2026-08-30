package com.xmo.music.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import com.xmo.music.data.AccentMode
import com.xmo.music.data.XmoAppearance

val XmoRed = Color(0xFFFF3B3B)
val XmoBlue = Color(0xFF398CFF)

val LocalXmoAccent = compositionLocalOf {
    XmoRed
}

@Composable
fun ProvideXmoAccent(
    appearance: XmoAppearance,
    content: @Composable () -> Unit
) {
    val accent = when (appearance.accentMode) {
        AccentMode.Red -> XmoRed
        AccentMode.Blue -> XmoBlue

        AccentMode.Custom -> {
            Color(
                appearance.customAccent.argb
                    .toULong()
                    .toLong()
            ).copy(
                alpha = appearance.customAccent.alpha.coerceIn(0f, 1f)
            )
        }
    }

    CompositionLocalProvider(
        LocalXmoAccent provides accent,
        content = content
    )
}
