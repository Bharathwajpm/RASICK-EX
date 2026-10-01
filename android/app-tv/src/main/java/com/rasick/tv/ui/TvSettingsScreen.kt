package com.rasick.tv.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import androidx.compose.material3.CircularProgressIndicator
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.data.DownloadDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("settings_prefs", Context.MODE_PRIVATE) }

    var streamingQuality by remember { mutableStateOf(prefs.getString("streaming_quality", "High") ?: "High") }
    var downloadQuality by remember { mutableStateOf(prefs.getString("download_quality", "High") ?: "High") }
    var autoWifi by remember { mutableStateOf(prefs.getBoolean("auto_wifi", false)) }

    var cacheSize by remember { mutableStateOf(0L) }
    var isClearing by remember { mutableStateOf(false) }

    val refreshCache = {
        cacheSize = DownloadManager.getUsedStorageSize()
    }

    LaunchedEffect(Unit) {
        refreshCache()
    }

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
                text = "TV Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Button(
                onClick = onBack,
                modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
            ) {
                Text("Back to Dashboard")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Streaming Quality Row
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(RoundedCornerShape(8.dp)) {
                            val nextQuality = when (streamingQuality) {
                                "Low" -> "Medium"
                                "Medium" -> "High"
                                else -> "Low"
                            }
                            streamingQuality = nextQuality
                            prefs.edit().putString("streaming_quality", nextQuality).apply()
                        }
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Streaming Quality", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(text = "Click to cycle through formats.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(text = streamingQuality, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Download Quality Row
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(RoundedCornerShape(8.dp)) {
                            val nextQuality = when (downloadQuality) {
                                "Low" -> "Medium"
                                "Medium" -> "High"
                                else -> "Low"
                            }
                            downloadQuality = nextQuality
                            prefs.edit().putString("download_quality", nextQuality).apply()
                        }
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Download Quality", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(text = "Click to cycle through download encodes.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(text = downloadQuality, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Auto Wifi Switch
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(RoundedCornerShape(8.dp)) {
                            autoWifi = !autoWifi
                            prefs.edit().putBoolean("auto_wifi", autoWifi).apply()
                        }
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Auto-download on Wi-Fi", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(text = "Restrict caching tasks to wireless connections.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Switch(checked = autoWifi, onCheckedChange = null)
                    }
                }
            }

            // Clear Cache
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(RoundedCornerShape(8.dp)) {
                            if (!isClearing) {
                                isClearing = true
                                scope.launch(Dispatchers.IO) {
                                    val db = DownloadDatabase.getDatabase(context)
                                    context.filesDir.resolve("media3_cache").listFiles()?.forEach { file ->
                                        file.deleteRecursively()
                                    }
                                    db.clearAllTables()
                                    withContext(Dispatchers.Main) {
                                        refreshCache()
                                        isClearing = false
                                    }
                                }
                            }
                        }
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Clear Local Media Cache", style = MaterialTheme.typography.titleMedium, color = Color.White)
                            Text(
                                text = "Current size: ${formatSize(cacheSize)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                        if (isClearing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe", tint = Color.Red)
                        }
                    }
                }
            }

            // Info Block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "RasickEx TV Streaming Engine", style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text(text = "Version 1.0.0 (TV Production Build)", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(text = "Direct pass-through renderer enabled.", style = MaterialTheme.typography.labelSmall, color = Color.DarkGray)
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    val megabytes = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format("%.1f MB", megabytes)
}
