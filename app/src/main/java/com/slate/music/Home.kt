@file:OptIn(ExperimentalMaterial3Api::class)

package com.slate.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.HazeBlurStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import com.slate.music.Heart.HeartEngine

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
    val vibrator = remember(context) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    }

    LaunchedEffect(isCollapsed) {
        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)) // Should I try click or tick? gonna try the newer click thing for now
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

    val songs by HeartEngine.songs.collectAsState()
    val isScanning by HeartEngine.isScanning.collectAsState()

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
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var liked by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = Color.Black
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = scaffoldPadding.calculateBottomPadding())
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize().hazeSource(state = hazeState),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                item {
                    val spacerHeight = (headerMaxHeight - stickyHeaderHeight).coerceAtLeast(0.dp)
                    Spacer(modifier = Modifier.height(spacerHeight))
                }

                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(stickyHeaderHeight)
                            .hazeBlur(
                                input = HazeInput.Sources(state = hazeState),
                                style = HazeBlurStyle {
                                    blurRadius(24.dp)
                                    noiseFactor(0f)
                                    progressive(
                                        HazeProgressive.verticalGradient(
                                            startIntensity = (0.48f * collapseFraction),
                                            endIntensity = (0.001f * collapseFraction)
                                        )
                                    )
                                }
                            )
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 2f * collapseFraction),
                                        Color.Black.copy(alpha = 0.030f * collapseFraction)
                                    )
                                )
                            )
                    ) {
                        Box(modifier = Modifier.statusBarsPadding().fillMaxSize()) {
                            val fontSize = (148 - ((148 - 108) * snappedFraction)).sp
                            val yOffset = ((-16) * (1 - snappedFraction)).dp

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
                                    .padding(start = 24.dp, bottom = 20.dp)
                                    .offset(y = yOffset)
                            )
                        }
                    }
                }

                // Use hazeSource specifically on each card or section
                item { 
                    MusicSectionRow(
                        title = "Top Played", 
                        tracks = displayTracks, 
                        onTrackSelected = { track -> selectedTrack = track },
                        hazeState = hazeState
                    ) 
                }
                
                item { 
                    MusicSectionRow(
                        title = "Your Top Artists", 
                        tracks = displayTracks, 
                        onTrackSelected = { track -> selectedTrack = track },
                        hazeState = hazeState
                    ) 
                }
                
                item { 
                    MusicSectionRow(
                        title = "Favourites <3", 
                        tracks = favTracks,
                        onTrackSelected = { track -> selectedTrack = track },
                        hazeState = hazeState
                    ) 
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }

            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                onSearchClick = { /* TODO: Start Search Action */ },
                hazeState = hazeState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            SettingsScreen(
                isVisible = selectedTab == 2,
                onClose = { selectedTab = 0 },
                hazeState = hazeState
            )

            if (selectedTrack != null) {
                ModalBottomSheet(
                    onDismissRequest = { selectedTrack = null },
                    sheetState = sheetState,
                    containerColor = Color(0xFF121212)
                ) {
                    selectedTrack?.let { track ->
                        val isTrackLiked = track.id in favTrackIds
                        MusicPlayer(
                            title = track.title,
                            artist = track.artist,
                            albumArtUrl = track.imageUrl,
                            isPlaying = isPlaying,
                            progress = progress,
                            currentPosText = "1:15",
                            durtnText = track.duration,
                            liked = isTrackLiked,
                            onPlayPauseToggle = { isPlaying = !isPlaying },
                            onSkipPrevious = { /* TODO */ },
                            onSkipNext = { /* TODO */ },
                            onSeek = { progress = it },
                            onLikeToggle = {
                                favTrackIds = if (track.id in favTrackIds) {
                                    favTrackIds - track.id
                                } else {
                                    favTrackIds + track.id
                                }
                            },
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun MusicSectionRow(
    title: String,
    tracks: List<Track>,
    onTrackSelected: (Track) -> Unit,
    hazeState: HazeState
) {
    Column(
        modifier = Modifier
                    .padding(top = 16.dp)
                    .hazeSource(state = hazeState)
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
                text = "No tracks added yet :(" +
                        "\nwon't ya' hit some music?",
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
