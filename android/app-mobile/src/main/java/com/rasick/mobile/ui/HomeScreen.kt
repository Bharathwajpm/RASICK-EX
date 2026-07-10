package com.rasick.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.*
import com.rasick.shared.model.Category
import com.rasick.shared.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    username: String,
    role: String,
    onLogout: () -> Unit,
    onNavigateToDownloads: () -> Unit,
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
    val pullToRefreshState = rememberPullToRefreshState()

    val context = LocalContext.current
    val db = remember { DownloadDatabase.getDatabase(context) }
    val downloadsState by db.downloadedSongDao().getAllDownloadedSongs().collectAsState(initial = emptyList())
    val libraryDao = remember { db.libraryDao() }
    val searchHistoryState by libraryDao.getSearchHistory().collectAsState(initial = emptyList())

    var isSearchFocused by remember { mutableStateOf(false) }

    // Debounce search input
    LaunchedEffect(searchQuery) {
        delay(300)
        debouncedQuery = searchQuery
    }

    val loadData = { isRefreshing: Boolean ->
        if (!isRefreshing) {
            loading = true
        }
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
                errorMsg = "Failed to load music from server."
            } finally {
                loading = false
                if (isRefreshing) {
                    pullToRefreshState.endRefresh()
                }
            }
        }
    }

    LaunchedEffect(debouncedQuery) {
        if (debouncedQuery.isNotBlank()) {
            scope.launch(Dispatchers.IO) {
                libraryDao.insertSearch(
                    SearchHistory(
                        query = debouncedQuery,
                        searchDate = System.currentTimeMillis()
                    )
                )
            }
        }
        loadData(false)
    }

    LaunchedEffect(selectedCategory) {
        loadData(false)
    }

    if (pullToRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            loadData(true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(pullToRefreshState.nestedScrollConnection)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Vanakkam 👋",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$username ($role)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateToDownloads) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Downloads"
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs or categories...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Input")
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Search Suggestions overlay
            if (searchQuery.isEmpty() && searchHistoryState.isNotEmpty()) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(searchHistoryState.take(5)) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { searchQuery = item.query }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.History, contentDescription = null, tint = Color.Gray)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = item.query, style = MaterialTheme.typography.bodyLarge)
                            }
                            IconButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        libraryDao.deleteSearch(item.query)
                                    }
                                }
                            ) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Delete", tint = Color.Gray)
                            }
                        }
                    }
                    item {
                        TextButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    libraryDao.clearSearchHistory()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Clear Search History", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Categories horizontal row
            if (categories.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All") }
                        )
                    }
                    items(categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = selectedCategory == category.name,
                            onClick = {
                                selectedCategory = if (selectedCategory == category.name) null else category.name
                            },
                            label = { Text(category.name) }
                        )
                    }
                }
            }

            // Song List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (errorMsg.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadData(false) }) {
                            Text("Retry")
                        }
                    }
                } else if (songs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No songs found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(songs, key = { it.id }) { song ->
                            val download = downloadsState.find { it.id == song.id }
                            SongItem(
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

        // Pull to refresh overlay indicator
        PullToRefreshContainer(
            state = pullToRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
fun SongItem(
    song: Song,
    onClick: () -> Unit,
    downloadState: DownloadedSong?,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coverUrl = if (song.cover.startsWith("http")) song.cover else {
        val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
        "$baseUrl/${song.cover.removePrefix("/")}"
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
                contentDescription = song.title,
                modifier = Modifier
                    .size(64.dp)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = song.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                song.category?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = it,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Download Trigger Icon Button
            IconButton(onClick = onDownloadClick) {
                when (downloadState?.status) {
                    "COMPLETED" -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Downloaded",
                            tint = Color.Green
                        )
                    }
                    "DOWNLOADING" -> {
                        CircularProgressIndicator(
                            progress = downloadState.progress.toFloat() / 100f,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    "PAUSED" -> {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Paused",
                            tint = Color.Yellow
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download"
                        )
                    }
                }
            }
        }
    }
}
