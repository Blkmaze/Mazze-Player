package com.example.ui.screens.livetv

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FavoriteChannelEntity
import com.example.data.model.LiveCategory
import com.example.data.model.LiveStream
import com.example.ui.theme.MaZzeAccentAmber
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LiveTvScreen(
    categories: List<LiveCategory>,
    selectedCategoryId: String,
    streams: List<LiveStream>,
    favorites: List<FavoriteChannelEntity>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onPlayChannel: (LiveStream) -> Unit,
    onToggleFavorite: (LiveStream, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteIds = favorites.map { it.streamId }.toSet()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
    ) {
        // Search bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search channels...", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaZzeSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", tint = TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaZzePrimary,
                    unfocusedBorderColor = MaZzeSurfaceBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = MaZzeSurfaceElevated,
                    unfocusedContainerColor = MaZzeSurfaceElevated
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_channels_input")
            )
        }

        // Categories chip carousel
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category.categoryId == selectedCategoryId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category.categoryId) },
                        label = {
                            Text(
                                text = category.categoryName,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaZzePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaZzeSurfaceDark,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("category_chip_${category.categoryId}")
                    )
                }
            }
        }

        // Channels list
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaZzeSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Loading live channels...", color = TextSecondary)
                }
            }
        } else if (streams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No channels found",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (searchQuery.isNotBlank()) "No channels match '$searchQuery'" else "Check category or update credentials in Settings.",
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
                items(streams, key = { it.streamIdInt }) { stream ->
                    val isFav = favoriteIds.contains(stream.streamIdInt)
                    ChannelListItem(
                        stream = stream,
                        isFavorite = isFav,
                        onPlay = { onPlayChannel(stream) },
                        onToggleFavorite = { onToggleFavorite(stream, isFav) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelListItem(
    stream: LiveStream,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPlay() }
            .testTag("channel_item_${stream.streamIdInt}"),
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Icon
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaZzeSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (!stream.streamIcon.isNullOrBlank()) {
                    AsyncImage(
                        model = stream.streamIcon,
                        contentDescription = stream.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(46.dp)
                            .padding(4.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = MaZzeSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Channel Name & Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaZzePrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaZzeSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    if (stream.num != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CH ${stream.num}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            // Favorite star button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("favorite_button_${stream.streamIdInt}")
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (isFavorite) MaZzeAccentAmber else TextMuted
                )
            }

            // Play button
            IconButton(
                onClick = onPlay,
                modifier = Modifier.testTag("play_button_${stream.streamIdInt}")
            ) {
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
