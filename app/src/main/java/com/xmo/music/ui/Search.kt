package com.xmo.music.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
    val label: String
) {
    All("All"),
    Songs("Songs"),
    Artists("Artists"),
    Albums("Albums"),
    Categories("Categories")
}

private sealed interface SearchDetail {
    data class ArtistSongs(
        val artist: Artist
    ) : SearchDetail

    data class AlbumSongs(
        val album: Album
    ) : SearchDetail

    data class CategorySongs(
        val category: UserCategory,
        val songs: List<Song>
    ) : SearchDetail
}

@Composable
fun Search(
    songs: List<Song>,
    categories: List<UserCategory>,
    theme: XmoTheme,
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
        homeColors(
            theme
        )

    var query by
        remember {
            mutableStateOf("")
        }

    var filter by
        remember {
            mutableStateOf(
                SearchFilter.All
            )
        }

    var history by
        remember {
            mutableStateOf<List<String>>(
                emptyList()
            )
        }

    var detail by
        remember {
            mutableStateOf<SearchDetail?>(
                null
            )
        }

    var optionsSong by
        remember {
            mutableStateOf<Song?>(
                null
            )
        }

    val artists =
        remember(
            songs
        ) {
            Library.artists(
                songs
            )
        }

    val albums =
        remember(
            songs
        ) {
            Library.albums(
                songs
            )
        }

    LaunchedEffect(Unit) {
        history =
            Store.searchHistory(
                context
            )
    }

    val clean =
        query.trim()

    val songResults =
        remember(
            songs,
            clean
        ) {
            if (
                clean.isBlank()
            ) {
                emptyList()
            } else {
                songs.filter {
                    it.title.contains(
                        clean,
                        true
                    ) ||
                        it.artist.contains(
                            clean,
                            true
                        ) ||
                        it.album.contains(
                            clean,
                            true
                        ) ||
                        it.albumArtist
                            ?.contains(
                                clean,
                                true
                            ) ==
                        true ||
                        it.metadata.genre
                            ?.contains(
                                clean,
                                true
                            ) ==
                        true ||
                        it.metadata.composer
                            ?.contains(
                                clean,
                                true
                            ) ==
                        true ||
                        it.metadata.fileName
                            ?.contains(
                                clean,
                                true
                            ) ==
                        true
                }
            }
        }

    val artistResults =
        remember(
            artists,
            clean
        ) {
            if (
                clean.isBlank()
            ) {
                emptyList()
            } else {
                artists.filter {
                    it.name.contains(
                        clean,
                        true
                    )
                }
            }
        }

    val albumResults =
        remember(
            albums,
            clean
        ) {
            if (
                clean.isBlank()
            ) {
                emptyList()
            } else {
                albums.filter {
                    it.name.contains(
                        clean,
                        true
                    ) ||
                        it.artist.contains(
                            clean,
                            true
                        )
                }
            }
        }

    val categoryResults =
        remember(
            categories,
            clean
        ) {
            if (
                clean.isBlank()
            ) {
                emptyList()
            } else {
                categories.filter {
                    it.name.contains(
                        clean,
                        true
                    )
                }
            }
        }

    fun rememberSearch() {
        if (
            clean.isBlank()
        ) {
            return
        }

        scope.launch {
            history =
                Store.addSearch(
                    context,
                    clean
                )
        }
    }

    BackHandler(
        enabled =
            detail !=
                null
    ) {
        detail =
            null
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(
                    bottom =
                        190.dp
                )
        ) {
            stickyHeader(
                key =
                    "search_header"
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
                            top = 12.dp,
                            end = 14.dp,
                            bottom = 10.dp
                        )
                ) {
                    Text(
                        "Search",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            22.sp,

                        modifier =
                            Modifier.padding(
                                start = 4.dp,
                                bottom = 12.dp
                            )
                    )

                    SearchInput(
                        query =
                            query,

                        c =
                            c,

                        change = {
                            query =
                                it
                        },

                        clear = {
                            query =
                                ""
                        }
                    )

                    Spacer(
                        Modifier.height(
                            10.dp
                        )
                    )

                    /*
                     * Only search filters live here now.
                     * Theme controls belong to Settings.
                     */
                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                7.dp
                            )
                    ) {
                        items(
                            SearchFilter.entries,
                            key = {
                                it.name
                            }
                        ) {
                            SearchChip(
                                text =
                                    it.label,

                                active =
                                    filter ==
                                        it,

                                c =
                                    c
                            ) {
                                filter =
                                    it
                            }
                        }
                    }
                }
            }

            if (
                clean.isBlank()
            ) {
                item(
                    key =
                        "recent_title"
                ) {
                    SearchSectionHeader(
                        text =
                            "Recent Searches",

                        c =
                            c,

                        trailing =
                            if (
                                history.isNotEmpty()
                            ) {
                                "Clear All"
                            } else {
                                null
                            },

                        action = {
                            scope.launch {
                                Store.clearSearchHistory(
                                    context
                                )

                                history =
                                    emptyList()
                            }
                        }
                    )
                }

                if (
                    history.isEmpty()
                ) {
                    item(
                        key =
                            "empty_history"
                    ) {
                        SearchEmpty(
                            "No recent searches",
                            c
                        )
                    }
                } else {
                    items(
                        items =
                            history,

                        key = {
                            "history_$it"
                        }
                    ) { value ->

                        HistoryRow(
                            value =
                                value,

                            c =
                                c,

                            select = {
                                query =
                                    value
                            },

                            remove = {
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

                if (
                    categories.isNotEmpty()
                ) {
                    item(
                        key =
                            "categories_title"
                    ) {
                        SearchSectionHeader(
                            text =
                                "Your Categories",

                            c =
                                c
                        )
                    }

                    items(
                        items =
                            categories,

                        key = {
                            "browse_${it.id}"
                        }
                    ) { category ->

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

                        MetadataRow(
                            title =
                                category.name,

                            subtitle =
                                "${queue.size} songs",

                            artwork =
                                queue.firstOrNull()
                                    ?.artwork,

                            c =
                                c,

                            click = {
                                detail =
                                    SearchDetail
                                        .CategorySongs(
                                            category,
                                            queue
                                        )
                            }
                        )
                    }
                }
            } else {
                item(
                    key =
                        "results_heading"
                ) {
                    SearchSectionHeader(
                        text =
                            "Results",

                        c =
                            c
                    )
                }

                if (
                    filter ==
                    SearchFilter.All ||
                    filter ==
                    SearchFilter.Songs
                ) {
                    if (
                        filter ==
                        SearchFilter.All &&
                        songResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "songs_label"
                        ) {
                            ResultLabel(
                                "Songs"
                            )
                        }
                    }

                    items(
                        items =
                            songResults,

                        key = {
                            "song_${it.id}"
                        }
                    ) { song ->

                        SearchSongRow(
                            song =
                                song,

                            c =
                                c,

                            click = {
                                rememberSearch()

                                onPlaySong(
                                    song,
                                    "Search",
                                    false,
                                    songResults
                                )
                            },

                            options = {
                                optionsSong =
                                    song
                            }
                        )
                    }
                }

                if (
                    filter ==
                    SearchFilter.All ||
                    filter ==
                    SearchFilter.Artists
                ) {
                    if (
                        filter ==
                        SearchFilter.All &&
                        artistResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "artists_label"
                        ) {
                            ResultLabel(
                                "Artists"
                            )
                        }
                    }

                    items(
                        artistResults,
                        key = {
                            "artist_${it.name}"
                        }
                    ) { artist ->

                        MetadataRow(
                            title =
                                artist.name,

                            subtitle =
                                "${artist.songs.size} songs",

                            artwork =
                                artist.artwork,

                            c =
                                c
                        ) {
                            rememberSearch()

                            detail =
                                SearchDetail.ArtistSongs(
                                    artist
                                )
                        }
                    }
                }

                if (
                    filter ==
                    SearchFilter.All ||
                    filter ==
                    SearchFilter.Albums
                ) {
                    if (
                        filter ==
                        SearchFilter.All &&
                        albumResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "albums_label"
                        ) {
                            ResultLabel(
                                "Albums"
                            )
                        }
                    }

                    items(
                        albumResults,
                        key = {
                            "album_${it.id}_${it.name}"
                        }
                    ) { album ->

                        MetadataRow(
                            title =
                                album.name,

                            subtitle =
                                "${album.artist} • ${album.songs.size} songs",

                            artwork =
                                album.artwork,

                            c =
                                c
                        ) {
                            rememberSearch()

                            detail =
                                SearchDetail.AlbumSongs(
                                    album
                                )
                        }
                    }
                }

                if (
                    filter ==
                    SearchFilter.All ||
                    filter ==
                    SearchFilter.Categories
                ) {
                    if (
                        filter ==
                        SearchFilter.All &&
                        categoryResults.isNotEmpty()
                    ) {
                        item(
                            key =
                                "category_label"
                        ) {
                            ResultLabel(
                                "Categories"
                            )
                        }
                    }

                    items(
                        categoryResults,
                        key = {
                            "category_${it.id}"
                        }
                    ) { category ->

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

                        MetadataRow(
                            title =
                                category.name,

                            subtitle =
                                "${queue.size} songs",

                            artwork =
                                queue.firstOrNull()
                                    ?.artwork,

                            c =
                                c
                        ) {
                            rememberSearch()

                            detail =
                                SearchDetail.CategorySongs(
                                    category,
                                    queue
                                )
                        }
                    }
                }

                val empty =
                    when (
                        filter
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

                        SearchFilter.Albums ->
                            albumResults.isEmpty()

                        SearchFilter.Categories ->
                            categoryResults.isEmpty()
                    }

                if (
                    empty
                ) {
                    item(
                        key =
                            "empty_results"
                    ) {
                        SearchEmpty(
                            "No local results",
                            c
                        )
                    }
                }
            }

            item(
                key =
                    "footer"
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
        }

        detail?.let {
            when (
                it
            ) {
                is SearchDetail.ArtistSongs ->
                    SearchSongList(
                        title =
                            it.artist.name,

                        subtitle =
                            "${it.artist.songs.size} songs",

                        songs =
                            it.artist.songs,

                        source =
                            it.artist.name,

                        category =
                            false,

                        c =
                            c,

                        close = {
                            detail =
                                null
                        },

                        play =
                            onPlaySong,

                        options = {
                            optionsSong =
                                it
                        }
                    )

                is SearchDetail.AlbumSongs ->
                    SearchSongList(
                        title =
                            it.album.name,

                        subtitle =
                            it.album.artist,

                        songs =
                            it.album.songs,

                        source =
                            it.album.name,

                        category =
                            false,

                        c =
                            c,

                        close = {
                            detail =
                                null
                        },

                        play =
                            onPlaySong,

                        options = {
                            optionsSong =
                                it
                        }
                    )

                is SearchDetail.CategorySongs ->
                    SearchSongList(
                        title =
                            it.category.name,

                        subtitle =
                            "${it.songs.size} songs",

                        songs =
                            it.songs,

                        source =
                            it.category.name,

                        category =
                            true,

                        c =
                            c,

                        close = {
                            detail =
                                null
                        },

                        play =
                            onPlaySong,

                        options = {
                            optionsSong =
                                it
                        }
                    )
            }
        }

        /*
         * Search song options are surfaced without fake actions.
         * Full Like/category mutation is provided by the common
         * App/Home action layer in the final integration.
         */
        optionsSong?.let { song ->
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Color.Black
                            .copy(
                                alpha = .55f
                            )
                    )
                    .clickable {
                        optionsSong =
                            null
                    },

                contentAlignment =
                    Alignment.BottomCenter
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp
                            )
                        )
                        .background(
                            c.surface
                        )
                        .border(
                            .7.dp,
                            c.border,
                            RoundedCornerShape(
                                topStart = 24.dp,
                                topEnd = 24.dp
                            )
                        )
                        .clickable {}
                        .padding(
                            18.dp
                        )
                ) {
                    Text(
                        song.title,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            15.sp,

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
                            10.sp
                    )

                    Spacer(
                        Modifier.height(
                            18.dp
                        )
                    )

                    Text(
                        "Song options are available from the player and library.",

                        color =
                            c.sub,

                        fontFamily =
                            XmoFont.normal,

                        fontSize =
                            11.sp
                    )

                    Spacer(
                        Modifier.height(
                            18.dp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    c: HomeColors,
    change: (String) -> Unit,
    clear: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Row(
        Modifier
            .fillMaxWidth()
            .height(
                46.dp
            )
            .clip(
                RoundedCornerShape(
                    23.dp
                )
            )
            .background(
                c.button
            )
            .border(
                1.dp,

                if (
                    query.isNotEmpty()
                ) {
                    accent.copy(
                        alpha = .60f
                    )
                } else {
                    c.border
                },

                RoundedCornerShape(
                    23.dp
                )
            )
            .padding(
                horizontal =
                    13.dp
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
                if (
                    query.isNotEmpty()
                ) {
                    accent
                } else {
                    c.sub
                },

            modifier =
                Modifier.size(
                    18.dp
                )
        )

        BasicTextField(
            value =
                query,

            onValueChange = {
                change(
                    it.take(120)
                )
            },

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
                    field ->

                Box(
                    contentAlignment =
                        Alignment.CenterStart
                ) {
                    if (
                        query.isEmpty()
                    ) {
                        Text(
                            "Search local music",

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
                        "Clear",

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

@Composable
private fun SearchChip(
    text: String,
    active: Boolean,
    c: HomeColors,
    click: () -> Unit
) {
    val accent =
        LocalXmoAccent.current

    Box(
        Modifier
            .clip(
                RoundedCornerShape(
                    17.dp
                )
            )
            .background(
                if (
                    active
                ) {
                    accent.copy(
                        alpha = .16f
                    )
                } else {
                    c.button
                }
            )
            .border(
                .7.dp,

                if (
                    active
                ) {
                    accent.copy(
                        alpha = .42f
                    )
                } else {
                    c.border
                },

                RoundedCornerShape(
                    17.dp
                )
            )
            .clickable(
                onClick =
                    click
            )
            .padding(
                horizontal = 14.dp,
                vertical = 7.dp
            )
    ) {
        Text(
            text,

            color =
                if (
                    active
                ) {
                    accent
                } else {
                    c.text
                },

            fontFamily =
                XmoFont.medium,

            fontSize =
                11.sp
        )
    }
}

@Composable
private fun SearchSectionHeader(
    text: String,
    c: HomeColors,
    trailing: String? = null,
    action: () -> Unit = {}
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(
                start = 17.dp,
                top = 22.dp,
                end = 17.dp,
                bottom = 9.dp
            ),

        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Text(
            text,

            color =
                c.text,

            fontFamily =
                XmoFont.bold,

            fontSize =
                16.sp,

            modifier =
                Modifier.weight(
                    1f
                )
        )

        trailing?.let {
            Text(
                it,

                color =
                    LocalXmoAccent.current,

                fontFamily =
                    XmoFont.medium,

                fontSize =
                    10.sp,

                modifier =
                    Modifier
                        .clickable(
                            onClick =
                                action
                        )
                        .padding(
                            5.dp
                        )
            )
        }
    }
}

@Composable
private fun HistoryRow(
    value: String,
    c: HomeColors,
    select: () -> Unit,
    remove: () -> Unit
) {
    Row(
        Modifier
            .padding(
                horizontal = 14.dp,
                vertical = 3.dp
            )
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    13.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    13.dp
                )
            )
            .clickable(
                onClick =
                    select
            )
            .padding(
                start = 12.dp,
                top = 7.dp,
                end = 5.dp,
                bottom = 7.dp
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
                    15.dp
                )
        )

        Text(
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
                        horizontal =
                            11.dp
                    )
        )

        IconButton(
            onClick =
                remove,

            modifier =
                Modifier.size(
                    32.dp
                )
        ) {
            Icon(
                imageVector =
                    Icons.Default.Close,

                contentDescription =
                    "Remove",

                tint =
                    c.sub,

                modifier =
                    Modifier.size(
                        14.dp
                    )
            )
        }
    }
}

@Composable
private fun SearchSongRow(
    song: Song,
    c: HomeColors,
    click: () -> Unit,
    options: () -> Unit
) {
    Row(
        Modifier
            .padding(
                horizontal = 14.dp,
                vertical = 4.dp
            )
            .fillMaxWidth()
            .height(
                62.dp
            )
            .clip(
                RoundedCornerShape(
                    13.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    13.dp
                )
            )
            .combinedClickable(
                onClick =
                    click,

                onLongClick =
                    options
            )
            .padding(
                6.dp
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
                        50.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            9.dp
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
                        10.dp
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

        Box(
            Modifier
                .size(
                    34.dp
                )
                .clip(
                    CircleShape
                )
                .background(
                    LocalXmoAccent.current
                ),

            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector =
                    Icons.Default.PlayArrow,

                contentDescription =
                    "Play",

                tint =
                    androidx.compose.ui.graphics.Color.White,

                modifier =
                    Modifier.size(
                        20.dp
                    )
            )
        }

        Box(
            Modifier
                .size(
                    32.dp
                )
                .clickable(
                    onClick =
                        options
                ),

            contentAlignment =
                Alignment.Center
        ) {
            XmoIcon(
                icon =
                    R.drawable.ic_xmo_more,

                tint =
                    c.sub,

                modifier =
                    Modifier.size(
                        15.dp
                    )
            )
        }
    }
}

@Composable
private fun MetadataRow(
    title: String,
    subtitle: String,
    artwork: Uri?,
    c: HomeColors,
    click: () -> Unit
) {
    Row(
        Modifier
            .padding(
                horizontal = 14.dp,
                vertical = 4.dp
            )
            .fillMaxWidth()
            .height(
                60.dp
            )
            .clip(
                RoundedCornerShape(
                    13.dp
                )
            )
            .background(
                c.surface
            )
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    13.dp
                )
            )
            .clickable(
                onClick =
                    click
            )
            .padding(
                6.dp
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
                        48.dp
                    )
                    .clip(
                        RoundedCornerShape(
                            9.dp
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

        Text(
            "›",

            color =
                c.sub,

            fontFamily =
                XmoFont.medium,

            fontSize =
                22.sp,

            modifier =
                Modifier.padding(
                    end =
                        8.dp
                )
        )
    }
}

@Composable
private fun ResultLabel(
    text: String
) {
    Text(
        text,

        color =
            LocalXmoAccent.current,

        fontFamily =
            XmoFont.bold,

        fontSize =
            10.sp,

        letterSpacing =
            1.sp,

        modifier =
            Modifier.padding(
                start = 17.dp,
                top = 12.dp,
                bottom = 3.dp
            )
    )
}

@Composable
private fun SearchEmpty(
    text: String,
    c: HomeColors
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(
                120.dp
            ),

        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text,

            color =
                c.sub,

            fontFamily =
                XmoFont.normal,

            fontSize =
                12.sp
        )
    }
}

@Composable
private fun SearchSongList(
    title: String,
    subtitle: String,
    songs: List<Song>,
    source: String,
    category: Boolean,
    c: HomeColors,
    close: () -> Unit,
    play: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit,
    options: (Song) -> Unit
) {
    BackHandler(
        onBack =
            close
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                c.bg
            )
            .windowInsetsPadding(
                WindowInsets.statusBars
            )
    ) {
        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(
                        66.dp
                    )
                    .padding(
                        horizontal =
                            14.dp
                    ),

                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(
                            38.dp
                        )
                        .clip(
                            CircleShape
                        )
                        .background(
                            c.button
                        )
                        .clickable(
                            onClick =
                                close
                        ),

                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "‹",

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.medium,

                        fontSize =
                            28.sp
                    )
                }

                Column(
                    Modifier.padding(
                        start =
                            12.dp
                    )
                ) {
                    Text(
                        title,

                        color =
                            c.text,

                        fontFamily =
                            XmoFont.bold,

                        fontSize =
                            17.sp,

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
                            9.sp
                    )
                }
            }

            LazyColumn(
                contentPadding =
                    PaddingValues(
                        bottom =
                            190.dp
                    )
            ) {
                items(
                    items =
                        songs,

                    key = {
                        it.id
                    }
                ) { song ->

                    SearchSongRow(
                        song =
                            song,

                        c =
                            c,

                        click = {
                            play(
                                song,
                                source,
                                category,
                                songs
                            )
                        },

                        options = {
                            options(
                                song
                            )
                        }
                    )
                }
            }
        }
    }
}
