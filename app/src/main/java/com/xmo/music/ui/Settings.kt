package com.xmo.music.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.XmoTheme
import androidx.compose.ui.draw.clip

@Composable
fun Settings(
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    rescan: () -> Unit
) {
    val c =
        homeColors(theme)

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),
        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 14.dp,
                bottom = 190.dp
            ),
        verticalArrangement =
            Arrangement.spacedBy(
                18.dp
            )
    ) {
        item {
            Text(
                "Settings",
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 20.sp,
                modifier =
                    Modifier.padding(
                        vertical = 8.dp
                    )
            )
        }

        item {
            SettingsGroup(
                "LOOK & FEEL",
                c
            ) {
                SettingRow(
                    title =
                        "App Accent Color",
                    subtitle =
                        "XMO interface accent",
                    value =
                        "XMO Red",
                    c = c,
                    accent = true
                )

                ThemeSetting(
                    theme,
                    setTheme,
                    c
                )

                SettingRow(
                    title =
                        "Player Screen Style",
                    subtitle =
                        "Current Now Playing layout",
                    value =
                        "Classic Card",
                    c = c
                )
            }
        }

        item {
            SettingsGroup(
                "AUDIO PROCESSING",
                c
            ) {
                ComingRow(
                    "Built-in Equalizer",
                    "10-band EQ, bass and loudness",
                    c
                )

                ComingRow(
                    "Playback Speed & Pitch",
                    "Tempo and pitch controls",
                    c
                )

                ComingRow(
                    "Gapless Playback",
                    "Configurable seamless transitions",
                    c
                )

                ComingRow(
                    "Crossfade Songs",
                    "Fade transition between tracks",
                    c
                )

                ComingRow(
                    "ReplayGain Normalization",
                    "Volume normalization",
                    c
                )
            }
        }

        item {
            SettingsGroup(
                "MEDIA LIBRARY & STORAGE",
                c
            ) {
                SettingRow(
                    title =
                        "Rescan Storage Now",
                    subtitle =
                        "Refresh local MediaStore library",
                    value =
                        "Scan",
                    c = c,
                    enabled = true,
                    click = rescan
                )

                ComingRow(
                    "Ignore Short Audio",
                    "Minimum duration library filter",
                    c
                )

                SettingRow(
                    title =
                        "Artwork Source",
                    subtitle =
                        "Embedded and local artwork only",
                    value =
                        "Local",
                    c = c
                )

                ComingRow(
                    "Excluded Directories",
                    "Folders ignored by library scan",
                    c
                )
            }
        }

        item {
            SettingsGroup(
                "HARDWARE & GESTURES",
                c
            ) {
                ComingRow(
                    "Shake to Skip Track",
                    "Shake phone to play next",
                    c
                )

                ComingRow(
                    "Resume on Headphone Plug",
                    "Resume on Bluetooth/AUX connect",
                    c
                )

                SettingRow(
                    title =
                        "Pause on Headphone Unplug",
                    subtitle =
                        "Handled by Media3 audio-becoming-noisy",
                    value =
                        "Active",
                    c = c,
                    accent = true
                )

                ComingRow(
                    "Sleep Timer",
                    "Stop playback after selected time",
                    c
                )
            }
        }

        item {
            SettingsGroup(
                "BACKUP & RESTORE",
                c
            ) {
                ComingRow(
                    "Backup Library & Settings",
                    "Export XMO application data",
                    c
                )

                ComingRow(
                    "Restore Data",
                    "Import XMO application data",
                    c
                )

                ComingRow(
                    "Auto Scheduled Backup",
                    "Automatic local backups",
                    c
                )
            }
        }

        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 60.dp,
                        bottom = 30.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "XMO",
                    color = c.text,
                    fontFamily =
                        XmoFont.logo,
                    fontSize = 19.sp
                )

                Text(
                    "lxzrvi • copyright © 2026",
                    color = c.sub,
                    fontFamily =
                        XmoFont.thin,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    label: String,
    c: HomeColors,
    content:
        @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                c.surface,
                RoundedCornerShape(
                    16.dp
                )
            )
            .border(
                .7.dp,
                c.border,
                RoundedCornerShape(
                    16.dp
                )
            )
            .padding(
                horizontal = 12.dp,
                vertical = 14.dp
            )
    ) {
        Text(
            label,
            color = XmoRed,
            fontFamily =
                XmoFont.bold,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp,
            modifier =
                Modifier.padding(
                    start = 6.dp,
                    bottom = 8.dp
                )
        )

        content()
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    value: String,
    c: HomeColors,
    accent: Boolean = false,
    enabled: Boolean = false,
    click: () -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    10.dp
                )
            )
            .clickable(
                enabled = enabled,
                onClick = click
            )
            .padding(
                horizontal = 8.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .background(
                    c.button,
                    RoundedCornerShape(
                        10.dp
                    )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Box(
                Modifier
                    .size(10.dp)
                    .background(
                        if (accent)
                            XmoRed
                        else
                            c.icon,
                        CircleShape
                    )
            )
        }

        Column(
            Modifier
                .weight(1f)
                .padding(
                    start = 14.dp,
                    end = 10.dp
                )
        ) {
            Text(
                title,
                color = c.text,
                fontFamily =
                    XmoFont.medium,
                fontSize = 13.sp
            )

            Text(
                subtitle,
                color = c.sub,
                fontFamily =
                    XmoFont.thin,
                fontSize = 10.sp
            )
        }

        Text(
            value,
            color =
                if (accent)
                    XmoRed
                else
                    c.sub,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ComingRow(
    title: String,
    subtitle: String,
    c: HomeColors
) {
    SettingRow(
        title = title,
        subtitle = subtitle,
        value = "Coming later",
        c = c
    )
}

@Composable
private fun ThemeSetting(
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    c: HomeColors
) {
    Column(
        Modifier.padding(
            horizontal = 8.dp,
            vertical = 9.dp
        )
    ) {
        Text(
            "Theme",
            color = c.text,
            fontFamily =
                XmoFont.medium,
            fontSize = 13.sp
        )

        Spacer(
            Modifier.height(
                8.dp
            )
        )

        Row(
            horizontalArrangement =
                Arrangement.spacedBy(
                    7.dp
                )
        ) {
            XmoTheme.entries.forEach {
                val active =
                    theme == it

                Box(
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                14.dp
                            )
                        )
                        .background(
                            if (active)
                                XmoRed
                            else
                                c.button
                        )
                        .clickable {
                            setTheme(it)
                        }
                        .padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        )
                ) {
                    Text(
                        when (it) {
                            XmoTheme.Dark ->
                                "Dark"

                            XmoTheme.Light ->
                                "Light"

                            XmoTheme.Amoled ->
                                "AMOLED"
                        },
                        color =
                            if (active)
                                Color.White
                            else
                                c.text,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
