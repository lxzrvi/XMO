package com.xmo.music.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.XmoTheme
import com.xmo.music.data.AccentMode
import com.xmo.music.data.CustomAccent
import com.xmo.music.data.LibraryPreferences
import com.xmo.music.data.PlaybackPreferences
import com.xmo.music.data.ThemeMode
import com.xmo.music.data.XmoAppearance
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private sealed interface SettingsPage {
    data object Appearance : SettingsPage
    data object CustomAccent : SettingsPage
    data object Playback : SettingsPage
    data object Library : SettingsPage
    data object About : SettingsPage
}

@Composable
fun Settings(
    theme: XmoTheme,
    appearance: XmoAppearance,
    libraryPreferences: LibraryPreferences,
    playbackPreferences: PlaybackPreferences,
    resumeOnHeadphones: Boolean,
    onAppearanceChanged: (XmoAppearance) -> Unit,
    onLibraryPreferencesChanged: (LibraryPreferences) -> Unit,
    onPlaybackPreferencesChanged: (PlaybackPreferences) -> Unit,
    onResumeHeadphonesChanged: (Boolean) -> Unit,
    rescan: () -> Unit
) {
    val c =
        homeColors(
            theme
        )

    var page by
        remember {
            mutableStateOf<SettingsPage?>(
                null
            )
        }

    BackHandler(
        enabled =
            page !=
                null
    ) {
        page =
            null
    }

    when (
        val current =
            page
    ) {
        SettingsPage.Appearance -> {
            AppearanceSettings(
                theme =
                    theme,

                appearance =
                    appearance,

                c =
                    c,

                update =
                    onAppearanceChanged,

                custom = {
                    page =
                        SettingsPage.CustomAccent
                },

                close = {
                    page =
                        null
                }
            )

            return
        }

        SettingsPage.CustomAccent -> {
            CustomAccentSettings(
                theme =
                    theme,

                appearance =
                    appearance,

                c =
                    c,

                update =
                    onAppearanceChanged,

                close = {
                    page =
                        SettingsPage.Appearance
                }
            )

            return
        }

        SettingsPage.Playback -> {
            PlaybackSettings(
                c =
                    c,

                preferences =
                    playbackPreferences,

                resumeOnHeadphones =
                    resumeOnHeadphones,

                update =
                    onPlaybackPreferencesChanged,

                updateResume =
                    onResumeHeadphonesChanged,

                close = {
                    page =
                        null
                }
            )

            return
        }

        SettingsPage.Library -> {
            LibrarySettings(
                c =
                    c,

                preferences =
                    libraryPreferences,

                update =
                    onLibraryPreferencesChanged,

                rescan =
                    rescan,

                close = {
                    page =
                        null
                }
            )

            return
        }

        SettingsPage.About -> {
            AboutSettings(
                c =
                    c
            ) {
                page =
                    null
            }

            return
        }

        null ->
            Unit
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),

        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 190.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            )
    ) {
        item(
            key =
                "title"
        ) {
            Text(
                "Settings",

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    22.sp,

                modifier =
                    Modifier.padding(
                        top = 8.dp,
                        bottom = 7.dp
                    )
            )
        }

        item(
            key =
                "appearance"
        ) {
            SettingsGroup(
                label =
                    "LOOK & FEEL",

                c =
                    c
            ) {
                NavigationSetting(
                    title =
                        "Appearance",

                    subtitle =
                        "Theme and XMO accent",

                    value =
                        "${appearance.themeMode.displayName()} • ${
                            appearance.accentMode.displayName()
                        }",

                    c =
                        c,

                    accent =
                        true
                ) {
                    page =
                        SettingsPage.Appearance
                }
            }
        }

        item(
            key =
                "playback"
        ) {
            SettingsGroup(
                label =
                    "PLAYBACK",

                c =
                    c
            ) {
                NavigationSetting(
                    title =
                        "Playback",

                    subtitle =
                        "Speed, pitch and hardware behavior",

                    value =
                        "${formatSpeed(playbackPreferences.playbackSpeed)}×",

                    c =
                        c
                ) {
                    page =
                        SettingsPage.Playback
                }
            }
        }

        item(
            key =
                "library"
        ) {
            SettingsGroup(
                label =
                    "MEDIA LIBRARY",

                c =
                    c
            ) {
                NavigationSetting(
                    title =
                        "Library & Storage",

                    subtitle =
                        "Scanning and local audio filters",

                    value =
                        "Local",

                    c =
                        c
                ) {
                    page =
                        SettingsPage.Library
                }

                SettingAction(
                    title =
                        "Rescan Storage Now",

                    subtitle =
                        "Refresh the real MediaStore library",

                    value =
                        "Scan",

                    c =
                        c,

                    accent =
                        true,

                    click =
                        rescan
                )
            }
        }

        item(
            key =
                "system_audio"
        ) {
            SettingsGroup(
                label =
                    "AUDIO BEHAVIOR",

                c =
                    c
            ) {
                StatusSetting(
                    title =
                        "Pause on Headphone Unplug",

                    subtitle =
                        "Handled by Media3 audio-becoming-noisy",

                    value =
                        "Active",

                    c =
                        c
                )

                ToggleSetting(
                    title =
                        "Resume on Headphone Connect",

                    subtitle =
                        "Stored preference for hardware resume behavior",

                    checked =
                        resumeOnHeadphones,

                    c =
                        c,

                    change =
                        onResumeHeadphonesChanged
                )
            }
        }

        item(
            key =
                "about"
        ) {
            SettingsGroup(
                label =
                    "XMO",

                c =
                    c
            ) {
                NavigationSetting(
                    title =
                        "About XMO",

                    subtitle =
                        "Application and playback information",

                    value =
                        "1.0",

                    c =
                        c
                ) {
                    page =
                        SettingsPage.About
                }
            }
        }

        item(
            key =
                "footer"
        ) {
            SettingsFooter(
                c
            )
        }
    }
}

