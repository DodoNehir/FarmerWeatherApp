package com.farmer.weather.data.remote.dto.getVilageFcst

import android.util.Log
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.util.shortRequireDouble
import com.farmer.weather.util.shortRequireInt
import com.farmer.weather.util.shortRequireString
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val response: Response
)

fun ForecastResponseDto.toShortTermForecasts(): List<ShortTermForecast> {
    // 정상 응답이 아니면 빈 리스트 반환
    if (this.response.header.resultCode != "00" || this.response.body == null) {
        return emptyList()
    }

    val items = this.response.body.items.item


    // K: <날짜, 시각> / V: List<Item>
    val itemsByForecastTime = items.groupBy { it.fcstDate to it.fcstTime }

    val forecasts = mutableListOf<ShortTermForecast>()

    for ((timePair, sameTimeItemList) in itemsByForecastTime) {
        try {
            val fcstDate = timePair.first
            val fcstTime = timePair.second

            // List<Item> ->  Map < K: category, V: Item >  리스트를 맵으로 변환
            val categoryMap = sameTimeItemList.associateBy { it.category }

            val forecast = ShortTermForecast(
                baseDate = sameTimeItemList.first().baseDate.toInt(),
                baseTime = sameTimeItemList.first().baseTime,
                fcstDate = fcstDate.toInt(),
                fcstTime = fcstTime,
                nx = sameTimeItemList.first().nx,
                ny = sameTimeItemList.first().ny,
                pop = categoryMap.shortRequireInt("POP"),

                // 강수 형태 : 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
                precipitationType = categoryMap.shortRequireInt("PTY"),
                // 1시간 강수량: String으로 mm까지 표시해줌.(강수없음 / 0 / 1mm 미만 / 6.9mm .. / 50.0mm 이상)
                pcp = categoryMap.shortRequireString("PCP"),
                // 하늘 상태 : 맑음(1), 구름많음(3), 흐림(4)
                skyStatus = categoryMap.shortRequireInt("SKY"),

                temperature = categoryMap.shortRequireInt("TMP"),
                minTemperature = categoryMap["TMN"]?.fcstValue?.toDouble()?.toInt(),
                maxTemperature = categoryMap["TMX"]?.fcstValue?.toDouble()?.toInt(),
                windSpeed = categoryMap.shortRequireDouble("WSD"),
            )
            forecasts.add(forecast)

        } catch (e: Exception) {
            Log.d(
                "ForecastResponseDto",
                "Forecast skipped at ${timePair.first} : ${timePair.second}, message: ${e.message}"
            )
        }

    }

    return forecasts.sortedWith(compareBy<ShortTermForecast> { it.fcstDate }.thenBy { it.fcstTime })

}