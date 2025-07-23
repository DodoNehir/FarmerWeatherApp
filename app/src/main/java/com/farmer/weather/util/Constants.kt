package com.farmer.weather.util

object Constants {
    const val BASE_URL = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/"
    const val DEFAULT_PAGE_NO = 1
    const val DEFAULT_DATA_TYPE = "JSON"
    const val DEFAULT_NX = 55
    const val DEFAULT_NY = 127

    const val FORECAST_NUM_OF_ROWS = 870
    // 단기예보에서 한 타임당 12개의 정보가 들어오지만, TMN, TMX가 있으면 13개
    const val TMN_TMX_23_NUM_OF_ROWS = 194
    const val TMN_TMX_2_NUM_OF_ROWS = 158


    const val NOWCASTING_NUM_OF_ROWS = 10
}