package com.farmer.weather.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,

    val location: String,
    val temperature: Int,
    val tempHigh: Int,
    val tempLow: Int,
    val time: Int,
    val wind: Int,
    val rainfall: Int
)
