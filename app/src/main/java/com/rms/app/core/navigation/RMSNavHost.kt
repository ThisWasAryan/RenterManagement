package com.rms.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.rms.app.feature.documents.DocumentsScreen
import com.rms.app.feature.electricity.AddReadingScreen
import com.rms.app.feature.home.HomeScreen
import com.rms.app.feature.onboarding.OnboardingScreen
import com.rms.app.feature.payment.PaymentHistoryScreen
import com.rms.app.feature.reminders.ReminderScreen
import com.rms.app.feature.settings.SettingsScreen
import com.rms.app.feature.splash.SplashScreen
import com.rms.app.feature.tenant.add.AddTenantScreen
import com.rms.app.feature.tenant.detail.TenantDetailScreen

@Composable
fun RMSNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Onboarding
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToTenantDetail = { tenantId ->
                    navController.navigate(Screen.TenantDetail.createRoute(tenantId))
                },
                onNavigateToAddTenant = {
                    navController.navigate(Screen.AddTenant.createRoute())
                },
                onNavigateToAddReading = { tenantId ->
                    navController.navigate(Screen.AddElectricityReading.createRoute(tenantId))
                }
            )
        }

        // Tenant Detail
        composable(
            route = Screen.TenantDetail.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) {
            TenantDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditTenant = { tenantId ->
                    navController.navigate(Screen.AddTenant.createRoute(tenantId))
                },
                onAddReading = { tenantId ->
                    navController.navigate(Screen.AddElectricityReading.createRoute(tenantId))
                }
            )
        }

        // Add / Edit Tenant
        composable(
            route = Screen.AddTenant.route,
            arguments = listOf(
                navArgument("tenantId") {
                    type = NavType.StringType
                    defaultValue = "-1"
                }
            )
        ) {
            AddTenantScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Payment History
        composable(Screen.PaymentHistory.route) {
            PaymentHistoryScreen()
        }

        // Add Electricity Reading
        composable(
            route = Screen.AddElectricityReading.route,
            arguments = listOf(navArgument("tenantId") { type = NavType.StringType })
        ) {
            AddReadingScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Documents
        composable(Screen.Documents.route) {
            DocumentsScreen()
        }

        // Reminders
        composable(Screen.Reminders.route) {
            ReminderScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
