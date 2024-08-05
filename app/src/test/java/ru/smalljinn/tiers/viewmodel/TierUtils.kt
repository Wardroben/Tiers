package ru.smalljinn.tiers.viewmodel

import android.graphics.Color
import androidx.core.graphics.toColor
import ru.smalljinn.tiers.data.database.model.TierCategory
import ru.smalljinn.tiers.data.database.model.TierElement
import ru.smalljinn.tiers.data.database.model.TierList

object TierUtils {
    fun createTierElement(
        id: Long = 0L,
        imageUrl: String = "image2.png",
        position: Int? = null,
        tierCategoryId: Long? = null,
        tierListId: Long
    ): TierElement {
        return TierElement(
            id = id,
            imageUrl = imageUrl,
            position = position,
            categoryId = tierCategoryId,
            tierListId = tierListId
        )
    }

    fun createTierElements(
        count: Int,
        tierCategoryId: Long? = null,
        tierListId: Long
    ): List<TierElement> =
        (1..count).map { step ->
            createTierElement(
                id = step.toLong(),
                tierCategoryId = tierCategoryId,
                imageUrl = "image$step",
                tierListId = tierListId
            )
        }

    fun createTierElementsForList(count: Int, tierListId: Long): List<TierElement> =
        createTierElements(count, null, tierListId)


    /*fun createTierElementsForCategories(
        tierCategories: List<TierCategory>,
        countElementsForCategory: Int
    ): List<TierElement> {
        val elements = mutableListOf<TierElement>()
        tierCategories.forEach { category ->
            elements.addAll(createTierElements(countElementsForCategory, category.id, ))
        }
        return elements
    }*/

    fun createTierCategory(
        id: Long = 0L,
        name: String = "A",
        color: Color = Color.GREEN.toColor(),
        tierListId: Long,
        position: Int = 0
    ): TierCategory {
        return TierCategory(
            id = id,
            name = name,
            colorArgb = color.toArgb(),
            tierListId = tierListId,
            position = position
        )
    }

    fun createBaseTierCategories(listId: Long): List<TierCategory> = baseCategories(listId)

    fun createTierList(
        id: Long = 0L,
        name: String = "Base tier list"
    ): TierList {
        return TierList(id, name)
    }

    private fun baseCategories(listId: Long) = listOf(
        createTierCategory(tierListId = listId, id = 1, name = "A", color = Color.GREEN.toColor()),
        createTierCategory(tierListId = listId, id = 2, name = "B", color = Color.YELLOW.toColor()),
        createTierCategory(tierListId = listId, id = 3, name = "C", color = Color.CYAN.toColor()),
        createTierCategory(tierListId = listId, id = 4, name = "D", color = Color.RED.toColor()),
    )
}