package com.farmer.weather.domain

data class DailyTemperature(
    val date: String, // YYYYMMDD
    val maxTemperature: Int,
    val minTemperature: Int
)