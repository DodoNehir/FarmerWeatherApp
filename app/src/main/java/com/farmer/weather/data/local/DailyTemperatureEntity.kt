package com.farmer.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_temperature")
data class DailyTemperatureEntity(
    @PrimaryKey val fcstDate: String, // YYYYMMDD : PK
    val maxTemperature: Int,
    val minTemperature: Int
)
