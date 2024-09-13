package ru.smalljinn.tiers.domain.mappers

import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.share.models.ShareElement

/**
 * Sets null categoryId and position to zero to unpin element from category
 */
fun TierElement.unpin() = this.copy(categoryId = null, position = 0)

/**
 * Set categoryId to null for all elements to unpin them
 */
private fun List<TierElement>.unpinElements() =
    this.map { element -> element.copy(categoryId = null) }

fun TierElement.toShare(image: ByteArray) = ShareElement(position, image)