@file:OptIn(ExperimentalMaterial3Api::class)

package com.slate.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import com.slate.music.data.HeartEngine
import com.slate.music.util.performHapticClick
import kotlinx.coroutines.launch
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material3.*
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.AppSettings
import com.slate.music.amp.AmpEngine
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TrackContextMenu(
    track: Track?,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Quick look-ups
    val songs by HeartEngine.songs.collectAsState()
    val favTrackIds by AppSettings.favoriteTrackIds.collectAsState()

    val isLiked = track?.id in favTrackIds

    val popAnimSpec = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val slideAnimSpec = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    AnimatedVisibility(
        visible = isVisible && track != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        if (track == null) return@AnimatedVisibility

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        context.performHapticClick()
                        onDismiss()
                    })
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(initialScale = 0.8f, animationSpec = popAnimSpec) + fadeIn(),
                    exit = scaleOut(targetScale = 0.8f) + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .aspectRatio(1f)
                    ) {
                        SquareMusicCard(
                            track = track,
                            onClick = { },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(initialOffsetY = { 50 }, animationSpec = slideAnimSpec) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            ContextMenuItem(
                                icon = Icons.Rounded.QueuePlayNext,
                                title = "Play Next",
                                onClick = {
                                    context.performHapticClick()
                                    songs.find { it.id.toString() == track.id }?.let { AmpEngine.playNext(it) }
                                    onDismiss()
                                }
                            )
                            
                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                            
                            ContextMenuItem(
                                icon = Icons.Rounded.PlaylistAdd,
                                title = "Add to Playlist",
                                onClick = {
                                    context.performHapticClick()
                                    // TODO: Gotta hook up to playlit dialog
                                    onDismiss()
                                }
                            )

                            Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))

                            ContextMenuItem(
                                icon = if (isLiked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                title = if (isLiked) "Remove from Favorites" else "Add to Favorites",
                                tint = if (isLiked) Color.Red else Color.White,
                                onClick = {
                                    context.performHapticClick()
                                    coroutineScope.launch {
                                        AppSettings.toggleFavorite(context, track.id)
                                    }
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextMenuItem(
    icon: ImageVector,
    title: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = tint,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}