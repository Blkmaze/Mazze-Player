package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.RecentStreamEntity
import com.example.data.local.XtreamProfileEntity
import com.example.data.model.LiveStream
import com.example.ui.theme.MaZzeAccentAmber
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzeError
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSuccess
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.Screen

@Composable
fun HomeScreen(
    activeProfile: XtreamProfileEntity?,
    authState: AuthUiState,
    recents: List<RecentStreamEntity>,
    onNavigate: (Screen) -> Unit,
    onPlayRecent: (RecentStreamEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Xtream Status Banner
        item {
            XtreamStatusHeroCard(
                activeProfile = activeProfile,
                authState = authState,
                onConfigureClick = { onNavigate(Screen.InternalSettings) }
            )
        }

        // Quick Navigation Tiles
        item {
            Text(
                text = "Media Library",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavCard(
                    title = "Live TV",
                    subtitle = "All Channels",
                    icon = Icons.Default.Tv,
                    accentColor = MaZzePrimary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.LiveTv) }
                )
                QuickNavCard(
                    title = "Movies / VOD",
                    subtitle = "On Demand",
                    icon = Icons.Default.Movie,
                    accentColor = MaZzeSecondary,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Vod) }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickNavCard(
                    title = "Favorites",
                    subtitle = "Saved Channels",
                    icon = Icons.Default.Favorite,
                    accentColor = MaZzeAccentAmber,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Favorites) }
                )
                QuickNavCard(
                    title = "Internal Settings",
                    subtitle = "Xtream Config",
                    icon = Icons.Default.Settings,
                    accentColor = Color(0xFF00B0FF),
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.InternalSettings) }
                )
            }
        }

        // Server Account Details Card (if connected)
        if (authState is AuthUiState.Success) {
            item {
                XtreamAccountDetailsCard(
                    authResponse = authState.auth,
                    activeProfile = activeProfile
                )
            }
        }

        // Recently Watched Shelf
        if (recents.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recently Watched",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recents) { item ->
                        RecentStreamCard(
                            item = item,
                            onClick = { onPlayRecent(item) }
                        )
                    }
                }
            }
        }

        // Internal Configuration Quick Tip
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaZzePrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaZzePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom Xtream Credentials",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "You can add multiple Xtream Codes server URLs, usernames, and passwords in Settings at any time.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun XtreamStatusHeroCard(
    activeProfile: XtreamProfileEntity?,
    authState: AuthUiState,
    onConfigureClick: () -> Unit
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF261852),
            Color(0xFF141935)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("xtream_status_hero_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(18.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (authState) {
                                        is AuthUiState.Success -> MaZzeSuccess
                                        is AuthUiState.Connecting -> MaZzeSecondary
                                        is AuthUiState.Error -> MaZzeError
                                        is AuthUiState.Idle -> if (activeProfile != null) MaZzeAccentAmber else Color.Gray
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (authState) {
                                is AuthUiState.Success -> "XTREAM CONNECTED"
                                is AuthUiState.Connecting -> "CONNECTING TO SERVER..."
                                is AuthUiState.Error -> "CONNECTION ERROR"
                                is AuthUiState.Idle -> if (activeProfile != null) "READY TO CONNECT" else "DEMO / UNCONFIGURED"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (authState) {
                                is AuthUiState.Success -> MaZzeSuccess
                                is AuthUiState.Connecting -> MaZzeSecondary
                                is AuthUiState.Error -> MaZzeError
                                is AuthUiState.Idle -> MaZzeAccentAmber
                            }
                        )
                    }

                    Text(
                        text = "MaZze v1.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = activeProfile?.profileName ?: "Xtream Codes Player",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = if (activeProfile != null) {
                        "Server: ${activeProfile.serverUrl} (${activeProfile.username})"
                    } else {
                        "No custom Xtream Codes server configured yet. You can add your own credentials in Settings."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onConfigureClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaZzePrimary
                    ),
                    modifier = Modifier.testTag("configure_credentials_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeProfile != null) "Edit Server Credentials" else "Add Xtream Credentials",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun QuickNavCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
        }
    }
}

@Composable
fun XtreamAccountDetailsCard(
    authResponse: com.example.data.model.XtreamAuthResponse,
    activeProfile: XtreamProfileEntity?
) {
    val user = authResponse.userInfo
    val server = authResponse.serverInfo

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Active Server Account Info",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaZzeSecondary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(label = "Status", value = user?.status ?: "Active", valueColor = MaZzeSuccess)
                InfoItem(label = "Max Cons", value = user?.maxConnections?.toString() ?: "1", valueColor = TextPrimary)
                InfoItem(label = "Active Cons", value = user?.activeConnections?.toString() ?: "0", valueColor = TextPrimary)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(label = "Protocol", value = server?.serverProtocol ?: "http", valueColor = TextPrimary)
                InfoItem(label = "Timezone", value = server?.timezone ?: "UTC", valueColor = TextPrimary)
                InfoItem(label = "Format", value = activeProfile?.streamFormat ?: "m3u8", valueColor = MaZzeAccentAmber)
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}

@Composable
fun RecentStreamCard(
    item: RecentStreamEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceElevated)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaZzeSurfaceBorder),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaZzeSecondary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.streamName,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = item.streamType.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaZzeAccentAmber
            )
        }
    }
}
