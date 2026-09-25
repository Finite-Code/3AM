package com.slate.music.amp

import android.content.Context
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slate.music.AppSettings
import com.slate.music.performHapticClick
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EqualizerState(
    val isEnabled: Boolean = true,
    val numberOfBands: Short = 5,
    val minBandLevelMb: Short = -1500,
    val maxBandLevelMb: Short = 1500,
    val bandLevelsMb: Map<Short, Short> = emptyMap(),
    val centerFreqsHz: Map<Short, Int> = emptyMap(),
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val loudnessGainMb: Int = 0,
    val activePresetName: String = "Flat"
)

data class EqualizerPreset(
    val name: String,
    val bandAdjustmentsMb: Map<Short, Short>,
    val bassBoost: Short = 0,
    val virtualizer: Short = 0
)

object AmpEqualizer {

    private const val PREFS_NAME = "amp_equalizer_prefs"
    private const val KEY_ENABLED = "eq_enabled"
    private const val KEY_PRESET = "eq_preset"
    private const val KEY_BASS = "eq_bass"
    private const val KEY_VIRTUALIZER = "eq_virt"
    private const val KEY_LOUDNESS = "eq_loud"

    private var equalizerFx: Equalizer? = null
    private var bassBoostFx: BassBoost? = null
    private var virtualizerFx: Virtualizer? = null
    private var loudnessFx: LoudnessEnhancer? = null

    private var activeAudioSessionId: Int = 0

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    val builtinPresets = listOf(
        EqualizerPreset("Flat", mapOf(0.toShort() to 0, 1.toShort() to 0, 2.toShort() to 0, 3.toShort() to 0, 4.toShort() to 0), 0, 0),
        EqualizerPreset("Bass Boost", mapOf(0.toShort() to 800, 1.toShort() to 400, 2.toShort() to 0, 3.toShort() to 200, 4.toShort() to 300), 750, 200),
        EqualizerPreset("Vocal Booster", mapOf(0.toShort() to -300, 1.toShort() to 200, 2.toShort() to 700, 3.toShort() to 800, 4.toShort() to 300), 100, 150),
        EqualizerPreset("Electronic", mapOf(0.toShort() to 600, 1.toShort() to 300, 2.toShort() to 0, 3.toShort() to 400, 4.toShort() to 600), 500, 400),
        EqualizerPreset("Acoustic", mapOf(0.toShort() to 400, 1.toShort() to 200, 2.toShort() to 300, 3.toShort() to 500, 4.toShort() to 400), 200, 250),
        EqualizerPreset("Rock", mapOf(0.toShort() to 700, 1.toShort() to 300, 2.toShort() to -100, 3.toShort() to 400, 4.toShort() to 800), 600, 300)
    )

