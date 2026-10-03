package com.slate.music.amp

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import com.slate.music.data.HeartSong
import com.slate.music.data.HeartEngine
import com.slate.music.data.ListeningStatsManager
import com.slate.music.ui.widget.updateGlanceWidgets
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
    val progressMs: Long = 0L,
    val durationMs: Long = 0L,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffleModeEnabled: Boolean = false,
)

object AmpEngine {

    private var controller: MediaController? = null
    private var isInitializing = false
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(AmpState())
    val state: StateFlow<AmpState> = _state.asStateFlow()

    private val _queue = MutableStateFlow<List<HeartSong>>(emptyList())
    val queue = _queue.asStateFlow()

    private var appContext: Context? = null

    private var lastWidgetProgressSec = -1L

    fun initialize(context: Context) {
        if (controller != null || isInitializing) return
        isInitializing = true
        appContext = context.applicationContext

        val sessionToken = SessionToken(
            context.applicationContext,
            ComponentName(context.applicationContext, AmpService::class.java)
        )

        val controllerFuture = MediaController.Builder(context.applicationContext, sessionToken).buildAsync()
        controllerFuture.addListener(
            {
                controller = controllerFuture.get()
                isInitializing = false
                setupPlayerListener()
                startProgressTracker()
            },
            MoreExecutors.directExecutor()
        )
    }

