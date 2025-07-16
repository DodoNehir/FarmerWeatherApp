package com.farmer.weather.data.local

class LocalRepositoryImpl(
    private val shortTermForecastDao: ShortTermForecastDao,
    private val dailyTemperatureDao: DailyTemperatureDao
) : LocalRepository {

    override suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>) =
        shortTermForecastDao.insertShortTermForecasts(forecasts)

    override suspend fun deleteShortTermForecasts(oldDate: String) =
        shortTermForecastDao.deleteShortTermForecasts(oldDate)

    override suspend fun getShortTermForecasts(
        date: String,
        time: String,
        nx: Int,
        ny: Int
    ): List<ShortTermForecastEntity> =
        shortTermForecastDao.getShortTermForecasts(date, time, nx, ny)


    override suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity) =
        dailyTemperatureDao.insertTemperature(dailyTemperature)

    override suspend fun deleteDailyTemperature(oldDate: String) =
        dailyTemperatureDao.deleteTemperature(oldDate)

    override suspend fun getDailyTemperature(
        date: String,
        nx: Int,
        ny: Int
    ): DailyTemperatureEntity? =
        dailyTemperatureDao.getDailyTemperature(date, nx, ny)

}