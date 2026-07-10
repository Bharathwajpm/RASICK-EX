package com.rasick.shared.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String
)

data class LoginResponse(
    @SerializedName("username") val username: String,
    @SerializedName("role") val role: String
)
