package com.farmer.weather.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ShortTermForecastEntity::class, DailyTemperatureEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun shortTermForecastDao(): ShortTermForecastDao
    abstract fun dailyTemperatureDao(): DailyTemperatureDao

    companion object {
        @Volatile
        private var Instance: WeatherDatabase? = null

        fun getDatabase(context: Context): WeatherDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context = context,
                    name = "weather_database",
                    klass = WeatherDatabase::class.java
                ).build()
                    .also { Instance = it }
            }
        }
    }
}