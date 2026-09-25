package com.slate.music

import android.content.Context
import com.slate.music.Heart.HeartEngine
import com.slate.music.Heart.HeartSong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

data class SongPlayCount(
    val songId: Long,
    val title: String,
    val artist: String,
    val albumArtUri: String?,
    val playCount: Int
)

data class ArtistPlayCount(
    val artist: String,
    val playCount: Int
)

data class StatsState(
    val todayListeningTimeMs: Long = 0L,
    val weeklyListeningTimeMs: Long = 0L,
    val hourlyDistribution: Map<Int, Int> = emptyMap(),
    val topSongs: List<SongPlayCount> = emptyList(),
    val topArtists: List<ArtistPlayCount> = emptyList(),
    val nightOwl3AmPlayCount: Int = 0
)

object ListeningStatsManager {

    private const val PREFS_NAME = "listening_stats_prefs"
    private const val KEY_TODAY_TIME = "today_time"
    private const val KEY_WEEKLY_TIME = "weekly_time"

    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    private val playCounts = mutableMapOf<Long, Int>()
    private val artistCounts = mutableMapOf<String, Int>()
    private val hourlyMap = mutableMapOf<Int, Int>()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val todayMs = prefs.getLong(KEY_TODAY_TIME, 0L)
        val weeklyMs = prefs.getLong(KEY_WEEKLY_TIME, 0L)

        for (h in 0..23) {
            hourlyMap[h] = prefs.getInt("hour_$h", if (h == 3) 14 else (1..8).random())
        }

        // Load real recorded hourly play counts from SharedPreferences
        for (h in 0..23) {
            hourlyMap[h] = prefs.getInt("hour_$h", 0)
        }

        refreshState(todayMs, weeklyMs)
    }

    fun recordTrackPlay(context: Context, song: HeartSong, actualPlayedMs: Long = 0L) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val hourCount = (hourlyMap[currentHour] ?: 0) + 1
        hourlyMap[currentHour] = hourCount

        val currentSongCount = (playCounts[song.id] ?: 0) + 1
        playCounts[song.id] = currentSongCount

        val currentArtistCount = (artistCounts[song.artist] ?: 0) + 1
        artistCounts[song.artist] = currentArtistCount

        // fixup!: Log actual listened time instead of assuming the user listened to the entire "x" minute song
        val timeToAddMs = if (actualPlayedMs > 0L) {
            actualPlayedMs.coerceAtMost(song.durationMs)
        } else {
            0L
        }

        // Also ignore any short term playback as "listened to time". since ppl don't listen to songs for 10s only!
        if (timeToAddMs < 10000L) return

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newTodayMs = prefs.getLong(KEY_TODAY_TIME, 0L) + timeToAddMs
        val newWeeklyMs = prefs.getLong(KEY_WEEKLY_TIME, 0L) + timeToAddMs

        prefs.edit()
            .putLong(KEY_TODAY_TIME, newTodayMs)
            .putLong(KEY_WEEKLY_TIME, newWeeklyMs)
            .putInt("hour_$currentHour", hourCount)
            .putInt("song_${song.id}", currentSongCount)
            .apply()

        refreshState(newTodayMs, newWeeklyMs)
    }

    private fun refreshState(todayMs: Long, weeklyMs: Long) {
        val allSongs = HeartEngine.songs.value
        val topSongList = playCounts.mapNotNull { (id, count) ->
            val song = allSongs.find { it.id == id } ?: return@mapNotNull null
            SongPlayCount(song.id, song.title, song.artist, song.albumArtUri, count)
        }.sortedByDescending { it.playCount }.take(5)

        val topArtistList = artistCounts.map { (artist, count) ->
            ArtistPlayCount(artist, count)
        }.sortedByDescending { it.playCount }.take(5)

        _state.value = StatsState(
            todayListeningTimeMs = todayMs,
            weeklyListeningTimeMs = weeklyMs,
            hourlyDistribution = hourlyMap.toMap(),
            topSongs = topSongList,
            topArtists = topArtistList,
            nightOwl3AmPlayCount = hourlyMap[3] ?: 0
        )
    }
}
