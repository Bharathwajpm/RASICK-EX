package com.rasick.shared.model

import com.google.gson.annotations.SerializedName

data class StorageInfo(
    @SerializedName("used") val used: Double = 0.0,
    @SerializedName("capacity") val capacity: Double = 5.0,
    @SerializedName("percentage") val percentage: Double = 0.0,
    @SerializedName("remaining") val remaining: Double = 5.0,
    @SerializedName("warning") val warning: Boolean = false,
    @SerializedName("rawUsed") val rawUsed: Long = 0L
)

data class AnalyticsInfo(
    @SerializedName("uploadsToday") val uploadsToday: Int = 0,
    @SerializedName("downloadsToday") val downloadsToday: Int = 0,
    @SerializedName("totalDownloads") val totalDownloads: Int = 0,
    @SerializedName("mostPlayedSong") val mostPlayedSong: String = "None",
    @SerializedName("largestSong") val largestSong: String = "None",
    @SerializedName("largestCoverImage") val largestCoverImage: String = "None",
    @SerializedName("avgSongSize") val avgSongSize: String = "0 MB"
)

data class SongStatsInfo(
    @SerializedName("totalAudioSize") val totalAudioSize: String = "0 Bytes",
    @SerializedName("avgAudioSize") val avgAudioSize: String = "0 Bytes",
    @SerializedName("largestAudioFile") val largestAudioFile: String = "None",
    @SerializedName("smallestAudioFile") val smallestAudioFile: String = "None",
    @SerializedName("newestUpload") val newestUpload: String = "None",
    @SerializedName("oldestUpload") val oldestUpload: String = "None",
    @SerializedName("totalCovers") val totalCovers: Int = 0,
    @SerializedName("avgCoverSize") val avgCoverSize: String = "0 Bytes",
    @SerializedName("largestCover") val largestCover: String = "None"
)

data class UserStatsInfo(
    @SerializedName("registeredUsers") val registeredUsers: Int = 0,
    @SerializedName("adminUsers") val adminUsers: Int = 0,
    @SerializedName("normalUsers") val normalUsers: Int = 0,
    @SerializedName("newUsersToday") val newUsersToday: Int = 0,
    @SerializedName("mostActiveUser") val mostActiveUser: String = "admin"
)

data class DownloadStatsInfo(
    @SerializedName("downloadedSongs") val downloadedSongs: Int = 0,
    @SerializedName("downloadsToday") val downloadsToday: Int = 0,
    @SerializedName("mostDownloadedSong") val mostDownloadedSong: String = "None",
    @SerializedName("totalOfflineStorageUsed") val totalOfflineStorageUsed: String = "0 Bytes",
    @SerializedName("offlineCachedSongs") val offlineCachedSongs: Int = 0
)

data class PlaybackStatsInfo(
    @SerializedName("mostPlayedSong") val mostPlayedSong: String = "None",
    @SerializedName("leastPlayedSong") val leastPlayedSong: String = "None",
    @SerializedName("recentlyPlayedCount") val recentlyPlayedCount: Int = 0,
    @SerializedName("favoriteCount") val favoriteCount: Int = 0,
    @SerializedName("playlistCount") val playlistCount: Int = 0
)

data class SystemHealthInfo(
    @SerializedName("backend") val backend: String = "🟢 Healthy",
    @SerializedName("mongodb") val mongodb: String = "🟢 Healthy",
    @SerializedName("filebase") val filebase: String = "🟢 Healthy",
    @SerializedName("uploadService") val uploadService: String = "🟢 Healthy",
    @SerializedName("streamingService") val streamingService: String = "🟢 Healthy",
    @SerializedName("downloadService") val downloadService: String = "🟢 Healthy",
    @SerializedName("storageService") val storageService: String = "🟢 Healthy"
)

data class DashboardStats(
    @SerializedName("totalSongs") val totalSongs: Int = 0,
    @SerializedName("totalCategories") val totalCategories: Int = 0,
    @SerializedName("totalUsers") val totalUsers: Int = 0,
    @SerializedName("downloads") val downloads: Int = 0,
    @SerializedName("storage") val storage: StorageInfo = StorageInfo(),
    @SerializedName("analytics") val analytics: AnalyticsInfo = AnalyticsInfo(),
    @SerializedName("songStats") val songStats: SongStatsInfo = SongStatsInfo(),
    @SerializedName("userStats") val userStats: UserStatsInfo = UserStatsInfo(),
    @SerializedName("downloadStats") val downloadStats: DownloadStatsInfo = DownloadStatsInfo(),
    @SerializedName("playbackStats") val playbackStats: PlaybackStatsInfo = PlaybackStatsInfo(),
    @SerializedName("systemHealth") val systemHealth: SystemHealthInfo = SystemHealthInfo()
)

data class DashboardData(
    @SerializedName("stats") val stats: DashboardStats = DashboardStats(),
    @SerializedName("songs") val songs: List<Song> = emptyList()
)
