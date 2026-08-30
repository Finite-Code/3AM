@file:OptIn(ExperimentalMaterial3Api::class)

package com.slate.music

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun MainScreen() {
    val hazeState = remember { HazeState() }
    val lazyColumnState = rememberLazyListState()

    // 1. Define sticky header heights
    val headerMaxHeight = 320.dp
    val stickyHeaderHeight = 240.dp 
    
    val density = LocalDensity.current
    val headerMaxHeightPx = with(density) { headerMaxHeight.toPx() }
    val stickyHeaderHeightPx = with(density) { stickyHeaderHeight.toPx() }
    val maxScrollPx = headerMaxHeightPx - stickyHeaderHeightPx

    val scrollOffset by remember {
        derivedStateOf {
            if (lazyColumnState.firstVisibleItemIndex == 0) {
                lazyColumnState.firstVisibleItemScrollOffset.toFloat()
            } else {
                maxScrollPx + 1000f
            }
        }
    }

    val collapseFraction = (scrollOffset / maxScrollPx).coerceIn(0f, 1f)

    val brushedSteelBrush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF6A6D73), // Slightly darker edge to anchor it
                Color(0xFF9BA0A5), // Smoother transition gray
                Color(0xFFE5E7EA), // Broad highlight
                Color(0xFFFFFFFF), // Pure white center gleam
                Color(0xFF8D9096), // Stronger shadow right after the gleam
                Color(0xFFB8BCC2)  // Soft edge
            )
        )
    }

    val dummies = listOf(
        Track("1", "Blinding Lights", "The Weeknd", "", "4:03"),
        Track("2", "Mesmerizing Lights", "The Monday", "", "4:03"),
        Track("3", "Lovely Lights", "Bro on Tuesday", "", "4:03"),
    )

    Scaffold(
        containerColor = Color.Black
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            LazyColumn(
                state = lazyColumnState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                            startIntensity = 1f,
                                            endIntensity = 0f
                                        )
                                    )
                                }
                            )
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 5f * collapseFraction),
                                        Color.Black.copy(alpha = 0f * collapseFraction)
                                    )
                                )
                            )
                    ) {
                        Box(modifier = Modifier.statusBarsPadding().fillMaxSize()) {
                            val fontSize = (148 - ((148 - 108) * collapseFraction)).sp
                            val yOffset = ((-16) * (1 - collapseFraction)).dp

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
                    HorizontalMusicSection(
                        title = "Top Played", 
                        tracks = dummies, 
                        onTrackClick = {},
                        hazeState = hazeState
                    ) 
                }
                
                item { 
                    HorizontalMusicSection(
                        title = "Your Top Artists", 
                        tracks = dummies, 
                        onTrackClick = {},
                        hazeState = hazeState
                    ) 
                }
                
                item { 
                    HorizontalMusicSection(
                        title = "Favourites <3", 
                        tracks = dummies, 
                        onTrackClick = {},
                        hazeState = hazeState
                    ) 
                }
                
                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}


@Composable
fun HorizontalMusicSection(
    title: String,
    tracks: List<Track>,
    onTrackClick: (Track) -> Unit,
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

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(tracks) { track ->
                SquareMusicCard(
                    track = track,
                    onClick = { onTrackClick(track) },
                    modifier = Modifier
                        .width(176.dp)
                )
            }
        }
    }
}
