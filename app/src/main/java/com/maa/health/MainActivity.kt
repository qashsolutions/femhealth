package com.maa.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.maa.health.ui.MaaApp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for Maa
 *
 * Single-activity architecture using Jetpack Compose Navigation
 * Features bottom navigation bar for global navigation
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Handle splash screen
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        setContent {
            MaaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaaColors.background
                ) {
                    MaaApp()
                }
            }
        }
    }
}
