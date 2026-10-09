package com.example.ui.components

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Jump navigation bar with Top and Bottom buttons.
 * Used at the top of category side panel and list columns.
 * Only triggers jump when explicitly selected and pressed with OK/Enter, never on DPAD LEFT/RIGHT.
 */
@Composable
fun JumpBarControl(
    onJumpToTop: () -> Unit,
    onJumpToBottom: () -> Unit,
    label: String = "Jump",
    modifier: Modifier = Modifier,
    tagPrefix: String = "jump",
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateRight: (() -> Unit)? = null,
    onNavigateDown: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaZzeSurfaceElevated.copy(alpha = 0.7f))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Jump to Top button: only triggers jump on OK/Enter/Click, never on DPAD LEFT/RIGHT
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaZzeSurfaceDark)
                .tvFocusable(shape = RoundedCornerShape(6.dp), borderWidth = 2.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        val keyCode = event.nativeKeyEvent.keyCode
                        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && onNavigateLeft != null) {
                            onNavigateLeft()
                            return@onPreviewKeyEvent true
                        }
                        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && onNavigateDown != null) {
                            onNavigateDown()
                            return@onPreviewKeyEvent true
                        }
                    }
                    false
                }
                .clickable { onJumpToTop() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("${tagPrefix}_jump_top"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerticalAlignTop,
                contentDescription = "Jump to Top",
                tint = MaZzeSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = " Top",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaZzeSecondary
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            color = TextMuted
        )

        // Jump to Bottom button: only triggers jump on OK/Enter/Click, never on DPAD LEFT/RIGHT
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaZzeSurfaceDark)
                .tvFocusable(shape = RoundedCornerShape(6.dp), borderWidth = 2.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyDown) {
                        val keyCode = event.nativeKeyEvent.keyCode
                        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && onNavigateRight != null) {
                            onNavigateRight()
                            return@onPreviewKeyEvent true
                        }
                        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && onNavigateDown != null) {
                            onNavigateDown()
                            return@onPreviewKeyEvent true
                        }
                    }
                    false
                }
                .clickable { onJumpToBottom() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag("${tagPrefix}_jump_bottom"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VerticalAlignBottom,
                contentDescription = "Jump to Bottom",
                tint = MaZzeSecondary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = " Bottom",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaZzeSecondary
            )
        }
    }
}

/**
 * Compact vertical A-Z / 0-9 index strip on the right edge of channel / movie / series lists.
 * Allows quick jumping to items starting with that character.
 */
@Composable
fun AlphabetJumpStrip(
    availableLetters: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = "alphabet"
) {
    val alphabet = remember {
        listOf('#') + ('A'..'Z').toList()
    }

    Surface(
        color = MaZzeSurfaceDark.copy(alpha = 0.95f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaZzeSurfaceBorder),
        modifier = modifier
            .width(28.dp)
            .fillMaxHeight()
            .testTag("${tagPrefix}_strip")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            items(alphabet) { char ->
                val hasItems = if (char == '#') {
                    availableLetters.any { it.isDigit() || !it.isLetter() }
                } else {
                    availableLetters.contains(char)
                }

                AlphabetCharItem(
                    char = char,
                    hasItems = hasItems,
                    onClick = { onLetterSelected(char) },
                    tagPrefix = tagPrefix
                )
            }
        }
    }
}

@Composable
private fun AlphabetCharItem(
    char: Char,
    hasItems: Boolean,
    onClick: () -> Unit,
    tagPrefix: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val textColor = when {
        isFocused -> Color.White
        hasItems -> MaZzeSecondary
        else -> TextMuted.copy(alpha = 0.35f)
    }

    val bgColor = when {
        isFocused -> MaZzePrimary
        else -> Color.Transparent
    }

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(
                width = if (isFocused) 1.5.dp else 0.dp,
                color = if (isFocused) MaZzeSecondary else Color.Transparent,
                shape = CircleShape
            )
            .focusable(interactionSource = interactionSource)
            .clickable(enabled = hasItems) { onClick() }
            .testTag("${tagPrefix}_char_$char"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isFocused || hasItems) FontWeight.Bold else FontWeight.Normal
        )
    }
}

