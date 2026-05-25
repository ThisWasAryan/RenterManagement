package com.rms.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rms.app.core.navigation.RMSNavHost
import com.rms.app.core.navigation.Screen
import com.rms.app.core.ui.components.RMSBottomNavBar
import com.rms.app.core.ui.components.bottomNavItems
import com.rms.app.core.ui.theme.RMSTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rms.app.core.ui.theme.ThemeManager
import com.rms.app.core.ui.theme.ThemeMode
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themeManager.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.DARK)
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            RMSTheme(darkTheme = isDark) {
                RMSApp()
            }
        }
    }
}

@Composable
fun RMSApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Routes where bottom nav should be visible
    val bottomNavRoutes = bottomNavItems.map { it.route }
    val showBottomNav = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                RMSBottomNavBar(navController = navController)
            }
        }
    ) { innerPadding ->
        RMSNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
