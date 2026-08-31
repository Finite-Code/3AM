package com.slate.music.Heart

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HeartDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "heart_engine.db"
        private const val DB_VERSION = 1
        private const val TABLE_SONGS = "songs"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_SONGS (
                id INTEGER PRIMARY KEY,
                title TEXT,
                artist TEXT,
                album TEXT,
                album_id INTEGER,
                content_uri TEXT,
                album_art_uri TEXT,
                duration INTEGER,
                path TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SONGS")
        onCreate(db)
    }

    fun syncSongs(scannedSongs: List<HeartSong>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (song in scannedSongs) {
                val values = ContentValues().apply {
                    put("id", song.id)
                    put("title", song.title)
                    put("artist", song.artist)
                    put("album", song.album)
                    put("album_id", song.albumId)
                    put("content_uri", song.contentUri)
                    put("album_art_uri", song.albumArtUri)
                    put("duration", song.durationMs)
                    put("path", song.path)
                }
                db.insertWithOnConflict(TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }

            val validIds = scannedSongs.map { it.id }.joinToString(",")
            if (validIds.isNotEmpty()) {
                db.delete(TABLE_SONGS, "id NOT IN ($validIds)", null)
            } else {
                db.delete(TABLE_SONGS, null, null)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getAllSongs(): List<HeartSong> {
        val songs = mutableListOf<HeartSong>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SONGS, null, null, null, null, null, "title ASC")
        cursor.use {
            while (it.moveToNext()) {
                songs.add(
                    HeartSong(
                        id = it.getLong(it.getColumnIndexOrThrow("id")),
                        title = it.getString(it.getColumnIndexOrThrow("title")),
                        artist = it.getString(it.getColumnIndexOrThrow("artist")),
                        album = it.getString(it.getColumnIndexOrThrow("album")),
                        albumId = it.getLong(it.getColumnIndexOrThrow("album_id")),
                        contentUri = it.getString(it.getColumnIndexOrThrow("content_uri")),
                        albumArtUri = it.getString(it.getColumnIndexOrThrow("album_art_uri")),
                        durationMs = it.getLong(it.getColumnIndexOrThrow("duration")),
                        path = it.getString(it.getColumnIndexOrThrow("path"))
                    )
                )
            }
        }
        return songs
    }
}