/**
 * Key handling helper modifier for fast navigation:
 * 1) Page Up / Page Down (KEYCODE_PAGE_UP, KEYCODE_PAGE_DOWN) -> jumps jumpStep items
 * 2) Channel Up / Channel Down (KEYCODE_CHANNEL_UP, KEYCODE_CHANNEL_DOWN) -> jumps jumpStep items
 * 3) Fast Forward / Rewind (KEYCODE_MEDIA_FAST_FORWARD, KEYCODE_MEDIA_REWIND) -> jumps jumpStep items
 * 4) Media Next / Media Previous (KEYCODE_MEDIA_NEXT, KEYCODE_MEDIA_PREVIOUS) -> jumps jumpStep items
 * 5) Holding D-Pad Up / Down accelerates scrolling (detects repeat count or rapid successive events)
 */
fun Modifier.fastTvKeyNavigation(
    lazyListState: LazyListState? = null,
    lazyGridState: LazyGridState? = null,
    itemCount: Int,
    jumpStep: Int = 10,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onNavigateLeft: (() -> Unit)? = null,
    onNavigateDownToBottomBar: (() -> Unit)? = null
): Modifier = this.onPreviewKeyEvent { event ->
    val nativeEvent = event.nativeKeyEvent
    val keyCode = nativeEvent.keyCode
    val repeatCount = nativeEvent.repeatCount

    val isPageUp = keyCode == KeyEvent.KEYCODE_PAGE_UP ||
            keyCode == KeyEvent.KEYCODE_CHANNEL_UP ||
            keyCode == KeyEvent.KEYCODE_MEDIA_REWIND ||
            keyCode == KeyEvent.KEYCODE_MEDIA_PREVIOUS

    val isPageDown = keyCode == KeyEvent.KEYCODE_PAGE_DOWN ||
            keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN ||
            keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ||
            keyCode == KeyEvent.KEYCODE_MEDIA_NEXT

    val isDpadUp = keyCode == KeyEvent.KEYCODE_DPAD_UP
    val isDpadDown = keyCode == KeyEvent.KEYCODE_DPAD_DOWN
    val isDpadLeft = keyCode == KeyEvent.KEYCODE_DPAD_LEFT

    if (event.type == KeyEventType.KeyDown) {
        // 1. Page/Channel/Media fast jump 10 items
        if (isPageUp) {
            coroutineScope.launch {
                val currentIndex = lazyListState?.firstVisibleItemIndex ?: lazyGridState?.firstVisibleItemIndex ?: 0
                val targetIndex = (currentIndex - jumpStep).coerceAtLeast(0)
                lazyListState?.animateScrollToItem(targetIndex)
                lazyGridState?.animateScrollToItem(targetIndex)
            }
            return@onPreviewKeyEvent true
        }

        if (isPageDown) {
            coroutineScope.launch {
                val currentIndex = lazyListState?.firstVisibleItemIndex ?: lazyGridState?.firstVisibleItemIndex ?: 0
                val targetIndex = (currentIndex + jumpStep).coerceAtMost((itemCount - 1).coerceAtLeast(0))
                lazyListState?.animateScrollToItem(targetIndex)
                lazyGridState?.animateScrollToItem(targetIndex)
            }
            return@onPreviewKeyEvent true
        }

        // 2. Holding DPAD up/down accelerates scrolling (repeat count >= 2 jumps several items)
        if (repeatCount >= 2 && (isDpadUp || isDpadDown)) {
            val accelStep = when {
                repeatCount > 10 -> 8
                repeatCount > 5 -> 5
                else -> 3
            }
            coroutineScope.launch {
                val currentIndex = lazyListState?.firstVisibleItemIndex ?: lazyGridState?.firstVisibleItemIndex ?: 0
                val targetIndex = if (isDpadUp) {
                    (currentIndex - accelStep).coerceAtLeast(0)
                } else {
                    (currentIndex + accelStep).coerceAtMost((itemCount - 1).coerceAtLeast(0))
                }
                lazyListState?.scrollToItem(targetIndex)
                lazyGridState?.scrollToItem(targetIndex)
            }
            // Let the key propagate or consume if at boundary
            return@onPreviewKeyEvent false
        }
    }

    false
}
