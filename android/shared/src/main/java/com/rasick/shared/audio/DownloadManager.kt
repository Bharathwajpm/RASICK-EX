package com.rasick.shared.audio

import android.content.Context
import android.net.Uri
import android.os.StatFs
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.NoOpCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.DownloadedSong
import com.rasick.shared.model.Song
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.Executor

@OptIn(UnstableApi::class)
object DownloadManager {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var database: DownloadDatabase? = null
    private var appContext: Context? = null
    
    private var databaseProvider: StandaloneDatabaseProvider? = null
    private var simpleCache: SimpleCache? = null
    private var downloadManager: androidx.media3.exoplayer.offline.DownloadManager? = null
    private var cacheDataSourceFactory: CacheDataSource.Factory? = null
    private var httpDataSourceFactory: DefaultHttpDataSource.Factory? = null

    private fun buildAuthHeaders(): Map<String, String> {
        val token = RetrofitClient.getAuthToken()
        return if (!token.isNullOrBlank()) {
            mapOf("Authorization" to "Bearer $token")
        } else {
            emptyMap()
        }
    }

    /**
     * Call after login/logout to update the JWT token on ExoPlayer's HTTP stack.
     */
    fun refreshAuthHeaders() {
        httpDataSourceFactory?.setDefaultRequestProperties(buildAuthHeaders())
    }

