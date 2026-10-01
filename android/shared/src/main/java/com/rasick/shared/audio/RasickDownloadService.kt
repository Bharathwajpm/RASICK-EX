package com.rasick.shared.audio

import android.app.Notification
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadManager
import androidx.media3.exoplayer.offline.DownloadService
import androidx.media3.exoplayer.offline.DownloadNotificationHelper
import androidx.media3.exoplayer.scheduler.PlatformScheduler
import androidx.media3.exoplayer.scheduler.Scheduler
import com.rasick.shared.R

@OptIn(UnstableApi::class)
class RasickDownloadService : DownloadService(
    1001,
    DownloadService.DEFAULT_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
    "download_channel",
    R.string.download_channel_name,
    0
) {

    override fun getDownloadManager(): DownloadManager {
        return com.rasick.shared.audio.DownloadManager.getMedia3DownloadManager(this)
    }

    override fun getScheduler(): Scheduler? {
        return try {
            PlatformScheduler(this, 1002)
        } catch (e: Exception) {
            null
        }
    }

    override fun getForegroundNotification(
        downloads: MutableList<Download>,
        notMetRequirements: Int
    ): Notification {
        val helper = DownloadNotificationHelper(this, "download_channel")
        val icon = android.R.drawable.stat_sys_download
        return helper.buildProgressNotification(
            this,
            icon,
            null,
            null,
            downloads,
            notMetRequirements
        )
    }
}
