@file:OptIn(ExperimentalTvMaterial3Api::class)
package com.rasick.tv

import androidx.tv.material3.ExperimentalTvMaterial3Api

import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.api.SessionManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.data.Playlist
import com.rasick.shared.util.CentralLogger
import com.rasick.shared.util.CrashHandler
import com.rasick.tv.ui.*

class TvActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CentralLogger.initialize(applicationContext)
        CrashHandler.install(applicationContext, TvCrashActivity::class.java)
        
        val sessionManager = SessionManager(applicationContext)
        PlaybackManager.initialize(applicationContext)
        DownloadManager.initialize(applicationContext)

        // Restore auth token from persisted session so authenticated API calls
        // work after app restart without requiring re-login
        sessionManager.getToken()?.let { savedToken ->
            RetrofitClient.setAuthToken(savedToken)
            DownloadManager.refreshAuthHeaders()
        }

        setContent {
            var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
            var username by remember { mutableStateOf(sessionManager.getUsername() ?: "") }
            var role by remember { mutableStateOf(sessionManager.getUserRole() ?: "") }
            var currentScreen by remember { mutableStateOf("home") }
            var selectedPlaylist by remember { mutableStateOf<Playlist?>(null) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                if (isLoggedIn) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Top Tab Navigation Bar (Hidden in full-screen player)
                            if (currentScreen != "player") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF111111))
                                        .padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "RasickEx TV",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(end = 32.dp)
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        listOf(
                                            Triple("home", "Home", Icons.Default.Home),
                                            Triple("search", "Search", Icons.Default.Search),
                                            Triple("queue", "Queue", Icons.Default.Queue),
                                            Triple("downloads", "Downloads", Icons.Default.Download),
                                            Triple("library", "Library", Icons.Default.LibraryMusic),
                                            Triple("settings", "Settings", Icons.Default.Settings)
                                        ).forEach { (screen, label, icon) ->
                                            val isActive = currentScreen == screen
                                            Button(
                                                onClick = { currentScreen = screen },
                                                modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                                                colors = ButtonDefaults.colors(
                                                    containerColor = if (isActive) Color.White else Color.Transparent,
                                                    contentColor = if (isActive) Color.Black else Color.White
                                                )
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(label)
                                                }
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            PlaybackManager.stop()
                                            sessionManager.clearSession()
                                            RetrofitClient.setAuthToken(null)
                                            DownloadManager.refreshAuthHeaders()
                                            isLoggedIn = false
                                            username = ""
                                            role = ""
                                            currentScreen = "home"
                                        },
                                        modifier = Modifier.tvFocusable(RoundedCornerShape(20.dp)) {},
                                        colors = ButtonDefaults.colors(
                                            containerColor = Color.Red,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Logout")
                                        }
                                    }
                                }
                            }

                            // Active Screen Container
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(top = if (currentScreen != "player") 16.dp else 0.dp)
                                    .padding(bottom = if (PlaybackManager.currentSong != null && currentScreen != "player") 120.dp else 0.dp)
                            ) {
                                when (currentScreen) {
                                    "home" -> {
                                        TvHomeScreen(
                                            onNavigateToPlaylistDetail = { playlist ->
                                                selectedPlaylist = playlist
                                                currentScreen = "playlist_detail"
                                            }
                                        )
                                    }
                                    "search" -> {
                                        TvSearchScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }
                                    "queue" -> {
                                        TvQueueScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }
                                    "downloads" -> {
                                        TvDownloadsScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }
                                    "library" -> {
                                        TvLibraryScreen(
                                            onNavigateToPlaylistDetail = { playlist ->
                                                selectedPlaylist = playlist
                                                currentScreen = "playlist_detail"
                                            }
                                        )
                                    }
                                    "playlist_detail" -> {
                                        selectedPlaylist?.let { playlist ->
                                            TvPlaylistDetailScreen(
                                                playlist = playlist,
                                                onBack = { currentScreen = "library" }
                                            )
                                        }
                                    }
                                    "settings" -> {
                                        TvSettingsScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }
                                    "player" -> {
                                        TvPlayerScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }
                                }
                            }
                        }

                        // Floating TV Player Widget at the bottom
                        if (PlaybackManager.currentSong != null && currentScreen != "player") {
                            TvPlayerWidget(
                                onClick = { currentScreen = "player" },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(24.dp)
                            )
                        }
                    }
                } else {
                    TvLoginScreen(
                        onLoginSuccess = { user, userRole, token ->
                            sessionManager.saveSession(user, userRole, token)
                            username = user
                            role = userRole
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            KeyEvent.KEYCODE_MEDIA_PLAY,
            KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                PlaybackManager.togglePlay()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_NEXT -> {
                PlaybackManager.next()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                PlaybackManager.prev()
                return true
            }
            KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                PlaybackManager.seekTo((PlaybackManager.position + 10000).coerceAtMost(PlaybackManager.duration))
                return true
            }
            KeyEvent.KEYCODE_MEDIA_REWIND -> {
                PlaybackManager.seekTo((PlaybackManager.position - 10000).coerceAtLeast(0L))
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackManager.release()
    }
}
