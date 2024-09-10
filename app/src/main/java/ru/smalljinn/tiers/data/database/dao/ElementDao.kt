package ru.smalljinn.tiers.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import ru.smalljinn.tiers.data.database.model.TierElement

@Dao
interface ElementDao {
    @Upsert//(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertElement(element: TierElement): Long

    @Upsert//(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTierElements(elements: List<TierElement>): List<Long>

    @Delete
    suspend fun deleteElement(element: TierElement)

    @Delete
    suspend fun deleteElements(elements: List<TierElement>)

    @Query("SELECT * FROM tier_element WHERE element_id = :id")
    suspend fun getElementById(id: Long): TierElement

    @Query("SELECT * FROM tier_element WHERE tier_list_id = :listId AND category_id is NULL")
    fun getUnassertedElementsStream(listId: Long): Flow<List<TierElement>>

    @Query("SELECT * FROM tier_element WHERE tier_list_id = :listId")
    suspend fun getTierListElements(listId: Long): List<TierElement>

    @Query("SELECT * FROM tier_element WHERE category_id = :categoryId AND position >= :position")
    suspend fun getElementsAfterTarget(categoryId: Long, position: Int): List<TierElement>

    @Query("SELECT * FROM tier_element WHERE category_id = :categoryId AND position <= :position")
    suspend fun getElementsBeforeTarget(categoryId: Long, position: Int): List<TierElement>

    @Query("SELECT position FROM tier_element WHERE element_id = :id")
    suspend fun getElementPosition(id: Long): Int

    @Query("SELECT MAX(position) FROM tier_element WHERE category_id = :categoryId")
    suspend fun getLastPositionInCategory(categoryId: Long): Int

    @Query("")
    suspend fun reorderElements(draggedElementId: Long, targetId: Long) {
        val draggedElement = getElementById(draggedElementId)
        val categoryId = draggedElement.categoryId ?: return
        val targetPosition = getElementPosition(targetId)

        val newPosition = if (draggedElement.position > targetPosition) 1 else -1

        val elementsToChange = if (draggedElement.position > targetPosition) getElementsAfterTarget(
            categoryId,
            targetPosition
        ) else getElementsBeforeTarget(categoryId, targetPosition)
        val shiftedElements =
            elementsToChange.map { element -> element.copy(position = element.position + newPosition) }
        insertTierElements(shiftedElements.plus(draggedElement.copy(position = targetPosition)))
    }
}