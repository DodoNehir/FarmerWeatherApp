package com.farmer.weather.data.local

import androidx.room.Entity
import com.farmer.weather.domain.DailyTemperature

@Entity(
    tableName = "daily_temperature",
    primaryKeys = ["fcstDate", "nx", "ny"]
)
data class DailyTemperatureEntity(
    val fcstDate: String, // YYYYMMDD : PK
    val nx: Int,
    val ny: Int,
    val maxTemperature: String,
    val minTemperature: String
)

fun DailyTemperature.toEntity(): DailyTemperatureEntity =
    DailyTemperatureEntity(
        fcstDate = this.fcstDate,
        nx = this.nx,
        ny = this.ny,
        maxTemperature = this.maxTemperature,
        minTemperature = this.minTemperature
    )

fun DailyTemperatureEntity.toDomain(): DailyTemperature =
    DailyTemperature(
        fcstDate = this.fcstDate,
        nx = this.nx,
        ny = this.ny,
        maxTemperature = this.maxTemperature,
        minTemperature = this.minTemperature
    )