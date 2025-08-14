package com.farmer.weather.data.di

import android.content.Context
import androidx.room.Room
import com.farmer.weather.data.local.LocalRepository
import com.farmer.weather.data.local.LocalRepositoryImpl
import com.farmer.weather.data.local.WeatherDatabase
import com.farmer.weather.data.location.LocationRepository
import com.farmer.weather.data.location.LocationRepositoryImpl
import com.farmer.weather.data.remote.RemoteRepository
import com.farmer.weather.data.remote.RemoteRepositoryImpl
import com.farmer.weather.data.remote.api.WeatherApiService
import com.farmer.weather.util.Constants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context = context,
            name = "weather_database",
            klass = WeatherDatabase::class.java
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

//    @Provides
//    fun provideBaseUrl(): String = Constants.BASE_URL

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json, okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .build()

    @Provides
    @Singleton
    fun provideRetrofitService(retrofit: Retrofit): WeatherApiService =
        retrofit.create(WeatherApiService::class.java)

    @Provides
    @Singleton
    fun provideRemoteRepository(retrofitService: WeatherApiService): RemoteRepository =
        RemoteRepositoryImpl(retrofitService)

    @Provides
    @Singleton
    fun provideLocalRepository(db: WeatherDatabase): LocalRepository =
        LocalRepositoryImpl(
            db.shortTermForecastDao(),
            db.dailyTemperatureDao(),
            db.nowCastingDao()
        )

    @Provides
    @Singleton
    fun provideLocationRepository(
        @ApplicationContext context: Context
    ): LocationRepository =
        LocationRepositoryImpl(context)

}