package com.farmer.weather.data.remote.dto.getUltraSrtNcst

import kotlinx.serialization.Serializable

@Serializable
data class Item(
    val baseDate: String,
    val baseTime: String,
    val category: String,
    val nx: Int,
    val ny: Int,
    val obsrValue: String
)
