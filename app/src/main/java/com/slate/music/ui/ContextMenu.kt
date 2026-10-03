@file:OptIn(ExperimentalMaterial3Api::class)

package com.slate.music.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.QueuePlayNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.AppSettings
import com.slate.music.amp.AmpEngine
import com.slate.music.data.HeartEngine
import com.slate.music.util.performHapticClick
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import kotlinx.coroutines.launch

@Composable
fun TrackContextMenu(
    track: Track?,
    bounds: Rect?,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    hazeState: HazeState
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val songs by HeartEngine.songs.collectAsState()
    val favTrackIds by AppSettings.favoriteTrackIds.collectAsState()
    val isLiked = track?.id in favTrackIds

    val density = LocalDensity.current
    val config = LocalConfiguration.current

    val springProgress by animateFloatAsState(
        targetValue = if (isVisible && track != null && bounds != null && bounds != Rect.Zero) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ContextMenuSpring"
    )

    AnimatedVisibility(
        visible = isVisible && track != null && bounds != null && bounds != Rect.Zero,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        if (track == null || bounds == null) return@AnimatedVisibility

        val screenHeightDp = config.screenHeightDp.dp
        val screenWidthDp = config.screenWidthDp.dp

        val cardLeftDp = with(density) { bounds.left.toDp() }
        val cardTopDp = with(density) { bounds.top.toDp() }
        val cardWidthDp = with(density) { bounds.width.toDp() }
        val cardHeightDp = with(density) { bounds.height.toDp() }
        val cardBottomDp = cardTopDp + cardHeightDp

        val fitsBelow = (screenHeightDp - cardBottomDp) > 220.dp

        val cardScale = 1.0f + (springProgress * 0.10f)
        val menuScale = 0.85f + (springProgress * 0.15f)
        val menuAlpha = (springProgress * 1.5f).coerceIn(0f, 1f)

        val menuWidth = 240.dp
        val rawMenuLeft = cardLeftDp + (cardWidthDp / 2) - (menuWidth / 2)
        val menuLeft = rawMenuLeft.coerceIn(16.dp, (screenWidthDp - menuWidth - 16.dp).coerceAtLeast(16.dp))

        val menuTop = if (fitsBelow) {
            cardBottomDp + 20.dp
        } else {
            (cardTopDp - 200.dp - 16.dp).coerceAtLeast(16.dp)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeBlur(
                        input = HazeInput.Sources(state = hazeState),
                        style = HazeBlurStyle {
                            blurRadius((springProgress * 28).dp)
                            noiseFactor(0f)
                        }
                    )
                    .background(Color.Black.copy(alpha = springProgress * 0.65f))
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            context.performHapticClick()
                            onDismiss()
                        })
                    }
            )

            Box(
                modifier = Modifier
                    .offset(x = cardLeftDp, y = cardTopDp)
                    .size(width = cardWidthDp, height = cardHeightDp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        shadowElevation = (springProgress * 24).dp.toPx()
                        shape = RoundedCornerShape(24.dp)
                        clip = false
                    }
            ) {
                SquareMusicCard(
                    track = track,
                    onClick = { }
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1E1E1E),
                modifier = Modifier
                    .offset(x = menuLeft, y = menuTop)
                    .width(menuWidth)
                    .graphicsLayer {
                        scaleX = menuScale
                        scaleY = menuScale
                        alpha = menuAlpha
                    }
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

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))

                    ContextMenuItem(
                        icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        title = "Add to Playlist",
                        onClick = {
                            context.performHapticClick()
                            // FIXME: Hook up to playlist dialog
                            onDismiss()
                        }
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))

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
