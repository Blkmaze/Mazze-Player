package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MaZzeSecondary

/**
 * Custom D-Pad focus modifier for Android TV and Fire TV:
 * - Clear focus highlighting with high-visibility cyan border
 * - Subtle scale-up animation on focus
 * - Hardware remote select/click support
 */
@Composable
fun Modifier.tvFocusable(
    shape: Shape = RoundedCornerShape(10.dp),
    focusBorderColor: Color = MaZzeSecondary,
    borderWidth: Dp = 2.dp,
    focusedScale: Float = 1.02f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
): Modifier {
    val isFocused by interactionSource.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        label = "tv_scale"
    )

    return this
        .scale(scale)
        .border(
            width = if (isFocused) borderWidth else 0.dp,
            color = if (isFocused) focusBorderColor else Color.Transparent,
            shape = shape
        )
        .focusable(interactionSource = interactionSource)
}
