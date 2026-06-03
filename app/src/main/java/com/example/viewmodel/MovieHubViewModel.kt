package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieHubViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = TmdbApiService.create()
    val localStorageManager = LocalStorageManager(application)

    // Observables for SharedPreferences lists
    val watchlist = localStorageManager.watchlistFlow
    val history = localStorageManager.historyFlow

    // --- HOME SCREEN STATES ---
    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    private val _featuredMovie = MutableStateFlow<CompactMedia?>(null)
    val featuredMovie: StateFlow<CompactMedia?> = _featuredMovie.asStateFlow()

    private val _trendingMovies = MutableStateFlow<List<CompactMedia>>(emptyList())
    val trendingMovies: StateFlow<List<CompactMedia>> = _trendingMovies.asStateFlow()

    private val _trendingTv = MutableStateFlow<List<CompactMedia>>(emptyList())
    val trendingTv: StateFlow<List<CompactMedia>> = _trendingTv.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<CompactMedia>>(emptyList())
    val popularMovies: StateFlow<List<CompactMedia>> = _popularMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<CompactMedia>>(emptyList())
    val topRatedMovies: StateFlow<List<CompactMedia>> = _topRatedMovies.asStateFlow()

    private val _nowPlayingMovies = MutableStateFlow<List<CompactMedia>>(emptyList())
    val nowPlayingMovies: StateFlow<List<CompactMedia>> = _nowPlayingMovies.asStateFlow()

    private val _upcomingMovies = MutableStateFlow<List<CompactMedia>>(emptyList())
    val upcomingMovies: StateFlow<List<CompactMedia>> = _upcomingMovies.asStateFlow()

    private val _popularActors = MutableStateFlow<List<Cast>>(emptyList())
    val popularActors: StateFlow<List<Cast>> = _popularActors.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    // --- HOME SUB-TABS STATE ---
    private val _selectedHomeTab = MutableStateFlow("Trending")
    val selectedHomeTab: StateFlow<String> = _selectedHomeTab.asStateFlow()

    private val _subTabContent = MutableStateFlow<List<CompactMedia>>(emptyList())
    val subTabContent: StateFlow<List<CompactMedia>> = _subTabContent.asStateFlow()

    // --- PAGINATED LIST STATE ---
    private val _listItems = MutableStateFlow<List<CompactMedia>>(emptyList())
    val listItems: StateFlow<List<CompactMedia>> = _listItems.asStateFlow()

    private val _listPage = MutableStateFlow(1)
    val listPage: StateFlow<Int> = _listPage.asStateFlow()

    private val _listTotalPages = MutableStateFlow(1)
    val listTotalPages: StateFlow<Int> = _listTotalPages.asStateFlow()

    private val _isLoadingList = MutableStateFlow(false)
    val isLoadingList: StateFlow<Boolean> = _isLoadingList.asStateFlow()

    // --- DETAIL SCREEN STATES ---
    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _movieDetail = MutableStateFlow<MovieDetail?>(null)
    val movieDetail: StateFlow<MovieDetail?> = _movieDetail.asStateFlow()

    private val _tvDetail = MutableStateFlow<TvDetail?>(null)
    val tvDetail: StateFlow<TvDetail?> = _tvDetail.asStateFlow()

    private val _currentCast = MutableStateFlow<List<Cast>>(emptyList())
    val currentCast: StateFlow<List<Cast>> = _currentCast.asStateFlow()

    private val _currentRecommendations = MutableStateFlow<List<CompactMedia>>(emptyList())
    val currentRecommendations: StateFlow<List<CompactMedia>> = _currentRecommendations.asStateFlow()

    // --- WATCH / PLAYER STATES ---
    private val _activeServerIndex = MutableStateFlow(0)
    val activeServerIndex: StateFlow<Int> = _activeServerIndex.asStateFlow()

    // --- ACTOR SCREEN STATES ---
    private val _isLoadingActor = MutableStateFlow(false)
    val isLoadingActor: StateFlow<Boolean> = _isLoadingActor.asStateFlow()

    private val _actorDetail = MutableStateFlow<ActorDetail?>(null)
    val actorDetail: StateFlow<ActorDetail?> = _actorDetail.asStateFlow()

    private val _actorFilmography = MutableStateFlow<List<CompactMedia>>(emptyList())
    val actorFilmography: StateFlow<List<CompactMedia>> = _actorFilmography.asStateFlow()

    // --- SEARCH STATES ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchCategory = MutableStateFlow("Movies") // "Movies", "Series", "Stars"
    val searchCategory: StateFlow<String> = _searchCategory.asStateFlow()

    private val _searchResults = MutableStateFlow<List<CompactMedia>>(emptyList())
    val searchResults: StateFlow<List<CompactMedia>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoadingHome.value = true
            try {
                // Fetch basic rows
                val trendingM = apiService.getTrendingMovies().results.map { it.toCompactMedia("movie") }
                _trendingMovies.value = trendingM

                // Set initial featured movie to first trending movie
                if (trendingM.isNotEmpty()) {
                    _featuredMovie.value = trendingM.first()
                }

                _trendingTv.value = apiService.getTrendingTv().results.map { it.toCompactMedia("tv") }
                _popularMovies.value = apiService.getPopularMovies().results.map { it.toCompactMedia("movie") }
                _topRatedMovies.value = apiService.getTopRatedMovies().results.map { it.toCompactMedia("movie") }
                _nowPlayingMovies.value = apiService.getNowPlayingMovies().results.map { it.toCompactMedia("movie") }
                _upcomingMovies.value = apiService.getUpcomingMovies().results.map { it.toCompactMedia("movie") }

                // Popular actors
                val actorResponse = apiService.getPopularActors()
                val actorsMapped = actorResponse.results.map {
                    Cast(
                        id = it.id,
                        name = it.name ?: "Unknown",
                        character = null,
                        profile_path = it.profile_path ?: it.poster_path, // TMDB mixes fields sometimes
                        order = 0
                    )
                }
                _popularActors.value = actorsMapped

                // Genres list
                _genres.value = apiService.getMovieGenres().genres

                // Load initial Sub-Tab content (Trending is default)
                updateHomeSubTab(_selectedHomeTab.value)

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingHome.value = false
            }
        }
    }

    fun updateHomeSubTab(tab: String) {
        _selectedHomeTab.value = tab
        viewModelScope.launch {
            try {
                _subTabContent.value = when (tab) {
                    "Trending" -> _trendingMovies.value
                    "Recommended" -> {
                        // Use recommendations for featured movie if available
                        val featuredId = _featuredMovie.value?.id
                        if (featuredId != null) {
                            apiService.getMovieRecommendations(featuredId).results.map { it.toCompactMedia("movie") }
                        } else {
                            _topRatedMovies.value
                        }
                    }
                    "New" -> _nowPlayingMovies.value
                    "Top Rated" -> _topRatedMovies.value
                    "Most Viewed" -> _popularMovies.value
                    "Coming Soon" -> _upcomingMovies.value
                    else -> _trendingMovies.value
                }
            } catch (e: Exception) {
                // Fallback to cached list
                _subTabContent.value = when (tab) {
                    "Recommended" -> _topRatedMovies.value
                    "New" -> _nowPlayingMovies.value
                    "Top Rated" -> _topRatedMovies.value
                    "Most Viewed" -> _popularMovies.value
                    "Coming Soon" -> _upcomingMovies.value
                    else -> _trendingMovies.value
                }
            }
        }
    }

    // --- PAGINATED LIST LOADING ---

    fun loadCategoryList(category: String, genreId: Int = -1, page: Int = 1) {
        _isLoadingList.value = true
        _listPage.value = page
        viewModelScope.launch {
            try {
                val response = when (category) {
                    "trending_movies" -> apiService.getTrendingMovies(page)
                    "trending_tv" -> apiService.getTrendingTv(page)
                    "popular" -> apiService.getPopularMovies(page)
                    "top_rated" -> apiService.getTopRatedMovies(page)
                    "now_playing" -> apiService.getNowPlayingMovies(page)
                    "upcoming" -> apiService.getUpcomingMovies(page)
                    "by-genre" -> apiService.getMoviesByGenre(genreId, page = page)
                    "popular_actors" -> apiService.getPopularActors(page)
                    else -> apiService.getPopularMovies(page)
                }
                
                val inferredType = if (category == "trending_tv") "tv" else if (category == "popular_actors") "actor" else "movie"
                _listItems.value = response.results.map {
                    if (category == "popular_actors") {
                        CompactMedia(
                            id = it.id,
                            title = it.name ?: it.title ?: "Unknown",
                            poster_path = it.profile_path ?: it.poster_path,
                            vote_average = 0.0,
                            vote_count = 0,
                            release_date = null,
                            media_type = "actor"
                        )
                    } else {
                        it.toCompactMedia(inferredType)
                    }
                }
                _listTotalPages.value = response.total_pages ?: 1
            } catch (e: Exception) {
                e.printStackTrace()
                _listItems.value = emptyList()
            } finally {
                _isLoadingList.value = false
            }
        }
    }

    // --- DETAIL LOADING ---

    fun loadMediaDetails(id: Int, isTv: Boolean) {
        _isLoadingDetail.value = true
        _movieDetail.value = null
        _tvDetail.value = null
        _currentCast.value = emptyList()
        _currentRecommendations.value = emptyList()

        viewModelScope.launch {
            try {
                if (isTv) {
                    val detail = apiService.getTvDetails(id)
                    _tvDetail.value = detail
                    val credits = apiService.getTvCredits(id)
                    _currentCast.value = credits.cast.sortedBy { it.order }
                    _currentRecommendations.value = apiService.getTvRecommendations(id).results.map { it.toCompactMedia("tv") }
                } else {
                    val detail = apiService.getMovieDetails(id)
                    _movieDetail.value = detail
                    val credits = apiService.getMovieCredits(id)
                    _currentCast.value = credits.cast.sortedBy { it.order }
                    _currentRecommendations.value = apiService.getMovieRecommendations(id).results.map { it.toCompactMedia("movie") }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    // --- ACTOR LOADING ---

    fun loadActorDetails(id: Int) {
        _isLoadingActor.value = true
        _actorDetail.value = null
        _actorFilmography.value = emptyList()

        viewModelScope.launch {
            try {
                val detail = apiService.getActorDetails(id)
                _actorDetail.value = detail
                
                val filmography = apiService.getActorFilmography(id)
                _actorFilmography.value = filmography.cast.map { it.toCompactMedia("movie") }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingActor.value = false
            }
        }
    }

    // --- SEARCH LOGIC ---

    fun updateSearchCategory(category: String) {
        _searchCategory.value = category
        triggerSearch(_searchQuery.value)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchDebounceJob?.cancel()
        if (query.trim().length >= 2) {
            searchDebounceJob = viewModelScope.launch {
                delay(300) // 300 ms debounce
                triggerSearch(query)
            }
        } else if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
        }
    }

    private fun triggerSearch(query: String) {
        if (query.trim().length < 2) return
        _isSearching.value = true
        val category = _searchCategory.value

        viewModelScope.launch {
            try {
                val response = when (category) {
                    "Movies" -> apiService.searchMovies(query)
                    "Series" -> apiService.searchTv(query)
                    "Stars" -> apiService.searchPeople(query)
                    else -> apiService.searchMovies(query)
                }

                if (category == "Stars") {
                    _searchResults.value = response.results.map {
                        CompactMedia(
                            id = it.id,
                            title = it.name ?: "Unknown",
                            poster_path = it.poster_path, // profile paths are mapped here
                            vote_average = 0.0,
                            vote_count = 0,
                            release_date = null,
                            media_type = "person"
                        )
                    }
                } else {
                    val inferredType = if (category == "Series") "tv" else "movie"
                    _searchResults.value = response.results.map { it.toCompactMedia(inferredType) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    // --- SERVER SELECTOR ---
    fun selectServer(index: Int) {
        _activeServerIndex.value = index
    }
}
