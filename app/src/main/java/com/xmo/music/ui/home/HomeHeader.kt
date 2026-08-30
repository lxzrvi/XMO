package com.xmo.music.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.xmo.music.XmoTheme
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.LocalXmoProfile
import com.xmo.music.ui.XmoFont
import com.xmo.music.ui.XmoProfileAvatar
import kotlinx.coroutines.delay

@Composable
internal fun HomeHeader(
    c: HomeColors,
    theme: XmoTheme,
    refresh: () -> Unit,
    openMenu: () -> Unit,
    openProfile: () -> Unit
) {
    val profile = LocalXmoProfile.current
    val accent = LocalXmoAccent.current
    val top = homeTopColors(theme)

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
            subtitle = (subtitle + 1) % subtitles.size
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(top.background)
            .border(
                .7.dp,
                top.border,
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    top = 7.dp,
                    end = 10.dp,
                    bottom = 7.dp
                ),
            verticalAlignment = Alignment.CenterVertically
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
                border = top.border
            )

            Column(
                Modifier
                    .padding(start = 10.dp)
                    .weight(1f)
            ) {
                Text(
                    text = profile.name.ifBlank { "XMO User" },
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

            Row(
                Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(top.selector)
                    .border(
                        .7.dp,
                        top.selectorBorder,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = refresh,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Scan music",
                        tint = top.inactive,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Box(
                    Modifier
                        .width(.6.dp)
                        .height(18.dp)
                        .background(top.border)
                )

                IconButton(
                    onClick = openMenu,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Menu",
                        tint = top.active,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
