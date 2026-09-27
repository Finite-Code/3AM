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
 * 2x2 Widget (Album Art, Metadata & Controls)
 */
@Composable
fun Square2x2Widget(state: AmpState, isLiked: Boolean) {
    val song = state.currentSong

    Column(
        modifier = GlanceModifier.fillMaxSize()
    ) {
        // Thumbnail Box
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color(0xFF242424))
                .cornerRadius(16.dp)
                .clickable(actionRunCallback<AlbumDTAction>()),
            contentAlignment = Alignment.Center
        ) {
            AlbumArtImage(
                uriString = song?.albumArtUri,
                modifier = GlanceModifier.fillMaxSize()
            )

            if (isLiked) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_heart_filled),
                        contentDescription = "Liked",
                        modifier = GlanceModifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Title & Artist
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = song?.title?.lowercase() ?: "3am music",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Text(
                text = song?.artist ?: "no track playing",
                style = TextStyle(
                    color = ColorProvider(day = Color(0xFFA0A0A0), night = Color(0xFFA0A0A0)),
                    fontSize = 11.sp
                ),
                maxLines = 1
            )
        }

        Spacer(modifier = GlanceModifier.height(6.dp))

        // Playback Controls Row
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_skip_previous),
                contentDescription = "Previous",
                modifier = GlanceModifier
                    .size(28.dp)
                    .clickable(actionRunCallback<SkipPreviousAction>())
            )

            Spacer(modifier = GlanceModifier.width(16.dp))

            Image(
                provider = ImageProvider(
                    if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                ),
                contentDescription = "Play/Pause",
                modifier = GlanceModifier
                    .size(32.dp)
                    .clickable(actionRunCallback<TogglePlayPauseAction>())
            )

            Spacer(modifier = GlanceModifier.width(16.dp))

            Image(
                provider = ImageProvider(R.drawable.ic_skip_next),
                contentDescription = "Next",
                modifier = GlanceModifier
                    .size(28.dp)
                    .clickable(actionRunCallback<SkipNextAction>())
            )
        }
    }
}