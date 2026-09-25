package com.slate.music

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntOffset
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
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.NightlightRound
import androidx.compose.material.icons.rounded.Schedule
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
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

@Composable
fun ListeningStatsScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible) {
        onClose()
    }

    val lowSpringAnim = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = lowSpringAnim
        ) + fadeIn(tween(300)),
        exit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = lowSpringAnim
        ) + fadeOut(tween(300)),
        modifier = modifier
    ) {
        val statsState by ListeningStatsManager.state.collectAsState()
        val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()
        val scrollState = rememberLazyListState()

        DeadEndHapticHandler(scrollState)

        val todayHours = statsState.todayListeningTimeMs / 1000 / 3600
        val todayMins = (statsState.todayListeningTimeMs / 1000 % 3600) / 60

        val weeklyHours = statsState.weeklyListeningTimeMs / 1000 / 3600
        val weeklyMins = (statsState.weeklyListeningTimeMs / 1000 % 3600) / 60

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                    top = 180.dp,
                    bottom = 120.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time Summary Card
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF141414),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Listening Time",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Today", color = Color.Gray, fontSize = 13.sp)
                                    Text(
                                        text = "${todayHours}h ${todayMins}m",
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                Column {
                                    Text("This Week", color = Color.Gray, fontSize = 13.sp)
                                    Text(
                                        text = "${weeklyHours}h ${weeklyMins}m",
                                        color = Color.White,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }

                // Night Heatmap Card
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF141414),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.08f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.NightlightRound,
                                        contentDescription = null,
                                        tint = Color(0xFFEFB4E0),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "3AM Night Sessions",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFF282828))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${statsState.nightOwl3AmPlayCount} plays",
                                        color = Color(0xFFEFB4E0),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Hourly Distribution Bars
                            val maxCount = (statsState.hourlyDistribution.values.maxOrNull()
                                ?: 1).coerceAtLeast(1)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                for (hour in 0..23) {
                                    val count = statsState.hourlyDistribution[hour] ?: 0
                                    val barHeightFraction =
                                        (count.toFloat() / maxCount).coerceIn(0.08f, 1f)
                                    val is3Am = hour == 3

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Bottom,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(barHeightFraction)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (is3Am) Color(0xFFEFB4E0) else Color.White.copy(
                                                        alpha = 0.25f
                                                    )
                                                )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("00:00", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    "03:00 (Peak)",
                                    color = Color(0xFFEFB4E0),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("12:00", color = Color.Gray, fontSize = 11.sp)
                                Text("23:00", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Top Songs Section
                if (statsState.topSongs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Most Played Tracks",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                        )
                    }

                    itemsIndexed(statsState.topSongs) { rank, song ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF141414),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${rank + 1}",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(28.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
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
                                        text = song.title,
                                        color = Color.White,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artist,
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "${song.playCount} plays",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
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
                    .background(if (isBlurEnabled) Color.Black.copy(alpha = 0.75f) else Color.Black)
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Insights,
                            contentDescription = null,
                            tint = Color(0xFFEFB4E0),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E1E1E),
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Stats",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}