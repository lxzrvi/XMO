package com.xmo.music.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xmo.music.R
import com.xmo.music.data.Song
import com.xmo.music.data.UserCategory
import com.xmo.music.ui.XmoFont

@Composable
internal fun HomeCategories(
    songs: List<Song>,
    categories: List<UserCategory>,
    covers: Map<String, String?>,
    c: HomeColors,
    back: () -> Unit,
    create: () -> Unit,
    open: (UserCategory) -> Unit,
    options: (UserCategory) -> Unit
) {
    BackHandler(
        onBack = back
    )

    val songsById =
        remember(songs) {
            songs.associateBy {
                it.id
            }
        }

    Column(
        Modifier.fillMaxSize()
    ) {
        SectionTitle(
            title = "Categories",
            subtitle =
                "${categories.size} categories",
            icon =
                R.drawable.ic_xmo_all,
            c = c
        )

        Box(
            Modifier
                .fillMaxWidth()
                .padding(
                    end = 12.dp,
                    bottom = 6.dp
                ),
            contentAlignment =
                Alignment.CenterEnd
        ) {
            HomeCircleAdd(
                click = create
            )
        }

        if (categories.isEmpty()) {
            HomeEmpty(
                "Create your first category",
                c
            )
            return
        }

        LazyVerticalGrid(
            columns =
                GridCells.Fixed(3),
            contentPadding =
                PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    bottom = 190.dp
                ),
            horizontalArrangement =
                Arrangement.spacedBy(8.dp),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = categories,
                key = {
                    it.id
                },
                contentType = {
                    "category"
                }
            ) { category ->
                val categorySongs =
                    category.songIds
                        .mapNotNull(
                            songsById::get
                        )

                Column(
                    Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            interactionSource =
                                remember {
                                    MutableInteractionSource()
                                },
                            indication = null,
                            onClick = {
                                open(category)
                            },
                            onLongClick = {
                                options(category)
                            }
                        )
                ) {
                    HomeCategoryCover(
                        songs =
                            categorySongs,
                        cover =
                            covers[category.id],
                        c = c
                    )

                    Text(
                        text =
                            category.name,
                        color = c.text,
                        fontFamily =
                            XmoFont.medium,
                        fontSize = 11.sp,
                        maxLines = 1,
                        modifier =
                            Modifier.padding(
                                top = 6.dp,
                                start = 3.dp,
                                end = 3.dp
                            )
                    )
                }
            }
        }
    }
}
