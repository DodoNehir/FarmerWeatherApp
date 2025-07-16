package com.farmer.weather.data.local

interface LocalRepository {
    // Short Term Forecast
    suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>)
    suspend fun deleteShortTermForecasts(oldDate: String)
    suspend fun getShortTermForecasts(date: String, time: String, nx: Int, ny: Int): List<ShortTermForecastEntity>

    // Daily Temperature
    suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity)
    suspend fun deleteDailyTemperature(oldDate: String)
    suspend fun getDailyTemperature(date: String, nx: Int, ny: Int): DailyTemperatureEntity?

}