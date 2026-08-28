package com.xmo.music.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.data.UserCategory
import com.xmo.music.data.XmoProfile
import java.util.UUID

internal data class SetupAvatar(
    val label: String,
    val icon: Int?
)

internal val XmoSetupAvatars =
    listOf(
        SetupAvatar(
            "Profile",
            null
        ),
        SetupAvatar(
            "Headphones",
            R.drawable.ic_xmo_songs
        ),
        SetupAvatar(
            "Album",
            R.drawable.ic_xmo_album
        ),
        SetupAvatar(
            "Artist",
            R.drawable.ic_xmo_artist
        ),
        SetupAvatar(
            "Bolt",
            R.drawable.ic_xmo_bolt
        ),
        SetupAvatar(
            "Spark",
            R.drawable.ic_xmo_spark
        )
    )

@Composable
fun Setup(
    initialProfile: XmoProfile,
    existingCategories: List<UserCategory>,
    onCategoriesChanged: (List<UserCategory>) -> Unit,
    finish: (XmoProfile) -> Unit,
    setupLater: (XmoProfile) -> Unit
) {
    val context =
        LocalContext.current

    val activity =
        context as? Activity

    val audioPermission =
        if (
            Build.VERSION.SDK_INT >= 33
        ) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    val notificationPermission =
        if (
            Build.VERSION.SDK_INT >= 33
        ) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

    var audioGranted by
        remember {
            mutableStateOf(
                context.hasPermission(
                    audioPermission
                )
            )
        }

    var notificationGranted by
        remember {
            mutableStateOf(
                notificationPermission == null ||
                    context.hasPermission(
                        notificationPermission
                    )
            )
        }

    var username by
        remember {
            mutableStateOf(
                initialProfile.name
            )
        }

    var selectedAvatar by
        remember {
            mutableIntStateOf(
                initialProfile.avatarIndex
                    .coerceIn(
                        XmoSetupAvatars.indices
                    )
            )
        }

    var customAvatarUri by
        remember {
            mutableStateOf(
                initialProfile.avatarUri
                    ?.let(
                        Uri::parse
                    )
            )
        }

    var categoryName by
        remember {
            mutableStateOf("")
        }

    var setupCategories by
        remember {
            mutableStateOf(
                existingCategories
            )
        }

    val audioLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            audioGranted =
                it
        }

    val notificationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            notificationGranted =
                it
        }

    val photoPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .PickVisualMedia()
        ) { uri ->

            if (
                uri != null
            ) {
                customAvatarUri =
                    uri

                /*
                 * First avatar slot becomes the custom image.
                 */
                selectedAvatar =
                    0
            }
        }

    fun profile():
        XmoProfile =
        XmoProfile(
            name =
                username
                    .trim()
                    .ifBlank {
                        "XMO User"
                    }
                    .take(32),

            avatarUri =
                if (
                    selectedAvatar == 0
                ) {
                    customAvatarUri
                        ?.toString()
                } else {
                    null
                },

            avatarIndex =
                selectedAvatar
        )

    val nameReady =
        username
            .trim()
            .isNotEmpty()

    val canContinue =
        audioGranted &&
            nameReady

    /*
     * Fixed-height page instead of a long onboarding list:
     * avatar stays centred, permissions/actions remain near the
     * bottom, and the layout stays edge-to-edge.
     */
    Column(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0D0F12),
                        Color(0xFF101115),
                        Color(0xFF08090B)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(
                horizontal = 16.dp
            )
    ) {
        Text(
            text =
                "XMO",

            color =
                Color.White,

            fontFamily =
                XmoFont.logo,

            fontSize =
                28.sp,

            letterSpacing =
                4.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 32.dp
                    )
        )

        Text(
            text =
                "Make XMO yours",

            color =
                Color.White.copy(
                    alpha = .55f
                ),

            fontFamily =
                XmoFont.thin,

            fontSize =
                11.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 5.dp
                    )
        )

        /*
         * PFP is intentionally lower than the previous layout.
         */
        Spacer(
            Modifier.height(
                42.dp
            )
        )

        SetupAvatarChooser(
            avatars =
                XmoSetupAvatars,

            selected =
                selectedAvatar,

            customUri =
                customAvatarUri,

            choose = {
                selectedAvatar =
                    it
            },

            pickCustom = {
                photoPicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts
                            .PickVisualMedia
                            .ImageOnly
                    )
                )
            }
        )

        Spacer(
            Modifier.height(
                24.dp
            )
        )

        SetupInput(
            value =
                username,

            hint =
                "Enter your name",

            onValue = {
                username =
                    it.take(32)
            }
        )

        Spacer(
            Modifier.height(
                12.dp
            )
        )

        /*
         * Optional category creation.
         */
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            SetupInput(
                value =
                    categoryName,

                hint =
                    "Add a category",

                onValue = {
                    categoryName =
                        it.take(24)
                },

                modifier =
                    Modifier.weight(1f)
            )

            Spacer(
                Modifier.size(
                    8.dp
                )
            )

            SetupSmallButton(
                text =
                    "Add",

                enabled =
                    categoryName
                        .trim()
                        .isNotEmpty()
            ) {
                val clean =
                    categoryName.trim()

                if (
                    clean.isNotEmpty()
                ) {
                    val next =
                        setupCategories +
                            UserCategory(
                                id =
                                    "cat_${UUID.randomUUID()}",

                                name =
                                    clean,

                                icon =
                                    setupCategories.size %
                                        4
                            )

                    setupCategories =
                        next

                    categoryName =
                        ""

                    onCategoriesChanged(
                        next
                    )
                }
            }
        }

        if (
            setupCategories.isNotEmpty()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 9.dp
                    )
                    .horizontalScroll(
                        rememberScrollState()
                    ),

                horizontalArrangement =
                    Arrangement.spacedBy(
                        7.dp
                    )
            ) {
                setupCategories.forEach { category ->

                    Row(
                        Modifier
                            .clip(
                                RoundedCornerShape(
                                    18.dp
                                )
                            )
                            .background(
                                Color.White.copy(
                                    alpha = .055f
                                )
                            )
                            .border(
                                .7.dp,
                                Color.White.copy(
                                    alpha = .08f
                                ),
                                RoundedCornerShape(
                                    18.dp
                                )
                            )
                            .padding(
                                start = 12.dp,
                                end = 4.dp,
                                top = 5.dp,
                                bottom = 5.dp
                            ),

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {
                        Text(
                            category.name,

                            color =
                                Color.White,

                            fontFamily =
                                XmoFont.medium,

                            fontSize =
                                10.sp,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )

                        Box(
                            Modifier
                                .size(
                                    26.dp
                                )
                                .clip(
                                    CircleShape
                                )
                                .clickable {
                                    val next =
                                        setupCategories
                                            .filterNot {
                                                it.id ==
                                                    category.id
                                            }

                                    setupCategories =
                                        next

                                    onCategoriesChanged(
                                        next
                                    )
                                },

                            contentAlignment =
                                Alignment.Center
                        ) {
                            Icon(
                                imageVector =
                                    Icons.Default.Close,

                                contentDescription =
                                    "Remove",

                                tint =
                                    Color.White.copy(
                                        alpha = .52f
                                    ),

                                modifier =
                                    Modifier.size(
                                        13.dp
                                    )
                            )
                        }
                    }
                }
            }
        }

        /*
         * Push permissions to the lower portion of the screen.
         */
        Spacer(
            Modifier.weight(
                1f
            )
        )

        Column(
            verticalArrangement =
                Arrangement.spacedBy(
                    9.dp
                )
        ) {
            SetupPermissionRow(
                title =
                    "Audio & Storage Access",

                subtitle =
                    "Required to find and play your local music",

                granted =
                    audioGranted,

                required =
                    true
            ) {
                if (
                    !audioGranted
                ) {
                    audioLauncher.launch(
                        audioPermission
                    )
                }
            }

            SetupPermissionRow(
                title =
                    "Notification Permission",

                subtitle =
                    if (
                        notificationPermission ==
                        null
                    ) {
                        "Not required on this Android version"
                    } else {
                        "Playback controls and media notifications"
                    },

                granted =
                    notificationGranted,

                required =
                    false
            ) {
                if (
                    notificationPermission != null &&
                    !notificationGranted
                ) {
                    notificationLauncher.launch(
                        notificationPermission
                    )
                }
            }
        }

        Spacer(
            Modifier.height(
                14.dp
            )
        )

        /*
         * Setup Later cannot bypass music permission anymore.
         */
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp
                )
        ) {
            SetupAction(
                text =
                    "Exit",

                modifier =
                    Modifier.weight(1f),

                background =
                    XmoRed.copy(
                        alpha = .12f
                    ),

                color =
                    XmoRed
            ) {
                activity?.finish()
            }

            SetupAction(
                text =
                    "Setup Later",

                modifier =
                    Modifier.weight(1f),

                background =
                    if (
                        canContinue
                    ) {
                        Color.White.copy(
                            alpha = .07f
                        )
                    } else {
                        Color.White.copy(
                            alpha = .025f
                        )
                    },

                color =
                    if (
                        canContinue
                    ) {
                        Color.White
                    } else {
                        Color.White.copy(
                            alpha = .28f
                        )
                    },

                enabled =
                    canContinue
            ) {
                setupLater(
                    profile()
                )
            }

            SetupAction(
                text =
                    "Start Now",

                modifier =
                    Modifier.weight(1f),

                background =
                    if (
                        canContinue
                    ) {
                        XmoRed
                    } else {
                        Color.White.copy(
                            alpha = .05f
                        )
                    },

                color =
                    if (
                        canContinue
                    ) {
                        Color.White
                    } else {
                        Color.White.copy(
                            alpha = .30f
                        )
                    },

                enabled =
                    canContinue
            ) {
                finish(
                    profile()
                )
            }
        }

        Text(
            text =
                "XMO stays local. Your music, profile and library data remain on this device.",

            color =
                Color.White.copy(
                    alpha = .40f
                ),

            fontFamily =
                XmoFont.thin,

            fontSize =
                8.sp,

            lineHeight =
                11.sp,

            textAlign =
                TextAlign.Center,

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 10.dp,
                        bottom = 8.dp
                    )
        )
    }
}

