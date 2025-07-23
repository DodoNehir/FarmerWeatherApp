package com.farmer.weather.data.remote.dto.getUltraSrtNcst

import kotlinx.serialization.Serializable

@Serializable
data class Body(
//    val dataType: String,
    val items: Items,
    val pageNo: Long,
    val numOfRows: Long,
    val totalCount: Long,
)
