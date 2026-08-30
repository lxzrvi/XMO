package com.xmo.music.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeMenuDialog(
    c: HomeColors,
    dismiss: () -> Unit,
    liked: () -> Unit,
    categories: () -> Unit,
    scanner: () -> Unit
) {
    XmoBox(
        title = "XMO",
        c = c,
        dismiss = dismiss
    ) {
        MenuItem(
            title = "Liked Songs",
            icon = Icons.Rounded.Favorite,
            c = c,
            click = liked
        )

        MenuItem(
            title = "Categories",
            icon = Icons.Rounded.Category,
            c = c,
            click = categories
        )

        MenuItem(
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
    XmoBox(
        title =
            if (scanning) {
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
                .height(70.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = null,
                tint = LocalXmoAccent.current,
                modifier = Modifier.size(32.dp)
            )
        }

        Text(
            text =
                if (scanning) {
                    "Reading local music…"
                } else {
                    "$songCount songs currently available."
                },
            color = c.sub,
            fontFamily = XmoFont.normal,
            fontSize = 11.sp
        )

        Spacer(Modifier.height(14.dp))

        HomeDialogAction(
            text =
                if (scanning) {
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
private fun MenuItem(
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
        verticalAlignment =
            Alignment.CenterVertically
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
