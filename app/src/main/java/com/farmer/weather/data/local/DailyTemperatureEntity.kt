package com.farmer.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.farmer.weather.domain.DailyTemperature

@Entity(tableName = "daily_temperature")
data class DailyTemperatureEntity(
    @PrimaryKey val fcstDate: String, // YYYYMMDD : PK
    val maxTemperature: String,
    val minTemperature: String
)

fun DailyTemperature.toEntity(): DailyTemperatureEntity =
    DailyTemperatureEntity(
        fcstDate = this.fcstDate,
        maxTemperature = this.maxTemperature,
        minTemperature = this.minTemperature
    )

fun DailyTemperatureEntity.toDomain(): DailyTemperature =
    DailyTemperature(
        fcstDate = this.fcstDate,
        maxTemperature = this.maxTemperature,
        minTemperature = this.minTemperature
    )