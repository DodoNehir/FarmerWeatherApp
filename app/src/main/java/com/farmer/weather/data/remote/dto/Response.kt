package com.farmer.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Response(
    val header: Header,
    val body: Body?
)
