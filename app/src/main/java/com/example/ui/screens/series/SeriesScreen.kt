package com.example.ui.screens.series

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoLibrary
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.SeriesCategory
import com.example.data.model.SeriesItem
import com.example.ui.components.AlphabetJumpStrip
import com.example.ui.components.CategoryItemData
import com.example.ui.components.CategorySidePanel
import com.example.ui.components.JumpBarControl
import com.example.ui.components.fastTvKeyNavigation
import com.example.ui.components.tvFocusable
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
import kotlinx.coroutines.launch

@Composable
fun SeriesScreen(
    categories: List<SeriesCategory>,
    selectedCategoryId: String,
    seriesList: List<SeriesItem>,
    isLoading: Boolean,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (String) -> Unit,
    onOpenSeries: (SeriesItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Extract available starting letters for A-Z strip
    val availableLetters = remember(seriesList) {
        seriesList.mapNotNull { it.name.trim().firstOrNull()?.uppercaseChar() }.toSet()
    }

    val sideCategories = remember(categories, seriesList) {
        val totalCount = seriesList.size
        categories.map { cat ->
            val count = if (cat.categoryId == "all") {
                totalCount
            } else {
                seriesList.count { it.categoryId == cat.categoryId }.takeIf { it > 0 }
            }
            CategoryItemData(
                id = cat.categoryId,
                name = cat.categoryName,
                count = count
            )
        }
    }

    val selectedCategoryFocusRequester = remember { FocusRequester() }
    val categoryListState = rememberLazyListState()
    val firstVisibleSeriesFocusRequester = remember { FocusRequester() }

    val selectedCategoryIndex = remember(sideCategories, selectedCategoryId) {
        sideCategories.indexOfFirst { it.id == selectedCategoryId }.coerceAtLeast(0)
    }

    val navigateToSelectedCategory: () -> Unit = {
        coroutineScope.launch {
            if (sideCategories.isNotEmpty() && selectedCategoryIndex in sideCategories.indices) {
                try {
                    categoryListState.scrollToItem(selectedCategoryIndex)
                } catch (_: Exception) {}
            }
            try {
                selectedCategoryFocusRequester.requestFocus()
            } catch (_: Exception) {
                kotlinx.coroutines.delay(16)
                try {
                    selectedCategoryFocusRequester.requestFocus()
                } catch (_: Exception) {
                    focusManager.moveFocus(FocusDirection.Left)
                }
            }
        }
    }

    val navigateToFirstVisibleSeries: () -> Unit = {
        coroutineScope.launch {
            try {
                firstVisibleSeriesFocusRequester.requestFocus()
            } catch (_: Exception) {
                kotlinx.coroutines.delay(16)
                try {
                    firstVisibleSeriesFocusRequester.requestFocus()
                } catch (_: Exception) {
                    focusManager.moveFocus(FocusDirection.Right)
                }
            }
        }
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
    ) {
        CategorySidePanel(
            categories = sideCategories,
            selectedCategoryId = selectedCategoryId,
            onSelectCategory = onSelectCategory,
            title = "Series",
            selectedCategoryFocusRequester = selectedCategoryFocusRequester,
            onNavigateRight = navigateToFirstVisibleSeries,
            listState = categoryListState
        )

        Spacer(modifier = Modifier.width(8.dp))

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
                    placeholder = { Text("Search TV series...", color = TextMuted, fontSize = 13.sp) },
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
                        .tvFocusable(shape = RoundedCornerShape(10.dp), focusedScale = 1.01f)
                        .onPreviewKeyEvent { event ->
                            if (event.type == KeyEventType.KeyDown) {
                                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                                    navigateToSelectedCategory()
                                    return@onPreviewKeyEvent true
                                }
                                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                                    navigateToFirstVisibleSeries()
                                    return@onPreviewKeyEvent true
                                }
                            }
                            false
                        }
                        .testTag("search_series_input")
                )

                // Top & Bottom Jump buttons for series poster grid
                JumpBarControl(
                    onJumpToTop = {
                        coroutineScope.launch {
                            gridState.animateScrollToItem(0)
                        }
                    },
                    onJumpToBottom = {
                        coroutineScope.launch {
                            if (seriesList.isNotEmpty()) {
                                gridState.animateScrollToItem(seriesList.size - 1)
                            }
                        }
                    },
                    label = "${seriesList.size} Series",
                    modifier = Modifier.width(190.dp),
                    tagPrefix = "series",
                    onNavigateLeft = {
                        focusManager.moveFocus(FocusDirection.Left)
                    },
                    onNavigateDown = navigateToFirstVisibleSeries
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaZzeSecondary, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Loading TV series...", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            } else if (seriesList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No series found",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (searchQuery.isNotBlank()) "No series match '$searchQuery'" else "Select another category.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val firstVisibleIndex = gridState.firstVisibleItemIndex
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 125.dp),
                        state = gridState,
                        contentPadding = PaddingValues(6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .fastTvKeyNavigation(
                                lazyGridState = gridState,
                                itemCount = seriesList.size,
                                jumpStep = 10,
                                coroutineScope = coroutineScope
                            )
                            .testTag("series_grid")
                    ) {
                        itemsIndexed(seriesList, key = { _, it -> it.seriesIdInt }) { index, series ->
                            val isFirstVisible = index == firstVisibleIndex || (firstVisibleIndex !in seriesList.indices && index == 0)
                            val isLast = index == seriesList.size - 1

                            SeriesGridCard(
                                series = series,
                                onClick = { onOpenSeries(series) },
                                focusRequester = if (isFirstVisible) firstVisibleSeriesFocusRequester else null,
                                onNavigateLeft = navigateToSelectedCategory,
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
                                    seriesList.indexOfFirst {
                                        val firstChar = it.name.trim().firstOrNull()
                                        firstChar != null && (firstChar.isDigit() || !firstChar.isLetter())
                                    }
                                } else {
                                    seriesList.indexOfFirst {
                                        it.name.trim().startsWith(char, ignoreCase = true)
                                    }
                                }
                                if (targetIndex >= 0) {
                                    gridState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                        modifier = Modifier.padding(start = 4.dp, end = 2.dp),
                        tagPrefix = "series_alpha"
                    )
                }
            }
        }
    }
}

