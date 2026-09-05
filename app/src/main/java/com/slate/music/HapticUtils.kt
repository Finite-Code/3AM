package com.slate.music

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext

/**
 * Triggers a predefined haptic effect on Android 13+ devices.
 */
fun Context.performHaptic(effectId: Int = VibrationEffect.EFFECT_TICK) {
    try {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator.vibrate(VibrationEffect.createPredefined(effectId))
    } catch (_: Exception) {
        // Ignore haptic failures
    }
}

fun Context.performHapticTick() = performHaptic(VibrationEffect.EFFECT_TICK)
fun Context.performHapticClick() = performHaptic(VibrationEffect.EFFECT_CLICK)

@Composable
fun DeadEndHapticHandler(scrollState: ScrollableState) {
    val context = LocalContext.current
    LaunchedEffect(scrollState) {
        var wasCanScrollForward = scrollState.canScrollForward
        snapshotFlow { Pair(scrollState.canScrollForward, scrollState.isScrollInProgress) }
            .collect { (canScrollForward, isScrollInProgress) ->
                if (wasCanScrollForward && !canScrollForward && isScrollInProgress) {
                    context.performHapticTick()
                }
                wasCanScrollForward = canScrollForward
            }
    }
}
