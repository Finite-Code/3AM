package com.slate.music.Heart

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore

class HeartScanner(private val context: Context) {

    fun scanSongs(): List<HeartSong> {
        val songs = mutableListOf<HeartSong>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 10000"

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA
        )

        val cursor = context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            "${MediaStore.Audio.Media.TITLE} ASC"
        )

        val albumArtBaseUri = Uri.parse("content://media/external/audio/albumart")

        cursor?.use { c ->
            val idColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

            while (c.moveToNext()) {
                val id = c.getLong(idColumn)
                val albumId = c.getLong(albumIdColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString()
                val albumArtUri = ContentUris.withAppendedId(albumArtBaseUri, albumId).toString()

                songs.add(
                    HeartSong(
                        id = id,
                        title = c.getString(titleColumn) ?: "Unknown Title",
                        artist = c.getString(artistColumn) ?: "Unknown Artist",
                        album = c.getString(albumColumn) ?: "Unknown Album",
                        albumId = albumId,
                        contentUri = contentUri,
                        albumArtUri = albumArtUri,
                        durationMs = c.getLong(durationColumn),
                        path = c.getString(dataColumn) ?: ""
                    )
                )
            }
        }

        return songs
    }
}