/*
 * =============================================================
 * APPEARANCE
 * =============================================================
 */

@Composable
private fun AppearanceSettings(
    theme: XmoTheme,
    appearance: XmoAppearance,
    c: HomeColors,
    update: (XmoAppearance) -> Unit,
    custom: () -> Unit,
    close: () -> Unit
) {
    SettingsSubPage(
        title =
            "Appearance",

        c =
            c,

        close =
            close
    ) {
        item {
            SettingsGroup(
                "THEME",
                c
            ) {
                ThemeMode.entries
                    .forEach { mode ->

                        SelectSetting(
                            title =
                                mode.displayName(),

                            subtitle =
                                when (
                                    mode
                                ) {
                                    ThemeMode.System ->
                                        "Follow Android light/dark theme"

                                    ThemeMode.Dark ->
                                        "XMO dark interface"

                                    ThemeMode.Light ->
                                        "Bright XMO interface"

                                    ThemeMode.Amoled ->
                                        "Pure black surfaces"
                                },

                            selected =
                                appearance.themeMode ==
                                    mode,

                            c =
                                c
                        ) {
                            update(
                                appearance.copy(
                                    themeMode =
                                        mode
                                )
                            )
                        }
                    }
            }
        }

        item {
            SettingsGroup(
                "ACCENT COLOR",
                c
            ) {
                AccentMode.entries
                    .forEach { mode ->

                        val preview =
                            when (
                                mode
                            ) {
                                AccentMode.Red ->
                                    XmoRed

                                AccentMode.Blue ->
                                    XmoBlue

                                AccentMode.Custom ->
                                    customAccentColor(
                                        appearance.customAccent
                                    )
                            }

                        ColorSetting(
                            title =
                                mode.displayName(),

                            selected =
                                appearance.accentMode ==
                                    mode,

                            preview =
                                preview,

                            c =
                                c
                        ) {
                            if (
                                mode ==
                                AccentMode.Custom
                            ) {
                                update(
                                    appearance.copy(
                                        accentMode =
                                            AccentMode.Custom
                                    )
                                )

                                custom()
                            } else {
                                update(
                                    appearance.copy(
                                        accentMode =
                                            mode
                                    )
                                )
                            }
                        }
                    }
            }
        }

        item {
            Text(
                "Theme and accent are saved and restored automatically.",

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp,

                modifier =
                    Modifier.padding(
                        horizontal = 8.dp,
                        vertical = 4.dp
                    )
            )
        }
    }
}

