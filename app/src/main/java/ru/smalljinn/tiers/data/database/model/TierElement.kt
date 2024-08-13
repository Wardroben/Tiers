package ru.smalljinn.tiers.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    "tier_elements",
    foreignKeys = [
        ForeignKey(
            entity = TierCategory::class,
            childColumns = ["categoryId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TierList::class,
            childColumns = ["tierListId"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TierElement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(index = true) val categoryId: Long? = null,
    @ColumnInfo(index = true) val tierListId: Long,
    val imageUrl: String,
    val position: Int = 0 //position in list. Null means that element not assigned to category id ???
)

/**
 * Sets null categoryId and position to zero to unpin element from category
 */
fun TierElement.unpin() = this.copy(categoryId = null, position = 0)
