package com.rasick.mobile.ui

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.*
import com.rasick.shared.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }
    val scope = rememberCoroutineScope()

    val favoriteSongs by dao.getFavoriteSongs().collectAsState(initial = emptyList())
    val recentSongs by dao.getRecentSongs().collectAsState(initial = emptyList())
    val playlists by dao.getPlaylists().collectAsState(initial = emptyList())

    var activePlaylist by remember { mutableStateOf<Playlist?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var playlistNameInput by remember { mutableStateOf("") }

    if (activePlaylist != null) {
        PlaylistDetailScreen(
            playlist = activePlaylist!!,
            onBack = { activePlaylist = null },
            dao = dao
        )
    } else {
        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showCreateDialog = true }) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Create Playlist")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Continue Listening Row
                if (recentSongs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Continue Listening",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(recentSongs.take(4)) { item ->
                        RecentSongCard(
                            item = item,
                            onClick = {
                                val song = Song(
                                    id = item.id,
                                    title = item.title,
                                    artist = item.artist,
                                    cover = item.cover,
                                    duration = item.duration.toString(),
                                    category = item.category,
                                    section = null,
                                    audioUrl = item.audioUrl
                                )
                                PlaybackManager.play(song, listOf(song))
                                PlaybackManager.seekTo(item.playbackPosition)
                            }
                        )
                    }
                }

                // Favorites Header
                item {
                    Text(
                        text = "Favorite Tracks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (favoriteSongs.isEmpty()) {
                    item {
                        Text(
                            text = "Songs you like will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(favoriteSongs) { item ->
                        FavoriteSongCard(
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
                                    audioUrl = item.audioUrl
                                )
                                val playQueue = favoriteSongs.map {
                                    Song(
                                        id = it.id,
                                        title = it.title,
                                        artist = it.artist,
                                        cover = it.cover,
                                        duration = "0",
                                        category = it.category,
                                        section = null,
                                        audioUrl = it.audioUrl
                                    )
                                }
                                PlaybackManager.play(song, playQueue)
                            },
                            onRemove = {
                                scope.launch(Dispatchers.IO) {
                                    dao.deleteFavoriteById(item.id)
                                }
                            }
                        )
                    }
                }

                // Playlists Section
                item {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (playlists.isEmpty()) {
                    item {
                        Text(
                            text = "No playlists created yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(playlists) { playlist ->
                        PlaylistRowCard(
                            playlist = playlist,
                            onClick = { activePlaylist = playlist },
                            onDelete = {
                                scope.launch(Dispatchers.IO) {
                                    dao.deletePlaylistById(playlist.id)
                                    dao.deleteSongsByPlaylistId(playlist.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistNameInput,
                    onValueChange = { playlistNameInput = it },
                    label = { Text("Playlist Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (playlistNameInput.isNotBlank()) {
                            scope.launch(Dispatchers.IO) {
                                dao.insertPlaylist(
                                    Playlist(
                                        id = UUID.randomUUID().toString(),
                                        name = playlistNameInput,
                                        coverImage = null,
                                        createdDate = System.currentTimeMillis()
                                    )
                                )
                                withContext(Dispatchers.Main) {
                                    playlistNameInput = ""
                                    showCreateDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun RecentSongCard(
    item: RecentSong,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverUrl = if (item.cover.startsWith("http")) item.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${item.cover.removePrefix("/")}"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                
                // Seek progress bar
                if (item.duration > 0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    val progress = item.playbackPosition.toFloat() / item.duration.toFloat()
                    LinearProgressIndicator(
                        progress = progress.coerceIn(0f, 1f),
                        modifier = Modifier.fillMaxWidth().height(4.dp)
                    )
                }
            }
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume track")
        }
    }
}

@Composable
fun FavoriteSongCard(
    item: FavoriteSong,
    onClickPlay: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverUrl = if (item.cover.startsWith("http")) item.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${item.cover.removePrefix("/")}"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClickPlay),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = coverUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRemove) {
                Icon(imageVector = Icons.Default.Favorite, contentDescription = "Liked", tint = Color.Red)
            }
        }
    }
}

@Composable
fun PlaylistRowCard(
    playlist: Playlist,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                val coverImage = playlist.coverImage
                if (coverImage != null) {
                    val coverUrl = if (coverImage.startsWith("http")) coverImage else {
                        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                        "$baseUrl/${coverImage.removePrefix("/")}"
                    }
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = playlist.name,
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(imageVector = Icons.Default.QueueMusic, contentDescription = playlist.name, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = playlist.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = "Local Playlist", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Playlist", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun PlaylistDetailScreen(
    playlist: Playlist,
    onBack: () -> Unit,
    dao: LibraryDao,
    modifier: Modifier = Modifier
) {
    val songs by dao.getPlaylistSongs(playlist.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf(playlist.name) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(text = playlist.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Rename Playlist")
                }
            }
        }

        if (songs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No songs added to this playlist yet.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(songs) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    val song = Song(
                                        id = item.songId,
                                        title = item.title,
                                        artist = item.artist,
                                        cover = item.cover,
                                        duration = "0",
                                        category = item.category,
                                        section = null,
                                        audioUrl = item.audioUrl
                                    )
                                    val playQueue = songs.map {
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
                                    PlaybackManager.play(song, playQueue)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val coverUrl = if (item.cover.startsWith("http")) item.cover else {
                                val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                "$baseUrl/${item.cover.removePrefix("/")}"
                            }
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.deletePlaylistSong(playlist.id, item.songId)
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircleOutline, contentDescription = "Remove song", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Playlist") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameInput.isNotBlank()) {
                            scope.launch(Dispatchers.IO) {
                                dao.renamePlaylist(playlist.id, renameInput)
                                withContext(Dispatchers.Main) {
                                    showRenameDialog = false
                                }
                            }
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
