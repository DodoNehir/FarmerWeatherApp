package com.farmer.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "short_term_forecast",
    primaryKeys = ["fcstDate", "fcstTime"])
data class ShortTermForecastEntity(
    val baseDate: String,
    val baseTime: String,

    val fcstDate: String,
    val fcstTime: String,

    val pop: Int?, // 강수 확률
    val precipitationType: Int?,
    val pcp: String?, // 1시간 강수량

    val skyStatus: Int?,

    val temperature: Int?,
    val minTemperature: Int?,
    val maxTemperature: Int?,

    val windSpeed: Double?,
)