    fun initialize(context: Context, audioSessionId: Int) {
        if (audioSessionId <= 0 || audioSessionId == activeAudioSessionId) return

        release()
        activeAudioSessionId = audioSessionId

        try {
            val eq = Equalizer(0, audioSessionId)
            val bass = BassBoost(0, audioSessionId)
            val virt = Virtualizer(0, audioSessionId)
            val loud = LoudnessEnhancer(audioSessionId)

            equalizerFx = eq
            bassBoostFx = bass
            virtualizerFx = virt
            loudnessFx = loud

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val enabled = prefs.getBoolean(KEY_ENABLED, true)
            val savedPreset = prefs.getString(KEY_PRESET, "Flat") ?: "Flat"
            val savedBass = prefs.getInt(KEY_BASS, 0).toShort()
            val savedVirt = prefs.getInt(KEY_VIRTUALIZER, 0).toShort()
            val savedLoud = prefs.getInt(KEY_LOUDNESS, 0)

            eq.enabled = enabled
            bass.enabled = enabled
            virt.enabled = enabled
            loud.enabled = enabled

            val bands = eq.numberOfBands
            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]

            val bandLevels = mutableMapOf<Short, Short>()
            val centerFreqs = mutableMapOf<Short, Int>()

            for (i in 0 until bands) {
                val bandIndex = i.toShort()
                val savedLevel = prefs.getInt("band_$i", eq.getBandLevel(bandIndex).toInt()).toShort()
                eq.setBandLevel(bandIndex, savedLevel)
                bandLevels[bandIndex] = savedLevel
                centerFreqs[bandIndex] = eq.getCenterFreq(bandIndex) / 1000
            }

            if (bass.strengthSupported) bass.setStrength(savedBass)
            if (virt.strengthSupported) virt.setStrength(savedVirt)
            loud.setTargetGain(savedLoud)

            _state.value = EqualizerState(
                isEnabled = enabled,
                numberOfBands = bands,
                minBandLevelMb = minLevel,
                maxBandLevelMb = maxLevel,
                bandLevelsMb = bandLevels,
                centerFreqsHz = centerFreqs,
                bassBoostStrength = savedBass,
                virtualizerStrength = savedVirt,
                loudnessGainMb = savedLoud,
                activePresetName = savedPreset
            )
        } catch (_: Exception) {
            // Audio FX initialization fallback
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        try {
            equalizerFx?.enabled = enabled
            bassBoostFx?.enabled = enabled
            virtualizerFx?.enabled = enabled
            loudnessFx?.enabled = enabled

            _state.value = _state.value.copy(isEnabled = enabled)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_ENABLED, enabled).apply()
        } catch (_: Exception) {}
    }

    fun setBandLevel(context: Context, band: Short, levelMb: Short) {
        try {
            equalizerFx?.setBandLevel(band, levelMb)
            val updatedLevels = _state.value.bandLevelsMb.toMutableMap()
            updatedLevels[band] = levelMb

            _state.value = _state.value.copy(
                bandLevelsMb = updatedLevels,
                activePresetName = "Custom"
            )

            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit().putInt("band_$band", levelMb.toInt())
                .putString(KEY_PRESET, "Custom").apply()
        } catch (_: Exception) {}
    }

    fun applyPreset(context: Context, presetName: String) {
        val preset = builtinPresets.find { it.name == presetName } ?: return
        try {
            preset.bandAdjustmentsMb.forEach { (band, levelMb) ->
                equalizerFx?.setBandLevel(band, levelMb)
            }
            if (bassBoostFx?.strengthSupported == true) bassBoostFx?.setStrength(preset.bassBoost)
            if (virtualizerFx?.strengthSupported == true) virtualizerFx?.setStrength(preset.virtualizer)

            val updatedLevels = _state.value.bandLevelsMb.toMutableMap()
            updatedLevels.putAll(preset.bandAdjustmentsMb)

            _state.value = _state.value.copy(
                bandLevelsMb = updatedLevels,
                bassBoostStrength = preset.bassBoost,
                virtualizerStrength = preset.virtualizer,
                activePresetName = preset.name
            )

            val editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            preset.bandAdjustmentsMb.forEach { (band, level) -> editor.putInt("band_$band", level.toInt()) }
            editor.putString(KEY_PRESET, preset.name)
                .putInt(KEY_BASS, preset.bassBoost.toInt())
                .putInt(KEY_VIRTUALIZER, preset.virtualizer.toInt())
                .apply()
        } catch (_: Exception) {}
    }

    fun setBassBoost(context: Context, strength: Short) {
        try {
            if (bassBoostFx?.strengthSupported == true) {
                bassBoostFx?.setStrength(strength)
                _state.value = _state.value.copy(bassBoostStrength = strength)
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putInt(KEY_BASS, strength.toInt()).apply()
            }
        } catch (_: Exception) {}
    }

    fun setVirtualizer(context: Context, strength: Short) {
        try {
            if (virtualizerFx?.strengthSupported == true) {
                virtualizerFx?.setStrength(strength)
                _state.value = _state.value.copy(virtualizerStrength = strength)
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putInt(KEY_VIRTUALIZER, strength.toInt()).apply()
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            equalizerFx?.release()
            bassBoostFx?.release()
            virtualizerFx?.release()
            loudnessFx?.release()
        } catch (_: Exception) {}
        equalizerFx = null
        bassBoostFx = null
        virtualizerFx = null
        loudnessFx = null
        activeAudioSessionId = 0
    }
}

// Why do Kotlin audio engineers love DSP equalizers?
// Because they turn low-frequency noise into high-fidelity music!
// Edit: thought this would make the code funny. this clearly isn't working and I might need some help.

@Composable
fun EqualizerControlCard(
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val eqState by AmpEqualizer.state.collectAsState()
    val isBlurEnabled by AppSettings.isBlurEnabled.collectAsState()
    val context = LocalContext.current

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF141414),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isBlurEnabled) {
                    Modifier.hazeBlur(
                        input = HazeInput.Sources(state = hazeState),
                        style = HazeBlurStyle {
                            blurRadius(24.dp)
                            noiseFactor(0f)
                        }
                    )
                } else Modifier
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.GraphicEq,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Equalizer & DSP",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                }

                Switch(
                    checked = eqState.isEnabled,
                    onCheckedChange = {
                        context.performHapticClick()
                        AmpEqualizer.setEnabled(context, it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = Color.White,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color(0xFF2C2C2C)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Presets Selector Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(AmpEqualizer.builtinPresets) { preset ->
                    val isSelected = eqState.activePresetName == preset.name
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else Color(0xFF222222))
                            .clickable {
                                context.performHapticClick()
                                AmpEqualizer.applyPreset(context, preset.name)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = preset.name,
                            color = if (isSelected) Color.Black else Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // DSP Sliders: Bass Boost & Virtualizer
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Bass Boost", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "${(eqState.bassBoostStrength / 10)}%", color = Color.Gray, fontSize = 13.sp)
                    }

                    Slider(
                        value = eqState.bassBoostStrength.toFloat(),
                        onValueChange = { AmpEqualizer.setBassBoost(context, it.toInt().toShort()) },
                        valueRange = 0f..1000f,
                        enabled = eqState.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "3D Spatial Virtualizer", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(text = "${(eqState.virtualizerStrength / 10)}%", color = Color.Gray, fontSize = 13.sp)
                    }

                    Slider(
                        value = eqState.virtualizerStrength.toFloat(),
                        onValueChange = { AmpEqualizer.setVirtualizer(context, it.toInt().toShort()) },
                        valueRange = 0f..1000f,
                        enabled = eqState.isEnabled,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}