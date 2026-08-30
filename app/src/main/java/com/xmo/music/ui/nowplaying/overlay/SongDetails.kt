package com.xmo.music.ui.nowplaying

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.data.Song
import com.xmo.music.ui.HomeColors
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.launch

@Composable
internal fun SongDetailsBox(
    song: Song?,
    album: String,
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
         * Animated backdrop.
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
                            24.dp
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
                    /*
                     * Consume card touches so backdrop does not
                     * close when interacting with the details.
                     */
                    .simpleTap {}
                    .padding(
                        17.dp
                    )
        ) {
            Row(
                modifier =
                    Modifier.fillMaxWidth(),
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Text(
                    text =
                        "Song Details",
                    color =
                        colors.text,
                    fontFamily =
                        XmoFont.bold,
                    fontSize =
                        17.sp,
                    modifier =
                        Modifier.weight(
                            1f
                        )
                )

                PremiumCircle(
                    size =
                        37.dp,
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
                                20.dp
                            )
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(
                        13.dp
                    )
            )

            song?.let { current ->
                DetailValue(
                    label =
                        "Title",
                    value =
                        current.title,
                    colors =
                        colors
                )

                DetailValue(
                    label =
                        "Artist",
                    value =
                        current.artist,
                    colors =
                        colors
                )

                DetailValue(
                    label =
                        "Album",
                    value =
                        album,
                    colors =
                        colors
                )

                current.metadata
                    ?.let { meta ->

                        meta.genre?.let {
                            DetailValue(
                                label =
                                    "Genre",
                                value =
                                    it,
                                colors =
                                    colors
                            )
                        }

                        meta.year?.let {
                            DetailValue(
                                label =
                                    "Year",
                                value =
                                    it.toString(),
                                colors =
                                    colors
                            )
                        }

                        meta.trackNumber?.let {
                            DetailValue(
                                label =
                                    "Track",
                                value =
                                    it.toString(),
                                colors =
                                    colors
                            )
                        }

                        meta.discNumber?.let {
                            DetailValue(
                                label =
                                    "Disc",
                                value =
                                    it.toString(),
                                colors =
                                    colors
                            )
                        }

                        meta.composer?.let {
                            DetailValue(
                                label =
                                    "Composer",
                                value =
                                    it,
                                colors =
                                    colors
                            )
                        }

                        meta.writer?.let {
                            DetailValue(
                                label =
                                    "Writer",
                                value =
                                    it,
                                colors =
                                    colors
                            )
                        }

                        meta.bitrate?.let {
                            DetailValue(
                                label =
                                    "Bitrate",
                                value =
                                    "${it / 1000} kbps",
                                colors =
                                    colors
                            )
                        }

                        meta.sampleRate?.let {
                            DetailValue(
                                label =
                                    "Sample rate",
                                value =
                                    "$it Hz",
                                colors =
                                    colors
                            )
                        }

                        meta.channelCount?.let {
                            DetailValue(
                                label =
                                    "Channels",
                                value =
                                    it.toString(),
                                colors =
                                    colors
                            )
                        }

                        meta.mimeType?.let {
                            DetailValue(
                                label =
                                    "Type",
                                value =
                                    it,
                                colors =
                                    colors
                            )
                        }

                        meta.fileName?.let {
                            DetailValue(
                                label =
                                    "File",
                                value =
                                    it,
                                colors =
                                    colors
                            )
                        }

                        meta.sizeBytes?.let {
                            DetailValue(
                                label =
                                    "Size",
                                value =
                                    formatBytes(
                                        it
                                    ),
                                colors =
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
    if (
        value.isBlank()
    ) {
        return
    }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical =
                        5.dp
                )
    ) {
        Text(
            text =
                label,
            color =
                colors.text,
            fontFamily =
                XmoFont.bold,
            fontSize =
                11.sp,
            modifier =
                Modifier.width(
                    92.dp
                )
        )

        Text(
            text =
                value,
            color =
                colors.sub,
            fontFamily =
                XmoFont.normal,
            fontSize =
                11.sp,
            maxLines =
                2,
            textAlign =
                TextAlign.End,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.weight(
                    1f
                )
        )
    }
}
