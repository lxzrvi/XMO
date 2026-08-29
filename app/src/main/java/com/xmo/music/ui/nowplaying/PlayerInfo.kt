package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Clock3
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
    sleepActive: Boolean,
    colors: HomeColors,
    accent: Color,
    toggleLike: () -> Unit,
    openCategories: () -> Unit,
    openSleep: () -> Unit,
    openQueue: () -> Unit,
    openDetails: () -> Unit,
    openArtist: () -> Unit
) {
    /*
     * Fixed-height region lets the action capsule remain anchored
     * at the top while title/artist independently move downward.
     */
    Box(
        Modifier
            .fillMaxWidth()
            .height(102.dp)
    ) {
        XmoCapsule(
            background =
                colors.button,
            modifier =
                Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(end = 4.dp)
        ) {
            CapsuleButton(
                size = 40.dp,
                onClick = toggleLike
            ) {
                FilledHeart(
                    filled = liked,
                    color =
                        if (liked) {
                            accent
                        } else {
                            colors.icon
                        }
                )
            }

            CapsuleButton(
                size = 40.dp,
                onClick =
                    openCategories
            ) {
                FilledStar(
                    filled =
                        inCategory,
                    color =
                        if (inCategory) {
                            accent
                        } else {
                            colors.icon
                        }
                )
            }

            CapsuleButton(
                size = 40.dp,
                onClick = openSleep
            ) {
                Icon(
                    imageVector =
                        Lucide.Clock3,
                    contentDescription =
                        "Sleep timer",
                    tint =
                        if (sleepActive) {
                            accent
                        } else {
                            colors.icon
                        },
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 40.dp,
                onClick = openQueue
            ) {
                Icon(
                    imageVector =
                        Lucide.ListMusic,
                    contentDescription =
                        "Queue",
                    tint = colors.icon,
                    modifier =
                        Modifier.size(18.dp)
                )
            }

            CapsuleButton(
                size = 40.dp,
                onClick = openDetails
            ) {
                Text(
                    text = "?",
                    color = colors.icon,
                    fontFamily =
                        XmoFont.bold,
                    fontSize = 18.sp
                )
            }
        }

        /*
         * This block is deliberately lower than the capsule.
         */
        Column(
            Modifier
                .align(
                    Alignment.BottomStart
                )
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 8.dp,
                    bottom = 2.dp
                )
        ) {
            Text(
                text =
                    title.ifBlank {
                        "Unknown song"
                    },
                color = colors.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 23.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            PressButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 1.dp
                        ),
                onClick =
                    openArtist
            ) {
                Text(
                    text =
                        artist.ifBlank {
                            "Unknown artist"
                        },
                    color = colors.sub,
                    fontFamily =
                        XmoFont.medium,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow =
                        TextOverflow.Ellipsis,
                    modifier =
                        Modifier.fillMaxWidth()
                )
            }
        }
    }
}
