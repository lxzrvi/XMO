package com.xmo.music.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.XmoTheme
import com.xmo.music.data.XmoProfile

val LocalXmoProfile =
    staticCompositionLocalOf {
        XmoProfile()
    }

/*
 * =============================================================
 * PROFILE EDITOR
 *
 * Uses the same avatar language as first-run Setup.
 * Nothing is persisted until Apply is pressed.
 * =============================================================
 */

@Composable
fun ProfileEditor(
    profile: XmoProfile,
    theme: XmoTheme,
    apply: (XmoProfile) -> Unit,
    cancel: () -> Unit
) {
    val c =
        homeColors(
            theme
        )

    var name by
        remember(
            profile
        ) {
            mutableStateOf(
                profile.name
            )
        }

    var selectedAvatar by
        remember(
            profile
        ) {
            mutableIntStateOf(
                profile.avatarIndex
                    .coerceIn(
                        XmoSetupAvatars.indices
                    )
            )
        }

    var customUri by
        remember(
            profile
        ) {
            mutableStateOf(
                profile.avatarUri
                    ?.let(
                        Uri::parse
                    )
            )
        }

    val photoPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .PickVisualMedia()
        ) { uri ->

            if (
                uri != null
            ) {
                customUri =
                    uri

                selectedAvatar =
                    0
            }
        }

    val valid =
        name
            .trim()
            .isNotEmpty()

    fun result():
        XmoProfile =
        XmoProfile(
            name =
                name
                    .trim()
                    .ifBlank {
                        "XMO User"
                    }
                    .take(32),

            avatarUri =
                if (
                    selectedAvatar ==
                    0
                ) {
                    customUri
                        ?.toString()
                } else {
                    null
                },

            avatarIndex =
                selectedAvatar
        )

    Column(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(
                horizontal =
                    18.dp
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(
                    62.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                "Cancel",

                color =
                    c.sub,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    12.sp,

                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        )
                        .clickable(
                            onClick =
                                cancel
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        )
            )

            Text(
                "Profile",

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    18.sp,

                textAlign =
                    TextAlign.Center,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            Text(
                "Apply",

                color =
                    if (
                        valid
                    ) {
                        XmoRed
                    } else {
                        c.sub.copy(
                            alpha = .35f
                        )
                    },

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    12.sp,

                modifier =
                    Modifier
                        .clip(
                            RoundedCornerShape(
                                16.dp
                            )
                        )
                        .clickable(
                            enabled =
                                valid
                        ) {
                            apply(
                                result()
                            )
                        }
                        .padding(
                            horizontal = 10.dp,
                            vertical = 8.dp
                        )
            )
        }

        Spacer(
            Modifier.height(
                40.dp
            )
        )

        /*
         * Large centred active PFP.
         */
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    132.dp
                ),

            contentAlignment =
                Alignment.Center
        ) {
            val scale by
                animateFloatAsState(
                    targetValue =
                        1.06f,

                    animationSpec =
                        spring(
                            dampingRatio =
                                .75f,

                            stiffness =
                                420f
                        ),

                    label =
                        "profileAvatar"
                )

            Box(
                Modifier
                    .size(
                        104.dp
                    )
                    .graphicsLayer {
                        scaleX =
                            scale

                        scaleY =
                            scale
                    }
                    .clip(
                        CircleShape
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(
                                XmoRed.copy(
                                    alpha = .24f
                                ),
                                c.button
                            )
                        )
                    )
                    .border(
                        2.dp,
                        XmoRed,
                        CircleShape
                    ),

                contentAlignment =
                    Alignment.Center
            ) {
                ProfileAvatarContent(
                    index =
                        selectedAvatar,

                    customUri =
                        customUri,

                    active =
                        true,

                    modifier =
                        Modifier
                            .size(
                                91.dp
                            )
                            .clip(
                                CircleShape
                            )
                )
            }

            Box(
                Modifier
                    .align(
                        Alignment.TopCenter
                    )
                    .padding(
                        start = 82.dp,
                        top = 8.dp
                    )
                    .size(
                        32.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        XmoRed
                    )
                    .border(
                        2.dp,
                        c.bg,
                        CircleShape
                    )
                    .clickable {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts
                                    .PickVisualMedia
                                    .ImageOnly
                            )
                        )
                    },

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        "Change profile picture",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            17.dp
                        )
                )
            }
        }

        /*
         * Same built-in PFP choices as Setup.
         */
        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                )
                .padding(
                    top = 14.dp,
                    bottom = 28.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    11.dp
                )
        ) {
            XmoSetupAvatars
                .forEachIndexed {
                        index,
                        _ ->

                    val active =
                        selectedAvatar ==
                            index

                    Box(
                        Modifier
                            .size(
                                54.dp
                            )
                            .clip(
                                CircleShape
                            )
                            .background(
                                if (
                                    active
                                ) {
                                    XmoRed.copy(
                                        alpha = .14f
                                    )
                                } else {
                                    c.button
                                }
                            )
                            .border(
                                if (
                                    active
                                ) {
                                    1.4.dp
                                } else {
                                    .7.dp
                                },

                                if (
                                    active
                                ) {
                                    XmoRed
                                } else {
                                    c.border
                                },

                                CircleShape
                            )
                            .clickable {
                                selectedAvatar =
                                    index
                            },

                        contentAlignment =
                            Alignment.Center
                    ) {
                        ProfileAvatarContent(
                            index =
                                index,

                            customUri =
                                customUri,

                            active =
                                active,

                            modifier =
                                Modifier
                                    .size(
                                        46.dp
                                    )
                                    .clip(
                                        CircleShape
                                    )
                        )
                    }
                }
        }

        Text(
            "NAME",

            color =
                XmoRed,

            fontFamily =
                XmoFont.bold,

            fontSize =
                10.sp,

            letterSpacing =
                1.2.sp,

            modifier =
                Modifier.padding(
                    start = 5.dp,
                    bottom = 8.dp
                )
        )

        Row(
            Modifier
                .fillMaxWidth()
                .height(
                    52.dp
                )
                .clip(
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .background(
                    c.button
                )
                .border(
                    1.dp,
                    if (
                        valid
                    ) {
                        c.border
                    } else {
                        XmoRed.copy(
                            alpha = .45f
                        )
                    },
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .padding(
                    horizontal =
                        16.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            BasicTextField(
                value =
                    name,

                onValueChange = {
                    name =
                        it.take(32)
                },

                singleLine =
                    true,

                textStyle =
                    androidx.compose.ui.text
                        .TextStyle(
                            color =
                                c.text,

                            fontFamily =
                                XmoFont.user,

                            fontSize =
                                16.sp
                        ),

                modifier =
                    Modifier.weight(
                        1f
                    ),

                decorationBox = {
                        field ->

                    Box(
                        contentAlignment =
                            Alignment.CenterStart
                    ) {
                        if (
                            name.isEmpty()
                        ) {
                            Text(
                                "Enter your name",

                                color =
                                    c.sub,

                                fontFamily =
                                    XmoFont.thin,

                                fontSize =
                                    13.sp
                            )
                        }

                        field()
                    }
                }
            )
        }

        Text(
            "${name.length}/32",

            color =
                c.sub,

            fontFamily =
                XmoFont.thin,

            fontSize =
                9.sp,

            textAlign =
                TextAlign.End,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 6.dp,
                        end = 4.dp
                    )
        )

        Spacer(
            Modifier.weight(
                1f
            )
        )

        /*
         * Large bottom actions as well, useful on one-handed
         * devices. Apply/Cancel semantics remain identical.
         */
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    bottom = 18.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            ProfileAction(
                text =
                    "Cancel",

                modifier =
                    Modifier.weight(
                        1f
                    ),

                background =
                    c.button,

                foreground =
                    c.text,

                border =
                    c.border,

                enabled =
                    true,

                click =
                    cancel
            )

            ProfileAction(
                text =
                    "Apply",

                modifier =
                    Modifier.weight(
                        1f
                    ),

                background =
                    if (
                        valid
                    ) {
                        XmoRed
                    } else {
                        c.button
                    },

                foreground =
                    if (
                        valid
                    ) {
                        Color.White
                    } else {
                        c.sub.copy(
                            alpha = .38f
                        )
                    },

                border =
                    if (
                        valid
                    ) {
                        XmoRed
                    } else {
                        c.border
                    },

                enabled =
                    valid
            ) {
                apply(
                    result()
                )
            }
        }
    }
}

