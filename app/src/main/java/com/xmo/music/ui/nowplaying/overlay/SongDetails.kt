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
            Animatable(0f)
        }

    LaunchedEffect(Unit) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec =
                XmoPlayerAnimation
                    .overlayRevealSpec
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
                        Modifier.weight(1f)
                )

                PremiumCircle(
                    size = 37.dp,
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
                Modifier.height(13.dp)
            )

            song?.let { current ->
                DetailValue(
                    "Title",
                    current.title,
                    colors
                )

                DetailValue(
                    "Artist",
                    current.artist,
                    colors
                )

                DetailValue(
                    "Album",
                    album,
                    colors
                )

                current.metadata?.let { meta ->
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
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 5.dp
                )
    ) {
        Text(
            text = label,
            color =
                colors.text,
            fontFamily =
                XmoFont.bold,
            fontSize =
                11.sp,
            modifier =
                Modifier.width(92.dp)
        )

        Text(
            text = value,
            color =
                colors.sub,
            fontFamily =
                XmoFont.normal,
            fontSize =
                11.sp,
            maxLines = 2,
            textAlign =
                TextAlign.End,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.weight(1f)
        )
    }
}
