package com.farmer.weather.domain

data class DailyTemperature(
    val fcstDate: Int, // YYYYMMDD
    val baseDate: Int,
    val baseTime: String,
    val nx: Int,
    val ny: Int,
    val maxTemperature: Int,
    val minTemperature: Int
)