package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomNavBar
import com.example.ui.components.MaZzeTopBar
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.livetv.LiveTvScreen
import com.example.ui.screens.player.VideoPlayerScreen
import com.example.ui.screens.settings.InternalSettingsScreen
import com.example.ui.screens.vod.VodScreen
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
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val authState by viewModel.authUiState.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val recents by viewModel.recents.collectAsStateWithLifecycle()

    val liveCategories by viewModel.liveCategories.collectAsStateWithLifecycle()
    val selectedLiveCategoryId by viewModel.selectedLiveCategoryId.collectAsStateWithLifecycle()
    val filteredLiveStreams by viewModel.filteredLiveStreams.collectAsStateWithLifecycle()
    val isLoadingStreams by viewModel.isLoadingStreams.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val vodCategories by viewModel.vodCategories.collectAsStateWithLifecycle()
    val selectedVodCategoryId by viewModel.selectedVodCategoryId.collectAsStateWithLifecycle()
    val vodStreams by viewModel.vodStreams.collectAsStateWithLifecycle()

    val isTesting by viewModel.isTestingConnection.collectAsStateWithLifecycle()
    val testResult by viewModel.testConnectionResult.collectAsStateWithLifecycle()
    val statusNotice by viewModel.statusNotice.collectAsStateWithLifecycle()

    // Handle back button for sub-screens
    if (currentScreen !is Screen.Home && currentScreen !is Screen.Player) {
        BackHandler {
            viewModel.navigateTo(Screen.Home)
        }
    }

    when (val screen = currentScreen) {
        is Screen.Player -> {
            VideoPlayerScreen(
                stream = screen.stream,
                playbackUrl = screen.playbackUrl,
                streamType = screen.streamType,
                onBack = { viewModel.navigateTo(Screen.LiveTv) }
            )
        }
        else -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
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
                val contentModifier = Modifier.padding(innerPadding)

                when (currentScreen) {
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
                            streams = vodStreams,
                            onSelectCategory = { viewModel.selectVodCategory(it) },
                            onPlayMovie = { viewModel.playVodMovie(it) },
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
