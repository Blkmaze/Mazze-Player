package com.example.ui.screens.livetv

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FavoriteChannelEntity
import com.example.data.model.LiveCategory
import com.example.data.model.LiveStream
import com.example.ui.components.AlphabetJumpStrip
import com.example.ui.components.CategoryItemData
import com.example.ui.components.CategorySidePanel
import com.example.ui.components.JumpBarControl
import com.example.ui.components.fastTvKeyNavigation
import com.example.ui.components.tvFocusable
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

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
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Extract available starting letters for A-Z strip
    val availableLetters = remember(streams) {
        streams.mapNotNull { it.name.trim().firstOrNull()?.uppercaseChar() }.toSet()
    }

    // Transform categories into side-panel items with counts
    val sideCategories = remember(categories, streams) {
        val totalCount = streams.size
        categories.map { cat ->
            val count = if (cat.categoryId == "all") {
                totalCount
            } else {
                streams.count { it.categoryId == cat.categoryId }.takeIf { it > 0 }
            }
            CategoryItemData(
                id = cat.categoryId,
                name = cat.categoryName,
                count = count
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
    ) {
        // 1. Left-hand vertical Category side panel (persistent list with counts, NO chips across top)
        CategorySidePanel(
            categories = sideCategories,
            selectedCategoryId = selectedCategoryId,
            onSelectCategory = onSelectCategory,
            title = "Channels"
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 2. Right-hand area: Search bar, Channel Top/Bottom control, Channels List & A-Z quick jump strip
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(end = 2.dp)
        ) {
            // Search Bar & Jump Bar Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Search channels...", color = TextMuted, fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaZzeSecondary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaZzePrimary,
                        unfocusedBorderColor = MaZzeSurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = MaZzeSurfaceElevated,
                        unfocusedContainerColor = MaZzeSurfaceElevated
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .tvFocusable(shape = RoundedCornerShape(10.dp))
                        .testTag("search_channels_input")
                )

                // Top & Bottom Jump buttons for channel list
                JumpBarControl(
                    onJumpToTop = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    onJumpToBottom = {
                        coroutineScope.launch {
                            if (streams.isNotEmpty()) {
                                listState.animateScrollToItem(streams.size - 1)
                            }
                        }
                    },
                    label = "${streams.size} Channels",
                    modifier = Modifier.width(190.dp),
                    tagPrefix = "channel"
                )
            }

            // Channels list / state + A-Z strip
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaZzeSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Loading channels...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else if (streams.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No channels found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No results for '$searchQuery'" else "Select another category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Channel rows: fit cleanly within the right area width with Heart and Play buttons fully visible
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .fastTvKeyNavigation(
                                lazyListState = listState,
                                itemCount = streams.size,
                                jumpStep = 10,
                                coroutineScope = coroutineScope
                            )
                            .testTag("channels_list")
                    ) {
                        itemsIndexed(streams, key = { _, it -> it.streamIdInt }) { index, stream ->
                            val isFav = favoriteIds.contains(stream.streamIdInt)
                            val isFirst = index == 0
                            val isLast = index == streams.size - 1

                            ChannelRowItem(
                                stream = stream,
                                isFavorite = isFav,
                                onPlay = { onPlayChannel(stream) },
                                onToggleFavorite = { onToggleFavorite(stream, isFav) },
                                onNavigateLeft = if (isFirst) {
                                    { focusManager.moveFocus(FocusDirection.Left) }
                                } else null,
                                onNavigateDown = if (isLast) {
                                    { focusManager.moveFocus(FocusDirection.Down) }
                                } else null
                            )
                        }
                    }

                    // Quick-jump A-Z/0-9 letter strip on the right edge
                    AlphabetJumpStrip(
                        availableLetters = availableLetters,
                        onLetterSelected = { char ->
                            coroutineScope.launch {
                                val targetIndex = if (char == '#') {
                                    streams.indexOfFirst {
                                        val firstChar = it.name.trim().firstOrNull()
                                        firstChar != null && (firstChar.isDigit() || !firstChar.isLetter())
                                    }
                                } else {
                                    streams.indexOfFirst {
                                        it.name.trim().startsWith(char, ignoreCase = true)
                                    }
                                }
                                if (targetIndex >= 0) {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier.padding(start = 4.dp, end = 2.dp),
                        tagPrefix = "channel_alpha"
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelRowItem(
    stream: LiveStream,
    isFavorite: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateDown: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .tvFocusable(shape = RoundedCornerShape(10.dp))
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown) {
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT && onNavigateLeft != null) {
                        onNavigateLeft()
                        return@onPreviewKeyEvent true
                    }
                    if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN && onNavigateDown != null) {
                        onNavigateDown()
                        return@onPreviewKeyEvent true
                    }
                }
                false
            }
            .clickable { onPlay() }
            .testTag("channel_item_${stream.streamIdInt}"),
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Icon
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                            .size(36.dp)
                            .padding(2.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = MaZzeSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Channel Name & Info (weight ensures it never overflows or pushes the buttons off)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stream.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaZzePrimary.copy(alpha = 0.25f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "LIVE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaZzeSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    if (stream.num != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CH ${stream.num}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Action Buttons Container - guaranteed fully visible at the right end of the row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Favorite Heart button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isFavorite) Color(0x33FF2A6D) else MaZzeSurfaceElevated)
                        .tvFocusable(shape = RoundedCornerShape(18.dp), borderWidth = 2.dp)
                        .clickable { onToggleFavorite() }
                        .testTag("favorite_button_${stream.streamIdInt}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (isFavorite) Color(0xFFFF2A6D) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(19.dp)
                    )
                }

                // Play button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaZzeSecondary.copy(alpha = 0.18f))
                        .tvFocusable(shape = RoundedCornerShape(18.dp), borderWidth = 2.dp)
                        .clickable { onPlay() }
                        .testTag("play_button_${stream.streamIdInt}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play channel",
                        tint = MaZzeSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
