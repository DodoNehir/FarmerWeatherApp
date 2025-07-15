package com.farmer.weather.data.location

interface LocationRepository {
    suspend fun getAddress(lat: Double, lon: Double): String?
    fun convertToNxNy(lat: Double, lon: Double): Pair<Int, Int>
}