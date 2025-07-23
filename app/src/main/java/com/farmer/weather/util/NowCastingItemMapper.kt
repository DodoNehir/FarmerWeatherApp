package com.farmer.weather.util

import com.farmer.weather.data.remote.dto.getUltraSrtNcst.Item

fun Map<String, Item>.nowRequireString(key: String): String =
    this[key]?.obsrValue ?: throw IllegalStateException("code is 00 but missing ${key} key data")

fun Map<String, Item>.nowRequireDouble(key: String): Double =
    this[key]?.obsrValue?.toDoubleOrNull()
        ?: throw IllegalStateException("code is 00 but missing ${key} key data")

fun Map<String, Item>.nowRequireInt(key: String): Int =
    this[key]?.obsrValue?.toIntOrNull()
        ?: throw IllegalStateException("code is 00 but missing ${key} key data")