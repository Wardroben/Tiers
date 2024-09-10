package ru.smalljinn.tiers.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessor
import ru.smalljinn.tiers.data.images.photo_processor.PhotoProcessorImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class PhotoModule {
    @Binds
    abstract fun providePhotoProcessor(photoProcessorImpl: PhotoProcessorImpl): PhotoProcessor
}