package com.rasick.shared.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "admin"
)

data class LoginData(
    @SerializedName("token") val token: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("username") val username: String?
)

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?
)

typealias LoginResponse = ApiResponse<LoginData>

data class RegisterRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String
)
