package com.farmer.weather.domain

data class ShortTermForecast(
    val baseDate: String,
    val baseTime: String,
    val fcstDate: String,
    val fcstTime: String,
    val pop: Int?, // 강수 확률
    val precipitationType: Int?,
    val pcp: String?, // 1시간 강수량
    val skyStatus: Int?,
    val temperature: Int?,
    val minTemperature: String?,
    val maxTemperature: String?,
    val windSpeed: Double?,
)
