package com.rasick.shared.api

import com.rasick.shared.model.AlbumListResponse
import com.rasick.shared.model.ApiResponse
import com.rasick.shared.model.ArtistListResponse
import com.rasick.shared.model.Category
import com.rasick.shared.model.DashboardData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface AdminService {
    @GET("api/admin/dashboard")
    suspend fun getDashboardStats(): ApiResponse<DashboardData>

    @Multipart
    @POST("api/songs")
    suspend fun uploadSong(
        @Part("title") title: RequestBody,
        @Part("artist") artist: RequestBody,
        @Part("category") category: RequestBody,
        @Part("section") section: RequestBody,
        @Part cover: MultipartBody.Part?,
        @Part audio: MultipartBody.Part?,
        @Part fallback: MultipartBody.Part? = null,
        @Part("album") album: RequestBody? = null,
        @Part("albumId") albumId: RequestBody? = null
    ): ApiResponse<Map<String, Any>>

    @PUT("api/songs/{id}")
    suspend fun updateSong(
        @Path("id") id: String,
        @Body updates: Map<String, String>
    ): ApiResponse<Map<String, Any>>

    @DELETE("api/songs/{id}")
    suspend fun deleteSong(
        @Path("id") id: String
    ): ApiResponse<Map<String, Any>>

    @POST("api/categories")
    suspend fun createCategory(
        @Body category: Map<String, String>
    ): ApiResponse<Category>

    @PUT("api/categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body category: Map<String, String>
    ): ApiResponse<Category>

    @DELETE("api/categories/{id}")
    suspend fun deleteCategory(
        @Path("id") id: String
    ): ApiResponse<Map<String, Any>>

    // Artist management
    @GET("api/artists")
    suspend fun getArtists(): ArtistListResponse

    @POST("api/artists")
    suspend fun createArtist(
        @Body body: Map<String, String>
    ): ApiResponse<Map<String, Any>>

    @PUT("api/artists/{id}")
    suspend fun updateArtist(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiResponse<Map<String, Any>>

    @DELETE("api/artists/{id}")
    suspend fun deleteArtist(
        @Path("id") id: String
    ): ApiResponse<Map<String, Any>>

    // Album management
    @GET("api/albums")
    suspend fun getAlbums(
        @Query("artistId") artistId: String? = null
    ): AlbumListResponse

    @POST("api/albums")
    suspend fun createAlbum(
        @Body body: Map<String, String>
    ): ApiResponse<Map<String, Any>>

    @PUT("api/albums/{id}")
    suspend fun updateAlbum(
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): ApiResponse<Map<String, Any>>

    @DELETE("api/albums/{id}")
    suspend fun deleteAlbum(
        @Path("id") id: String
    ): ApiResponse<Map<String, Any>>
}
