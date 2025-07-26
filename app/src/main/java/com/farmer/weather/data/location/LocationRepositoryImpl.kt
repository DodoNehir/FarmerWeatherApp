package com.farmer.weather.data.location

import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.util.Log.e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale

class LocationRepositoryImpl(private val context: Context) : LocationRepository {

    private val TAG = "LocationRepositoryImpl"

    override suspend fun getAddress(lat: Double, lon: Double): String? {

        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lon, 1)

                // admin 대구광역시  subLocality 수성구 thoroughfare=신매동
                // addressLines=[0:"대한민국 대구광역시 수성구 신매동 364-1"]
                if (addresses?.first() != null) {
                    val adr = addresses.first()
                    Log.d(TAG, "get address from lat, lon: ${adr}")
                    // 순서대로 가장 작은 행정구역을 우선 반환함
                    if (adr.thoroughfare != null) {
                        return@withContext adr.thoroughfare
                    } else if (adr.subLocality != null) {
                        return@withContext adr.subLocality
                    }
                }
                null
            } catch (e: IOException) {
                null
            }
        }
    }

    override fun convertToNxNy(
        lat: Double,
        lon: Double
    ): Pair<Int, Int> {
        val RE = 6371.00877 // 지구 반경(km)
        val GRID = 5.0      // 격자 간격(km)
        val SLAT1 = 30.0    // 투영 위도1(degree)
        val SLAT2 = 60.0    // 투영 위도2(degree)
        val OLON = 126.0    // 기준점 경도
        val OLAT = 38.0     // 기준점 위도
        val XO = 43         // 기준점 X좌표 (GRID)
        val YO = 136        // 기준점 Y좌표 (GRID)

        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = lon * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return Pair(x, y)
    }
}