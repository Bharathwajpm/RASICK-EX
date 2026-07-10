package com.rasick.mobile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.model.Category
import com.rasick.shared.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeSubScreen by remember { mutableStateOf("dashboard") }

    when (activeSubScreen) {
        "dashboard" -> {
            AdminMainView(
                onNavigateToDiagnostics = onNavigateToDiagnostics,
                onNavigateToSongs = { activeSubScreen = "songs" },
                onNavigateToCategories = { activeSubScreen = "categories" },
                modifier = modifier
            )
        }
        "songs" -> {
            AdminSongsManager(
                onBack = { activeSubScreen = "dashboard" },
                modifier = modifier
            )
        }
        "categories" -> {
            AdminCategoriesManager(
                onBack = { activeSubScreen = "dashboard" },
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainView(
    onNavigateToDiagnostics: () -> Unit,
    onNavigateToSongs: () -> Unit,
    onNavigateToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var songsCount by remember { mutableStateOf(0) }
    var categoriesCount by remember { mutableStateOf(0) }
    var newestSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    val db = remember { DownloadDatabase.getDatabase(context) }
    val localDownloadsState by db.downloadedSongDao().getAllDownloadedSongs().collectAsState(initial = emptyList())
    val downloadsCount = localDownloadsState.size

    val loadStats = {
        loading = true
        scope.launch {
            try {
                val songsRes = RetrofitClient.songService.getSongs()
                songsCount = songsRes.songs.size
                newestSongs = songsRes.songs.take(5)

                val catsRes = RetrofitClient.songService.getCategories()
                categoriesCount = catsRes.categories.size
            } catch (e: Exception) {
                // Fetch fallback
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Console") },
                actions = {
                    IconButton(onClick = { loadStats() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Statistics Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        StatCard(label = "Total Tracks", count = songsCount.toString(), icon = Icons.Default.MusicNote, onClick = onNavigateToSongs)
                    }
                    item {
                        StatCard(label = "Categories", count = categoriesCount.toString(), icon = Icons.Default.Category, onClick = onNavigateToCategories)
                    }
                    item {
                        StatCard(label = "Downloads", count = downloadsCount.toString(), icon = Icons.Default.Download, onClick = {})
                    }
                    item {
                        StatCard(label = "System Users", count = "N/A (Local)", icon = Icons.Default.Person, onClick = {})
                    }
                }
            }

            // Quick diagnostics launcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDiagnostics() },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Device Information & Diagnostics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Query RAM, storage, CPU details, and audio encoders.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
            }

            // Recently Added Songs
            Text("Recently Added Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                if (newestSongs.isEmpty()) {
                    item {
                        Text("No tracks loaded from server.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                } else {
                    items(newestSongs) { song ->
                        val coverUrl = if (song.cover.startsWith("http")) song.cover else {
                            val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                            "$baseUrl/${song.cover.removePrefix("/")}"
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = coverUrl,
                                    contentDescription = song.title,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .aspectRatio(1f),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(song.title, fontWeight = FontWeight.Bold)
                                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    label: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSongsManager(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var songs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var selectedSongForDetail by remember { mutableStateOf<Song?>(null) }
    var songToDelete by remember { mutableStateOf<Song?>(null) }

    val fetchSongs = {
        loading = true
        scope.launch {
            try {
                val res = RetrofitClient.songService.getSongs()
                songs = res.songs
            } catch (e: Exception) {
                // Fetch fallback
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchSongs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tracks") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search songs...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filtered = songs.filter { it.title.contains(searchQuery, true) || it.artist.contains(searchQuery, true) }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { song ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { selectedSongForDetail = song }) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(song.title, fontWeight = FontWeight.Bold)
                                    Text(song.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { songToDelete = song }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedSongForDetail?.let { song ->
        val coverUrl = if (song.cover.startsWith("http")) song.cover else {
            val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
            "$baseUrl/${song.cover.removePrefix("/")}"
        }
        AlertDialog(
            onDismissRequest = { selectedSongForDetail = null },
            title = { Text(song.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AsyncImage(
                        model = coverUrl,
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        contentScale = ContentScale.Crop
                    )
                    Text("Artist: ${song.artist}")
                    Text("Category: ${song.category ?: "N/A"}")
                    Text("Duration: ${song.duration} seconds")
                    Text("Stream Path: ${song.audioUrl ?: "N/A"}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedSongForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }

    songToDelete?.let { song ->
        AlertDialog(
            onDismissRequest = { songToDelete = null },
            title = { Text("Delete Track") },
            text = { Text("Are you sure you want to delete ${song.title}? This is a local simulated action as the backend API is in read-only mode.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Mock deletion
                        songs = songs.filter { it.id != song.id }
                        songToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { songToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesManager(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    var selectedCategoryForDetail by remember { mutableStateOf<Category?>(null) }

    val fetchCategories = {
        loading = true
        scope.launch {
            try {
                val res = RetrofitClient.songService.getCategories()
                categories = res.categories
            } catch (e: Exception) {
                // Fetch fallback
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchCategories()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Categories") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search categories...") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filtered = categories.filter { it.name.contains(searchQuery, true) }
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { cat ->
                        Card(modifier = Modifier.fillMaxWidth().clickable { selectedCategoryForDetail = cat }) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(cat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }

    selectedCategoryForDetail?.let { cat ->
        AlertDialog(
            onDismissRequest = { selectedCategoryForDetail = null },
            title = { Text("Category Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category Name: ${cat.name}", fontWeight = FontWeight.Bold)
                    Text("ID: ${cat.id}")
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCategoryForDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}
