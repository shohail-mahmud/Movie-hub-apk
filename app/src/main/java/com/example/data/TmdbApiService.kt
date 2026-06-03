package com.example.data

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    // --- MOVIES ---

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("movie/{id}?append_to_response=videos")
    suspend fun getMovieDetails(
        @Path("id") id: Int
    ): MovieDetail

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(
        @Path("id") id: Int
    ): CreditResponse

    @GET("movie/{id}/recommendations")
    suspend fun getMovieRecommendations(
        @Path("id") id: Int
    ): TmdbResponse

    @GET("movie/{id}/similar")
    suspend fun getMovieSimilar(
        @Path("id") id: Int
    ): TmdbResponse

    // --- TV SHOWS ---

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("tv/popular")
    suspend fun getPopularTv(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("tv/{id}?append_to_response=videos")
    suspend fun getTvDetails(
        @Path("id") id: Int
    ): TvDetail

    @GET("tv/{id}/credits")
    suspend fun getTvCredits(
        @Path("id") id: Int
    ): CreditResponse

    @GET("tv/{id}/recommendations")
    suspend fun getTvRecommendations(
        @Path("id") id: Int
    ): TmdbResponse

    // --- ACTORS (PEOPLE) ---

    @GET("person/popular")
    suspend fun getPopularActors(
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("person/{id}")
    suspend fun getActorDetails(
        @Path("id") id: Int
    ): ActorDetail

    @GET("person/{id}/movie_credits")
    suspend fun getActorFilmography(
        @Path("id") id: Int
    ): ActorFilmographyResponse

    // --- SEARCH ---

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("search/tv")
    suspend fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse

    @GET("search/person")
    suspend fun searchPeople(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbResponse

    // --- GENRES ---

    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbResponse

    companion object {
        const val TMDB_API_KEY = "8265bd1679663a7ea12ac168da84d2e8"
        private const val BASE_URL = "https://api.themoviedb.org/3/"

        fun create(): TmdbApiService {
            val keyInterceptor = Interceptor { chain ->
                val originalRequest = chain.request()
                val originalUrl = originalRequest.url
                
                val urlWithKey = originalUrl.newBuilder()
                    .addQueryParameter("api_key", TMDB_API_KEY)
                    .build()
                
                val newRequest = originalRequest.newBuilder()
                    .url(urlWithKey)
                    .build()
                
                chain.proceed(newRequest)
            }

            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(keyInterceptor)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()

            return retrofit.create(TmdbApiService::class.java)
        }
    }
}
