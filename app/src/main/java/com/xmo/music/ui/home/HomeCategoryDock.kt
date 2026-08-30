package com.xmo.music.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
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

    var preview by remember(order) {
        mutableStateOf(order)
    }

    var dragging by remember {
        mutableStateOf<String?>(null)
    }

    var dragX by remember {
        mutableFloatStateOf(0f)
    }

    var autoJob by remember {
        mutableStateOf<Job?>(null)
    }

    LaunchedEffect(order) {
        if (dragging == null) {
            preview = order
        }
    }

    fun reorder(id: String) {
        val from = preview.indexOf(id)
        if (from < 0) return

        val target =
            state.layoutInfo.visibleItemsInfo
                .filter {
                    (it.key as? String)
                        ?.startsWith("cat_") == true
                }
                .minByOrNull {
                    kotlin.math.abs(
                        dragX -
                            (it.offset + it.size / 2f)
                    )
                }
                ?.key
                ?.toString()
                ?.removePrefix("cat_")
                ?.let(preview::indexOf)
                ?: return

        if (target == from || target < 0) {
            return
        }

        preview =
            preview.toMutableList().also {
                val item = it.removeAt(from)
                it.add(
                    target.coerceIn(0, it.size),
                    item
                )
            }
    }

    LazyRow(
        state = state,
        userScrollEnabled = dragging == null,
        contentPadding =
            PaddingValues(horizontal = 12.dp),
        horizontalArrangement =
            Arrangement.spacedBy(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        item(key = "all") {
            CategoryLabel(
                title = "All",
                selected = selected == "all",
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
                        modifier = Modifier.size(16.dp)
                    )
                }
            ) {
                select("all")
            }
        }

        items(
            items = preview,
            key = { "cat_$it" }
        ) { id ->
            val section =
                sections[id] ?: return@items

            val isDragging =
                dragging == id

            Box(
                Modifier
                    .graphicsLayer {
                        alpha =
                            if (isDragging) .25f else 1f
                    }
                    .pointerInput(id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                dragging = id
                                dragX =
                                    state.layoutInfo
                                        .visibleItemsInfo
                                        .firstOrNull {
                                            it.key == "cat_$id"
                                        }
                                        ?.offset
                                        ?.toFloat()
                                        ?: 0f

                                haptic.performHapticFeedback(
                                    HapticFeedbackType.LongPress
                                )
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragX += amount.x
                                reorder(id)

                                val viewport =
                                    state.layoutInfo

                                val direction =
                                    when {
                                        dragX <
                                            viewport.viewportStartOffset +
                                            80f -> -1f

                                        dragX >
                                            viewport.viewportEndOffset -
                                            80f -> 1f

                                        else -> 0f
                                    }

                                if (
                                    direction != 0f &&
                                    autoJob?.isActive != true
                                ) {
                                    autoJob = scope.launch {
                                        while (
                                            isActive &&
                                            dragging == id
                                        ) {
                                            state.scrollBy(
                                                direction * 14f
                                            )
                                            reorder(id)
                                            delay(16L)
                                        }
                                    }
                                } else if (direction == 0f) {
                                    autoJob?.cancel()
                                }
                            },
                            onDragEnd = {
                                autoJob?.cancel()
                                dragging = null
                                commit(preview)
                            },
                            onDragCancel = {
                                autoJob?.cancel()
                                dragging = null
                                preview = order
                            }
                        )
                    }
            ) {
                CategoryLabel(
                    title = section.title,
                    selected = selected == id,
                    c = c,
                    icon = {
                        XmoIcon(
                            icon = section.icon,
                            tint =
                                if (selected == id) {
                                    LocalXmoAccent.current
                                } else {
                                    c.icon
                                },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                ) {
                    if (dragging == null) {
                        select(id)
                    }
                }
            }
        }

        item(key = "add") {
            CategoryLabel(
                title = "Add",
                selected = false,
                c = c,
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = c.icon,
                        modifier = Modifier.size(17.dp)
                    )
                },
                click = add
            )
        }
    }
}

@Composable
private fun CategoryLabel(
    title: String,
    selected: Boolean,
    c: HomeColors,
    icon: @Composable () -> Unit,
    click: () -> Unit
) {
    Row(
        Modifier
            .height(48.dp)
            .clickable(onClick = click),
        verticalAlignment =
            Alignment.CenterVertically,
        horizontalArrangement =
            Arrangement.spacedBy(6.dp)
    ) {
        icon()

        Text(
            text = title,
            color =
                if (selected) {
                    LocalXmoAccent.current
                } else {
                    c.text
                },
            fontFamily = XmoFont.medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
