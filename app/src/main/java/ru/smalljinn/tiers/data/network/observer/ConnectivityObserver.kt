package ru.smalljinn.tiers.data.network.observer

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe(): Flow<ConnectionStatus>

    enum class ConnectionStatus {
        AVAILABLE, UNAVAILABLE, LOSING, LOST
    }
}

