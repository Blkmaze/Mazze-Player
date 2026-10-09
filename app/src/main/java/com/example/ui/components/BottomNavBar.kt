package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.Screen

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onSelectScreen: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = MaZzeSurfaceDark,
        modifier = modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentScreen is Screen.Home,
            onClick = { onSelectScreen(Screen.Home) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaZzeSecondary,
                indicatorColor = MaZzePrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_home")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.LiveTv,
            onClick = { onSelectScreen(Screen.LiveTv) },
            icon = { Icon(Icons.Default.Tv, contentDescription = "Live TV") },
            label = { Text("Live TV") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaZzeSecondary,
                indicatorColor = MaZzePrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_live_tv")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Vod,
            onClick = { onSelectScreen(Screen.Vod) },
            icon = { Icon(Icons.Default.Movie, contentDescription = "Movies") },
            label = { Text("VOD") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaZzeSecondary,
                indicatorColor = MaZzePrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_vod")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.Favorites,
            onClick = { onSelectScreen(Screen.Favorites) },
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
            label = { Text("Favorites") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaZzeSecondary,
                indicatorColor = MaZzePrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_favorites")
        )

        NavigationBarItem(
            selected = currentScreen is Screen.InternalSettings,
            onClick = { onSelectScreen(Screen.InternalSettings) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
            label = { Text("Settings") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.White,
                selectedTextColor = MaZzeSecondary,
                indicatorColor = MaZzePrimary,
                unselectedIconColor = TextMuted,
                unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_settings")
        )
    }
}
