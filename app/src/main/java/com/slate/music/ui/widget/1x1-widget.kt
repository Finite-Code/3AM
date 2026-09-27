package com.slate.music.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import com.slate.music.R
import com.slate.music.amp.AmpState

/**
 * 1x1 Controller (Play/Pause Toggle Button)
 */
@Composable
fun Compact1x1Widget(state: AmpState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .cornerRadius(18.dp)
            .clickable(actionRunCallback<TogglePlayPauseAction>())
    ) {
        Image(
            provider = ImageProvider(
                if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            ),
            contentDescription = if (state.isPlaying) "Pause" else "Play",
            modifier = GlanceModifier.size(32.dp)
        )
    }
}