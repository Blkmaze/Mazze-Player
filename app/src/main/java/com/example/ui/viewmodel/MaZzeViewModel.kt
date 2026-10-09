package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.FavoriteChannelEntity
import com.example.data.local.MaZzeDatabase
import com.example.data.local.RecentStreamEntity
import com.example.data.local.XtreamProfileEntity
import com.example.data.model.InternalConfig
import com.example.data.model.LiveCategory
import com.example.data.model.LiveStream
import com.example.data.model.SeriesCategory
import com.example.data.model.SeriesDetail
import com.example.data.model.SeriesEpisode
import com.example.data.model.SeriesItem
import com.example.data.model.VodCategory
import com.example.data.model.VodStream
import com.example.data.model.XtreamAuthResponse
import com.example.data.repository.XtreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Splash : Screen()
    data object Home : Screen()
    data object LiveTv : Screen()
    data object Vod : Screen()
    data object Series : Screen()
    data class SeriesDetailScreen(val series: SeriesItem) : Screen()
    data object Favorites : Screen()
    data object InternalSettings : Screen()
    data class Player(
        val stream: LiveStream,
        val playbackUrl: String,
        val streamType: String = "live"
    ) : Screen()
}

sealed class AuthUiState {
    data object Idle : AuthUiState()
    data object Connecting : AuthUiState()
    data class Success(val auth: XtreamAuthResponse) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class MaZzeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MaZzeDatabase.getDatabase(application)
    private val repository = XtreamRepository(db.dao())

    val allProfiles: StateFlow<List<XtreamProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<XtreamProfileEntity?> = repository.activeProfileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val favorites: StateFlow<List<FavoriteChannelEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recents: StateFlow<List<RecentStreamEntity>> = repository.recents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // --- Live Streams ---
    private val _liveCategories = MutableStateFlow<List<LiveCategory>>(emptyList())
    val liveCategories: StateFlow<List<LiveCategory>> = _liveCategories.asStateFlow()

    private val _selectedLiveCategoryId = MutableStateFlow("all")
    val selectedLiveCategoryId: StateFlow<String> = _selectedLiveCategoryId.asStateFlow()

    private val _liveStreams = MutableStateFlow<List<LiveStream>>(emptyList())
    val liveStreams: StateFlow<List<LiveStream>> = _liveStreams.asStateFlow()

