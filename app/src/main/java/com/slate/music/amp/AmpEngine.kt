package com.slate.music.amp

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.slate.music.Heart.HeartSong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class AmpState(
    val currentSong: HeartSong? = null,
    val isPlaying: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L
)

object AmpEngine {

    private var controller: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(AmpState())
    val state: StateFlow<AmpState> = _state.asStateFlow()

    private var playlistSongs: List<HeartSong> = emptyList()

    fun initialize(context: Context) {
        if (controller != null) return

        val sessionToken = SessionToken(
            context.applicationContext,
            ComponentName(context.applicationContext, AmpService::class.java)
        )

        val controllerFuture = MediaController.Builder(context.applicationContext, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                setupPlayerListener()
                startProgressTracker()
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playPlaylist(songs: List<HeartSong>, startPosition: Int = 0) {
        val ctrl = controller ?: return
        playlistSongs = songs

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .build()
        }

        ctrl.setMediaItems(mediaItems, startPosition, 0L)
        ctrl.prepare()
        ctrl.play()
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
    }

    fun skipNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    private fun setupPlayerListener() {
        controller?.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                updateState(player)
            }
        })
        controller?.let { updateState(it) }
    }

    private fun updateState(player: Player) {
        val currentMediaId = player.currentMediaItem?.mediaId
        val currentSong = playlistSongs.find { it.id.toString() == currentMediaId }

        _state.value = AmpState(
            currentSong = currentSong,
            isPlaying = player.isPlaying,
            progressMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L)
        )
    }

    private fun startProgressTracker() {
        scope.launch {
            while (isActive) {
                controller?.let { ctrl ->
                    if (ctrl.isPlaying) {
                        _state.value = _state.value.copy(
                            progressMs = ctrl.currentPosition.coerceAtLeast(0L),
                            durationMs = ctrl.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500)
            }
        }
    }
}
