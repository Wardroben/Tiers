package ru.smalljinn.tiers.util.network.observer

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe(): Flow<ConnectionStatus>

    enum class ConnectionStatus {
        AVAILABLE, UNAVAILABLE, LOSING, LOST
    }
}

