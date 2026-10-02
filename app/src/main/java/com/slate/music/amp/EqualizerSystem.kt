@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.slate.music.amp

import android.media.audiofx.Equalizer
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.util.performHapticClick
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.roundToInt

private const val TAG = "EqEngine"
private val ThumbSize = 24.dp

object ThemeColors {
    val SurfaceDark = Color(0xFF141414)
    val SurfaceDarker = Color(0xFF1E1E1E)
    val Background = Color(0xFF0A0A0A)
    val ActivePink = Color(0xFFEFB4E0)
}

data class EqBand(
    val index: Short,
    val centerFreqHz: Int,
    val levelMb: Short,
    val minLevelMb: Short,
    val maxLevelMb: Short
)

data class EqPreset(val id: Short, val name: String)

object EqualizerEngine {
    private var eq: Equalizer? = null

    private val _isEnabled = MutableStateFlow(false)
    val isEnabled = _isEnabled.asStateFlow()

    private val _bands = MutableStateFlow<List<EqBand>>(emptyList())
    val bands = _bands.asStateFlow()

    private val _presets = MutableStateFlow<List<EqPreset>>(emptyList())
    val presets = _presets.asStateFlow()

    fun attachToAudioSession(sessionId: Int) {
        eq?.release()
        eq = try {
            Equalizer(0, sessionId).also { it.enabled = _isEnabled.value }
        } catch (e: Exception) {
            // UnsupportedOperationException / RuntimeException on devices w/o a usable effect
            Log.w(TAG, "Equalizer unavailable for session $sessionId :/", e)
            null
        }
        refreshBands()
        refreshPresets()
    }

    fun toggleEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        eq?.enabled = enabled
    }

    fun setBandLevel(bandIndex: Short, levelMb: Short) {
        eq?.setBandLevel(bandIndex, levelMb)
        _bands.value = _bands.value.map {
            if (it.index == bandIndex) it.copy(levelMb = levelMb) else it
        }
    }

    fun applyPreset(presetId: Short) {
        eq?.usePreset(presetId)
        refreshBands()
    }

    fun resetToFlat() {
        val e = eq ?: return
        for (i in 0 until e.numberOfBands) e.setBandLevel(i.toShort(), 0)
        refreshBands()
    }

    fun release() {
        eq?.release()
        eq = null
        _bands.value = emptyList()
        _presets.value = emptyList()
    }

    private fun refreshBands() {
        val e = eq
        if (e == null) {
            _bands.value = emptyList()
            return
        }
        val (lo, hi) = e.bandLevelRange.let { it[0] to it[1] }
        _bands.value = (0 until e.numberOfBands).map { i ->
            val idx = i.toShort()
            EqBand(idx, e.getCenterFreq(idx) / 1000, e.getBandLevel(idx), lo, hi)
        }
    }

    private fun refreshPresets() {
        val e = eq
        if (e == null) {
            _presets.value = emptyList()
            return
        }
        _presets.value = (0 until e.numberOfPresets).map {
            EqPreset(it.toShort(), e.getPresetName(it.toShort()))
        }
    }
}

// cleaned - final1

