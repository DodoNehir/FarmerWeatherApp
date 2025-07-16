package com.farmer.weather.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ShortTermForecastDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShortTermForecasts(forecasts: List<ShortTermForecastEntity>)

    @Query(value = "DELETE FROM short_term_forecast WHERE fcstDate < :oldDate")
    suspend fun deleteShortTermForecasts(oldDate: String)

    @Query(value = """
        SELECT * 
        FROM short_term_forecast 
        WHERE nx = :nx AND ny = :ny 
         AND fcstDate >= :date 
         AND (fcstTime >= :time OR fcstDate > :date) 
        ORDER BY fcstDate, fcstTime ASC
        LIMIT 28
        """)
    suspend fun getShortTermForecasts(date : String, time: String, nx: Int, ny: Int) : List<ShortTermForecastEntity>
}