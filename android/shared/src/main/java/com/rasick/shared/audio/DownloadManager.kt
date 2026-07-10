package com.rasick.shared.audio

import android.content.Context
import android.os.StatFs
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.DownloadedSong
import com.rasick.shared.model.Song
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.ConcurrentHashMap

object DownloadManager {
    private val client = OkHttpClient()
    private val activeCalls = ConcurrentHashMap<String, okhttp3.Call>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var database: DownloadDatabase? = null
    private var downloadDir: File? = null

    fun initialize(context: Context) {
        if (database != null) return
        database = DownloadDatabase.getDatabase(context.applicationContext)
        downloadDir = File(context.applicationContext.filesDir, "downloads").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getDb() = database ?: throw IllegalStateException("DownloadManager not initialized")
    private fun getDir() = downloadDir ?: throw IllegalStateException("DownloadManager not initialized")

    fun startDownload(song: Song) {
        val callKey = song.id
        if (activeCalls.containsKey(callKey)) return

        scope.launch {
            val db = getDb()
            val dao = db.downloadedSongDao()

            var downloadedSong = dao.getSongById(song.id)
            if (downloadedSong == null) {
                val extension = song.audioUrl?.substringAfterLast(".", "mp3") ?: "mp3"
                val localFile = File(getDir(), "${song.id}.$extension")
                downloadedSong = DownloadedSong(
                    id = song.id,
                    title = song.title,
                    artist = song.artist,
                    category = song.category,
                    cover = song.cover,
                    originalUrl = song.audioUrl ?: "",
                    localPath = localFile.absolutePath,
                    fileSize = 0L,
                    downloadDate = System.currentTimeMillis(),
                    status = "DOWNLOADING",
                    progress = 0
                )
                dao.insertSong(downloadedSong)
            } else {
                if (downloadedSong.status == "COMPLETED") return@launch
                downloadedSong = downloadedSong.copy(status = "DOWNLOADING", progress = downloadedSong.progress)
                dao.insertSong(downloadedSong)
            }

            val destinationFile = File(downloadedSong.localPath)
            var existingBytes = 0L
            if (destinationFile.exists()) {
                existingBytes = destinationFile.length()
            }

            if (getAvailableInternalMemorySize() < 50 * 1024 * 1024) {
                dao.insertSong(downloadedSong.copy(status = "FAILED", progress = 0))
                return@launch
            }

            val resolvedUrl = if (song.audioUrl?.startsWith("http") == true) song.audioUrl else {
                val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                "$baseUrl/${song.audioUrl?.removePrefix("/")}"
            }

            val request = Request.Builder()
                .url(resolvedUrl)
                .addHeader("Range", "bytes=$existingBytes-")
                .build()

            val call = client.newCall(request)
            activeCalls[callKey] = call

            try {
                val response = call.execute()
                if (!response.isSuccessful) {
                    val code = response.code
                    if (code == 416) {
                        if (existingBytes > 1024) {
                            dao.insertSong(downloadedSong.copy(status = "COMPLETED", progress = 100, fileSize = existingBytes))
                        } else {
                            destinationFile.delete()
                            dao.insertSong(downloadedSong.copy(status = "FAILED", progress = 0))
                        }
                    } else {
                        dao.insertSong(downloadedSong.copy(status = "FAILED", progress = 0))
                    }
                    activeCalls.remove(callKey)
                    return@launch
                }

                val body = response.body
                if (body == null) {
                    dao.insertSong(downloadedSong.copy(status = "FAILED"))
                    activeCalls.remove(callKey)
                    return@launch
                }

                val contentLength = body.contentLength()
                val totalBytes = if (contentLength == -1L) -1L else (contentLength + existingBytes)

                val randomAccessFile = RandomAccessFile(destinationFile, "rw")
                randomAccessFile.seek(existingBytes)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var bytesWritten = existingBytes

                body.byteStream().use { inputStream ->
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        if (!activeCalls.containsKey(callKey)) {
                            randomAccessFile.close()
                            return@launch
                        }
                        randomAccessFile.write(buffer, 0, bytesRead)
                        bytesWritten += bytesRead
                        if (totalBytes > 0) {
                            val progress = ((bytesWritten * 100) / totalBytes).toInt()
                            dao.insertSong(downloadedSong.copy(progress = progress, fileSize = totalBytes))
                        }
                    }
                }
                randomAccessFile.close()

                if (totalBytes > 0 && destinationFile.length() < totalBytes) {
                    dao.insertSong(downloadedSong.copy(status = "FAILED"))
                } else {
                    dao.insertSong(downloadedSong.copy(status = "COMPLETED", progress = 100, fileSize = destinationFile.length()))
                }

            } catch (e: Exception) {
                if (activeCalls.containsKey(callKey)) {
                    dao.insertSong(downloadedSong.copy(status = "FAILED"))
                }
            } finally {
                activeCalls.remove(callKey)
            }
        }
    }

    fun pauseDownload(songId: String) {
        val call = activeCalls.remove(songId)
        call?.cancel()
        scope.launch {
            val dao = getDb().downloadedSongDao()
            val downloadedSong = dao.getSongById(songId)
            if (downloadedSong != null && downloadedSong.status == "DOWNLOADING") {
                dao.insertSong(downloadedSong.copy(status = "PAUSED"))
            }
        }
    }

    fun cancelDownload(songId: String) {
        val call = activeCalls.remove(songId)
        call?.cancel()
        scope.launch {
            val dao = getDb().downloadedSongDao()
            val downloadedSong = dao.getSongById(songId)
            if (downloadedSong != null) {
                dao.deleteSong(downloadedSong)
                val file = File(downloadedSong.localPath)
                if (file.exists()) file.delete()
            }
        }
    }

    fun deleteDownload(songId: String) {
        cancelDownload(songId)
    }

    fun getAvailableInternalMemorySize(): Long {
        val path = getDir()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize
    }

    fun getUsedStorageSize(): Long {
        var totalSize = 0L
        getDir().listFiles()?.forEach { file ->
            if (file.isFile) {
                totalSize += file.length()
            }
        }
        return totalSize
    }

    fun isDownloaded(songId: String): Boolean {
        var result = false
        runBlocking(Dispatchers.IO) {
            val song = getDb().downloadedSongDao().getSongById(songId)
            if (song != null && song.status == "COMPLETED" && File(song.localPath).exists()) {
                result = true
            }
        }
        return result
    }
}
