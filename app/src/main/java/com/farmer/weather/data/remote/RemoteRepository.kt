package com.farmer.weather.data.remote

import android.util.Log.e
import com.farmer.weather.data.remote.api.WeatherApiService
import com.farmer.weather.data.remote.dto.toShortTermForecasts
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.util.Constants
import com.farmer.weather.BuildConfig

interface RemoteRepository {
    suspend fun getShortTermForecast(
        baseDate: String,
        baseTime: String,
//        nx: Int,
//        ny: Int
    ): ApiResult<List<ShortTermForecast>>
}

class RemoteRepositoryImpl(
    private val weatherApiService: WeatherApiService
) : RemoteRepository {

    override suspend fun getShortTermForecast(
        baseDate: String,
        baseTime: String,
//        nx: Int,
//        ny: Int
    ): ApiResult<List<ShortTermForecast>> {

        return try {
            val responseDto = weatherApiService.getVilageForecast(
                serviceKey = BuildConfig.WEATHER_API_KEY,
                pageNo = Constants.DEFAULT_PAGE_NO,
                numOfRows = Constants.DEFAULT_NUM_OF_ROWS,
                dataType = Constants.DEFAULT_DATA_TYPE,
                baseDate = baseDate,
                baseTime = baseTime,
                // TODO nx, ny 계산하기
                nx = Constants.DEFAULT_NX,
                ny = Constants.DEFAULT_NY
            )

            when (responseDto.response.header.resultCode) {
                "00" -> {
                    ApiResult.Success(responseDto.toShortTermForecasts())
                }

                "03" -> {
                    ApiResult.NoData
                }

                else -> {
                    ApiResult.Error(
                        code = responseDto.response.header.resultCode,
                        message = responseDto.response.header.resultMsg
                    )
                }
            }

        } catch (e: Exception) {
            ApiResult.Error(code = null, message = e.message, exception = e)
        }


    }
}