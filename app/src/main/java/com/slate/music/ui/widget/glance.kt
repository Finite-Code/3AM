package com.slate.music.ui.widget

import com.slate.music.ui.widget.GlanceWidget
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.slate.music.AppSettings
import com.slate.music.R
import com.slate.music.amp.AmpEngine
import com.slate.music.amp.AmpState
import androidx.glance.appwidget.GlanceAppWidgetManager

// Glance Wigdet & Responsive Size - Pass 3

class GlanceWidget : GlanceAppWidget() {

    companion object {
        // Defined Widget Size Thresholds
        private val SIZE_COMPACT_1X1 = DpSize(60.dp, 60.dp)
        private val SIZE_SQUARE_2X2 = DpSize(140.dp, 140.dp)
        private val SIZE_MEDIUM_4X2 = DpSize(260.dp, 110.dp)
        private val SIZE_LARGE_4X4 = DpSize(260.dp, 260.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            SIZE_COMPACT_1X1,
            SIZE_SQUARE_2X2,
            SIZE_MEDIUM_4X2,
            SIZE_LARGE_4X4
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val ampState by AmpEngine.state.collectAsState()
                val favorites by AppSettings.favoriteTrackIds.collectAsState()
                val size = LocalSize.current

                val isLiked = ampState.currentSong?.id?.toString()?.let { it in favorites } ?: false

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(Color(0xFF121212))
                        .cornerRadius(24.dp)
                        .padding(12.dp)
                ) {
                    when {
                        size.width < 120.dp && size.height < 120.dp -> {
                            Compact1x1Widget(ampState)
                        }
                        size.height < 150.dp -> {
                            Medium4x2Widget(ampState, isLiked)
                        }
                        size.width < 200.dp -> {
                            Square2x2Widget(ampState, isLiked)
                        }
                        else -> {
                            Large4x4DashboardWidget(ampState, isLiked)
                        }
                    }
                }
            }
        }
    }
}


// Receiver for auto updates

class GlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlanceWidget()
}

// Size Wise Composables

/**
 * 1x1 Controller (Play/Pause Toggle Button)
 */
@Composable
private fun Compact1x1Widget(state: AmpState) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
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

/**
 * 2x2 Widget (Album Art, Metadata & Controls)
 */
@Composable
private fun Square2x2Widget(state: AmpState, isLiked: Boolean) {
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

/**
 * 4x2 Widget -  Horizontal Media Bar
 */
@Composable
private fun Medium4x2Widget(state: AmpState, isLiked: Boolean) {
    val song = state.currentSong
    val progressFraction = if (state.durationMs > 0) {
        (state.progressMs.toFloat() / state.durationMs).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFF181818))
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
private fun Large4x4DashboardWidget(state: AmpState, isLiked: Boolean) {
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
            .background(Color(0xFF141414))
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
                    text = song?.artist ?: "open 3am to play music",
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

// Helper Components

@Composable
private fun AlbumArtImage(uriString: String?, modifier: GlanceModifier = GlanceModifier) {
    val context = LocalContext.current
    val bitmap: Bitmap? = uriString?.let { uri ->
        try {
            val contentUri = Uri.parse(uri)
            context.contentResolver.openInputStream(contentUri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    if (bitmap != null) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = "Album Art",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(android.R.drawable.ic_menu_gallery),
                contentDescription = "Default Music Note",
                modifier = GlanceModifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun WidgetProgressBar(progressFraction: Float, modifier: GlanceModifier = GlanceModifier) {
    LinearProgressIndicator(
        progress = progressFraction.coerceIn(0f, 1f),
        modifier = modifier.fillMaxWidth().height(2.dp),
        color = ColorProvider(day = Color.White, night = Color.White),
        backgroundColor = ColorProvider(day = Color.White.copy(alpha = 0.2f), night = Color.White.copy(alpha = 0.2f))
    )
}

// Action Callbacks

class TogglePlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AmpEngine.togglePlayPause()
        GlanceWidget().update(context, glanceId)
    }
}

class SkipNextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AmpEngine.skipNext()
        GlanceWidget().update(context, glanceId)
    }
}

class SkipPreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AmpEngine.skipPrevious()
        GlanceWidget().update(context, glanceId)
    }
}

class ToggleFavoriteAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AmpEngine.state.value.currentSong?.id?.toString()?.let { trackId ->
            AppSettings.toggleFavorite(context, trackId)
        }
        GlanceWidget().update(context, glanceId)
    }
}

class ToggleShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AmpEngine.toggleShuffleMode()
        GlanceWidget().update(context, glanceId)
    }
}

class ToggleRepeatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        AmpEngine.toggleRepeatMode()
        GlanceWidget().update(context, glanceId)
    }
}

suspend fun updateGlanceWidgets(context: Context){
    try{
        val manager = GlanceAppWidgetManager(context)
        val glanceIds = manager.getGlanceIds(GlanceWidget::class.java)
        if(glanceIds.isNotEmpty()){
            val widget = GlanceWidget()
            for(glanceId in glanceIds){
                widget.update(context, glanceId)
            }
        }
    } catch(e: Exception){
        // just do it. (checkmark)
    }
}

class AlbumDTAction: ActionCallback{
    companion object{
        private var lastClickTime = 0L
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val currentTime = System.currentTimeMillis()

        if(currentTime - lastClickTime > 400){
            AmpEngine.state.value.currentSong?.id?.toString()?.let{ trackId ->
                AppSettings.toggleFavorite(context, trackId)
            }
            GlanceWidget().update(context, glanceId)
            lastClickTime = 0L // resets the time btw
        } else{
            lastClickTime = currentTime // for first tap
        }
    }
}