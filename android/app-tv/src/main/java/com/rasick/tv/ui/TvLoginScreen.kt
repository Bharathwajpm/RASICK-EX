package com.rasick.tv.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.rasick.shared.model.LoginRequest
import com.rasick.shared.api.RetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvLoginScreen(
    onLoginSuccess: (username: String, role: String, token: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isAdmin by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "RasickEx TV Portal",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Authenticate with your receiver account",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username", color = Color.White) },
            singleLine = true,
            modifier = Modifier.width(360.dp).padding(bottom = 12.dp),
            enabled = !loading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Color.White) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.width(360.dp).padding(bottom = 16.dp),
            enabled = !loading,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.Gray
            )
        )

        Row(
            modifier = Modifier.width(360.dp).padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { isAdmin = false },
                enabled = !loading,
                modifier = Modifier.weight(1f).padding(end = 6.dp),
                colors = ButtonDefaults.colors(
                    containerColor = if (!isAdmin) Color.White else Color.DarkGray,
                    contentColor = if (!isAdmin) Color.Black else Color.White
                )
            ) {
                Text("User Mode")
            }

            Button(
                onClick = { isAdmin = true },
                enabled = !loading,
                modifier = Modifier.weight(1f).padding(start = 6.dp),
                colors = ButtonDefaults.colors(
                    containerColor = if (isAdmin) Color.White else Color.DarkGray,
                    contentColor = if (isAdmin) Color.Black else Color.White
                )
            ) {
                Text("Admin Mode")
            }
        }

        if (loading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
        } else {
            Button(
                onClick = {
                    if (username.isBlank() || password.isBlank()) {
                        errorMessage = "Please enter both username and password."
                        return@Button
                    }
                    loading = true
                    errorMessage = ""
                    val role = if (isAdmin) "admin" else "user"
                    scope.launch {
                        try {
                            val response = RetrofitClient.authService.login(
                                LoginRequest(username.trim().lowercase(), password, role)
                            )
                            if (response.isSuccessful && response.body() != null) {
                                val envelope = response.body()!!
                                val data = envelope.data
                                val token = data?.token
                                val resUser = data?.username ?: username.trim().lowercase()
                                val resRole = data?.role ?: role

                                RetrofitClient.setAuthToken(token)
                                com.rasick.shared.audio.DownloadManager.refreshAuthHeaders()
                                onLoginSuccess(resUser, resRole, token)
                            } else {
                                val errorBody = response.errorBody()?.string()
                                val message = try {
                                    org.json.JSONObject(errorBody ?: "").getString("message")
                                } catch (e: Exception) {
                                    "Invalid Username or Password"
                                }
                                errorMessage = message
                            }
                        } catch (e: Exception) {
                            errorMessage = "Unable to connect to server. Is the backend running?"
                        } finally {
                            loading = false
                        }
                    }
                },
                modifier = Modifier.width(360.dp).height(48.dp)
            ) {
                Text("Sign In")
            }
        }
    }
}
