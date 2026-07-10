package com.rasick.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.model.Song

@Composable
fun QueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queue = PlaybackManager.queue
    val currentSong = PlaybackManager.currentSong

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Play Queue",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { PlaybackManager.clearPlaybackQueue() }) {
                Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Queue", tint = MaterialTheme.colorScheme.error)
            }
        }

        if (queue.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "Queue is empty.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(queue) { index, song ->
                    val isCurrent = song.id == currentSong?.id
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val coverUrl = if (song.cover.startsWith("http")) song.cover else {
                                val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                "$baseUrl/${song.cover.removePrefix("/")}"
                            }
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { PlaybackManager.play(song, queue) },
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f).clickable { PlaybackManager.play(song, queue) }) {
                                Text(
                                    text = song.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else Color.Unspecified
                                )
                                Text(text = song.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            
                            // Reorder Arrows
                            Row {
                                IconButton(
                                    onClick = { PlaybackManager.reorderQueue(index, index - 1) },
                                    enabled = index > 0
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up")
                                }
                                IconButton(
                                    onClick = { PlaybackManager.reorderQueue(index, index + 1) },
                                    enabled = index < queue.size - 1
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down")
                                }
                                IconButton(onClick = { PlaybackManager.removeSongFromQueue(song.id) }) {
                                    Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
