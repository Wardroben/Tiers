package ru.smalljinn.tiers.presentation.navigation

const val TIERS_LIST_ROUTE = "tiersList"

const val EDIT_TIER_NAV_ROOT = "edit/"
const val EDIT_TIER_NAV_ARGUMENT = "tierId"
const val EDIT_TIER_ROUTE = "$EDIT_TIER_NAV_ROOT{$EDIT_TIER_NAV_ARGUMENT}"

sealed class NavigationDestination(val route: String) {
    data object TiersList : NavigationDestination(route = TIERS_LIST_ROUTE)
    data object Edit : NavigationDestination(route = EDIT_TIER_ROUTE) {
        fun createRoute(id: Long) = "$EDIT_TIER_NAV_ROOT$id"
    }
}