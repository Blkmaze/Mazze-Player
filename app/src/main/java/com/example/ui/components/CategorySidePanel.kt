package com.example.ui.components

import android.content.res.Configuration
import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.MaZzeSurfaceDark
import com.example.ui.theme.MaZzeSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

data class CategoryItemData(
    val id: String,
    val name: String,
    val count: Int? = null
)

@Composable
fun CategorySidePanel(
    categories: List<CategoryItemData>,
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Categories",
    selectedCategoryFocusRequester: FocusRequester = remember { FocusRequester() },
    onNavigateRight: (() -> Unit)? = null,
    listState: LazyListState = rememberLazyListState()
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val panelWidth = if (isLandscape) 175.dp else 135.dp
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val selectedIndex = remember(categories, selectedCategoryId) {
        categories.indexOfFirst { it.id == selectedCategoryId }.coerceAtLeast(0)
    }

    Surface(
        color = MaZzeSurfaceDark,
        modifier = modifier
            .width(panelWidth)
            .fillMaxHeight()
            .border(width = 0.5.dp, color = MaZzeSurfaceBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaZzeSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaZzeSecondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            // Top and Bottom Jump Buttons at the start of the category panel
            JumpBarControl(
                onJumpToTop = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
                onJumpToBottom = {
                    coroutineScope.launch {
                        if (categories.isNotEmpty()) {
                            listState.animateScrollToItem(categories.size - 1)
                        }
                    }
                },
                label = "${categories.size} Cats",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                tagPrefix = "cat",
                onNavigateRight = onNavigateRight,
                onNavigateDown = {
                    coroutineScope.launch {
                        if (categories.isNotEmpty() && selectedIndex in categories.indices) {
                            try {
                                listState.scrollToItem(selectedIndex)
                            } catch (_: Exception) {}
                            try {
                                selectedCategoryFocusRequester.requestFocus()
                            } catch (_: Exception) {}
                        }
                    }
                }
            )

            // Scrollable Category List with TV D-pad focusability and fast remote navigation
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .fastTvKeyNavigation(
                        lazyListState = listState,
                        itemCount = categories.size,
                        jumpStep = 10,
                        coroutineScope = coroutineScope
                    )
                    .testTag("category_side_panel_list"),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(categories, key = { _, it -> it.id }) { index, cat ->
                    val isSelected = cat.id == selectedCategoryId
                    val isLastItem = index == categories.size - 1

                    CategoryPanelRow(
                        item = cat,
                        isSelected = isSelected,
                        onClick = { onSelectCategory(cat.id) },
                        focusRequester = if (isSelected) selectedCategoryFocusRequester else null,
                        onNavigateRight = onNavigateRight,
                        onNavigateDown = if (isLastItem) {
                            { focusManager.moveFocus(FocusDirection.Down) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryPanelRow(
    item: CategoryItemData,
    isSelected: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
    onNavigateRight: (() -> Unit)? = null,
    onNavigateDown: (() -> Unit)? = null
) {
    val backgroundColor = if (isSelected) MaZzePrimary.copy(alpha = 0.35f) else Color.Transparent
    val textColor = if (isSelected) Color.White else TextSecondary

    var rowModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(backgroundColor)
        .tvFocusable(shape = RoundedCornerShape(8.dp))

    if (focusRequester != null) {
        rowModifier = rowModifier.focusRequester(focusRequester)
    }

    rowModifier = rowModifier
        .onPreviewKeyEvent { event ->
            if (event.type == KeyEventType.KeyDown) {
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && onNavigateRight != null) {
                    onNavigateRight()
                    return@onPreviewKeyEvent true
                }
                if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_DOWN && onNavigateDown != null) {
                    onNavigateDown()
                    return@onPreviewKeyEvent true
                }
            }
            false
        }
        .clickable { onClick() }
        .padding(horizontal = 10.dp, vertical = 9.dp)
        .testTag("side_category_${item.id}")

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active indicator line
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(22.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaZzeSecondary)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 15.sp,
                fontSize = 12.sp
            )
            if (item.count != null) {
                Text(
                    text = "${item.count} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaZzeSecondary else TextMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}
