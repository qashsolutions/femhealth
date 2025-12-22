package com.maa.health.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.maa.health.ui.screens.care.CareHubScreen
import com.maa.health.ui.screens.childcare.ChildCareHomeScreen
import com.maa.health.ui.screens.cycle.CycleHomeScreen
import com.maa.health.ui.screens.emergency.EmergencyScreen
import com.maa.health.ui.screens.home.HomeScreen
import com.maa.health.ui.screens.learn.LearnHubScreen
import com.maa.health.ui.screens.mentalhealth.MentalHealthHomeScreen
import com.maa.health.ui.screens.onboarding.LanguageSelectionScreen
import com.maa.health.ui.screens.onboarding.LifecycleStageSelectionScreen
import com.maa.health.ui.screens.onboarding.OtpVerificationScreen
import com.maa.health.ui.screens.onboarding.PhoneAuthScreen
import com.maa.health.ui.screens.onboarding.ProfileSetupScreen
import com.maa.health.ui.screens.onboarding.SplashScreen
import com.maa.health.ui.screens.pregnancy.PregnancyHomeScreen
import com.maa.health.ui.screens.settings.SettingsScreen
import com.maa.health.ui.screens.symptom.SymptomTriageScreen
import com.maa.health.ui.screens.track.TrackHubScreen
import com.maa.health.ui.screens.you.YouHubScreen

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
            OtpVerificationScreen(
                phoneNumber = phoneNumber,
                onVerified = {
                    navController.navigate(Screen.ProfileSetup.route) {
                        popUpTo(Screen.LanguageSelection.route) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
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

        // ═══════════════════════════════════════════════════════════════════════════
        // BOTTOM NAV HUB SCREENS
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.TrackHub.route) {
            TrackHubScreen(
                onNavigateToBodyMap = {
                    navController.navigate(Screen.BodyMap.route)
                },
                onNavigateToCycle = {
                    navController.navigate(Screen.CycleHome.route)
                },
                onNavigateToMood = {
                    navController.navigate(Screen.MentalHealthHome.route)
                },
                onNavigateToSymptomHistory = {
                    navController.navigate(Screen.TriageHistory.route)
                }
            )
        }

        composable(Screen.LearnHub.route) {
            LearnHubScreen(
                onNavigateToArticle = { articleId ->
                    navController.navigate(Screen.ArticleDetail.createRoute(articleId))
                },
                onNavigateToVideos = {
                    navController.navigate(Screen.HealthLibrary.route)
                },
                onNavigateToSearch = {
                    navController.navigate(Screen.FAQ.route)
                }
            )
        }

        composable(Screen.CareHub.route) {
            CareHubScreen(
                onNavigateToPregnancy = {
                    navController.navigate(Screen.PregnancyHome.route)
                },
                onNavigateToChildCare = {
                    navController.navigate(Screen.ChildCareHome.route)
                },
                onNavigateToVaccinations = {
                    navController.navigate(Screen.ChildCareHome.route)
                },
                onNavigateToMedications = {
                    navController.navigate(Screen.MedicationManager.route)
                },
                onNavigateToAppointments = {
                    // TODO: Appointments screen
                },
                onAddChild = {
                    navController.navigate(Screen.AddChild.route)
                }
            )
        }

        composable(Screen.YouHub.route) {
            YouHubScreen(
                onNavigateToEditProfile = {
                    navController.navigate(Screen.ProfileEdit.route)
                },
                onNavigateToLanguage = {
                    navController.navigate(Screen.LanguageSettings.route)
                },
                onNavigateToVoiceSettings = {
                    // TODO: Voice settings screen
                },
                onNavigateToNotifications = {
                    navController.navigate(Screen.NotificationSettings.route)
                },
                onNavigateToHealthMonitoring = {
                    // TODO: Health monitoring settings
                },
                onExportData = {
                    navController.navigate(Screen.DataExport.route)
                },
                onCloudSync = {
                    // TODO: Trigger cloud sync
                },
                onDeleteAccount = {
                    // TODO: Delete account confirmation
                },
                onNavigateToPrivacy = {
                    navController.navigate(Screen.PrivacySettings.route)
                },
                onNavigateToHelp = {
                    navController.navigate(Screen.HelpSupport.route)
                },
                onNavigateToPremium = {
                    navController.navigate(Screen.Subscription.route)
                },
                onLogout = {
                    navController.navigate(Screen.LanguageSelection.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Insights.route) {
            PlaceholderScreen(
                title = "Insights",
                subtitle = "Your health patterns and trends",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            PlaceholderScreen(
                title = "History",
                subtitle = "Your health timeline",
                onAction = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Screen.ProfileSetup.route) },
                onLanguageSettings = { navController.navigate(Screen.LanguageSelection.route) },
                onNotificationSettings = { /* TODO */ },
                onPrivacySettings = { /* TODO */ },
                onDataExport = { /* TODO */ },
                onAbout = { /* TODO */ },
                onLogout = {
                    navController.navigate(Screen.LanguageSelection.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // BODY MAP & SYMPTOM TRIAGE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.BodyMap.route) {
            SymptomTriageScreen(
                onBack = { navController.popBackStack() },
                onEmergency = { navController.navigate(Screen.EmergencyScreen.route) }
            )
        }

        composable(
            route = Screen.SymptomEntry.route,
            arguments = listOf(
                navArgument(NavArgs.REGION) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val region = backStackEntry.arguments?.getString(NavArgs.REGION) ?: ""
            SymptomTriageScreen(
                onBack = { navController.popBackStack() },
                onEmergency = { navController.navigate(Screen.EmergencyScreen.route) }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // PREGNANCY MODULE
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.PregnancyHome.route) {
            PregnancyHomeScreen(
                onBack = { navController.popBackStack() },
                onKickCounter = { navController.navigate(Screen.KickCounter.route) },
                onContractionTimer = { navController.navigate(Screen.ContractionTimer.route) },
                onEmergency = { navController.navigate(Screen.EmergencyScreen.route) }
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
            ChildCareHomeScreen(
                onBack = { navController.popBackStack() },
                onVaccinations = { childId ->
                    navController.navigate(Screen.VaccinationSchedule.createRoute(childId))
                },
                onGrowth = { childId ->
                    navController.navigate(Screen.GrowthChart.createRoute(childId))
                },
                onSymptoms = { navController.navigate(Screen.BodyMap.route) }
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
            CycleHomeScreen(
                onBack = { navController.popBackStack() }
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
            MentalHealthHomeScreen(
                onBack = { navController.popBackStack() },
                onCrisisSupport = { navController.navigate(Screen.CrisisSupport.route) }
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
            EmergencyScreen(
                onBack = { navController.popBackStack() },
                onFindHospital = { navController.navigate(Screen.NearbyFacilities.route) }
            )
        }

        // ═══════════════════════════════════════════════════════════════════════════
        // EMERGENCY
        // ═══════════════════════════════════════════════════════════════════════════

        composable(Screen.EmergencyScreen.route) {
            EmergencyScreen(
                onBack = { navController.popBackStack() },
                onFindHospital = { navController.navigate(Screen.NearbyFacilities.route) }
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
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
            Button(onClick = onAction) {
                Text("Back")
            }
        }
    }
}
