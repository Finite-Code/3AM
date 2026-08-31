package com.slate.music.Heart

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object HeartEngine {

    private lateinit var database: HeartDatabase
    private lateinit var appContext: Context
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _songs = MutableStateFlow<List<HeartSong>>(emptyList())
    val songs: StateFlow<List<HeartSong>> = _songs.asStateFlow()

    private val _isScanning = MutableStateFlow(value = false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var contentObserver: ContentObserver? = null

    fun initialize(context: Context) {
        if (::appContext.isInitialized) return

        appContext = context.applicationContext
        database = HeartDatabase(appContext)

        scope.launch {
            _songs.value = database.getAllSongs()
        }

        registerObserver(appContext)
    }

    fun scanNow() {
        if (_isScanning.value || !::appContext.isInitialized) return

        scope.launch {
            _isScanning.value = true
            val scanner = HeartScanner(appContext)
            val scanned = scanner.scanSongs()
            database.syncSongs(scanned)
            _songs.value = database.getAllSongs()
            _isScanning.value = false
        }
    }

    private fun registerObserver(context: Context) {
        if (contentObserver != null) return

        contentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                scanNow()
            }
        }

        contentObserver?.let { observer ->
            context.contentResolver.registerContentObserver(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                true,
                observer,
            )
        }
    }
}
