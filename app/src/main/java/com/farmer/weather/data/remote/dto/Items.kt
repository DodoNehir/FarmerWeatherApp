package com.farmer.weather.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class Items(
    val item: List<Item>,
)
