package com.farmer.weather.domain

data class DailyTemperature(
    val fcstDate: String, // YYYYMMDD
    val maxTemperature: String,
    val minTemperature: String
)