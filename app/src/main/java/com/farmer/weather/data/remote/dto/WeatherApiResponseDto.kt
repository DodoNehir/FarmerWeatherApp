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

    for ((timePair, sameTimeItemList) in itemsByForecastTime) {
        val fcstDate = timePair.first
        val fcstTime = timePair.second

        // List<Item> ->  Map < K: category, V: Item >  리스트를 맵으로 변환
        val categoryMap = sameTimeItemList.associateBy { it.category }

        // json 결과로 String 타입으로 오는 값도 있어서 여기서 형변환한다.
        val forecast = ShortTermForecast(
            baseDate = sameTimeItemList.first().baseDate.toInt(),
            baseTime = sameTimeItemList.first().baseTime,
            fcstDate = fcstDate.toInt(),
            fcstTime = fcstTime,
            nx = sameTimeItemList.first().nx,
            ny = sameTimeItemList.first().ny,

            // 강수 확률
            pop = categoryMap["POP"]?.fcstValue?.toIntOrNull(),
            // 강수 형태 : 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
            precipitationType = categoryMap["PTY"]?.fcstValue?.toIntOrNull(),
            // 1시간 강수량: String으로 mm까지 표시해줌.(강수없음 / 1mm 미만 / 6.9mm .. / 50.0mm 이상)
            pcp = categoryMap["PCP"]?.fcstValue,

            // 하늘 상태 : 맑음(1), 구름많음(3), 흐림(4)
            skyStatus = categoryMap["SKY"]?.fcstValue?.toIntOrNull(),

            temperature = categoryMap["TMP"]?.fcstValue?.toIntOrNull(),
            minTemperature = categoryMap["TMN"]?.fcstValue?.toInt(),
            maxTemperature = categoryMap["TMX"]?.fcstValue?.toInt(),

            windSpeed = categoryMap["WSD"]?.fcstValue?.toDoubleOrNull(),
        )
        forecasts.add(forecast)
    }

    return forecasts.sortedWith(compareBy<ShortTermForecast> { it.fcstDate }.thenBy { it.fcstTime })

}