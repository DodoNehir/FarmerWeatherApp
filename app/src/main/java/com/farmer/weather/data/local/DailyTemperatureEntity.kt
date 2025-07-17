package com.farmer.weather.data.local

import androidx.room.Entity
import com.farmer.weather.domain.DailyTemperature

@Entity(
    tableName = "daily_temperature",
    primaryKeys = ["fcstDate", "nx", "ny"]
)
data class DailyTemperatureEntity(
    val fcstDate: Int, // YYYYMMDD
    val baseDate: Int,
    val baseTime: String,
    val nx: Int,
    val ny: Int,
    val maxTemperature: Int,
    val minTemperature: Int
)

fun DailyTemperature.toEntity(): DailyTemperatureEntity =
    DailyTemperatureEntity(
        fcstDate = this.fcstDate,
        baseDate = this.baseDate,
        baseTime = this.baseTime,
        nx = this.nx,
        ny = this.ny,
        maxTemperature = this.maxTemperature,
        minTemperature = this.minTemperature
    )

fun DailyTemperatureEntity.toDomain(): DailyTemperature =
    DailyTemperature(
        fcstDate = this.fcstDate,
        baseDate = this.baseDate,
        baseTime = this.baseTime,
        nx = this.nx,
        ny = this.ny,
        maxTemperature = this.maxTemperature,
        minTemperature = this.minTemperature
    )