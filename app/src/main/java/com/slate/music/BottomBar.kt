package com.slate.music

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.Alignment.*
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur

@Composable
fun BottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF1E1E1E).copy(alpha = 0.16f),
            modifier = Modifier
                .clip(CircleShape)
                .hazeBlur(
                    input = HazeInput.Sources(state = hazeState),
                    style = HazeBlurStyle {
                        blurRadius(24.dp)
                        noiseFactor(0f)
                    }
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = {onTabSelected(0)}) {
                    Icon(
                        imageVector = Icons.Rounded.Home,
                        contentDescription = "Home",
                        tint = if (selectedTab == 0) Color.White else Color.LightGray
                    )
                }

                IconButton(onClick = {onTabSelected(1)}) {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Library",
                        tint = if (selectedTab == 1) Color.White else Color.LightGray
                    )
                }

                IconButton(onClick = {onTabSelected(2)}) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = if (selectedTab == 2) Color.White else Color.LightGray
                    )
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = Color(0xFF1E1E1E).copy(alpha = 0.16f),
            shadowElevation = 8.dp,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .hazeBlur(
                    input = HazeInput.Sources(state = hazeState),
                    style = HazeBlurStyle {
                        blurRadius(24.dp)
                        noiseFactor(0f)
                    }
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }
        }
    }
}