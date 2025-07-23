package com.farmer.weather.data.remote.dto.getVilageFcst

import kotlinx.serialization.Serializable

@Serializable
data class Header(
    val resultCode: String,
    val resultMsg: String
)
