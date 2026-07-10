package com.rasick.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.DownloadedSong
import com.rasick.shared.model.Song
import kotlinx.coroutines.launch

@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = remember { DownloadDatabase.getDatabase(context) }
    val downloadedSongs by db.downloadedSongDao().getAllDownloadedSongs().collectAsState(initial = emptyList())
    var totalUsedStorage by remember { mutableStateOf(0L) }

    val refreshStorage = {
        totalUsedStorage = DownloadManager.getUsedStorageSize()
    }

    LaunchedEffect(downloadedSongs) {
        refreshStorage()
    }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Offline Downloads",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Local Storage", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Used Space: ${formatSize(totalUsedStorage)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(imageVector = Icons.Default.Folder, contentDescription = "Storage icon", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (downloadedSongs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No downloaded tracks.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloadedSongs) { item ->
                    DownloadItemCard(
                        item = item,
                        onClickPlay = {
                            val song = Song(
                                id = item.id,
                                title = item.title,
                                artist = item.artist,
                                cover = item.cover,
                                duration = "0",
                                category = item.category,
                                section = null,
                                audioUrl = item.originalUrl
                            )
                            val playQueue = downloadedSongs.map {
                                Song(
                                    id = it.id,
                                    title = it.title,
                                    artist = it.artist,
                                    cover = it.cover,
                                    duration = "0",
                                    category = it.category,
                                    section = null,
                                    audioUrl = it.originalUrl
                                )
                            }
                            PlaybackManager.play(song, playQueue)
                        },
                        onPause = { DownloadManager.pauseDownload(item.id) },
                        onResume = {
                            val song = Song(
                                id = item.id,
                                title = item.title,
                                artist = item.artist,
                                cover = item.cover,
                                duration = "0",
                                category = item.category,
                                section = null,
                                audioUrl = item.originalUrl
                            )
                            DownloadManager.startDownload(song)
                        },
                        onDelete = { DownloadManager.deleteDownload(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItemCard(
    item: DownloadedSong,
    onClickPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = item.status == "COMPLETED", onClick = onClickPlay),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${item.status}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when (item.status) {
                            "COMPLETED" -> Color.Green
                            "DOWNLOADING" -> MaterialTheme.colorScheme.primary
                            "PAUSED" -> Color.Yellow
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }

                Row {
                    when (item.status) {
                        "DOWNLOADING" -> {
                            IconButton(onClick = onPause) {
                                Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause")
                            }
                            IconButton(onClick = onDelete) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                            }
                        }
                        "PAUSED", "FAILED" -> {
                            IconButton(onClick = onResume) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                            }
                            IconButton(onClick = onDelete) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel")
                            }
                        }
                        "COMPLETED" -> {
                            IconButton(onClick = onDelete) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (item.status == "DOWNLOADING" || item.status == "PAUSED") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = item.progress.toFloat() / 100f,
                        modifier = Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "${item.progress}%", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    val megabytes = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format("%.1f MB", megabytes)
}
