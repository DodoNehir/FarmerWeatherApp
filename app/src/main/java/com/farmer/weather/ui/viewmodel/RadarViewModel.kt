package com.farmer.weather.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class RadarViewModel @Inject constructor() : ViewModel() {
//    private val _imageUrl = MutableStateFlow("")
//    val imageUrl: StateFlow<String> = _imageUrl

    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

    fun radarUrlFlow(): Flow<String> = flow {
        while (true) {
            emit(createUrl())
            delay(5 * 60 * 1000L) // 5분마다
        }
    }

    private fun createUrl(): String {
        val now = LocalDateTime.now()

        val radarTime = now
            .minusMinutes(10L)
            .let { it.withMinute(it.minute / 10 * 10) }
            .format(formatter)

        val fileName = "https://www.kma.go.kr/repositary/image/rdr/img/RDR_CMP_WRC_${radarTime}.png"
        Log.d("RadarViewModel", "image URL: ${fileName}")

        return fileName
    }
}