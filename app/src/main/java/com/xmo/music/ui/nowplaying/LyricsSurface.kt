package com.xmo.music.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val targetSurface =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .80f
                )

            XmoTheme.Dark ->
                Color(0xFF202126)
                    .copy(
                        alpha = .76f
                    )

            XmoTheme.Amoled ->
                Color(0xFF08090B)
                    .copy(
                        alpha = .82f
                    )
        }

    val surface by
        animateColorAsState(
            targetValue =
                targetSurface,
            animationSpec =
                tween(
                    durationMillis = 400
                ),
            label =
                "lyricsSurface"
        )

    Box(
        modifier =
            modifier
                .clip(
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .background(
                    surface
                )
    ) {
        /*
         * Controls float over the lyrics instead of consuming
         * list height. The list viewport centre therefore remains
         * exactly the artwork-card centre.
         */
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
                colors.button.copy(
                    alpha = .82f
                ),
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(
                        10.dp
                    )
        ) {
            CapsuleButton(
                size = 38.dp,
                onClick =
                    pickLyrics
            ) {
                Icon(
                    imageVector =
                        Lucide.Plus,
                    contentDescription =
                        "Choose local lyrics",
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
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
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }

            CapsuleButton(
                size = 38.dp,
                onClick =
                    showArtwork
            ) {
                Icon(
                    imageVector =
                        Lucide.X,
                    contentDescription =
                        "Show artwork",
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }
        }
    }
}
