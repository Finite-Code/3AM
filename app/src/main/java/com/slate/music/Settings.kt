@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.slate.music

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import dev.chrisbanes.haze.*
import androidx.activity.compose.BackHandler

@Composable
fun SettingsScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = isVisible){
        onClose()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight / 3 },
            animationSpec = tween(400)
        ) + fadeIn(tween(300)),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight / 3 },
            animationSpec = tween(300)
        ) + fadeOut(tween(300)),
        modifier = modifier
    ) {
        var hapticFeedback by remember { mutableStateOf(true) }
        var highRes by remember { mutableStateOf(true) }
        var x_fadeDur by remember { mutableFloatStateOf(3f) }
        var gaplessPlay by remember { mutableStateOf(true) }
        var darkGlass by remember { mutableStateOf(true) }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState),
                contentPadding = PaddingValues(
                    top = 64.dp,
                    bottom = 120.dp,
                    start = 20.dp,
                    end = 20.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "settings",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold
                            ),
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
                                    contentDescription = "Close Settings",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                /* SECTION 1: Audio */
                item {
                    SettingsSectionHeader(title = "audio and playback")
                }

                item {
                    SettingsItemSwitch(
                        icon = Icons.Rounded.GraphicEq,
                        title = "High-Res Audio",
                        subtitle = "Stream in high-res audio",
                        checked = highRes,
                        onCheckedChange = { highRes = it }
                    )
                }

                item {
                    SettingsItemSwitch(
                        icon = Icons.AutoMirrored.Rounded.QueueMusic,
                        title = "Gapless Playback",
                        subtitle = "Skip songs when finished",
                        checked = gaplessPlay,
                        onCheckedChange = { gaplessPlay = it }
                    )
                }

                item {
                    SettingsItemSlider(
                        icon = Icons.Rounded.Timelapse,
                        title = "Crossfade",
                        value = x_fadeDur,
                        valueRange = 0f..10f,
                        valueLabel = "${x_fadeDur.toInt()}s",
                        onValueChange = { x_fadeDur = it }
                    )
                }

                /* SECTION 2: Haptics & Visuals */
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSectionHeader(title = "appearance and haptics")
                }

                item {
                    SettingsItemSwitch(
                        icon = Icons.Rounded.BlurOn,
                        title = "Glassmorphic Blur",
                        subtitle = "Enable real-time ambient blur effects",
                        checked = darkGlass,
                        onCheckedChange = { darkGlass = it }
                    )
                }

                item {
                    SettingsItemSwitch(
                        icon = Icons.Rounded.Vibration,
                        title = "Haptic Feedback",
                        subtitle = "Enable or disable haptic feedback",
                        checked = hapticFeedback,
                        onCheckedChange = { hapticFeedback = it }
                    )
                }

                /* SECTION 3: About */
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    SettingsSectionHeader(title = "about")
                }

                item {
                    SettingsItemClickable(
                        icon = Icons.Rounded.Info,
                        title = "Build ID",
                        value = "0.1.0-alpha",
                        onClick = { /* TODO: An Easter Egg perhaps */ }
                    )
                }
            }
        }
    }
}

/* --- REUSABLE COMPONENTS --- */

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.lowercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Color.LightGray.copy(alpha = 0.6f),
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141414),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
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
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color.White,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF2C2C2C)
                )
            )
        }
    }
}

@Composable
fun SettingsItemClickable(
    icon: ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141414),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 16.dp),
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

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            if (value != null) {
                Text(
                    text = value,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(end = 8.dp)
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

@Composable
fun SettingsItemSlider(
    icon: ImageVector,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueLabel: String,
    onValueChange: (Float) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF141414),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = valueLabel,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}