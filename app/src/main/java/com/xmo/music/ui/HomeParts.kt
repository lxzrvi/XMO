package com.xmo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song

val XmoRed = Color(0xFFFF3B3B)

data class HomeColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val sub: Color,
    val button: Color,
    val inactive: Color
)

fun XmoTheme.colors() = when (this) {
    XmoTheme.Dark -> HomeColors(
        Color(0xFF121212), Color(0xBF16161A), Color.White,
        Color.White.copy(.65f), Color.White.copy(.10f), Color(0xFFB3B3B3)
    )
    XmoTheme.Light -> HomeColors(
        Color(0xFFF4F6F9), Color.White, Color(0xFF121417),
        Color(0xA6121417), Color.Black.copy(.05f), Color(0xFF555555)
    )
    XmoTheme.Amoled -> HomeColors(
        Color.Black, Color(0xD90C0C0C), Color.White,
        Color.White.copy(.60f), Color.White.copy(.08f), Color(0xFF888888)
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
                fontFamily = XmoFont.bold,
                fontSize = 17.sp
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
                fontSize = 18.sp,
                maxLines = 1
            )
            Text(
                "What are you listening today?",
                color = c.sub,
                fontFamily = XmoFont.thin,
                fontSize = 11.sp
            )
        }

        IconButton(onClick = refresh) {
            Icon(Icons.Rounded.Refresh, null, tint = c.inactive)
        }

        Box {
            IconButton(onClick = { menu = true }) {
                Icon(Icons.Rounded.Menu, null, tint = c.inactive)
            }

            DropdownMenu(
                expanded = menu,
                onDismissRequest = { menu = false },
                containerColor = c.surface,
                shape = RoundedCornerShape(12.dp)
            ) {
                XmoTheme.entries.forEach {
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (it) {
                                    XmoTheme.Dark -> "Dark Theme"
                                    XmoTheme.Light -> "Light Theme"
                                    XmoTheme.Amoled -> "AMOLED"
                                },
                                color = if (theme == it) XmoRed else c.text,
                                fontFamily = XmoFont.medium,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            setTheme(it)
                            menu = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Categories(c: HomeColors) {
    var selected by remember { mutableIntStateOf(0) }

    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf(
            "All" to Icons.Rounded.Dashboard,
            "All Songs" to Icons.Rounded.MusicNote,
            "Albums" to Icons.Rounded.Album,
            "Liked Songs" to Icons.Rounded.Favorite,
            "Artists" to Icons.Rounded.Person
        ).forEachIndexed { i, item ->
            val active = selected == i

            Row(
                Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        if (active) XmoRed.copy(.18f)
                        else c.button
                    )
                    .clickable { selected = i }
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    item.second,
                    null,
                    tint = if (active) XmoRed else c.inactive,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    item.first,
                    color = if (active) XmoRed else c.text,
                    fontFamily = XmoFont.medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String,
    icon: ImageVector,
    c: HomeColors
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = XmoRed,
            modifier = Modifier.size(17.dp)
        )
        Column(Modifier.padding(start = 7.dp)) {
            Text(
                title,
                color = c.text,
                fontFamily = XmoFont.bold,
                fontSize = 17.sp
            )
            Text(
                subtitle,
                color = c.sub,
                fontFamily = XmoFont.thin,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun SongTile(song: Song, index: Int, c: HomeColors) {
    val colors = listOf(
        Color(0xFFFF3B3B), Color(0xFF007AFF),
        Color(0xFF34C759), Color(0xFFAF52DE),
        Color(0xFFFF9500), Color(0xFFFF2D55)
    )
    val art = colors[index % colors.size]

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(art.copy(.42f), c.surface)
                )
            )
            .padding(5.dp)
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

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

            Icon(
                Icons.Rounded.MoreVert,
                null,
                tint = c.sub,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
