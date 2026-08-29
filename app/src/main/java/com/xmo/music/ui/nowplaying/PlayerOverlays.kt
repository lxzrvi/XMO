package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.TimerReset
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.xmo.music.XmoTheme
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

/*
 * =============================================================
 * OVERLAY STATE
 * =============================================================
 */

internal sealed interface PlayerOverlay {
    data object Options : PlayerOverlay
    data object Queue : PlayerOverlay
    data object Sleep : PlayerOverlay
    data object Details : PlayerOverlay
    data object Artist : PlayerOverlay
}

internal data class PopMessage(
    val text: String,
    val key: Long = System.nanoTime()
)

/*
 * =============================================================
 * QUEUE
 * =============================================================
 */

@Composable
internal fun QueueSheet(
    queue: List<Song>,
    currentSongId: Long?,
    colors: HomeColors,
    dismiss: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .44f
                )
            )
            .clickable(
                onClick = dismiss
            ),
        contentAlignment =
            Alignment.BottomCenter
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .height(
                    androidx.compose.ui.platform
                        .LocalConfiguration.current
                        .screenHeightDp.dp *
                        .52f
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 28.dp,
                        topEnd = 28.dp
                    )
                )
                .background(colors.surface)
                .clickable {}
                .padding(top = 12.dp)
        ) {
            Box(
                Modifier
                    .align(
                        Alignment.CenterHorizontally
                    )
                    .width(42.dp)
                    .height(4.dp)
                    .clip(
                        RoundedCornerShape(2.dp)
                    )
                    .background(
                        colors.sub.copy(
                            alpha = .30f
                        )
                    )
            )

            Text(
                text = "Queue",
                color = colors.text,
                fontFamily = XmoFont.bold,
                fontSize = 18.sp,
                modifier =
                    Modifier.padding(
                        start = 18.dp,
                        top = 15.dp,
                        bottom = 10.dp
                    )
            )

            LazyColumn(
                modifier =
                    Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = 30.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(6.dp)
            ) {
                items(
                    items = queue,
                    key = { song ->
                        song.id
                    }
                ) { song ->
                    QueueRow(
                        song = song,
                        active =
                            song.id ==
                                currentSongId,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueRow(
    song: Song,
    active: Boolean,
    colors: HomeColors
) {
    val accent =
        LocalXmoAccent.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(
                RoundedCornerShape(13.dp)
            )
            .background(
                if (active) {
                    accent.copy(
                        alpha = .12f
                    )
                } else {
                    colors.button
                }
            )
            .padding(5.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier =
                Modifier
                    .size(48.dp)
                    .clip(
                        RoundedCornerShape(9.dp)
                    )
                    .background(
                        colors.button
                    ),
            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(1f)
                .padding(
                    horizontal = 10.dp
                )
        ) {
            Text(
                text = song.title,
                color =
                    if (active) {
                        accent
                    } else {
                        colors.text
                    },
                fontFamily =
                    XmoFont.bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                color = colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize = 9.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

/*
 * =============================================================
 * SONG OPTIONS
 * =============================================================
 */

@Composable
internal fun SongOptionsBox(
    song: Song?,
    categories: List<UserCategory>,
    colors: HomeColors,
    liked: Boolean,
    close: () -> Unit,
    toggleLike: () -> Unit,
    share: () -> Unit,
    removeLyrics: () -> Unit,
    setCategory: (
        UserCategory,
        Boolean
    ) -> Unit,
    createCategory: (String) -> Boolean
) {
    var newCategory by remember {
        mutableStateOf("")
    }

    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .48f
                )
            )
            .clickable(
                onClick = close
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 24.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            song?.title
                                ?: "Song Options",
                        color = colors.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    song?.artist
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?.let { artist ->
                            Text(
                                text = artist,
                                color = colors.sub,
                                fontFamily =
                                    XmoFont.normal,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow =
                                    TextOverflow.Ellipsis
                            )
                        }
                }

                PremiumCircle(
                    size = 38.dp,
                    background =
                        colors.button,
                    onClick = close
                ) {
                    Icon(
                        imageVector =
                            Lucide.X,
                        contentDescription =
                            "Close",
                        tint = colors.text,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }

            Spacer(
                Modifier.height(12.dp)
            )

            OverlayAction(
                icon = Lucide.Heart,
                title =
                    if (liked) {
                        "Remove from Liked Songs"
                    } else {
                        "Add to Liked Songs"
                    },
                colors = colors,
                active = liked,
                click = toggleLike
            )

            OverlayAction(
                icon = Lucide.Share2,
                title = "Share Song",
                colors = colors,
                click = share
            )

            OverlayAction(
                icon = Lucide.Trash2,
                title =
                    "Remove Attached Lyrics",
                colors = colors,
                click = removeLyrics
            )

            Text(
                text = "CATEGORIES",
                color = accent,
                fontFamily =
                    XmoFont.bold,
                fontSize = 9.sp,
                letterSpacing = 1.sp,
                modifier =
                    Modifier.padding(
                        start = 5.dp,
                        top = 14.dp,
                        bottom = 5.dp
                    )
            )

            categories
                .take(6)
                .forEach { category ->

                    val added =
                        song?.id in
                            category.songIds

                    OverlayAction(
                        icon = Lucide.Star,
                        title = category.name,
                        trailing =
                            if (added) {
                                "Added"
                            } else {
                                "Add"
                            },
                        active = added,
                        colors = colors
                    ) {
                        setCategory(
                            category,
                            !added
                        )
                    }
                }

            Spacer(
                Modifier.height(12.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        colors.button
                    )
                    .padding(
                        start = 13.dp,
                        end = 5.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = newCategory,
                    onValueChange = {
                        newCategory =
                            it.take(24)
                    },
                    singleLine = true,
                    textStyle =
                        TextStyle(
                            color =
                                colors.text,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 12.sp
                        ),
                    modifier =
                        Modifier.weight(1f),
                    decorationBox = {
                        Box {
                            if (
                                newCategory
                                    .isBlank()
                            ) {
                                Text(
                                    text =
                                        "Create category",
                                    color =
                                        colors.sub,
                                    fontFamily =
                                        XmoFont.normal,
                                    fontSize =
                                        11.sp
                                )
                            }

                            it()
                        }
                    }
                )

                PremiumCircle(
                    size = 36.dp,
                    background =
                        accent.copy(
                            alpha = .16f
                        ),
                    enabled =
                        newCategory
                            .trim()
                            .isNotEmpty(),
                    onClick = {
                        val name =
                            newCategory
                                .trim()

                        if (
                            name.isNotEmpty() &&
                            createCategory(name)
                        ) {
                            newCategory = ""
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Lucide.Plus,
                        contentDescription =
                            "Create category",
                        tint = accent,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

/*
 * =============================================================
 * SLEEP TIMER
 * =============================================================
 */

@Composable
internal fun SleepTimerBox(
    colors: HomeColors,
    active: Boolean,
    dismiss: () -> Unit,
    setTimer: (
        Long,
        String
    ) -> Unit,
    cancel: () -> Unit
) {
    var customMinutes by remember {
        mutableStateOf("")
    }

    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .48f
                )
            )
            .clickable(
                onClick = dismiss
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 30.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(17.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "Sleep Timer",
                    color = colors.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 17.sp,
                    modifier =
                        Modifier.weight(1f)
                )

                PremiumCircle(
                    size = 36.dp,
                    background =
                        colors.button,
                    onClick = dismiss
                ) {
                    Icon(
                        imageVector =
                            Lucide.X,
                        contentDescription =
                            "Close",
                        tint = colors.text,
                        modifier =
                            Modifier.size(16.dp)
                    )
                }
            }

            Spacer(
                Modifier.height(9.dp)
            )

            listOf(
                15L to "15 minutes",
                30L to "30 minutes",
                45L to "45 minutes",
                60L to "1 hour"
            ).forEach {
                    (minutes, label) ->

                OverlayAction(
                    icon =
                        Lucide.Clock3,
                    title = label,
                    colors = colors
                ) {
                    setTimer(
                        minutes *
                            60_000L,
                        label
                    )
                }
            }

            Spacer(
                Modifier.height(5.dp)
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(
                        RoundedCornerShape(14.dp)
                    )
                    .background(
                        colors.button
                    )
                    .padding(
                        horizontal = 12.dp
                    ),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = customMinutes,
                    onValueChange = {
                        customMinutes =
                            it.filter(
                                Char::isDigit
                            )
                                .take(4)
                    },
                    singleLine = true,
                    textStyle =
                        TextStyle(
                            color =
                                colors.text,
                            fontFamily =
                                XmoFont.normal,
                            fontSize =
                                12.sp
                        ),
                    modifier =
                        Modifier.weight(1f),
                    decorationBox = {
                        Box {
                            if (
                                customMinutes
                                    .isBlank()
                            ) {
                                Text(
                                    text =
                                        "Custom minutes",
                                    color =
                                        colors.sub,
                                    fontFamily =
                                        XmoFont.normal,
                                    fontSize =
                                        11.sp
                                )
                            }

                            it()
                        }
                    }
                )

                PremiumCircle(
                    size = 34.dp,
                    background =
                        accent.copy(
                            alpha = .16f
                        ),
                    enabled =
                        (
                            customMinutes
                                .toLongOrNull()
                                ?: 0L
                            ) > 0L,
                    onClick = {
                        val minutes =
                            customMinutes
                                .toLongOrNull()
                                ?: return@PremiumCircle

                        setTimer(
                            minutes *
                                60_000L,
                            "$minutes min"
                        )
                    }
                ) {
                    Icon(
                        imageVector =
                            Lucide.TimerReset,
                        contentDescription =
                            "Set custom timer",
                        tint = accent,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }

            if (active) {
                Spacer(
                    Modifier.height(8.dp)
                )

                OverlayAction(
                    icon = Lucide.X,
                    title =
                        "Cancel Timer",
                    active = true,
                    colors = colors,
                    click = cancel
                )
            }
        }
    }
}

/*
 * =============================================================
 * REAL SONG DETAILS
 * =============================================================
 */
@Composable
internal fun ArtistInfoBox(
    artist: String,
    trackCount: Int,
    colors: HomeColors,
    close: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .48f
                )
            )
            .clickable(
                onClick = close
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 32.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        25.dp
                    )
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(18.dp),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text =
                    artist.ifBlank {
                        "Unknown artist"
                    },
                color =
                    colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    19.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                Modifier.height(7.dp)
            )

            Text(
                text =
                    when (trackCount) {
                        1 ->
                            "1 track on this device"

                        else ->
                            "$trackCount tracks on this device"
                    },
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    11.sp
            )

            Spacer(
                Modifier.height(15.dp)
            )

            PremiumCircle(
                size = 39.dp,
                background =
                    colors.button,
                onClick = close
            ) {
                Icon(
                    imageVector =
                        Lucide.X,
                    contentDescription =
                        "Close",
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(
                            18.dp
                        )
                )
            }
        }
    }
}
 
@Composable
internal fun SongDetailsBox(
    song: Song?,
    album: String,
    colors: HomeColors,
    close: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Color.Black.copy(
                    alpha = .48f
                )
            )
            .clickable(
                onClick = close
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Column(
            Modifier
                .padding(
                    horizontal = 24.dp
                )
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(25.dp)
                )
                .background(
                    colors.surface
                )
                .clickable {}
                .padding(17.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text = "Song Details",
                    color = colors.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 17.sp,
                    modifier =
                        Modifier.weight(1f)
                )

                PremiumCircle(
                    size = 37.dp,
                    background =
                        colors.button,
                    onClick = close
                ) {
                    Icon(
                        imageVector =
                            Lucide.X,
                        contentDescription =
                            "Close",
                        tint = colors.text,
                        modifier =
                            Modifier.size(17.dp)
                    )
                }
            }

            Spacer(
                Modifier.height(13.dp)
            )

            song?.let { current ->
                DetailValue(
                    label = "Title",
                    value = current.title,
                    colors = colors
                )

                DetailValue(
                    label = "Artist",
                    value = current.artist,
                    colors = colors
                )

                DetailValue(
                    label = "Album",
                    value = album,
                    colors = colors
                )

                current.metadata
                    ?.let { meta ->

                        meta.genre?.let {
                            DetailValue(
                                "Genre",
                                it,
                                colors
                            )
                        }

                        meta.year?.let {
                            DetailValue(
                                "Year",
                                it.toString(),
                                colors
                            )
                        }

                        meta.trackNumber?.let {
                            DetailValue(
                                "Track",
                                it.toString(),
                                colors
                            )
                        }

                        meta.discNumber?.let {
                            DetailValue(
                                "Disc",
                                it.toString(),
                                colors
                            )
                        }

                        meta.composer?.let {
                            DetailValue(
                                "Composer",
                                it,
                                colors
                            )
                        }

                        meta.writer?.let {
                            DetailValue(
                                "Writer",
                                it,
                                colors
                            )
                        }

                        meta.bitrate?.let {
                            DetailValue(
                                "Bitrate",
                                "${it / 1000} kbps",
                                colors
                            )
                        }

                        meta.sampleRate?.let {
                            DetailValue(
                                "Sample rate",
                                "$it Hz",
                                colors
                            )
                        }

                        meta.channelCount?.let {
                            DetailValue(
                                "Channels",
                                it.toString(),
                                colors
                            )
                        }

                        meta.mimeType?.let {
                            DetailValue(
                                "Type",
                                it,
                                colors
                            )
                        }

                        meta.fileName?.let {
                            DetailValue(
                                "File",
                                it,
                                colors
                            )
                        }

                        meta.sizeBytes?.let {
                            DetailValue(
                                "Size",
                                formatBytes(it),
                                colors
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun DetailValue(
    label: String,
    value: String,
    colors: HomeColors
) {
    if (value.isBlank()) {
        return
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                vertical = 5.dp
            )
    ) {
        Text(
            text = label,
            color = colors.sub,
            fontFamily =
                XmoFont.normal,
            fontSize = 11.sp,
            modifier =
                Modifier.width(90.dp)
        )

        Text(
            text = value,
            color = colors.text,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
            maxLines = 2,
            overflow =
                TextOverflow.Ellipsis,
            textAlign =
                TextAlign.End,
            modifier =
                Modifier.weight(1f)
        )
    }
}

/*
 * =============================================================
 * COMMON ACTION
 * =============================================================
 */

@Composable
private fun OverlayAction(
    icon: ImageVector,
    title: String,
    colors: HomeColors,
    trailing: String? = null,
    active: Boolean = false,
    click: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(13.dp)
            )
            .clickable(
                onClick = click
            )
            .padding(
                horizontal = 7.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint =
                if (active) {
                    accent
                } else {
                    colors.icon
                },
            modifier =
                Modifier.size(18.dp)
        )

        Text(
            text = title,
            color =
                if (active) {
                    accent
                } else {
                    colors.text
                },
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = 12.dp
                    )
        )

        trailing?.let {
            Text(
                text = it,
                color =
                    if (active) {
                        accent
                    } else {
                        colors.sub
                    },
                fontFamily =
                    XmoFont.medium,
                fontSize = 9.sp
            )
        }
    }
}

/*
 * =============================================================
 * POP
 * =============================================================
 */

@Composable
internal fun XmoPop(
    message: String,
    theme: XmoTheme,
    modifier: Modifier = Modifier
) {
    val background =
        when (theme) {
            XmoTheme.Light ->
                Color.White.copy(
                    alpha = .94f
                )

            XmoTheme.Dark ->
                Color(0xFF1C1C1E)
                    .copy(
                        alpha = .94f
                    )

            XmoTheme.Amoled ->
                Color.Black.copy(
                    alpha = .96f
                )
        }

    val foreground =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF151515)

            else ->
                Color.White
        }

    Box(
        modifier
            .clip(
                RoundedCornerShape(22.dp)
            )
            .background(background)
            .padding(
                horizontal = 17.dp,
                vertical = 11.dp
            )
    ) {
        Text(
            text = message,
            color = foreground,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp,
            textAlign =
                TextAlign.Center
        )
    }
}
