package ru.smalljinn.tiers.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ru.smalljinn.tiers.presentation.ui.screens.edit.TierEditScreen
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
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMediumLow)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                spring(Spring.DampingRatioNoBouncy, Spring.StiffnessMediumLow)
            )
        }
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
        ) {
            TierEditScreen()
        }
    }
}