package ru.smalljinn.tiers.data.share.models

import kotlinx.serialization.Serializable

@Serializable
data class ShareElement(
    val position: Int = 0,
    val image: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShareElement

        if (position != other.position) return false
        if (!image.contentEquals(other.image)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position
        result = 31 * result + image.contentHashCode()
        return result
    }
}