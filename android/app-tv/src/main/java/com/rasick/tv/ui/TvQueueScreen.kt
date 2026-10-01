package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.api.RetrofitClient

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvQueueScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val queue = PlaybackManager.queue
    val currentSong = PlaybackManager.currentSong

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // TV Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Play Queue",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Clear Queue Button
                if (queue.isNotEmpty()) {
                    Button(
                        onClick = { PlaybackManager.clearPlaybackQueue() },
                        modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                        colors = ButtonDefaults.colors(containerColor = Color.Red, contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "Clear Queue", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Queue")
                        }
                    }
                }

                // Back Button
                Button(
                    onClick = onBack,
                    modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                    colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Text("Back to Dashboard")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (queue.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No songs inside playback queue.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(queue, key = { idx, song -> "${song.id}_$idx" }) { index, song ->
                    val isCurrent = currentSong?.id == song.id
                    var itemFocused by remember { mutableStateOf(false) }

                    Surface(
                        onClick = { PlaybackManager.play(song, queue) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { itemFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (itemFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(if (isCurrent) Color(0xFF1B3D2B) else Color(0xFF1E1E1E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val coverUrl = if (song.cover.startsWith("http")) song.cover else {
                                    val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                    "$baseUrl/${song.cover.removePrefix("/")}"
                                }
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = song.title,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Gray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = song.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.White,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(text = song.artist, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                                }
                                if (isCurrent) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "NOW PLAYING",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Green
                                    )
                                }
                            }

                            // Reordering and removing actions
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Move Up
                                if (index > 0) {
                                    Button(
                                        onClick = { PlaybackManager.reorderQueue(index, index - 1) },
                                        modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = "Move Up", modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Move Down
                                if (index < queue.size - 1) {
                                    Button(
                                        onClick = { PlaybackManager.reorderQueue(index, index + 1) },
                                        modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Move Down", modifier = Modifier.size(16.dp))
                                    }
                                }

                                // Remove
                                Button(
                                    onClick = { PlaybackManager.removeSongFromQueue(song.id) },
                                    modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                                    colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                                ) {
                                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
