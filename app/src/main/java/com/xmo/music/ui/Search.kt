package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.xmo.music.R
import com.xmo.music.XmoTheme
import com.xmo.music.data.Album
import com.xmo.music.data.Artist
import com.xmo.music.data.Library
import com.xmo.music.data.Song
import com.xmo.music.data.Store
import com.xmo.music.data.UserCategory
import kotlinx.coroutines.launch

private enum class SearchFilter(
    val title: String
) {
    All("All"),
    Songs("Songs"),
    Artists("Artists"),
    Categories("Categories"),
    Albums("Albums")
}

@Composable
fun Search(
    songs: List<Song>,
    categories: List<UserCategory>,
    theme: XmoTheme,
    setTheme: (XmoTheme) -> Unit,
    onPlaySong: (
        song: Song,
        source: String,
        isCategory: Boolean,
        queue: List<Song>
    ) -> Unit
) {
    val context =
        LocalContext.current

    val scope =
        rememberCoroutineScope()

    val c =
        homeColors(theme)

    var query by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf(
            SearchFilter.All
        )
    }

    var history by remember {
        mutableStateOf<List<String>>(
            emptyList()
        )
    }

    var historyExpanded by remember {
        mutableStateOf(false)
    }

    /*
     * Metadata grouping only rebuilds when actual library changes.
     */
    val artists =
        remember(songs) {
            Library.artists(
                songs
            )
        }

    val albums =
        remember(songs) {
            Library.albums(
                songs
            )
        }

    /*
     * Load persisted history.
     */
    LaunchedEffect(Unit) {
        history =
            Store.searchHistory(
                context
            )
    }

    val cleanQuery =
        query.trim()

    /*
     * Songs match against:
     *
     * title
     * artist
     * album
     */
    val songResults =
        remember(
            songs,
            cleanQuery
        ) {
            if (
                cleanQuery.isBlank()
            ) {
                emptyList()
            } else {
                songs.filter { song ->

                    song.title.contains(
                        cleanQuery,
                        ignoreCase = true
                    ) ||
                        song.artist.contains(
                            cleanQuery,
                            ignoreCase = true
                        ) ||
                        song.album.contains(
                            cleanQuery,
                            ignoreCase = true
                        )
                }
            }
        }

    val artistResults =
        remember(
            artists,
            cleanQuery
        ) {
            if (
                cleanQuery.isBlank()
            ) {
                emptyList()
            } else {
                artists.filter {
                    it.name.contains(
                        cleanQuery,
                        ignoreCase = true
                    )
                }
            }
        }

    val albumResults =
        remember(
            albums,
            cleanQuery
        ) {
            if (
                cleanQuery.isBlank()
            ) {
                emptyList()
            } else {
                albums.filter {
                    it.name.contains(
                        cleanQuery,
                        ignoreCase = true
                    ) ||
                        it.artist.contains(
                            cleanQuery,
                            ignoreCase = true
                        )
                }
            }
        }

    val categoryResults =
        remember(
            categories,
            cleanQuery
        ) {
            if (
                cleanQuery.isBlank()
            ) {
                emptyList()
            } else {
                categories.filter {
                    it.name.contains(
                        cleanQuery,
                        ignoreCase = true
                    )
                }
            }
        }

    fun saveCurrentSearch() {
        if (
            cleanQuery.isBlank()
        ) {
            return
        }

        scope.launch {
            history =
                Store.addSearch(
                    context,
                    cleanQuery
                )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
    ) {
        LazyColumn(
            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom = 190.dp
                )
        ) {
            /*
             * =================================================
             * STICKY SEARCH HEADER
             * =================================================
             */
            stickyHeader(
                key = "search_header"
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            c.bg.copy(
                                alpha = .98f
                            )
                        )
                        .windowInsetsPadding(
                            WindowInsets.statusBars
                        )
                        .padding(
                            start = 14.dp,
                            top = 10.dp,
                            end = 14.dp,
                            bottom = 10.dp
                        )
                ) {
                    SearchInput(
                        query = query,
                        c = c,

                        onQueryChange = {
                            query = it
                        },

                        clear = {
                            query = ""
                        }
                    )

                    Spacer(
                        Modifier.height(
                            10.dp
                        )
                    )

                    /*
                     * Filters + themes.
                     */
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {
                        items(
                            items =
                                SearchFilter.entries,

                            key = {
                                "filter_${it.name}"
                            }
                        ) { filter ->

                            SearchChoiceChip(
                                text =
                                    filter.title,

                                active =
                                    selectedFilter ==
                                        filter,

                                c = c
                            ) {
                                selectedFilter =
                                    filter
                            }
                        }

                        item(
                            key =
                                "filter_divider"
                        ) {
                            Box(
                                Modifier
                                    .padding(
                                        horizontal =
                                            2.dp,
                                        vertical =
                                            7.dp
                                    )
                                    .width(
                                        1.dp
                                    )
                                    .height(
                                        18.dp
                                    )
                                    .background(
                                        c.border
                                    )
                            )
                        }

                        item(
                            key =
                                "theme_dark"
                        ) {
                            SearchChoiceChip(
                                text = "Dark",
                                active =
                                    theme ==
                                        XmoTheme.Dark,
                                c = c
                            ) {
                                setTheme(
                                    XmoTheme.Dark
                                )
                            }
                        }

                        item(
                            key =
                                "theme_light"
                        ) {
                            SearchChoiceChip(
                                text = "Light",
                                active =
                                    theme ==
                                        XmoTheme.Light,
                                c = c
                            ) {
                                setTheme(
                                    XmoTheme.Light
                                )
                            }
                        }

                        item(
                            key =
                                "theme_amoled"
                        ) {
                            SearchChoiceChip(
                                text = "AMOLED",
                                active =
                                    theme ==
                                        XmoTheme.Amoled,
                                c = c
                            ) {
                                setTheme(
                                    XmoTheme.Amoled
                                )
                            }
                        }
                    }
                }
            }

            /*
             * =================================================
             * DEFAULT VIEW
             * =================================================
             */
            if (
                cleanQuery.isBlank()
            ) {
                item(
                    key =
                        "recent_searches"
                ) {
                    RecentSearchSection(
                        history =
                            history,

                        c = c,

                        select = {
                            query = it
                        },

                        remove = {
                                value ->

                            scope.launch {
                                history =
                                    Store.removeSearch(
                                        context,
                                        value
                                    )
                            }
                        },

                        clearAll = {
                            scope.launch {
                                Store.clearSearchHistory(
                                    context
                                )

                                history =
                                    emptyList()
                            }
                        },

                        expand = {
                            historyExpanded =
                                true
                        }
                    )
                }

                item(
                    key =
                        "browse_categories"
                ) {
                    BrowseCategorySection(
                        categories =
                            categories,

                        songs =
                            songs,

                        c = c,

                        play =
                            onPlaySong
                    )
                }
            } else {
                /*
                 * =================================================
                 * SEARCH RESULTS
                 * =================================================
                 */
                item(
                    key =
                        "results_title"
                ) {
                    Text(
                        "Search Results",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            16.sp,

                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                top = 18.dp,
                                end = 16.dp,
                                bottom = 8.dp
                            )
                    )
                }

                /*
                 * SONGS
                 */
                if (
                    selectedFilter ==
                        SearchFilter.All ||
                    selectedFilter ==
                        SearchFilter.Songs
                ) {
                    if (
                        selectedFilter ==
                        SearchFilter.All &&
                        songResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "songs_label"
                        ) {
                            ResultLabel(
                                "Songs",
                                c
                            )
                        }
                    }

                    items(
                        items =
                            songResults,

                        key = {
                            "search_song_${it.id}"
                        }
                    ) { song ->

                        SearchSongRow(
                            song =
                                song,

                            c = c
                        ) {
                            saveCurrentSearch()

                            /*
                             * Current filtered song list becomes
                             * actual playback queue.
                             */
                            onPlaySong(
                                song,
                                "Search",
                                false,
                                songResults
                            )
                        }
                    }
                }

                /*
                 * ARTISTS
                 */
                if (
                    selectedFilter ==
                        SearchFilter.All ||
                    selectedFilter ==
                        SearchFilter.Artists
                ) {
                    if (
                        selectedFilter ==
                        SearchFilter.All &&
                        artistResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "artists_label"
                        ) {
                            ResultLabel(
                                "Artists",
                                c
                            )
                        }
                    }

                    items(
                        items =
                            artistResults,

                        key = {
                            "search_artist_${it.name}"
                        }
                    ) { artist ->

                        SearchArtistRow(
                            artist =
                                artist,

                            c = c
                        )
                    }
                }

                /*
                 * ALBUMS
                 */
                if (
                    selectedFilter ==
                        SearchFilter.All ||
                    selectedFilter ==
                        SearchFilter.Albums
                ) {
                    if (
                        selectedFilter ==
                        SearchFilter.All &&
                        albumResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "albums_label"
                        ) {
                            ResultLabel(
                                "Albums",
                                c
                            )
                        }
                    }

                    items(
                        items =
                            albumResults,

                        key = {
                            "search_album_${it.id}"
                        }
                    ) { album ->

                        SearchAlbumRow(
                            album =
                                album,

                            c = c
                        )
                    }
                }

                /*
                 * CUSTOM CATEGORIES
                 */
                if (
                    selectedFilter ==
                        SearchFilter.All ||
                    selectedFilter ==
                        SearchFilter.Categories
                ) {
                    if (
                        selectedFilter ==
                        SearchFilter.All &&
                        categoryResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "categories_label"
                        ) {
                            ResultLabel(
                                "Categories",
                                c
                            )
                        }
                    }

                    items(
                        items =
                            categoryResults,

                        key = {
                            "search_category_${it.id}"
                        }
                    ) { category ->

                        SearchCategoryRow(
                            category =
                                category,

                            songs =
                                songs,

                            c = c,

                            saveSearch = {
                                saveCurrentSearch()
                            },

                            play =
                                onPlaySong
                        )
                    }
                }

                /*
                 * Empty-state depends on selected filter.
                 */
                val empty =
                    when (
                        selectedFilter
                    ) {
                        SearchFilter.All ->
                            songResults.isEmpty() &&
                                artistResults.isEmpty() &&
                                albumResults.isEmpty() &&
                                categoryResults.isEmpty()

                        SearchFilter.Songs ->
                            songResults.isEmpty()

                        SearchFilter.Artists ->
                            artistResults.isEmpty()

                        SearchFilter.Categories ->
                            categoryResults.isEmpty()

                        SearchFilter.Albums ->
                            albumResults.isEmpty()
                    }

                if (empty) {
                    item(
                        key =
                            "no_results"
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(
                                    180.dp
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {
                            Text(
                                "No local results",

                                color =
                                    c.sub,

                                fontFamily =
                                    XmoFont.normal,

                                fontSize =
                                    13.sp
                            )
                        }
                    }
                }
            }

            /*
             * =================================================
             * FOOTER
             * =================================================
             */
            item(
                key =
                    "search_footer"
            ) {
                SearchFooter(
                    c
                )
            }
        }

        /*
         * =====================================================
         * FULLSCREEN RECENT HISTORY
         * =====================================================
         */
        if (
            historyExpanded
        ) {
            FullRecentSearches(
                history =
                    history,

                c = c,

                close = {
                    historyExpanded =
                        false
                },

                select = {
                    query = it

                    historyExpanded =
                        false
                },

                remove = {
                        value ->

                    scope.launch {
                        history =
                            Store.removeSearch(
                                context,
                                value
                            )
                    }
                }
            )
        }
    }
}

