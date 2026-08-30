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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.HomeColors
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

    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
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
                                reveal.value
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
                click =
                    toggleLike
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

            Spacer(
                Modifier.height(14.dp)
            )

            CategoryPickerContent(
                song = song,
                categories =
                    categories,
                colors = colors,
                setCategory =
                    setCategory,
                createCategory =
                    createCategory
            )
        }
    }
}
