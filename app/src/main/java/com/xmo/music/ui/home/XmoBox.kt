package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.xmo.music.ui.XmoFont

@Composable
internal fun XmoBox(
    title: String,
    c: HomeColors,
    dismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    BackHandler(onBack = dismiss)

    Dialog(
        onDismissRequest = dismiss,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(120))
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            c.surface,
                            RoundedCornerShape(24.dp)
                        )
                        .border(
                            .7.dp,
                            c.border,
                            RoundedCornerShape(24.dp)
                        )
                        .padding(18.dp)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp),
                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            color = c.text,
                            fontFamily = XmoFont.logo,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = dismiss,
                            modifier =
                                Modifier.background(
                                    c.button,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = c.sub
                            )
                        }
                    }

                    content()
                }
            }
        }
    }
}
