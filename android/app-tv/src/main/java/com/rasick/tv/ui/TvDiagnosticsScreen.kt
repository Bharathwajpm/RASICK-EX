package com.rasick.tv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.rasick.shared.audio.DeviceCapabilities
import com.rasick.shared.audio.PlaybackManager

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvDiagnosticsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val isHdmi = remember { DeviceCapabilities.isHdmiConnected(context) }
    val hdmiEncodings = remember { DeviceCapabilities.getHdmiSupportedEncodings(context) }
    val supportedCodecs = remember { DeviceCapabilities.getSupportedCodecs() }

    val activeMime = PlaybackManager.activeMimeType
    val activeSampleRate = PlaybackManager.activeSampleRate
    val activeChannels = PlaybackManager.activeChannels
    val activeDecoder = PlaybackManager.activeDecoderName

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
            Column {
                Text(
                    text = "System Codec & Audio Diagnostics",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "ExoPlayer bitstream passthrough mapping profile",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            var backFocused by remember { mutableStateOf(false) }
            Button(
                onClick = onBack,
                modifier = Modifier
                    .onFocusChanged { backFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (backFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.colors(containerColor = Color.DarkGray, contentColor = Color.White)
            ) {
                Text("Back to Dashboard")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            // Left Column: Active Decoder & HDMI connection
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Active Decoder Panel
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.colors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Active Playback Diagnostic", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (activeMime != null) {
                            Text(text = "Format MIME: $activeMime", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            Text(text = "Sample Rate: $activeSampleRate Hz", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            Text(text = "Channels count: $activeChannels", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                            Text(text = "Active Decoder: ${activeDecoder ?: "Native Codec"}", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        } else {
                            Text(text = "Player Idle (Start playback to capture format metrics)", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                    }
                }

                // HDMI Port Status Panel
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.colors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "HDMI Audio Plug Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "HDMI Status: ${if (isHdmi) "Connected" else "Disconnected (Internal Speaker)"}", style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        if (isHdmi && hdmiEncodings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "HDMI supported direct bitstreams:", style = MaterialTheme.typography.bodyMedium, color = Color.LightGray)
                            hdmiEncodings.forEach { enc ->
                                Text(text = "• $enc", style = MaterialTheme.typography.bodySmall, color = Color.Green)
                            }
                        }
                    }
                }
            }

            // Right Column: System Audio Decoders List (Dolby, DTS, MP3, etc.)
            Column(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.colors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Text(text = "Registered MediaCodec Audio Decoders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.Cyan)
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val audioDecoders = supportedCodecs.filter {
                                it.mimeType.contains("ac3", ignoreCase = true) ||
                                it.mimeType.contains("dts", ignoreCase = true) ||
                                it.mimeType.contains("mpeg", ignoreCase = true) ||
                                it.mimeType.contains("mp4a", ignoreCase = true) ||
                                it.mimeType.contains("flac", ignoreCase = true)
                            }
                            if (audioDecoders.isEmpty()) {
                                item {
                                    Text(text = "No custom audio decoders found on system.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                                }
                            } else {
                                items(audioDecoders) { codec ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            val shortMime = codec.mimeType.substringAfter("audio/")
                                            Text(
                                                text = shortMime.uppercase(),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = codec.decoderName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        Text(
                                            text = if (codec.isHardware) "Hardware Accel" else "Software Decoder",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (codec.isHardware) Color.Green else Color.Yellow,
                                            fontWeight = FontWeight.Bold
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
}
