package com.xmo.music.ui

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
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
        Song,
        String,
        Boolean,
        List<Song>
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

    var filter by remember {
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

    LaunchedEffect(Unit) {
        history =
            Store.searchHistory(
                context
            )
    }

    val trimmed =
        query.trim()

    val matchedSongs =
        remember(
            songs,
            trimmed
        ) {
            if (trimmed.isBlank()) {
                emptyList()
            } else {
                songs.filter {
                    it.title.contains(
                        trimmed,
                        ignoreCase = true
                    ) ||
                        it.artist.contains(
                            trimmed,
                            ignoreCase = true
                        ) ||
                        it.album.contains(
                            trimmed,
                            ignoreCase = true
                        )
                }
            }
        }

    val matchedArtists =
        remember(
            artists,
            trimmed
        ) {
            if (trimmed.isBlank()) {
                emptyList()
            } else {
                artists.filter {
                    it.name.contains(
                        trimmed,
                        ignoreCase = true
                    )
                }
            }
        }

    val matchedAlbums =
        remember(
            albums,
            trimmed
        ) {
            if (trimmed.isBlank()) {
                emptyList()
            } else {
                albums.filter {
                    it.name.contains(
                        trimmed,
                        ignoreCase = true
                    ) ||
                        it.artist.contains(
                            trimmed,
                            ignoreCase = true
                        )
                }
            }
        }

    val matchedCategories =
        remember(
            categories,
            trimmed
        ) {
            if (trimmed.isBlank()) {
                emptyList()
            } else {
                categories.filter {
                    it.name.contains(
                        trimmed,
                        ignoreCase = true
                    )
                }
            }
        }

    fun rememberSearch() {
        if (trimmed.isBlank()) {
            return
        }

        scope.launch {
            history =
                Store.addSearch(
                    context,
                    trimmed
                )
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    bottom = 190.dp
                )
        ) {
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
                            end = 14.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                ) {
                    SearchBar(
                        query = query,
                        c = c,
                        setQuery = {
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

                    LazyRow(
                        horizontalArrangement =
                            Arrangement.spacedBy(
                                8.dp
                            )
                    ) {
                        items(
                            SearchFilter.entries
                        ) { item ->
                            SearchChip(
                                text = item.label,
                                active =
                                    filter == item,
                                c = c
                            ) {
                                filter = item
                            }
                        }

                        item {
                            Box(
                                Modifier
                                    .padding(
                                        horizontal = 2.dp
                                    )
                                    .width(1.dp)
                                    .height(18.dp)
                                    .background(
                                        c.border
                                    )
                            )
                        }

                        item {
                            ThemeChip(
                                "Dark",
                                theme ==
                                    XmoTheme.Dark,
                                c
                            ) {
                                setTheme(
                                    XmoTheme.Dark
                                )
                            }
                        }

                        item {
                            ThemeChip(
                                "Light",
                                theme ==
                                    XmoTheme.Light,
                                c
                            ) {
                                setTheme(
                                    XmoTheme.Light
                                )
                            }
                        }

                        item {
                            ThemeChip(
                                "AMOLED",
                                theme ==
                                    XmoTheme.Amoled,
                                c
                            ) {
                                setTheme(
                                    XmoTheme.Amoled
                                )
                            }
                        }
                    }
                }
            }

            if (trimmed.isBlank()) {
                item(
                    key = "recent"
                ) {
                    RecentSearches(
                        history = history,
                        c = c,

                        use = {
                            query = it
                        },

                        remove = { value ->
                            scope.launch {
                                history =
                                    Store.removeSearch(
                                        context,
                                        value
                                    )
                            }
                        },

                        clear = {
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
                    key = "browse"
                ) {
                    BrowseCategories(
                        categories =
                            categories,
                        songs =
                            songs,
                        c = c,
                        onPlaySong =
                            onPlaySong
                    )
                }
            } else {
                item {
                    Text(
                        "Search Results",
                        color = c.text,
                        fontFamily =
                            XmoFont.bold,
                        fontSize = 16.sp,
                        modifier =
                            Modifier.padding(
                                start = 16.dp,
                                top = 18.dp,
                                bottom = 8.dp
                            )
                    )
                }

                if (
                    filter ==
                        SearchFilter.All ||
                    filter ==
                        SearchFilter.Songs
                ) {
                    items(
                        items =
                            matchedSongs,
                        key = {
                            "result_song_${it.id}"
                        }
                    ) { song ->
                        SongResult(
                            song = song,
                            c = c
                        ) {
                            rememberSearch()

                            onPlaySong(
                                song,
                                "Search",
                                false,
                                matchedSongs
                            )
                        }
                    }
                }

                if (
                    filter ==
                        SearchFilter.All ||
                    filter ==
                        SearchFilter.Artists
                ) {
                    items(
                        matchedArtists,
                        key = {
                            "artist_${it.name}"
                        }
                    ) {
                        ArtistResult(
                            artist = it,
                            c = c
                        )
                    }
                }

                if (
                    filter ==
                        SearchFilter.All ||
                    filter ==
                        SearchFilter.Albums
                ) {
                    items(
                        matchedAlbums,
                        key = {
                            "album_${it.id}"
                        }
                    ) {
                        AlbumResult(
                            album = it,
                            c = c
                        )
                    }
                }

                if (
                    filter ==
                        SearchFilter.All ||
                    filter ==
                        SearchFilter.Categories
                ) {
                    items(
                        matchedCategories,
                        key = {
                            "category_${it.id}"
                        }
                    ) { cat ->
                        CategoryResult(
                            category =
                                cat,
                            songs =
                                songs,
                            c = c,
                            onPlaySong =
                                onPlaySong,
                            saveSearch = {
                                rememberSearch()
                            }
                        )
                    }
                }

                val empty =
                    when (filter) {
                        SearchFilter.All ->
                            matchedSongs.isEmpty() &&
                                matchedArtists.isEmpty() &&
                                matchedAlbums.isEmpty() &&
                                matchedCategories.isEmpty()

                        SearchFilter.Songs ->
                            matchedSongs.isEmpty()

                        SearchFilter.Artists ->
                            matchedArtists.isEmpty()

                        SearchFilter.Categories ->
                            matchedCategories.isEmpty()

                        SearchFilter.Albums ->
                            matchedAlbums.isEmpty()
                    }

                if (empty) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment =
                                Alignment.Center
                        ) {
                            Text(
                                "No local results",
                                color = c.sub,
                                fontFamily =
                                    XmoFont.normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            item(
                key = "search_footer"
            ) {
                SearchFooter(c)
            }
        }

        if (historyExpanded) {
            RecentHistoryOverlay(
                history = history,
                c = c,

                close = {
                    historyExpanded =
                        false
                },

                use = {
                    query = it
                    historyExpanded = false
                },

                remove = { value ->
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

@Composable
private fun SearchBar(
    query: String,
    c: HomeColors,
    setQuery: (String) -> Unit,
    clear: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(
                RoundedCornerShape(
                    22.dp
                )
            )
            .background(c.button)
            .border(
                1.dp,
                if (query.isNotEmpty())
                    XmoRed.copy(.55f)
                else
                    c.border,
                RoundedCornerShape(
                    22.dp
                )
            )
            .padding(
                horizontal = 12.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Search,
            null,
            tint = c.sub,
            modifier =
                Modifier.size(18.dp)
        )

        BasicTextField(
            value = query,
            onValueChange = setQuery,
            singleLine = true,
            textStyle =
                LocalTextStyle.current.copy(
                    color = c.text,
                    fontFamily =
                        XmoFont.normal,
                    fontSize = 14.sp
                ),
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        horizontal = 10.dp
                    ),
            decorationBox = {
                if (query.isEmpty()) {
                    Text(
                        "Search songs, artists, albums...",
                        color = c.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize = 13.sp
                    )
                }

                it()
            }
        )

        if (query.isNotEmpty()) {
            IconButton(
                onClick = clear,
                modifier =
                    Modifier.size(30.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = c.sub,
                    modifier =
                        Modifier.size(16.dp)
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
                onClick = click
            )
            .padding(
                horizontal = 14.dp,
                vertical = 7.dp
            )
    ) {
        Text(
            text,
            color =
                if (active)
                    Color.White
                else
                    c.sub,
            fontFamily =
                XmoFont.medium,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ThemeChip(
    text: String,
    active: Boolean,
    c: HomeColors,
    click: () -> Unit
) {
    SearchChip(
        text,
        active,
        c,
        click
    )
}

@Composable
private fun RecentSearches(
    history: List<String>,
    c: HomeColors,
    use: (String) -> Unit,
    remove: (String) -> Unit,
    clear: () -> Unit,
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
                    horizontal = 4.dp,
                    bottom = 8.dp
                ),
            verticalAlignment =
                Alignment.CenterVertically
        ) {
            Text(
                "Recent Searches",
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 15.sp,
                modifier =
                    Modifier.weight(1f)
            )

            if (history.isNotEmpty()) {
                Text(
                    "Clear All",
                    color = XmoRed,
                    fontFamily =
                        XmoFont.medium,
                    fontSize = 11.sp,
                    modifier =
                        Modifier
                            .clickable(
                                onClick = clear
                            )
                            .padding(6.dp)
                )

                Spacer(
                    Modifier.width(4.dp)
                )

                Text(
                    "↗",
                    color = c.text,
                    fontSize = 15.sp,
                    modifier =
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(c.button)
                            .clickable(
                                onClick = expand
                            )
                            .wrapContentSize()
                )
            }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .background(c.surface)
                .border(
                    .7.dp,
                    c.border,
                    RoundedCornerShape(
                        16.dp
                    )
                )
                .padding(10.dp)
        ) {
            if (history.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Text(
                        "No recent searches",
                        color = c.sub,
                        fontFamily =
                            XmoFont.thin,
                        fontSize = 12.sp
                    )
                }
            } else {
                Column(
                    verticalArrangement =
                        Arrangement.spacedBy(
                            7.dp
                        )
                ) {
                    history
                        .take(5)
                        .forEach {
                            RecentRow(
                                it,
                                c,
                                use,
                                remove
                            )
                        }
                }
            }
        }
    }
}

@Composable
private fun RecentRow(
    text: String,
    c: HomeColors,
    use: (String) -> Unit,
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
            .background(c.button)
            .clickable {
                use(text)
            }
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        XmoIcon(
            R.drawable.ic_xmo_history,
            c.sub,
            Modifier.size(14.dp)
        )

        Text(
            text,
            color = c.text,
            fontFamily =
                XmoFont.normal,
            fontSize = 12.sp,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(
                        start = 10.dp
                    ),
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis
        )

        Text(
            "×",
            color = c.sub,
            fontSize = 18.sp,
            modifier =
                Modifier
                    .clickable {
                        remove(text)
                    }
                    .padding(
                        horizontal = 6.dp
                    )
        )
    }
}

@Composable
private fun BrowseCategories(
    categories: List<UserCategory>,
    songs: List<Song>,
    c: HomeColors,
    onPlaySong: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit
) {
    if (categories.isEmpty()) {
        return
    }

    Column(
        Modifier.padding(
            horizontal = 14.dp,
            vertical = 24.dp
        )
    ) {
        Text(
            "Browse Categories",
            color = c.text,
            fontFamily =
                XmoFont.bold,
            fontSize = 15.sp,
            modifier =
                Modifier.padding(
                    bottom = 10.dp
                )
        )

        categories
            .chunked(2)
            .forEach { row ->

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(
                            10.dp
                        )
                ) {
                    repeat(2) { i ->
                        Box(
                            Modifier.weight(1f)
                        ) {
                            row.getOrNull(i)
                                ?.let { cat ->
                                    val queue =
                                        remember(
                                            cat,
                                            songs
                                        ) {
                                            songs.filter {
                                                it.id in
                                                    cat.songIds
                                            }
                                        }

                                    BrowseCategoryCard(
                                        cat,
                                        queue,
                                        c,
                                        onPlaySong
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
            .background(c.surface)
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
            .padding(9.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(
                    RoundedCornerShape(
                        10.dp
                    )
                )
                .background(c.button)
        ) {
            Column {
                repeat(2) { r ->
                    Row(
                        Modifier.weight(1f)
                    ) {
                        repeat(2) { col ->
                            val song =
                                songs.getOrNull(
                                    r * 2 +
                                        col
                                )

                            AsyncImage(
                                model =
                                    song?.artwork,
                                contentDescription =
                                    null,
                                modifier =
                                    Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                contentScale =
                                    ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        Text(
            category.name,
            color = c.text,
            fontFamily =
                XmoFont.bold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow =
                TextOverflow.Ellipsis,
            modifier =
                Modifier.padding(
                    top = 7.dp
                )
        )

        Text(
            "${songs.size} tracks",
            color = c.sub,
            fontFamily =
                XmoFont.thin,
            fontSize = 9.sp
        )
    }
}

@Composable
private fun SongResult(
    song: Song,
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
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(c.surface)
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    12.dp
                )
            )
            .clickable(
                onClick = click
            )
            .padding(7.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model = song.artwork,
            contentDescription = null,
            modifier =
                Modifier
                    .size(46.dp)
                    .clip(
                        RoundedCornerShape(
                            8.dp
                        )
                    )
                    .background(c.button),
            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier
                .weight(1f)
                .padding(
                    horizontal = 11.dp
                )
        ) {
            Text(
                song.title,
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                "${song.artist} • ${song.album}",
                color = c.sub,
                fontFamily =
                    XmoFont.thin,
                fontSize = 9.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )
        }

        Box(
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(XmoRed),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                "▶",
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun ArtistResult(
    artist: Artist,
    c: HomeColors
) {
    SimpleResult(
        title = artist.name,
        subtitle =
            "${artist.songs.size} songs",
        artwork =
            artist.songs
                .firstOrNull()
                ?.artwork,
        c = c
    )
}

@Composable
private fun AlbumResult(
    album: Album,
    c: HomeColors
) {
    SimpleResult(
        title = album.name,
        subtitle =
            "${album.artist} • ${album.songs.size} songs",
        artwork =
            album.artwork,
        c = c
    )
}

@Composable
private fun SimpleResult(
    title: String,
    subtitle: String,
    artwork: Uri?,
    c: HomeColors
) {
    Row(
        Modifier
            .padding(
                horizontal = 14.dp,
                vertical = 4.dp
            )
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(c.surface)
            .border(
                .6.dp,
                c.border,
                RoundedCornerShape(
                    12.dp
                )
            )
            .padding(7.dp),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        AsyncImage(
            model = artwork,
            contentDescription = null,
            modifier =
                Modifier
                    .size(46.dp)
                    .clip(
                        RoundedCornerShape(
                            8.dp
                        )
                    )
                    .background(c.button),
            contentScale =
                ContentScale.Crop
        )

        Column(
            Modifier.padding(
                start = 11.dp
            )
        ) {
            Text(
                title,
                color = c.text,
                fontFamily =
                    XmoFont.bold,
                fontSize = 12.sp,
                maxLines = 1,
                overflow =
                    TextOverflow.Ellipsis
            )

            Text(
                subtitle,
                color = c.sub,
                fontFamily =
                    XmoFont.thin,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun CategoryResult(
    category: UserCategory,
    songs: List<Song>,
    c: HomeColors,
    onPlaySong: (
        Song,
        String,
        Boolean,
        List<Song>
    ) -> Unit,
    saveSearch: () -> Unit
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
                horizontal = 14.dp,
                vertical = 4.dp
            )
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    12.dp
                )
            )
            .background(c.surface)
            .clickable(
                enabled =
                    queue.isNotEmpty()
            ) {
                saveSearch()

                onPlaySong(
                    queue.first(),
                    category.name,
                    true,
                    queue
                )
            }
            .padding(14.dp)
    ) {
        Text(
            category.name,
            color = c.text,
            fontFamily =
                XmoFont.bold,
            fontSize = 12.sp,
            modifier =
                Modifier.weight(1f)
        )

        Text(
            "${queue.size} tracks",
            color = c.sub,
            fontFamily =
                XmoFont.thin,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RecentHistoryOverlay(
    history: List<String>,
    c: HomeColors,
    close: () -> Unit,
    use: (String) -> Unit,
    remove: (String) -> Unit
) {
    val scope =
        rememberCoroutineScope()

    val y =
        remember {
            Animatable(0f)
        }

    var height by remember {
        mutableFloatStateOf(1f)
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(c.bg)
            .onSizeChanged {
                height =
                    it.height.toFloat()
            }
            .graphicsLayer {
                translationY =
                    y.value
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
                    .height(58.dp)
                    .pointerInput(height) {
                        detectVerticalDragGestures(
                            onVerticalDrag = {
                                    change,
                                    amount ->

                                change.consume()

                                scope.launch {
                                    y.snapTo(
                                        (
                                            y.value +
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
                                        y.value >
                                        height * .15f
                                    ) {
                                        y.animateTo(
                                            height,
                                            tween(260)
                                        )

                                        close()
                                    } else {
                                        y.animateTo(0f)
                                    }
                                }
                            }
                        )
                    },
                verticalAlignment =
                    Alignment.CenterVertically
            ) {
                Spacer(
                    Modifier.width(48.dp)
                )

                Box(
                    Modifier
                        .weight(1f),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Box(
                        Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .background(
                                c.sub,
                                RoundedCornerShape(
                                    3.dp
                                )
                            )
                    )
                }

                IconButton(
                    onClick = close,
                    modifier =
                        Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = c.text
                    )
                }
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding =
                    PaddingValues(
                        start = 14.dp,
                        end = 14.dp,
                        bottom = 40.dp
                    ),
                verticalArrangement =
                    Arrangement.spacedBy(
                        8.dp
                    )
            ) {
                items(
                    history,
                    key = {
                        it
                    }
                ) {
                    RecentRow(
                        it,
                        c,
                        use,
                        remove
                    )
                }
            }
        }
    }
}

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
            color = c.text,
            fontFamily =
                XmoFont.logo,
            fontSize = 19.sp
        )

        Text(
            "lxzrvi • copyright © 2026",
            color = c.sub,
            fontFamily =
                XmoFont.thin,
            fontSize = 9.sp
        )
    }
}
