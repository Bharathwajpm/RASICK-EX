package com.rasick.mobile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.rasick.mobile.ui.*
import com.rasick.mobile.ui.theme.RasickMobileTheme
import com.rasick.shared.api.RetrofitClient
import com.rasick.shared.api.SessionManager
import com.rasick.shared.audio.DownloadManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.util.CentralLogger
import com.rasick.shared.util.CrashHandler

class MainActivity : ComponentActivity() {
    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        CentralLogger.initialize(applicationContext)
        CrashHandler.install(applicationContext, CrashActivity::class.java)

        val sessionManager = SessionManager(applicationContext)
        RetrofitClient.setAuthToken(sessionManager.getToken())

        PlaybackManager.initialize(applicationContext)
        DownloadManager.initialize(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
            var username by remember { mutableStateOf(sessionManager.getUsername() ?: "") }
            var role by remember { mutableStateOf(sessionManager.getUserRole() ?: "") }

            // Default to "admin" screen when logged in as admin
            var currentScreen by remember { mutableStateOf(if (role.equals("admin", ignoreCase = true)) "admin" else "home") }

            RasickMobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            if (isLoggedIn && (currentScreen == "home" || currentScreen == "library" || currentScreen == "settings" || currentScreen == "admin")) {
                                NavigationBar {
                                    if (role.equals("admin", ignoreCase = true)) {
                                        NavigationBarItem(
                                            selected = currentScreen == "admin",
                                            onClick = { currentScreen = "admin" },
                                            icon = { Icon(Icons.Default.SupervisorAccount, contentDescription = "Admin Console") },
                                            label = { Text("Admin Console") }
                                        )
                                    }
                                    NavigationBarItem(
                                        selected = currentScreen == "home",
                                        onClick = { currentScreen = "home" },
                                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                        label = { Text("Home") }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "library",
                                        onClick = { currentScreen = "library" },
                                        icon = { Icon(Icons.Default.LibraryMusic, contentDescription = "Library") },
                                        label = { Text("Library") }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == "settings",
                                        onClick = { currentScreen = "settings" },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text("Settings") }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            if (isLoggedIn) {
                                val bottomPadding = if (PlaybackManager.currentSong != null) 64.dp else 0.dp

                                when (currentScreen) {
                                    "admin" -> {
                                        AdminDashboard(
                                            onNavigateToDiagnostics = {
                                                currentScreen = "diagnostics"
                                            },
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                    "home" -> {
                                        HomeScreen(
                                            username = username,
                                            role = role,
                                            onLogout = {
                                                PlaybackManager.stop()
                                                sessionManager.clearSession()
                                                RetrofitClient.setAuthToken(null)
                                                isLoggedIn = false
                                                username = ""
                                                role = ""
                                                currentScreen = "home"
                                            },
                                            onNavigateToDownloads = {
                                                currentScreen = "downloads"
                                            },
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                    "library" -> {
                                        LibraryScreen(
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                    "settings" -> {
                                        SettingsScreen(
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                    "downloads" -> {
                                        DownloadsScreen(
                                            onBack = { currentScreen = "home" },
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                    "queue" -> {
                                        QueueScreen(
                                            onBack = { currentScreen = "home" },
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                    "diagnostics" -> {
                                        DeviceInfoScreen(
                                            onBack = { currentScreen = "admin" },
                                            modifier = Modifier.padding(bottom = bottomPadding)
                                        )
                                    }
                                }

                                // Floating Mini Player
                                if (PlaybackManager.currentSong != null) {
                                    MiniPlayer(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 8.dp)
                                    )
                                }
                            } else {
                                LoginScreen(
                                    onLoginSuccess = { user, userRole, token ->
                                        sessionManager.saveSession(user, userRole, token)
                                        username = user
                                        role = userRole
                                        isLoggedIn = true
                                        currentScreen = if (userRole.equals("admin", ignoreCase = true)) "admin" else "home"
                                    }
                                )
                            }

                            // Full Screen Slide Up Player Overlay
                            AnimatedVisibility(
                                visible = PlaybackManager.isExpanded && PlaybackManager.currentSong != null,
                                enter = slideInVertically(initialOffsetY = { it }),
                                exit = slideOutVertically(targetOffsetY = { it })
                            ) {
                                FullPlayer(
                                    onNavigateToQueue = {
                                        PlaybackManager.isExpanded = false
                                        currentScreen = "queue"
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackManager.release()
    }
}
