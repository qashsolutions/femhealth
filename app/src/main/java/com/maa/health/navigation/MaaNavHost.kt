package com.maa.health.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maa.health.ui.screens.home.HomeScreen
import com.maa.health.ui.screens.onboarding.LanguageSelectionScreen
import com.maa.health.ui.screens.onboarding.LifecycleStageSelectionScreen
import com.maa.health.ui.screens.onboarding.PhoneAuthScreen
import com.maa.health.ui.screens.onboarding.ProfileSetupScreen
import com.maa.health.ui.screens.onboarding.SplashScreen

/**
 * Main navigation host for Maa app
 *
 * Handles all navigation between screens with appropriate transitions
 */
@Composable
fun MaaNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    innerPadding: PaddingValues = PaddingValues()
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding),
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(300))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(300))
        }
    ) {
        // ═══════════════════════════════════════════════════════════════════════════
        // ONBOARDING
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLanguage = {
                    navController.navigate(Screen.LanguageSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.LanguageSelection.route) {
            LanguageSelectionScreen(
                onLanguageSelected = {
                    navController.navigate(Screen.PhoneAuth.route)
                }
            )
        }

        composable(Screen.PhoneAuth.route) {
            PhoneAuthScreen(
                onOtpSent = { phoneNumber ->
                    navController.navigate(Screen.OtpVerification.createRoute(phoneNumber))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument(NavArgs.PHONE_NUMBER) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val phoneNumber = backStackEntry.arguments?.getString(NavArgs.PHONE_NUMBER) ?: ""
            // OtpVerificationScreen - placeholder
            PlaceholderScreen(
                title = "OTP Verification",
                subtitle = "Verifying $phoneNumber",
                onAction = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.LanguageSelection.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.ProfileSetup.route) {
            ProfileSetupScreen(
                onProfileComplete = {
                    navController.navigate(Screen.LifecycleStageSelection.route)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.LifecycleStageSelection.route) {
            LifecycleStageSelectionScreen(
                onStageSelected = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // MAIN SCREENS
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBodyMap = {
                    navController.navigate(Screen.BodyMap.route)
                },
                onNavigateToPregnancy = {
                    navController.navigate(Screen.PregnancyHome.route)
                },
                onNavigateToChildCare = {
                    navController.navigate(Screen.ChildCareHome.route)
                },
                onNavigateToCycle = {
                    navController.navigate(Screen.CycleHome.route)
                },
                onNavigateToMentalHealth = {
                    navController.navigate(Screen.MentalHealthHome.route)
                },
                onNavigateToEmergency = {
                    navController.navigate(Screen.EmergencyScreen.route)
                }
            )
        }

        composable(Screen.Insights.route) {
            PlaceholderScreen(
                title = "Insights",
                subtitle = "Your health patterns and trends",
                onAction = {}
            )
        }

        composable(Screen.History.route) {
            PlaceholderScreen(
                title = "History",
                subtitle = "Your health timeline",
                onAction = {}
            )
        }

        composable(Screen.Settings.route) {
            PlaceholderScreen(
                title = "Settings",
                subtitle = "App preferences",
                onAction = {}
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // BODY MAP & SYMPTOM TRIAGE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.BodyMap.route) {
            PlaceholderScreen(
                title = "Body Map",
                subtitle = "Tap a body region to log symptoms",
                onAction = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SymptomEntry.route,
            arguments = listOf(
                navArgument(NavArgs.REGION) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val region = backStackEntry.arguments?.getString(NavArgs.REGION) ?: ""
            PlaceholderScreen(
                title = "Log Symptom",
                subtitle = "Region: $region",
                onAction = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // PREGNANCY MODULE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.PregnancyHome.route) {
            PlaceholderScreen(
                title = "Pregnancy",
                subtitle = "Track your pregnancy journey",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.KickCounter.route) {
            PlaceholderScreen(
                title = "Kick Counter",
                subtitle = "Track baby movements",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.ContractionTimer.route) {
            PlaceholderScreen(
                title = "Contraction Timer",
                subtitle = "Time your contractions",
                onAction = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // CHILD CARE MODULE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.ChildCareHome.route) {
            PlaceholderScreen(
                title = "Child Care",
                subtitle = "Track your child's health",
                onAction = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.VaccinationSchedule.route,
            arguments = listOf(
                navArgument(NavArgs.CHILD_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString(NavArgs.CHILD_ID) ?: ""
            PlaceholderScreen(
                title = "Vaccination Schedule",
                subtitle = "Child: $childId",
                onAction = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.GrowthChart.route,
            arguments = listOf(
                navArgument(NavArgs.CHILD_ID) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val childId = backStackEntry.arguments?.getString(NavArgs.CHILD_ID) ?: ""
            PlaceholderScreen(
                title = "Growth Chart",
                subtitle = "Child: $childId",
                onAction = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // CYCLE TRACKING MODULE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.CycleHome.route) {
            PlaceholderScreen(
                title = "Cycle Tracking",
                subtitle = "Track your menstrual cycle",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.CycleCalendar.route) {
            PlaceholderScreen(
                title = "Cycle Calendar",
                subtitle = "View your cycle calendar",
                onAction = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // MENTAL HEALTH MODULE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.MentalHealthHome.route) {
            PlaceholderScreen(
                title = "Mental Wellness",
                subtitle = "Track your mood and wellbeing",
                onAction = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Screening.route,
            arguments = listOf(
                navArgument(NavArgs.SCREENING_TYPE) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString(NavArgs.SCREENING_TYPE) ?: ""
            PlaceholderScreen(
                title = "Mental Health Screening",
                subtitle = "Type: $type",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.CrisisSupport.route) {
            PlaceholderScreen(
                title = "Crisis Support",
                subtitle = "Get help now",
                onAction = { navController.popBackStack() }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // EMERGENCY
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.EmergencyScreen.route) {
            PlaceholderScreen(
                title = "Emergency",
                subtitle = "Get immediate help",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.NearbyFacilities.route) {
            PlaceholderScreen(
                title = "Nearby Facilities",
                subtitle = "Find healthcare near you",
                onAction = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Placeholder screen for unimplemented features
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    onAction: () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .padding(PaddingValues())
            .then(Modifier),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.headlineMedium
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(vertical = androidx.compose.ui.unit.dp(8))
            )
            androidx.compose.material3.Text(
                text = subtitle,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            androidx.compose.foundation.layout.Spacer(
                modifier = Modifier.padding(vertical = androidx.compose.ui.unit.dp(16))
            )
            androidx.compose.material3.Button(onClick = onAction) {
                androidx.compose.material3.Text("Back")
            }
        }
    }
}
