package com.rasick.shared.audio

import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.*
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.DecoderReuseEvaluation
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.*
import com.rasick.shared.model.Song
import kotlinx.coroutines.*

object PlaybackManager {
    private var player: ExoPlayer? = null
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var appContext: Context? = null

    var currentSong by mutableStateOf<Song?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var isBuffering by mutableStateOf(false)
        private set
    var position by mutableStateOf(0L)
        private set
    var duration by mutableStateOf(0L)
        private set
    var errorMsg by mutableStateOf<String?>(null)
        private set
    var queue by mutableStateOf<List<Song>>(emptyList())
        private set
    var isExpanded by mutableStateOf(false)
    var isRepeatOne by mutableStateOf(false)
        private set
    var isShuffleEnabled by mutableStateOf(false)
        private set

    // Codec active streaming diagnostics states
    var activeMimeType by mutableStateOf<String?>(null)
        private set
    var activeSampleRate by mutableStateOf(0)
        private set
    var activeChannels by mutableStateOf(0)
        private set
    var activeDecoderName by mutableStateOf<String?>(null)
        private set

    private var currentIndex = -1

    fun getPlayerInstance(): ExoPlayer? {
        return player
    }

    // Setter helper functions accessible to package/module classes
    internal fun updatePlaying(playing: Boolean) {
        isPlaying = playing
    }

    internal fun updateBuffering(buffering: Boolean) {
        isBuffering = buffering
    }

    internal fun updateDuration(dur: Long) {
        duration = dur
    }

    internal fun updateError(error: String?) {
        errorMsg = error
    }

    internal fun updateCurrentSong(song: Song?, index: Int) {
        currentSong = song
        currentIndex = index
    }

    internal fun updateActiveFormat(mime: String?, sampleRate: Int, channels: Int) {
        activeMimeType = mime
        activeSampleRate = sampleRate
        activeChannels = channels
    }

    internal fun updateActiveDecoder(decoder: String?) {
        activeDecoderName = decoder
    }

    @OptIn(UnstableApi::class)
    fun initialize(context: Context) {
        if (player != null) return
        appContext = context.applicationContext

        val renderersFactory = DefaultRenderersFactory(context.applicationContext).apply {
            setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        player = ExoPlayer.Builder(context.applicationContext)
            .setRenderersFactory(renderersFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(playing: Boolean) {
                        updatePlaying(playing)
                        if (playing) {
                            startPositionUpdates()
                            startPlaybackService()
                        } else {
                            stopPositionUpdates()
                            saveQueueState()
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        updateBuffering(state == Player.STATE_BUFFERING)
                        updateError(null)
                        if (state == Player.STATE_READY) {
                            updateDuration(player?.duration?.coerceAtLeast(0L) ?: 0L)
                        }
                        if (state == Player.STATE_ENDED) {
                            onPlaybackEnded()
                        }
                    }

                    override fun onPlayerError(error: PlaybackException) {
                        updatePlaying(false)
                        updateBuffering(false)
                        stopPositionUpdates()
                        val msg = when (error.errorCode) {
                            PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                            PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> 
                                "Network connection failed. Reconnecting..."
                            PlaybackException.ERROR_CODE_DECODING_FAILED,
                            PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED -> 
                                "Audio format not supported by device."
                            else -> "Playback failed: ${error.localizedMessage}"
                        }
                        updateError(msg)
                    }

                    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                        val mediaId = mediaItem?.mediaId
                        if (mediaId != null) {
                            val song = queue.find { s -> s.id == mediaId }
                            if (song != null) {
                                val idx = queue.indexOf(song)
                                updateCurrentSong(song, idx)
                                recordPlaybackStart(song)
                            }
                        }
                    }
                })

                // Register diagnostics analyzer listener
                addAnalyticsListener(object : AnalyticsListener {
                    override fun onAudioInputFormatChanged(
                        eventTime: AnalyticsListener.EventTime,
                        format: Format,
                        decoderReuseEvaluation: DecoderReuseEvaluation?
                    ) {
                        updateActiveFormat(format.sampleMimeType, format.sampleRate, format.channelCount)
                    }

                    override fun onAudioDecoderInitialized(
                        eventTime: AnalyticsListener.EventTime,
                        decoderName: String,
                        initializedMs: Long,
                        initializationDurationMs: Long
                    ) {
                        updateActiveDecoder(decoderName)
                    }
                })
            }
        
