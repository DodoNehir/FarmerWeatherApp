package com.farmer.weather.data.local

import kotlinx.coroutines.flow.Flow

interface LocalRepository {
    // Short Term Forecast
    suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>)
    suspend fun deleteShortTermForecasts(oldDate: String)
    fun getAllShortTermForecasts(date: String): Flow<List<ShortTermForecastEntity>>

    // Daily Temperature
    suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity)
    suspend fun deleteDailyTemperature(oldDate: String)
    fun getDailyTemperature(date: String): Flow<DailyTemperatureEntity?>

}