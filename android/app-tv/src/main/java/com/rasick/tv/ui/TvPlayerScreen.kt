package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.FavoriteSong
import com.rasick.shared.data.Playlist
import com.rasick.shared.data.PlaylistSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPlayerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val song = PlaybackManager.currentSong ?: return
    val isPlaying = PlaybackManager.isPlaying
    val isBuffering = PlaybackManager.isBuffering
    val position = PlaybackManager.position
    val duration = PlaybackManager.duration
    val errorMsg = PlaybackManager.errorMsg

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }

    var isFavorite by remember { mutableStateOf(false) }
    var isDownloaded by remember { mutableStateOf(false) }
    var showPlaylistDialog by remember { mutableStateOf(false) }
    var playlists by remember { mutableStateOf<List<Playlist>>(emptyList()) }

    // Load initial states
    LaunchedEffect(song.id) {
        scope.launch(Dispatchers.IO) {
            val fav = dao.getFavoriteById(song.id)
            isFavorite = fav != null
        }
        isDownloaded = DownloadManager.isDownloaded(song.id)
    }

    val coverUrl = if (song.cover.startsWith("http")) song.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${song.cover.removePrefix("/")}"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D0D0D), Color(0xFF1A1A1A))
                )
            )
    ) {
        // Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Back to Portal")
                }
            }
        }

        // Main Player Layout (Split screen)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 48.dp, vertical = 72.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Side: Large Artwork
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(360.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, Color.DarkGray, RoundedCornerShape(16.dp))
                        .background(Color.DarkGray),
                    contentScale = ContentScale.Crop
                )
            }

            // Right Side: Details & Controls
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .padding(start = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 36.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.LightGray,
                    fontSize = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Codec & Quality badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Codec Badge
                    val codec = when (PlaybackManager.activeMimeType?.lowercase()) {
                        "audio/mpeg" -> "MP3"
                        "audio/flac" -> "FLAC"
                        "audio/mp4a-latm", "audio/aac" -> "AAC"
                        "audio/ac3" -> "AC3"
                        "audio/eac3" -> "EAC3"
                        "audio/vnd.dts" -> "DTS"
                        "audio/vnd.dts.hd" -> "DTS-HD"
                        "audio/truehd" -> "TrueHD"
                        else -> "MP3"
                    }
                    Text(
                        text = codec,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Green,
                        modifier = Modifier
                            .background(Color(0xFF1E3A1E), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )

                    // Audio output configuration info
                    val formatStr = if (PlaybackManager.activeSampleRate > 0) {
                        "${PlaybackManager.activeSampleRate / 1000} kHz • ${if (PlaybackManager.activeChannels > 2) "${PlaybackManager.activeChannels}.1 ch" else "Stereo"}"
                    } else {
                        "48 kHz • Stereo"
                    }
                    Text(
                        text = formatStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (isDownloaded) {
                        Text(
                            text = "OFFLINE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.Cyan,
                            modifier = Modifier
                                .background(Color(0xFF0F323A), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                errorMsg?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Progress Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTime(position),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    
                    val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color.White,
                        trackColor = Color.DarkGray
                    )

                    Text(
                        text = formatTime(duration),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Playback Control Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Shuffle
                    IconButton(
                        onClick = { PlaybackManager.toggleShuffle() },
                        icon = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        active = PlaybackManager.isShuffleEnabled
                    )

                    // Previous
                    IconButton(
                        onClick = { PlaybackManager.prev() },
                        icon = Icons.Default.SkipPrevious,
                        contentDescription = "Previous"
                    )

                    // Rewind (10s)
                    IconButton(
                        onClick = { PlaybackManager.seekTo((position - 10000).coerceAtLeast(0)) },
                        icon = Icons.Default.Replay10,
                        contentDescription = "Rewind"
                    )

                    // Play/Pause
                    Box(contentAlignment = Alignment.Center) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(52.dp),
                                color = Color.White,
                                strokeWidth = 3.dp
                            )
                        }
                        IconButton(
                            onClick = { PlaybackManager.togglePlay() },
                            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            large = true
                        )
                    }

                    // Fast Forward (10s)
                    IconButton(
                        onClick = { PlaybackManager.seekTo((position + 10000).coerceAtMost(duration)) },
                        icon = Icons.Default.Forward10,
                        contentDescription = "Fast Forward"
                    )

                    // Next
                    IconButton(
                        onClick = { PlaybackManager.next() },
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Next"
                    )

                    // Repeat One
                    IconButton(
                        onClick = { PlaybackManager.toggleRepeatOne() },
                        icon = Icons.Default.RepeatOne,
                        contentDescription = "Repeat One",
                        active = PlaybackManager.isRepeatOne
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (Favorite, Playlist, Queue) Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like/Favorite
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                if (isFavorite) {
                                    dao.deleteFavoriteById(song.id)
                                    isFavorite = false
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
                                    isFavorite = true
                                }
                            }
                        },
                        modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                        colors = ButtonDefaults.colors(
                            containerColor = if (isFavorite) Color.Red else Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isFavorite) "Liked" else "Like")
                        }
                    }

                    // Add to Playlist
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                dao.getPlaylists().collect { list ->
                                    playlists = list
                                    showPlaylistDialog = true
                                }
                            }
                        },
                        modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PlaylistAdd, contentDescription = "Add to Playlist", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add to Playlist")
                        }
                    }
                }
            }
        }

        // Overlay Playlist Dialog if visible
        if (showPlaylistDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .width(400.dp)
                        .background(Color(0xFF222222), RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (playlists.isEmpty()) {
                        Text(text = "No playlists found.", color = Color.Gray, modifier = Modifier.padding(vertical = 12.dp))
                        Button(
                            onClick = { showPlaylistDialog = false },
                            modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {}
                        ) {
                            Text("Close")
                        }
                    } else {
                        playlists.forEach { playlist ->
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.insertPlaylistSong(
                                            PlaylistSong(
                                                playlistId = playlist.id,
                                                songId = song.id,
                                                title = song.title,
                                                artist = song.artist,
                                                cover = song.cover,
                                                audioUrl = song.audioUrl ?: "",
                                                category = song.category,
                                                orderIndex = System.currentTimeMillis().toInt()
                                            )
                                        )
                                        showPlaylistDialog = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .tvFocusable(RoundedCornerShape(8.dp)) {},
                                colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                            ) {
                                Text(playlist.name)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showPlaylistDialog = false },
                            modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {}
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun IconButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    active: Boolean = false,
    large: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = Modifier.tvFocusable(RoundedCornerShape(if (large) 28.dp else 20.dp)) {},
        colors = ButtonDefaults.colors(
            containerColor = if (active) Color.White else Color.DarkGray,
            contentColor = if (active) Color.Black else Color.White
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(if (large) 28.dp else 20.dp)
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%02d:%02d", min, sec)
}
