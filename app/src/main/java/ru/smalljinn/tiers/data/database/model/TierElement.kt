package ru.smalljinn.tiers.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tier_elements")
data class TierElement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val tierCategoryId: Long,
    val imageUrl: String,
    val position: Int = 0 //position in list
)
