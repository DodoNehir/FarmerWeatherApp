package com.farmer.weather.data.local

import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    suspend fun updateWeather(weatherEntity: WeatherEntity)

    suspend fun deleteWeather(weatherEntity: WeatherEntity)

    fun getAllWeather(): Flow<List<WeatherEntity>>
}

class LocalRepositoryImpl(private val weatherDao: WeatherDao) : LocalRepository {
    override suspend fun insertWeather(weatherEntity: WeatherEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun updateWeather(weatherEntity: WeatherEntity) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWeather(weatherEntity: WeatherEntity) {
        TODO("Not yet implemented")
    }

    override fun getAllWeather(): Flow<List<WeatherEntity>> {
        TODO("Not yet implemented")
    }
}