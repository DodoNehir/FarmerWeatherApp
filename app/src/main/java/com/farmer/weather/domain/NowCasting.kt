package com.farmer.weather.domain

data class NowCasting(
    val temperature: Double,
    val rn1: String, // 1시간 강수량
    val humidity: Int,
    val precipitationType: Int, // 강수형태
    val windDirection: Int,
    val windSpeed: Double
)
