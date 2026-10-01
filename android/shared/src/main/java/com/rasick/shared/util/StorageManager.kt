package com.rasick.shared.util

import android.content.Context
import android.os.StatFs
import java.io.File

object StorageManager {
    private var downloadDir: File? = null

    fun initialize(context: Context) {
        downloadDir = File(context.applicationContext.filesDir, "downloads").apply {
            if (!exists()) mkdirs()
        }
    }

    private fun getDir(): File {
        return downloadDir ?: throw IllegalStateException("StorageManager not initialized")
    }

    /**
     * Get total storage used by downloaded songs
     */
    fun getUsedStorageSize(): Long {
        var totalSize = 0L
        getDir().listFiles()?.forEach { file ->
            if (file.isFile) {
                totalSize += file.length()
            }
        }
        return totalSize
    }

    /**
     * Get number of downloaded songs
     */
    fun getDownloadedSongCount(): Int {
        return getDir().listFiles()?.count { it.isFile } ?: 0
    }

    /**
     * Get available internal storage space
     */
    fun getAvailableStorageSize(): Long {
        val path = getDir()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val availableBlocks = stat.availableBlocksLong
        return availableBlocks * blockSize
    }

    /**
     * Get total internal storage space
     */
    fun getTotalStorageSize(): Long {
        val path = getDir()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        return totalBlocks * blockSize
    }

    /**
     * Format bytes to human-readable string
     */
    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024) return String.format("%.1f KB", kb)
        val mb = kb / 1024.0
        if (mb < 1024) return String.format("%.1f MB", mb)
        val gb = mb / 1024.0
        return String.format("%.1f GB", gb)
    }

    /**
     * Get storage usage percentage
     */
    fun getStorageUsagePercentage(): Int {
        val used = getUsedStorageSize()
        val total = getTotalStorageSize()
        if (total == 0L) return 0
        return ((used * 100) / total).toInt()
    }

    /**
     * Check if there's enough space for download
     */
    fun hasEnoughSpace(requiredBytes: Long): Boolean {
        return getAvailableStorageSize() >= requiredBytes
    }

    /**
     * Get downloads directory
     */
    fun getDownloadsDirectory(): File {
        return getDir()
    }

    /**
     * Clear all downloaded files
     */
    fun clearAllDownloads(): Boolean {
        val dir = getDir()
        var success = true
        dir.listFiles()?.forEach { file ->
            if (!file.delete()) {
                success = false
            }
        }
        return success
    }
}
