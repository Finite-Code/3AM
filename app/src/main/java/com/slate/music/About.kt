package com.slate.music

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.Heart.HeartEngine
import com.slate.music.amp.AmpEngine
import com.slate.music.amp.AmpEqualizer
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.CancellationException

@Composable
fun AboutScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    var backProgress by remember { mutableFloatStateOf(0f) }

    PredictiveBackHandler(enabled = isVisible) { progressFlow ->
        try {
            progressFlow.collect { backEvent ->
                backProgress = backEvent.progress
            }
            onClose()
        } catch (_: CancellationException) {
            backProgress = 0f
        }
    }

    LaunchedEffect(isVisible) {
        if (!isVisible) {
            backProgress = 0f
        }
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
        val songs by HeartEngine.songs.collectAsState()
        val isScanning by HeartEngine.isScanning.collectAsState()
        val ampState by AmpEngine.state.collectAsState()
        val eqState by AmpEqualizer.state.collectAsState()
        val scrollState = rememberLazyListState()

        DeadEndHapticHandler(scrollState)

        val totalDurationMs = remember(songs) { songs.sumOf { it.durationMs } }
        val totalHours = totalDurationMs / 1000 / 3600
        val totalMins = (totalDurationMs / 1000 % 3600) / 60

        val packageInfo = remember(context) {
            try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (_: Exception) {
                null
            }
        }
        val versionName = packageInfo?.versionName ?: "1.0.0"
        val cleanVersionName = versionName.substringBefore('-')
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: 1L
        } else {
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toLong() ?: 1L
        }

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
                contentPadding = PaddingValues(top = 180.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Identity Card ig
                item {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF141414),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF222222)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "3AM",
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                fontSize = 42.sp,
                                color = Color.White
                            )

                            Text(
                                text = "built for late-night music lovers",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF242424)
                            ) {
                                Text(
                                    text = "v$cleanVersionName • Build #$versionCode",
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Diagnostics Card
                item {
                    AboutSectionHeader(title = "System & Engine Diagnostics")
                }

                item {
                    AboutInfoRow(
                        icon = Icons.Rounded.Build,
                        title = "Build & Version",
                        subtitle = "Automated Build Counter #$versionCode",
                        value = "v$cleanVersionName"
                    )
                }

                item {
                    AboutInfoRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Audio Engine",
                        subtitle = "Jetpack Media3 (ExoPlayer 1.5.1)",
                        value = if (ampState.isPlaying) "Playing" else "Ready"
                    )
                }

                item {
                    AboutInfoRow(
                        icon = Icons.Rounded.Equalizer,
                        title = "DSP Audio Effects",
                        subtitle = "AmpEqualizer 5-Band EQ & Bass Boost",
                        value = if (eqState.isEnabled) "Active" else "Disabled"
                    )
                }

                item {
                    AboutInfoRow(
                        icon = Icons.Rounded.LibraryMusic,
                        title = "Scanned Library",
                        subtitle = "${songs.size} songs (${totalHours}h ${totalMins}m total)",
                        value = if (isScanning) "Scanning..." else "Synced"
                    )
                }

                item {
                    AboutInfoRow(
                        icon = Icons.Rounded.PhoneAndroid,
                        title = "Android Environment",
                        subtitle = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
                        value = "Target 37"
                    )
                }

                // Actions Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    AboutSectionHeader(title = "Tools & Actions")
                }

                item {
                    AboutActionRow(
                        icon = Icons.Rounded.Sync,
                        title = "Rescan Music Library",
                        subtitle = "Force MediaStore scan for new audio files",
                        onClick = {
                            context.performHapticClick()
                            HeartEngine.scanNow()
                        }
                    )
                }

                item {
                    AboutActionRow(
                        icon = Icons.Rounded.Code,
                        title = "GitHub Repository",
                        subtitle = "Finite-Code / 3AM (Open Source GPL-3.0)",
                        onClick = {
                            context.performHapticClick()
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Finite-Code/3AM"))
                            context.startActivity(intent)
                        }
                    )
                }

                item {
                    AboutActionRow(
                        icon = Icons.Rounded.RestartAlt,
                        title = "Reset App Preferences",
                        subtitle = "Restore default settings & clear caches",
                        onClick = {
                            context.performHapticClick()
                            val prefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                            prefs.edit().clear().apply()
                            AppSettings.initialize(context)
                        }
                    )
                }

                // Credits
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "A submission for HackClub 3AM",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Licensed under GPL-3.0",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            // Header
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
                        text = "about",
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
                                contentDescription = "Close About",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = Color.LightGray.copy(alpha = 0.88f),
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun AboutInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141414),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                text = value,
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun AboutActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF141414),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}