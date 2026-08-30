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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Text
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
    val inactiveHeart =
        when (theme) {
            XmoTheme.Light ->
                Color(0xFF202126)

            XmoTheme.Dark,
            XmoTheme.Amoled ->
                Color.White
        }

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(104.dp)
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
                /*
                 * Always filled:
                 * inactive = neutral/white
                 * liked = XMO accent red
                 */
                FilledHeart(
                    filled = true,
                    color =
                        if (liked) {
                            accent
                        } else {
                            inactiveHeart
                        }
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
                PremiumQueueIcon(
                    color =
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
                PremiumInfoIcon(
                    color =
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
            PremiumClockIcon(
                color =
                    if (active) {
                        accent
                    } else {
                        colors.icon
                    },
                modifier =
                    Modifier.size(
                        19.dp
                    )
            )
        }

        if (progress != null) {
            Canvas(
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
                        360f * progress,
                    useCenter =
                        false,
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

@Composable
private fun PremiumClockIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.minDimension * .095f

        drawCircle(
            color = color,
            radius =
                size.minDimension * .31f,
            center = center,
            style =
                Stroke(
                    width = stroke
                )
        )

        drawLine(
            color = color,
            start = center,
            end =
                Offset(
                    center.x,
                    size.height * .31f
                ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        drawLine(
            color = color,
            start = center,
            end =
                Offset(
                    size.width * .64f,
                    size.height * .57f
                ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PremiumQueueIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.minDimension * .11f

        val ys =
            listOf(
                .31f,
                .50f,
                .69f
            )

        ys.forEachIndexed {
                index,
                value ->

            drawLine(
                color = color,
                start =
                    Offset(
                        size.width * .22f,
                        size.height * value
                    ),
                end =
                    Offset(
                        size.width *
                            if (index == 1) {
                                .73f
                            } else {
                                .63f
                            },
                        size.height * value
                    ),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }

        drawCircle(
            color = color,
            radius =
                size.minDimension * .075f,
            center =
                Offset(
                    size.width * .75f,
                    size.height * .68f
                )
        )

        drawLine(
            color = color,
            start =
                Offset(
                    size.width * .81f,
                    size.height * .30f
                ),
            end =
                Offset(
                    size.width * .81f,
                    size.height * .67f
                ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun PremiumInfoIcon(
    color: Color,
    modifier: Modifier
) {
    Canvas(modifier) {
        val stroke =
            size.minDimension * .095f

        drawCircle(
            color = color,
            radius =
                size.minDimension * .31f,
            center = center,
            style =
                Stroke(
                    width = stroke
                )
        )

        drawCircle(
            color = color,
            radius =
                size.minDimension * .055f,
            center =
                Offset(
                    center.x,
                    size.height * .35f
                )
        )

        drawLine(
            color = color,
            start =
                Offset(
                    center.x,
                    size.height * .49f
                ),
            end =
                Offset(
                    center.x,
                    size.height * .67f
                ),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
