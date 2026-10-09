package com.example

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomNavBar
import com.example.ui.components.MaZzeTopBar
import com.example.ui.components.UpdateDialog
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.livetv.LiveTvScreen
import com.example.ui.screens.player.VideoPlayerScreen
import com.example.ui.screens.series.SeriesDetailScreen
import com.example.ui.screens.series.SeriesScreen
import com.example.ui.screens.settings.InternalSettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.vod.VodScreen
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzeTheme
import com.example.ui.viewmodel.MaZzeViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

    private val viewModel: MaZzeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaZzeTheme {
                MaZzeApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MaZzeApp(viewModel: MaZzeViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val uiModeManager = remember(context) { context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager }
    val isTv = remember(uiModeManager, context) {
        uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION ||
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK) ||
                context.packageManager.hasSystemFeature("android.hardware.type.television")
    }

    // TV safe overscan margins: 48dp horizontal, 27dp vertical around every screen on TV or landscape
    val isTvOrLandscape = isTv || isLandscape
    val tvSafeHoriz = if (isTvOrLandscape) 48.dp else 12.dp
    val tvSafeVert = if (isTvOrLandscape) 27.dp else 8.dp

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val authState by viewModel.authUiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recents by viewModel.recents.collectAsStateWithLifecycle()

    // Live TV State
    val liveCategories by viewModel.liveCategories.collectAsStateWithLifecycle()
    val selectedLiveCategoryId by viewModel.selectedLiveCategoryId.collectAsStateWithLifecycle()
    val filteredLiveStreams by viewModel.filteredLiveStreams.collectAsStateWithLifecycle()
    val isLoadingStreams by viewModel.isLoadingStreams.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    // VOD State
    val vodCategories by viewModel.vodCategories.collectAsStateWithLifecycle()
    val selectedVodCategoryId by viewModel.selectedVodCategoryId.collectAsStateWithLifecycle()
    val filteredVodStreams by viewModel.filteredVodStreams.collectAsStateWithLifecycle()
    val searchQueryVod by viewModel.searchQueryVod.collectAsStateWithLifecycle()

    // Series State
    val seriesCategories by viewModel.seriesCategories.collectAsStateWithLifecycle()
    val selectedSeriesCategoryId by viewModel.selectedSeriesCategoryId.collectAsStateWithLifecycle()
    val filteredSeriesList by viewModel.filteredSeriesList.collectAsStateWithLifecycle()
    val isLoadingSeries by viewModel.isLoadingSeries.collectAsStateWithLifecycle()
    val searchQuerySeries by viewModel.searchQuerySeries.collectAsStateWithLifecycle()
    val selectedSeriesDetail by viewModel.selectedSeriesDetail.collectAsStateWithLifecycle()
    val isLoadingSeriesDetail by viewModel.isLoadingSeriesDetail.collectAsStateWithLifecycle()

    // Settings & Diagnostics State
    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testResult by viewModel.testConnectionResult.collectAsStateWithLifecycle()
    val statusNotice by viewModel.statusNotice.collectAsStateWithLifecycle()

    // Update Checker State
    val availableUpdate by viewModel.availableUpdate.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsStateWithLifecycle()
    val manualUpdateCheckResult by viewModel.manualUpdateCheckResult.collectAsStateWithLifecycle()

    // Back button handling
    when (currentScreen) {
        is Screen.Splash -> {
            val activity = context as? ComponentActivity
            BackHandler {
                activity?.finish()
            }
        }
        is Screen.SeriesDetailScreen -> {
            BackHandler {
                viewModel.navigateTo(Screen.Series)
            }
        }
        is Screen.Home, is Screen.Player -> {
            // Handled internally or default back
        }
        else -> {
            BackHandler {
                viewModel.navigateTo(Screen.Home)
            }
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            if (initialState is Screen.Splash || targetState is Screen.Splash) {
                fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
            } else {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            }
        },
        label = "screen_transition"
    ) { screen ->
        when (screen) {
            is Screen.Splash -> {
                SplashScreen(
                    tvSafeHoriz = tvSafeHoriz,
                    tvSafeVert = tvSafeVert,
                    onSplashComplete = {
                        viewModel.navigateTo(Screen.Home)
                        viewModel.checkUpdateOnAppStart()
                    }
                )
            }
            is Screen.Player -> {
                VideoPlayerScreen(
                    stream = screen.stream,
                    playbackUrl = screen.playbackUrl,
                    streamType = screen.streamType,
                    onBack = {
                        when (screen.streamType) {
                            "series" -> viewModel.navigateTo(Screen.Series)
                            "movie" -> viewModel.navigateTo(Screen.Vod)
                            else -> viewModel.navigateTo(Screen.LiveTv)
                        }
                    }
                )
            }
            is Screen.SeriesDetailScreen -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaZzeDarkBackground)
                        .padding(horizontal = tvSafeHoriz, vertical = tvSafeVert)
                ) {
                    SeriesDetailScreen(
                        seriesItem = screen.series,
                        seriesDetail = selectedSeriesDetail,
                        isLoading = isLoadingSeriesDetail,
                        onBack = { viewModel.navigateTo(Screen.Series) },
                        onPlayEpisode = { ep, item ->
                            viewModel.playSeriesEpisode(ep, item)
                        }
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaZzeDarkBackground)
                        .padding(horizontal = tvSafeHoriz, vertical = tvSafeVert)
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaZzeDarkBackground,
                        topBar = {
                            MaZzeTopBar(
                                serverName = activeProfile?.profileName,
                                authState = authState,
                                onOpenSettings = { viewModel.navigateTo(Screen.InternalSettings) },
                                onRefresh = { viewModel.refreshActiveConnection() }
                            )
                        },
                        bottomBar = {
                            BottomNavBar(
                                currentScreen = currentScreen,
                                onSelectScreen = { viewModel.navigateTo(it) }
                            )
                        }
                    ) { innerPadding ->
                        val contentModifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)

                        when (screen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    activeProfile = activeProfile,
                                    authState = authState,
                                    recents = recents,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onPlayRecent = { recent ->
                                        val stream = com.example.data.model.LiveStream(
                                            streamId = recent.streamId,
                                            name = recent.streamName,
                                            streamIcon = recent.streamIcon,
                                            streamType = recent.streamType
                                        )
                                        viewModel.playChannel(stream)
                                    },
                                    modifier = contentModifier
                                )
                            }
                            is Screen.LiveTv -> {
                                LiveTvScreen(
                                    categories = liveCategories,
                                    selectedCategoryId = selectedLiveCategoryId,
                                    streams = filteredLiveStreams,
                                    favorites = favorites,
                                    isLoading = isLoadingStreams,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                    onSelectCategory = { viewModel.selectLiveCategory(it) },
                                    onPlayChannel = { viewModel.playChannel(it) },
                                    onToggleFavorite = { stream, isFav -> viewModel.toggleFavorite(stream, isFav) },
                                    modifier = contentModifier
                                )
                            }
                            is Screen.Vod -> {
                                VodScreen(
                                    categories = vodCategories,
                                    selectedCategoryId = selectedVodCategoryId,
                                    streams = filteredVodStreams,
                                    searchQuery = searchQueryVod,
                                    onSearchQueryChange = { viewModel.setSearchQueryVod(it) },
                                    onSelectCategory = { viewModel.selectVodCategory(it) },
                                    onPlayMovie = { viewModel.playVodMovie(it) },
                                    modifier = contentModifier
                                )
                            }
                            is Screen.Series -> {
                                SeriesScreen(
                                    categories = seriesCategories,
                                    selectedCategoryId = selectedSeriesCategoryId,
                                    seriesList = filteredSeriesList,
                                    isLoading = isLoadingSeries,
                                    searchQuery = searchQuerySeries,
                                    onSearchQueryChange = { viewModel.setSearchQuerySeries(it) },
                                    onSelectCategory = { viewModel.selectSeriesCategory(it) },
                                    onOpenSeries = { series -> viewModel.openSeriesDetail(series) },
                                    modifier = contentModifier
                                )
                            }
                            is Screen.Favorites -> {
                                FavoritesScreen(
                                    favorites = favorites,
                                    onPlayChannel = { viewModel.playChannel(it) },
                                    onRemoveFavorite = { viewModel.toggleFavorite(it, true) },
                                    modifier = contentModifier
                                )
                            }
                            is Screen.InternalSettings -> {
                                InternalSettingsScreen(
                                    activeProfile = activeProfile,
                                    allProfiles = allProfiles,
                                    isTesting = isTesting,
                                    testResult = testResult,
                                    statusNotice = statusNotice,
                                    isCheckingUpdate = isCheckingUpdate,
                                    updateCheckResult = manualUpdateCheckResult,
                                    onCheckForUpdates = { viewModel.checkForUpdates(isManual = true) },
                                    onSaveProfile = { id, name, url, user, pass, format ->
                                        viewModel.saveInternalConfiguration(
                                            id = id,
                                            profileName = name,
                                            serverUrl = url,
                                            username = user,
                                            password = pass,
                                            streamFormat = format
                                        )
                                    },
                                    onTestConnection = { url, user, pass ->
                                        viewModel.testServerConnection(url, user, pass)
                                    },
                                    onSwitchProfile = { id ->
                                        viewModel.switchProfile(id)
                                    },
                                    onDeleteProfile = { profile ->
                                        viewModel.deleteProfile(profile)
                                    },
                                    onClearStatusNotice = { viewModel.clearStatusNotice() },
                                    modifier = contentModifier
                                )
                            }
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // Global TV-friendly update dialog
    val currentUpdate = availableUpdate
    if (currentUpdate != null) {
        UpdateDialog(
            updateInfo = currentUpdate,
            downloadState = downloadState,
            onUpdateClick = { viewModel.startDownloadingUpdate(context) },
            onLaterClick = { viewModel.dismissUpdateDialog() },
            onRetryInstall = { viewModel.retryInstall(context) }
        )
    }
}
