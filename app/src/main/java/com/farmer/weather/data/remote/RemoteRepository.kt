package com.farmer.weather.data.remote

import com.farmer.weather.BuildConfig
import com.farmer.weather.data.remote.api.WeatherApiService
import com.farmer.weather.data.remote.dto.getUltraSrtNcst.toNowCasting
import com.farmer.weather.data.remote.dto.getVilageFcst.toShortTermForecasts
import com.farmer.weather.domain.NowCasting
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.util.Constants

interface RemoteRepository {
    suspend fun getShortTermForecast(
        baseDate: Int,
        baseTime: String,
        nx: Int,
        ny: Int
    ): ApiResult<List<ShortTermForecast>>

    suspend fun getNowCasting(
        baseDate: Int,
        baseTime: String,
        nx: Int,
        ny: Int
    ): ApiResult<NowCasting>
}

class RemoteRepositoryImpl(
    private val weatherApiService: WeatherApiService
) : RemoteRepository {

    override suspend fun getShortTermForecast(
        baseDate: Int,
        baseTime: String,
        nx: Int,
        ny: Int
    ): ApiResult<List<ShortTermForecast>> {
        return try {
            val responseDto = weatherApiService.getVilageForecast(
                serviceKey = BuildConfig.WEATHER_API_KEY,
                pageNo = Constants.DEFAULT_PAGE_NO,
                numOfRows = Constants.DEFAULT_NUM_OF_ROWS,
                dataType = Constants.DEFAULT_DATA_TYPE,
                baseDate = baseDate,
                baseTime = baseTime,
                nx = nx,
                ny = ny
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

    // TODO 200OK 를 받아오는데도 최종으로는 Error가 반환되고 있는 문제??
    override suspend fun getNowCasting(
        baseDate: Int,
        baseTime: String,
        nx: Int,
        ny: Int
    ): ApiResult<NowCasting> {
        return try {
            val responseDto = weatherApiService.getNowCasting(
                serviceKey = BuildConfig.WEATHER_API_KEY,
                pageNo = Constants.DEFAULT_PAGE_NO,
                numOfRows = Constants.NOWCASTING_NUM_OF_ROWS,
                dataType = Constants.DEFAULT_DATA_TYPE,
                baseDate = baseDate,
                baseTime = baseTime,
                nx = nx,
                ny = ny
            )

            when (responseDto.response.header.resultCode) {
                "00" -> {
                    try {
                        val result = responseDto.toNowCasting()
                        ApiResult.Success(result)
                    } catch (e: IllegalStateException) {
                        ApiResult.Error(
                            code = "00",
                            message = e.message,
                            exception = e
                        )
                    }
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