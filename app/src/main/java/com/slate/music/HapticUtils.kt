package com.slate.music

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext

@Composable
fun DeadEndHapticHandler(scrollState: ScrollableState) {
    val context = LocalContext.current
    LaunchedEffect(scrollState) {
        var wasCanScrollForward = scrollState.canScrollForward
        snapshotFlow { Pair(scrollState.canScrollForward, scrollState.isScrollInProgress) }
            .collect { (canScrollForward, isScrollInProgress) ->
                if (wasCanScrollForward && !canScrollForward && isScrollInProgress) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator.vibrate(
                        VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                    )
                }
                wasCanScrollForward = canScrollForward
            }
    }
}
