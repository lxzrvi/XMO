package com.xmo.music.ui.nowplaying

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.xmo.music.data.Song
import com.xmo.music.ui.HomeColors
import kotlinx.coroutines.launch

@Composable
internal fun QueueSheet(
    queue: List<Song>,
    currentSongId: Long?,
    colors: HomeColors,
    playIndex: (Int) -> Unit,
    dismiss: () -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val configuration =
        LocalConfiguration.current

    val density =
        LocalDensity.current

    val sheetHeight =
        configuration
            .screenHeightDp
            .dp *
            .72f

    val initialOffsetPx =
        with(density) {
            sheetHeight.toPx() *
                XmoPlayerAnimation
                    .queueInitialOffsetFraction
        }

    val sheetY =
        remember {
            Animatable(
                initialOffsetPx
            )
        }

    var sheetHeightPx by
        remember {
            mutableFloatStateOf(
                with(density) {
                    sheetHeight.toPx()
                }
                    .coerceAtLeast(1f)
            )
        }

    var menuIndex by
        remember {
            mutableStateOf<Int?>(
                null
            )
        }

    var closing by
        remember {
            mutableStateOf(false)
        }

    LaunchedEffect(Unit) {
        sheetY.animateTo(
            targetValue = 0f,
            animationSpec =
                XmoPlayerAnimation
                    .queueEnterSpec
        )
    }

    suspend fun closeSheet() {
        if (closing) {
            return
        }

        closing = true

        sheetY.animateTo(
            targetValue =
                sheetHeightPx,
            animationSpec =
                XmoPlayerAnimation
                    .queueExitSpec
        )

        dismiss()
    }

    BackHandler {
        if (menuIndex != null) {
            menuIndex = null
        } else {
            scope.launch {
                closeSheet()
            }
        }
    }

    Box(
        modifier =
            Modifier.fillMaxSize()
    ) {
        /*
         * Invisible backdrop: it intercepts the tap, but it does
         * not tint the Now Playing screen.
         */
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .simpleTap {
                        scope.launch {
                            closeSheet()
                        }
                    }
        )

        Column(
            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .height(
                        sheetHeight
                    )
                    .onSizeChanged {
                        sheetHeightPx =
                            it.height
                                .toFloat()
                                .coerceAtLeast(1f)
                    }
                    .graphicsLayer {
                        translationY =
                            sheetY.value
                    }
                    .clip(
                        RoundedCornerShape(
                            topStart = 30.dp,
                            topEnd = 30.dp
                        )
                    )
                    .background(
                        colors.surface
                    )
        ) {
            QueueHandle(
                colors = colors,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .pointerInput(
                            sheetHeightPx
                        ) {
                            detectDragGestures(
                                onDrag = {
                                        change,
                                        amount ->

                                    if (
                                        amount.y > 0f ||
                                        sheetY.value > 0f
                                    ) {
                                        change.consume()

                                        scope.launch {
                                            sheetY.snapTo(
                                                (
                                                    sheetY.value +
                                                        amount.y
                                                    )
                                                    .coerceIn(
                                                        0f,
                                                        sheetHeightPx
                                                    )
                                            )
                                        }
                                    }
                                },

                                onDragEnd = {
                                    scope.launch {
                                        if (
                                            sheetY.value >
                                            sheetHeightPx *
                                                XmoPlayerAnimation
                                                    .queueDismissThreshold
                                        ) {
                                            closeSheet()
                                        } else {
                                            sheetY.animateTo(
                                                targetValue = 0f,
                                                animationSpec =
                                                    XmoPlayerAnimation
                                                        .queueSettleSpec
                                            )
                                        }
                                    }
                                },

                                onDragCancel = {
                                    scope.launch {
                                        sheetY.animateTo(
                                            targetValue = 0f,
                                            animationSpec =
                                                XmoPlayerAnimation
                                                    .queueSettleSpec
                                        )
                                    }
                                }
                            )
                        }
            )

            QueueContent(
                queue = queue,
                currentSongId =
                    currentSongId,
                colors = colors,
                playIndex =
                    playIndex,
                openMenu = {
                    menuIndex = it
                }
            )
        }

        menuIndex?.let { index ->
            queue
                .getOrNull(index)
                ?.let { song ->
                    QueueActionMenu(
                        song = song,
                        active =
                            song.id ==
                                currentSongId,
                        colors = colors,
                        play = {
                            playIndex(index)
                            menuIndex = null
                        },
                        dismiss = {
                            menuIndex = null
                        }
                    )
                }
        }
    }
}
