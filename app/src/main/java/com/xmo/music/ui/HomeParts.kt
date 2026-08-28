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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.xmo.music.data.Song
import com.xmo.music.data.XmoProfile
import kotlinx.coroutines.delay

val XmoRed =
    Color(0xFFFF3B3B)

/*
 * =============================================================
 * HOME THEME COLORS
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
): HomeColors {
    return when (theme) {

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
}

/*
 * =============================================================
 * XMO DRAWABLE ICON
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
    setTheme: (XmoTheme) -> Unit,
    refresh: () -> Unit
) {
    /*
     * Profile is supplied once from App.kt through
     * CompositionLocalProvider.
     *
     * Home.kt does not need extra profile plumbing.
     */
    val profile =
        LocalXmoProfile.current

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

    var menu by
        remember {
            mutableStateOf(
                false
            )
        }

    /*
     * Rotating Home subtitle.
     */
    LaunchedEffect(Unit) {
        while (true) {
            delay(
                4500
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
                start = 16.dp,
                top = 7.dp,
                end = 12.dp,
                bottom = 4.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        /*
         * Real persisted profile.
         */
        HomeProfileAvatar(
            profile =
                profile,

            c = c
        )

        /*
         * Username / subtitle.
         */
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
                    "subtitle"
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
         * -----------------------------------------------------
         * REFRESH + MENU CAPSULE
         * -----------------------------------------------------
         */
        Box {
            Row(
                Modifier
                    .clip(
                        RoundedCornerShape(
                            24.dp
                        )
                    )
                    .background(
                        c.button
                    )
                    .border(
                        .6.dp,
                        c.border,
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
                            R.drawable
                                .ic_xmo_refresh,

                        tint =
                            c.icon,

                        modifier =
                            Modifier.size(
                                18.dp
                            )
                    )
                }

                /*
                 * Divider.
                 */
                Box(
                    Modifier
                        .width(
                            .6.dp
                        )
                        .height(
                            18.dp
                        )
                        .background(
                            c.border
                        )
                )

                IconButton(
                    onClick = {
                        menu =
                            true
                    },

                    modifier =
                        Modifier.size(
                            38.dp
                        )
                ) {
                    XmoIcon(
                        icon =
                            R.drawable
                                .ic_xmo_menu,

                        tint =
                            c.icon,

                        modifier =
                            Modifier.size(
                                19.dp
                            )
                    )
                }
            }

            /*
             * Actual global XMO theme selector.
             */
            DropdownMenu(
                expanded =
                    menu,

                onDismissRequest = {
                    menu =
                        false
                },

                containerColor =
                    c.surface,

                shape =
                    RoundedCornerShape(
                        12.dp
                    )
            ) {
                XmoTheme.entries
                    .forEach {
                            item ->

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text =
                                        when (
                                            item
                                        ) {
                                            XmoTheme.Dark ->
                                                "Dark Theme"

                                            XmoTheme.Light ->
                                                "Light Theme"

                                            XmoTheme.Amoled ->
                                                "AMOLED"
                                        },

                                    color =
                                        if (
                                            theme ==
                                            item
                                        ) {
                                            XmoRed
                                        } else {
                                            c.text
                                        },

                                    fontFamily =
                                        XmoFont.medium,

                                    fontSize =
                                        13.sp
                                )
                            },

                            onClick = {
                                setTheme(
                                    item
                                )

                                menu =
                                    false
                            }
                        )
                    }
            }
        }
    }
}

/*
 * =============================================================
 * PROFILE AVATAR
 * =============================================================
 */