@Composable
internal fun SetupAvatarChooser(
    avatars: List<SetupAvatar>,
    selected: Int,
    customUri: Uri?,
    choose: (Int) -> Unit,
    pickCustom: () -> Unit
) {
    /*
     * Three-column construction keeps the selected PFP physically
     * centred. The neighbouring choices scroll independently on
     * either side instead of shifting the selected card off-centre.
     */
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(
                    114.dp
                ),
            contentAlignment =
                Alignment.Center
        ) {
            val activeScale by
                animateFloatAsState(
                    targetValue =
                        1.08f,

                    animationSpec =
                        spring(
                            dampingRatio = .74f,
                            stiffness = 430f
                        ),

                    label =
                        "selectedAvatarScale"
                )

            Box(
                Modifier
                    .size(
                        96.dp
                    )
                    .graphicsLayer {
                        scaleX =
                            activeScale

                        scaleY =
                            activeScale
                    }
                    .clip(
                        CircleShape
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(
                                XmoRed.copy(
                                    alpha = .27f
                                ),
                                Color.White.copy(
                                    alpha = .045f
                                )
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
                AvatarContent(
                    avatar =
                        avatars[
                            selected.coerceIn(
                                avatars.indices
                            )
                        ],

                    index =
                        selected,

                    customUri =
                        customUri,

                    active =
                        true,

                    modifier =
                        Modifier
                            .size(
                                84.dp
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
                        start = 76.dp,
                        top = 5.dp
                    )
                    .size(
                        30.dp
                    )
                    .clip(
                        CircleShape
                    )
                    .background(
                        XmoRed
                    )
                    .border(
                        2.dp,
                        Color(0xFF0D0F12),
                        CircleShape
                    )
                    .clickable(
                        onClick =
                            pickCustom
                    ),

                contentAlignment =
                    Alignment.Center
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Add,

                    contentDescription =
                        "Choose profile picture",

                    tint =
                        Color.White,

                    modifier =
                        Modifier.size(
                            16.dp
                        )
                )
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                )
                .padding(
                    top = 7.dp,
                    bottom = 2.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    10.dp
                )
        ) {
            avatars.forEachIndexed {
                    index,
                    avatar ->

                val active =
                    selected ==
                        index

                Box(
                    Modifier
                        .size(
                            52.dp
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
                                Color.White.copy(
                                    alpha = .035f
                                )
                            }
                        )
                        .border(
                            if (
                                active
                            ) {
                                1.3.dp
                            } else {
                                .7.dp
                            },

                            if (
                                active
                            ) {
                                XmoRed
                            } else {
                                Color.White.copy(
                                    alpha = .08f
                                )
                            },

                            CircleShape
                        )
                        .clickable {
                            choose(
                                index
                            )
                        },

                    contentAlignment =
                        Alignment.Center
                ) {
                    AvatarContent(
                        avatar =
                            avatar,

                        index =
                            index,

                        customUri =
                            customUri,

                        active =
                            active,

                        modifier =
                            Modifier
                                .size(
                                    44.dp
                                )
                                .clip(
                                    CircleShape
                                )
                    )
                }
            }
        }
    }
}