    @Synchronized
    fun initialize(context: Context) {
        if (downloadManager != null) return
        appContext = context.applicationContext
        
        database = DownloadDatabase.getDatabase(context.applicationContext)
        
        val dbProvider = StandaloneDatabaseProvider(context.applicationContext)
        databaseProvider = dbProvider
        
        val cacheDir = File(context.applicationContext.filesDir, "media3_cache")
        val cache = SimpleCache(cacheDir, NoOpCacheEvictor(), dbProvider)
        simpleCache = cache
        
        val factory = DefaultHttpDataSource.Factory()
            .setDefaultRequestProperties(buildAuthHeaders())
        httpDataSourceFactory = factory
        
        val manager = androidx.media3.exoplayer.offline.DownloadManager(
            context.applicationContext,
            dbProvider,
            cache,
            factory,
            Executor { it.run() }
        )
        downloadManager = manager
        
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(factory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        manager.addListener(object : androidx.media3.exoplayer.offline.DownloadManager.Listener {
            override fun onDownloadChanged(
                downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                download: Download,
                finalException: java.lang.Exception?
            ) {
                syncDownloadToRoom(download)
            }

            override fun onDownloadRemoved(
                downloadManager: androidx.media3.exoplayer.offline.DownloadManager,
                download: Download
            ) {
                scope.launch {
                    database?.downloadedSongDao()?.deleteSongById(download.request.id)
                }
            }
        })
        
        // Recover states and sync Room DB on startup
        scope.launch {
            delay(1000)
            try {
                val cursor = manager.downloadIndex.getDownloads()
                while (cursor.moveToNext()) {
                    syncDownloadToRoom(cursor.download)
                }
                cursor.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    @Synchronized
    fun getMedia3DownloadManager(context: Context): androidx.media3.exoplayer.offline.DownloadManager {
        initialize(context)
        return downloadManager ?: throw IllegalStateException("DownloadManager not initialized")
    }

    @Synchronized
    fun getCacheDataSourceFactory(context: Context): CacheDataSource.Factory {
        initialize(context)
        return cacheDataSourceFactory ?: throw IllegalStateException("DownloadManager not initialized")
    }

    private fun syncDownloadToRoom(download: Download) {
        scope.launch {
            val dao = database?.downloadedSongDao() ?: return@launch
            val existing = dao.getSongById(download.request.id) ?: return@launch
            
            val status = when (download.state) {
                Download.STATE_COMPLETED -> "COMPLETED"
                Download.STATE_FAILED -> "FAILED"
                Download.STATE_STOPPED -> "PAUSED"
                else -> "DOWNLOADING"
            }
            
            val progress = if (download.percentDownloaded < 0) {
                0
            } else {
                download.percentDownloaded.toInt().coerceIn(0, 100)
            }

            val localPath = if (status == "COMPLETED") {
                getCacheDir() ?: existing.localPath
            } else {
                existing.localPath
            }
            
            val updated = existing.copy(
                status = status,
                progress = progress,
                fileSize = download.contentLength,
                localPath = localPath
            )
            dao.insertSong(updated)
        }
    }

    fun startDownload(song: Song) {
        val manager = downloadManager ?: return
        val db = database ?: return
        
        scope.launch {
            val dao = db.downloadedSongDao()
            var downloadedSong = dao.getSongById(song.id)
            
            val resolvedUrl = if (song.audioUrl?.startsWith("http") == true) song.audioUrl else {
                val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                "$baseUrl/${song.audioUrl?.removePrefix("/")}"
            }

            val extension = song.audioUrl?.substringAfterLast(".", "")?.lowercase() ?: ""
            val audioType = when (extension) {
                "mp3" -> "MP3"
                "flac" -> "FLAC"
                "wav" -> "WAV"
                "aac", "m4a" -> "AAC"
                "ac3" -> "AC3"
                "ec3", "eac3" -> "E-AC3"
                "dts" -> "DTS"
                "dtshd" -> "DTS-HD"
                else -> extension.uppercase().ifBlank { null }
            }
            
            if (downloadedSong == null) {
                downloadedSong = DownloadedSong(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    category = song.category,
                    cover = song.cover,
                    originalUrl = resolvedUrl,
                    localPath = "",
                    fileSize = 0L,
                    downloadDate = System.currentTimeMillis(),
                    status = "DOWNLOADING",
                    progress = 0,
                    audioType = audioType
                )
                dao.insertSong(downloadedSong)
            } else {
                if (downloadedSong.status == "COMPLETED") return@launch
                downloadedSong = downloadedSong.copy(status = "DOWNLOADING", audioType = audioType ?: downloadedSong.audioType)
                dao.insertSong(downloadedSong)
            }

            val downloadRequest = DownloadRequest.Builder(song.id, Uri.parse(resolvedUrl))
                .build()
            
            val ctx = appContext ?: return@launch
            DownloadService.sendAddDownload(
                ctx,
                RasickDownloadService::class.java,
                downloadRequest,
                /* foreground = */ true
            )
        }
    }

    fun pauseDownload(songId: String) {
        val ctx = appContext ?: return
        DownloadService.sendSetStopReason(
            ctx,
            RasickDownloadService::class.java,
            songId,
            /* stopReason = */ 1,
            /* foreground = */ false
        )
    }

    fun resumeDownload(songId: String) {
        val ctx = appContext ?: return
        DownloadService.sendSetStopReason(
            ctx,
            RasickDownloadService::class.java,
            songId,
            /* stopReason = */ Download.STOP_REASON_NONE,
            /* foreground = */ true
        )
    }

    fun cancelDownload(songId: String) {
        val ctx = appContext ?: return
        DownloadService.sendRemoveDownload(
            ctx,
            RasickDownloadService::class.java,
            songId,
            /* foreground = */ false
        )
    }

    fun deleteDownload(songId: String) {
        cancelDownload(songId)
    }

    fun getAvailableInternalMemorySize(): Long {
        val cacheDir = appContext?.filesDir ?: return 0L
        val stat = StatFs(cacheDir.path)
        return stat.availableBlocksLong * stat.blockSizeLong
    }

    fun getUsedStorageSize(): Long {
        val cache = simpleCache ?: return 0L
        return cache.cacheSpace
    }

    fun isDownloaded(songId: String): Boolean {
        val manager = downloadManager ?: return false
        val download = manager.downloadIndex.getDownload(songId)
        return download != null && download.state == Download.STATE_COMPLETED
    }

    /**
     * Returns the local cache directory path for downloaded media.
     * Media3 SimpleCache stores files in this directory; the CacheDataSource
     * will find them automatically by content ID.
     */
    fun getCacheDir(): String? {
        return appContext?.let { File(it.filesDir, "media3_cache").absolutePath }
    }
}
