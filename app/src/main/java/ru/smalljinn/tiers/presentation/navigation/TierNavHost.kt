package ru.smalljinn.tiers.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.smalljinn.tiers.presentation.ui.screens.tierslist.TiersListScreen

@Composable
fun TierNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
    startDestination: String = NavigationDestination.TiersList.route
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = startDestination
    ) {
        composable(route = NavigationDestination.TiersList.route) {
            TiersListScreen(
                navigateToEdit = { tierId ->
                    navHostController.navigate(NavigationDestination.Edit.createRoute(tierId))
                }
            )
        }
        composable(
            route = NavigationDestination.Edit.route,
            arguments = listOf(navArgument(EDIT_TIER_NAV_ARGUMENT) { type = NavType.LongType })
        ) { navBackStackEntry ->

        }
    }
}