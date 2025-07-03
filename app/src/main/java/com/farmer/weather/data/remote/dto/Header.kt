package com.farmer.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Header(
    val resultCode: String,
    val resultMsg: String
)
