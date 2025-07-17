package com.farmer.weather.domain

data class ShortTermForecast(
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
