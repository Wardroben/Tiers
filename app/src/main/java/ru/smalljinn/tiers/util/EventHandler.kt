package ru.smalljinn.tiers.util

interface EventHandler<T> {
    fun obtainEvent(event: T)
}