        restoreQueueState(context.applicationContext)
    }

    private fun startPlaybackService() {
        val ctx = appContext ?: return
        try {
            val serviceIntent = Intent(ctx, PlaybackService::class.java)
            ctx.startService(serviceIntent)
        } catch (e: Exception) {
            // Service launch fallback
        }
    }

    fun play(song: Song, newQueue: List<Song>) {
        val p = player ?: return
        updateError(null)
        queue = newQueue
        currentIndex = newQueue.indexOfFirst { s -> s.id == song.id }
        currentSong = song

        // Clear active diagnostics prior to playing next song
        updateActiveFormat(null, 0, 0)
        updateActiveDecoder(null)

        val extension = song.audioUrl?.substringAfterLast(".", "")?.lowercase() ?: ""
        val mimeType = when (extension) {
            "mp3" -> "audio/mpeg"
            "ac3" -> "audio/ac3"
            "dts" -> "audio/vnd.dts"
            "aac" -> "audio/mp4a-latm"
            "wav" -> "audio/wav"
            "flac" -> "audio/flac"
            else -> "audio/mpeg"
        }

        // Validate platform codec decoders list
        if (mimeType == "audio/ac3" || mimeType == "audio/vnd.dts") {
            val ctx = appContext
            val isSupported = if (ctx != null) {
                DeviceCapabilities.isCodecSupported(mimeType) || DeviceCapabilities.isHdmiConnected(ctx)
            } else {
                DeviceCapabilities.isCodecSupported(mimeType)
            }
            if (!isSupported) {
                updateError("Codec not supported by device hardware.")
                return
            }
        }

        var localPath: String? = null
        appContext?.let { ctx ->
            runBlocking(Dispatchers.IO) {
                val db = DownloadDatabase.getDatabase(ctx)
                val downloaded = db.downloadedSongDao().getSongById(song.id)
                if (downloaded != null && downloaded.status == "COMPLETED") {
                    val file = java.io.File(downloaded.localPath)
                    if (file.exists()) {
                        localPath = file.absolutePath
                    }
                }
            }
        }

        val resolvedUrl = if (localPath != null) {
            "file://$localPath"
        } else if (song.audioUrl?.startsWith("http") == true) {
            song.audioUrl
        } else {
            val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
            "$baseUrl/${song.audioUrl?.removePrefix("/")}"
        }

        p.stop()
        p.clearMediaItems()
        
        val mediaItem = MediaItem.Builder()
            .setUri(resolvedUrl)
            .setMediaId(song.id)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .build()
            )
            .build()

        p.setMediaItem(mediaItem)
        p.prepare()
        p.play()

        recordPlaybackStart(song)
        saveQueueState()
        startPlaybackService()
    }

    fun togglePlay() {
        val p = player ?: return
        if (p.isPlaying) {
            p.pause()
        } else {
            if (p.playbackState == Player.STATE_ENDED) {
                p.seekTo(0)
                p.prepare()
            }
            p.play()
            startPlaybackService()
        }
    }

    fun next() {
        if (queue.isEmpty()) return
        val nextIndex = (currentIndex + 1) % queue.size
        play(queue[nextIndex], queue)
    }

    fun prev() {
        if (queue.isEmpty()) return
        val prevIndex = (currentIndex - 1 + queue.size) % queue.size
        play(queue[prevIndex], queue)
    }

    fun seekTo(ms: Long) {
        player?.seekTo(ms)
        position = ms
        saveQueueState()
    }

    fun toggleRepeatOne() {
        isRepeatOne = !isRepeatOne
        player?.repeatMode = if (isRepeatOne) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
    }

    fun toggleShuffle() {
        isShuffleEnabled = !isShuffleEnabled
        player?.shuffleModeEnabled = isShuffleEnabled
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in queue.indices || toIndex !in queue.indices) return
        val list = queue.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        queue = list
        currentIndex = list.indexOf(currentSong)
        saveQueueState()
    }

    fun removeSongFromQueue(songId: String) {
        val list = queue.toMutableList()
        val songToRemove = list.find { it.id == songId } ?: return
        val index = list.indexOf(songToRemove)
        list.removeAt(index)

        if (songToRemove.id == currentSong?.id) {
            if (list.isNotEmpty()) {
                val nextIndex = index % list.size
                play(list[nextIndex], list)
            } else {
                stop()
                currentSong = null
                queue = emptyList<Song>()
                currentIndex = -1
            }
        } else {
            queue = list
            currentIndex = list.indexOf(currentSong)
        }
        saveQueueState()
    }

    fun clearPlaybackQueue() {
        stop()
        currentSong = null
        queue = emptyList<Song>()
        currentIndex = -1
        saveQueueState()
    }

    fun stop() {
        player?.stop()
        updatePlaying(false)
        updateBuffering(false)
        stopPositionUpdates()
    }

    fun release() {
        saveQueueState()
        stopPositionUpdates()
        player?.release()
        player = null
        currentSong = null
        updatePlaying(false)
        updateBuffering(false)
        queue = emptyList<Song>()
        currentIndex = -1
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        job = scope.launch {
            while (isActive) {
                player?.let { p ->
                    position = p.currentPosition.coerceAtLeast(0L)
                    savePositionProgress(position)
                }
                delay(1000)
            }
        }
    }

    private fun stopPositionUpdates() {
        job?.cancel()
        job = null
    }

    private fun onPlaybackEnded() {
        stopPositionUpdates()
        if (!isRepeatOne) {
            next()
        }
    }

    private fun recordPlaybackStart(song: Song) {
        val ctx = appContext ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val db = DownloadDatabase.getDatabase(ctx)
                val dao = db.libraryDao()

                dao.insertRecent(
                    RecentSong(
                        id = song.id,
                        title = song.title,
                        artist = song.artist,
                        cover = song.cover,
                        audioUrl = song.audioUrl ?: "",
                        category = song.category,
                        playedDate = System.currentTimeMillis(),
                        playbackPosition = 0L,
                        duration = duration
                    )
                )

                val history = dao.getHistoryById(song.id)
                val count = (history?.playCount ?: 0) + 1
                dao.insertHistory(
                    PlaybackHistory(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        cover = song.cover,
                        audioUrl = song.audioUrl ?: "",
                        category = song.category,
                        lastPlayedDate = System.currentTimeMillis(),
                        playCount = count
                    )
                )
            } catch (e: Exception) {
                // Ignore DB write errors
            }
        }
    }

    private fun savePositionProgress(pos: Long) {
        val ctx = appContext ?: return
        val song = currentSong ?: return
        scope.launch(Dispatchers.IO) {
            try {
                val db = DownloadDatabase.getDatabase(ctx)
                val dao = db.libraryDao()
                val recent = dao.getRecentById(song.id)
                if (recent != null) {
                    dao.insertRecent(recent.copy(playbackPosition = pos, duration = duration))
                }
            } catch (e: Exception) {
                // Ignore DB write errors
            }
        }
    }

    private fun saveQueueState() {
        val ctx = appContext ?: return
        val current = currentSong
        val pos = position
        scope.launch(Dispatchers.IO) {
            try {
                val db = DownloadDatabase.getDatabase(ctx)
                val dao = db.libraryDao()
                dao.clearQueue()
                
                val queueItems = queue.mapIndexed { idx, s ->
                    QueueItem(
                        songId = s.id,
                        title = s.title,
                        artist = s.artist,
                        cover = s.cover,
                        audioUrl = s.audioUrl ?: "",
                        category = s.category,
                        orderIndex = idx
                    )
                }
                dao.insertQueueItems(queueItems)

                val prefs = ctx.getSharedPreferences("playback_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("last_song_id", current?.id)
                    .putLong("last_position", pos)
                    .apply()
            } catch (e: Exception) {
                // Ignore queue saving issues
            }
        }
    }

    private fun restoreQueueState(context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                val db = DownloadDatabase.getDatabase(context)
                val dao = db.libraryDao()
                val savedItems = dao.getQueueItemsSync()
                if (savedItems.isNotEmpty()) {
                    val songsList = savedItems.map {
                        Song(
                            id = it.songId,
                            title = it.title,
                            artist = it.artist,
                            cover = it.cover,
                            duration = "0",
                            category = it.category,
                            section = null,
                            audioUrl = it.audioUrl
                        )
                    }

                    val prefs = context.getSharedPreferences("playback_prefs", Context.MODE_PRIVATE)
                    val lastSongId = prefs.getString("last_song_id", null)
                    val lastPos = prefs.getLong("last_position", 0L)

                    val targetSong = songsList.find { it.id == lastSongId } ?: songsList.first()

                    var localPath: String? = null
                    val downloaded = db.downloadedSongDao().getSongById(targetSong.id)
                    if (downloaded != null && downloaded.status == "COMPLETED") {
                        val file = java.io.File(downloaded.localPath)
                        if (file.exists()) {
                            localPath = file.absolutePath
                        }
                    }

                    val resolvedUrl = if (localPath != null) {
                        "file://$localPath"
                    } else if (targetSong.audioUrl?.startsWith("http") == true) {
                        targetSong.audioUrl
                    } else {
                        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                        "$baseUrl/${targetSong.audioUrl?.removePrefix("/")}"
                    }

                    withContext(Dispatchers.Main) {
                        queue = songsList
                        currentIndex = songsList.indexOf(targetSong)
                        currentSong = targetSong

                        player?.let { p ->
                            p.stop()
                            p.clearMediaItems()
                            val mediaItem = MediaItem.Builder()
                                .setUri(resolvedUrl)
                                .setMediaId(targetSong.id)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(targetSong.title)
                                        .setArtist(targetSong.artist)
                                        .build()
                                )
                                .build()
                            p.setMediaItem(mediaItem)
                            p.seekTo(lastPos)
                            p.prepare()
                            position = lastPos
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore queue restoration failures
            }
        }
    }
}
