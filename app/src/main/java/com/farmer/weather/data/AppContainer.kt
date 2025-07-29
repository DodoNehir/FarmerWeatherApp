package com.farmer.weather.data

import android.content.Context
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
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

interface AppContainer {
    val remoteRepository: RemoteRepository
    val localRepository: LocalRepository
    val locationRepository: LocationRepository
}

class WeatherContainer(private val context: Context) : AppContainer {

    private val baseUrl = Constants.BASE_URL

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    private val retrofit = Retrofit.Builder()
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .build()

    private val retrofitService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)

    }

    override val remoteRepository: RemoteRepository by lazy {
        RemoteRepositoryImpl(retrofitService)
    }

    override val localRepository: LocalRepository by lazy {
        LocalRepositoryImpl(
            WeatherDatabase.getDatabase(context).shortTermForecastDao(),
            WeatherDatabase.getDatabase(context).dailyTemperatureDao(),
            WeatherDatabase.getDatabase(context).nowCastingDao()
        )
    }

    override val locationRepository: LocationRepository by lazy {
        LocationRepositoryImpl(context)
    }
}