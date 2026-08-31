package com.slate.music.Heart

data class HeartSong(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val contentUri: String,  // Uri to music
    val albumArtUri: String?, // Uri to Album Art
    val durationMs: Long,    // Duration in milliseconds
    val path: String,
)