/*
 * =============================================================
 * SEARCH INPUT
 * =============================================================
 */

@Composable
private fun SearchInput(
    query: String,
    c: HomeColors,
    onQueryChange: (String) -> Unit,
    clear: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(
                44.dp
            )
            .clip(
                RoundedCornerShape(
                    22.dp
                )
            )
            .background(
                c.button
            )
            .border(
                width =
                    1.dp,

                color =
                    if (
                        query.isNotEmpty()
                    ) {
                        XmoRed.copy(
                            alpha =
                                .60f
                        )
                    } else {
                        c.border
                    },

                shape =
                    RoundedCornerShape(
                        22.dp
                    )
            )
            .padding(
                horizontal =
                    12.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            imageVector =
                Icons.Default.Search,

            contentDescription =
                null,

            tint =
                c.sub,

            modifier =
                Modifier.size(
                    18.dp
                )
        )

        BasicTextField(
            value =
                query,

            onValueChange =
                onQueryChange,

            singleLine =
                true,

            textStyle =
                LocalTextStyle.current.copy(
                    color =
                        c.text,

                    fontFamily =
                        XmoFont.normal,

                    fontSize =
                        14.sp
                ),

            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        horizontal =
                            10.dp
                    ),

            decorationBox = {
                    innerTextField ->

                Box(
                    contentAlignment =
                        Alignment.CenterStart
                ) {
                    if (
                        query.isEmpty()
                    ) {
                        Text(
                            "Search songs, artists, albums...",

                            color =
                                c.sub,

                            fontFamily =
                                XmoFont.thin,

                            fontSize =
                                13.sp,

                            maxLines =
                                1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    innerTextField()
                }
            }
        )

        if (
            query.isNotEmpty()
        ) {
            IconButton(
                onClick =
                    clear,

                modifier =
                    Modifier.size(
                        30.dp
                    )
            ) {
                Icon(
                    imageVector =
                        Icons.Default.Close,

                    contentDescription =
                        "Clear search",

                    tint =
                        c.sub,

                    modifier =
                        Modifier.size(
                            16.dp
                        )
                )
            }
        }
    }
}

