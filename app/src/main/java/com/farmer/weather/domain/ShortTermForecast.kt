package com.farmer.weather.domain

data class ShortTermForecast(
    val baseDate: String,
    val baseTime: String,
    val fcstDate: String,
    val fcstTime: String,
    val temperature: Int,
    val windSpeed: Int,
    val rainfall: String,
    val skyStatus: String,
    val minTemperature: Int,
    val maxTemperature: Int,
    val precipitationType: Int
)
