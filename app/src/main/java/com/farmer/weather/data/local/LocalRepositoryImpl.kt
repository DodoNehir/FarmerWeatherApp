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

    override suspend fun getShortTermForecasts(date: String, time: String): List<ShortTermForecastEntity> =
        shortTermForecastDao.getShortTermForecasts(date, time)


    override suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity) =
        dailyTemperatureDao.insertTemperature(dailyTemperature)

    override suspend fun deleteDailyTemperature(oldDate: String) =
        dailyTemperatureDao.deleteTemperature(oldDate)

    override suspend fun getDailyTemperature(date: String): DailyTemperatureEntity? =
        dailyTemperatureDao.getDailyTemperature(date)

}