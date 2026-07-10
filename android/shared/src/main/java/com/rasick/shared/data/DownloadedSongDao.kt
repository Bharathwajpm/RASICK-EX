package com.rasick.shared.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadedSongDao {
    @Query("SELECT * FROM downloaded_songs ORDER BY downloadDate DESC")
    fun getAllDownloadedSongs(): Flow<List<DownloadedSong>>

    @Query("SELECT * FROM downloaded_songs WHERE id = :songId LIMIT 1")
    suspend fun getSongById(songId: String): DownloadedSong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: DownloadedSong)

    @Delete
    suspend fun deleteSong(song: DownloadedSong)

    @Query("DELETE FROM downloaded_songs WHERE id = :songId")
    suspend fun deleteSongById(songId: String)
}
