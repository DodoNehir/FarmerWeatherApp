package com.farmer.weather.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel
class RadarViewModel @Inject constructor(): ViewModel() {
    private val _imageUrl = MutableStateFlow("")
    val imageUrl: StateFlow<String> = _imageUrl

    val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHm0")

    init {
//        viewModelScope.launch {
//            while(isActive) {
                loadLatestUrl()
                // TODO delay??
//            }
//        }
    }

    private fun loadLatestUrl() {
        val now = LocalDateTime.now()
        val fileName = now.minusMinutes(10L).format(formatter)
        _imageUrl.value = "https://www.kma.go.kr/repositary/image/rdr/img/RDR_CMP_WRC_${fileName}.png"
    }
}