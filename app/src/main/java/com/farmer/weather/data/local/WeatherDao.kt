package com.farmer.weather.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWeather(weatherEntity: WeatherEntity)

    @Update
    suspend fun updateWeather(weatherEntity: WeatherEntity)

    @Delete
    suspend fun deleteWeather(weatherEntity: WeatherEntity)

    @Query(value = "SELECT * FROM weather ORDER BY time")
    fun getAllWeather() : Flow<List<WeatherEntity>>
}