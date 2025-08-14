package com.farmer.weather.data.remote

import android.util.Log
import com.farmer.weather.BuildConfig
import com.farmer.weather.data.remote.api.WeatherApiService
import com.farmer.weather.data.remote.dto.getUltraSrtNcst.toNowCasting
import com.farmer.weather.data.remote.dto.getVilageFcst.toShortTermForecasts
import com.farmer.weather.domain.NowCasting
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.util.Constants

class RemoteRepositoryImpl(
    private val weatherApiService: WeatherApiService
) : RemoteRepository {

    override suspend fun fetchShortTermForecast(
        baseDate: Int,
        baseTime: String,
        numOfRows: Int,
        nx: Int,
        ny: Int
    ): ApiResult<List<ShortTermForecast>> {
        return try {
            Log.d("RemoteRepository", "start http get shortTermForecast")
            val responseDto = weatherApiService.getVilageForecast(
                serviceKey = BuildConfig.WEATHER_API_KEY,
                pageNo = Constants.DEFAULT_PAGE_NO,
                numOfRows = numOfRows,
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

    override suspend fun fetchNowCasting(
        baseDate: Int,
        baseTime: String,
        nx: Int,
        ny: Int
    ): ApiResult<NowCasting> {
        return try {
            Log.d("RemoteRepository", "start http get nowCasting")
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