package com.xmo.music.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun XmoIcon(
    @DrawableRes icon: Int,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
internal fun SectionTitle(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    c: HomeColors,
    modifier: Modifier = Modifier
) {
    val accent = LocalXmoAccent.current

    Row(
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XmoIcon(
            icon = icon,
            tint = accent,
            modifier = Modifier.size(18.dp)
        )

        Column(
            Modifier
                .padding(start = 9.dp)
                .weight(1f)
        ) {
            Text(
                text = title,
                color = c.text,
                fontFamily = XmoFont.bold,
                fontSize = 17.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (subtitle.isNotBlank()) {
                Text(
                    text = subtitle,
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun HomeCircleAdd(
    click: () -> Unit
) {
    val accent = LocalXmoAccent.current

    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = .16f))
            .border(
                .6.dp,
                accent.copy(alpha = .32f),
                CircleShape
            )
            .clickable(onClick = click),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Open",
            tint = accent,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
internal fun HomeEmpty(
    text: String,
    c: HomeColors
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(82.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = c.sub,
            fontFamily = XmoFont.normal,
            fontSize = 12.sp
        )
    }
}

@Composable
internal fun HomeDialogAction(
    text: String,
    enabled: Boolean = true,
    click: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(45.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                LocalXmoAccent.current.copy(
                    alpha = if (enabled) 1f else .25f
                )
            )
            .clickable(
                enabled = enabled,
                onClick = click
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White.copy(
                alpha = if (enabled) 1f else .45f
            ),
            fontFamily = XmoFont.medium,
            fontSize = 11.sp
        )
    }
}
