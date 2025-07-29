package com.farmer.weather.data.local

class LocalRepositoryImpl(
    private val shortTermForecastDao: ShortTermForecastDao,
    private val dailyTemperatureDao: DailyTemperatureDao,
    private val nowCastingDao: NowCastingDao
) : LocalRepository {

    override suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>) =
        shortTermForecastDao.insertShortTermForecasts(forecasts)

    override suspend fun deleteShortTermForecasts(oldDate: Int) =
        shortTermForecastDao.deleteShortTermForecasts(oldDate)

    override suspend fun getShortTermForecasts(
        date: Int,
        time: String,
        nx: Int,
        ny: Int
    ): List<ShortTermForecastEntity> =
        shortTermForecastDao.getShortTermForecasts(date, time, nx, ny)

    override suspend fun getOneForecast(
        date: Int,
        time: String,
        nx: Int,
        ny: Int
    ): ShortTermForecastEntity? =
        shortTermForecastDao.getOneForecast(date, time, nx, ny)


    override suspend fun insertDailyTemperature(dailyTemperature: DailyTemperatureEntity) =
        dailyTemperatureDao.insertTemperature(dailyTemperature)

    override suspend fun deleteDailyTemperature(oldDate: Int) =
        dailyTemperatureDao.deleteTemperature(oldDate)

    override suspend fun getDailyTemperature(
        date: Int,
        nx: Int,
        ny: Int
    ): DailyTemperatureEntity? =
        dailyTemperatureDao.getDailyTemperature(date, nx, ny)


    override suspend fun insertNowCasting(nowCastingEntity: NowCastingEntity) =
        nowCastingDao.insertNowCasting(nowCastingEntity)

    override suspend fun deleteNowCasting(oldDate: Int) =
        nowCastingDao.deleteNowCasting(oldDate)

    override suspend fun getNowCasting(
        date: Int,
        time: String,
        nx: Int,
        ny: Int
    ): NowCastingEntity? =
        nowCastingDao.getNowCasting(date, time, nx, ny)
}