package com.rasick.shared.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloaded_songs")
data class DownloadedSong(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val category: String?,
    val cover: String,
    val originalUrl: String,
    val localPath: String,
    val fileSize: Long,
    val downloadDate: Long,
    val status: String, // DOWNLOADING, COMPLETED, PAUSED, FAILED
    val progress: Int = 0,
    val audioType: String? = null, // MP3, AAC, WAV, FLAC, AC3, E-AC3, DTS, DTS-HD
    val playbackPosition: Long = 0L // Last playback position in milliseconds
)
