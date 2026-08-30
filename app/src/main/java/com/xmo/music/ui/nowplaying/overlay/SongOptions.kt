package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.launch

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
    var newCategory by
        remember {
            mutableStateOf("")
        }

    val accent =
        LocalXmoAccent.current

    val scope =
        rememberCoroutineScope()

    val reveal =
        remember {
            Animatable(0f)
        }

    var closing by
        remember {
            mutableStateOf(false)
        }

    LaunchedEffect(Unit) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec =
                XmoPlayerAnimation
                    .overlayRevealSpec
        )
    }

    suspend fun closeAnimated() {
        if (closing) {
            return
        }

        closing = true

        reveal.animateTo(
            targetValue = 0f,
            animationSpec =
                XmoPlayerAnimation
                    .overlayHideSpec
        )

        close()
    }

    val progress =
        reveal.value
            .coerceIn(
                0f,
                1f
            )

    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        /*
         * No visible black backdrop.
         * This layer exists only as the outside-tap target.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .simpleTap {
                        scope.launch {
                            closeAnimated()
                        }
                    }
        )

        Column(
            modifier =
                Modifier
                    .padding(
                        horizontal = 24.dp
                    )
                    .fillMaxWidth()
                    .graphicsLayer {
                        with(
                            XmoPlayerAnimation
                        ) {
                            centerOverlay(
                                progress
                            )
                        }
                    }
                    .clip(
                        RoundedCornerShape(
                            25.dp
                        )
                    )
                    .background(
                        colors.surface
                    )
                    .simpleTap {}
                    .padding(
                        16.dp
                    )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Column(
                    modifier =
                        Modifier.weight(1f)
                ) {
                    Text(
                        text =
                            song?.title
                                ?: "Song Options",
                        color =
                            colors.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize =
                            16.sp,
                        maxLines = 1,
                        overflow =
                            TextOverflow.Ellipsis
                    )

                    song?.artist?.let {
                        Text(
                            text = it,
                            color =
                                colors.sub,
                            fontFamily =
                                XmoFont.normal,
                            fontSize =
                                10.sp,
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
                    onClick = {
                        scope.launch {
                            closeAnimated()
                        }
                    }
                ) {
                    Icon(
                        imageVector =
                            Icons.Rounded.Close,
                        contentDescription =
                            "Close",
                        tint =
                            colors.text,
                        modifier =
                            Modifier.size(20.dp)
                    )
                }
            }

            Spacer(
                Modifier.height(12.dp)
            )

            OverlayAction(
                icon =
                    if (liked) {
                        Icons.Rounded.Favorite
                    } else {
                        Icons.Rounded.FavoriteBorder
                    },
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
                icon =
                    Icons.Rounded.Share,
                title =
                    "Share Song",
                colors = colors,
                click = share
            )

            OverlayAction(
                icon =
                    Icons.Rounded.Delete,
                title =
                    "Delete Song",
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
                        icon =
                            if (added) {
                                Icons.Rounded.Star
                            } else {
                                Icons.Rounded.StarBorder
                            },
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
                modifier =
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
                            fontSize =
                                12.sp
                        ),
                    modifier =
                        Modifier.weight(1f),
                    decorationBox = {
                            inner ->

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

                            inner()
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
                            Icons.Rounded.Add,
                        contentDescription =
                            "Create category",
                        tint = accent,
                        modifier =
                            Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