/*
 * =============================================================
 * CUSTOM ACCENT
 * =============================================================
 */

@Composable
private fun CustomAccentSettings(
    theme: XmoTheme,
    appearance: XmoAppearance,
    c: HomeColors,
    update: (XmoAppearance) -> Unit,
    close: () -> Unit
) {
    var custom by
        remember(
            appearance.customAccent
        ) {
            mutableStateOf(
                appearance.customAccent
            )
        }

    var hue by
        remember(
            appearance.customAccent.argb
        ) {
            mutableFloatStateOf(
                colorHue(
                    customAccentColor(
                        appearance.customAccent
                    )
                )
            )
        }

    fun commit(
        next: CustomAccent
    ) {
        custom =
            next

        update(
            appearance.copy(
                accentMode =
                    AccentMode.Custom,

                customAccent =
                    next
            )
        )
    }

    SettingsSubPage(
        title =
            "Custom Accent",

        c =
            c,

        close =
            close
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(
                        c.surface,
                        RoundedCornerShape(
                            22.dp
                        )
                    )
                    .border(
                        .7.dp,
                        c.border,
                        RoundedCornerShape(
                            22.dp
                        )
                    )
                    .padding(
                        18.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                /*
                 * Native Compose hue wheel.
                 */
                HueWheel(
                    hue =
                        hue,

                    lightness =
                        custom.lightness,

                    alpha =
                        custom.alpha,

                    change = { value ->
                        hue =
                            value

                        val color =
                            hslColor(
                                hue =
                                    value,

                                lightness =
                                    custom.lightness,

                                alpha =
                                    custom.alpha
                            )

                        commit(
                            custom.copy(
                                argb =
                                    color
                                        .toArgb()
                                        .toLong() and
                                        0xFFFFFFFFL
                            )
                        )
                    }
                )

                Spacer(
                    Modifier.height(
                        24.dp
                    )
                )

                SettingSlider(
                    title =
                        "Light / Dark",

                    value =
                        custom.lightness,

                    valueText =
                        "${(custom.lightness * 100f).toInt()}%",

                    c =
                        c,

                    change = { value ->
                        val color =
                            hslColor(
                                hue =
                                    hue,

                                lightness =
                                    value,

                                alpha =
                                    custom.alpha
                            )

                        commit(
                            custom.copy(
                                argb =
                                    color
                                        .toArgb()
                                        .toLong() and
                                        0xFFFFFFFFL,

                                lightness =
                                    value
                            )
                        )
                    }
                )

                SettingSlider(
                    title =
                        "Transparency",

                    value =
                        custom.alpha,

                    valueText =
                        "${(custom.alpha * 100f).toInt()}%",

                    c =
                        c,

                    change = { value ->
                        commit(
                            custom.copy(
                                alpha =
                                    value
                            )
                        )
                    }
                )

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

                val color =
                    customAccentColor(
                        custom
                    )

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            54.dp
                        )
                        .background(
                            c.button,
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .border(
                            .7.dp,
                            c.border,
                            RoundedCornerShape(
                                15.dp
                            )
                        )
                        .padding(
                            horizontal =
                                14.dp
                        ),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .size(
                                30.dp
                            )
                            .background(
                                color,
                                CircleShape
                            )
                            .border(
                                .7.dp,
                                c.border,
                                CircleShape
                            )
                    )

                    Column(
                        Modifier.padding(
                            start =
                                12.dp
                        )
                    ) {
                        Text(
                            "HEX",

                            color =
                                c.sub,

                            fontFamily =
                                XmoFont.thin,

                            fontSize =
                                8.sp
                        )

                        Text(
                            colorHex(
                                color
                            ),

                            color =
                                c.text,

                            fontFamily =
                                XmoFont.medium,

                            fontSize =
                                13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HueWheel(
    hue: Float,
    lightness: Float,
    alpha: Float,
    change: (Float) -> Unit
) {
    Box(
        Modifier.size(
            220.dp
        ),

        contentAlignment =
            Alignment.Center
    ) {
        Canvas(
            Modifier
                .fillMaxSize()
                .clickable {
                    /*
                     * Clicking wheel advances through hue as a
                     * keyboard/accessibility-safe fallback.
                     */
                    change(
                        (
                            hue +
                                30f
                            ) %
                            360f
                    )
                }
        ) {
            val radius =
                size.minDimension /
                    2f

            val stroke =
                30.dp.toPx()

            for (
                degree in
                0 until
                360
            ) {
                val start =
                    degree.toFloat() -
                        90f

                drawArc(
                    color =
                        hslColor(
                            hue =
                                degree.toFloat(),

                            lightness =
                                .5f,

                            alpha =
                                1f
                        ),

                    startAngle =
                        start,

                    sweepAngle =
                        1.8f,

                    useCenter =
                        false,

                    topLeft =
                        Offset(
                            stroke /
                                2f,
                            stroke /
                                2f
                        ),

                    size =
                        androidx.compose.ui.geometry.Size(
                            size.width -
                                stroke,

                            size.height -
                                stroke
                        ),

                    style =
                        Stroke(
                            width =
                                stroke
                        )
                )
            }

            val angle =
                (
                    hue -
                        90f
                    ) *
                    PI.toFloat() /
                    180f

            val markerRadius =
                radius -
                    stroke /
                    2f

            val center =
                Offset(
                    size.width /
                        2f,

                    size.height /
                        2f
                )

            val marker =
                Offset(
                    center.x +
                        cos(
                            angle
                        ) *
                        markerRadius,

                    center.y +
                        sin(
                            angle
                        ) *
                        markerRadius
                )

            drawCircle(
                color =
                    Color.White,

                radius =
                    8.dp.toPx(),

                center =
                    marker
            )

            drawCircle(
                color =
                    hslColor(
                        hue,
                        lightness,
                        alpha
                    ),

                radius =
                    6.dp.toPx(),

                center =
                    marker
            )
        }

        Box(
            Modifier
                .size(
                    112.dp
                )
                .background(
                    hslColor(
                        hue,
                        lightness,
                        alpha
                    ),
                    CircleShape
                )
                .border(
                    1.dp,
                    Color.White.copy(
                        alpha = .20f
                    ),
                    CircleShape
                )
        )
    }
}

/*
 * =============================================================
 * PLAYBACK
 * =============================================================
 */

@Composable
private fun PlaybackSettings(
    c: HomeColors,
    preferences: PlaybackPreferences,
    resumeOnHeadphones: Boolean,
    update: (PlaybackPreferences) -> Unit,
    updateResume: (Boolean) -> Unit,
    close: () -> Unit
) {
    SettingsSubPage(
        title =
            "Playback",

        c =
            c,

        close =
            close
    ) {
        item {
            SettingsGroup(
                "SPEED & PITCH",
                c
            ) {
                SettingSlider(
                    title =
                        "Playback Speed",

                    value =
                        (
                            preferences.playbackSpeed -
                                .25f
                            ) /
                            2.75f,

                    valueText =
                        "${formatSpeed(preferences.playbackSpeed)}×",

                    c =
                        c,

                    change = {
                        update(
                            preferences.copy(
                                playbackSpeed =
                                    .25f +
                                        it *
                                        2.75f
                            )
                        )
                    }
                )

                SettingSlider(
                    title =
                        "Pitch",

                    value =
                        (
                            preferences.playbackPitch -
                                .5f
                            ) /
                            1.5f,

                    valueText =
                        "${formatSpeed(preferences.playbackPitch)}×",

                    c =
                        c,

                    change = {
                        update(
                            preferences.copy(
                                playbackPitch =
                                    .5f +
                                        it *
                                        1.5f
                            )
                        )
                    }
                )

                SettingAction(
                    title =
                        "Reset Speed & Pitch",

                    subtitle =
                        "Restore normal playback",

                    value =
                        "Reset",

                    c =
                        c
                ) {
                    update(
                        PlaybackPreferences()
                    )
                }
            }
        }

        item {
            SettingsGroup(
                "HEADPHONES",
                c
            ) {
                StatusSetting(
                    title =
                        "Pause on Unplug",

                    subtitle =
                        "Media3 audio-becoming-noisy protection",

                    value =
                        "Active",

                    c =
                        c
                )

                ToggleSetting(
                    title =
                        "Resume on Connect",

                    subtitle =
                        "Remember resume preference",

                    checked =
                        resumeOnHeadphones,

                    c =
                        c,

                    change =
                        updateResume
                )
            }
        }

        item {
            Text(
                "Shuffle, repeat and sleep timer are available directly on Now Playing.",

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp,

                modifier =
                    Modifier.padding(
                        horizontal =
                            8.dp
                    )
            )
        }
    }
}

/*
 * =============================================================
 * LIBRARY
 * =============================================================
 */

@Composable
private fun LibrarySettings(
    c: HomeColors,
    preferences: LibraryPreferences,
    update: (LibraryPreferences) -> Unit,
    rescan: () -> Unit,
    close: () -> Unit
) {
    SettingsSubPage(
        title =
            "Library & Storage",

        c =
            c,

        close =
            close
    ) {
        item {
            SettingsGroup(
                "SCANNING",
                c
            ) {
                SettingAction(
                    title =
                        "Rescan Storage Now",

                    subtitle =
                        "Read the Android MediaStore again",

                    value =
                        "Scan",

                    c =
                        c,

                    accent =
                        true,

                    click =
                        rescan
                )

                StatusSetting(
                    title =
                        "Artwork Source",

                    subtitle =
                        "Embedded / MediaStore local artwork",

                    value =
                        "Local",

                    c =
                        c
                )
            }
        }

        item {
            SettingsGroup(
                "FILTER",
                c
            ) {
                ToggleSetting(
                    title =
                        "Ignore Short Audio",

                    subtitle =
                        "Hide audio shorter than selected duration",

                    checked =
                        preferences.ignoreShortAudio,

                    c =
                        c
                ) {
                    update(
                        preferences.copy(
                            ignoreShortAudio =
                                it
                        )
                    )
                }

                if (
                    preferences.ignoreShortAudio
                ) {
                    SettingSlider(
                        title =
                            "Minimum Duration",

                        value =
                            (
                                preferences.minimumDurationMs /
                                    300_000f
                                )
                                .coerceIn(
                                    0f,
                                    1f
                                ),

                        valueText =
                            "${preferences.minimumDurationMs / 1000L}s",

                        c =
                            c,

                        change = {
                            val duration =
                                (
                                    it *
                                        300_000f
                                    )
                                    .toLong()
                                    .coerceAtLeast(
                                        1_000L
                                    )

                            update(
                                preferences.copy(
                                    minimumDurationMs =
                                        duration
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

/*
 * =============================================================
 * ABOUT
 * =============================================================
 */

@Composable
private fun AboutSettings(
    c: HomeColors,
    close: () -> Unit
) {
    SettingsSubPage(
        title =
            "About XMO",

        c =
            c,

        close =
            close
    ) {
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 70.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    "XMO",

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.logo,

                    fontSize =
                        32.sp
                )

                Spacer(
                    Modifier.height(
                        8.dp
                    )
                )

                Text(
                    "Native offline music player",

                    color =
                        c.sub,

                    fontFamily =
                        XmoFont.normal,

                    fontSize =
                        12.sp
                )

                Text(
                    "Version 1.0",

                    color =
                        c.sub,

                    fontFamily =
                        XmoFont.thin,

                    fontSize =
                        10.sp,

                    modifier =
                        Modifier.padding(
                            top =
                                4.dp
                        )
                )

                Spacer(
                    Modifier.height(
                        35.dp
                    )
                )

                Text(
                    "Local MediaStore • Media3 • Jetpack Compose",

                    color =
                        LocalXmoAccent.current,

                    fontFamily =
                        XmoFont.medium,

                    fontSize =
                        10.sp
                )
            }
        }
    }
}

/*
 * =============================================================
 * COMMON SETTINGS UI
 * =============================================================
 */

@Composable
private fun SettingsSubPage(
    title: String,
    c: HomeColors,
    close: () -> Unit,
    content:
        androidx.compose.foundation.lazy
            .LazyListScope.() -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            ),

        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 190.dp
            ),

        verticalArrangement =
            Arrangement.spacedBy(
                16.dp
            )
    ) {
        item(
            key =
                "sub_header"
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        68.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(
                            40.dp
                        )
                        .background(
                            c.button,
                            CircleShape
                        )
                        .border(
                            .6.dp,
                            c.border,
                            CircleShape
                        )
                        .clickable(
                            onClick =
                                close
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "‹",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            28.sp
                    )
                }

                Text(
                    title,

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.bold,

                    fontSize =
                        19.sp,

                    modifier =
                        Modifier.padding(
                            start =
                                13.dp
                        )
                )
            }
        }

        content()
    }
}

@Composable
private fun SettingsGroup(
    label: String,
    c: HomeColors,
    content:
        @Composable Column.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                c.surface,
                RoundedCornerShape(
                    18.dp
                )
            )
            .border(
                .7.dp,
                c.border,
                RoundedCornerShape(
                    18.dp
                )
            )
            .padding(
                horizontal = 10.dp,
                vertical = 13.dp
            )
    ) {
        Text(
            label,

            color =
                LocalXmoAccent.current,

            fontFamily =
                XmoFont.bold,

            fontSize =
                10.sp,

            letterSpacing =
                1.2.sp,

            modifier =
                Modifier.padding(
                    start = 7.dp,
                    bottom = 7.dp
                )
        )

        content()
    }
}

@Composable
private fun NavigationSetting(
    title: String,
    subtitle: String,
    value: String,
    c: HomeColors,
    accent: Boolean = false,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 8.dp,
                vertical = 11.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        SettingDot(
            active =
                accent,

            c =
                c
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    start = 13.dp,
                    end = 10.dp
                )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    13.sp
            )

            Text(
                subtitle,

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp
            )
        }

        Text(
            value,

            color =
                if (
                    accent
                ) {
                    LocalXmoAccent.current
                } else {
                    c.sub
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                10.sp
        )

        Text(
            "›",

            color =
                c.sub,

            fontFamily =
                XmoFont.medium,

            fontSize =
                20.sp,

            modifier =
                Modifier.padding(
                    start =
                        6.dp
                )
        )
    }
}

@Composable
private fun SettingAction(
    title: String,
    subtitle: String,
    value: String,
    c: HomeColors,
    accent: Boolean = false,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 8.dp,
                vertical = 11.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        SettingDot(
            active =
                accent,

            c =
                c
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    start = 13.dp,
                    end = 10.dp
                )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    13.sp
            )

            Text(
                subtitle,

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp
            )
        }

        Text(
            value,

            color =
                if (
                    accent
                ) {
                    LocalXmoAccent.current
                } else {
                    c.sub
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
        )
    }
}

