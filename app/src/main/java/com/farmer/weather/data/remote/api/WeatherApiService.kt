package com.farmer.weather.data.remote.api

import com.farmer.weather.data.remote.dto.WeatherApiResponseDto
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface WeatherApiService {
    @Headers("Accept: application/json")
    @GET("getVilageFcst")
    suspend fun getVilageForecast(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): WeatherApiResponseDto
}