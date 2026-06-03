package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CompactMedia(
    val id: Int,
    val title: String,
    val poster_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val release_date: String?,
    val media_type: String, // "movie" or "tv"
    val savedAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class Genre(
    val id: Int,
    val name: String
)

@JsonClass(generateAdapter = true)
data class GenreListResponse(
    val genres: List<Genre>
)

@JsonClass(generateAdapter = true)
data class TmdbMedia(
    val id: Int,
    val title: String?,
    val name: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val overview: String?,
    val vote_average: Double?,
    val vote_count: Int?,
    val release_date: String?,
    val first_air_date: String?,
    val media_type: String?
) {
    fun toCompactMedia(inferredType: String): CompactMedia {
        return CompactMedia(
            id = id,
            title = title ?: name ?: "Unknown",
            poster_path = poster_path,
            vote_average = vote_average ?: 0.0,
            vote_count = vote_count ?: 0,
            release_date = release_date ?: first_air_date,
            media_type = media_type ?: inferredType
        )
    }
}

@JsonClass(generateAdapter = true)
data class TmdbResponse(
    val results: List<TmdbMedia>,
    val page: Int?,
    val total_pages: Int?,
    val total_results: Int?
)

@JsonClass(generateAdapter = true)
data class MovieDetail(
    val id: Int,
    val title: String,
    val overview: String?,
    val release_date: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val runtime: Int?,
    val genres: List<Genre>?,
    val videos: VideoResponse?
) {
    fun toCompactMedia(): CompactMedia {
        return CompactMedia(
            id = id,
            title = title,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            release_date = release_date,
            media_type = "movie"
        )
    }
}

@JsonClass(generateAdapter = true)
data class TvDetail(
    val id: Int,
    val name: String,
    val overview: String?,
    val first_air_date: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double,
    val vote_count: Int,
    val number_of_seasons: Int?,
    val seasons: List<TvSeason>?,
    val genres: List<Genre>?,
    val videos: VideoResponse?
) {
    fun toCompactMedia(): CompactMedia {
        return CompactMedia(
            id = id,
            title = name,
            poster_path = poster_path,
            vote_average = vote_average,
            vote_count = vote_count,
            release_date = first_air_date,
            media_type = "tv"
        )
    }
}

@JsonClass(generateAdapter = true)
data class TvSeason(
    val id: Int,
    val name: String,
    val season_number: Int,
    val episode_count: Int,
    val poster_path: String?
)

@JsonClass(generateAdapter = true)
data class VideoResponse(
    val results: List<Video>
)

@JsonClass(generateAdapter = true)
data class Video(
    val id: String,
    val key: String,
    val name: String,
    val site: String,
    val type: String
)

@JsonClass(generateAdapter = true)
data class CreditResponse(
    val cast: List<Cast>
)

@JsonClass(generateAdapter = true)
data class Cast(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?,
    val order: Int
)

@JsonClass(generateAdapter = true)
data class ActorDetail(
    val id: Int,
    val name: String,
    val biography: String?,
    val birthday: String?,
    val place_of_birth: String?,
    val profile_path: String?
)

@JsonClass(generateAdapter = true)
data class ActorFilmographyResponse(
    val cast: List<TmdbMedia>
)
