package com.rogers.eventapp.di

import com.rogers.eventapp.data.cache.CacheResponse
import com.rogers.eventapp.data.repository.EventRepositoryImpl
import com.rogers.eventapp.domain.repository.EventRepository
import com.rogers.eventapp.domain.usecase.DistanceUseCase
import com.rogers.eventapp.domain.usecase.EventsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO
    @Provides
    @Singleton
    fun provideResponseCache(): CacheResponse = CacheResponse()

    @Provides
    @Singleton
    fun provideEventRepository(
        impl: EventRepositoryImpl
    ): EventRepository = impl

    // Use Cases
    @Provides
    fun provideGetEventsUseCase(repository: EventRepository): EventsUseCase =
        EventsUseCase(repository)

    @Provides
    fun provideDistanceUseCase() : DistanceUseCase = DistanceUseCase()

}