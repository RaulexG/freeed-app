package com.raulcn.freeed.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberFreeEdAppState(
    navController: NavHostController = rememberNavController()
): FreeEdAppState = remember(navController) {
    FreeEdAppState(navController)
}

@Stable
class FreeEdAppState(
    val navController: NavHostController
) {
    @Composable
    fun currentDestination(): NavDestination? {
        return navController.currentBackStackEntryAsState().value?.destination
    }

    @Composable
    fun shouldShowBottomBar(): Boolean {
        val destination = currentDestination()
        return MainTabDestination.entries.any { tab ->
            destination.isTopLevelDestination(tab.route)
        }
    }

    fun navigateToTopLevelDestination(destination: MainTabDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = false
            }
            launchSingleTop = true
            restoreState = false
        }
    }
}

private fun NavDestination?.isTopLevelDestination(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}
