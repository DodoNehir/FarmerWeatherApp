package com.farmer.weather.util

import com.farmer.weather.data.remote.dto.getVilageFcst.Item

fun Map<String, Item>.shortRequireString(key: String): String =
    this[key]?.fcstValue ?: throw IllegalStateException("code is 00 but missing [${key}] data")

fun Map<String, Item>.shortRequireDouble(key: String): Double =
    this[key]?.fcstValue?.toDoubleOrNull()
        ?: throw IllegalStateException("code is 00 but missing [${key}] data")

fun Map<String, Item>.shortRequireInt(key: String): Int =
    this[key]?.fcstValue?.toIntOrNull()
        ?: throw IllegalStateException("code is 00 but missing [${key}] data")