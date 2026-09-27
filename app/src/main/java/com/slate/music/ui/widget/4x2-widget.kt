package com.slate.music.ui.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.slate.music.R
import com.slate.music.amp.AmpState

/**
 * 4x2 Widget -  Horizontal Media Bar
 */
@Composable
fun Medium4x2Widget(state: AmpState, isLiked: Boolean) {
    val song = state.currentSong
    val progressFraction = if (state.durationMs > 0) {
        (state.progressMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .cornerRadius(20.dp)
            .padding(10.dp)
    ) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album Art
            Box(
                modifier = GlanceModifier
                    .size(54.dp)
                    .background(Color(0xFF282828))
                    .cornerRadius(12.dp)
                    .clickable(actionRunCallback<AlbumDTAction>())
            ) {
                AlbumArtImage(
                    uriString = song?.albumArtUri,
                    modifier = GlanceModifier.fillMaxSize()
                )
            }

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Title & Artist
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = song?.title?.lowercase() ?: "3am music",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF)),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = song?.artist ?: "select a song to start",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFCCCCCC), night = Color(0xFFCCCCCC)),
                        fontSize = 12.sp
                    ),
                    maxLines = 1
                )
            }

            // Controls
            Row(verticalAlignment = Alignment.CenterVertically) {

                Image(
                    provider = ImageProvider(R.drawable.ic_skip_previous),
                    contentDescription = "Previous",
                    modifier = GlanceModifier
                        .size(28.dp)
                        .clickable(actionRunCallback<SkipPreviousAction>())
                )

                Spacer(modifier = GlanceModifier.width(10.dp))

                Image(
                    provider = ImageProvider(
                        if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                    ),
                    contentDescription = "Play/Pause",
                    modifier = GlanceModifier
                        .size(36.dp)
                        .clickable(actionRunCallback<TogglePlayPauseAction>())
                )

                Spacer(modifier = GlanceModifier.width(10.dp))

                Image(
                    provider = ImageProvider(R.drawable.ic_skip_next),
                    contentDescription = "Next",
                    modifier = GlanceModifier
                        .size(28.dp)
                        .clickable(actionRunCallback<SkipNextAction>())
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        WidgetProgressBar(
            progressFraction = progressFraction,
            modifier = GlanceModifier.fillMaxWidth().height(4.dp)
        )
    }
}