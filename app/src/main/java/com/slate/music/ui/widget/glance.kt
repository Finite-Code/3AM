package com.slate.music.ui.widget

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
import androidx.glance.action.actionStartActivity
import com.slate.music.MainActivity

// Glance Widget & Responsive Size - Pass 3

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
                        .background(Color(0xFF000000))
                        .cornerRadius(24.dp)
                        .padding(12.dp)
                ) {
                   if(ampState.currentSong == null){
                       EmptyWidget() // New empty widget when there's no music
                   } else {
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
}


// Receiver for auto updates

class GlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = GlanceWidget()
}

// Helper Components

@Composable
fun AlbumArtImage(uriString: String?, modifier: GlanceModifier = GlanceModifier) {
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
fun WidgetProgressBar(progressFraction: Float, modifier: GlanceModifier = GlanceModifier) {
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


@Composable
private fun EmptyWidget() {
    Column(
        modifier = GlanceModifier.fillMaxSize().clickable(actionStartActivity<MainActivity>()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(android.R.drawable.ic_menu_search),
            contentDescription = "Choose a Song",
            modifier = GlanceModifier.size(36.dp)
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        Text(
            text = "Tap to pick a song",
            style = TextStyle(
                color = ColorProvider(day = Color.White, night = Color.White), // both white ig
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

/**
 * ## NOTE
 * A proposed volume thingy, but now I think maybe, it's useless. Also broken code below
 */

/*
class VolumeUpAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_RAISE,
            AudioManager.FLAG_SHOW_UI
        )
    }
}

class VolumeDownAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioManager.adjustStreamVolume(
            AudioManager.STREAM_MUSIC,
            AudioManager.ADJUST_LOWER,
            AudioManager.FLAG_SHOW_UI
        )
    }
}

@Composable
fun WidgetVolumeControls(modifier: GlanceModifier = GlanceModifier) {
    Row(
        modifier = modifier
            .background(Color(0xFF222222))
            .cornerRadius(24.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier.size(32.dp).background(Color(0xFF333333))
                .cornerRadius(16.dp).clickable(actionRunCallback<VolumeDownAction>()),
            contentAlignment = Alignment.Center
        ) {
            Text("-", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp))
        }

        Spacer(modifier = GlanceModifier.width(16.dp))

        Text(
            text = "VOL",
            style = TextStyle(
                color = ColorProvider(Color(0xFFA0A0A0)),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = GlanceModifier.width(16.dp))

        Box(
            modifier = GlanceModifier.size(32.dp).background(Color(0xFF333333))
                .cornerRadius(16.dp).clickable(actionRunCallback<VolumeUpAction>()),
            contentAlignment = Alignment.Center
        ) {
            Text("+", style = TextStyle(color = ColorProvider(Color.White), fontSize = 18.sp))
        }
    }
}

*/