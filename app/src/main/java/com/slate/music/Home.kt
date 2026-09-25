@file:OptIn(ExperimentalMaterial3Api::class)

package com.slate.music

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.Heart.HeartEngine
import com.slate.music.Heart.HeartSong
import com.slate.music.amp.AmpEngine
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import coil.compose.AsyncImage

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val duration: String,
)

@Preview
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    val hazeState = remember { HazeState() }
    val scrollState = rememberLazyListState()

    val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()

    DeadEndHapticHandler(scrollState)

    var selectedTab by remember { mutableStateOf(0) }

    // 1. Define sticky header heights
    val headerMaxHeight = 320.dp
    val stickyHeaderHeight = 240.dp 
    
    val density = LocalDensity.current
    val headerMaxHeightPx = with(density) { headerMaxHeight.toPx() }
    val stickyHeaderHeightPx = with(density) { stickyHeaderHeight.toPx() }
    val maxScrollPx = headerMaxHeightPx - stickyHeaderHeightPx

    val scrollOffset by remember {
        derivedStateOf {
            if (scrollState.firstVisibleItemIndex == 0) {
                scrollState.firstVisibleItemScrollOffset.toFloat()
            } else {
                maxScrollPx + 1000f
            }
        }
    }

    val collapseFraction = (scrollOffset / maxScrollPx).coerceIn(0f, 1f)
    val isCollapsed = collapseFraction > 0.5f

    val context = LocalContext.current
    LaunchedEffect(isCollapsed) {
        context.performHapticClick()
    }

    val snappedFraction by animateFloatAsState(
        targetValue = if (isCollapsed) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "SnapAnim"
    )

    val brushedSteelBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xF06A6D73), // Slightly darker edge to anchor it
                Color(0xFF9BA0A5), // Smoother transition gray
                Color(0xFFEFB4E0), // Slightly Red, Broad highlight
                Color(0xFFD2D2FF), // Bluish center gleam
                Color(0xFF8D9096), // Stronger shadow right after the gleam
                Color(0xEBB8BCC2)  // Soft edge
            )
        )
    }

    val ampState by AmpEngine.state.collectAsState()
    val songs by HeartEngine.songs.collectAsState()

    val displayTracks = remember(songs) {
        songs.map { song ->
            val mins = (song.durationMs / 1000 / 60).toInt()
            val secs = (song.durationMs / 1000 % 60).toInt()
            Track(
                id = song.id.toString(),
                title = song.title,
                artist = song.artist,
                imageUrl = song.albumArtUri ?: "",
                duration = String.format("%d:%02d", mins, secs)
            )
        }
    }

    var favTrackIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    val favTracks = remember(displayTracks, favTrackIds) {
        displayTracks.filter { track -> track.id in favTrackIds }
    }

    var selectedTrack by remember { mutableStateOf<Track?>(null) }
    var isPlayerSheetVisible by remember { mutableStateOf(false) }

    val handleTrackSelected: (Track) -> Unit = { track ->
        selectedTrack = track
        isPlayerSheetVisible = true
        val songIndex = songs.indexOfFirst { it.id.toString() == track.id }
        if (songIndex >= 0) {
            AmpEngine.playPlaylist(songs, songIndex)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var isSearchOpen by remember { mutableStateOf(false) }
    var isStatsOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Black
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount > 40f && !isStatsOpen && !isSearchOpen && !isPlayerSheetVisible) {
                            context.performHapticClick()
                            isStatsOpen = true
                        }
                    }
                }
        ) {
            if (selectedTab == 1) {
                LibraryScreen(hazeState = hazeState)
            } else {
                LazyColumn(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(state = hazeState),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(
                        top = headerMaxHeight,
                        bottom = 120.dp
                    )
                ) {
                    item {
                        MusicSectionRow(
                            title = "Top Played",
                            tracks = displayTracks,
                            onTrackSelected = handleTrackSelected
                        )
                    }

                    item {
                        MusicSectionRow(
                            title = "Your Top Artists",
                            tracks = displayTracks,
                            onTrackSelected = handleTrackSelected
                        )
                    }

                    item {
                        MusicSectionRow(
                            title = "Favourites <3",
                            tracks = favTracks,
                            onTrackSelected = handleTrackSelected
                        )
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }

                val currentHeaderHeight = headerMaxHeight - ((headerMaxHeight - stickyHeaderHeight) * collapseFraction)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(currentHeaderHeight)
                        .hazeBlur(
                            input = HazeInput.Sources(state = hazeState),
                            style = HazeBlurStyle {
                                blurRadius(if (isBlurEnabled) 24.dp else 0.dp)
                                noiseFactor(0f)
                                progressive(
                                    HazeProgressive.verticalGradient(
                                        startIntensity = if (isBlurEnabled) 0.90f else 0f,
                                        endIntensity = 0.0f
                                    )
                                )
                            }
                        )
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = (0.80f * collapseFraction + 0.25f).coerceIn(0f, 1f)),
                                    Color.Black.copy(alpha = (0.30f * collapseFraction).coerceIn(0f, 1f)),
                                    Color.Transparent
                                )
                            )
                        )
                        .align(Alignment.TopCenter)
                ) {
                    Box(modifier = Modifier.statusBarsPadding().fillMaxSize()) {
                        val fontSize = (148 - ((148 - 108) * snappedFraction)).sp

                        Text(
                            text = "3AM",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp,
                                brush = brushedSteelBrush,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    offset = Offset(0f, 4f),
                                    blurRadius = 6f
                                )
                            ),
                            fontSize = fontSize,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 24.dp, bottom = 12.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                AnimatedVisibility(
                    visible = ampState.currentSong != null && !isPlayerSheetVisible,
                    enter = slideInVertically { it} + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    MiniPlayer(
                        song = ampState.currentSong,
                        isPlaying = ampState.isPlaying,
                        progressMs = ampState.progressMs,
                        durationMs = ampState.durationMs,
                        onPlayPauseToggle = { AmpEngine.togglePlayPause() },
                        onClick = { isPlayerSheetVisible = true },
                        hazeState = hazeState
                    )
                }

                BottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    onSearchClick = {
                        context.performHapticClick()
                        isSearchOpen = true
                    },
                    hazeState = hazeState
                )
            }

            SettingsScreen(
                isVisible = selectedTab == 2,
                onClose = { selectedTab = 0 },
                hazeState = hazeState
            )

            SearchScreen(
                isVisible = isSearchOpen,
                onClose = { isSearchOpen = false },
                hazeState = hazeState
            )

            ListeningStatsScreen(
                isVisible = isStatsOpen,
                onClose = { isStatsOpen = false },
                hazeState = hazeState
            )

            if (isPlayerSheetVisible && (selectedTrack != null || ampState.currentSong != null)) {
                ModalBottomSheet(
                    onDismissRequest = {
                        isPlayerSheetVisible = false
                        selectedTrack = null
                    },
                    sheetState = sheetState,
                    containerColor = Color(0xFF121212)
                ) {
                    val currentSong = ampState.currentSong
                    val title = currentSong?.title ?: selectedTrack?.title ?: ""
                    val artist = currentSong?.artist ?: selectedTrack?.artist ?: ""
                    val albumArtUrl = currentSong?.albumArtUri ?: selectedTrack?.imageUrl
                    val trackId = currentSong?.id?.toString() ?: selectedTrack?.id ?: ""

                    val isTrackLiked = trackId in favTrackIds

                    val curSecs = ampState.progressMs / 1000
                    val currentPosText = String.format("%d:%02d", curSecs / 60, curSecs % 60)

                    val durSecs = ampState.durationMs / 1000
                    val durtnText = String.format("%d:%02d", durSecs / 60, durSecs % 60)

                    val progress = if (ampState.durationMs > 0) {
                        (ampState.progressMs.toFloat() / ampState.durationMs).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    MusicPlayer(
                        title = title,
                        artist = artist,
                        albumArtUrl = albumArtUrl,
                        isPlaying = ampState.isPlaying,
                        progress = progress,
                        currentPosText = currentPosText,
                        durtnText = durtnText,
                        liked = isTrackLiked,
                        onPlayPauseToggle = { AmpEngine.togglePlayPause() },
                        onSkipPrevious = { AmpEngine.skipPrevious() },
                        onSkipNext = { AmpEngine.skipNext() },
                        onSeek = { newProgressFraction ->
                            val targetMs = (newProgressFraction * ampState.durationMs).toLong()
                            AmpEngine.seekTo(targetMs)
                        },
                        onLikeToggle = {
                            if (trackId.isNotEmpty()) {
                                favTrackIds = if (trackId in favTrackIds) {
                                    favTrackIds - trackId
                                } else {
                                    favTrackIds + trackId
                                }
                            }
                        },
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun MiniPlayer(
    song: HeartSong?,
    isPlaying: Boolean,
    progressMs: Long,
    durationMs: Long,
    onPlayPauseToggle: () -> Unit,
    onClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    if (song == null) return

    val progressFraction = if (durationMs > 0) {
        (progressMs.toFloat() / durationMs).coerceIn(0f, 1f)
    } else 0f

    val outerShape = RoundedCornerShape(18.dp)
    val innerAlbumArtShape = RoundedCornerShape(10.dp)

    val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()

    Surface(
        shape = outerShape,
        color = if (isBlurEnabled) Color(0xFF1E1E1E).copy(alpha = 0.85f) else Color(0xFF1E1E1E),        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(outerShape)
            .clickable(onClick = onClick)
            .hazeBlur(
                input = HazeInput.Sources(state = hazeState),
                style = HazeBlurStyle {
                    blurRadius(if (isBlurEnabled) 24.dp else 0.dp)
                    noiseFactor(0f)
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(innerAlbumArtShape)
                        .background(Color(0xFF282828))
                ) {
                    if (!song.albumArtUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = song.albumArtUri,
                            contentDescription = "Easter Egg duhh",
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

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onPlayPauseToggle,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                trackColor = Color.White.copy(alpha = 0.15f),
                color = Color.White
            )
        }
    }
}

@Composable
fun MusicSectionRow(
    title: String,
    tracks: List<Track>,
    onTrackSelected: (Track) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier
                .padding(start = 16.dp, bottom = 12.dp)
        )

        if (tracks.isEmpty()) {
            Text(
                text = "No tracks added yet :(\nwon't ya' hit some music?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(tracks, key = { it.id }) { track ->
                    SquareMusicCard(
                        track = track,
                        onClick = { onTrackSelected(track) },
                        modifier = Modifier.width(176.dp)
                    )
                }
            }
        }
    }
}
