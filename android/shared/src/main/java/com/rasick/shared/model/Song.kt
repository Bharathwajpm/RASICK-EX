package com.rasick.shared.model

import com.google.gson.annotations.SerializedName

data class Song(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artist") val artist: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("duration") val duration: String,
    @SerializedName("category") val category: String? = null,
    @SerializedName("album") val album: String? = null,
    @SerializedName("albumId") val albumId: String? = null,
    @SerializedName("section") val section: String? = null,
    @SerializedName("audioUrl") val audioUrl: String? = null,
    @SerializedName("surroundUrl") val surroundUrl: String? = null,
    @SerializedName("fallbackUrl") val fallbackUrl: String? = null,
    @SerializedName("audioSizeBytes") val audioSizeBytes: Long = 0,
    @SerializedName("coverSizeBytes") val coverSizeBytes: Long = 0,
    @SerializedName("playCount") val playCount: Int = 0,
    @SerializedName("downloadCount") val downloadCount: Int = 0,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class Category(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("color") val color: String = "#6366F1"
)

data class Artist(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String? = null,
    @SerializedName("songCount") val songCount: Int = 0
)

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("artistId") val artistId: String? = null,
    @SerializedName("artistName") val artistName: String? = null,
    @SerializedName("cover") val cover: String? = null,
    @SerializedName("releaseYear") val releaseYear: Int? = null,
    @SerializedName("songCount") val songCount: Int = 0
)

data class SongListResponse(
    @SerializedName("songs") val songs: List<Song>
)

data class CategoryListResponse(
    @SerializedName("categories") val categories: List<Category>
)

data class ArtistListResponse(
    @SerializedName("artists") val artists: List<Artist>
)

data class AlbumListResponse(
    @SerializedName("albums") val albums: List<Album>
)
