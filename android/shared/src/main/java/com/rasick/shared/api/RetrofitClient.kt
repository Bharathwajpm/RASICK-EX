package com.rasick.shared.api

import com.rasick.shared.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var baseUrl: String = BuildConfig.BACKEND_URL
    private var authToken: String? = null

    fun setBaseUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
        resetInstances()
    }

    fun setAuthToken(token: String?) {
        authToken = token
    }

    fun getBaseUrl(): String = baseUrl

    fun getAuthToken(): String? = authToken

    private fun resetInstances() {
        retrofitInstance = null
        authServiceInstance = null
        songServiceInstance = null
        adminServiceInstance = null
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val requestBuilder = original.newBuilder()
        authToken?.let {
            if (it.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $it")
            }
        }
        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private var retrofitInstance: Retrofit? = null

    private fun getRetrofit(): Retrofit {
        return retrofitInstance ?: synchronized(this) {
            val instance = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            retrofitInstance = instance
            instance
        }
    }

    private var authServiceInstance: AuthService? = null

    val authService: AuthService
        get() = authServiceInstance ?: synchronized(this) {
            val service = getRetrofit().create(AuthService::class.java)
            authServiceInstance = service
            service
        }

    private var songServiceInstance: SongService? = null

    val songService: SongService
        get() = songServiceInstance ?: synchronized(this) {
            val service = getRetrofit().create(SongService::class.java)
            songServiceInstance = service
            service
        }

    private var adminServiceInstance: AdminService? = null

    val adminService: AdminService
        get() = adminServiceInstance ?: synchronized(this) {
            val service = getRetrofit().create(AdminService::class.java)
            adminServiceInstance = service
            service
        }
}
