package com.xmo.music.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.R
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
    val icon: Color,
    val border: Color
)

fun homeColors(theme: XmoTheme) = when (theme) {
    XmoTheme.Dark -> HomeColors(
        Color(0xFF121212),
        Color(0xFF181818),
        Color.White,
        Color.White.copy(.65f),
        Color.White.copy(.09f),
        Color(0xFFB8B8B8),
        Color.White.copy(.11f)
    )

    XmoTheme.Light -> HomeColors(
        Color(0xFFF4F6F9),
        Color.White,
        Color(0xFF121417),
        Color(0xA6121417),
        Color.Black.copy(.055f),
        Color(0xFF55575B),
        Color.Black.copy(.12f)
    )

    XmoTheme.Amoled -> HomeColors(
        Color.Black,
        Color(0xFF0B0B0B),
        Color.White,
        Color.White.copy(.60f),
        Color.White.copy(.075f),
        Color(0xFF999999),
        Color.White.copy(.19f)
    )
}

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
fun HomeHeader(
    c: HomeColors,
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit
) {
    val subtitles = listOf(
        "What are you listening today?",
        "Mood for some chill music?",
        "Feel the beat & rhythm...",
        "Turn up the volume!"
    )

    var subtitle by remember { mutableIntStateOf(0) }
    var menu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(4500)
            subtitle = (subtitle + 1) % subtitles.size
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = 16.dp,
                top = 8.dp,
                end = 10.dp,
                bottom = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            XmoRed,
                            Color(0xFF641E27)
                        )
                    )
                )
                .border(.6.dp, c.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "X",
                color = Color.White,
                fontFamily = XmoFont.logo,
                fontSize = 15.sp
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

            AnimatedContent(
                targetState = subtitle,
                transitionSpec = {
                    fadeIn(tween(220)) togetherWith
                        fadeOut(tween(180))
                },
                label = "subtitle"
            ) { index ->
                Text(
                    subtitles[index],
                    color = c.sub,
                    fontFamily = XmoFont.thin,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }

        IconButton(
            onClick = refresh,
            modifier = Modifier.size(42.dp)
        ) {
            XmoIcon(
                R.drawable.ic_xmo_refresh,
                c.icon,
                Modifier.size(20.dp)
            )
        }

        Box {
            IconButton(
                onClick = { menu = true },
                modifier = Modifier.size(42.dp)
            ) {
                XmoIcon(
                    R.drawable.ic_xmo_menu,
                    c.icon,
                    Modifier.size(21.dp)
                )
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
                                color = if (item == theme)
                                    XmoRed else c.text,
                                fontFamily = XmoFont.medium,
                                fontSize = 13.sp
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
    @DrawableRes icon: Int,
    tint: Color = c.icon,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (active) XmoRed.copy(.18f)
                else c.button
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 13.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        XmoIcon(
            icon,
            if (active) XmoRed else tint,
            Modifier.size(14.dp)
        )

        Text(
            text,
            color = if (active) XmoRed else c.text,
            fontFamily = XmoFont.medium,
            fontSize = 12.sp,
            maxLines = 1
        )
    }
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    c: HomeColors,
    modifier: Modifier = Modifier,
    action: Int? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        XmoIcon(
            icon,
            XmoRed,
            Modifier.size(17.dp)
        )

        Column(
            Modifier
                .padding(start = 8.dp)
                .weight(1f)
        ) {
            Text(
                title,
                color = c.text,
                fontFamily = XmoFont.bold,
                fontSize = 17.sp
            )

            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    color = c.sub,
                    fontFamily = XmoFont.thin,
                    fontSize = 10.sp
                )
            }
        }

        action?.let {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(XmoRed.copy(.18f))
                    .clickable(onClick = onAction),
                contentAlignment = Alignment.Center
            ) {
                XmoIcon(
                    it,
                    XmoRed,
                    Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SongTile(
    song: Song,
    index: Int,
    c: HomeColors,
    theme: XmoTheme,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var dominant by remember(song.artwork) {
        mutableStateOf(Color(0xFF35353A))
    }

    LaunchedEffect(song.artwork) {
        dominant = Artwork.color(context, song.artwork)
    }

    val alpha = when (theme) {
        XmoTheme.Light -> .35f
        XmoTheme.Dark -> .40f
        XmoTheme.Amoled -> .45f
    }

    Column(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        dominant.copy(alpha),
                        Artwork.end(dominant, theme)
                    )
                )
            )
            .border(
                .55.dp,
                c.border,
                RoundedCornerShape(10.dp)
            )
            .padding(5.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(dominant.copy(.15f))
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

            XmoIcon(
                R.drawable.ic_xmo_more,
                c.sub,
                Modifier.size(14.dp)
            )
        }
    }
}