@Composable
fun EqualizerScreen(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState
) {
    if (!isVisible) return

    val enabled by EqualizerEngine.isEnabled.collectAsState()
    val bands by EqualizerEngine.bands.collectAsState()
    val presets by EqualizerEngine.presets.collectAsState()
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = ThemeColors.Background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Equalizer",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 32.sp,
                        letterSpacing = (-1).sp
                    ),
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        context.performHapticClick()
                        EqualizerEngine.resetToFlat()
                    }) {
                        Icon(Icons.Rounded.Refresh, "Reset to flat", tint = Color.LightGray)
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = CircleShape,
                        color = ThemeColors.SurfaceDarker,
                        modifier = Modifier.size(40.dp)
                    ) {
                        IconButton(onClick = {
                            context.performHapticClick()
                            onClose()
                        }) {
                            Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                        }
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = ThemeColors.SurfaceDarker,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Master EQ", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(if (enabled) "Active" else "Bypassed", color = Color.Gray, fontSize = 14.sp)
                    }
                    Switch(
                        checked = enabled,
                        onCheckedChange = {
                            context.performHapticClick()
                            EqualizerEngine.toggleEnabled(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = Color.White,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2C2C2C)
                        )
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            val sliderHeight = 250.dp
            if (bands.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(sliderHeight + 40.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    EqCurve(bands, Modifier.fillMaxWidth().height(sliderHeight))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        bands.forEach { band ->
                            EqBandSlider(
                                band = band,
                                height = sliderHeight,
                                enabled = enabled,
                                onLevelChange = { EqualizerEngine.setBandLevel(band.index, it) }
                            )
                        }
                    }
                }
            } else {
                Box(Modifier.fillMaxWidth().height(sliderHeight), contentAlignment = Alignment.Center) {
                    Text("Equalizer not supported on this device :/", color = Color.Gray)
                }
            }

            Spacer(Modifier.weight(1f))

            if (presets.isNotEmpty()) {
                Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text(
                        "Presets",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 24.dp, bottom = 16.dp)
                    )
                    FlowRow(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ThemeColors.SurfaceDarker,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(enabled = enabled) {
                                        context.performHapticClick()
                                        EqualizerEngine.applyPreset(preset.id)
                                    }
                            ) {
                                Text(
                                    preset.name,
                                    color = if (enabled) Color.White else Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun EqBand.normalized(): Float {
    val range = (maxLevelMb - minLevelMb).toFloat()
    return if (range > 0f) (levelMb - minLevelMb) / range else 0.5f
}

@Composable
private fun EqBandSlider(
    band: EqBand,
    height: Dp,
    enabled: Boolean,
    onLevelChange: (Short) -> Unit
) {
    val level by animateFloatAsState(band.normalized(), tween(150), label = "bandLevel")

    // read through these inside the gesture block so a darg doesn't restart on every update
    val currentBand by rememberUpdatedState(band)
    val currentOnChange by rememberUpdatedState(onLevelChange)

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
        val hz = band.centerFreqHz
        Text(
            if (hz >= 1000) "${hz / 1000}k" else "$hz",
            color = if (enabled) Color.Gray else Color.DarkGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(height)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    val travel = size.height - ThumbSize.toPx()
                    detectVerticalDragGestures { change, dy ->
                        change.consume()
                        val b = currentBand
                        val range = (b.maxLevelMb - b.minLevelMb).toFloat()
                        val next = (b.normalized() - dy / travel).coerceIn(0f, 1f)
                        currentOnChange((b.minLevelMb + next * range).roundToInt().toShort())
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // track
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
            )
            // 0 dB tik
            Box(
                Modifier
                    .width(12.dp)
                    .height(2.dp)
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.3f))
            )
            // offset instead of padding: padding can't go negative
            Box(
                Modifier
                    .offset(y = -((height - ThumbSize) * level))
                    .size(ThumbSize)
                    .clip(CircleShape)
                    .background(if (enabled) Color.White else Color.DarkGray)
            )
        }

        val db = band.levelMb / 100f
        Text(
            if (abs(db) < 0.05f) "0.0" else "%+.1f".format(db),
            color = if (enabled) Color.White else Color.DarkGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun EqCurve(bands: List<EqBand>, modifier: Modifier = Modifier) {
    val color = ThemeColors.ActivePink.copy(alpha = 0.4f)
    val levels = bands.map { animateFloatAsState(it.normalized(), tween(150), label = "curve").value }

    Canvas(modifier.padding(vertical = 12.dp)) {
        if (levels.size < 2) return@Canvas

        val step = size.width / levels.size
        val pts = levels.mapIndexed { i, l -> Offset(step / 2 + i * step, size.height * (1f - l)) }

        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 0 until pts.lastIndex) {
                val midX = (pts[i].x + pts[i + 1].x) / 2
                cubicTo(midX, pts[i].y, midX, pts[i + 1].y, pts[i + 1].x, pts[i + 1].y)
            }
        }
        drawPath(path, color, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
