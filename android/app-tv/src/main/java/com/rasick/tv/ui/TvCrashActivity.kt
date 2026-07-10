package com.rasick.tv.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.rasick.shared.data.DownloadDatabase
import com.rasick.tv.TvActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.system.exitProcess

class TvCrashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val errorLog = intent.getStringExtra("error_log") ?: "No logs captured."

        setContent {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                TvCrashScreen(
                    errorLog = errorLog,
                    onRestart = { restartApp() },
                    onClearAndRestart = { clearAndRestart() }
                )
            }
        }
    }

    private fun restartApp() {
        val intent = Intent(this, TvActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        exitProcess(0)
    }

    private fun clearAndRestart() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = DownloadDatabase.getDatabase(applicationContext)
                db.clearAllTables()
                filesDir.resolve("downloads").listFiles()?.forEach { file ->
                    file.delete()
                }
            } catch (e: Exception) {
                // Ignore clearing failures
            }
            restartApp()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvCrashScreen(
    errorLog: String,
    onRestart: () -> Unit,
    onClearAndRestart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "RasickEx TV Engine Crash Safety Recovery",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "An unexpected error caused the player engine to crash. Select an option below to recover.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorLog,
                    color = Color.Green,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
        ) {
            var clearFocused by remember { mutableStateOf(false) }
            Button(
                onClick = onClearAndRestart,
                modifier = Modifier
                    .width(240.dp)
                    .onFocusChanged { clearFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (clearFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.colors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.Red
                )
            ) {
                Text("Simulate Wipe & Restart")
            }

            var restartFocused by remember { mutableStateOf(false) }
            Button(
                onClick = onRestart,
                modifier = Modifier
                    .width(240.dp)
                    .onFocusChanged { restartFocused = it.isFocused }
                    .border(
                        width = 2.dp,
                        color = if (restartFocused) Color.White else Color.Transparent,
                        shape = RoundedCornerShape(20.dp)
                    ),
                colors = ButtonDefaults.colors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text("Restart Engine")
            }
        }
    }
}
