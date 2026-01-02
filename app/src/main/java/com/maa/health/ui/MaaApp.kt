package com.maa.health.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.maa.health.auth.BiometricAuthManager
import com.maa.health.navigation.MaaNavHost
import com.maa.health.navigation.Screen
import com.maa.health.ui.components.MaaBottomNavBar
import com.maa.health.ui.components.NavItem

/**
 * Main App composable with bottom navigation
 *
 * Provides the scaffold structure with:
 * - Bottom navigation bar (visible on main screens)
 * - Content area for navigation host
 */
@Composable
fun MaaApp(
    biometricAuthManager: BiometricAuthManager
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Determine if bottom nav should be visible
    val showBottomNav = remember(currentRoute) {
        currentRoute in listOf(
            Screen.Home.route,
            Screen.TrackHub.route,
            Screen.LearnHub.route,
            Screen.CareHub.route,
            Screen.YouHub.route,
            // Also show on child screens of these tabs
            Screen.CycleHome.route,
            Screen.MentalHealthHome.route,
            Screen.PregnancyHome.route,
            Screen.ChildCareHome.route,
            Screen.Settings.route
        )
    }

    // Hide during onboarding
    val isOnboarding = remember(currentRoute) {
        currentRoute in listOf(
            Screen.Splash.route,
            Screen.LanguageSelection.route,
            Screen.Auth.route,
            Screen.PhoneAuth.route,
            Screen.ProfileSetup.route,
            Screen.LifecycleStageSelection.route
        ) || currentRoute.startsWith("otp_verification") || currentRoute.startsWith("onboarding/otp")
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav && !isOnboarding,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                MaaBottomNavBar(
                    currentRoute = mapRouteToNavItem(currentRoute),
                    onNavigate = { navItem ->
                        val route = navItemToRoute(navItem)
                        navController.navigate(route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MaaNavHost(
                navController = navController,
                biometricAuthManager = biometricAuthManager,
                startDestination = Screen.Splash.route
            )
        }
    }
}

/**
 * Map current route to NavItem for bottom nav selection
 */
private fun mapRouteToNavItem(route: String): String {
    return when {
        route == Screen.Home.route -> NavItem.HOME.route
        route == Screen.TrackHub.route ||
        route == Screen.CycleHome.route ||
        route == Screen.MentalHealthHome.route ||
        route == Screen.BodyMap.route ||
        route.startsWith("symptom") -> NavItem.TRACK.route

        route == Screen.LearnHub.route -> NavItem.LEARN.route

        route == Screen.CareHub.route ||
        route == Screen.PregnancyHome.route ||
        route == Screen.ChildCareHome.route ||
        route.startsWith("vaccination") ||
        route.startsWith("growth") -> NavItem.CARE.route

        route == Screen.YouHub.route ||
        route == Screen.Settings.route -> NavItem.YOU.route

        else -> NavItem.HOME.route
    }
}

/**
 * Map NavItem to actual route
 */
private fun navItemToRoute(navItem: NavItem): String {
    return when (navItem) {
        NavItem.HOME -> Screen.Home.route
        NavItem.TRACK -> Screen.TrackHub.route
        NavItem.LEARN -> Screen.LearnHub.route
        NavItem.CARE -> Screen.CareHub.route
        NavItem.YOU -> Screen.YouHub.route
    }
}
