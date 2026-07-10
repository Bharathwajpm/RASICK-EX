package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.DownloadedSong
import com.rasick.shared.model.Song

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDownloadsScreen(
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TV Offline Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Local Storage Consumed: ${formatSize(totalUsedStorage)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            var backFocused by remember { mutableStateOf(false) }
            Button(
                onClick = onBack,
                modifier = Modifier
                    .onFocusChanged { backFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (backFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.colors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                )
            ) {
                Text("Back to Dashboard")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (downloadedSongs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No downloaded tracks.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(downloadedSongs) { item ->
                    TvDownloadItemCard(
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDownloadItemCard(
    item: DownloadedSong,
    onClickPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cardFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = { if (item.status == "COMPLETED") onClickPlay() },
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { cardFocused = it.isFocused }
            .border(
                width = 2.dp,
                color = if (cardFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .background(Color(0xFF1E1E1E))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = item.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Status: ${item.status}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when (item.status) {
                            "COMPLETED" -> Color.Green
                            "DOWNLOADING" -> Color.Cyan
                            "PAUSED" -> Color.Yellow
                            else -> Color.Red
                        }
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (item.status) {
                        "DOWNLOADING" -> {
                            TvActionIconButton(onClick = onPause, icon = Icons.Default.Pause, contentDescription = "Pause")
                            TvActionIconButton(onClick = onDelete, icon = Icons.Default.Close, contentDescription = "Cancel")
                        }
                        "PAUSED", "FAILED" -> {
                            TvActionIconButton(onClick = onResume, icon = Icons.Default.PlayArrow, contentDescription = "Resume")
                            TvActionIconButton(onClick = onDelete, icon = Icons.Default.Close, contentDescription = "Cancel")
                        }
                        "COMPLETED" -> {
                            TvActionIconButton(onClick = onDelete, icon = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            }

            if (item.status == "DOWNLOADING" || item.status == "PAUSED") {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = item.progress.toFloat() / 100f,
                        modifier = Modifier.weight(1f).height(6.dp),
                        color = Color.White,
                        trackColor = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${item.progress}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvActionIconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var focused by remember { mutableStateOf(false) }
    Button(
        onClick = onClick,
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .border(
                width = 2.dp,
                color = if (focused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = ButtonDefaults.colors(
            containerColor = Color.DarkGray,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun formatSize(bytes: Long): String {
    val megabytes = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format("%.1f MB", megabytes)
}
