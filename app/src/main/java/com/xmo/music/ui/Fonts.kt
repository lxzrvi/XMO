package com.xmo.music.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.fontResource
import com.xmo.music.R

object XmoFont {
    val logo @Composable get() = fontResource(R.font.xmo_logo_text)
    val user @Composable get() = fontResource(R.font.xmo_user_text)
    val bold @Composable get() = fontResource(R.font.xmo_bold)
    val normal @Composable get() = fontResource(R.font.xmo_normal)
    val medium @Composable get() = fontResource(R.font.xmo_medium)
    val thin @Composable get() = fontResource(R.font.xmo_thin)
}
