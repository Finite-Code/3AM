package com.slate.music.amp

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.slate.music.Heart.HeartSong
import com.slate.music.ListeningStatsManager
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
    val currentIndex: Int = -1,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleModeEnabled: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val volume: Float = 1.0f
)

object AmpEngine {

    private var controller: MediaController? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(AmpState())
    val state: StateFlow<AmpState> = _state.asStateFlow()

    private var playlistSongs: List<HeartSong> = emptyList()

    private var appContext: Context? = null

    fun initialize(context: Context) {
        if (controller != null) return
        appContext = context.applicationContext

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

    fun toggleRepeatMode() {
        val ctrl = controller ?: return
        val nextMode = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        ctrl.repeatMode = nextMode
        _state.value = _state.value.copy(repeatMode = nextMode)
    }

    fun toggleShuffleMode() {
        val ctrl = controller ?: return
        val nextShuffle = !ctrl.shuffleModeEnabled
        ctrl.shuffleModeEnabled = nextShuffle
        _state.value = _state.value.copy(shuffleModeEnabled = nextShuffle)
    }

    fun setPlaybackSpeed(speed: Float) {
        val ctrl = controller ?: return
        val validSpeed = speed.coerceIn(0.25f, 2.0f)
        ctrl.setPlaybackSpeed(validSpeed)
        _state.value = _state.value.copy(playbackSpeed = validSpeed)
    }

    fun setVolume(volume: Float) {
        val ctrl = controller ?: return
        val validVolume = volume.coerceIn(0.0f, 1.0f)
        ctrl.volume = validVolume
        _state.value = _state.value.copy(volume = validVolume)
    }

    fun addToQueue(song: HeartSong) {
        val ctrl = controller ?: return
        playlistSongs = playlistSongs + song
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .build()
        ctrl.addMediaItem(mediaItem)
    }

    fun playNext(song: HeartSong) {
        val ctrl = controller ?: return
        val nextIndex = (ctrl.currentMediaItemIndex + 1).coerceAtMost(playlistSongs.size)
        playlistSongs = playlistSongs.toMutableList().apply { add(nextIndex, song) }
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .build()
        ctrl.addMediaItem(nextIndex, mediaItem)
    }

    fun removeQueueItem(index: Int) {
        val ctrl = controller ?: return
        if (index in playlistSongs.indices) {
            playlistSongs = playlistSongs.toMutableList().apply { removeAt(index) }
            ctrl.removeMediaItem(index)
        }
    }

    fun clearQueue() {
        val ctrl = controller ?: return
        ctrl.clearMediaItems()
        playlistSongs = emptyList()
        _state.value = AmpState()
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
        val previousSong = _state.value.currentSong
        val previousProgress = _state.value.progressMs

        // fixup!: When active track changes, log the previous track's actual listened duration
        if (previousSong != null && previousSong.id.toString() != currentMediaId && previousProgress > 3000L) {
            appContext?.let { ctx ->
                ListeningStatsManager.recordTrackPlay(ctx, previousSong, actualPlayedMs = previousProgress)
            }
        }

        val currentSong = playlistSongs.find { it.id.toString() == currentMediaId }

        _state.value = AmpState(
            currentSong = currentSong,
            currentIndex = player.currentMediaItemIndex,
            isPlaying = player.isPlaying,
            isBuffering = player.playbackState == Player.STATE_BUFFERING,
            progressMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L),
            repeatMode = player.repeatMode,
            shuffleModeEnabled = player.shuffleModeEnabled,
            playbackSpeed = player.playbackParameters.speed,
            volume = player.volume
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
