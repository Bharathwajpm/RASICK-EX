package com.rasick.tv.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.DownloadedSong
import com.rasick.shared.model.Category
import com.rasick.shared.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvHomeScreen(
    username: String,
    role: String,
    onLogout: () -> Unit,
    onNavigateToDownloads: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var debouncedQuery by remember { mutableStateOf("") }

    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val db = remember { DownloadDatabase.getDatabase(context) }
    val downloadsState by db.downloadedSongDao().getAllDownloadedSongs().collectAsState(initial = emptyList())

    // Debounce search query
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }

    val loadData = {
        loading = true
        errorMsg = ""
        scope.launch {
            try {
                // Fetch categories
                val catsResponse = RetrofitClient.songService.getCategories()
                categories = catsResponse.categories

                // Fetch songs
                val songsResponse = RetrofitClient.songService.getSongs(
                    category = selectedCategory,
                    query = debouncedQuery.ifBlank { null }
                )
                songs = songsResponse.songs
            } catch (e: Exception) {
                errorMsg = "Failed to fetch media from server."
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(selectedCategory, debouncedQuery) {
        loadData()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = if (PlaybackManager.currentSong != null) 120.dp else 0.dp)
        ) {
            // TV Header Row
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome to RasickEx TV",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Connected as $username ($role)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    var libFocused by remember { mutableStateOf(false) }
                    Button(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier
                            .onFocusChanged { libFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (libFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                    ) {
                        Text("My Library")
                    }

                    var downFocused by remember { mutableStateOf(false) }
                    Button(
                        onClick = onNavigateToDownloads,
                        modifier = Modifier
                            .onFocusChanged { downFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (downFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                    ) {
                        Text("Downloads")
                    }

                    var settingsFocused by remember { mutableStateOf(false) }
                    Button(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .onFocusChanged { settingsFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (settingsFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                    ) {
                        Text("Settings")
                    }

                    var logoutFocused by remember { mutableStateOf(false) }
                    Button(
                        onClick = onLogout,
                        modifier = Modifier
                            .onFocusChanged { logoutFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (logoutFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.colors(containerColor = Color.Red, contentColor = Color.White)
                    ) {
                        Text("Logout")
                    }
                }
            }

            // Search Input (Focusable)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs or categories...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            // Categories Horizontal Row
            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        var allFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = { selectedCategory = null },
                            modifier = Modifier
                                .onFocusChanged { allFocused = it.isFocused }
                                .border(
                                    width = 2.dp,
                                    color = if (allFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            colors = ButtonDefaults.colors(
                                containerColor = if (selectedCategory == null) Color.White else Color.DarkGray,
                                contentColor = if (selectedCategory == null) Color.Black else Color.White
                            )
                        ) {
                            Text("All")
                        }
                    }
                    items(categories, key = { it.id }) { category ->
                        var catFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                selectedCategory = if (selectedCategory == category.name) null else category.name
                            },
                            modifier = Modifier
                                .onFocusChanged { catFocused = it.isFocused }
                                .border(
                                    width = 2.dp,
                                    color = if (catFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            colors = ButtonDefaults.colors(
                                containerColor = if (selectedCategory == category.name) Color.White else Color.DarkGray,
                                contentColor = if (selectedCategory == category.name) Color.Black else Color.White
                            )
                        ) {
                            Text(category.name)
                        }
                    }
                }
            }

            // Song Lists / Loading / Errors
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (errorMsg.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = errorMsg, color = Color.Red)
                        Spacer(modifier = Modifier.height(12.dp))
                        var retryFocused by remember { mutableStateOf(false) }
                        Button(
                            onClick = { loadData() },
                            modifier = Modifier
                                .onFocusChanged { retryFocused = it.isFocused }
                                .border(
                                    width = 2.dp,
                                    color = if (retryFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(20.dp)
                                )
                        ) {
                            Text("Retry")
                        }
                    }
                } else if (songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "No songs found in this category", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(songs, key = { it.id }) { song ->
                            val download = downloadsState.find { it.id == song.id }
                            TvSongItem(
                                song = song,
                                onClick = { PlaybackManager.play(song, songs) },
                                downloadState = download,
                                onDownloadClick = {
                                    if (download == null || download.status == "FAILED" || download.status == "PAUSED") {
                                        DownloadManager.startDownload(song)
                                    } else if (download.status == "DOWNLOADING") {
                                        DownloadManager.pauseDownload(song.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating TV Player Widget
        if (PlaybackManager.currentSong != null) {
            TvPlayerWidget(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSongItem(
    song: Song,
    onClick: () -> Unit,
    downloadState: DownloadedSong?,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val coverUrl = if (song.cover.startsWith("http")) song.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${song.cover.removePrefix("/")}"
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = 2.dp,
                color = if (isFocused) Color.White else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = song.title,
                    modifier = Modifier
                        .size(60.dp)
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = song.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = song.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    song.category?.let {
                        Text(
                            text = "Category: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            // TV Focusable Download Button inside row
            var downloadButtonFocused by remember { mutableStateOf(false) }
            Button(
                onClick = onDownloadClick,
                modifier = Modifier
                    .onFocusChanged { downloadButtonFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (downloadButtonFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.colors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                )
            ) {
                when (downloadState?.status) {
                    "COMPLETED" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    "DOWNLOADING" -> {
                        CircularProgressIndicator(
                            progress = downloadState.progress.toFloat() / 100f,
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    "PAUSED" -> {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Paused",
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
