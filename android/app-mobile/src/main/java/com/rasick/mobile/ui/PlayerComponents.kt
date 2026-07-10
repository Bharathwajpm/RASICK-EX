package com.rasick.mobile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.*
import com.rasick.shared.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MiniPlayer(
    modifier: Modifier = Modifier
) {
    val song = PlaybackManager.currentSong ?: return
    val isPlaying = PlaybackManager.isPlaying
    val position = PlaybackManager.position
    val duration = PlaybackManager.duration

    val coverUrl = if (song.cover.startsWith("http")) song.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${song.cover.removePrefix("/")}"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { PlaybackManager.isExpanded = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = { PlaybackManager.togglePlay() }) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Play/Pause"
                    )
                }
                IconButton(onClick = { PlaybackManager.next() }) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next"
                    )
                }
            }

            // Simple micro linear progress line
            val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.Transparent
            )
        }
    }
}

@Composable
fun FullPlayer(
    onNavigateToQueue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = PlaybackManager.currentSong ?: return
    val isPlaying = PlaybackManager.isPlaying
    val isBuffering = PlaybackManager.isBuffering
    val position = PlaybackManager.position
    val duration = PlaybackManager.duration
    val isRepeatOne = PlaybackManager.isRepeatOne
    val isShuffle = PlaybackManager.isShuffleEnabled
    val errorMsg = PlaybackManager.errorMsg

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }

    val favorites by dao.getFavoriteSongs().collectAsState(initial = emptyList())
    val playlists by dao.getPlaylists().collectAsState(initial = emptyList())

    val isLiked = favorites.any { it.id == song.id }

    var showPlaylistDialog by remember { mutableStateOf(false) }

    val coverUrl = if (song.cover.startsWith("http")) song.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${song.cover.removePrefix("/")}"
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { PlaybackManager.isExpanded = false }) {
                    Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Minimize")
                }
                Text(
                    text = "NOW PLAYING",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onNavigateToQueue) {
                    Icon(imageVector = Icons.Default.QueueMusic, contentDescription = "View Queue")
                }
            }

            // Cover Art
            AsyncImage(
                model = coverUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            // Metadata & Actions (Like, Playlist Add)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    if (isLiked) {
                                        dao.deleteFavoriteById(song.id)
                                    } else {
                                        dao.insertFavorite(
                                            FavoriteSong(
                                                id = song.id,
                                                title = song.title,
                                                artist = song.artist,
                                                cover = song.cover,
                                                audioUrl = song.audioUrl ?: "",
                                                category = song.category,
                                                addedDate = System.currentTimeMillis()
                                            )
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like Track",
                                tint = if (isLiked) Color.Red else LocalContentColor.current
                            )
                        }

                        IconButton(onClick = { showPlaylistDialog = true }) {
                            Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = "Add to Playlist")
                        }
                    }
                }

                errorMsg?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }

            // Slider Timeline
            Column(modifier = Modifier.fillMaxWidth()) {
                val sliderPosition = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        val seekMs = (it * duration).toLong()
                        PlaybackManager.seekTo(seekMs)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(position), style = MaterialTheme.typography.bodySmall)
                    Text(text = formatTime(duration), style = MaterialTheme.typography.bodySmall)
                }
            }

            // Playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { PlaybackManager.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (isShuffle) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }

                IconButton(onClick = { PlaybackManager.prev() }) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous")
                }

                Box(contentAlignment = Alignment.Center) {
                    if (isBuffering) {
                        CircularProgressIndicator(modifier = Modifier.size(64.dp))
                    }
                    IconButton(
                        onClick = { PlaybackManager.togglePlay() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                IconButton(onClick = { PlaybackManager.next() }) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
                }

                IconButton(onClick = { PlaybackManager.toggleRepeatOne() }) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repeat Mode",
                        tint = if (isRepeatOne) MaterialTheme.colorScheme.primary else LocalContentColor.current
                    )
                }
            }
        }
    }

    if (showPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists created yet. Create one inside your Library tab first!")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(playlists) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch(Dispatchers.IO) {
                                            // Get songs count to assign orderIndex
                                            dao.insertPlaylistSong(
                                                PlaylistSong(
                                                    playlistId = playlist.id,
                                                    songId = song.id,
                                                    title = song.title,
                                                    artist = song.artist,
                                                    cover = song.cover,
                                                    audioUrl = song.audioUrl ?: "",
                                                    category = song.category,
                                                    orderIndex = 0
                                                )
                                            )
                                            // Set playlist coverImage to song cover
                                            dao.updatePlaylistCover(playlist.id, song.cover)
                                            withContext(Dispatchers.Main) {
                                                showPlaylistDialog = false
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    text = playlist.name,
                                    modifier = Modifier.padding(16.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
