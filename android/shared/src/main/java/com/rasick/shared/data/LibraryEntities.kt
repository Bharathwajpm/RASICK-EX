package com.rasick.shared.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_songs")
data class FavoriteSong(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val cover: String,
    val audioUrl: String,
    val category: String?,
    val addedDate: Long
)

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val coverImage: String?,
    val createdDate: Long
)

@Entity(tableName = "playlist_songs", indices = [androidx.room.Index(value = ["playlistId"])])
data class PlaylistSong(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val playlistId: String,
    val songId: String,
    val title: String,
    val artist: String,
    val cover: String,
    val audioUrl: String,
    val category: String?,
    val orderIndex: Int
)

@Entity(tableName = "recent_songs")
data class RecentSong(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val cover: String,
    val audioUrl: String,
    val category: String?,
    val playedDate: Long,
    val playbackPosition: Long,
    val duration: Long
)

@Entity(tableName = "playback_history")
data class PlaybackHistory(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val cover: String,
    val audioUrl: String,
    val category: String?,
    val lastPlayedDate: Long,
    val playCount: Int
)

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey val query: String,
    val searchDate: Long
)

@Entity(tableName = "queue_items")
data class QueueItem(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val cover: String,
    val audioUrl: String,
    val category: String?,
    val orderIndex: Int
)
