package com.farmer.weather.data.remote

import com.farmer.weather.domain.NowCasting
import com.farmer.weather.domain.ShortTermForecast

interface RemoteRepository {
    suspend fun fetchShortTermForecast(
        baseDate: Int,
        baseTime: String,
        numOfRows: Int,
        nx: Int,
        ny: Int
    ): ApiResult<List<ShortTermForecast>>

    suspend fun fetchNowCasting(
        baseDate: Int,
        baseTime: String,
        nx: Int,
        ny: Int
    ): ApiResult<NowCasting>
}