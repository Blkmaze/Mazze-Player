package com.example.ui.screens.series

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SeriesDetail
import com.example.data.model.SeriesEpisode
import com.example.data.model.SeriesItem
import com.example.ui.components.tvFocusable
import com.example.ui.theme.MaZzeAccentAmber
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SeriesDetailScreen(
    seriesItem: SeriesItem,
    seriesDetail: SeriesDetail?,
    isLoading: Boolean,
    onBack: () -> Unit,
    onPlayEpisode: (SeriesEpisode, SeriesItem) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    var selectedSeasonNumber by remember(seriesDetail) {
        val firstSeason = seriesDetail?.seasons?.firstOrNull()?.seasonNumber ?: 1
        mutableIntStateOf(firstSeason)
    }

    val episodesForSeason = seriesDetail?.episodesBySeason?.get(selectedSeasonNumber) ?: emptyList()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
    ) {
        // Hero Header with Poster & Metadata
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                if (!seriesItem.cover.isNullOrBlank()) {
                    AsyncImage(
                        model = seriesItem.cover,
                        contentDescription = seriesItem.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaZzeSurfaceElevated)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.85f),
                                    MaZzeDarkBackground
                                )
                            )
                        )
                )

                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .tvFocusable(shape = CircleShape)
                        .testTag("series_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = seriesItem.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaZzePrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SERIES",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        if (seriesItem.rating != null && seriesItem.rating.toString().isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MaZzeAccentAmber, modifier = Modifier.size(14.dp))
                                Text(
                                    text = " ${seriesItem.rating}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (!seriesItem.releaseDate.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "• ${seriesItem.releaseDate}", color = TextSecondary, fontSize = 12.sp)
                        }
                        if (!seriesItem.genre.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "• ${seriesItem.genre}", color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Synopsis / Plot
        item {
            if (!seriesItem.plot.isNullOrBlank()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Synopsis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaZzeSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = seriesItem.plot,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Seasons selector tabs
        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaZzeSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading episodes...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else if (seriesDetail != null && seriesDetail.seasons.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Seasons & Episodes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(seriesDetail.seasons) { season ->
                            val isSelected = season.seasonNumber == selectedSeasonNumber
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedSeasonNumber = season.seasonNumber },
                                label = { Text(season.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaZzePrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaZzeSurfaceDark,
                                    labelColor = TextSecondary
                                ),
                                modifier = Modifier
                                    .tvFocusable(shape = RoundedCornerShape(8.dp))
                                    .testTag("season_chip_${season.seasonNumber}")
                            )
                        }
                    }
                }
            }
        }

        // Episodes List
        if (episodesForSeason.isNotEmpty()) {
            items(episodesForSeason, key = { it.id }) { episode ->
                EpisodeItemCard(
                    episode = episode,
                    seriesCover = seriesItem.cover,
                    onClick = { onPlayEpisode(episode, seriesItem) }
                )
            }
        } else if (!isLoading && seriesDetail != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No episodes found for this season.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun EpisodeItemCard(
    episode: SeriesEpisode,
    seriesCover: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .tvFocusable(shape = RoundedCornerShape(10.dp), focusedScale = 1.015f)
            .clickable { onClick() }
            .testTag("episode_item_${episode.id}"),
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp, 45.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaZzeSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (!seriesCover.isNullOrBlank()) {
                    AsyncImage(
                        model = seriesCover,
                        contentDescription = episode.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = MaZzeSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "E${episode.episodeNum}. ${episode.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                if (!episode.duration.isNullOrBlank()) {
                    Text(
                        text = episode.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaZzeAccentAmber,
                        fontSize = 10.sp
                    )
                }
                if (!episode.plot.isNullOrBlank()) {
                    Text(
                        text = episode.plot,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        maxLines = 2,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            IconButton(
                onClick = onClick,
                modifier = Modifier.tvFocusable(shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play episode",
                    tint = MaZzeSecondary
                )
            }
        }
    }
}
