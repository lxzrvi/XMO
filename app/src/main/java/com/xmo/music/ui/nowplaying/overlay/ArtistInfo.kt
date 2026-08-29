package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.X
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont

@Composable
internal fun ArtistInfoBox(
    artist: String,
    trackCount: Int,
    colors: HomeColors,
    close: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .30f
                )
            )
            .clickable(
                onClick = close
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 32.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(18.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    artist.ifBlank {
                        "Unknown artist"
                    },
                color = colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 19.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                Modifier.height(7.dp)
            )

            Text(
                text =
                    if (trackCount == 1) {
                        "1 track on this device"
                    } else {
                        "$trackCount tracks on this device"
                    },
                color = colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 11.sp
            )

            Spacer(
                Modifier.height(15.dp)
            )

            PremiumCircle(
                size = 39.dp,
                background =
                    colors.button,
                onClick = close
            ) {
                Icon(
                    imageVector =
                        Lucide.X,
                    contentDescription =
                        "Close",
                    tint = colors.text,
                    modifier =
                        Modifier.size(18.dp)
                )
            }
        }
    }
}
