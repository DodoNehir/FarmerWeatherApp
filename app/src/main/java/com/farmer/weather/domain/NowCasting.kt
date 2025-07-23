package com.farmer.weather.domain

data class NowCasting(
    val temperature: Double,
    val rn1: String, // 1시간 강수량
    val humidity: Int,

    // 단기예보와 조금 다르다!
    // 0 없음 / 1 비 / 2 비/눈 / 3 눈 / 5 빗방울 / 6 빗방울눈날림 / 7 눈날림
    val precipitationType: Int, // 강수형태
    val windDirection: Int,
    val windSpeed: Double
)
