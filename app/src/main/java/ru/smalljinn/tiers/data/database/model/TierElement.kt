package ru.smalljinn.tiers.data.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    "tier_element",
    foreignKeys = [
        ForeignKey(
            entity = TierCategory::class,
            childColumns = ["category_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TierList::class,
            childColumns = ["tier_list_id"],
            parentColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TierElement(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "element_id")
    val elementId: Long = 0L,
    @ColumnInfo(index = true, name = "category_id") val categoryId: Long? = null,
    @ColumnInfo(index = true, name = "tier_list_id") val tierListId: Long,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    val position: Int = 0 //position in list. Null means that element not assigned to category elementId ???
)