package com.farmer.weather.ui.screens

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.farmer.weather.WeatherApplication
import com.farmer.weather.data.remote.ApiResult
import com.farmer.weather.data.remote.RemoteRepository
import com.farmer.weather.domain.ShortTermForecast
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


sealed interface WeatherUiState {

    data class Success(val data: List<ShortTermForecast>) : WeatherUiState

    object NoData : WeatherUiState

    data class Error(val message: String) : WeatherUiState

    object Loading : WeatherUiState
}


class WeatherViewModel(
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    val TAG = "WeatherViewModel"

    var weatherUiState: WeatherUiState by mutableStateOf(WeatherUiState.Loading)
        private set

    init {
        fetchWeatherData()
    }

    fun fetchWeatherData() {

        val now = LocalDateTime.now()

        val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        val formattedDate = now.format(dateFormatter)

        val timeFormatter = DateTimeFormatter.ofPattern("HH")
        val formattedTime = "${now.minusHours(1L).format(timeFormatter)}00"

        viewModelScope.launch {
            val result = remoteRepository.getShortTermForecast(
                baseDate = formattedDate,
                baseTime = formattedTime
            )

            when (result) {
                is ApiResult.Success -> {
                    weatherUiState = WeatherUiState.Success(result.value)
                    Log.i(TAG, "Success")
                }

                is ApiResult.NoData -> {
                    weatherUiState = WeatherUiState.NoData
                    Log.i(TAG, "NoData")
                }

                is ApiResult.Error -> {
                    Log.i(TAG, "Error")
                    weatherUiState = WeatherUiState.Error(result.message ?: "데이터를 가져오지 못했습니다.")
                }
            }
        }

    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                val remoteRepository = application.container.remoteRepository
                WeatherViewModel(remoteRepository = remoteRepository)
            }
        }
    }

}