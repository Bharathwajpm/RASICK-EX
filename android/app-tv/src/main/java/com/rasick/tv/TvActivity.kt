package com.rasick.tv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rasick.shared.api.SessionManager
import com.rasick.shared.audio.PlaybackManager
import com.rasick.shared.audio.DownloadManager
import com.rasick.tv.ui.TvHomeScreen
import com.rasick.tv.ui.TvLoginScreen
import com.rasick.tv.ui.TvDownloadsScreen
import com.rasick.tv.ui.TvLibraryScreen
import com.rasick.tv.ui.TvSettingsScreen
import com.rasick.tv.ui.TvCrashActivity
import com.rasick.shared.util.CentralLogger
import com.rasick.shared.util.CrashHandler

class TvActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CentralLogger.initialize(applicationContext)
        CrashHandler.install(applicationContext, TvCrashActivity::class.java)
        
        val sessionManager = SessionManager(applicationContext)
        PlaybackManager.initialize(applicationContext)
        DownloadManager.initialize(applicationContext)

        setContent {
            var isLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
            var username by remember { mutableStateOf(sessionManager.getUsername() ?: "") }
            var role by remember { mutableStateOf(sessionManager.getUserRole() ?: "") }
            var currentScreen by remember { mutableStateOf("home") }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                if (isLoggedIn) {
                    when (currentScreen) {
                        "home" -> {
                            TvHomeScreen(
                                username = username,
                                role = role,
                                onLogout = {
                                    PlaybackManager.stop()
                                    sessionManager.clearSession()
                                    isLoggedIn = false
                                    username = ""
                                    role = ""
                                    currentScreen = "home"
                                },
                                onNavigateToDownloads = {
                                    currentScreen = "downloads"
                                },
                                onNavigateToLibrary = {
                                    currentScreen = "library"
                                },
                                onNavigateToSettings = {
                                    currentScreen = "settings"
                                }
                            )
                        }
                        "downloads" -> {
                            TvDownloadsScreen(
                                onBack = { currentScreen = "home" }
                            )
                        }
                        "library" -> {
                            TvLibraryScreen(
                                onBack = { currentScreen = "home" }
                            )
                        }
                        "settings" -> {
                            TvSettingsScreen(
                                onBack = { currentScreen = "home" }
                            )
                        }
                    }
                } else {
                    TvLoginScreen(
                        onLoginSuccess = { user, userRole ->
                            sessionManager.saveSession(user, userRole)
                            username = user
                            role = userRole
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackManager.release()
    }
}
