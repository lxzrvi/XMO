package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.XmoTheme
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont

@Composable
internal fun PlayerInfo(
    title: String,
    artist: String,
    liked: Boolean,
    inCategory: Boolean,
    sleepRemainingMs: Long,
    sleepTotalMs: Long?,
    colors: HomeColors,
    accent: Color,
    softButton: Color,
    theme: XmoTheme,
    toggleLike: () -> Unit,
    openCategories: () -> Unit,
    openSleep: () -> Unit,
    openQueue: () -> Unit,
    openDetails: () -> Unit,
    openArtist: () -> Unit
) {
    val iconColor =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF15161A)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(120.dp)
    ) {
        /*
         * Only the actions move slightly upward.
         *
         * The title/artist below remain bottom-aligned to the
         * exact same 120dp host, so their approved position is
         * unchanged.
         */
        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart
                    )
                    .offset(
                        y = (-4).dp
                    )
                    .padding(
                        start = 4.dp
                    ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            PremiumCircle(
                size = 40.dp,
                background =
                    softButton,
                onClick =
                    toggleLike
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Favorite,
                    contentDescription =
                        if (liked) {
                            "Unlike"
                        } else {
                            "Like"
                        },
                    tint =
                        if (liked) {
                            accent
                        } else {
                            iconColor
                        },
                    modifier =
                        Modifier.size(
                            21.dp
                        )
                )
            }

            Spacer(
                Modifier.width(7.dp)
            )

            PremiumCircle(
                size = 40.dp,
                background =
                    softButton,
                onClick =
                    openCategories
            ) {
                Icon(
                    imageVector =
                        if (inCategory) {
                            Icons.Rounded.Star
                        } else {
                            Icons.Rounded.StarBorder
                        },
                    contentDescription =
                        "Categories",
                    tint =
                        if (inCategory) {
                            accent
                        } else {
                            iconColor
                        },
                    modifier =
                        Modifier.size(
                            21.dp
                        )
                )
            }
        }

        XmoCapsule(
            background =
                softButton,
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .offset(
                        y = (-4).dp
                    )
                    .padding(
                        end = 4.dp
                    )
        ) {
            SleepRingButton(
                remainingMs =
                    sleepRemainingMs,
                totalMs =
                    sleepTotalMs,
                colors =
                    colors,
                accent =
                    accent,
                iconColor =
                    iconColor,
                onClick =
                    openSleep
            )

            CapsuleButton(
                size = 40.dp,
                onClick =
                    openQueue
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.QueueMusic,
                    contentDescription =
                        "Queue",
                    tint =
                        iconColor,
                    modifier =
                        Modifier.size(
                            21.dp
                        )
                )
            }

            CapsuleButton(
                size = 40.dp,
                onClick =
                    openDetails
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Info,
                    contentDescription =
                        "Song details",
                    tint =
                        iconColor,
                    modifier =
                        Modifier.size(
                            21.dp
                        )
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .fillMaxWidth()
                    .padding(
                        start = 4.dp,
                        end = 8.dp
                    )
        ) {
            Text(
                text =
                    title.ifBlank {
                        "Unknown song"
                    },
                color =
                    colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize =
                    23.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                text =
                    artist.ifBlank {
                        "Unknown artist"
                    },
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.medium,
                fontSize =
                    14.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .simpleTap(
                            openArtist
                        )
            )
        }
    }
}

@Composable
private fun SleepRingButton(
    remainingMs: Long,
    totalMs: Long?,
    colors: HomeColors,
    accent: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    val active =
        remainingMs > 0L

    val progress =
        if (
            active &&
            totalMs != null &&
            totalMs > 0L
        ) {
            (
                remainingMs.toFloat() /
                    totalMs.toFloat()
                )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            null
        }

    Box(
        Modifier.size(40.dp),
        contentAlignment =
            Alignment.Center
    ) {
        CapsuleButton(
            size = 40.dp,
            onClick = onClick
        ) {
            Icon(
                imageVector =
                    Icons.Rounded.Schedule,
                contentDescription =
                    "Sleep timer",
                tint =
                    if (active) {
                        accent
                    } else {
                        iconColor
                    },
                modifier =
                    Modifier.size(
                        21.dp
                    )
            )
        }

        if (progress != null) {
            Canvas(
                Modifier.size(32.dp)
            ) {
                drawArc(
                    color = accent,
                    startAngle = -90f,
                    sweepAngle =
                        360f * progress,
                    useCenter = false,
                    style =
                        Stroke(
                            width =
                                1.5.dp.toPx(),
                            cap =
                                StrokeCap.Round
                        )
                )
            }
        }
    }
}
