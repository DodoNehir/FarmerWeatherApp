package com.farmer.weather.domain

data class DailyTemperature(
    val fcstDate: String, // YYYYMMDD
    val nx: Int,
    val ny: Int,
    val maxTemperature: String,
    val minTemperature: String
)