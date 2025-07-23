package com.farmer.weather.domain

data class ShortTermForecast(
    val baseDate: Int,
    val baseTime: String,
    val fcstDate: Int,
    val fcstTime: String,
    val nx: Int,
    val ny: Int,
    val pop: Int, // 강수 확률 : 64 ..
    val precipitationType: Int, // 강수 형태 : 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
    val pcp: String, // 1시간 강수량 : 강수없음 / 0 / 1mm 미만 / 6.9mm .. / 50.0mm 이상
    val skyStatus: Int, // 하늘 상태 : 맑음(1), 구름많음(3), 흐림(4)
    val temperature: Int,
    val minTemperature: Int?,
    val maxTemperature: Int?,
    val windSpeed: Double,
)