/*
 * =============================================================
 * CHIPS
 * =============================================================
 */

@Composable
private fun SearchChoiceChip(
    text: String,
    active: Boolean,
    c: HomeColors,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(
                RoundedCornerShape(
                    16.dp
                )
            )
            .background(
                if (active)
                    XmoRed
                else
                    c.button
            )
            .border(
                .7.dp,

                if (active)
                    XmoRed
                else
                    c.border,

                RoundedCornerShape(
                    16.dp
                )
            )
            .clickable(
                onClick =
                    onClick
            )
            .padding(
                horizontal =
                    14.dp,

                vertical =
                    7.dp
            )
    ) {
        Text(
            text =
                text,

            color =
                if (active)
                    Color.White
                else
                    c.sub,

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
        )
    }
}

/*
 * =============================================================
 * RECENT SEARCHES
 * =============================================================
 */

@Composable
private fun RecentSearchSection(
    history: List<String>,
    c: HomeColors,
    select: (String) -> Unit,
    remove: (String) -> Unit,
    clearAll: () -> Unit,
    expand: () -> Unit
) {
    Column(
        Modifier.padding(
            start = 14.dp,
            top = 20.dp,
            end = 14.dp
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(
                    start = 4.dp,
                    end = 4.dp,
                    bottom = 8.dp
                ),

            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                "Recent Searches",

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    15.sp,

                modifier =
                    Modifier.weight(
                        1f
                    )
            )

            if (
                history.isNotEmpty()
            ) {
                Text(
                    "Clear All",

                    color =
                        XmoRed,

                    fontFamily =
                        XmoFont.medium,

                    fontSize =
                        11.sp,

                    modifier =
                        Modifier
                            .clickable(
                                onClick =
                                    clearAll
                            )
                            .padding(
                                6.dp
                            )
                )

                Spacer(
                    Modifier.width(
                        4.dp
                    )
                )

                Box(
                    Modifier
                        .size(
                            28.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            c.button
                        )
                        .clickable(
                            onClick =
                                expand
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    /*
                     * Lightweight fullscreen-ish symbol.
                     */
                    Text(
                        "↗",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            14.sp
                    )
                }
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .background(
                    c.surface
                )
                .border(
                    .7.dp,
                    c.border,
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .padding(
                    10.dp
                ),

            verticalArrangement =
                Arrangement.spacedBy(
                    7.dp
                )
        ) {
            if (
                history.isEmpty()
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(
                            68.dp
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "No recent searches",

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.thin,

                        fontSize =
                            12.sp
                    )
                }
            } else {
                history
                    .take(5)
                    .forEach {
                        RecentSearchRow(
                            value =
                                it,

                            c = c,

                            select =
                                select,

                            remove =
                                remove
                        )
                    }
            }
        }
    }
}

@Composable
private fun RecentSearchRow(
    value: String,
    c: HomeColors,
    select: (String) -> Unit,
    remove: (String) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(
                c.button
            )
            .clickable {
                select(
                    value
                )
            }
            .padding(
                horizontal =
                    12.dp,

                vertical =
                    10.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoIcon(
            icon =
                R.drawable.ic_xmo_history,

            tint =
                c.sub,

            modifier =
                Modifier.size(
                    14.dp
                )
        )

        Text(
            text =
                value,

            color =
                c.text,

            fontFamily =
                XmoFont.normal,

            fontSize =
                12.sp,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier
                    .weight(
                        1f
                    )
                    .padding(
                        start =
                            10.dp
                    )
        )

        /*
         * Remove button.
         */
        Box(
            Modifier
                .size(
                    28.dp
                )
                .clip(
                    CircleShape
                )
                .clickable {
                    remove(
                        value
                    )
                },

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                "×",

                color =
                    c.sub,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    18.sp
            )
        }
    }
}

