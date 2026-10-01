package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.SearchHistory
import com.rasick.shared.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSearchScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Song>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var voiceSearchActive by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember { DownloadDatabase.getDatabase(context) }
    val dao = remember { db.libraryDao() }

    val recentSearches by dao.getSearchHistory().collectAsState(initial = emptyList())

    val performSearch = { query: String ->
        if (query.isNotBlank()) {
            loading = true
            scope.launch(Dispatchers.IO) {
                // Save to history
                dao.insertSearch(SearchHistory(query = query.trim(), searchDate = System.currentTimeMillis()))
                try {
                    val response = RetrofitClient.songService.getSongs(query = query.trim())
                    results = response.songs
                } catch (e: Exception) {
                    results = emptyList()
                } finally {
                    loading = false
                }
            }
        } else {
            results = emptyList()
        }
    }

    LaunchedEffect(searchQuery) {
        delay(400)
        performSearch(searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Search Header Row
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Search Media Library",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Voice Search trigger
                Button(
                    onClick = {
                        scope.launch {
                            voiceSearchActive = true
                            delay(3000)
                            voiceSearchActive = false
                            // Fake voice search result
                            searchQuery = "chill"
                        }
                    },
                    modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                    colors = ButtonDefaults.colors(containerColor = Color(0xFF1E3A5F), contentColor = Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Search", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice Search")
                    }
                }

                Button(
                    onClick = onBack,
                    modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                    colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                ) {
                    Text("Back to Dashboard")
                }
            }
        }

        // Search Input (Focusable)
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Type song title, artist, or tag...", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { performSearch(searchQuery) }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.DarkGray
            )
        )

        // Recent Searches Row
        if (recentSearches.isNotEmpty()) {
            Text(text = "Recent Searches", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recentSearches, key = { it.query }) { item ->
                    var btnFocused by remember { mutableStateOf(false) }
                    Button(
                        onClick = { searchQuery = item.query },
                        modifier = Modifier
                            .onFocusChanged { btnFocused = it.isFocused }
                            .border(
                                width = 2.dp,
                                color = if (btnFocused) Color.White else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            ),
                        colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.query)
                            Spacer(modifier = Modifier.width(6.dp))
                            // Small delete button to remove search query from history
                            androidx.compose.material3.IconButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dao.deleteSearch(item.query)
                                    }
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }

        // Results Grid
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (results.isEmpty() && searchQuery.isNotBlank()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No results match \"$searchQuery\"", color = Color.Gray)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(results, key = { it.id }) { song ->
                        var cardFocused by remember { mutableStateOf(false) }
                        val coverUrl = if (song.cover.startsWith("http")) song.cover else {
                            val baseUrl = RetrofitClient.getBaseUrl().removeSuffix("/")
                            "$baseUrl/${song.cover.removePrefix("/")}"
                        }

                        Surface(
                            onClick = { PlaybackManager.play(song, results) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .onFocusChanged { cardFocused = it.isFocused }
                                .border(
                                    width = 2.dp,
                                    color = if (cardFocused) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = ClickableSurfaceDefaults.colors(
                                containerColor = Color(0xFF1E1E1E),
                                contentColor = Color.White
                            )
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
                }
            }
        }

        // Voice Search overlay
        if (voiceSearchActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Listening",
                        tint = Color.Red,
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Listening for voice search query...",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "Speak into your remote control microphone now.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
