package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
    /*
     * Same transparent glass language as the lower player panel.
     */
    val surface =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .50f
                )

            XmoTheme.Dark ->
                Color(0xFF17191E)
                    .copy(
                        alpha = .48f
                    )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .54f
                )
        }

    val foreground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF15161A)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    val toolbar =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .58f
                )

            XmoTheme.Dark ->
                Color.Black.copy(
                    alpha = .32f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .11f
                )
        }

    val lyricColors =
        HomeColors(
            bg = Color.Transparent,
            surface = Color.Transparent,
            text = foreground,
            sub =
                foreground.copy(
                    alpha = .58f
                ),
            button = toolbar,
            icon = foreground,
            border =
                foreground.copy(
                    alpha = .14f
                )
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
        FollowLyrics(
            lyrics = lyrics,
            position = position,
            colors = lyricColors,
            accent = accent,
            fullscreen = false,
            modifier =
                Modifier.fillMaxSize(),
            pickLyrics =
                pickLyrics
        )

        /*
         * Keep toolbar available even when lyrics are missing.
         */
        XmoCapsule(
            background = toolbar,
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(10.dp)
        ) {
            CapsuleButton(
                size = 38.dp,
                onClick =
                    pickLyrics
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Add,
                    contentDescription =
                        "Add local lyrics",
                    tint = foreground,
                    modifier =
                        Modifier.size(
                            21.dp
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
                        Icons.Rounded.Fullscreen,
                    contentDescription =
                        "Fullscreen lyrics",
                    tint = foreground,
                    modifier =
                        Modifier.size(
                            22.dp
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
                        Icons.Rounded.Close,
                    contentDescription =
                        "Show artwork",
                    tint = foreground,
                    modifier =
                        Modifier.size(
                            21.dp
                        )
                )
            }
        }
    }
}
