package com.slate.music

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class Playlist(
    val id: Long,
    val name: String,
    val description: String,
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val songIds: List<Long> = emptyList()
)

// TODO: Migrate to Room DB once we add multi-artist support, custom playlist covers & reordering!
object PlaylistManager {

    private const val FILE_NAME = "user_playlists_store.json"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        isInitialized = true

        scope.launch {
            val loaded = loadFromStorage(context)
            _playlists.value = loaded
        }
    }

    fun createPlaylist(context: Context, name: String, description: String = ""): Playlist {
        val newPlaylist = Playlist(
            id = System.currentTimeMillis(),
            name = name,
            description = description,
            createdAtTimestamp = System.currentTimeMillis(),
            songIds = emptyList()
        )

        val updatedList = _playlists.value + newPlaylist
        _playlists.value = updatedList
        persistToStorage(context, updatedList)
        return newPlaylist
    }

    fun deletePlaylist(context: Context, playlistId: Long) {
        val updatedList = _playlists.value.filterNot { it.id == playlistId }
        _playlists.value = updatedList
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
        _playlists.value = updatedList
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
        _playlists.value = updatedList
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
            // Json parse error / corrupt file, just return whatever loaded
        }
        return loadedPlaylists
    }

    private fun persistToStorage(context: Context, playlistsList: List<Playlist>) {
        scope.launch {
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

                context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use { output ->
                    output.write(jsonArray.toString(2).toByteArray())
                }
            } catch (_: Exception) {
                // Ignore storage write failures
            }
        }
    }
}