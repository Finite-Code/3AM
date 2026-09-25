package com.slate.music

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.slate.music.Heart.HeartEngine
import com.slate.music.Heart.HeartSong
import com.slate.music.amp.AmpEngine
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource

@Composable
fun SearchScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible) {
        onClose()
    }

    if (!isVisible) return

    val songs by HeartEngine.songs.collectAsState()
    val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = remember { listOf("All", "Songs", "Artists", "Albums") }
    val scrollState = rememberLazyListState()

    DeadEndHapticHandler(scrollState)

    val filteredSongs = remember(songs, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true) ||
                    it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchedArtists = remember(filteredSongs) {
        filteredSongs.groupBy { it.artist }
    }

    val matchedAlbums = remember(filteredSongs) {
        filteredSongs.groupBy { it.album }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentPadding = PaddingValues(top = 180.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (searchQuery.isBlank()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Type to search songs, artists, or albums",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (filteredSongs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results for \"$searchQuery\"",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                when (selectedFilterIndex) {
                    0 -> {
                        if (filteredSongs.isNotEmpty()) {
                            item { SearchSectionHeader("Songs") }
                            itemsIndexed(filteredSongs.take(5), key = { _, song -> "song_${song.id}" }) { index, song ->
                                SearchSongRow(
                                    song = song,
                                    onClick = {
                                        context.performHapticClick()
                                        AmpEngine.playPlaylist(filteredSongs, index)
                                    }
                                )
                            }
                        }

                        if (matchedArtists.isNotEmpty()) {
                            item { SearchSectionHeader("Artists") }
                            matchedArtists.forEach { (artist, artistSongs) ->
                                item(key = "artist_$artist") {
                                    SearchGroupRow(
                                        title = artist,
                                        subtitle = "${artistSongs.size} tracks",
                                        albumArtUri = artistSongs.firstOrNull()?.albumArtUri,
                                        isArtist = true,
                                        onClick = {
                                            context.performHapticClick()
                                            AmpEngine.playPlaylist(artistSongs, 0)
                                        }
                                    )
                                }
                            }
                        }

                        if (matchedAlbums.isNotEmpty()) {
                            item { SearchSectionHeader("Albums") }
                            matchedAlbums.forEach { (album, albumSongs) ->
                                item(key = "album_$album") {
                                    SearchGroupRow(
                                        title = album,
                                        subtitle = "${albumSongs.size} tracks",
                                        albumArtUri = albumSongs.firstOrNull()?.albumArtUri,
                                        isArtist = false,
                                        onClick = {
                                            context.performHapticClick()
                                            AmpEngine.playPlaylist(albumSongs, 0)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    1 -> {
                        itemsIndexed(filteredSongs, key = { _, song -> song.id }) { index, song ->
                            SearchSongRow(
                                song = song,
                                onClick = {
                                    context.performHapticClick()
                                    AmpEngine.playPlaylist(filteredSongs, index)
                                }
                            )
                        }
                    }

                    2 -> {
                        matchedArtists.forEach { (artist, artistSongs) ->
                            item(key = "artist_$artist") {
                                SearchGroupRow(
                                    title = artist,
                                    subtitle = "${artistSongs.size} tracks",
                                    albumArtUri = artistSongs.firstOrNull()?.albumArtUri,
                                    isArtist = true,
                                    onClick = {
                                        context.performHapticClick()
                                        AmpEngine.playPlaylist(artistSongs, 0)
                                    }
                                )
                            }
                        }
                    }

                    3 -> {
                        matchedAlbums.forEach { (album, albumSongs) ->
                            item(key = "album_$album") {
                                SearchGroupRow(
                                    title = album,
                                    subtitle = "${albumSongs.size} tracks",
                                    albumArtUri = albumSongs.firstOrNull()?.albumArtUri,
                                    isArtist = false,
                                    onClick = {
                                        context.performHapticClick()
                                        AmpEngine.playPlaylist(albumSongs, 0)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(175.dp)
                .then(
                    if (isBlurEnabled) {
                        Modifier.hazeBlur(
                            input = HazeInput.Sources(state = hazeState),
                            style = HazeBlurStyle {
                                blurRadius(24.dp)
                                noiseFactor(0f)
                                progressive(
                                    HazeProgressive.verticalGradient(
                                        startIntensity = 0.90f,
                                        endIntensity = 0.0f
                                    )
                                )
                            }
                        )
                    } else Modifier
                )
                .background(if (isBlurEnabled) Color.Black.copy(alpha = 0.85f) else Color.Black)
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1C1C1C),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(22.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search songs, artists, albums...", color = Color.Gray) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Clear search",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close search",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF181818),
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp).fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        filters.forEachIndexed { index, title ->
                            val isSelected = selectedFilterIndex == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else Color.Transparent)
                                    .clickable {
                                        context.performHapticClick()
                                        selectedFilterIndex = index
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.Black else Color.Gray,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.LightGray,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchSongRow(
    song: HeartSong,
    onClick: () -> Unit
) {
    val durationSecs = song.durationMs / 1000
    val durationText = String.format("%d:%02d", durationSecs / 60, durationSecs % 60)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141414),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF242424))
            ) {
                if (!song.albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = song.albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = durationText,
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun SearchGroupRow(
    title: String,
    subtitle: String,
    albumArtUri: String?,
    isArtist: Boolean,
    onClick: () -> Unit
) {
    val artShape = if (isArtist) CircleShape else RoundedCornerShape(12.dp)

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141414),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(artShape)
                    .background(Color(0xFF242424))
            ) {
                if (!albumArtUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = albumArtUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(26.dp).align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    maxLines = 1
                )
            }
        }
    }
}