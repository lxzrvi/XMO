package com.xmo.music.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.xmo.music.data.XmoProfile

val LocalXmoProfile =
    staticCompositionLocalOf {
        XmoProfile()
    }
