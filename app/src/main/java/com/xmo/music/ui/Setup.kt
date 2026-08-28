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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.data.UserCategory
import com.xmo.music.data.XmoProfile
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

private data class SetupAvatar(
    val label: String,
    val icon: Int?
)

@Composable
fun Setup(
    initialProfile: XmoProfile,
    existingCategories: List<UserCategory>,
    onCategoriesChanged: (
        List<UserCategory>
    ) -> Unit,
    finish: (
        XmoProfile
    ) -> Unit,
    setupLater: (
        XmoProfile
    ) -> Unit
) {
    val context =
        LocalContext.current

    val activity =
        context as? Activity

    val scope =
        rememberCoroutineScope()

    val colors =
        homeColors(
            com.xmo.music.XmoTheme.Dark
        )

    val audioPermission =
        if (
            Build.VERSION.SDK_INT >= 33
        ) {
            Manifest.permission
                .READ_MEDIA_AUDIO
        } else {
            Manifest.permission
                .READ_EXTERNAL_STORAGE
        }

    val notificationPermission =
        if (
            Build.VERSION.SDK_INT >= 33
        ) {
            Manifest.permission
                .POST_NOTIFICATIONS
        } else {
            null
        }

    var audioGranted by remember {
        mutableStateOf(
            context.hasPermission(
                audioPermission
            )
        )
    }

    var notificationGranted by remember {
        mutableStateOf(
            notificationPermission ==
                null ||
                context.hasPermission(
                    notificationPermission
                )
        )
    }

    var username by remember {
        mutableStateOf(
            initialProfile.name
        )
    }

    var selectedAvatar by remember {
        mutableIntStateOf(
            initialProfile.avatarIndex
        )
    }

    var customAvatarUri by remember {
        mutableStateOf<Uri?>(
            initialProfile.avatarUri
                ?.let(Uri::parse)
        )
    }

    var categoryName by remember {
        mutableStateOf("")
    }

    var setupCategories by remember {
        mutableStateOf(
            existingCategories
        )
    }

    val audioLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            audioGranted = it
        }

    val notificationLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .RequestPermission()
        ) {
            notificationGranted = it
        }

    /*
     * Android Photo Picker.
     *
     * No broad photo/storage permission.
     */
    val photoPicker =
        rememberLauncherForActivityResult(
            ActivityResultContracts
                .PickVisualMedia()
        ) { uri ->

            if (uri != null) {
                customAvatarUri =
                    uri

                selectedAvatar =
                    0
            }
        }

    val avatars =
        remember {
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
        }

    val ready =
        audioGranted &&
            username.trim()
                .isNotEmpty()

    fun currentProfile():
        XmoProfile {

        return XmoProfile(
            name =
                username
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
                    customAvatarUri
                        ?.toString()
                } else {
                    null
                },

            avatarIndex =
                selectedAvatar
        )
    }

    /*
     * Dark onboarding matches supplied reference.
     * Global theme can be selected later.
     */
    LazyColumn(
        Modifier
            .fillMaxSize()
            .background(
                Color(0xFF0D0F12)
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
            .navigationBarsPadding(),

        contentPadding =
            PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 16.dp
            )
    ) {
        item(
            key = "brand"
        ) {
            Text(
                "XMO",

                color =
                    Color.White,

                fontFamily =
                    XmoFont.logo,

                fontSize =
                    28.sp,

                letterSpacing =
                    4.sp,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 48.dp,
                            bottom = 32.dp
                        ),

                textAlign =
                    androidx.compose.ui.text.style
                        .TextAlign.Center
            )
        }

        /*
         * =====================================================
         * AVATAR
         * =====================================================
         */
        item(
            key = "avatar"
        ) {
            SetupAvatarChooser(
                avatars =
                    avatars,

                selected =
                    selectedAvatar,

                customUri =
                    customAvatarUri,

                choose = {
                    selectedAvatar = it
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
                    20.dp
                )
            )
        }

        /*
         * =====================================================
         * USERNAME
         * =====================================================
         */
        item(
            key = "name"
        ) {
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
                    16.dp
                )
            )
        }

        /*
         * =====================================================
         * INITIAL CATEGORIES
         * =====================================================
         */
        item(
            key = "categories"
        ) {
            Row(
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                SetupInput(
                    value =
                        categoryName,

                    hint =
                        "Add Category...",

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

                Box(
                    Modifier
                        .height(48.dp)
                        .clip(
                            RoundedCornerShape(
                                12.dp
                            )
                        )
                        .background(
                            Color.White.copy(
                                alpha = .06f
                            )
                        )
                        .clickable(
                            enabled =
                                categoryName
                                    .trim()
                                    .isNotEmpty()
                        ) {
                            val clean =
                                categoryName
                                    .trim()

                            if (
                                clean.isNotEmpty()
                            ) {
                                val category =
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
                                    setupCategories +
                                        category

                                onCategoriesChanged(
                                    setupCategories
                                )

                                categoryName =
                                    ""
                            }
                        }
                        .padding(
                            horizontal =
                                16.dp
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "Add",

                        color =
                            Color.White,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            12.sp
                    )
                }
            }

            if (
                setupCategories.isNotEmpty()
            ) {
                Spacer(
                    Modifier.height(
                        10.dp
                    )
                )

                /*
                 * Native horizontal compact pills.
                 * No fake/demo categories.
                 */
                LazyRow(
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    itemsIndexed(
                        items =
                            setupCategories,

                        key = { _, cat ->
                            cat.id
                        }
                    ) {
                            _,
                            category ->

                        Row(
                            Modifier
                                .clip(
                                    RoundedCornerShape(
                                        20.dp
                                    )
                                )
                                .background(
                                    Color.White.copy(
                                        alpha = .06f
                                    )
                                )
                                .border(
                                    .6.dp,
                                    Color.White.copy(
                                        alpha = .06f
                                    ),
                                    RoundedCornerShape(
                                        20.dp
                                    )
                                )
                                .padding(
                                    start = 12.dp,
                                    top = 7.dp,
                                    end = 6.dp,
                                    bottom = 7.dp
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
                                    11.sp,

                                maxLines =
                                    1,

                                overflow =
                                    TextOverflow.Ellipsis
                            )

                            Box(
                                Modifier
                                    .size(
                                        24.dp
                                    )
                                    .clip(
                                        CircleShape
                                    )
                                    .clickable {
                                        setupCategories =
                                            setupCategories
                                                .filterNot {
                                                    it.id ==
                                                        category.id
                                                }

                                        onCategoriesChanged(
                                            setupCategories
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
                                            alpha =
                                                .55f
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

            Spacer(
                Modifier.height(
                    28.dp
                )
            )
        }

        /*
         * =====================================================
         * PERMISSIONS
         * =====================================================
         */
        item(
            key = "permissions"
        ) {
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
                        "Allow playback alerts & updates"
                    },

                granted =
                    notificationGranted,

                click = {
                    if (
                        notificationPermission !=
                        null &&
                        !notificationGranted
                    ) {
                        notificationLauncher.launch(
                            notificationPermission
                        )
                    }
                }
            )

            Spacer(
                Modifier.height(
                    10.dp
                )
            )

            SetupPermissionRow(
                title =
                    "Audio & Storage Access",

                subtitle =
                    "Scan local device music files",

                granted =
                    audioGranted,

                click = {
                    if (!audioGranted) {
                        audioLauncher.launch(
                            audioPermission
                        )
                    }
                }
            )

            Spacer(
                Modifier.height(
                    18.dp
                )
            )
        }

        /*
         * =====================================================
         * ACTIONS
         * =====================================================
         */
        item(
            key = "actions"
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                SetupAction(
                    text = "Exit",
                    modifier =
                        Modifier.weight(
                            1f
                        ),
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
                        Modifier.weight(
                            1f
                        ),
                    background =
                        Color.White.copy(
                            alpha = .06f
                        ),
                    color =
                        Color.White
                ) {
                    setupLater(
                        currentProfile()
                    )
                }

                SetupAction(
                    text =
                        "Start Now",
                    modifier =
                        Modifier.weight(
                            1f
                        ),
                    background =
                        if (ready) {
                            Color(0xFF34C759)
                        } else {
                            Color.White.copy(
                                alpha = .12f
                            )
                        },
                    color =
                        if (ready) {
                            Color.White
                        } else {
                            Color.White.copy(
                                alpha = .40f
                            )
                        },
                    enabled =
                        ready
                ) {
                    finish(
                        currentProfile()
                    )
                }
            }

            Text(
                "XMO is offline. Your profile, library preferences and playback data stay on this device and are not sent to external servers.",

                color =
                    Color.White.copy(
                        alpha = .50f
                    ),

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    9.sp,

                lineHeight =
                    13.sp,

                textAlign =
                    androidx.compose.ui.text.style
                        .TextAlign.Center,

                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            top = 14.dp,
                            bottom = 6.dp
                        )
            )
        }
    }
}

@Composable
private fun SetupAvatarChooser(
    avatars: List<SetupAvatar>,
    selected: Int,
    customUri: Uri?,
    choose: (Int) -> Unit,
    pickCustom: () -> Unit
) {
    val state =
        rememberLazyListState(
            initialFirstVisibleItemIndex =
                selected.coerceAtLeast(
                    0
                )
        )

    /*
     * Keep selected card visible.
     */
    LaunchedEffect(selected) {
        if (
            selected in
            avatars.indices
        ) {
            state.animateScrollToItem(
                selected
            )
        }
    }

    Box(
        Modifier
            .fillMaxWidth()
            .height(
                112.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {
        /*
         * Selected target.
         */
        Box(
            Modifier
                .size(
                    92.dp
                )
                .border(
                    2.dp,
                    XmoRed,
                    CircleShape
                )
        )

        LazyRow(
            state =
                state,

            contentPadding =
                PaddingValues(
                    horizontal =
                        135.dp
                ),

            horizontalArrangement =
                Arrangement.spacedBy(
                    24.dp
                )
        ) {
            itemsIndexed(
                items =
                    avatars,

                key = {
                        index,
                        _ ->

                    index
                }
            ) {
                    index,
                    avatar ->

                val active =
                    selected ==
                        index

                Box(
                    Modifier
                        .size(
                            80.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            Color.White.copy(
                                alpha =
                                    if (active)
                                        .08f
                                    else
                                        .03f
                            )
                        )
                        .border(
                            .7.dp,
                            Color.White.copy(
                                alpha =
                                    if (active)
                                        .14f
                                    else
                                        .05f
                            ),
                            CircleShape
                        )
                        .clickable {
                            choose(index)
                        },

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
                    } else if (
                        avatar.icon != null
                    ) {
                        XmoIcon(
                            icon =
                                avatar.icon,

                            tint =
                                Color.White.copy(
                                    alpha =
                                        if (active)
                                            1f
                                        else
                                            .28f
                                ),

                            modifier =
                                Modifier.size(
                                    25.dp
                                )
                        )
                    } else {
                        Icon(
                            imageVector =
                                Icons.Default.Person,

                            contentDescription =
                                null,

                            tint =
                                Color.White.copy(
                                    alpha =
                                        if (active)
                                            1f
                                        else
                                            .28f
                                ),

                            modifier =
                                Modifier.size(
                                    25.dp
                                )
                        )
                    }
                }
            }
        }

        /*
         * Custom photo add button.
         */
        Box(
            Modifier
                .align(
                    Alignment.TopCenter
                )
                .padding(
                    start = 66.dp,
                    top = 11.dp
                )
                .size(
                    27.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    XmoRed
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
                        15.dp
                    )
            )
        }
    }
}

@Composable
private fun SetupInput(
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
                    12.dp
                )
            )
            .background(
                Color.White.copy(
                    alpha = .025f
                )
            )
            .border(
                1.dp,
                Color.White.copy(
                    alpha = .04f
                ),
                RoundedCornerShape(
                    12.dp
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
                    input ->

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
                                    alpha = .40f
                                ),

                            fontFamily =
                                XmoFont.thin,

                            fontSize =
                                13.sp
                        )
                    }

                    input()
                }
            }
        )
    }
}

