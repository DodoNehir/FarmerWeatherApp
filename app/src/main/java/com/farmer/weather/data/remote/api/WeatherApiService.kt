package com.farmer.weather.data.remote.api

import com.farmer.weather.data.remote.dto.getUltraSrtNcst.NowCastingResponseDto
import com.farmer.weather.data.remote.dto.getVilageFcst.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("getVilageFcst")
    suspend fun getVilageForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: Int,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): ForecastResponseDto

    @GET("getUltraSrtNcst")
    suspend fun getNowCasting(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: Int,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): NowCastingResponseDto
}