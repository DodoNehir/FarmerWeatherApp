package com.farmer.weather.data.local

interface LocalRepository {
    // Short Term Forecast
    suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>)
    suspend fun deleteShortTermForecasts(oldDate: Int)
    suspend fun getShortTermForecasts(date: Int, time: String, nx: Int, ny: Int): List<ShortTermForecastEntity>
    suspend fun getOneForecast(date: Int, time: String, nx: Int, ny: Int): ShortTermForecastEntity?

    // Daily Temperature
    suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity)
    suspend fun deleteDailyTemperature(oldDate: Int)
    suspend fun getDailyTemperature(date: Int, nx: Int, ny: Int): DailyTemperatureEntity?

    // Nowcasting
    suspend fun insertNowCasting(nowCastingEntity: NowCastingEntity)
    suspend fun deleteNowCasting(oldDate: Int)
    suspend fun getNowCasting(date: Int, time: String, nx: Int, ny: Int): NowCastingEntity?

}