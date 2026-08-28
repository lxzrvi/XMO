package com.xmo.music.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.AccentMode
import com.xmo.music.data.Song
import com.xmo.music.data.XmoAppearance
import com.xmo.music.data.XmoProfile
import com.xmo.music.ui.blur.glassBorder
import com.xmo.music.ui.blur.glassDivider
import com.xmo.music.ui.blur.liveBlur
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.delay

val XmoRed =
    Color(0xFFFF3B3B)

val XmoBlue =
    Color(0xFF398CFF)

/*
 * =============================================================
 * GLOBAL ACCENT
 *
 * App.kt supplies this once. Every screen then follows the same
 * persisted accent without hard-coding red into interaction UI.
 * =============================================================
 */

val LocalXmoAccent =
    compositionLocalOf {
        XmoRed
    }

@Composable
fun ProvideXmoAccent(
    appearance: XmoAppearance,
    content: @Composable () -> Unit
) {
    val accent =
        when (
            appearance.accentMode
        ) {
            AccentMode.Red ->
                XmoRed

            AccentMode.Blue ->
                XmoBlue

            AccentMode.Custom -> {
                val raw =
                    appearance
                        .customAccent
                        .argb
                        .toULong()
                        .toLong()

                val base =
                    Color(
                        raw.toULong()
                            .toLong()
                    )

                /*
                 * Apply persisted transparency. Lightness is used
                 * by the Settings color editor while the selected
                 * ARGB remains the authoritative picked color.
                 */
                base.copy(
                    alpha =
                        appearance
                            .customAccent
                            .alpha
                            .coerceIn(
                                0f,
                                1f
                            )
                )
            }
        }

    CompositionLocalProvider(
        LocalXmoAccent provides
            accent,

        content =
            content
    )
}

/*
 * =============================================================
 * HOME COLORS
 * =============================================================
 */

data class HomeColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val sub: Color,
    val button: Color,
    val icon: Color,
    val border: Color
)

fun homeColors(
    theme: XmoTheme
): HomeColors =
    when (
        theme
    ) {
        XmoTheme.Dark ->
            HomeColors(
                bg =
                    Color(0xFF121212),

                surface =
                    Color(0xFF181818),

                text =
                    Color.White,

                sub =
                    Color.White.copy(
                        alpha = .65f
                    ),

                button =
                    Color.White.copy(
                        alpha = .09f
                    ),

                icon =
                    Color(0xFFB8B8B8),

                border =
                    Color.White.copy(
                        alpha = .11f
                    )
            )

        XmoTheme.Light ->
            HomeColors(
                bg =
                    Color(0xFFF4F6F9),

                surface =
                    Color.White,

                text =
                    Color(0xFF121417),

                sub =
                    Color(0xA6121417),

                button =
                    Color.Black.copy(
                        alpha = .055f
                    ),

                icon =
                    Color(0xFF55575B),

                border =
                    Color.Black.copy(
                        alpha = .12f
                    )
            )

        XmoTheme.Amoled ->
            HomeColors(
                bg =
                    Color.Black,

                surface =
                    Color(0xFF0B0B0B),

                text =
                    Color.White,

                sub =
                    Color.White.copy(
                        alpha = .60f
                    ),

                button =
                    Color.White.copy(
                        alpha = .075f
                    ),

                icon =
                    Color(0xFF999999),

                border =
                    Color.White.copy(
                        alpha = .19f
                    )
            )
    }

/*
 * =============================================================
 * VECTOR RESOURCE ICON
 * =============================================================
 */

@Composable
fun XmoIcon(
    @DrawableRes icon: Int,
    tint: Color,
    modifier: Modifier =
        Modifier
) {
    Icon(
        painter =
            painterResource(
                icon
            ),

        contentDescription =
            null,

        tint =
            tint,

        modifier =
            modifier
    )
}

/*
 * =============================================================
 * HOME HEADER
 * =============================================================
 */

