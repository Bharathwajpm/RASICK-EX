package com.rasick.mobile.ui

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rasick.shared.audio.DeviceCapabilities
import com.rasick.shared.net.NetworkMonitor
import com.rasick.shared.net.NetworkStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceInfoScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val networkMonitor = remember { NetworkMonitor(context) }
    val netStatus by networkMonitor.status.collectAsState(initial = NetworkStatus.OFFLINE)

    DisposableEffect(networkMonitor) {
        onDispose {
            networkMonitor.release()
        }
    }

    val ram = remember { DeviceCapabilities.getRamInfo(context) }
    val (totalStorage, freeStorage) = remember { DeviceCapabilities.getStorageInfo(context) }
    val (res, refreshRate) = remember { DeviceCapabilities.getDisplayInfo(context) }
    val cpu = remember { DeviceCapabilities.getCpuAbi() }
    val isHdmi = remember { DeviceCapabilities.isHdmiConnected(context) }
    val hdmiEncodings = remember { DeviceCapabilities.getHdmiSupportedEncodings(context) }
    val codecs = remember { DeviceCapabilities.getSupportedCodecs() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Device Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Platform specs
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Device Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(label = "Model", value = "${Build.MANUFACTURER} ${Build.MODEL}")
                        InfoRow(label = "Android Version", value = Build.VERSION.RELEASE)
                        InfoRow(label = "API Level", value = Build.VERSION.SDK_INT.toString())
                        InfoRow(label = "RAM Capacity", value = ram)
                        InfoRow(label = "Total Storage", value = totalStorage)
                        InfoRow(label = "Available Storage", value = freeStorage)
                        InfoRow(label = "CPU Architecture", value = cpu)
                        InfoRow(label = "Screen Resolution", value = res)
                        InfoRow(label = "Refresh Rate", value = refreshRate)
                    }
                }
            }

            // Connectivity
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Network & Sound Output", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        InfoRow(label = "Network Status", value = netStatus.name)
                        InfoRow(label = "HDMI Connected", value = if (isHdmi) "Yes" else "No")
                        if (isHdmi && hdmiEncodings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("HDMI Encodings:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            hdmiEncodings.forEach { enc ->
                                Text("• $enc", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            // Decoders List
            item {
                Text("Audio Decoders Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(codecs.filter {
                it.mimeType.contains("mpeg", true) ||
                it.mimeType.contains("ac3", true) ||
                it.mimeType.contains("dts", true) ||
                it.mimeType.contains("flac", true) ||
                it.mimeType.contains("mp4a", true)
            }) { codec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(codec.mimeType.substringAfter("audio/").uppercase(), fontWeight = FontWeight.Bold)
                            Text(codec.decoderName, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Text(
                            text = if (codec.isHardware) "Hardware" else "Software",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (codec.isHardware) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontWeight = FontWeight.Bold)
    }
}
