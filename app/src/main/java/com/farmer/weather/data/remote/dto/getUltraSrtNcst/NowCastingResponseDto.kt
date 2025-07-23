package com.farmer.weather.data.remote.dto.getUltraSrtNcst

import com.farmer.weather.domain.NowCasting
import com.farmer.weather.util.nowRequireDouble
import com.farmer.weather.util.nowRequireInt
import com.farmer.weather.util.nowRequireString
import kotlinx.serialization.Serializable

@Serializable
data class NowCastingResponseDto(
    val response: Response
)

fun NowCastingResponseDto.toNowCasting(): NowCasting {
    val items = this.response.body?.items?.item
        ?: throw IllegalStateException("code is 00 but body is null")

    val itemMap = items.associateBy { it.category }

    return NowCasting(
        temperature = itemMap.nowRequireDouble("T1H"),
        rn1 = itemMap.nowRequireString("RN1"),
        humidity = itemMap.nowRequireInt("REH"),
        precipitationType = itemMap.nowRequireInt("PTY"),
        windDirection = itemMap.nowRequireInt("VEC"),
        windSpeed = itemMap.nowRequireDouble("WSD")
    )
}