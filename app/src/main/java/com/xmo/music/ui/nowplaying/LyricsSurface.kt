package com.xmo.music.ui.nowplaying

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Expand
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.X
import com.xmo.music.XmoTheme
import com.xmo.music.data.SongLyrics
import com.xmo.music.ui.HomeColors

@Composable
internal fun ArtworkLyrics(
    lyrics: SongLyrics?,
    position: Long,
    colors: HomeColors,
    accent: Color,
    theme: XmoTheme,
    pickLyrics: () -> Unit,
    fullscreenLyrics: () -> Unit,
    showArtwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    val surface =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .91f
                )

            XmoTheme.Dark ->
                Color(0xFF19191C)
                    .copy(
                        alpha = .91f
                    )

            XmoTheme.Amoled ->
                Color(0xFF080808)
                    .copy(
                        alpha = .94f
                    )
        }

    Box(
        modifier
            .clip(
                RoundedCornerShape(24.dp)
            )
            .background(surface)
    ) {
        FollowLyrics(
            lyrics = lyrics,
            position = position,
            colors = colors,
            accent = accent,
            fullscreen = false,
            modifier =
                Modifier.fillMaxSize()
        )

        XmoCapsule(
            background =
                colors.button,
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(10.dp)
        ) {
            CapsuleButton(
                size = 38.dp,
                onClick = pickLyrics
            ) {
                Icon(
                    imageVector =
                        Lucide.Plus,
                    contentDescription =
                        "Choose local lyrics",
                    tint = colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 38.dp,
                onClick =
                    fullscreenLyrics
            ) {
                Icon(
                    imageVector =
                        Lucide.Expand,
                    contentDescription =
                        "Fullscreen lyrics",
                    tint = colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 38.dp,
                onClick = showArtwork
            ) {
                Icon(
                    imageVector =
                        Lucide.X,
                    contentDescription =
                        "Return to artwork",
                    tint = colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }
        }
    }
}
