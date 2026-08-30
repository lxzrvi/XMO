package com.xmo.music.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.xmo.music.ui.LocalXmoAccent
import com.xmo.music.ui.XmoFont
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

@Composable
internal fun HomeCategoryDock(
    sections: Map<String, HomeSectionModel>,
    order: List<String>,
    selected: String,
    c: HomeColors,
    select: (String) -> Unit,
    commit: (List<String>) -> Unit,
    add: () -> Unit
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val edge = with(density) { 62.dp.toPx() }

    var preview by remember(order) {
        mutableStateOf(order)
    }

    var draggingId by remember {
        mutableStateOf<String?>(null)
    }

    var fingerX by remember {
        mutableFloatStateOf(0f)
    }

    var grabX by remember {
        mutableFloatStateOf(0f)
    }

    var autoScroll by remember {
        mutableStateOf<Job?>(null)
    }

    LaunchedEffect(order) {
        if (draggingId == null) {
            preview = order
        }
    }

    fun itemInfo(id: String): LazyListItemInfo? =
        state.layoutInfo.visibleItemsInfo.firstOrNull {
            it.key == "category_$id"
        }

    fun stopAutoScroll() {
        autoScroll?.cancel()
        autoScroll = null
    }

    fun moveFromFinger(id: String) {
        val from = preview.indexOf(id)
        if (from < 0) return

        var destination = from

        state.layoutInfo.visibleItemsInfo.forEach { item ->
            val key = item.key as? String ?: return@forEach

            if (!key.startsWith("category_")) {
                return@forEach
            }

            val candidate =
                key.removePrefix("category_")

            if (candidate == id) {
                return@forEach
            }

            val candidateIndex =
                preview.indexOf(candidate)

            if (candidateIndex < 0) {
                return@forEach
            }

            val center =
                item.offset + item.size / 2f

            if (candidateIndex < from && fingerX < center) {
                destination =
                    minOf(destination, candidateIndex)
            }

            if (candidateIndex > from && fingerX > center) {
                destination =
                    maxOf(destination, candidateIndex)
            }
        }

        if (destination != from) {
            val next = preview.toMutableList()
            val moving = next.removeAt(from)

            next.add(
                destination.coerceIn(0, next.size),
                moving
            )

            preview = next
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        LazyRow(
            state = state,
            userScrollEnabled = draggingId == null,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 10.dp,
                vertical = 6.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(key = "all") {
                FixedCategoryChip(
                    text = "All",
                    active = selected == "all",
                    c = c,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Home,
                            contentDescription = null,
                            tint = if (selected == "all") {
                                LocalXmoAccent.current
                            } else {
                                c.icon
                            },
                            modifier = Modifier.size(15.dp)
                        )
                    }
                ) {
                    if (draggingId == null) {
                        select("all")
                    }
                }
            }

            items(
                items = preview,
                key = { "category_$it" }
            ) { id ->
                val section =
                    sections[id] ?: return@items

                val dragging =
                    draggingId == id

                Box(
                    Modifier
                        .graphicsLayer {
                            alpha =
                                if (dragging) .12f else 1f
                        }
                        .pointerInput(id, order) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { local ->
                                    val info =
                                        itemInfo(id)
                                            ?: return@detectDragGesturesAfterLongPress

                                    draggingId = id
                                    fingerX = info.offset + local.x
                                    grabX = local.x

                                    haptic.performHapticFeedback(
                                        HapticFeedbackType.LongPress
                                    )
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    fingerX += amount.x
                                    moveFromFinger(id)

                                    val start =
                                        state.layoutInfo
                                            .viewportStartOffset
                                            .toFloat()

                                    val end =
                                        state.layoutInfo
                                            .viewportEndOffset
                                            .toFloat()

                                    val left =
                                        fingerX < start + edge &&
                                            state.canScrollBackward

                                    val right =
                                        fingerX > end - edge &&
                                            state.canScrollForward

                                    if (left || right) {
                                        val direction =
                                            if (left) -1f else 1f

                                        if (autoScroll?.isActive != true) {
                                            autoScroll = scope.launch {
                                                while (
                                                    isActive &&
                                                    draggingId == id
                                                ) {
                                                    val consumed =
                                                        state.scrollBy(
                                                            direction * 15f
                                                        )

                                                    moveFromFinger(id)

                                                    if (abs(consumed) < .1f) {
                                                        break
                                                    }

                                                    delay(16L)
                                                }
                                            }
                                        }
                                    } else {
                                        stopAutoScroll()
                                    }
                                },
                                onDragEnd = {
                                    stopAutoScroll()
                                    val result = preview.toList()
                                    draggingId = null
                                    fingerX = 0f
                                    grabX = 0f
                                    commit(result)
                                },
                                onDragCancel = {
                                    stopAutoScroll()
                                    draggingId = null
                                    preview = order
                                    fingerX = 0f
                                    grabX = 0f
                                }
                            )
                        }
                ) {
                    CategoryChip(
                        text = section.title,
                        active = selected == id,
                        c = c,
                        icon = section.icon,
                        tint = section.tint ?: c.icon
                    ) {
                        if (draggingId == null) {
                            select(id)
                        }
                    }
                }
            }

            item(key = "add") {
                FixedCategoryChip(
                    text = "Add",
                    active = false,
                    c = c,
                    icon = {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = LocalXmoAccent.current,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    onClick = add
                )
            }
        }

        draggingId?.let { id ->
            val section =
                sections[id] ?: return@let

            Box(
                Modifier
                    .zIndex(100f)
                    .graphicsLayer {
                        translationX = fingerX - grabX
                        translationY = 5.dp.toPx()
                        scaleX = 1.06f
                        scaleY = 1.06f
                        shadowElevation = 12.dp.toPx()
                    }
            ) {
                CategoryChip(
                    text = section.title,
                    active = true,
                    c = c,
                    icon = section.icon,
                    tint = section.tint ?: c.icon
                ) {}
            }
        }
    }
}

@Composable
private fun CategoryChip(
    text: String,
    active: Boolean,
    c: HomeColors,
    icon: Int,
    tint: Color = c.icon,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val accent = LocalXmoAccent.current

    Row(
        modifier
            .background(
                if (active) {
                    accent.copy(alpha = .17f)
                } else {
                    c.button
                },
                RoundedCornerShape(18.dp)
            )
            .border(
                .6.dp,
                if (active) {
                    accent.copy(alpha = .34f)
                } else {
                    c.border
                },
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 13.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        XmoIcon(
            icon = icon,
            tint = if (active) accent else tint,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = text,
            color = if (active) accent else c.text,
            fontFamily = XmoFont.medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun FixedCategoryChip(
    text: String,
    active: Boolean,
    c: HomeColors,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    val accent = LocalXmoAccent.current

    Row(
        Modifier
            .background(
                if (active) {
                    accent.copy(alpha = .17f)
                } else {
                    c.button
                },
                RoundedCornerShape(18.dp)
            )
            .border(
                .6.dp,
                if (active) {
                    accent.copy(alpha = .34f)
                } else {
                    c.border
                },
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(
                horizontal = 13.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon()

        Text(
            text = text,
            color = if (active) accent else c.text,
            fontFamily = XmoFont.medium,
            fontSize = 12.sp
        )
    }
}