    private val _isLoadingStreams = MutableStateFlow(false)
    val isLoadingStreams: StateFlow<Boolean> = _isLoadingStreams.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredLiveStreams: StateFlow<List<LiveStream>> = combine(
        _liveStreams,
        _searchQuery
    ) { streams, query ->
        if (query.isBlank()) {
            streams
        } else {
            streams.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- VOD (Movies) ---
    private val _vodCategories = MutableStateFlow<List<VodCategory>>(emptyList())
    val vodCategories: StateFlow<List<VodCategory>> = _vodCategories.asStateFlow()

    private val _vodStreams = MutableStateFlow<List<VodStream>>(emptyList())
    val vodStreams: StateFlow<List<VodStream>> = _vodStreams.asStateFlow()

    private val _selectedVodCategoryId = MutableStateFlow("all")
    val selectedVodCategoryId: StateFlow<String> = _selectedVodCategoryId.asStateFlow()

    private val _searchQueryVod = MutableStateFlow("")
    val searchQueryVod: StateFlow<String> = _searchQueryVod.asStateFlow()

    val filteredVodStreams: StateFlow<List<VodStream>> = combine(
        _vodStreams,
        _searchQueryVod
    ) { movies, query ->
        if (query.isBlank()) {
            movies
        } else {
            movies.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Series ---
    private val _seriesCategories = MutableStateFlow<List<SeriesCategory>>(emptyList())
    val seriesCategories: StateFlow<List<SeriesCategory>> = _seriesCategories.asStateFlow()

    private val _seriesList = MutableStateFlow<List<SeriesItem>>(emptyList())
    val seriesList: StateFlow<List<SeriesItem>> = _seriesList.asStateFlow()

    private val _selectedSeriesCategoryId = MutableStateFlow("all")
    val selectedSeriesCategoryId: StateFlow<String> = _selectedSeriesCategoryId.asStateFlow()

    private val _isLoadingSeries = MutableStateFlow(false)
    val isLoadingSeries: StateFlow<Boolean> = _isLoadingSeries.asStateFlow()

    private val _searchQuerySeries = MutableStateFlow("")
    val searchQuerySeries: StateFlow<String> = _searchQuerySeries.asStateFlow()

    val filteredSeriesList: StateFlow<List<SeriesItem>> = combine(
        _seriesList,
        _searchQuerySeries
    ) { series, query ->
        if (query.isBlank()) {
            series
        } else {
            series.filter { it.name.contains(query, ignoreCase = true) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Series Detail
    private val _selectedSeriesDetail = MutableStateFlow<SeriesDetail?>(null)
    val selectedSeriesDetail: StateFlow<SeriesDetail?> = _selectedSeriesDetail.asStateFlow()

    private val _isLoadingSeriesDetail = MutableStateFlow(false)
    val isLoadingSeriesDetail: StateFlow<Boolean> = _isLoadingSeriesDetail.asStateFlow()

    // --- Diagnostics & Testing ---
    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    private val _testConnectionResult = MutableStateFlow<String?>(null)
    val testConnectionResult: StateFlow<String?> = _testConnectionResult.asStateFlow()

    private val _statusNotice = MutableStateFlow<String?>(null)
    val statusNotice: StateFlow<String?> = _statusNotice.asStateFlow()

    init {
        viewModelScope.launch {
            repository.activeProfileFlow.collect { profile ->
                if (profile != null) {
                    connectWithProfile(profile)
                } else {
                    loadDemoContent()
                }
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchQueryVod(query: String) {
        _searchQueryVod.value = query
    }

    fun setSearchQuerySeries(query: String) {
        _searchQuerySeries.value = query
    }

    fun clearStatusNotice() {
        _statusNotice.value = null
    }

    fun clearTestResult() {
        _testConnectionResult.value = null
    }

    private fun connectWithProfile(profile: XtreamProfileEntity) {
        val config = InternalConfig(
            serverUrl = profile.serverUrl,
            username = profile.username,
            password = profile.password,
            profileName = profile.profileName,
            streamFormat = profile.streamFormat
        )

        if (!config.isValid) {
            _authUiState.value = AuthUiState.Error("Server credentials incomplete. Open Settings to configure.")
            loadDemoContent()
            return
        }

        viewModelScope.launch {
            _authUiState.value = AuthUiState.Connecting
            _isLoadingStreams.value = true
            _isLoadingSeries.value = true

            val authResult = repository.authenticate(config)
            authResult.fold(
                onSuccess = { authResponse ->
                    _authUiState.value = AuthUiState.Success(authResponse)
                    _statusNotice.value = "Connected to ${profile.profileName}"

                    // Fetch Live Categories
                    val catResult = repository.getLiveCategories(config)
                    val categories = catResult.getOrDefault(emptyList()).toMutableList()
                    categories.add(0, LiveCategory(categoryId = "all", categoryName = "All Channels"))
                    _liveCategories.value = categories

                    // Fetch Live Streams
                    val streamsResult = repository.getLiveStreams(config, null)
                    _liveStreams.value = streamsResult.getOrDefault(emptyList())
                    _isLoadingStreams.value = false

                    // Fetch VOD
                    loadVod(config)

                    // Fetch Series
                    loadSeries(config)
                },
                onFailure = { error ->
                    _authUiState.value = AuthUiState.Error(error.localizedMessage ?: "Failed to connect to Xtream server")
                    _isLoadingStreams.value = false
                    _isLoadingSeries.value = false
                    loadDemoContent()
                }
            )
        }
    }

    fun selectLiveCategory(categoryId: String) {
        _selectedLiveCategoryId.value = categoryId
        val active = activeProfile.value
        if (active != null && _authUiState.value is AuthUiState.Success) {
            val config = InternalConfig(
                serverUrl = active.serverUrl,
                username = active.username,
                password = active.password,
                streamFormat = active.streamFormat
            )
            viewModelScope.launch {
                _isLoadingStreams.value = true
                val streams = repository.getLiveStreams(config, if (categoryId == "all") null else categoryId)
                _liveStreams.value = streams.getOrDefault(emptyList())
                _isLoadingStreams.value = false
            }
        } else {
            _liveStreams.value = repository.getDemoStreams(categoryId)
        }
    }

    private fun loadVod(config: InternalConfig) {
        viewModelScope.launch {
            val vodCats = repository.getVodCategories(config).getOrDefault(emptyList()).toMutableList()
            vodCats.add(0, VodCategory(categoryId = "all", categoryName = "All Movies"))
            _vodCategories.value = vodCats

            val vodStreams = repository.getVodStreams(config, null)
            _vodStreams.value = vodStreams.getOrDefault(emptyList())
        }
    }

    fun selectVodCategory(categoryId: String) {
        _selectedVodCategoryId.value = categoryId
        val active = activeProfile.value
        if (active != null && _authUiState.value is AuthUiState.Success) {
            val config = InternalConfig(
                serverUrl = active.serverUrl,
                username = active.username,
                password = active.password,
                streamFormat = active.streamFormat
            )
            viewModelScope.launch {
                val streams = repository.getVodStreams(config, if (categoryId == "all") null else categoryId)
                _vodStreams.value = streams.getOrDefault(emptyList())
            }
        }
    }

    private fun loadSeries(config: InternalConfig) {
        viewModelScope.launch {
            _isLoadingSeries.value = true
            val sCats = repository.getSeriesCategories(config).getOrDefault(emptyList()).toMutableList()
            sCats.add(0, SeriesCategory(categoryId = "all", categoryName = "All Series"))
            _seriesCategories.value = sCats

            val sList = repository.getSeriesList(config, null)
            _seriesList.value = sList.getOrDefault(emptyList())
            _isLoadingSeries.value = false
        }
    }

    fun selectSeriesCategory(categoryId: String) {
        _selectedSeriesCategoryId.value = categoryId
        val active = activeProfile.value
        if (active != null && _authUiState.value is AuthUiState.Success) {
            val config = InternalConfig(
                serverUrl = active.serverUrl,
                username = active.username,
                password = active.password,
                streamFormat = active.streamFormat
            )
            viewModelScope.launch {
                _isLoadingSeries.value = true
                val list = repository.getSeriesList(config, if (categoryId == "all") null else categoryId)
                _seriesList.value = list.getOrDefault(emptyList())
                _isLoadingSeries.value = false
            }
        } else {
            _seriesList.value = repository.getDemoSeriesList(categoryId)
        }
    }

    fun openSeriesDetail(series: SeriesItem) {
        _currentScreen.value = Screen.SeriesDetailScreen(series)
        _isLoadingSeriesDetail.value = true
        _selectedSeriesDetail.value = null

        val active = activeProfile.value
        if (active != null && _authUiState.value is AuthUiState.Success) {
            val config = InternalConfig(
                serverUrl = active.serverUrl,
                username = active.username,
                password = active.password,
                streamFormat = active.streamFormat
            )
            viewModelScope.launch {
                val detailResult = repository.getSeriesInfo(config, series)
                _selectedSeriesDetail.value = detailResult.getOrNull() ?: repository.getDemoSeriesDetail(series)
                _isLoadingSeriesDetail.value = false
            }
        } else {
            _selectedSeriesDetail.value = repository.getDemoSeriesDetail(series)
            _isLoadingSeriesDetail.value = false
        }
    }

    fun playSeriesEpisode(episode: SeriesEpisode, seriesItem: SeriesItem) {
        viewModelScope.launch {
            val active = activeProfile.value
            val isDemo = active == null || _authUiState.value !is AuthUiState.Success
            val url = if (isDemo) {
                // Return playable demo video
                repository.getDemoStreamPlaybackUrl(1001 + (episode.episodeNum % 5))
            } else {
                val config = InternalConfig(
                    serverUrl = active.serverUrl,
                    username = active.username,
                    password = active.password
                )
                config.buildSeriesStreamUrl(episode.id, episode.containerExtension)
            }

            val streamId = episode.id.hashCode()
            repository.recordPlayed(streamId, "${seriesItem.name} - ${episode.title}", seriesItem.cover, "series")

            _currentScreen.value = Screen.Player(
                stream = LiveStream(
                    streamId = streamId,
                    name = "${seriesItem.name} - S${episode.season}E${episode.episodeNum}: ${episode.title}",
                    streamIcon = seriesItem.cover,
                    streamType = "series"
                ),
                playbackUrl = url,
                streamType = "series"
            )
        }
    }

    private fun loadDemoContent() {
        _liveCategories.value = repository.getDemoCategories()
        _liveStreams.value = repository.getDemoStreams()
        _seriesCategories.value = repository.getDemoSeriesCategories()
        _seriesList.value = repository.getDemoSeriesList()
        _isLoadingStreams.value = false
        _isLoadingSeries.value = false
    }

    fun saveInternalConfiguration(
        id: Long = 0,
        profileName: String,
        serverUrl: String,
        username: String,
        password: String,
        streamFormat: String = "m3u8",
        makeActive: Boolean = true
    ) {
        viewModelScope.launch {
            repository.saveOrUpdateProfile(
                id = id,
                profileName = profileName.ifBlank { "MaZze Server" },
                serverUrl = serverUrl.trim(),
                username = username.trim(),
                password = password.trim(),
                streamFormat = streamFormat,
                makeActive = makeActive
            )
            _statusNotice.value = "Internal configuration saved successfully!"
        }
    }

    fun testServerConnection(serverUrl: String, username: String, password: String) {
        viewModelScope.launch {
            _isTestingConnection.value = true
            _testConnectionResult.value = null
            val config = InternalConfig(
                serverUrl = serverUrl,
                username = username,
                password = password
            )
            val result = repository.authenticate(config)
            _isTestingConnection.value = false
            result.fold(
                onSuccess = { auth ->
                    val user = auth.userInfo
                    val exp = user?.expDate ?: "N/A"
                    val maxCons = user?.maxConnections ?: "1"
                    _testConnectionResult.value = "SUCCESS: Authorized!\nStatus: ${user?.status ?: "Active"}\nExpires: $exp\nMax Connections: $maxCons"
                },
                onFailure = { error ->
                    _testConnectionResult.value = "FAILED: ${error.localizedMessage}"
                }
            )
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch {
            repository.setActiveProfile(profileId)
        }
    }

    fun deleteProfile(profile: XtreamProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun playChannel(stream: LiveStream) {
        viewModelScope.launch {
            val active = activeProfile.value
            val isDemo = active == null || _authUiState.value !is AuthUiState.Success
            val url = if (isDemo) {
                repository.getDemoStreamPlaybackUrl(stream.streamIdInt)
            } else {
                val config = InternalConfig(
                    serverUrl = active.serverUrl,
                    username = active.username,
                    password = active.password,
                    streamFormat = active.streamFormat
                )
                config.buildLiveStreamUrl(stream.streamIdInt)
            }

            repository.recordPlayed(stream.streamIdInt, stream.name, stream.streamIcon, "live")
            _currentScreen.value = Screen.Player(
                stream = stream,
                playbackUrl = url,
                streamType = "live"
            )
        }
    }

    fun playVodMovie(vod: VodStream) {
        val active = activeProfile.value
        val isDemo = active == null || _authUiState.value !is AuthUiState.Success
        val url = if (isDemo) {
            repository.getDemoStreamPlaybackUrl(vod.streamIdInt)
        } else {
            val config = InternalConfig(
                serverUrl = active.serverUrl,
                username = active.username,
                password = active.password
            )
            config.buildVodStreamUrl(vod.streamIdInt, vod.containerExtension ?: "mp4")
        }

        viewModelScope.launch {
            repository.recordPlayed(vod.streamIdInt, vod.name, vod.streamIcon, "movie")
            _currentScreen.value = Screen.Player(
                stream = LiveStream(
                    streamId = vod.streamId,
                    name = vod.name,
                    streamIcon = vod.streamIcon,
                    streamType = "movie"
                ),
                playbackUrl = url,
                streamType = "movie"
            )
        }
    }

    fun toggleFavorite(stream: LiveStream, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(stream, isFav)
        }
    }

    fun refreshActiveConnection() {
        val active = activeProfile.value
        if (active != null) {
            connectWithProfile(active)
        } else {
            loadDemoContent()
        }
    }
}
