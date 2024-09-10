package ru.smalljinn.tiers.features.tier_lists

import android.content.Intent

sealed class ActionEvent {
    data class StartIntent(val intent: Intent) : ActionEvent()
}