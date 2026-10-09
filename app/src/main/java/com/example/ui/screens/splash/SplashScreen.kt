package com.example.ui.screens.splash

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MaZzeDarkBackground
import com.example.ui.theme.MaZzePrimary
import com.example.ui.theme.MaZzePrimaryVariant
import com.example.ui.theme.MaZzeSecondary
import com.example.ui.theme.MaZzeSurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    tvSafeHoriz: Dp,
    tvSafeVert: Dp,
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = remember(context) { context as? ComponentActivity }

    // Pressing Back during the splash exits normally
    BackHandler {
        activity?.finish()
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val animAlpha = remember { Animatable(0f) }
    val animScale = remember { Animatable(0.76f) }

    LaunchedEffect(Unit) {
        // Run short fade and scale animation
        launch {
            animAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        launch {
            animScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
            )
        }
        // Total display duration ~2.1 seconds before fading into Home screen (total ~2.5s)
        delay(2100L)
        onSplashComplete()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaZzeDarkBackground)
            .padding(horizontal = tvSafeHoriz, vertical = tvSafeVert)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .graphicsLayer {
                    alpha = animAlpha.value
                    scaleX = animScale.value
                    scaleY = animScale.value
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // MaZze Branded Logo Icon
            val logoSize = if (isLandscape) 96.dp else 84.dp
            val iconSize = if (isLandscape) 52.dp else 46.dp

            Box(
                modifier = Modifier
                    .size(logoSize)
                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(24.dp), spotColor = MaZzePrimary)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaZzePrimary,
                                MaZzePrimaryVariant,
                                Color(0xFF4A148C)
                            )
                        )
                    )
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaZzeSecondary.copy(alpha = 0.8f),
                                MaZzePrimary.copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("splash_logo"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tv,
                    contentDescription = "MaZze Logo",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // App Name
            Text(
                text = "MaZze",
                fontSize = if (isLandscape) 40.sp else 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.5.sp,
                modifier = Modifier.testTag("splash_title")
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Subtitle / Tagline
            Text(
                text = "Android TV • Fire TV • Mobile",
                fontSize = 12.sp,
                color = TextSecondary,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 28.dp else 36.dp))

            // Subtle Loading Indicator underneath
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .testTag("splash_loader"),
                    color = MaZzeSecondary,
                    trackColor = MaZzeSurfaceBorder,
                    strokeWidth = 2.5.dp
                )

                Text(
                    text = "Loading media engine...",
                    fontSize = 11.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
