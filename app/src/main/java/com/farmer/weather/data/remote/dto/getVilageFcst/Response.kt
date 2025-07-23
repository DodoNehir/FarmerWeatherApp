package com.farmer.weather.data.remote.dto.getVilageFcst

import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val header: Header,
    val body: Body? = null
)
