package com.rogers.eventapp.di

import android.content.Context
import com.rogers.eventapp.data.remote.api.EventApiService
import com.rogers.eventapp.utils.AppConfig
import com.rogers.eventapp.data.remote.interceptor.FakeApiInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFakeInterceptor(
        @ApplicationContext context: Context
    ): FakeApiInterceptor {
        return FakeApiInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        fakeApiInterceptor: FakeApiInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(fakeApiInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConfig.Network.baseUrl) // dummy base URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideEventApi(
        retrofit: Retrofit
    ): EventApiService {
        return retrofit.create(EventApiService::class.java)
    }
}