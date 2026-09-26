package com.slate.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.toLowerCase
import coil.compose.AsyncImage


@Composable
fun MusicPlayer(
    title: String,
    artist: String,
    albumArtUrl: String?,
    isPlaying: Boolean,
    progress: Float,
    currentPosText: String,
    durtnText: String,
    liked: Boolean,
    onPlayPauseToggle: ()  -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onSeek: (Float) -> Unit,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // album art wrapper
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1E1E))
        ) {
            if(!albumArtUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = albumArtUrl,
                    contentDescription = "Album Art :)",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else{
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.DarkGray)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // song text & side actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)){
                Text(
                    text = title.lowercase(),
                    style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                     fontSize = 28.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    letterSpacing = (-1.5).sp,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onLikeToggle) {
                Icon(
                    imageVector = if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = null,
                    tint = if (liked) Color.Red else Color.LightGray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // progress scrub
        Column(modifier = Modifier.fillMaxWidth()){
            Slider(
                value = progress,
                onValueChange = onSeek,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha=0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = currentPosText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )

                Text(
                    text = durtnText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // main transport controls
        Row(
           modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            IconButton(onClick = {/* TODO: Shuffle */ }){
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    tint = Color.LightGray,
                )
            }

            IconButton(onClick = onSkipPrevious) {
                Icon(
                    imageVector = Icons.Rounded.SkipPrevious,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(36.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(52.dp)
            ){
                IconButton(onClick = onPlayPauseToggle,
                    modifier = Modifier.fillMaxSize()
                ) {

                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            IconButton(onClick = onSkipNext) {
                Icon(
                    imageVector = Icons.Rounded.SkipNext,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            IconButton(onClick = {/* TODO: Repeat */ }){
                Icon(
                    imageVector = Icons.Rounded.Repeat,
                    contentDescription = null,
                    tint = Color.LightGray,
                )
            }
        }
    }
}