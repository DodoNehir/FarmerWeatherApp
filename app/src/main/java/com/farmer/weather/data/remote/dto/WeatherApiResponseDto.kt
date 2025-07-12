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

            // 강수 확률
            pop = categoryMap["POP"]?.fcstValue?.toIntOrNull(),
            // 강수 형태 : 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
            precipitationType = categoryMap["PTY"]?.fcstValue?.toIntOrNull(),
            // 1시간 강수량: String으로 mm까지 표시해줌.(강수없음 / 1mm 미만 / 6.9mm .. / 50.0mm 이상)
            pcp = categoryMap["PCP"]?.fcstValue,

            // 하늘 상태 : 맑음(1), 구름많음(3), 흐림(4)
            skyStatus = categoryMap["SKY"]?.fcstValue?.toIntOrNull(),

            temperature = categoryMap["TMP"]?.fcstValue?.toIntOrNull(),
            minTemperature = categoryMap["TMN"]?.fcstValue,
            maxTemperature = categoryMap["TMX"]?.fcstValue,

            windSpeed = categoryMap["WSD"]?.fcstValue?.toDoubleOrNull(),
        )
        forecasts.add(forecast)
    }

    return forecasts.sortedWith(compareBy<ShortTermForecast> { it.fcstDate }.thenBy { it.fcstTime })

}