/*
 * =============================================================
 * BROWSE CUSTOM CATEGORIES
 * =============================================================
 */

@Composable
private fun BrowseCategorySection(
    categories: List<UserCategory>,
    songs: List<Song>,
    c: HomeColors,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit
) {
    if (
        categories.isEmpty()
    ) {
        return
    }

    Column(
        Modifier.padding(
            start = 14.dp,
            top = 24.dp,
            end = 14.dp,
            bottom = 4.dp
        )
    ) {
        Text(
            "Browse Categories",

            color =
                c.text,

            fontFamily =
                XmoFont.bold,

            fontSize =
                15.sp,

            modifier =
                Modifier.padding(
                    bottom =
                        10.dp
                )
        )

        categories
            .chunked(2)
            .forEach {
                    rowCategories ->

                Row(
                    Modifier
                        .fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement
                            .spacedBy(
                                10.dp
                            )
                ) {
                    repeat(2) {
                            column ->

                        val category =
                            rowCategories
                                .getOrNull(
                                    column
                                )

                        Box(
                            Modifier.weight(
                                1f
                            )
                        ) {
                            if (
                                category !=
                                null
                            ) {
                                val queue =
                                    remember(
                                        category,
                                        songs
                                    ) {
                                        songs.filter {
                                            it.id in
                                                category.songIds
                                        }
                                    }

                                BrowseCategoryCard(
                                    category =
                                        category,

                                    songs =
                                        queue,

                                    c = c,

                                    play =
                                        play
                                )
                            }
                        }
                    }
                }

                Spacer(
                    Modifier.height(
                        10.dp
                    )
                )
            }
    }
}