@Composable
fun HomeHeader(
    c: HomeColors,
    theme: XmoTheme,
    hazeState: HazeState,
    refresh: () -> Unit,
    openMenu: () -> Unit,
    openProfile: () -> Unit
) {
    val profile =
        LocalXmoProfile.current

    val accent =
        LocalXmoAccent.current

    val subtitles =
        remember {
            listOf(
                "What are you listening today?",
                "Mood for some chill music?",
                "Feel the beat & rhythm...",
                "Turn up the volume!"
            )
        }

    var subtitle by
        remember {
            mutableIntStateOf(
                0
            )
        }

    LaunchedEffect(Unit) {
        while (true) {
            delay(
                4500L
            )

            subtitle =
                (
                    subtitle +
                        1
                    ) %
                    subtitles.size
        }
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = 14.dp,
                top = 7.dp,
                end = 12.dp,
                bottom = 6.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoProfileAvatar(
            profile =
                profile,

            modifier =
                Modifier
                    .size(
                        40.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .combinedClickable(
                        onClick =
                            openProfile,

                        onLongClick =
                            openProfile
                    ),

            background =
                accent,

            border =
                glassBorder(
                    theme
                )
        )

        Column(
            Modifier
                .padding(
                    start = 10.dp
                )
                .weight(
                    1f
                )
        ) {
            Text(
                text =
                    profile.name
                        .ifBlank {
                            "XMO User"
                        },

                color =
                    c.text,

                fontFamily =
                    XmoFont.user,

                fontWeight =
                    FontWeight.Bold,

                fontSize =
                    18.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            AnimatedContent(
                targetState =
                    subtitle,

                transitionSpec = {
                    fadeIn(
                        tween(
                            220
                        )
                    ) togetherWith
                        fadeOut(
                            tween(
                                180
                            )
                        )
                },

                label =
                    "homeSubtitle"
            ) { index ->

                Text(
                    text =
                        subtitles[index],

                    color =
                        c.sub,

                    fontFamily =
                        XmoFont.thin,

                    fontSize =
                        11.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }

        /*
         * Same Haze material language as NavBar.
         */
        Row(
            Modifier
                .clip(
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .liveBlur(
                    hazeState,
                    theme
                )
                .border(
                    .65.dp,
                    glassBorder(
                        theme
                    ),
                    RoundedCornerShape(
                        24.dp
                    )
                )
                .padding(
                    horizontal =
                        3.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            IconButton(
                onClick =
                    refresh,

                modifier =
                    Modifier.size(
                        38.dp
                    )
            ) {
                XmoIcon(
                    icon =
                        R.drawable.ic_xmo_refresh,

                    tint =
                        c.icon,

                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }

            Box(
                Modifier
                    .width(
                        .6.dp
                    )
                    .height(
                        18.dp
                    )
                    .background(
                        glassDivider(
                            theme
                        )
                    )
            )

            IconButton(
                onClick =
                    openMenu,

                modifier =
                    Modifier.size(
                        38.dp
                    )
            ) {
                XmoIcon(
                    icon =
                        R.drawable.ic_xmo_menu,

                    tint =
                        c.icon,

                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )
            }
        }
    }
}

/*
 * =============================================================
 * CATEGORY CHIP
 * =============================================================
 */

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
    val accent =
        LocalXmoAccent.current

    Row(
        modifier
            .clip(
                RoundedCornerShape(
                    18.dp
                )
            )
            .background(
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .18f
                    )
                } else {
                    c.button
                }
            )
            .border(
                .6.dp,
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .34f
                    )
                } else {
                    c.border
                },
                RoundedCornerShape(
                    18.dp
                )
            )
            .clickable(
                onClick =
                    onClick
            )
            .padding(
                horizontal = 13.dp,
                vertical = 7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically,

        horizontalArrangement =
            Arrangement.spacedBy(
                6.dp
            )
    ) {
        XmoIcon(
            icon =
                icon,

            tint =
                if (
                    active
                ) {
                    accent
                } else {
                    tint
                },

            modifier =
                Modifier.size(
                    14.dp
                )
        )

        Text(
            text =
                text,

            color =
                if (
                    active
                ) {
                    accent
                } else {
                    c.text
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                12.sp,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}

/*
 * =============================================================
 * SECTION TITLE
 * =============================================================
 */

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
    val accent =
        LocalXmoAccent.current

    Row(
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(
                horizontal = 10.dp,
                vertical = 8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoIcon(
            icon =
                icon,

            tint =
                accent,

            modifier =
                Modifier.size(
                    17.dp
                )
        )

        Column(
            Modifier
                .padding(
                    start = 8.dp
                )
                .weight(
                    1f
                )
        ) {
            Text(
                text =
                    title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    17.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            if (
                subtitle.isNotBlank()
            ) {
                Text(
                    text =
                        subtitle,

                    color =
                        c.sub,

                    fontFamily =
                        XmoFont.thin,

                    fontSize =
                        10.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }
        }

        action?.let { actionIcon ->

            Box(
                Modifier
                    .size(
                        30.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        accent.copy(
                            alpha = .16f
                        )
                    )
                    .border(
                        .6.dp,
                        accent.copy(
                            alpha = .28f
                        ),
                        CircleShape
                    )
                    .clickable(
                        onClick =
                            onAction
                    ),

                contentAlignment =
                    Alignment.Center
            ) {
                XmoIcon(
                    icon =
                        actionIcon,

                    tint =
                        accent,

                    modifier =
                        Modifier.size(
                            14.dp
                        )
                )
            }
        }
    }
}

/*
 * =============================================================
 * SONG TILE
 *
 * Tap = playback.
 * More button = options.
 * Long press = same options.
 * =============================================================
 */

@Composable
fun SongTile(
    song: Song,
    index: Int,
    c: HomeColors,
    theme: XmoTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onOptions: (Song) -> Unit = {}
) {
    val context =
        androidx.compose.ui.platform
            .LocalContext.current

    var dominant by
        remember(
            song.artwork
        ) {
            mutableStateOf(
                Artwork.cached(
                    song.artwork
                )
                    ?: Color(
                        0xFF35353A
                    )
            )
        }

    LaunchedEffect(
        song.artwork
    ) {
        dominant =
            Artwork.cached(
                song.artwork
            )
                ?: Artwork.color(
                    context,
                    song.artwork
                )
    }

    val artworkAlpha =
        when (
            theme
        ) {
            XmoTheme.Light ->
                .35f

            XmoTheme.Dark ->
                .42f

            XmoTheme.Amoled ->
                .48f
        }

    Column(
        modifier
            .clip(
                RoundedCornerShape(
                    10.dp
                )
            )
            .combinedClickable(
                onClick =
                    onClick,

                onLongClick = {
                    onOptions(
                        song
                    )
                }
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        dominant.copy(
                            alpha =
                                artworkAlpha
                        ),

                        Artwork.end(
                            dominant,
                            theme
                        )
                    )
                )
            )
            .border(
                .55.dp,
                c.border,
                RoundedCornerShape(
                    10.dp
                )
            )
            .padding(
                5.dp
            )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .weight(
                    1f,
                    fill = false
                )
        )

        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    0.dp
                )
        )

        /*
         * Square artwork without AspectRatio import dependency.
         */
        Box(
            Modifier
                .fillMaxWidth()
        ) {
            androidx.compose.foundation.layout.BoxWithConstraints(
                Modifier.fillMaxWidth()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            maxWidth
                        )
                        .clip(
                            RoundedCornerShape(
                                6.dp
                            )
                        )
                        .background(
                            dominant.copy(
                                alpha = .15f
                            )
                        )
                ) {
                    AsyncImage(
                        model =
                            song.artwork,

                        contentDescription =
                            song.title,

                        modifier =
                            Modifier.fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )

                    if (
                        song.artwork ==
                        null
                    ) {
                        Box(
                            Modifier.fillMaxSize(),

                            contentAlignment =
                                Alignment.Center
                        ) {
                            Text(
                                text =
                                    song.title
                                        .firstOrNull()
                                        ?.uppercase()
                                        ?: "X",

                                color =
                                    c.text.copy(
                                        alpha = .60f
                                    ),

                                fontFamily =
                                    XmoFont.bold,

                                fontSize =
                                    17.sp
                            )
                        }
                    }
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    top = 4.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(
                    1f
                )
            ) {
                Text(
                    text =
                        song.title,

                    color =
                        c.text,

                    fontFamily =
                        XmoFont.bold,

                    fontSize =
                        10.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )

                Text(
                    text =
                        song.artist,

                    color =
                        c.sub,

                    fontFamily =
                        XmoFont.thin,

                    fontSize =
                        8.sp,

                    maxLines =
                        1,

                    overflow =
                        TextOverflow.Ellipsis
                )
            }

            Box(
                Modifier
                    .size(
                        25.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .clickable {
                        onOptions(
                            song
                        )
                    },

                contentAlignment =
                    Alignment.Center
            ) {
                XmoIcon(
                    icon =
                        R.drawable.ic_xmo_more,

                    tint =
                        c.sub,

                    modifier =
                        Modifier.size(
                            14.dp
                        )
                )
            }
        }
    }
}
