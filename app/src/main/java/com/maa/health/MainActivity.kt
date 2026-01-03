package com.maa.health

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.maa.health.auth.BiometricAuthManager
import com.maa.health.data.repository.AccountDeletionManager
import com.maa.health.ui.MaaApp
import com.maa.health.ui.theme.MaaColors
import com.maa.health.ui.theme.MaaTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main Activity for Maa
 *
 * Single-activity architecture using Jetpack Compose Navigation
 * Features bottom navigation bar for global navigation
 *
 * Authentication Flow:
 * 1. Language selection
 * 2. Face ID / Biometric (primary)
 * 3. Phone OTP (fallback)
 * 4. Profile setup
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager

    @Inject
    lateinit var accountDeletionManager: AccountDeletionManager

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
                    MaaApp(
                        biometricAuthManager = biometricAuthManager,
                        accountDeletionManager = accountDeletionManager
                    )
                }
            }
        }
    }
}
