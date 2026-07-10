package com.rasick.shared.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("category") val category: String?,
    @SerializedName("section") val section: String?,
    @SerializedName("audioUrl") val audioUrl: String?
)

data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("count") val count: Int,
    @SerializedName("color") val color: String
)

data class SongListResponse(
    @SerializedName("songs") val songs: List<Song>
)

data class CategoryListResponse(
    @SerializedName("categories") val categories: List<Category>
)
