package com.farmer.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ShortTermForecastEntity::class, DailyTemperatureEntity::class, NowCastingEntity::class],
    version = 6,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun shortTermForecastDao(): ShortTermForecastDao
    abstract fun dailyTemperatureDao(): DailyTemperatureDao
    abstract fun nowCastingDao(): NowCastingDao

}