@Composable
fun SeriesGridCard(
    series: SeriesItem,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateDown: (() -> Unit)? = null
) {
    val focusManager = LocalFocusManager.current
    var isLeftEdge by remember { mutableStateOf(false) }

    var cardModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .onGloballyPositioned { coordinates ->
            val x = coordinates.positionInParent().x
            isLeftEdge = x < 50f
        }
        .tvFocusable(shape = RoundedCornerShape(8.dp))

    if (focusRequester != null) {
        cardModifier = cardModifier.focusRequester(focusRequester)
    }

    cardModifier = cardModifier
        .onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (isLeftEdge && onNavigateLeft != null) {
                        onNavigateLeft()
                        return@onPreviewKeyEvent true
                    } else {
                        val moved = focusManager.moveFocus(FocusDirection.Left)
                        if (!moved && onNavigateLeft != null) {
                            onNavigateLeft()
                            return@onPreviewKeyEvent true
                        }
                        if (moved) return@onPreviewKeyEvent true
                    }
                }
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN && onNavigateDown != null) {
                    onNavigateDown()
                    return@onPreviewKeyEvent true
                }
            }
            false
        }
        .clickable { onClick() }
        .testTag("series_item_${series.seriesIdInt}")

    Card(
        modifier = cardModifier,
        colors = CardDefaults.cardColors(containerColor = MaZzeSurfaceDark),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaZzeSurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                if (!series.cover.isNullOrBlank()) {
                    AsyncImage(
                        model = series.cover,
                        contentDescription = series.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = null,
                        tint = MaZzeSecondary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (series.rating != null && series.rating.toString().isNotBlank() && series.rating.toString() != "0") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaZzeAccentAmber,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = " ${series.rating}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(6.dp)) {
                Text(
                    text = series.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    fontSize = 11.sp
                )
                Text(
                    text = series.genre ?: "TV Series",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
        }
    }
}
