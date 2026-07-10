package com.rasick.shared.api

import com.rasick.shared.model.CategoryListResponse
import com.rasick.shared.model.SongListResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SongService {
    @GET("api/songs")
    suspend fun getSongs(
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): SongListResponse

    @GET("api/categories")
    suspend fun getCategories(): CategoryListResponse
}
