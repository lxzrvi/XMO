package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun OverlayAction(
    icon: ImageVector,
    title: String,
    colors: HomeColors,
    trailing: String? = null,
    active: Boolean = false,
    enabled: Boolean = true,
    click: () -> Unit = {}
) {
    val accent =
        LocalXmoAccent.current

    PressButton(
        enabled = enabled,
        onClick = click,
        modifier =
            Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        13.dp
                    )
                )
                .padding(
                    horizontal = 7.dp,
                    vertical = 10.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint =
                    when {
                        !enabled ->
                            colors.sub.copy(
                                alpha = .55f
                            )

                        active ->
                            accent

                        else ->
                            colors.icon
                    },
                modifier =
                    Modifier.size(18.dp)
            )

            Text(
                text = title,
                color =
                    when {
                        !enabled ->
                            colors.sub.copy(
                                alpha = .62f
                            )

                        active ->
                            accent

                        else ->
                            colors.text
                    },
                fontFamily =
                    XmoFont.medium,
                fontSize = 11.sp,
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(
                            start = 12.dp
                        )
            )

            trailing?.let {
                Text(
                    text = it,
                    color =
                        if (active) {
                            accent
                        } else {
                            colors.sub
                        },
                    fontFamily =
                        XmoFont.normal,
                    fontSize = 9.sp
                )
            }
        }
    }
}
