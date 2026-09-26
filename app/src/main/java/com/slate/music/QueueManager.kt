package com.slate.music

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.slate.music.Heart.HeartSong
import com.slate.music.amp.AmpEngine
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.CancellationException

// Queue Management Sheet & Actions
// TODO: Add drag-and-drop item reordering once Compose LazyList reorder API is stable!
// Note: Currently active playing track is highlighted with a metallic pink stroke border.

@Composable
fun QueueSheet(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    var backProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = isVisible) { progressFlow ->
        try {
            progressFlow.collect { backEvent -> backProgress = backEvent.progress }
            onClose()
        } catch (_: CancellationException) {
            backProgress = 0f
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) backProgress = 0f
    }

    val lowSpringAnim = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    val localHazeState = remember { HazeState() }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 3 },
            animationSpec = lowSpringAnim
        ) + fadeIn(tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight / 3 },
            animationSpec = lowSpringAnim
        ) + fadeOut(tween(300)),
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        val context = LocalContext.current
        val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()
        val ampState by AmpEngine.state.collectAsState()
        val scrollState = rememberLazyListState()

        DeadEndHapticHandler(scrollState)

        var showSaveDialog by remember { mutableStateOf(false) }
        var playlistNameInput by remember { mutableStateOf("") }

        val scale = 1f - (backProgress * 0.16f)
        val cornerRadius = (backProgress * 32).dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    clip = true
                    shape = RoundedCornerShape(cornerRadius)
                    shadowElevation = (backProgress * 16).dp.toPx()
                }
                .background(Color.Black)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = localHazeState),
                contentPadding = PaddingValues(top = 180.dp, bottom = 120.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val trackLabel = if (ampState.currentIndex >= 0) "Track ${ampState.currentIndex + 1} of queue" else "Active Queue"
                        Text(
                            text = trackLabel,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                context.performHapticClick()
                                playlistNameInput = ampState.currentSong?.title?.let { "$it Session" } ?: "Queue Playlist"
                                showSaveDialog = true
                            }) {
                                Text("Save as Playlist", color = Color(0xFFEFB4E0), fontSize = 13.sp)
                            }

                            TextButton(onClick = {
                                context.performHapticClick()
                                AmpEngine.clearQueue()
                            }) {
                                Text("Clear", color = Color.Red.copy(alpha = 0.8f), fontSize = 13.sp)
                            }
                        }
                    }
                }

                // Currently active playing song item row
                item {
                    ampState.currentSong?.let { activeSong ->
                        QueueSongRow(
                            song = activeSong,
                            isPlaying = true,
                            onRemove = { AmpEngine.removeQueueItem(ampState.currentIndex) }
                        )
                    }
                }
            }

            // Header Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .then(
                        if (isBlurEnabled) {
                            Modifier.hazeBlur(
                                input = HazeInput.Sources(state = localHazeState),
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
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (isBlurEnabled) 0.85f else 1.0f),
                                Color.Black.copy(alpha = if (isBlurEnabled) 0.35f else 0.70f),
                                Color.Transparent
                            )
                        )
                    )
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "playing queue",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 40.sp,
                        color = Color.White
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Queue",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            if (showSaveDialog) {
                AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    confirmButton = {
                        TextButton(onClick = {
                            if (playlistNameInput.isNotBlank()) {
                                PlaylistManager.createPlaylist(context, playlistNameInput)
                            }
                            showSaveDialog = false
                        }) {
                            Text("Save", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSaveDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    title = { Text("Save Queue as Playlist", color = Color.White) },
                    text = {
                        OutlinedTextField(
                            value = playlistNameInput,
                            onValueChange = { playlistNameInput = it },
                            label = { Text("Playlist Name") },
                            singleLine = true
                        )
                    },
                    containerColor = Color(0xFF1E1E1E)
                )
            }
        }
    }
}

@Composable
private fun QueueSongRow(
    song: HeartSong,
    isPlaying: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141414),
        border = if (isPlaying) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEFB4E0).copy(alpha = 0.5f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
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
                        modifier = Modifier.size(22.dp).align(Alignment.Center)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title.lowercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = if (isPlaying) Color(0xFFEFB4E0) else Color.White,
                    maxLines = 1,
                    letterSpacing = (-0.8).sp,
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

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Remove",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
