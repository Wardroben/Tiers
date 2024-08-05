package ru.smalljinn.tiers.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("tier_lists")
data class TierList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    //val categories: List<TierCategory>
)