@Composable
private fun BrowseCategoryCard(
    category: UserCategory,
    songs: List<Song>,
    c: HomeColors,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    14.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .7.dp,
                c.border,
                RoundedCornerShape(
                    14.dp
                )
            )
            .clickable(
                enabled =
                    songs.isNotEmpty()
            ) {
                play(
                    songs.first(),
                    category.name,
                    true,
                    songs
                )
            }
            .padding(
                9.dp
            )
    ) {
        /*
         * Real four-cover mosaic.
         */
        Column(
            Modifier
                .fillMaxWidth()
                .aspectRatio(
                    1f
                )
                .clip(
                    RoundedCornerShape(
                        10.dp
                    )
                )
                .background(
                    c.button
                )
        ) {
            repeat(2) {
                    row ->

                Row(
                    Modifier
                        .weight(
                            1f
                        )
                ) {
                    repeat(2) {
                            column ->

                        val song =
                            songs.getOrNull(
                                row * 2 +
                                    column
                            )

                        Box(
                            Modifier
                                .weight(
                                    1f
                                )
                                .fillMaxHeight()
                                .background(
                                    c.button
                                )
                        ) {
                            if (
                                song != null
                            ) {
                                AsyncImage(
                                    model =
                                        song.artwork,

                                    contentDescription =
                                        null,

                                    modifier =
                                        Modifier
                                            .fillMaxSize(),

                                    contentScale =
                                        ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            category.name,

            color =
                c.text,

            fontFamily =
                XmoFont.bold,

            fontSize =
                12.sp,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.padding(
                    top = 7.dp
                )
        )

        Text(
            "${songs.size} tracks",

            color =
                c.sub,

            fontFamily =
                XmoFont.thin,

            fontSize =
                9.sp
        )
    }
}

/*
 * =============================================================
 * RESULT LABEL
 * =============================================================
 */

@Composable
private fun ResultLabel(
    text: String,
    c: HomeColors
) {
    Text(
        text = text,

        color =
            XmoRed,

        fontFamily =
            XmoFont.bold,

        fontSize =
            10.sp,

        letterSpacing =
            1.sp,

        modifier =
            Modifier.padding(
                start = 16.dp,
                top = 12.dp,
                end = 16.dp,
                bottom = 3.dp
            )
    )
}

/*
 * =============================================================
 * SONG RESULT
 * =============================================================
 */

@Composable
private fun SearchSongRow(
    song: Song,
    c: HomeColors,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .padding(
                horizontal =
                    14.dp,

                vertical =
                    4.dp
            )
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    12.dp
                )
            )
            .clickable(
                onClick =
                    onClick
            )
            .padding(
                7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model =
                song.artwork,

            contentDescription =
                null,

            modifier =
                Modifier
                    .size(
                        46.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            8.dp
                        )
                    )
                    .background(
                        c.button
                    ),

            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    horizontal =
                        11.dp
                )
        ) {
            Text(
                song.title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    12.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                "${song.artist} • ${song.album}",

                color =
                    c.sub,

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

        /*
         * Lightweight play circle without extended icons.
         */
        Box(
            Modifier
                .size(
                    32.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    XmoRed
                ),

            contentAlignment =
                Alignment.Center
        ) {
            Text(
                "▶",

                color =
                    Color.White,

                fontSize =
                    10.sp
            )
        }
    }
}

/*
 * =============================================================
 * ARTIST RESULT
 * =============================================================
 */

@Composable
private fun SearchArtistRow(
    artist: Artist,
    c: HomeColors
) {
    SearchMetadataRow(
        title =
            artist.name,

        subtitle =
            "${artist.songs.size} songs",

        artwork =
            artist.songs
                .firstOrNull()
                ?.artwork,

        c = c
    )
}

/*
 * =============================================================
 * ALBUM RESULT
 * =============================================================
 */

@Composable
private fun SearchAlbumRow(
    album: Album,
    c: HomeColors
) {
    SearchMetadataRow(
        title =
            album.name,

        subtitle =
            "${album.artist} • ${album.songs.size} songs",

        artwork =
            album.artwork,

        c = c
    )
}

@Composable
private fun SearchMetadataRow(
    title: String,
    subtitle: String,
    artwork: Uri?,
    c: HomeColors
) {
    Row(
        Modifier
            .padding(
                horizontal =
                    14.dp,

                vertical =
                    4.dp
            )
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    12.dp
                )
            )
            .padding(
                7.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model =
                artwork,

            contentDescription =
                null,

            modifier =
                Modifier
                    .size(
                        46.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            8.dp
                        )
                    )
                    .background(
                        c.button
                    ),

            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(
                    1f
                )
                .padding(
                    horizontal =
                        11.dp
                )
        ) {
            Text(
                title,

                color =
                    c.text,

                fontFamily =
                    XmoFont.bold,

                fontSize =
                    12.sp,

                maxLines =
                    1,

                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                subtitle,

                color =
                    c.sub,

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
    }
}

/*
 * =============================================================
 * CATEGORY SEARCH RESULT
 * =============================================================
 */

@Composable
private fun SearchCategoryRow(
    category: UserCategory,
    songs: List<Song>,
    c: HomeColors,
    saveSearch: () -> Unit,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit
) {
    val queue =
        remember(
            category,
            songs
        ) {
            songs.filter {
                it.id in
                    category.songIds
            }
        }

    Row(
        Modifier
            .padding(
                horizontal =
                    14.dp,

                vertical =
                    4.dp
            )
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    12.dp
                )
            )
            .clickable(
                enabled =
                    queue.isNotEmpty()
            ) {
                saveSearch()

                play(
                    queue.first(),
                    category.name,
                    true,
                    queue
                )
            }
            .padding(
                horizontal =
                    14.dp,

                vertical =
                    12.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            category.name,

            color =
                c.text,

            fontFamily =
                XmoFont.bold,

            fontSize =
                12.sp,

            maxLines =
                1,

            overflow =
                TextOverflow.Ellipsis,

            modifier =
                Modifier.weight(
                    1f
                )
        )

        Text(
            "${queue.size} tracks",

            color =
                c.sub,

            fontFamily =
                XmoFont.thin,

            fontSize =
                10.sp
        )
    }
}

