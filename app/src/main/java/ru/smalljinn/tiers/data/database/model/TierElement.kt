package ru.smalljinn.tiers.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tier_elements")
data class TierElement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val categoryId: Long?,
    val tierListId: Long,
    val imageUrl: String,
    val position: Int? = null //position in list. Null means that element not assigned to category id ???
)
