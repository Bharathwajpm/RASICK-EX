package com.rasick.mobile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.data.DownloadDatabase
import com.rasick.shared.data.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember { SettingsRepository(context) }

    val theme by repository.themeFlow.collectAsState(initial = "System")
    val autoResume by repository.autoResumeFlow.collectAsState(initial = false)
    val autoDownloadWifi by repository.autoDownloadWifiFlow.collectAsState(initial = false)
    val streamingQuality by repository.streamingQualityFlow.collectAsState(initial = "High")

    var cacheSize by remember { mutableStateOf(0L) }

    val loadCacheSize = {
        cacheSize = DownloadManager.getUsedStorageSize()
    }

    LaunchedEffect(Unit) {
        loadCacheSize()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Quality Settings
        Text(text = "Audio Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Streaming Quality Selector
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Streaming Quality", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Currently set to $streamingQuality", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                var expandedStream by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expandedStream = true }) {
                        Text(streamingQuality)
                    }
                    DropdownMenu(expanded = expandedStream, onDismissRequest = { expandedStream = false }) {
                        listOf("Low", "Medium", "High").forEach { quality ->
                            DropdownMenuItem(
                                text = { Text(quality) },
                                onClick = {
                                    scope.launch {
                                        repository.setStreamingQuality(quality)
                                    }
                                    expandedStream = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preference Toggles
        Text(text = "Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        // Theme dropdown
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Application Theme", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Currently set to $theme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                var expandedTheme by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expandedTheme = true }) {
                        Text(theme)
                    }
                    DropdownMenu(expanded = expandedTheme, onDismissRequest = { expandedTheme = false }) {
                        listOf("Light", "Dark", "System").forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    scope.launch {
                                        repository.setTheme(t)
                                    }
                                    expandedTheme = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Auto Download Wifi
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Restrict caching to Wi-Fi", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = autoDownloadWifi,
                    onCheckedChange = {
                        scope.launch {
                            repository.setAutoDownloadWifi(it)
                        }
                    }
                )
            }
        }

        // Auto Resume Playback
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Auto-resume playback on startup", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = autoResume,
                    onCheckedChange = {
                        scope.launch {
                            repository.setAutoResume(it)
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Cache Management
        Text(text = "Cache Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Offline Media Cache", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Used size: ${formatSize(cacheSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            val db = DownloadDatabase.getDatabase(context)
                            
                            // Delete all cached files
                            context.filesDir.resolve("downloads").listFiles()?.forEach { file ->
                                file.delete()
                            }
                            
                            db.clearAllTables()
                            
                            withContext(Dispatchers.Main) {
                                loadCacheSize()
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Cache", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // About / App Version
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "RasickEx Music Streaming Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Version 1.0.0 (Build 1 - Production release)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "Built with high-fidelity Media3 surround protocols.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

private fun formatSize(bytes: Long): String {
    val megabytes = bytes.toDouble() / (1024.0 * 1024.0)
    return String.format("%.1f MB", megabytes)
}