/*
 * =============================================================
 * FULL RECENT SEARCHES
 * =============================================================
 */

@Composable
private fun FullRecentSearches(
    history: List<String>,
    c: HomeColors,
    close: () -> Unit,
    select: (String) -> Unit,
    remove: (String) -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val offset =
        remember {
            Animatable(
                0f
            )
        }

    var height by remember {
        mutableFloatStateOf(
            1f
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
            .onSizeChanged {
                height =
                    it.height
                        .toFloat()
                        .coerceAtLeast(
                            1f
                        )
            }
            .graphicsLayer {
                translationY =
                    offset.value
            }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.statusBars
                )
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        58.dp
                    )
                    .pointerInput(
                        height
                    ) {
                        detectVerticalDragGestures(
                            onVerticalDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                scope.launch {
                                    offset.snapTo(
                                        (
                                            offset.value +
                                                amount
                                            )
                                            .coerceIn(
                                                0f,
                                                height
                                            )
                                    )
                                }
                            },

                            onDragEnd = {
                                scope.launch {
                                    if (
                                        offset.value >
                                        height *
                                            .15f
                                    ) {
                                        offset.animateTo(
                                            height,

                                            tween(
                                                durationMillis =
                                                    260
                                            )
                                        )

                                        close()
                                    } else {
                                        offset.animateTo(
                                            0f,

                                            spring(
                                                dampingRatio =
                                                    .85f,

                                                stiffness =
                                                    380f
                                            )
                                        )
                                    }
                                }
                            },

                            onDragCancel = {
                                scope.launch {
                                    offset.animateTo(
                                        0f
                                    )
                                }
                            }
                        )
                    },

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Spacer(
                    Modifier.width(
                        48.dp
                    )
                )

                Box(
                    Modifier
                        .weight(
                            1f
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Box(
                        Modifier
                            .width(
                                42.dp
                            )
                            .height(
                                5.dp
                            )
                            .clip(
                                RoundedCornerShape(
                                    3.dp
                                )
                            )
                            .background(
                                c.sub.copy(
                                    alpha =
                                        .65f
                                )
                            )
                    )
                }

                IconButton(
                    onClick =
                        close,

                    modifier =
                        Modifier.size(
                            48.dp
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Default.Close,

                        contentDescription =
                            "Close",

                        tint =
                            c.text
                    )
                }
            }

            if (
                history.isEmpty()
            ) {
                Box(
                    Modifier
                        .fillMaxSize(),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "No recent searches",

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.normal,

                        fontSize =
                            13.sp
                    )
                }
            } else {
                LazyColumn(
                    Modifier
                        .fillMaxSize(),

                    contentPadding =
                        PaddingValues(
                            start =
                                14.dp,

                            end =
                                14.dp,

                            bottom =
                                40.dp
                        ),

                    verticalArrangement =
                        Arrangement
                            .spacedBy(
                                8.dp
                            )
                ) {
                    items(
                        items =
                            history,

                        key = {
                            it
                        }
                    ) {
                        RecentSearchRow(
                            value =
                                it,

                            c = c,

                            select =
                                select,

                            remove =
                                remove
                        )
                    }
                }
            }
        }
    }
}

/*
 * =============================================================
 * FOOTER
 * =============================================================
 */

@Composable
private fun SearchFooter(
    c: HomeColors
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(
                top = 70.dp,
                bottom = 35.dp
            ),

        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            "XMO",

            color =
                c.text,

            fontFamily =
                XmoFont.logo,

            fontSize =
                19.sp
        )

        Spacer(
            Modifier.height(
                3.dp
            )
        )

        Text(
            "lxzrvi • copyright © 2026",

            color =
                c.sub,

            fontFamily =
                XmoFont.thin,

            fontSize =
                9.sp
        )
    }
}
