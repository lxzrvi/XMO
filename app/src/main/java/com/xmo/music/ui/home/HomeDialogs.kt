package com.xmo.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

@Composable
internal fun HomeMenuDialog(
    c: HomeColors,
    dismiss: () -> Unit,
    allSongs: () -> Unit,
    liked: () -> Unit,
    scanner: () -> Unit
) {
    HomeDialog(
        title = "XMO",
        c = c,
        dismiss = dismiss
    ) {
        HomeMenuItem(
            title = "All Songs",
            icon = Icons.Rounded.LibraryMusic,
            c = c,
            click = allSongs
        )

        HomeMenuItem(
            title = "Liked Songs",
            icon = Icons.Rounded.Favorite,
            c = c,
            click = liked
        )

        HomeMenuItem(
            title = "Scan Music",
            icon = Icons.Rounded.Refresh,
            c = c,
            click = scanner
        )
    }
}

@Composable
internal fun HomeScannerDialog(
    c: HomeColors,
    scanning: Boolean,
    songCount: Int,
    scan: () -> Unit,
    dismiss: () -> Unit
) {
    HomeDialog(
        title = if (scanning) {
            "Scanning music…"
        } else {
            "Scan local music"
        },
        c = c,
        dismiss = dismiss
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = LocalXmoAccent.current,
                modifier = Modifier.size(34.dp)
            )
        }

        Text(
            text = if (scanning) {
                "Reading Android MediaStore and local audio metadata…"
            } else {
                "$songCount songs currently available."
            },
            color = c.sub,
            fontFamily = XmoFont.normal,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(14.dp))

        HomeDialogAction(
            text = if (scanning) {
                "Scanning…"
            } else {
                "Scan Now"
            },
            enabled = !scanning,
            click = scan
        )
    }
}

@Composable
private fun HomeMenuItem(
    title: String,
    icon: ImageVector,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = click)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = LocalXmoAccent.current,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = title,
            color = c.text,
            fontFamily = XmoFont.medium,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 13.dp)
        )
    }
}

@Composable
internal fun HomeDialog(
    title: String,
    c: HomeColors,
    dismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .zIndex(1000f)
            .background(Color.Black.copy(alpha = .56f))
            .clickable(onClick = dismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .background(
                    c.surface,
                    RoundedCornerShape(24.dp)
                )
                .border(
                    .8.dp,
                    c.border,
                    RoundedCornerShape(24.dp)
                )
                .clickable {}
                .padding(18.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = c.text,
                    fontFamily = XmoFont.bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                Box(
                    Modifier
                        .size(30.dp)
                        .background(
                            c.button,
                            CircleShape
                        )
                        .clickable(onClick = dismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = c.sub,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            content()
        }
    }
}
