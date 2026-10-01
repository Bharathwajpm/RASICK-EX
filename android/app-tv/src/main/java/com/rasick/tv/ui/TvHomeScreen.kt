package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.DownloadedSong
import com.rasick.shared.data.Playlist
import com.rasick.shared.model.Artist
import com.rasick.shared.model.Album
import com.rasick.shared.model.Category
import com.rasick.shared.model.Song
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    onNavigateToPlaylistDetail: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }

    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var apiArtists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var apiAlbums by remember { mutableStateOf<List<Album>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    val recentSongs by dao.getRecentSongs().collectAsState(initial = emptyList())
    val favoriteSongs by dao.getFavoriteSongs().collectAsState(initial = emptyList())
    val playlists by dao.getPlaylists().collectAsState(initial = emptyList())
    val downloadsState by db.downloadedSongDao().getAllDownloadedSongs().collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        loading = true
        scope.launch {
            try {
                categories = RetrofitClient.songService.getCategories().categories
                songs = RetrofitClient.songService.getSongs().songs
                try { apiArtists = RetrofitClient.songService.getArtists().artists } catch (_: Exception) {}
                try { apiAlbums = RetrofitClient.songService.getAlbums().albums } catch (_: Exception) {}
            } catch (e: Exception) {
                // Fail silently or load from room
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
            contentPadding = PaddingValues(bottom = 48.dp)
        ) {
            // 1. Continue Watching
            if (recentSongs.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Continue Watching",
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
                                val coverUrl = if (item.cover.startsWith("http")) item.cover else {
                                    val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                    "$baseUrl/${item.cover.removePrefix("/")}"
                                }
                                Box(
                                    modifier = Modifier
                                        .width(180.dp)
                                        .height(180.dp)
                                        .tvFocusable(RoundedCornerShape(8.dp)) {
                                            PlaybackManager.play(song, listOf(song))
                                            PlaybackManager.seekTo(item.playbackPosition)
                                        }
                                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        AsyncImage(
                                            model = coverUrl,
                                            contentDescription = item.title,
                                            modifier = Modifier
                                                .size(90.dp)
                                                .align(Alignment.CenterHorizontally)
                                                .clip(RoundedCornerShape(6.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = item.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(text = item.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        
                                        Spacer(modifier = Modifier.weight(1f))
                                        
                                        // Playback progress indicator
                                        val progress = if (item.duration > 0) item.playbackPosition.toFloat() / item.duration.toFloat() else 0f
                                        LinearProgressIndicator(
                                            progress = progress,
                                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                                            color = Color.Green,
                                            trackColor = Color.DarkGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Recently Played
            if (recentSongs.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Recently Played",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(recentSongs) { item ->
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
                                MovieCard(song = song, onClick = { PlaybackManager.play(song, recentSongs.map { Song(id = it.id, title = it.title, artist = it.artist, cover = it.cover, duration = it.duration.toString(), category = it.category, audioUrl = it.audioUrl) }) })
                            }
                        }
                    }
                }
            }

            // 3. Favorites (Liked Songs)
            if (favoriteSongs.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Favorites",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(favoriteSongs) { item ->
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
                                MovieCard(song = song, onClick = { PlaybackManager.play(song, favoriteSongs.map { Song(id = it.id, title = it.title, artist = it.artist, cover = it.cover, duration = "0", category = it.category, audioUrl = it.audioUrl) }) })
                            }
                        }
                    }
                }
            }

            // 4. Playlists
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
                            items(playlists) { playlist ->
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
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                        Icon(imageVector = Icons.Default.LibraryMusic, contentDescription = playlist.name, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = playlist.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 5. Downloads
            if (downloadsState.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Downloads",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(downloadsState) { item ->
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
                                MovieCard(song = song, onClick = { PlaybackManager.play(song, downloadsState.map { Song(id = it.id, title = it.title, artist = it.artist, cover = it.cover, duration = "0", category = it.category, audioUrl = it.originalUrl) }) })
                            }
                        }
                    }
                }
            }

            // 6. Artists (from API, fallback to song-derived)
            val displayArtists = if (apiArtists.isNotEmpty()) apiArtists.map { it.name } else songs.map { it.artist }.distinct()
            if (displayArtists.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Artists",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(displayArtists) { artistName ->
                                val artistSongs = songs.filter { it.artist == artistName }
                                val apiArtist = apiArtists.firstOrNull { it.name == artistName }
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(140.dp)
                                        .tvFocusable(CircleShape) {
                                            if (artistSongs.isNotEmpty()) {
                                                PlaybackManager.play(artistSongs.first(), artistSongs)
                                            }
                                        }
                                        .clip(CircleShape)
                                ) {
                                    if (apiArtist?.image != null) {
                                        AsyncImage(
                                            model = apiArtist.image,
                                            contentDescription = artistName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = artistName.take(2).uppercase(),
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 7. Albums (from API, fallback to category-simulated)
            if (apiAlbums.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Albums",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(apiAlbums) { album ->
                                val albumSongs = songs.filter { it.albumId == album.id || it.album == album.title }
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(160.dp)
                                        .tvFocusable(RoundedCornerShape(12.dp)) {
                                            if (albumSongs.isNotEmpty()) {
                                                PlaybackManager.play(albumSongs.first(), albumSongs)
                                            }
                                        }
                                        .clip(RoundedCornerShape(12.dp))
                                ) {
                                    if (album.cover != null) {
                                        AsyncImage(
                                            model = album.cover,
                                            contentDescription = album.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(Color(0xFF2C2C2C)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                                Icon(imageVector = Icons.Default.Album, contentDescription = album.title, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = album.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (categories.isNotEmpty()) {
                // Fallback: simulate albums from categories
                item {
                    Column {
                        Text(
                            text = "Albums",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(categories) { category ->
                                val albumSongs = songs.filter { it.category == category.name }
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(160.dp)
                                        .tvFocusable(RoundedCornerShape(12.dp)) {
                                            if (albumSongs.isNotEmpty()) {
                                                PlaybackManager.play(albumSongs.first(), albumSongs)
                                            }
                                        }
                                        .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                        Icon(imageVector = Icons.Default.Album, contentDescription = category.name, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "${category.name} Album", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 8. Categories
            if (categories.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "Categories",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(categories) { category ->
                                val catSongs = songs.filter { it.category == category.name }
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(80.dp)
                                        .tvFocusable(RoundedCornerShape(8.dp)) {
                                            if (catSongs.isNotEmpty()) {
                                                PlaybackManager.play(catSongs.first(), catSongs)
                                            }
                                        }
                                        .background(Color(0xFF333333), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 9. Trending Songs
            val trendingSongs = songs.filter { it.section?.contains("trending", ignoreCase = true) == true }.ifEmpty { songs.take(5) }
            item {
                Column {
                    Text(
                        text = "Trending Songs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(trendingSongs) { song ->
                            MovieCard(song = song, onClick = { PlaybackManager.play(song, trendingSongs) })
                        }
                    }
                }
            }

            // 10. Latest Songs
            val latestSongs = songs.filter { it.section?.contains("latest", ignoreCase = true) == true || it.section?.contains("recent", ignoreCase = true) == true }.ifEmpty { songs.takeLast(5) }
            item {
                Column {
                    Text(
                        text = "Latest Songs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(latestSongs) { song ->
                            MovieCard(song = song, onClick = { PlaybackManager.play(song, latestSongs) })
                        }
                    }
                }
            }

            // 11. Recommended Songs
            val recommendedSongs = songs.filter { it.section?.contains("recommended", ignoreCase = true) == true || it.section?.contains("suggested", ignoreCase = true) == true }.ifEmpty { songs.shuffled().take(5) }
            item {
                Column {
                    Text(
                        text = "Recommended Songs",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(recommendedSongs) { song ->
                            MovieCard(song = song, onClick = { PlaybackManager.play(song, recommendedSongs) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    song: Song,
    onClick: () -> Unit
) {
    val coverUrl = if (song.cover.startsWith("http")) song.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${song.cover.removePrefix("/")}"
    }

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(220.dp)
            .tvFocusable(RoundedCornerShape(12.dp), onClick = onClick)
            .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            AsyncImage(
                model = coverUrl,
                contentDescription = song.title,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
