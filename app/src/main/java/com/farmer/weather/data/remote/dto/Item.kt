package com.farmer.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val nx: Int,
    val ny: Int,

    val fcstDate: String?, // ShortTermForecast 에서 사용됨
    val fcstTime: String?,
    val fcstValue: String?,

    val obsrValue: String? // Nowcasting 에서만 사용됨
)
