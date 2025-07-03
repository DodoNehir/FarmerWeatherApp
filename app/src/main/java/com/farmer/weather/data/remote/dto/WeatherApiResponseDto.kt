package com.farmer.weather.data.remote.dto

import com.farmer.weather.domain.ShortTermForecast
import kotlinx.serialization.Serializable

@Serializable
data class WeatherApiResponseDto(
    val response: Response
)

fun WeatherApiResponseDto.toShortTermForecasts(): List<ShortTermForecast> {
    // 정상 응답이 아니면 빈 리스트 반환
    if (this.response.header.resultCode != "00" || this.response.body == null) {
        return emptyList()
    }

    val items = this.response.body.items.item


    // K: <날짜, 시각> / V: List<Item>
    val itemsByForecastTime = items.groupBy { it.fcstDate to it.fcstTime }

    val forecasts = mutableListOf<ShortTermForecast>()

    for ((timeKey, timeValues) in itemsByForecastTime) {
        val fcstDate = timeKey.first
        val fcstTime = timeKey.second

        // List<Item> ->  (K: category / V: Item)  1:1 매핑
        val categoryMap = timeValues.associateBy { it.category }

        val forecast = ShortTermForecast(
            baseDate = timeValues.first().baseDate,
            baseTime = timeValues.first().baseTime,
            fcstDate = fcstDate,
            fcstTime = fcstTime,
            temperature = categoryMap["TMP"]?.fcstValue?.toIntOrNull() ?: -99,
            windSpeed = categoryMap["WSD"]?.fcstValue?.toIntOrNull() ?: -99,
            rainfall = categoryMap["PCP"]?.fcstValue ?: "",
            skyStatus = categoryMap["SKY"]?.fcstValue ?: "",
            minTemperature = categoryMap["TMN"]?.fcstValue?.toIntOrNull() ?: -99,
            maxTemperature = categoryMap["TMX"]?.fcstValue?.toIntOrNull() ?: -99,
            precipitationType = categoryMap["PTY"]?.fcstValue?.toIntOrNull() ?: -99,
        )
        forecasts.add(forecast)
    }

    return forecasts.sortedWith(compareBy<ShortTermForecast> { it.fcstDate }.thenBy { it.fcstTime })

}