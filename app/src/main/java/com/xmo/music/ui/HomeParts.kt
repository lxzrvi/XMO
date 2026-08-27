package com.xmo.music.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import kotlinx.coroutines.delay

val XmoRed = Color(0xFFFF3B3B)

data class HomeColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val sub: Color,
    val button: Color,
    val icon: Color
)

fun homeColors(theme: XmoTheme): HomeColors = when (theme) {
    XmoTheme.Dark -> HomeColors(
        Color(0xFF121212),
        Color(0xBF16161A),
        Color.White,
        Color.White.copy(.65f),
        Color.White.copy(.10f),
        Color(0xFFB3B3B3)
    )

    XmoTheme.Light -> HomeColors(
        Color(0xFFF4F6F9),
        Color.White,
        Color(0xFF121417),
        Color(0xA6121417),
        Color.Black.copy(.05f),
        Color(0xFF555555)
    )

    XmoTheme.Amoled -> HomeColors(
        Color.Black,
        Color(0xD90C0C0C),
        Color.White,
        Color.White.copy(.60f),
        Color.White.copy(.08f),
        Color(0xFF888888)
    )
}

@Composable
fun HomeHeader(
    c: HomeColors,
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    val subtitles = listOf(
        "What are you listening today?",
        "Mood for some chill music?",
        "Feel the beat & rhythm...",
        "Turn up the volume!"
    )
    var subtitle by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            subtitle = (subtitle + 1) % subtitles.size
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(XmoRed, Color(0xFF671E28))
                    )
                )
                .border(1.dp, Color.White.copy(.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "X",
                color = Color.White,
                fontFamily = XmoFont.bold
            )
        }

        Column(
            Modifier
                .padding(start = 10.dp)
                .weight(1f)
        ) {
            Text(
                "XMO User",
                color = c.text,
                fontFamily = XmoFont.user,
                fontSize = 18.sp
            )
            Text(
                subtitles[subtitle],
                color = c.sub,
                fontFamily = XmoFont.thin,
                fontSize = 11.sp
            )
        }

        IconButton(onClick = refresh) {
            Icon(Icons.Rounded.Refresh, null, tint = c.icon)
        }

        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Rounded.Menu, null, tint = c.icon)
            }

            DropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                containerColor = c.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                XmoTheme.entries.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (item) {
                                    XmoTheme.Dark -> "Dark Theme"
                                    XmoTheme.Light -> "Light Theme"
                                    XmoTheme.Amoled -> "AMOLED"
                                },
                                color = if (theme == item)
                                    XmoRed else c.text,
                                fontFamily = XmoFont.medium
                            )
                        },
                        onClick = {
                            setTheme(item)
                            menu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    text: String,
    active: Boolean,
    c: HomeColors,
    icon: String,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (active) XmoRed.copy(.18f)
                else c.button
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            icon,
            color = if (active) XmoRed else c.icon,
            fontFamily = XmoFont.bold,
            fontSize = 11.sp
        )
        Text(
            text,
            color = if (active) XmoRed else c.text,
            fontFamily = XmoFont.medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String,
    icon: String,
    c: HomeColors,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            icon,
            color = XmoRed,
            fontFamily = XmoFont.bold,
            fontSize = 15.sp
        )

        Column(
            Modifier
                .padding(start = 8.dp)
                .weight(1f)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    title,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 17.sp
                )
            }

            if (subtitle.isNotEmpty()) {
                Text(
                    subtitle,
                    color = c.sub,
                    fontFamily = XmoFont.thin,
                    fontSize = 10.sp
                )
            }
        }

        if (action != null) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(XmoRed.copy(.18f))
                    .clickable(onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    action,
                    color = XmoRed,
                    fontFamily = XmoFont.bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun SongTile(
    song: Song,
    index: Int,
    c: HomeColors
) {
    val palette = listOf(
        Color(0xFFFF3B3B),
        Color(0xFF007AFF),
        Color(0xFF34C759),
        Color(0xFFAF52DE),
        Color(0xFFFF9500),
        Color(0xFFFF2D55)
    )
    val art = palette[index % palette.size]

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(art.copy(.40f), c.surface)
                )
            )
            .padding(5.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(art.copy(.22f))
        ) {
            AsyncImage(
                model = song.artwork,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale =
                    androidx.compose.ui.layout.ContentScale.Crop
            )
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    song.title,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    song.artist,
                    color = c.sub,
                    fontFamily = XmoFont.thin,
                    fontSize = 8.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                "⋮",
                color = c.sub,
                fontSize = 14.sp
            )
        }
    }
}