@Composable
internal fun AvatarContent(
    avatar: SetupAvatar,
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
        when {
            index == 0 &&
                customUri != null -> {

                AsyncImage(
                    model =
                        customUri,

                    contentDescription =
                        null,

                    contentScale =
                        ContentScale.Crop,

                    modifier =
                        Modifier.fillMaxSize()
                )
            }

            avatar.icon != null -> {

                XmoIcon(
                    icon =
                        avatar.icon,

                    tint =
                        Color.White.copy(
                            alpha =
                                if (
                                    active
                                ) {
                                    1f
                                } else {
                                    .52f
                                }
                        ),

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

            else -> {
                /*
                 * This exact first built-in avatar becomes the
                 * default everywhere when no photo was selected.
                 */
                Icon(
                    imageVector =
                        Icons.Default.Person,

                    contentDescription =
                        null,

                    tint =
                        Color.White.copy(
                            alpha =
                                if (
                                    active
                                ) {
                                    1f
                                } else {
                                    .52f
                                }
                        ),

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
            }
        }
    }
}

@Composable
internal fun SetupInput(
    value: String,
    hint: String,
    onValue: (String) -> Unit,
    modifier: Modifier =
        Modifier
) {
    Row(
        modifier
            .height(
                48.dp
            )
            .clip(
                RoundedCornerShape(
                    15.dp
                )
            )
            .background(
                Color.White.copy(
                    alpha = .045f
                )
            )
            .border(
                .8.dp,
                Color.White.copy(
                    alpha = .10f
                ),
                RoundedCornerShape(
                    15.dp
                )
            )
            .padding(
                horizontal = 15.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        BasicTextField(
            value =
                value,

            onValueChange =
                onValue,

            singleLine =
                true,

            textStyle =
                androidx.compose.ui.text.TextStyle(
                    color =
                        Color.White,

                    fontFamily =
                        XmoFont.normal,

                    fontSize =
                        14.sp
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
                        value.isEmpty()
                    ) {
                        Text(
                            hint,

                            color =
                                Color.White.copy(
                                    alpha = .38f
                                ),

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
}

@Composable
private fun SetupSmallButton(
    text: String,
    enabled: Boolean,
    click: () -> Unit
) {
    Box(
        Modifier
            .height(
                48.dp
            )
            .clip(
                RoundedCornerShape(
                    15.dp
                )
            )
            .background(
                if (
                    enabled
                ) {
                    XmoRed.copy(
                        alpha = .17f
                    )
                } else {
                    Color.White.copy(
                        alpha = .025f
                    )
                }
            )
            .border(
                .8.dp,
                if (
                    enabled
                ) {
                    XmoRed.copy(
                        alpha = .40f
                    )
                } else {
                    Color.White.copy(
                        alpha = .06f
                    )
                },
                RoundedCornerShape(
                    15.dp
                )
            )
            .clickable(
                enabled =
                    enabled,

                onClick =
                    click
            )
            .padding(
                horizontal =
                    17.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text,

            color =
                if (
                    enabled
                ) {
                    XmoRed
                } else {
                    Color.White.copy(
                        alpha = .28f
                    )
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
        )
    }
}

@Composable
private fun SetupPermissionRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    required: Boolean,
    click: () -> Unit
) {
    val green =
        Color(0xFF34C759)

    val background by
        animateColorAsState(
            targetValue =
                if (
                    granted
                ) {
                    green.copy(
                        alpha = .075f
                    )
                } else {
                    Color.White.copy(
                        alpha = .035f
                    )
                },

            label =
                "permissionBackground"
        )

    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    20.dp
                )
            )
            .background(
                background
            )
            .border(
                .8.dp,

                if (
                    granted
                ) {
                    green.copy(
                        alpha = .38f
                    )
                } else if (
                    required
                ) {
                    XmoRed.copy(
                        alpha = .32f
                    )
                } else {
                    Color.White.copy(
                        alpha = .08f
                    )
                },

                RoundedCornerShape(
                    20.dp
                )
            )
            .clickable(
                enabled =
                    !granted,

                onClick =
                    click
            )
            .padding(
                horizontal = 16.dp,
                vertical = 11.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(
                    10.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    if (
                        granted
                    ) {
                        green
                    } else if (
                        required
                    ) {
                        XmoRed
                    } else {
                        Color.White.copy(
                            alpha = .42f
                        )
                    }
                )
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    start = 12.dp,
                    end = 8.dp
                )
        ) {
            Text(
                title,

                color =
                    Color.White,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    12.sp
            )

            Text(
                subtitle,

                color =
                    Color.White.copy(
                        alpha = .48f
                    ),

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    9.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Text(
            if (
                granted
            ) {
                "Allowed"
            } else {
                "Allow"
            },

            color =
                if (
                    granted
                ) {
                    green
                } else {
                    Color.White.copy(
                        alpha = .65f
                    )
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                10.sp
        )
    }
}

@Composable
private fun SetupAction(
    text: String,
    modifier: Modifier,
    background: Color,
    color: Color,
    enabled: Boolean = true,
    click: () -> Unit
) {
    Box(
        modifier
            .height(
                44.dp
            )
            .clip(
                RoundedCornerShape(
                    14.dp
                )
            )
            .background(
                background
            )
            .border(
                .7.dp,
                if (
                    enabled
                ) {
                    background.copy(
                        alpha = 1f
                    )
                } else {
                    Color.White.copy(
                        alpha = .04f
                    )
                },
                RoundedCornerShape(
                    14.dp
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
                color,

            fontFamily =
                XmoFont.medium,

            fontSize =
                10.sp,

            textAlign =
                TextAlign.Center
        )
    }
}

private fun Context.hasPermission(
    permission: String
): Boolean =
    ContextCompat
        .checkSelfPermission(
            this,
            permission
        ) ==
        PackageManager.PERMISSION_GRANTED
