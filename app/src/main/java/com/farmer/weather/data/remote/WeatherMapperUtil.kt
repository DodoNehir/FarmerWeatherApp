package com.farmer.weather.data.remote

import com.farmer.weather.data.remote.dto.Item
import com.farmer.weather.domain.NowCasting


fun Map<String, Item>.nowRequireString(key: String): String =
    this[key]?.obsrValue ?: throw IllegalStateException("code is 00 but missing ${key} key data")

fun Map<String, Item>.nowRequireDouble(key: String): Double =
    this[key]?.obsrValue?.toDoubleOrNull()
        ?: throw IllegalStateException("code is 00 but missing ${key} key data")

fun Map<String, Item>.nowRequireInt(key: String): Int =
    this[key]?.obsrValue?.toIntOrNull()
        ?: throw IllegalStateException("code is 00 but missing ${key} key data")


fun Map<String, Item>.shortRequireString(key: String): String =
    this[key]?.fcstValue ?: throw IllegalStateException("code is 00 but missing ${key} key data")

fun Map<String, Item>.shortRequireDouble(key: String): Double =
    this[key]?.fcstValue?.toDoubleOrNull()
        ?: throw IllegalStateException("code is 00 but missing ${key} key data")

fun Map<String, Item>.shortRequireInt(key: String): Int =
    this[key]?.fcstValue?.toIntOrNull()
        ?: throw IllegalStateException("code is 00 but missing ${key} key data")