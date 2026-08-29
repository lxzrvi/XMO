package com.xmo.music.ui.nowplaying

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Share2
import com.composables.icons.lucide.Star
import com.composables.icons.lucide.Trash2
import com.composables.icons.lucide.X
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont

@Composable
internal fun SongOptionsBox(
    song: Song?,
    categories: List<UserCategory>,
    colors: HomeColors,
    liked: Boolean,
    close: () -> Unit,
    toggleLike: () -> Unit,
    share: () -> Unit,
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
                    alpha = .32f
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

                    song?.artist?.let {
                        Text(
                            text = it,
                            color = colors.sub,
                            fontFamily =
                                XmoFont.normal,
                            fontSize = 10.sp,
                            maxLines = 1
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
                            Modifier.size(
                                17.dp
                            )
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
                click =
                    toggleLike
            )

            OverlayAction(
                icon = Lucide.Share2,
                title = "Share Song",
                colors = colors,
                click = share
            )

            /*
             * Visible only. No fake MediaStore deletion.
             */
            OverlayAction(
                icon = Lucide.Trash2,
                title = "Delete Song",
                trailing =
                    "Coming later",
                colors = colors,
                enabled = false
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
                        title =
                            category.name,
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
                        RoundedCornerShape(
                            14.dp
                        )
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
                    value =
                        newCategory,
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
                            fontSize =
                                12.sp
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
                        val value =
                            newCategory
                                .trim()

                        if (
                            value.isNotEmpty() &&
                            createCategory(
                                value
                            )
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
                            Modifier.size(
                                17.dp
                            )
                    )
                }
            }
        }
    }
}
