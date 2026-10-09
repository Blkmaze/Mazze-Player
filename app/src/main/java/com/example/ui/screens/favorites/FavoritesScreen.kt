package com.example.ui.screens.favorites

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.FavoriteChannelEntity
import com.example.data.model.LiveStream
import com.example.ui.theme.MaZzeAccentAmber
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteChannelEntity>,
    onPlayChannel: (LiveStream) -> Unit,
    onRemoveFavorite: (LiveStream) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Favorite Channels (${favorites.size})",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No favorites saved",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Tap the star icon next to any channel to save it here for fast access.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(favorites, key = { it.streamId }) { fav ->
                    val stream = LiveStream(
                        streamId = fav.streamId,
                        name = fav.streamName,
                        streamIcon = fav.streamIcon,
                        categoryId = fav.categoryId,
                        streamType = fav.streamType
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPlayChannel(stream) }
                            .testTag("fav_channel_${fav.streamId}"),
                        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaZzeSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!fav.streamIcon.isNullOrBlank()) {
                                    AsyncImage(
                                        model = fav.streamIcon,
                                        contentDescription = fav.streamName,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(42.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = MaZzeAccentAmber,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = fav.streamName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "FAVORITE CHANNEL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaZzeAccentAmber
                                )
                            }
                            IconButton(onClick = { onRemoveFavorite(stream) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove favorite",
                                    tint = TextMuted
                                )
                            }
                            IconButton(onClick = { onPlayChannel(stream) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircle,
                                    contentDescription = "Play channel",
                                    tint = MaZzeSecondary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
