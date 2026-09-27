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
 * A comprehensive 4x4 Dashboard Widget containing full configuration controls.
 *
 * # Experimental Feature:
 * > **Night Mode Indicator:** Under active evaluation. Might require layout optimization
 *   for smaller mobile viewports.
 *
 * TODO: (Experimental) Finalize Night Mode Indicator design before final 3AM push.
 */
@Composable
fun Large4x4DashboardWidget(state: AmpState, isLiked: Boolean) {
    val song = state.currentSong
    val curSecs = state.progressMs / 1000
    val durSecs = state.durationMs / 1000
    val currentPosText = String.format("%d:%02d", curSecs / 60, curSecs % 60)
    val durationText = String.format("%d:%02d", durSecs / 60, durSecs % 60)

    val progressFraction = if (state.durationMs > 0) {
        (state.progressMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .cornerRadius(24.dp)
            .padding(16.dp)
    ) {

        // Large Album Art Box
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(130.dp)
                .background(Color(0xFF222222))
                .cornerRadius(18.dp),
            contentAlignment = Alignment.Center
        ) {
            AlbumArtImage(
                uriString = song?.albumArtUri,
                modifier = GlanceModifier.fillMaxSize()
            )
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = song?.title?.lowercase() ?: "no track playing",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFFFFFFFF)),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = song?.artist ?: "open 3AM to play music",
                    style = TextStyle(
                        color = ColorProvider(day = Color(0xFF888888), night = Color(0xFF888888)),
                        fontSize = 13.sp
                    ),
                    maxLines = 1
                )
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            Image(
                provider = ImageProvider(
                    if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart
                ),
                contentDescription = "Favorite",
                modifier = GlanceModifier
                    .size(24.dp)
                    .clickable(actionRunCallback<ToggleFavoriteAction>())
            )
        }

        Spacer(modifier = GlanceModifier.height(10.dp))

        // Progress Bar
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            WidgetProgressBar(
                progressFraction = progressFraction,
                modifier = GlanceModifier.fillMaxWidth().height(4.dp)
            )

            Spacer(modifier = GlanceModifier.height(4.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth()
            ) {
                Text(
                    text = currentPosText,
                    style = TextStyle(color = ColorProvider(day = Color(0xFFA0A0A0), night = Color(0xFFA0A0A0)), fontSize = 10.sp)
                )
                Spacer(modifier = GlanceModifier.defaultWeight())
                Text(
                    text = durationText,
                    style = TextStyle(color = ColorProvider(day = Color(0xFFA0A0A0), night = Color(0xFFA0A0A0)), fontSize = 10.sp)
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(12.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(android.R.drawable.ic_menu_rotate),
                contentDescription = "Shuffle",
                modifier = GlanceModifier
                    .size(22.dp)
                    .clickable(actionRunCallback<ToggleShuffleAction>())
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Image(
                provider = ImageProvider(R.drawable.ic_skip_previous),
                contentDescription = "Previous",
                modifier = GlanceModifier
                    .size(28.dp)
                    .clickable(actionRunCallback<SkipPreviousAction>())
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Box(
                modifier = GlanceModifier
                    .size(48.dp)
                    .background(Color(0xFFFFFFFF))
                    .cornerRadius(24.dp)
                    .clickable(actionRunCallback<TogglePlayPauseAction>()),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(
                        if (state.isPlaying) R.drawable.ic_pause_black else R.drawable.ic_play_black
                    ),
                    contentDescription = "Play/Pause",
                    modifier = GlanceModifier.size(28.dp)
                )
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            Image(
                provider = ImageProvider(R.drawable.ic_skip_next),
                contentDescription = "Next",
                modifier = GlanceModifier
                    .size(28.dp)
                    .clickable(actionRunCallback<SkipNextAction>())
            )

            Spacer(modifier = GlanceModifier.defaultWeight())

            Image(
                provider = ImageProvider(android.R.drawable.ic_menu_revert),
                contentDescription = "Repeat",
                modifier = GlanceModifier
                    .size(22.dp)
                    .clickable(actionRunCallback<ToggleRepeatAction>())
            )
        }
    }
}