@Composable
private fun StatusSetting(
    title: String,
    subtitle: String,
    value: String,
    c: HomeColors
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 11.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        SettingDot(
            active =
                true,

            c =
                c
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    start = 13.dp,
                    end = 10.dp
                )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    13.sp
            )

            Text(
                subtitle,

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp
            )
        }

        Text(
            value,

            color =
                LocalXmoAccent.current,

            fontFamily =
                XmoFont.medium,

            fontSize =
                10.sp
        )
    }
}

@Composable
private fun ToggleSetting(
    title: String,
    subtitle: String,
    checked: Boolean,
    c: HomeColors,
    change: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {
                change(
                    !checked
                )
            }
            .padding(
                horizontal = 8.dp,
                vertical = 10.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    end =
                        10.dp
                )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    13.sp
            )

            Text(
                subtitle,

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp
            )
        }

        Switch(
            checked =
                checked,

            onCheckedChange =
                change,

            colors =
                SwitchDefaults.colors(
                    checkedThumbColor =
                        Color.White,

                    checkedTrackColor =
                        LocalXmoAccent.current,

                    uncheckedThumbColor =
                        c.sub,

                    uncheckedTrackColor =
                        c.button,

                    uncheckedBorderColor =
                        c.border
                )
        )
    }
}

@Composable
private fun SelectSetting(
    title: String,
    subtitle: String,
    selected: Boolean,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 8.dp,
                vertical = 10.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .weight(
                    1f
                )
        ) {
            Text(
                title,

                color =
                    if (
                        selected
                    ) {
                        LocalXmoAccent.current
                    } else {
                        c.text
                    },

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    13.sp
            )

            Text(
                subtitle,

                color =
                    c.sub,

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    9.sp
            )
        }

        Box(
            Modifier
                .size(
                    20.dp
                )
                .border(
                    1.5.dp,

                    if (
                        selected
                    ) {
                        LocalXmoAccent.current
                    } else {
                        c.border
                    },

                    CircleShape
                ),

            contentAlignment =
                Alignment.Center
        ) {
            if (
                selected
            ) {
                Box(
                    Modifier
                        .size(
                            10.dp
                        )
                        .background(
                            LocalXmoAccent.current,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun ColorSetting(
    title: String,
    selected: Boolean,
    preview: Color,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 8.dp,
                vertical = 11.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(
                    34.dp
                )
                .background(
                    preview,
                    CircleShape
                )
                .border(
                    if (
                        selected
                    ) {
                        2.dp
                    } else {
                        .7.dp
                    },

                    if (
                        selected
                    ) {
                        Color.White.copy(
                            alpha = .72f
                        )
                    } else {
                        c.border
                    },

                    CircleShape
                )
        )

        Text(
            title,

            color =
                if (
                    selected
                ) {
                    preview
                } else {
                    c.text
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                13.sp,

            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            13.dp
                    )
        )

        if (
            selected
        ) {
            Text(
                "Selected",

                color =
                    preview,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    10.sp
            )
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    value: Float,
    valueText: String,
    c: HomeColors,
    change: (Float) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 8.dp,
                vertical = 9.dp
            )
    ) {
        Row(
            Modifier.fillMaxWidth()
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    12.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            Text(
                valueText,

                color =
                    LocalXmoAccent.current,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    10.sp
            )
        }

        Slider(
            value =
                value.coerceIn(
                    0f,
                    1f
                ),

            onValueChange =
                change,

            colors =
                SliderDefaults.colors(
                    thumbColor =
                        LocalXmoAccent.current,

                    activeTrackColor =
                        LocalXmoAccent.current,

                    inactiveTrackColor =
                        c.button
                )
        )
    }
}

@Composable
private fun SettingDot(
    active: Boolean,
    c: HomeColors
) {
    Box(
        Modifier
            .size(
                36.dp
            )
            .background(
                c.button,
                RoundedCornerShape(
                    11.dp
                )
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Box(
            Modifier
                .size(
                    10.dp
                )
                .background(
                    if (
                        active
                    ) {
                        LocalXmoAccent.current
                    } else {
                        c.icon
                    },
                    CircleShape
                )
        )
    }
}

@Composable
private fun SettingsFooter(
    c: HomeColors
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                top = 58.dp,
                bottom = 28.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            "XMO",

            color =
                c.text,

            fontFamily =
                XmoFont.logo,

            fontSize =
                19.sp
        )

        Text(
            "lxzrvi • copyright © 2026",

            color =
                c.sub,

            fontFamily =
                XmoFont.thin,

            fontSize =
                9.sp
        )
    }
}

/*
 * =============================================================
 * COLOR HELPERS
 * =============================================================
 */

private fun ThemeMode.displayName():
    String =
    when (
        this
    ) {
        ThemeMode.System ->
            "System"

        ThemeMode.Dark ->
            "Dark"

        ThemeMode.Light ->
            "Light"

        ThemeMode.Amoled ->
            "AMOLED"
    }

private fun AccentMode.displayName():
    String =
    when (
        this
    ) {
        AccentMode.Red ->
            "XMO Red"

        AccentMode.Blue ->
            "XMO Blue"

        AccentMode.Custom ->
            "Custom"
    }

private fun hslColor(
    hue: Float,
    lightness: Float,
    alpha: Float
): Color {
    val h =
        (
            hue %
                360f +
                360f
            ) %
            360f /
            360f

    val l =
        lightness.coerceIn(
            0f,
            1f
        )

    /*
     * Fixed high saturation keeps the accent vivid while the user
     * controls hue and brightness.
     */
    val s =
        .88f

    fun component(
        p: Float,
        q: Float,
        input: Float
    ): Float {
        var t =
            input

        if (
            t <
            0f
        ) {
            t +=
                1f
        }

        if (
            t >
            1f
        ) {
            t -=
                1f
        }

        return when {
            t <
                1f /
                6f ->
                p +
                    (
                        q -
                            p
                        ) *
                    6f *
                    t

            t <
                .5f ->
                q

            t <
                2f /
                3f ->
                p +
                    (
                        q -
                            p
                        ) *
                    (
                        2f /
                            3f -
                            t
                        ) *
                    6f

            else ->
                p
        }
    }

    val q =
        if (
            l <
            .5f
        ) {
            l *
                (
                    1f +
                        s
                    )
        } else {
            l +
                s -
                l *
                s
        }

    val p =
        2f *
            l -
            q

    return Color(
        red =
            component(
                p,
                q,
                h +
                    1f /
                    3f
            ),

        green =
            component(
                p,
                q,
                h
            ),

        blue =
            component(
                p,
                q,
                h -
                    1f /
                    3f
            ),

        alpha =
            alpha.coerceIn(
                0f,
                1f
            )
    )
}

private fun customAccentColor(
    custom: CustomAccent
): Color {
    val argb =
        custom.argb
            .and(
                0xFFFFFFFFL
            )
            .toInt()

    return Color(
        argb
    ).copy(
        alpha =
            custom.alpha
                .coerceIn(
                    0f,
                    1f
                )
    )
}

private fun colorHue(
    color: Color
): Float {
    val r =
        color.red

    val g =
        color.green

    val b =
        color.blue

    val max =
        maxOf(
            r,
            g,
            b
        )

    val min =
        minOf(
            r,
            g,
            b
        )

    val delta =
        max -
            min

    if (
        delta ==
        0f
    ) {
        return 0f
    }

    var hue =
        when (
            max
        ) {
            r ->
                60f *
                    (
                        (
                            g -
                                b
                            ) /
                            delta %
                            6f
                        )

            g ->
                60f *
                    (
                        (
                            b -
                                r
                            ) /
                            delta +
                            2f
                        )

            else ->
                60f *
                    (
                        (
                            r -
                                g
                            ) /
                            delta +
                            4f
                        )
        }

    if (
        hue <
        0f
    ) {
        hue +=
            360f
    }

    return hue
}

private fun colorHex(
    color: Color
): String {
    val value =
        color.toArgb()

    return String.format(
        "#%02X%02X%02X",
        value shr
            16 and
            0xFF,
        value shr
            8 and
            0xFF,
        value and
            0xFF
    )
}

private fun formatSpeed(
    value: Float
): String =
    String.format(
        "%.2f",
        value
    )
        .trimEnd(
            '0'
        )
        .trimEnd(
            '.'
        )
