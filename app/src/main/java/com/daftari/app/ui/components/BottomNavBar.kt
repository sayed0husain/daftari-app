package com.daftari.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.daftari.app.R
import com.daftari.app.ui.navigation.Routes

data class BottomItem(val route: String, val labelRes: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

val bottomItems = listOf(
    BottomItem(Routes.HOME, R.string.nav_home, Icons.Default.Home),
    BottomItem(Routes.SUBJECTS, R.string.nav_subjects, Icons.Default.MenuBook),
    BottomItem(Routes.SCHEDULE, R.string.nav_schedule, Icons.Default.CalendarMonth),
    BottomItem(Routes.BEHAVIOR, R.string.nav_behavior, Icons.Default.EmojiEvents),
    BottomItem(Routes.SETTINGS, R.string.nav_settings, Icons.Default.Settings)
)

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        bottomItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = null) },
                label = { Text(stringResource(item.labelRes)) },
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