@Composable
private fun HomeProfileAvatar(
    profile: XmoProfile,
    c: HomeColors
) {
    /*
     * Local photo selected through Android Photo Picker.
     */
    val customUri =
        remember(
            profile.avatarUri
        ) {
            profile.avatarUri
                ?.let(
                    android.net.Uri::parse
                )
        }

    val fallbackIcon =
        remember(
            profile.avatarIndex
        ) {
            when (
                profile.avatarIndex
            ) {
                /*
                 * Setup carousel index mapping.
                 */
                1 ->
                    R.drawable
                        .ic_xmo_songs

                2 ->
                    R.drawable
                        .ic_xmo_album

                3 ->
                    R.drawable
                        .ic_xmo_artist

                4 ->
                    R.drawable
                        .ic_xmo_bolt

                5 ->
                    R.drawable
                        .ic_xmo_spark

                else ->
                    null
            }
        }

    Box(
        Modifier
            .size(
                38.dp
            )
            .clip(
                CircleShape
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        XmoRed,
                        Color(
                            0xFF641E27
                        )
                    )
                )
            )
            .border(
                .6.dp,
                c.border,
                CircleShape
            ),

        contentAlignment =
            Alignment.Center
    ) {
        when {
            /*
             * Real local user image.
             */
            customUri != null -> {
                AsyncImage(
                    model =
                        customUri,

                    contentDescription =
                        "Profile",

                    modifier =
                        Modifier
                            .fillMaxSize(),

                    contentScale =
                        ContentScale.Crop
                )
            }

            /*
             * Built-in XMO profile icon.
             */
            fallbackIcon != null -> {
                XmoIcon(
                    icon =
                        fallbackIcon,

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )
            }

            /*
             * Default:
             * first username letter.
             */
            else -> {
                Text(
                    text =
                        profile.name
                            .trim()
                            .firstOrNull()
                            ?.uppercase()
                            ?: "X",

                    color =
                        Color.White,

                    fontFamily =
                        XmoFont.logo,

                    fontSize =
                        15.sp
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
    Row(
        modifier
            .clip(
                RoundedCornerShape(
                    18.dp
                )
            )
            .background(
                if (active) {
                    XmoRed.copy(
                        alpha = .18f
                    )
                } else {
                    c.button
                }
            )
            .clickable(
                onClick =
                    onClick
            )
            .padding(
                horizontal =
                    13.dp,

                vertical =
                    7.dp
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
                if (active) {
                    XmoRed
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
                if (active) {
                    XmoRed
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
    Row(
        modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(
                horizontal =
                    10.dp,

                vertical =
                    8.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoIcon(
            icon =
                icon,

            tint =
                XmoRed,

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

        action?.let {
                actionIcon ->

            Box(
                Modifier
                    .size(
                        28.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        XmoRed.copy(
                            alpha = .18f
                        )
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
                        XmoRed,

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
 * =============================================================
 *
 * Shared by Home All Songs and custom-category layouts.
 *
 * onClick is the real playback entry point.
 * =============================================================
 */

@Composable
fun SongTile(
    song: Song,
    index: Int,
    c: HomeColors,
    theme: XmoTheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val context =
        androidx.compose.ui.platform
            .LocalContext.current

    /*
     * Use synchronous memory cache first.
     *
     * This helps:
     * - tab switching
     * - Home re-entry
     * - scrolling back to old tiles
     */
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

    /*
     * Only perform extraction if cache does not already contain it.
     */
    LaunchedEffect(
        song.artwork
    ) {
        val cached =
            Artwork.cached(
                song.artwork
            )

        dominant =
            cached
                ?: Artwork.color(
                    context,
                    song.artwork
                )
    }

    val artworkAlpha =
        when (theme) {
            XmoTheme.Light ->
                .35f

            XmoTheme.Dark ->
                .40f

            XmoTheme.Amoled ->
                .45f
        }

    Column(
        modifier
            .clip(
                RoundedCornerShape(
                    10.dp
                )
            )
            /*
             * Actual playback callback supplied by Home.
             */
            .clickable(
                onClick =
                    onClick
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
                width =
                    .55.dp,

                color =
                    c.border,

                shape =
                    RoundedCornerShape(
                        10.dp
                    )
            )
            .padding(
                5.dp
            )
    ) {
        /*
         * Artwork.
         */
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(
                    1f
                )
                .clip(
                    RoundedCornerShape(
                        6.dp
                    )
                )
                .background(
                    dominant.copy(
                        alpha =
                            .15f
                    )
                )
        ) {
            AsyncImage(
                model =
                    song.artwork,

                contentDescription =
                    song.title,

                modifier =
                    Modifier
                        .fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )

            /*
             * No fake network image fallback.
             *
             * When local art is null, gradient card remains visible.
             */
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
                                alpha =
                                    .60f
                            ),

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            17.sp
                    )
                }
            }
        }

        /*
         * Title / Artist / More.
         */
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

            /*
             * Song Options backend is still pending.
             * Keep visual more indicator; no fake action.
             */
            XmoIcon(
                icon =
                    R.drawable
                        .ic_xmo_more,

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