/*
 * =============================================================
 * SHARED PROFILE AVATAR
 *
 * This is also used by Home so index 0 has exactly the same
 * default Person appearance as Setup/Profile instead of a name
 * initial.
 * =============================================================
 */

@Composable
fun XmoProfileAvatar(
    profile: XmoProfile,
    modifier: Modifier = Modifier,
    foreground: Color = Color.White,
    background: Color = XmoRed,
    border: Color =
        Color.White.copy(
            alpha = .12f
        )
) {
    val uri =
        remember(
            profile.avatarUri
        ) {
            profile.avatarUri
                ?.let(
                    Uri::parse
                )
        }

    Box(
        modifier
            .clip(
                CircleShape
            )
            .background(
                Brush.linearGradient(
                    listOf(
                        background,
                        background.copy(
                            alpha = .42f
                        )
                    )
                )
            )
            .border(
                .7.dp,
                border,
                CircleShape
            ),

        contentAlignment =
            Alignment.Center
    ) {
        if (
            profile.avatarIndex == 0 &&
            uri != null
        ) {
            AsyncImage(
                model =
                    uri,

                contentDescription =
                    "Profile picture",

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )
        } else {
            val icon =
                profileAvatarIcon(
                    profile.avatarIndex
                )

            if (
                icon == null
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Person,

                    contentDescription =
                        "Profile",

                    tint =
                        foreground,

                    modifier =
                        Modifier.size(
                            20.dp
                        )
                )
            } else {
                XmoIcon(
                    icon =
                        icon,

                    tint =
                        foreground,

                    modifier =
                        Modifier.size(
                            19.dp
                        )
                )
            }
        }
    }
}

