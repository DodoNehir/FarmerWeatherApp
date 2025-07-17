package com.farmer.weather.data.local

import androidx.room.Entity
import com.farmer.weather.domain.ShortTermForecast

@Entity(
    tableName = "short_term_forecast",
    primaryKeys = ["fcstDate", "fcstTime", "nx", "ny"]
)
data class ShortTermForecastEntity(
    val baseDate: Int,
    val baseTime: String,

    val fcstDate: Int,
    val fcstTime: String,
    val nx: Int,
    val ny: Int,

    val pop: Int?, // 강수 확률
    val precipitationType: Int?,
    val pcp: String?, // 1시간 강수량

    val skyStatus: Int?,

    val temperature: Int?,
    val minTemperature: Int?,
    val maxTemperature: Int?,

    val windSpeed: Double?,
)

fun ShortTermForecastEntity.toDomain(): ShortTermForecast =
    ShortTermForecast(
        baseDate = this.baseDate,
        baseTime = this.baseTime,
        fcstDate = this.fcstDate,
        fcstTime = this.fcstTime,
        nx = this.nx,
        ny = this.ny,
        pop = this.pop,
        precipitationType = this.precipitationType,
        pcp = this.pcp,
        skyStatus = this.skyStatus,
        temperature = this.temperature,
        minTemperature = this.minTemperature,
        maxTemperature = this.maxTemperature,
        windSpeed = this.windSpeed
    )

fun ShortTermForecast.toEntity(): ShortTermForecastEntity =
    ShortTermForecastEntity(
        baseDate = this.baseDate,
        baseTime = this.baseTime,
        fcstDate = this.fcstDate,
        fcstTime = this.fcstTime,
        nx = this.nx,
        ny = this.ny,
        pop = this.pop,
        precipitationType = this.precipitationType,
        pcp = this.pcp,
        skyStatus = this.skyStatus,
        temperature = this.temperature,
        minTemperature = this.minTemperature,
        maxTemperature = this.maxTemperature,
        windSpeed = this.windSpeed
    )