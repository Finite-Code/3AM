@file:OptIn(ExperimentalMaterial3Api::class)

package com.slate.music.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.TimerOff
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
import com.slate.music.amp.AmpEngine
import com.slate.music.util.performHapticClick
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

// SLEEP TIMER

object SleepTimerEngine {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null
    
    private val _timeLeftMs = MutableStateFlow(0L)
    val timeLeftMs = _timeLeftMs.asStateFlow()

    private val _isTimerActive = MutableStateFlow(false)
    val isTimerActive = _isTimerActive.asStateFlow()

    private val _stopAtEndOfTrack = MutableStateFlow(false)
    val stopAtEndOfTrack = _stopAtEndOfTrack.asStateFlow()

    // Configuration
    private const val FADE_DURATION_MS = 10000L // this is 10 seconds
    private const val FADE_INTERVAL_MS = 100L

    fun startTimer(minutes: Int) {
        cancelTimer()
        _isTimerActive.value = true
        _stopAtEndOfTrack.value = false
        val totalMs = minutes * 60 * 1000L
        _timeLeftMs.value = totalMs

        timerJob = scope.launch {
            val endTime = System.currentTimeMillis() + totalMs
            var remaining = totalMs

            while (remaining > 0 && isActive) {
                delay(1000)
                remaining = endTime - System.currentTimeMillis()
                _timeLeftMs.value = remaining.coerceAtLeast(0L)

                if (remaining in 1L..FADE_DURATION_MS) {
                    val volumeFraction = remaining.toFloat() / FADE_DURATION_MS
                    AmpEngine.setVolume(volumeFraction)
                }
            }

            if (isActive) executeStop()
        }
    }

    fun stopAfterCurrentTrack() {
        cancelTimer()
        _isTimerActive.value = true
        _stopAtEndOfTrack.value = true
        
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                val state = AmpEngine.state.value
                val remainingTrackMs = state.durationMs - state.progressMs
                
                _timeLeftMs.value = remainingTrackMs.coerceAtLeast(0L)

                if (state.durationMs > 0 && remainingTrackMs < 2000L) {
                    executeStop()
                    break
                }
            }
        }
    }

    fun startTimerAtExactTime(hour: Int, minute: Int) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        val diffMs = target.timeInMillis - now.timeInMillis
        val diffMinutes = (diffMs / 1000 / 60).toInt()
        startTimer(diffMinutes)
    }

    fun cancelTimer() {
        timerJob?.cancel()
        _isTimerActive.value = false
        _stopAtEndOfTrack.value = false
        _timeLeftMs.value = 0L
        AmpEngine.setVolume(1.0f) // Reset volume if we cancel while a fade is in progress
    }

    private fun executeStop() {
        AmpEngine.togglePlayPause() // Assuming it's playing, this will pause it
        AmpEngine.setVolume(1.0f)   // Reset volume for the next time
        cancelTimer()
    }
}


// BOTTOM SHEET

@Composable
fun SleepTimerSheet(
    isVisible: Boolean,
    onClose: () -> Unit,
    hazeState: HazeState
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    
    val isActive by SleepTimerEngine.isTimerActive.collectAsState()
    val stopAtEnd by SleepTimerEngine.stopAtEndOfTrack.collectAsState()
    val timeLeft by SleepTimerEngine.timeLeftMs.collectAsState()

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onClose,
            sheetState = sheetState,
            containerColor = Color(0xFF121212),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.DarkGray) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                
                // Header
                Icon(
                    imageVector = Icons.Rounded.Bedtime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Sleep Timer",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Considering: Countdown
                val progressAlpha by animateFloatAsState(if (isActive) 1f else 0f, tween(300))
                
                if (progressAlpha > 0f) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (stopAtEnd) "Stopping after this track" else formatTime(timeLeft),
                        color = Color(0xFFEFB4E0),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimerOptionCard(title = "15m", onClick = { context.performHapticClick(); SleepTimerEngine.startTimer(15); onClose() }, modifier = Modifier.weight(1f))
                    TimerOptionCard(title = "30m", onClick = { context.performHapticClick(); SleepTimerEngine.startTimer(30); onClose() }, modifier = Modifier.weight(1f))
                    TimerOptionCard(title = "60m", onClick = { context.performHapticClick(); SleepTimerEngine.startTimer(60); onClose() }, modifier = Modifier.weight(1f))
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (stopAtEnd) Color(0xFFEFB4E0).copy(alpha = 0.2f) else Color(0xFF1E1E1E),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { 
                            context.performHapticClick()
                            SleepTimerEngine.stopAfterCurrentTrack()
                            onClose()
                        }
                ) {
                    Text(
                        text = "End of track",
                        color = if (stopAtEnd) Color(0xFFEFB4E0) else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                // and finally, Cancel xD
                if (isActive) {
                    Spacer(modifier = Modifier.height(24.dp))
                    TextButton(
                        onClick = { 
                            context.performHapticClick()
                            SleepTimerEngine.cancelTimer() 
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Rounded.TimerOff, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cancel Timer", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimerOptionCard(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E1E1E),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 18.dp)) {
            Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("Stopping in %02d:%02d", minutes, seconds)
}
