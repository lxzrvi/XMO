package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                    alpha = .94f
                )

            XmoTheme.Dark ->
                Color(0xFF1C1C1E)
                    .copy(alpha = .94f)

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .96f
                )
        }

    val foreground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF151515)

            else ->
                Color.White
        }

    Box(
        modifier
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(background)
            .padding(
                horizontal = 17.dp,
                vertical = 11.dp
            )
    ) {
        Text(
            text = message,
            color = foreground,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
            textAlign =
                TextAlign.Center
        )
    }
}