    fun playPlaylist(songs: List<HeartSong>, startPosition: Int = 0) {
        val ctrl = controller
        if(ctrl == null) return
        _queue.value = songs

        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .build()
                )
                .build()
        }

        ctrl.setMediaItems(mediaItems, startPosition, 0L)
        ctrl.prepare()
        ctrl.play()
    }

    fun togglePlayPause() {
        val ctrl = controller
        if(ctrl == null) return
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
        val ctrl = controller
        if(ctrl == null) return
        val nextMode = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        ctrl.repeatMode = nextMode
        _state.value = _state.value.copy(repeatMode = nextMode)
    }

    fun toggleShuffleMode() {
        val ctrl = controller
        if(ctrl == null) return
        val nextShuffle = !ctrl.shuffleModeEnabled
        ctrl.shuffleModeEnabled = nextShuffle
        _state.value = _state.value.copy(shuffleModeEnabled = nextShuffle)
    }

        val ctrl = controller
        if(ctrl == null) return
        val validSpeed = speed.coerceIn(0.25f, 2.0f)
        ctrl.setPlaybackSpeed(validSpeed)
        _state.value = _state.value.copy(playbackSpeed = validSpeed)
    }

        val ctrl = controller
        if(ctrl == null) return
        val validVolume = volume.coerceIn(0.0f, 1.0f)
        ctrl.volume = validVolume
        _state.value = _state.value.copy(volume = validVolume)
    }

        val ctrl = controller
        if(ctrl == null) return
        _queue.value = _queue.value + song
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build())
            .build()
        ctrl.addMediaItem(mediaItem)
    }

    fun playNext(song: HeartSong) {
        val ctrl = controller
        if(ctrl == null) return
        val nextIndex = (ctrl.currentMediaItemIndex + 1).coerceAtMost(_queue.value.size)
        _queue.value = _queue.value.toMutableList().apply { add(nextIndex, song) }
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build())
            .build()
        ctrl.addMediaItem(nextIndex, mediaItem)
    }

    fun removeQueueItem(index: Int) {
        val ctrl = controller
        if(ctrl == null) return
        if (index in _queue.value.indices) {
            _queue.value = _queue.value.toMutableList().apply { removeAt(index) }
            ctrl.removeMediaItem(index)
        }
    }

    fun clearQueue() {
        val ctrl = controller
        if(ctrl == null) return
        ctrl.clearMediaItems()
        _queue.value = emptyList()
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

        // Reconnect session recovery
        if (_queue.value.isEmpty() && currentMediaId != null) {
            HeartEngine.songs.value.find { it.id.toString() == currentMediaId }?.let {
                _queue.value = listOf(it)
            }
        }
        val previousSong = _state.value.currentSong
        val previousProgress = _state.value.progressMs
        val previousIsPlaying = _state.value.isPlaying
        val previousMediaId = _state.value.currentSong?.id?.toString()

        // fixup!: When active track changes, log the previous track's actual listened duration
        if (previousSong != null && previousSong.id.toString() != currentMediaId && previousProgress > 3000L) {
            appContext?.let { ctx ->
                ListeningStatsManager.recordTrackPlay(ctx, previousSong, actualPlayedMs = previousProgress)
            }
        }

        val currentSong = _queue.value.getOrNull(player.currentMediaItemIndex)

        _state.value = AmpState(
            currentSong = currentSong,
            currentIndex = player.currentMediaItemIndex,
            isPlaying = player.isPlaying,
            progressMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L),
            repeatMode = player.repeatMode,
            shuffleModeEnabled = player.shuffleModeEnabled
        )

        // For widget updates
        if(previousMediaId != currentMediaId || previousIsPlaying != player.isPlaying){
            appContext?.let{ ctx ->
                scope.launch(Dispatchers.IO) { updateGlanceWidgets(ctx) }
            }
        }
    }

    private fun startProgressTracker() {
        scope.launch {
            while (isActive) {
                controller?.let { ctrl ->
                    if (ctrl.isPlaying) {

                        val posMs = ctrl.currentPosition.coerceAtLeast(0L)
                        val durMs = ctrl.duration.coerceAtLeast(0L)

                        _state.value = _state.value.copy(
                            progressMs = posMs,
                            durationMs = durMs
                        )

                        val currentProgressTrack = posMs / 5000L
                        if(currentProgressTrack != lastWidgetProgressSec){
                            lastWidgetProgressSec = currentProgressTrack
                            appContext?.let { ctx ->
                                launch(Dispatchers.IO) { updateGlanceWidgets(ctx) }
                            }
                        }
                    }
                }
                delay(1000)
            }
        }
    }
}
fun playNext(song: HeartSong) {
        val ctrl = controller
        if(ctrl == null) return
        val nextIndex = (ctrl.currentMediaItemIndex + 1).coerceAtMost(_queue.value.size)
        _queue.value = _queue.value.toMutableList().apply { add(nextIndex, song) }
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(song.contentUri)
            .setMediaMetadata(MediaMetadata.Builder().setTitle(song.title).setArtist(song.artist).build())
            .build()
        ctrl.addMediaItem(nextIndex, mediaItem)
    }

    fun removeQueueItem(index: Int) {
        val ctrl = controller
        if(ctrl == null) return
        if (index in _queue.value.indices) {
            _queue.value = _queue.value.toMutableList().apply { removeAt(index) }
            ctrl.removeMediaItem(index)
        }
    }

    fun clearQueue() {
        val ctrl = controller
        if(ctrl == null) return
        ctrl.clearMediaItems()
        _queue.value = emptyList()
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

        // Reconnect session recovery
        if (_queue.value.isEmpty() && currentMediaId != null) {
            HeartEngine.songs.value.find { it.id.toString() == currentMediaId }?.let {
                _queue.value = listOf(it)
            }
        }
        val previousSong = _state.value.currentSong
        val previousProgress = _state.value.progressMs
        val previousIsPlaying = _state.value.isPlaying
        val previousMediaId = _state.value.currentSong?.id?.toString()

        // fixup!: When active track changes, log the previous track's actual listened duration
        if (previousSong != null && previousSong.id.toString() != currentMediaId && previousProgress > 3000L) {
            appContext?.let { ctx ->
                ListeningStatsManager.recordTrackPlay(ctx, previousSong, actualPlayedMs = previousProgress)
            }
        }

        val currentSong = _queue.value.getOrNull(player.currentMediaItemIndex)

        _state.value = AmpState(
            currentSong = currentSong,
            currentIndex = player.currentMediaItemIndex,
            isPlaying = player.isPlaying,
            progressMs = player.currentPosition.coerceAtLeast(0L),
            durationMs = player.duration.coerceAtLeast(0L),
            repeatMode = player.repeatMode,
            shuffleModeEnabled = player.shuffleModeEnabled
        )

        // For widget updates
        if(previousMediaId != currentMediaId || previousIsPlaying != player.isPlaying){
            appContext?.let{ ctx ->
                scope.launch(Dispatchers.IO) { updateGlanceWidgets(ctx) }
            }
        }
    }

    private fun startProgressTracker() {
        scope.launch {
            while (isActive) {
                controller?.let { ctrl ->
                    if (ctrl.isPlaying) {

                        val posMs = ctrl.currentPosition.coerceAtLeast(0L)
                        val durMs = ctrl.duration.coerceAtLeast(0L)

                        _state.value = _state.value.copy(
                            progressMs = posMs,
                            durationMs = durMs
                        )

                        val currentProgressTrack = posMs / 5000L
                        if(currentProgressTrack != lastWidgetProgressSec){
                            lastWidgetProgressSec = currentProgressTrack
                            appContext?.let { ctx ->
                                launch(Dispatchers.IO) { updateGlanceWidgets(ctx) }
                            }
                        }
                    }
                }
                delay(1000)
            }
        }
    }
}
