package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.launch

@Composable
internal fun ArtistInfoBox(
    artist: String,
    trackCount: Int,
    colors: HomeColors,
    close: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val reveal =
        remember {
            Animatable(
                0f
            )
        }

    LaunchedEffect(Unit) {
        reveal.animateTo(
            targetValue =
                1f,
            animationSpec =
                tween(
                    durationMillis =
                        250,
                    easing =
                        FastOutSlowInEasing
                )
        )
    }

    val progress =
        reveal.value
            .coerceIn(
                0f,
                1f
            )

    suspend fun closeAnimated() {
        reveal.animateTo(
            targetValue =
                0f,
            animationSpec =
                tween(
                    durationMillis =
                        180,
                    easing =
                        FastOutSlowInEasing
                )
        )

        close()
    }

    Box(
        modifier =
            Modifier.fillMaxSize(),
        contentAlignment =
            Alignment.Center
    ) {
        /*
         * Independent animated backdrop.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(
                            alpha =
                                .28f *
                                    progress
                        )
                    )
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
                        horizontal =
                            32.dp
                    )
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha =
                            progress

                        val scale =
                            .95f +
                                .05f *
                                progress

                        scaleX =
                            scale

                        scaleY =
                            scale

                        translationY =
                            (
                                1f -
                                    progress
                                ) *
                                22f
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
                        18.dp
                    ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            /*
             * Same Material Rounded family as the rest of the
             * revised Now Playing controls.
             */
            Box(
                modifier =
                    Modifier
                        .size(
                            52.dp
                        )
                        .clip(
                            RoundedCornerShape(
                                18.dp
                            )
                        )
                        .background(
                            colors.button
                        ),
                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Rounded.Person,
                    contentDescription =
                        null,
                    tint =
                        colors.text,
                    modifier =
                        Modifier.size(
                            27.dp
                        )
                )
            }

            Spacer(
                modifier =
                    Modifier.height(
                        11.dp
                    )
            )

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
                modifier =
                    Modifier.height(
                        7.dp
                    )
            )

            /*
             * trackCount remains the actual full MediaStore
             * artist count supplied by NowPlaying.
             */
            Text(
                text =
                    if (
                        trackCount ==
                        1
                    ) {
                        "1 track on this device"
                    } else {
                        "$trackCount tracks on this device"
                    },
                color =
                    colors.sub,
                fontFamily =
                    XmoFont.normal,
                fontSize =
                    11.sp,
                textAlign =
                    TextAlign.Center
            )

            Spacer(
                modifier =
                    Modifier.height(
                        16.dp
                    )
            )

            PremiumCircle(
                size =
                    39.dp,
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
                        Modifier.size(
                            21.dp
                        )
                )
            }
        }
    }
}
