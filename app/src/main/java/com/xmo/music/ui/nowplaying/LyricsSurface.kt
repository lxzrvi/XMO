package com.xmo.music.ui.nowplaying

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
     * Surface and toolbar have their own contrast pair.
     *
     * They do not reuse Home button/text colors, removing
     * white-on-white and dark-on-dark combinations.
     */
    val surfaceTarget =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFFF6F7F9)
                    .copy(
                        alpha = .88f
                    )

            XmoTheme.Dark ->
                Color(0xFF1C1E23)
                    .copy(
                        alpha = .88f
                    )

            XmoTheme.Amoled ->
                Color(0xFF08090B)
                    .copy(
                        alpha = .91f
                    )
        }

    val foregroundTarget =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF17181C)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    val toolbarTarget =
        when (theme) {
            XmoTheme.Light ->
                Color.Black.copy(
                    alpha = .075f
                )

            XmoTheme.Dark ->
                Color.White.copy(
                    alpha = .11f
                )

            XmoTheme.Amoled ->
                Color.White.copy(
                    alpha = .12f
                )
        }

    val surface by
        animateColorAsState(
            targetValue =
                surfaceTarget,
            animationSpec =
                tween(
                    320
                ),
            label =
                "lyricsSurface"
        )

    val foreground by
        animateColorAsState(
            targetValue =
                foregroundTarget,
            animationSpec =
                tween(
                    320
                ),
            label =
                "lyricsForeground"
        )

    val lyricColors =
        HomeColors(
            bg =
                Color.Transparent,
            surface =
                Color.Transparent,
            text =
                foreground,
            sub =
                foreground.copy(
                    alpha = .55f
                ),
            button =
                toolbarTarget,
            icon =
                foreground,
            border =
                foreground.copy(
                    alpha = .12f
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
            lyrics =
                lyrics,
            position =
                position,
            colors =
                lyricColors,
            accent =
                accent,
            fullscreen =
                false,
            modifier =
                Modifier.fillMaxSize()
        )

        XmoCapsule(
            background =
                toolbarTarget,
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
                size =
                    38.dp,
                onClick =
                    pickLyrics
            ) {
                LyricsAddIcon(
                    color =
                        foreground,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }

            CapsuleButton(
                size =
                    38.dp,
                onClick =
                    fullscreenLyrics
            ) {
                LyricsExpandIcon(
                    color =
                        foreground,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }

            CapsuleButton(
                size =
                    38.dp,
                onClick =
                    showArtwork
            ) {
                LyricsCloseIcon(
                    color =
                        foreground,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun LyricsAddIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val stroke =
            size.minDimension *
                .12f

        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .50f,
                    size.height *
                        .20f
                ),
            end =
                Offset(
                    size.width *
                        .50f,
                    size.height *
                        .80f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )

        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .20f,
                    size.height *
                        .50f
                ),
            end =
                Offset(
                    size.width *
                        .80f,
                    size.height *
                        .50f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )
    }
}

@Composable
private fun LyricsExpandIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val stroke =
            size.minDimension *
                .105f

        val style =
            Stroke(
                width =
                    stroke,
                cap =
                    StrokeCap.Round
            )

        val first =
            Path().apply {
                moveTo(
                    size.width *
                        .18f,
                    size.height *
                        .39f
                )

                lineTo(
                    size.width *
                        .18f,
                    size.height *
                        .18f
                )

                lineTo(
                    size.width *
                        .39f,
                    size.height *
                        .18f
                )
            }

        drawPath(
            path =
                first,
            color =
                color,
            style =
                style
        )

        val second =
            Path().apply {
                moveTo(
                    size.width *
                        .61f,
                    size.height *
                        .18f
                )

                lineTo(
                    size.width *
                        .82f,
                    size.height *
                        .18f
                )

                lineTo(
                    size.width *
                        .82f,
                    size.height *
                        .39f
                )
            }

        drawPath(
            path =
                second,
            color =
                color,
            style =
                style
        )

        val third =
            Path().apply {
                moveTo(
                    size.width *
                        .82f,
                    size.height *
                        .61f
                )

                lineTo(
                    size.width *
                        .82f,
                    size.height *
                        .82f
                )

                lineTo(
                    size.width *
                        .61f,
                    size.height *
                        .82f
                )
            }

        drawPath(
            path =
                third,
            color =
                color,
            style =
                style
        )

        val fourth =
            Path().apply {
                moveTo(
                    size.width *
                        .39f,
                    size.height *
                        .82f
                )

                lineTo(
                    size.width *
                        .18f,
                    size.height *
                        .82f
                )

                lineTo(
                    size.width *
                        .18f,
                    size.height *
                        .61f
                )
            }

        drawPath(
            path =
                fourth,
            color =
                color,
            style =
                style
        )
    }
}

@Composable
private fun LyricsCloseIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(
        modifier
    ) {
        val stroke =
            size.minDimension *
                .115f

        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .24f,
                    size.height *
                        .24f
                ),
            end =
                Offset(
                    size.width *
                        .76f,
                    size.height *
                        .76f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )

        drawLine(
            color =
                color,
            start =
                Offset(
                    size.width *
                        .76f,
                    size.height *
                        .24f
                ),
            end =
                Offset(
                    size.width *
                        .24f,
                    size.height *
                        .76f
                ),
            strokeWidth =
                stroke,
            cap =
                StrokeCap.Round
        )
    }
}
