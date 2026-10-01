package com.rasick.mobile.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.model.Category
import com.rasick.shared.model.DashboardStats
import com.rasick.shared.model.Song
import com.rasick.shared.model.Artist
import com.rasick.shared.model.Album
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onNavigateToDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "Overview" to Icons.Default.Dashboard,
        "Upload Track" to Icons.Default.CloudUpload,
        "Tracks" to Icons.Default.MusicNote,
        "Categories" to Icons.Default.Category,
        "Artists" to Icons.Default.Person,
        "Albums" to Icons.Default.Album
    )

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            edgePadding = 12.dp,
            containerColor = Color(0xFF0F172A),
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, (title, icon) ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(imageVector = icon, contentDescription = title) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> AdminOverviewTab(onNavigateToDiagnostics = onNavigateToDiagnostics)
                1 -> AdminUploadStudioTab(onUploadSuccess = { activeTab = 2 })
                2 -> AdminSongsTab()
                3 -> AdminCategoriesTab()
                4 -> AdminArtistsTab()
                5 -> AdminAlbumsTab()
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 1: OVERVIEW & ANALYTICS DASHBOARD
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOverviewTab(
    onNavigateToDiagnostics: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var dashboardStats by remember { mutableStateOf<DashboardStats?>(null) }
    var newestSongs by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val db = remember { DownloadDatabase.getDatabase(context) }
    val localDownloadsState by db.downloadedSongDao().getAllDownloadedSongs().collectAsState(initial = emptyList())

    val fetchDashboard = {
        loading = true
        errorMessage = ""
        scope.launch {
            try {
                val res = RetrofitClient.adminService.getDashboardStats()
                if (res.success && res.data != null) {
                    dashboardStats = res.data!!.stats
                    newestSongs = res.data!!.songs.take(5)
                } else {
                    errorMessage = res.message ?: "Failed to fetch dashboard"
                }
            } catch (e: Exception) {
                // Fallback to song list if admin endpoint fails
                try {
                    val songRes = RetrofitClient.songService.getSongs()
                    newestSongs = songRes.songs.take(5)
                    dashboardStats = DashboardStats(
                        totalSongs = songRes.songs.size,
                        totalCategories = 4
                    )
                } catch (ex: Exception) {
                    errorMessage = "Network connection failed. Check backend URL."
                }
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchDashboard()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Fleet Console Overview", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (errorMessage.isEmpty()) "🟢 Backend Online (${RetrofitClient.getBaseUrl()})" else "🔴 Offline / Error",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (errorMessage.isEmpty()) Color(0xFF10B981) else Color(0xFFEF4444)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { fetchDashboard() }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val stats = dashboardStats ?: DashboardStats()

                // S3 Storage Capacity Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (stats.storage.warning) Color(0xFF451A03) else Color(0xFF1E293B)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (stats.storage.warning) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Filebase S3 Storage Bucket", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Text(
                                text = "${stats.storage.used} GB / ${stats.storage.capacity} GB",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = (stats.storage.percentage / 100.0).toFloat().coerceIn(0f, 1f),
                            modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                            color = if (stats.storage.warning) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${stats.storage.percentage}% Capacity Used",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "${stats.storage.remaining} GB Remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // Statistics Grid Cards
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false
                ) {
                    item {
                        StatMetricCard("Total Tracks", stats.totalSongs.toString(), Icons.Default.MusicNote, Color(0xFF6366F1))
                    }
                    item {
                        StatMetricCard("Categories", stats.totalCategories.toString(), Icons.Default.Category, Color(0xFF8B5CF6))
                    }
                    item {
                        StatMetricCard("Bus Offline Downloads", localDownloadsState.size.toString(), Icons.Default.Download, Color(0xFF10B981))
                    }
                    item {
                        StatMetricCard("Total Users", stats.totalUsers.toString(), Icons.Default.Group, Color(0xFFEC4899))
                    }
                }

                // Diagnostics Launcher Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDiagnostics() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("System Hardware Diagnostics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("Check RAM, CPU codecs, & Surround passthrough capability", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                // System Health Breakdown
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Services Health", fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
                        
                        HealthRow("Express Backend API", stats.systemHealth.backend)
                        HealthRow("MongoDB Atlas Database", stats.systemHealth.mongodb)
                        HealthRow("Filebase Private S3 Bucket", stats.systemHealth.filebase)
                        HealthRow("Audio Streaming 206 Proxy", stats.systemHealth.streamingService)
                        HealthRow("Upload Processing Service", stats.systemHealth.uploadService)
                    }
                }

                // Recently Uploaded Songs
                Text("Recently Added Tracks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                if (newestSongs.isEmpty()) {
                    Text("No tracks found in library.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        newestSongs.forEach { song ->
                            val coverUrl = if (song.cover.startsWith("http")) song.cover else {
                                val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                "$baseUrl/${song.cover.removePrefix("/")}"
                            }
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    AsyncImage(
                                        model = coverUrl,
                                        contentDescription = song.title,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(song.title, fontWeight = FontWeight.Bold)
                                        Text(song.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    }
                                    AssistChip(
                                        onClick = {},
                                        label = { Text(song.category ?: "General", fontSize = 11.sp) }
                                    )
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
fun StatMetricCard(label: String, count: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun HealthRow(label: String, status: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCBD5E1))
        Text(status, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// -----------------------------------------------------------------------------
// TAB 2: SONG UPLOAD STUDIO (Surround DTS/AC3/EAC3 + Fallback MP3)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUploadStudioTab(
    onUploadSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var selectedArtistId by remember { mutableStateOf("") }
    var selectedAlbumId by remember { mutableStateOf("") }
    var selectedAlbumTitle by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Folk") }
    var section by remember { mutableStateOf("latest") }

    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var coverFileName by remember { mutableStateOf("") }

    var surroundAudioUri by remember { mutableStateOf<Uri?>(null) }
    var surroundFileName by remember { mutableStateOf("") }

    var fallbackAudioUri by remember { mutableStateOf<Uri?>(null) }
    var fallbackFileName by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0f) }
    var uploadStatusText by remember { mutableStateOf("") }

    var apiArtists by remember { mutableStateOf<List<Artist>>(emptyList()) }
    var apiAlbums by remember { mutableStateOf<List<Album>>(emptyList()) }

    val categoriesList = listOf("Folk", "Temple", "DJ", "Perambalur", "Melody", "Cinematic", "Devotional")
    val sectionsList = listOf("latest" to "Latest Releases", "trending" to "Trending Now", "perambalur" to "Perambalur Specials", "recommended" to "Recommended")

    var categoryExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var artistExpanded by remember { mutableStateOf(false) }
    var albumExpanded by remember { mutableStateOf(false) }

    // Fetch artists and albums from API
    LaunchedEffect(Unit) {
        try {
            val artistResp = withContext(Dispatchers.IO) { RetrofitClient.adminService.getArtists() }
            apiArtists = artistResp.artists
        } catch (_: Exception) { }
        try {
            val albumResp = withContext(Dispatchers.IO) { RetrofitClient.adminService.getAlbums() }
            apiAlbums = albumResp.albums
        } catch (_: Exception) { }
    }

    val filteredAlbums = if (selectedArtistId.isNotBlank()) {
        apiAlbums.filter { it.artistId == selectedArtistId }
    } else {
        apiAlbums
    }

    // Cover Image Picker
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coverUri = it
            coverFileName = getFileNameFromUri(context, it) ?: "Selected Cover Image"
        }
    }

    // Audio File Picker (Surround or Main Audio)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            surroundAudioUri = it
            surroundFileName = getFileNameFromUri(context, it) ?: "Selected Audio File"
        }
    }

    // Fallback Audio File Picker
    val fallbackPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fallbackAudioUri = it
            fallbackFileName = getFileNameFromUri(context, it) ?: "Selected Fallback Audio"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Song Upload Studio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "Upload tracks directly to Filebase S3 Private Bucket with dual DTS/AC3 surround and MP3 fallback support.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        // Metadata Fields
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Track Title *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading
        )

        // Artist Selector Dropdown (API-backed)
        ExposedDropdownMenuBox(
            expanded = artistExpanded,
            onExpandedChange = { if (!isUploading) artistExpanded = !artistExpanded }
        ) {
            OutlinedTextField(
                value = artist.ifBlank { "Select Artist *" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Artist *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = artistExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isUploading
            )
            ExposedDropdownMenu(
                expanded = artistExpanded,
                onDismissRequest = { artistExpanded = false }
            ) {
                apiArtists.forEach { a ->
                    DropdownMenuItem(
                        text = { Text(a.name) },
                        onClick = {
                            artist = a.name
                            selectedArtistId = a.id
                            selectedAlbumId = ""
                            selectedAlbumTitle = ""
                            artistExpanded = false
                        }
                    )
                }
            }
        }

        // Album Selector Dropdown (filtered by artist)
        ExposedDropdownMenuBox(
            expanded = albumExpanded,
            onExpandedChange = { if (!isUploading) albumExpanded = !albumExpanded }
        ) {
            OutlinedTextField(
                value = selectedAlbumTitle.ifBlank { "No Album (optional)" },
                onValueChange = {},
                readOnly = true,
                label = { Text("Album") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = albumExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isUploading
            )
            ExposedDropdownMenu(
                expanded = albumExpanded,
                onDismissRequest = { albumExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("No Album") },
                    onClick = {
                        selectedAlbumId = ""
                        selectedAlbumTitle = ""
                        albumExpanded = false
                    }
                )
                filteredAlbums.forEach { al ->
                    DropdownMenuItem(
                        text = { Text(al.title) },
                        onClick = {
                            selectedAlbumId = al.id
                            selectedAlbumTitle = al.title
                            albumExpanded = false
                        }
                    )
                }
            }
        }

        // Category Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { if (!isUploading) categoryExpanded = !categoryExpanded }
        ) {
            OutlinedTextField(
                value = category,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isUploading
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                categoriesList.forEach { cat ->
                    DropdownMenuItem(
                        text = { Text(cat) },
                        onClick = {
                            category = cat
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        // Section Selector Dropdown
        ExposedDropdownMenuBox(
            expanded = sectionExpanded,
            onExpandedChange = { if (!isUploading) sectionExpanded = !sectionExpanded }
        ) {
            OutlinedTextField(
                value = sectionsList.firstOrNull { it.first == section }?.second ?: section,
                onValueChange = {},
                readOnly = true,
                label = { Text("Home Section *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                enabled = !isUploading
            )
            ExposedDropdownMenu(
                expanded = sectionExpanded,
                onDismissRequest = { sectionExpanded = false }
            ) {
                sectionsList.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            section = key
                            sectionExpanded = false
                        }
                    )
                }
            }
        }

        Divider()

        // Cover Image Upload Picker Card
        FileSelectionCard(
            title = "Cover Image (JPG / PNG)",
            fileName = coverFileName,
            isPicked = coverUri != null,
            icon = Icons.Default.Image,
            onPick = { coverPickerLauncher.launch("image/*") }
        )

        // Audio File Upload Picker Card
        FileSelectionCard(
            title = "Surround / Primary Audio (DTS, AC3, EAC3, MP3, WAV)",
            fileName = surroundFileName,
            isPicked = surroundAudioUri != null,
            icon = Icons.Default.Audiotrack,
            onPick = { audioPickerLauncher.launch("audio/*") }
        )

        // Fallback Audio File Picker Card (Optional)
        FileSelectionCard(
            title = "Fallback Browser Audio (MP3 / AAC - Optional)",
            fileName = fallbackFileName,
            isPicked = fallbackAudioUri != null,
            icon = Icons.Default.Subtitles,
            onPick = { fallbackPickerLauncher.launch("audio/*") }
        )

        if (isUploading) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(uploadStatusText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = uploadProgress,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Button(
                onClick = {
                    if (title.isBlank() || artist.isBlank() || coverUri == null || surroundAudioUri == null) {
                        Toast.makeText(context, "Title, Artist, Cover Image, and Audio file are required!", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    isUploading = true
                    uploadStatusText = "Preparing files for Filebase S3 upload..."
                    uploadProgress = 0.2f

                    scope.launch {
                        try {
                            val coverPart = createMultipartBodyPart(context, coverUri!!, "cover")
                            val audioPart = createMultipartBodyPart(context, surroundAudioUri!!, "audio")
                            val fallbackPart = if (fallbackAudioUri != null) {
                                createMultipartBodyPart(context, fallbackAudioUri!!, "fallback")
                            } else null

                            val titleBody = title.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                            val artistBody = artist.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                            val categoryBody = category.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                            val sectionBody = section.trim().toRequestBody("text/plain".toMediaTypeOrNull())
                            val albumBody = if (selectedAlbumTitle.isNotBlank()) selectedAlbumTitle.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
                            val albumIdBody = if (selectedAlbumId.isNotBlank()) selectedAlbumId.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null

                            uploadStatusText = "Uploading streams to S3 server..."
                            uploadProgress = 0.6f

                            val response = RetrofitClient.adminService.uploadSong(
                                title = titleBody,
                                artist = artistBody,
                                category = categoryBody,
                                section = sectionBody,
                                cover = coverPart,
                                audio = audioPart,
                                fallback = fallbackPart,
                                album = albumBody,
                                albumId = albumIdBody
                            )

                            uploadProgress = 1.0f

                            if (response.success) {
                                Toast.makeText(context, "Song uploaded successfully to Filebase S3!", Toast.LENGTH_LONG).show()
                                title = ""
                                artist = ""
                                selectedArtistId = ""
                                selectedAlbumId = ""
                                selectedAlbumTitle = ""
                                coverUri = null
                                coverFileName = ""
                                surroundAudioUri = null
                                surroundFileName = ""
                                fallbackAudioUri = null
                                fallbackFileName = ""
                                onUploadSuccess()
                            } else {
                                Toast.makeText(context, "Upload failed: ${response.message}", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Upload exception: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isUploading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Track to Fleet", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FileSelectionCard(
    title: String,
    fileName: String,
    isPicked: Boolean,
    icon: ImageVector,
    onPick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onPick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPicked) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = if (isPicked) fileName else "Tap to choose file",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPicked) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            if (isPicked) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Picked", tint = Color(0xFF10B981))
            } else {
                Button(onClick = onPick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("Select")
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// TAB 3: TRACKS MANAGEMENT (Search, Edit, Delete)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSongsTab() {
    val context = LocalContext.current
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
                Toast.makeText(context, "Failed to load songs", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchSongs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tracks by title, artist, or category...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val filtered = songs.filter {
                it.title.contains(searchQuery, true) ||
                it.artist.contains(searchQuery, true) ||
                (it.category?.contains(searchQuery, true) == true)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { song ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { selectedSongForDetail = song }) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val coverUrl = if (song.cover.startsWith("http")) song.cover else {
                                val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                                "$baseUrl/${song.cover.removePrefix("/")}"
                            }
                            AsyncImage(
                                model = coverUrl,
                                contentDescription = song.title,
                                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(song.title, fontWeight = FontWeight.Bold)
                                Text(song.artist, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                            IconButton(onClick = { songToDelete = song }) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                            }
                        }
                    }
                }
            }
        }
    }

    selectedSongForDetail?.let { song ->
        AlertDialog(
            onDismissRequest = { selectedSongForDetail = null },
            title = { Text(song.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Artist: ${song.artist}")
                    Text("Category: ${song.category ?: "General"}")
                    Text("Section: ${song.section ?: "latest"}")
                    Text("Duration: ${song.duration}")
                    Text("Play Count: ${song.playCount}")
                    Text("Download Count: ${song.downloadCount}")
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
            text = { Text("Are you sure you want to delete '${song.title}' from the cloud catalog?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            try {
                                val res = RetrofitClient.adminService.deleteSong(song.id)
                                if (res.success) {
                                    Toast.makeText(context, "Track deleted", Toast.LENGTH_SHORT).show()
                                    songs = songs.filter { it.id != song.id }
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                songToDelete = null
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444))
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

// -----------------------------------------------------------------------------
// TAB 4: CATEGORIES MANAGEMENT
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<Category>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }

    var showCreateDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val fetchCategories = {
        loading = true
        scope.launch {
            try {
                val res = RetrofitClient.songService.getCategories()
                categories = res.categories
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load categories", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchCategories()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Category Taxonomy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { cat ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(cat.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text("${cat.count} tracks") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Category") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.adminService.createCategory(mapOf("name" to newCategoryName.trim()))
                                    if (res.success && res.data != null) {
                                        Toast.makeText(context, "Category created", Toast.LENGTH_SHORT).show()
                                        fetchCategories()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Creation failed", Toast.LENGTH_SHORT).show()
                                } finally {
                                    showCreateDialog = false
                                    newCategoryName = ""
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
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

// -----------------------------------------------------------------------------
// TAB 5: ARTISTS MANAGEMENT (Real API CRUD)
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminArtistsTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var artists by remember { mutableStateOf<List<com.rasick.shared.model.Artist>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newArtistName by remember { mutableStateOf("") }
    var newArtistImage by remember { mutableStateOf("") }
    var artistToDelete by remember { mutableStateOf<com.rasick.shared.model.Artist?>(null) }

    val fetchArtists = {
        loading = true
        scope.launch {
            try {
                val res = RetrofitClient.adminService.getArtists()
                artists = res.artists
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load artists", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { fetchArtists() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Artist")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Artist Directory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(artists) { artist ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (artist.image != null) {
                                    AsyncImage(
                                        model = artist.image,
                                        contentDescription = artist.name,
                                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(artist.name, fontWeight = FontWeight.Bold)
                                    Text("${artist.songCount} tracks", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { artistToDelete = artist }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Artist") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newArtistName,
                        onValueChange = { newArtistName = it },
                        label = { Text("Artist Name *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newArtistImage,
                        onValueChange = { newArtistImage = it },
                        label = { Text("Image URL (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newArtistName.isNotBlank()) {
                            scope.launch {
                                try {
                                    val body = mutableMapOf("name" to newArtistName.trim())
                                    if (newArtistImage.isNotBlank()) body["image"] = newArtistImage.trim()
                                    val res = RetrofitClient.adminService.createArtist(body)
                                    if (res.success) {
                                        Toast.makeText(context, "Artist created", Toast.LENGTH_SHORT).show()
                                        fetchArtists()
                                    } else {
                                        Toast.makeText(context, res.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    showCreateDialog = false
                                    newArtistName = ""
                                    newArtistImage = ""
                                }
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newArtistName = ""; newArtistImage = "" }) { Text("Cancel") }
            }
        )
    }

    artistToDelete?.let { artist ->
        AlertDialog(
            onDismissRequest = { artistToDelete = null },
            title = { Text("Delete Artist") },
            text = { Text("Are you sure you want to delete '${artist.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val res = RetrofitClient.adminService.deleteArtist(artist.id)
                            if (res.success) {
                                Toast.makeText(context, "Artist deleted", Toast.LENGTH_SHORT).show()
                                fetchArtists()
                            } else {
                                Toast.makeText(context, res.message ?: "Cannot delete", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            artistToDelete = null
                        }
                    }
                }) { Text("Delete", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { artistToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// TAB 6: ALBUMS MANAGEMENT
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAlbumsTab() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var albums by remember { mutableStateOf<List<com.rasick.shared.model.Album>>(emptyList()) }
    var artists by remember { mutableStateOf<List<com.rasick.shared.model.Artist>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newAlbumTitle by remember { mutableStateOf("") }
    var selectedArtistId by remember { mutableStateOf("") }
    var albumToDelete by remember { mutableStateOf<com.rasick.shared.model.Album?>(null) }

    val fetchData = {
        loading = true
        scope.launch {
            try {
                albums = RetrofitClient.adminService.getAlbums().albums
                artists = RetrofitClient.adminService.getArtists().artists
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load albums", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) { fetchData() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Album")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text("Album Directory", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(albums) { album ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Album, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(album.title, fontWeight = FontWeight.Bold)
                                    Text(album.artistName ?: "Unknown", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                    Text("${album.songCount} tracks", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                                IconButton(onClick = { albumToDelete = album }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var artistExpanded by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Album") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newAlbumTitle,
                        onValueChange = { newAlbumTitle = it },
                        label = { Text("Album Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    ExposedDropdownMenuBox(
                        expanded = artistExpanded,
                        onExpandedChange = { artistExpanded = !artistExpanded }
                    ) {
                        val selectedName = artists.firstOrNull { it.id == selectedArtistId }?.name ?: ""
                        OutlinedTextField(
                            value = selectedName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Artist *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = artistExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = artistExpanded,
                            onDismissRequest = { artistExpanded = false }
                        ) {
                            artists.forEach { a ->
                                DropdownMenuItem(
                                    text = { Text(a.name) },
                                    onClick = {
                                        selectedArtistId = a.id
                                        artistExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAlbumTitle.isNotBlank() && selectedArtistId.isNotBlank()) {
                            scope.launch {
                                try {
                                    val res = RetrofitClient.adminService.createAlbum(
                                        mapOf("title" to newAlbumTitle.trim(), "artistId" to selectedArtistId)
                                    )
                                    if (res.success) {
                                        Toast.makeText(context, "Album created", Toast.LENGTH_SHORT).show()
                                        fetchData()
                                    } else {
                                        Toast.makeText(context, res.message ?: "Failed", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    showCreateDialog = false
                                    newAlbumTitle = ""
                                    selectedArtistId = ""
                                }
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newAlbumTitle = ""; selectedArtistId = "" }) { Text("Cancel") }
            }
        )
    }

    albumToDelete?.let { album ->
        AlertDialog(
            onDismissRequest = { albumToDelete = null },
            title = { Text("Delete Album") },
            text = { Text("Are you sure you want to delete '${album.title}'? Songs will be unlinked, not deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        try {
                            val res = RetrofitClient.adminService.deleteAlbum(album.id)
                            if (res.success) {
                                Toast.makeText(context, "Album deleted", Toast.LENGTH_SHORT).show()
                                fetchData()
                            } else {
                                Toast.makeText(context, res.message ?: "Cannot delete", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            albumToDelete = null
                        }
                    }
                }) { Text("Delete", color = Color(0xFFEF4444)) }
            },
            dismissButton = {
                TextButton(onClick = { albumToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

// -----------------------------------------------------------------------------
// HELPER FUNCTIONS FOR FILE MULTIPART CONVERSION
// -----------------------------------------------------------------------------
fun getFileNameFromUri(context: Context, uri: Uri): String? {
    var name: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) name = it.getString(index)
            }
        }
    }
    if (name == null) {
        name = uri.path
        val cut = name?.lastIndexOf('/')
        if (cut != null && cut != -1) {
            name = name?.substring(cut + 1)
        }
    }
    return name
}

suspend fun createMultipartBodyPart(context: Context, uri: Uri, partName: String): MultipartBody.Part = withContext(Dispatchers.IO) {
    val contentResolver = context.contentResolver
    val fileName = getFileNameFromUri(context, uri) ?: "upload_file"
    val tempFile = File(context.cacheDir, fileName)

    contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(tempFile).use { output ->
            input.copyTo(output)
        }
    }

    val mimeType = contentResolver.getType(uri) ?: "application/octet-stream"
    val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())
    MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
}
