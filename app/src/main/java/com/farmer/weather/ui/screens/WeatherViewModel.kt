package com.farmer.weather.ui.screens

import android.util.Log
import androidx.compose.material3.DatePickerDefaults.dateFormatter
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

    object Error : WeatherUiState

    object Loading : WeatherUiState
}


class WeatherViewModel(
    private val remoteRepository: RemoteRepository
) : ViewModel() {

    val TAG = javaClass.simpleName

    var weatherUiState: WeatherUiState by mutableStateOf(WeatherUiState.Loading)
        private set

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HHmm")

    init {
        fetchWeatherData()
    }

    fun fetchWeatherData() {

        val base = calculateBase()

        viewModelScope.launch {
            val result = remoteRepository.getShortTermForecast(
                baseDate = base.first,
                baseTime = base.second
            )

            Log.i(TAG, "result TAG: ${result.TAG}")

            when (result) {
                is ApiResult.Success -> {
                    weatherUiState = WeatherUiState.Success(result.value)
                }

                is ApiResult.NoData -> {
                    weatherUiState = WeatherUiState.NoData
                }

                is ApiResult.Error -> {
                    weatherUiState = WeatherUiState.Error
                    Log.e(
                        TAG,
                        "error code: ${result.code} / message: ${result.message} / exception: ${result.exception}"
                    )
                }
            }
        }

    }

    /**
     * Pair <baseDate, baseTime> 반환
     */
    fun calculateBase(): Pair<String, String> {
        val now = LocalDateTime.now()
        val currentTimeInt = now.format(timeFormatter).toInt()

        // API 업데이트 시간 (시) : 2, 5, 8, 11, 14, 17, 20, 23
        //  00시~2시 19분: 전날의 23시 예보를 받아와야 한다.
        //  기준은 20분으로
        //  2시 20분부터 5시 19분은 2시 예보 요청

        return when (currentTimeInt) {
            in 0..219 -> Pair(now.minusDays(1).format(dateFormatter), "2300")
            in 220..519 -> Pair(now.format(dateFormatter), "0200")
            in 520..819 -> Pair(now.format(dateFormatter), "0500")
            in 820..1119 -> Pair(now.format(dateFormatter), "0800")
            in 1120..1419 -> Pair(now.format(dateFormatter), "1100")
            in 1420..1719 -> Pair(now.format(dateFormatter), "1400")
            in 1720..2019 -> Pair(now.format(dateFormatter), "1700")
            in 2020..2319 -> Pair(now.format(dateFormatter), "2000")
            in 2320..2359 -> Pair(now.format(dateFormatter), "2300")
            else -> {
                Log.e("BaseDate error", "base 시간이 잘못되었습니다. 오늘 23시 데이터를 요청합니다.")
                Pair(now.format(dateFormatter), "2300")
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