@Composable
private fun ProfileAvatarContent(
    index: Int,
    customUri: Uri?,
    active: Boolean,
    modifier: Modifier
) {
    Box(
        modifier,
        contentAlignment =
            Alignment.Center
    ) {
        if (
            index == 0 &&
            customUri != null
        ) {
            AsyncImage(
                model =
                    customUri,

                contentDescription =
                    null,

                modifier =
                    Modifier.fillMaxSize(),

                contentScale =
                    ContentScale.Crop
            )

            return@Box
        }

        val icon =
            profileAvatarIcon(
                index
            )

        val tint =
            Color.White.copy(
                alpha =
                    if (
                        active
                    ) {
                        1f
                    } else {
                        .55f
                    }
            )

        if (
            icon == null
        ) {
            Icon(
                imageVector =
                    Icons.Default.Person,

                contentDescription =
                    null,

                tint =
                    tint,

                modifier =
                    Modifier.size(
                        if (
                            active
                        ) {
                            29.dp
                        } else {
                            21.dp
                        }
                    )
            )
        } else {
            XmoIcon(
                icon =
                    icon,

                tint =
                    tint,

                modifier =
                    Modifier.size(
                        if (
                            active
                        ) {
                            27.dp
                        } else {
                            20.dp
                        }
                    )
            )
        }
    }
}

private fun profileAvatarIcon(
    index: Int
): Int? =
    XmoSetupAvatars
        .getOrNull(
            index
        )
        ?.icon

@Composable
private fun ProfileAction(
    text: String,
    modifier: Modifier,
    background: Color,
    foreground: Color,
    border: Color,
    enabled: Boolean,
    click: () -> Unit
) {
    Box(
        modifier
            .height(
                48.dp
            )
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
            .background(
                background
            )
            .border(
                .8.dp,
                border,
                RoundedCornerShape(
                    16.dp
                )
            )
            .clickable(
                enabled =
                    enabled,

                onClick =
                    click
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text,

            color =
                foreground,

            fontFamily =
                XmoFont.medium,

            fontSize =
                12.sp,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis
        )
    }
}
