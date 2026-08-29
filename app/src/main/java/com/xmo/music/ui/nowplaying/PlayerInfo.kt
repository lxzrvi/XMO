package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.composables.icons.lucide.Clock3
import com.composables.icons.lucide.Info
import com.composables.icons.lucide.ListMusic
import com.composables.icons.lucide.Lucide
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
    toggleLike: () -> Unit,
    openCategories: () -> Unit,
    openSleep: () -> Unit,
    openQueue: () -> Unit,
    openDetails: () -> Unit,
    openArtist: () -> Unit
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(
                    104.dp
                )
    ) {
        Row(
            modifier =
                Modifier
                    .align(
                        Alignment.TopStart
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
                FilledHeart(
                    filled =
                        liked,
                    color =
                        if (
                            liked
                        ) {
                            accent
                        } else {
                            colors.icon
                        }
                )
            }

            Spacer(
                modifier =
                    Modifier.width(
                        7.dp
                    )
            )

            PremiumCircle(
                size = 40.dp,
                background =
                    softButton,
                onClick =
                    openCategories
            ) {
                FilledStar(
                    filled =
                        inCategory,
                    color =
                        if (
                            inCategory
                        ) {
                            accent
                        } else {
                            colors.icon
                        }
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
                        Lucide.ListMusic,
                    contentDescription =
                        "Queue",
                    tint =
                        colors.icon,
                    modifier =
                        Modifier.size(
                            19.dp
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
                        Lucide.Info,
                    contentDescription =
                        "Song details",
                    tint =
                        colors.icon,
                    modifier =
                        Modifier.size(
                            19.dp
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
    onClick: () -> Unit
) {
    val active =
        remainingMs >
            0L

    val progress =
        if (
            active &&
            totalMs != null &&
            totalMs > 0L
        ) {
            (
                remainingMs
                    .toFloat() /
                    totalMs
                        .toFloat()
                )
                .coerceIn(
                    0f,
                    1f
                )
        } else {
            null
        }

    Box(
        modifier =
            Modifier.size(
                40.dp
            ),
        contentAlignment =
            Alignment.Center
    ) {
        CapsuleButton(
            size = 40.dp,
            onClick =
                onClick
        ) {
            Icon(
                imageVector =
                    Lucide.Clock3,
                contentDescription =
                    "Sleep timer",
                tint =
                    if (
                        active
                    ) {
                        accent
                    } else {
                        colors.icon
                    },
                modifier =
                    Modifier.size(
                        18.dp
                    )
            )
        }

        /*
         * No made-up percentage. Ring only exists when the
         * original timer duration is actually known.
         */
        if (
            progress != null
        ) {
            Canvas(
                modifier =
                    Modifier.size(
                        32.dp
                    )
            ) {
                drawArc(
                    color =
                        accent,
                    startAngle =
                        -90f,
                    sweepAngle =
                        360f *
                            progress,
                    useCenter =
                        false,
                    style =
                        Stroke(
                            width =
                                1.5.dp
                                    .toPx(),
                            cap =
                                StrokeCap.Round
                        )
                )
            }
        }
    }
}
