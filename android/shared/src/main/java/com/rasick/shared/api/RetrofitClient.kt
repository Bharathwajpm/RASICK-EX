package com.rasick.shared.api

import com.rasick.shared.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var baseUrl: String = BuildConfig.BACKEND_URL

    fun setBaseUrl(url: String) {
        baseUrl = if (url.endsWith("/")) url else "$url/"
        retrofitInstance = null
        authServiceInstance = null
        songServiceInstance = null
    }

    fun getBaseUrl(): String = baseUrl

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
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
}
