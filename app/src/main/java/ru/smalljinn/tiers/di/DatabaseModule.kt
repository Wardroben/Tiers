package ru.smalljinn.tiers.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.smalljinn.tiers.data.database.DATABASE_NAME
import ru.smalljinn.tiers.data.database.TierDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideTierDatabase(@ApplicationContext context: Context) =
        Room.databaseBuilder(context, TierDatabase::class.java, DATABASE_NAME).build()
}