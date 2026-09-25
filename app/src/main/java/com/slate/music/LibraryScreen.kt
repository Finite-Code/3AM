package com.slate.music

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
fun LibraryScreen(
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val songs by HeartEngine.songs.collectAsState()
    val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()
    var selectedCategory by remember { mutableIntStateOf(0) }
    val categories = remember { listOf("Songs", "Artists", "Albums") }
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    DeadEndHapticHandler(scrollState)

    val groupedArtists = remember(songs) { songs.groupBy { it.artist } }
    val groupedAlbums = remember(songs) { songs.groupBy { it.album } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(state = hazeState),
            contentPadding = PaddingValues(top = 190.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when (selectedCategory) {
                0 -> {
                    itemsIndexed(songs, key = { _, song -> song.id }) { index, song ->
                        LibrarySongRow(
                            song = song,
                            onClick = {
                                context.performHapticClick()
                                AmpEngine.playPlaylist(songs, index)
                            }
                        )
                    }
                }
                1 -> {
                    groupedArtists.forEach { (artist, artistSongs) ->
                        item(key = artist) {
                            LibraryGroupCard(
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
                2 -> {
                    groupedAlbums.forEach { (album, albumSongs) ->
                        item(key = album) {
                            LibraryGroupCard(
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

        // Glassmorphic Header Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
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
                .background(
                    if (isBlurEnabled) {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.85f),
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black,
                                Color.Black.copy(alpha = 0.90f),
                                Color.Transparent
                            )
                        )
                    }
                )
                .align(Alignment.TopCenter)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "library",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 64.sp,
                    color = Color.White
                )

                // Category Selector Bar
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF181818),
                    modifier = Modifier.height(44.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(4.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        categories.forEachIndexed { index, title ->
                            val isSelected = selectedCategory == index
                            val backgroundColor by animateColorAsState(
                                targetValue = if (isSelected) Color.White else Color.Transparent,
                                label = "CategoryBg"
                            )
                            val textColor by animateColorAsState(
                                targetValue = if (isSelected) Color.Black else Color.Gray,
                                label = "CategoryText"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(backgroundColor)
                                    .clickable {
                                        context.performHapticClick()
                                        selectedCategory = index
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = textColor,
                                    fontSize = 14.sp,
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
private fun LibrarySongRow(
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
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title.lowercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    letterSpacing = (-1.0).sp,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
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
private fun LibraryGroupCard(
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
                    .size(52.dp)
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
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
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
