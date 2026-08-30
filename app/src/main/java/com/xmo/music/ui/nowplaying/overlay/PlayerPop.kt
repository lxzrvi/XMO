package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.XmoTheme
import com.xmo.music.ui.XmoFont

@Composable
internal fun XmoPop(
    message: String,
    theme: XmoTheme,
    modifier: Modifier = Modifier
) {
    val background =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .78f
                )

            XmoTheme.Dark ->
                Color(0xFF17181C)
                    .copy(
                        alpha = .80f
                    )

            XmoTheme.Amoled ->
                Color(0xFF101114)
                    .copy(
                        alpha = .84f
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

    val reveal =
        remember(message) {
            Animatable(0f)
        }

    LaunchedEffect(message) {
        reveal.snapTo(0f)

        reveal.animateTo(
            targetValue = 1f,
            animationSpec =
                XmoPlayerAnimation
                    .popRevealSpec
        )
    }

    Box(
        modifier =
            modifier
                .graphicsLayer {
                    with(
                        XmoPlayerAnimation
                    ) {
                        pop(
                            reveal.value
                        )
                    }
                }
                .clip(
                    RoundedCornerShape(
                        22.dp
                    )
                )
                .background(
                    background
                )
                .padding(
                    horizontal = 17.dp,
                    vertical = 11.dp
                )
    ) {
        Text(
            text = message,
            color =
                foreground,
            fontFamily =
                XmoFont.medium,
            fontSize =
                11.sp,
            textAlign =
                TextAlign.Center
        )
    }
}
