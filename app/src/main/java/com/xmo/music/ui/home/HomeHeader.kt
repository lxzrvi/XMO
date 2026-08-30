package com.xmo.music.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.LocalXmoProfile
import com.xmo.music.ui.XmoFont
import com.xmo.music.ui.XmoProfileAvatar
import kotlinx.coroutines.delay

@Composable
internal fun HomeHeader(
    c: HomeColors,
    refresh: () -> Unit,
    openMenu: () -> Unit,
    openProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = LocalXmoProfile.current
    val accent = LocalXmoAccent.current

    val subtitles = remember {
        listOf(
            "What are you listening today?",
            "Mood for some chill music?",
            "Feel the beat & rhythm...",
            "Turn up the volume!"
        )
    }

    var subtitle by remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4500L)
            subtitle =
                (subtitle + 1) % subtitles.size
        }
    }

    Row(
        modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 9.dp,
                end = 8.dp,
                bottom = 9.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoProfileAvatar(
            profile = profile,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .combinedClickable(
                    onClick = openProfile,
                    onLongClick = openProfile
                ),
            background = accent,
            border = c.border
        )

        Column(
            Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                text =
                    profile.name.ifBlank {
                        "XMO User"
                    },
                color = c.text,
                fontFamily = XmoFont.user,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedContent(
                targetState = subtitle,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith
                        fadeOut(tween(180))
                },
                label = "homeSubtitle"
            ) { index ->
                Text(
                    text = subtitles[index],
                    color = c.sub,
                    fontFamily = XmoFont.normal,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = refresh,
            modifier = Modifier.size(45.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Scan",
                tint = c.icon,
                modifier = Modifier.size(23.dp)
            )
        }

        IconButton(
            onClick = openMenu,
            modifier = Modifier.size(45.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Menu",
                tint = c.text,
                modifier = Modifier.size(25.dp)
            )
        }
    }
}
