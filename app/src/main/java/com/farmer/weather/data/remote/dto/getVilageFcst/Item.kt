package com.farmer.weather.data.remote.dto.getVilageFcst

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val nx: Int,
    val ny: Int,
    val fcstDate: String,
    val fcstTime: String,
    val fcstValue: String
)
