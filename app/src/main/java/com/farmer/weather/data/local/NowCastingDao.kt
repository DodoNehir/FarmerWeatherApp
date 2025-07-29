package com.farmer.weather.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface NowCastingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNowCasting(nowCastingEntity: NowCastingEntity)

    @Query(value = "DELETE FROM now_casting WHERE baseDate < :oldDate")
    suspend fun deleteNowCasting(oldDate: Int)

    @Query(value = """
        SELECT * FROM now_casting
        WHERE baseDate = :date AND baseTime = :time AND nx = :nx AND ny = :ny
    """)
    suspend fun getNowCasting(date: Int, time: String, nx: Int, ny: Int): NowCastingEntity?
}