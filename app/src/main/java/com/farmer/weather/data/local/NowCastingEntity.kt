package com.farmer.weather.data.local

import androidx.room.Entity
import com.farmer.weather.domain.NowCasting

@Entity(
    tableName = "now_casting",
    primaryKeys = ["baseDate", "baseTime", "nx", "ny"]
)
data class NowCastingEntity(
    val baseDate: Int,
    val baseTime: String,
    val nx: Int,
    val ny: Int,
    val temperature: Double,
    val rn1: String, // 1시간 강수량 : 강수없음 / 0 / 1mm 미만 / 6.9mm .. / 50.0mm 이상
    val humidity: Int,

    // 단기예보와 조금 다르다!
    // 0 없음 / 1 비 / 2 비/눈 / 3 눈 / 5 빗방울 / 6 빗방울눈날림 / 7 눈날림
    val precipitationType: Int, // 강수형태
    val windDirection: Int,
    val windSpeed: Double
)

fun NowCastingEntity.toDomain() = NowCasting(
    baseDate = this.baseDate,
    baseTime = this.baseTime,
    nx = this.nx,
    ny = this.ny,
    temperature = this.temperature,
    rn1 = this.rn1,
    humidity = this.humidity,
    precipitationType = this.precipitationType,
    windDirection = this.windDirection,
    windSpeed = this.windSpeed
)

fun NowCasting.toEntity() = NowCastingEntity(
    baseDate = this.baseDate,
    baseTime = this.baseTime,
    nx = this.nx,
    ny = this.ny,
    temperature = this.temperature,
    rn1 = this.rn1,
    humidity = this.humidity,
    precipitationType = this.precipitationType,
    windDirection = this.windDirection,
    windSpeed = this.windSpeed
)