package com.slate.music.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.io.File
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.update

data class Playlist(
    val id: Long,
    val name: String,
    val description: String,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val songIds: List<Long> = emptyList()
)

// TODO: Migrate to Room DB eventually. json file is getting slow
object PlaylistManager {

    private const val FILE_NAME = "user_playlists_store.json"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val mutex = Mutex()
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        scope.launch {
            val loaded = loadFromStorage(context)
            _playlists.update { loaded }
        }
    }

    fun createPlaylist(context: Context, name: String, description: String = ""): Playlist {
        val newPlaylist = Playlist(
            id = UUID.randomUUID().mostSignificantBits and Long.MAX_VALUE,
            name = name,
            description = description,
            songIds = emptyList()
        )

        val updatedList = _playlists.value + newPlaylist
        _playlists.update { updatedList }
        persistToStorage(context, updatedList)
        return newPlaylist
    }

    fun deletePlaylist(context: Context, playlistId: Long) {
        val updatedList = _playlists.value.filterNot { it.id == playlistId }
        _playlists.update { updatedList }
        persistToStorage(context, updatedList)
    }

    fun addSongToPlaylist(context: Context, playlistId: Long, songId: Long) {
        val updatedList = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                if (songId !in playlist.songIds) {
                    playlist.copy(songIds = playlist.songIds + songId)
                } else {
                    playlist
                }
            } else {
                playlist
            }
        }
        _playlists.update { updatedList }
        persistToStorage(context, updatedList)
    }

    fun removeSongFromPlaylist(context: Context, playlistId: Long, songId: Long) {
        val updatedList = _playlists.value.map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(songIds = playlist.songIds - songId)
            } else {
                playlist
            }
        }
        _playlists.update { updatedList }
        persistToStorage(context, updatedList)
    }

    private fun loadFromStorage(context: Context): List<Playlist> {
        val loadedPlaylists = mutableListOf<Playlist>()
        try {
            val file = context.getFileStreamPath(FILE_NAME)
            if (!file.exists()) return emptyList()

            val jsonContent = context.openFileInput(FILE_NAME).bufferedReader().use { it.readText() }
            val jsonArray = JSONArray(jsonContent)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.getLong("id")
                val name = obj.getString("name")
                val description = obj.optString("description", "")
                val createdAt = obj.optLong("createdAtTimestamp", System.currentTimeMillis())

                val songIdsArray = obj.optJSONArray("songIds") ?: JSONArray()
                val songIdsList = mutableListOf<Long>()
                for (j in 0 until songIdsArray.length()) {
                    songIdsList.add(songIdsArray.getLong(j))
                }

                loadedPlaylists.add(
                    Playlist(
                        id = id,
                        name = name,
                        description = description,
                        createdAtTimestamp = createdAt,
                        songIds = songIdsList
                    )
                )
            }
        } catch (_: Exception) {
            // corrupt file or smth
        }
        return loadedPlaylists
    }

    private fun persistToStorage(context: Context, playlistsList: List<Playlist>) {
        scope.launch {
            mutex.withLock {
                try {
                    val jsonArray = JSONArray()
                    playlistsList.forEach { playlist ->
                        val obj = JSONObject().apply {
                            put("id", playlist.id)
                            put("name", playlist.name)
                            put("description", playlist.description)
                            put("createdAtTimestamp", playlist.createdAtTimestamp)

                            val idsArray = JSONArray()
                            playlist.songIds.forEach { idsArray.put(it) }
                            put("songIds", idsArray)
                        }
                        jsonArray.put(obj)
                    }

                    val file = File(context.filesDir, FILE_NAME)
                    val tmpFile = File(context.filesDir, "$FILE_NAME.tmp")
                    tmpFile.writeText(jsonArray.toString())
                    tmpFile.renameTo(file)
                } catch (_: Exception) {
                    // ignore write fails
                }
            }
        }
    }
}