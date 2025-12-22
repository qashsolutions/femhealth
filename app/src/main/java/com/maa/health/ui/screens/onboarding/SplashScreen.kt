package com.maa.health.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaTypography
import kotlinx.coroutines.delay

/**
 * Splash screen with app branding
 *
 * Determines navigation:
 * - First launch -> Language Selection
 * - Returning user -> Home
 */
@Composable
fun SplashScreen(
    onNavigateToLanguage: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(800),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        isVisible = true
        delay(1500)
        // TODO: Check if user is already onboarded
        // For now, always go to language selection
        onNavigateToLanguage()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App name in Hindi
            Text(
                text = "माँ",
                style = MaaTypography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaaColors.accent
            )

            Spacer(modifier = Modifier.height(8.dp))

            // App name in English
            Text(
                text = "Maa",
                style = MaaTypography.headlineMedium,
                color = MaaColors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tagline
            Text(
                text = "Women's Health Companion",
                style = MaaTypography.bodyMedium,
                color = MaaColors.textSecondary
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaaColors.accent,
                strokeWidth = 2.dp
            )
        }
    }
}
