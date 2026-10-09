package com.example.ui.screens.vod

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.VodCategory
import com.example.data.model.VodStream
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
fun VodScreen(
    categories: List<VodCategory>,
    selectedCategoryId: String,
    streams: List<VodStream>,
    onSelectCategory: (String) -> Unit,
    onPlayMovie: (VodStream) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
    ) {
        // Categories
        if (categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category.categoryId == selectedCategoryId
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectCategory(category.categoryId) },
                        label = { Text(category.categoryName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaZzePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = MaZzeSurfaceDark,
                            labelColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("vod_cat_${category.categoryId}")
                    )
                }
            }
        }

        if (streams.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No movies available",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Check that your Xtream Codes subscription includes VOD content.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 140.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(streams, key = { it.streamIdInt }) { movie ->
                    MovieGridCard(
                        movie = movie,
                        onClick = { onPlayMovie(movie) }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieGridCard(
    movie: VodStream,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("movie_item_${movie.streamIdInt}"),
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaZzeSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (!movie.streamIcon.isNullOrBlank()) {
                    AsyncImage(
                        model = movie.streamIcon,
                        contentDescription = movie.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = MaZzeSecondary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                if (movie.rating != null && movie.rating.toString().isNotBlank() && movie.rating.toString() != "0") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaZzeAccentAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = " ${movie.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = movie.containerExtension?.uppercase() ?: "MP4",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaZzeAccentAmber,
                    fontSize = 10.sp
                )
            }
        }
    }
}
