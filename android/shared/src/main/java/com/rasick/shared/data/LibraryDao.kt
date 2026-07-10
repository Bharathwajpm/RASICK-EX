package com.rasick.shared.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {
    // Favorites
    @Query("SELECT * FROM favorite_songs ORDER BY addedDate DESC")
    fun getFavoriteSongs(): Flow<List<FavoriteSong>>

    @Query("SELECT * FROM favorite_songs WHERE id = :songId LIMIT 1")
    suspend fun getFavoriteById(songId: String): FavoriteSong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(song: FavoriteSong)

    @Query("DELETE FROM favorite_songs WHERE id = :songId")
    suspend fun deleteFavoriteById(songId: String)

    // Recents (Recently Played / Continue Listening)
    @Query("SELECT * FROM recent_songs ORDER BY playedDate DESC")
    fun getRecentSongs(): Flow<List<RecentSong>>

    @Query("SELECT * FROM recent_songs WHERE id = :songId LIMIT 1")
    suspend fun getRecentById(songId: String): RecentSong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecent(song: RecentSong)

    @Query("DELETE FROM recent_songs WHERE id = :songId")
    suspend fun deleteRecentById(songId: String)

    // Playback History (Most Played)
    @Query("SELECT * FROM playback_history ORDER BY playCount DESC, lastPlayedDate DESC")
    fun getPlaybackHistory(): Flow<List<PlaybackHistory>>

    @Query("SELECT * FROM playback_history WHERE songId = :songId LIMIT 1")
    suspend fun getHistoryById(songId: String): PlaybackHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlaybackHistory)

    // Search History
    @Query("SELECT * FROM search_history ORDER BY searchDate DESC")
    fun getSearchHistory(): Flow<List<SearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistory)

    @Query("DELETE FROM search_history WHERE `query` = :searchQuery")
    suspend fun deleteSearch(searchQuery: String)

    @Query("DELETE FROM search_history")
    suspend fun clearSearchHistory()

    // Playlist CRUD
    @Query("SELECT * FROM playlists ORDER BY createdDate DESC")
    fun getPlaylists(): Flow<List<Playlist>>

    @Query("SELECT * FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistById(playlistId: String): Playlist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: Playlist)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylistById(playlistId: String)

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: String, newName: String)

    @Query("UPDATE playlists SET coverImage = :coverUrl WHERE id = :playlistId")
    suspend fun updatePlaylistCover(playlistId: String, coverUrl: String?)

    // Playlist Songs
    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY orderIndex ASC")
    fun getPlaylistSongs(playlistId: String): Flow<List<PlaylistSong>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylistSong(song: PlaylistSong)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun deletePlaylistSong(playlistId: String, songId: String)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deleteSongsByPlaylistId(playlistId: String)

    // Queue Items
    @Query("SELECT * FROM queue_items ORDER BY orderIndex ASC")
    fun getQueueItems(): Flow<List<QueueItem>>

    @Query("SELECT * FROM queue_items ORDER BY orderIndex ASC")
    suspend fun getQueueItemsSync(): List<QueueItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueItems(items: List<QueueItem>)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()
}