@Composable
private fun SetupPermissionRow(
    title: String,
    subtitle: String,
    granted: Boolean,
    click: () -> Unit
) {
    val background by
        animateColorAsState(
            targetValue =
                if (granted) {
                    Color(0xFF34C759)
                        .copy(
                            alpha = .08f
                        )
                } else {
                    Color.White.copy(
                        alpha = .025f
                    )
                },

            label =
                "permission"
        )

    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    24.dp
                )
            )
            .background(
                background
            )
            .border(
                1.dp,

                if (granted) {
                    Color(0xFF34C759)
                        .copy(
                            alpha = .50f
                        )
                } else {
                    Color.White.copy(
                        alpha = .04f
                    )
                },

                RoundedCornerShape(
                    24.dp
                )
            )
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 18.dp,
                vertical = 12.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        /*
         * Naked status indicator.
         */
        Canvas(
            Modifier.size(
                18.dp
            )
        ) {
            drawCircle(
                color =
                    if (granted)
                        Color(0xFF34C759)
                    else
                        Color.White.copy(
                            alpha = .45f
                        ),

                radius =
                    size.minDimension *
                        .22f,

                center =
                    Offset(
                        size.width / 2f,
                        size.height / 2f
                    )
            )
        }

        Column(
            Modifier
                .weight(1f)
                .padding(
                    start = 12.dp,
                    end = 10.dp
                )
        ) {
            Text(
                title,

                color =
                    Color.White,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    13.sp
            )

            Text(
                subtitle,

                color =
                    Color.White.copy(
                        alpha = .50f
                    ),

                fontFamily =
                    XmoFont.thin,

                fontSize =
                    10.sp
            )
        }

        Text(
            if (granted)
                "Allowed"
            else
                "Allow",

            color =
                if (granted)
                    Color(0xFF34C759)
                else
                    Color.White.copy(
                        alpha = .55f
                    ),

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
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
                46.dp
            )
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(
                background
            )
            .border(
                .7.dp,
                background,
                RoundedCornerShape(
                    12.dp
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
                11.sp
        )
    }
}

private fun Context.hasPermission(
    permission: String
): Boolean {
    return ContextCompat
        .checkSelfPermission(
            this,
            permission
        ) ==
        PackageManager
            .PERMISSION_GRANTED
}
