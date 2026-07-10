package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.*
import com.rasick.shared.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvLibraryScreen(
    onBack: () -> Unit,
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

    if (activePlaylist != null) {
        TvPlaylistDetailScreen(
            playlist = activePlaylist!!,
            onBack = { activePlaylist = null },
            dao = dao
        )
    } else {
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
                Text(
                    text = "My TV Library",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

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
                    colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Text("Back to Dashboard")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Continue Listening
                if (recentSongs.isNotEmpty()) {
                    item {
                        Text(text = "Continue Listening", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(recentSongs, key = { it.id }) { item ->
                                var cardFocused by remember { mutableStateOf(false) }
                                Surface(
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
                                    },
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(180.dp)
                                        .onFocusChanged { cardFocused = it.isFocused }
                                        .border(
                                            width = 2.dp,
                                            color = if (cardFocused) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                                ) {
                                    val coverUrl = if (item.cover.startsWith("http")) item.cover else {
                                        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                        "$baseUrl/${item.cover.removePrefix("/")}"
                                    }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        AsyncImage(
                                            model = coverUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .align(Alignment.CenterHorizontally)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                        Text(text = item.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Favorites
                if (favoriteSongs.isNotEmpty()) {
                    item {
                        Text(text = "Liked Songs", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(favoriteSongs, key = { it.id }) { item ->
                                var cardFocused by remember { mutableStateOf(false) }
                                Surface(
                                    onClick = {
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
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(180.dp)
                                        .onFocusChanged { cardFocused = it.isFocused }
                                        .border(
                                            width = 2.dp,
                                            color = if (cardFocused) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                                ) {
                                    val coverUrl = if (item.cover.startsWith("http")) item.cover else {
                                        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                        "$baseUrl/${item.cover.removePrefix("/")}"
                                    }
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        AsyncImage(
                                            model = coverUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .size(100.dp)
                                                .align(Alignment.CenterHorizontally)
                                                .clip(RoundedCornerShape(4.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = item.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                        Text(text = item.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }

                // Playlists
                if (playlists.isNotEmpty()) {
                    item {
                        Text(text = "Playlists", style = MaterialTheme.typography.titleLarge, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(playlists, key = { it.id }) { playlist ->
                                var cardFocused by remember { mutableStateOf(false) }
                                Surface(
                                    onClick = { activePlaylist = playlist },
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(180.dp)
                                        .onFocusChanged { cardFocused = it.isFocused }
                                        .border(
                                            width = 2.dp,
                                            color = if (cardFocused) Color.White else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = ClickableSurfaceDefaults.colors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.DarkGray),
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
                                                Icon(imageVector = Icons.Default.QueueMusic, contentDescription = playlist.name, modifier = Modifier.size(48.dp))
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = playlist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvPlaylistDetailScreen(
    playlist: Playlist,
    onBack: () -> Unit,
    dao: LibraryDao,
    modifier: Modifier = Modifier
) {
    val songs by dao.getPlaylistSongs(playlist.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                    colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Text("Back")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = playlist.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }

            var deleteFocused by remember { mutableStateOf(false) }
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        dao.deletePlaylistById(playlist.id)
                        dao.deleteSongsByPlaylistId(playlist.id)
                        withContext(Dispatchers.Main) {
                            onBack()
                        }
                    }
                },
                modifier = Modifier
                    .onFocusChanged { deleteFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (deleteFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.colors(containerColor = Color.Red, contentColor = Color.White)
            ) {
                Text("Delete Playlist")
            }
        }

        if (songs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No songs inside this playlist yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(songs) { item ->
                    var itemFocused by remember { mutableStateOf(false) }
                    Surface(
                        onClick = {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged { itemFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (itemFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .background(Color(0xFF1E1E1E))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val coverUrl = if (item.cover.startsWith("http")) item.cover else {
                                    val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                    "$baseUrl/${item.cover.removePrefix("/")}"
                                }
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                                }
                            }

                            var removeBtnFocused by remember { mutableStateOf(false) }
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.deletePlaylistSong(playlist.id, item.songId)
                                    }
                                },
                                modifier = Modifier
                                    .onFocusChanged { removeBtnFocused = it.isFocused }
                                    .border(
                                        width = 2.dp,
                                        color = if (removeBtnFocused) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(20.dp)
                                    ),
                                colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                            ) {
                                Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove")
                            }
                        }
                    }
                }
            }
        }
    }
}
