package com.rasick.shared.api

import com.rasick.shared.model.LoginRequest
import com.rasick.shared.model.LoginResponse
import com.rasick.shared.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>
}
