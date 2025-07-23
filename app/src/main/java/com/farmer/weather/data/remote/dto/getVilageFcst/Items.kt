package com.farmer.weather.data.remote.dto.getVilageFcst

import kotlinx.serialization.Serializable

@Serializable
data class Items(
    val item: List<Item>,
)
