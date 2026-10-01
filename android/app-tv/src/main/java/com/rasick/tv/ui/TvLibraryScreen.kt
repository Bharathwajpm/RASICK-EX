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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onNavigateToPlaylistDetail: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }

    val favoriteSongs by dao.getFavoriteSongs().collectAsState(initial = emptyList())
    val recentSongs by dao.getRecentSongs().collectAsState(initial = emptyList())
    val playlists by dao.getPlaylists().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
        contentPadding = PaddingValues(bottom = 48.dp)
    ) {
        // 1. Liked Songs
        if (favoriteSongs.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Liked Songs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(favoriteSongs, key = { it.id }) { item ->
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
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(220.dp)
                                    .tvFocusable(RoundedCornerShape(12.dp)) {
                                        PlaybackManager.play(song, favoriteSongs.map { Song(it.id, it.title, it.artist, it.cover, "0", it.category, null, it.audioUrl) })
                                    }
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
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
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Playlists
        if (playlists.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Playlists",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(playlists, key = { it.id }) { playlist ->
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(160.dp)
                                    .tvFocusable(RoundedCornerShape(8.dp)) {
                                        onNavigateToPlaylistDetail(playlist)
                                    }
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier
                                            .size(90.dp)
                                            .clip(RoundedCornerShape(6.dp))
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
                                            Icon(imageVector = Icons.Default.QueueMusic, contentDescription = playlist.name, modifier = Modifier.size(40.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = playlist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Recently Played List
        if (recentSongs.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(recentSongs, key = { it.id }) { item ->
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
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(220.dp)
                                    .tvFocusable(RoundedCornerShape(12.dp)) {
                                        PlaybackManager.play(song, recentSongs.map { Song(it.id, it.title, it.artist, it.cover, it.duration.toString(), it.category, null, it.audioUrl) })
                                    }
                                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
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
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.Gray),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                                    Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }
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
                Button(
                    onClick = onBack,
                    modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                    colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Text("Back to Library")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = playlist.name, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            }

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
                modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                colors = ButtonDefaults.colors(containerColor = Color.Red, contentColor = Color.White)
            ) {
                Text("Delete Playlist")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (songs.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = "No songs inside this playlist yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(songs, key = { it.localId }) { item ->
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                val coverUrl = if (item.cover.startsWith("http")) item.cover else {
                                    val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                    "$baseUrl/${item.cover.removePrefix("/")}"
                                }
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.Gray),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(text = item.title, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                                    Text(text = item.artist, style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                                }
                            }

                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.deletePlaylistSong(playlist.id, item.songId)
                                    }
                                },
                                modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
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
