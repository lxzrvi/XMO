package com.xmo.music.ui

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.xmo.music.XmoTheme

typealias HomeColors = com.xmo.music.ui.home.HomeColors

fun homeColors(
    theme: XmoTheme
): HomeColors =
    com.xmo.music.ui.home.homeColors(theme)

@Composable
fun XmoIcon(
    @DrawableRes icon: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
