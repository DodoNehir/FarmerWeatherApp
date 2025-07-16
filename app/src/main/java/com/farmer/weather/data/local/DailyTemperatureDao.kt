package com.farmer.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyTemperatureDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemperature(dailyTemperatureEntity: DailyTemperatureEntity)

    @Query(value = "DELETE FROM daily_temperature WHERE fcstDate < :oldDate")
    suspend fun deleteTemperature(oldDate: String)

    @Query(value = """
        SELECT * FROM daily_temperature 
        WHERE nx = :nx AND ny = :ny 
         AND fcstDate = :date
    """)
    suspend fun getDailyTemperature(date: String, nx: Int, ny: Int): DailyTemperatureEntity?
}