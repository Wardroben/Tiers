package ru.smalljinn.tiers.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.smalljinn.tiers.features.app_settings.SettingsScreen
import ru.smalljinn.tiers.features.tier_edit.TierEditScreen
import ru.smalljinn.tiers.features.tier_lists.TiersListScreen
import ru.smalljinn.tiers.navigation.routes.Settings
import ru.smalljinn.tiers.navigation.routes.TierEdit
import ru.smalljinn.tiers.navigation.routes.TiersList

@Composable
fun TierNavHost(
    modifier: Modifier = Modifier,
    navHostController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navHostController,
        startDestination = TiersList,
    ) {
        composable<TiersList> {
            TiersListScreen(
                navigateToEdit = { tierId -> navHostController.navigate(TierEdit(listId = tierId)) },
                navigateToSettings = { navHostController.navigate(Settings) }
            )
        }
        composable<Settings> { SettingsScreen(navigateBack = { navHostController.navigateUp() }) }
        composable<TierEdit> { TierEditScreen() }
    }
}