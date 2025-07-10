package com.farmer.weather.data.local

import kotlinx.coroutines.flow.Flow

class LocalRepositoryImpl(
    private val shortTermForecastDao: ShortTermForecastDao,
    private val dailyTemperatureDao: DailyTemperatureDao
) : LocalRepository {

    override suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>) =
        shortTermForecastDao.insertShortTermForecasts(forecasts)

    override suspend fun deleteShortTermForecasts(oldDate: String) =
        shortTermForecastDao.deleteShortTermForecasts(oldDate)

    override fun getAllShortTermForecasts(date: String): Flow<List<ShortTermForecastEntity>> =
        shortTermForecastDao.getAllShortTermForecasts(date)


    override suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity) =
        dailyTemperatureDao.insertTemperature(dailyTemperature)

    override suspend fun deleteDailyTemperature(oldDate: String) =
        dailyTemperatureDao.deleteTemperature(oldDate)

    override fun getDailyTemperature(date: String): Flow<DailyTemperatureEntity?> =
        dailyTemperatureDao.getDailyTemperature(date)

}