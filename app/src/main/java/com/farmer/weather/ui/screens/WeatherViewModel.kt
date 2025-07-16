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
import com.farmer.weather.data.local.LocalRepository
import com.farmer.weather.data.local.toDomain
import com.farmer.weather.data.local.toEntity
import com.farmer.weather.data.location.LocationRepository
import com.farmer.weather.data.remote.ApiResult
import com.farmer.weather.data.remote.RemoteRepository
import com.farmer.weather.domain.DailyTemperature
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.util.Constants
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


sealed interface WeatherUiState {

    data class Success(
        val weatherList: List<ShortTermForecast>,
        val dailyTemperature: DailyTemperature,
        val dongAddress: String
    ) : WeatherUiState

    object NoData : WeatherUiState

    object Error : WeatherUiState

    object Loading : WeatherUiState
}


class WeatherViewModel(
    private val localRepository: LocalRepository,
    private val remoteRepository: RemoteRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    val TAG = javaClass.simpleName

    var weatherUiState: WeatherUiState by mutableStateOf(WeatherUiState.Loading)
        private set

    private var currentLat: Double? = null
    private var currentLon: Double? = null
    private var dongAddress: String? = null
    private var nxny: Pair<Int, Int>? = null

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH00")

//    init {
//        loadData()
//    }

    // 위치 권한이 있는 한 lat, lon 은 not null
    fun updateLocation(lat: Double, lon: Double) {
        viewModelScope.launch {

            setLocation(lat, lon)

            if (dongAddress != null && nxny != null) {
                loadData()
            }
        }
    }

    suspend fun loadData() {
        val now = LocalDateTime.now()
        val dailyTemperatureEntity =
            localRepository.getDailyTemperature(now.format(dateFormatter))

        if (dailyTemperatureEntity == null) {
            Log.d(TAG, "local에 Daily Temperature 없음. API 호출 시작")

            val apiRequestResult = fetchWeatherData()

            Log.i(TAG, "result TAG: ${apiRequestResult.TAG}")

            when (apiRequestResult) {
                is ApiResult.Success -> {
                    // api 결과를 로컬에 저장
                    saveData(apiRequestResult.value)

                    // 저장 값 불러오며 상태 업데이트
                    val dailyTemperatureEntity =
                        localRepository.getDailyTemperature(
                            now.format(dateFormatter)
                        )
                    val shortTermForecastEntities =
                        localRepository.getShortTermForecasts(
                            now.format(dateFormatter),
                            now.format(timeFormatter)
                        )

                    weatherUiState = WeatherUiState.Success(
                        weatherList = shortTermForecastEntities.map { it.toDomain() },
                        dailyTemperature = dailyTemperatureEntity!!.toDomain(),
                        dongAddress = dongAddress ?: "대한민국"
                    )
                }

                is ApiResult.NoData -> {
                    weatherUiState = WeatherUiState.NoData
                }

                is ApiResult.Error -> {
                    weatherUiState = WeatherUiState.Error
                    Log.e(
                        TAG,
                        "error code: ${apiRequestResult.code} / message: ${apiRequestResult.message} / exception: ${apiRequestResult.exception}"
                    )
                }
            }


        } else {
            Log.d(TAG, "local 에서 Daily Temperature 발견: ${dailyTemperatureEntity}")

            val shortTermForecastEntities =
                localRepository.getShortTermForecasts(
                    now.format(dateFormatter),
                    now.format(timeFormatter)
                )

            weatherUiState = WeatherUiState.Success(
                weatherList = shortTermForecastEntities.map { it.toDomain() },
                dailyTemperature = dailyTemperatureEntity.toDomain(),
                dongAddress = dongAddress ?: "대한민국"
            )
        }

    }

    /**
     * 받은 모든 데이터를 로컬에 저장
     * 불러올 때의 SQL에서 날짜, 시간에 따라 불러온다.
     */
    suspend fun saveData(result: List<ShortTermForecast>) {
        // 1. save Daily Temperature
        val dailyMixedTemperatures = result.filter {
            it.minTemperature != null || it.maxTemperature != null
        }

        for (i in 0..2) {
            val index = i * 2
            val dailyTemp = DailyTemperature(
                fcstDate = dailyMixedTemperatures.get(index).fcstDate,
                minTemperature = dailyMixedTemperatures.get(index).minTemperature!!,
                maxTemperature = dailyMixedTemperatures.get(index + 1).maxTemperature!!
            )
            localRepository.insertDailyTemperature(dailyTemp.toEntity())
        }

        // 2. save Short Term Forecast
        localRepository.insertShortTermForecasts(result.map {
            it.toEntity()
        })
    }

    /**
     * 주소명, nx ny 값
     */
    suspend fun setLocation(lat: Double, lon: Double) {
        if (currentLat == lat && currentLon == lon) return
        currentLat = lat
        currentLon = lon

        dongAddress = locationRepository.getAddress(lat, lon)
        Log.d(TAG, " 동 이름: $dongAddress")
        nxny = locationRepository.convertToNxNy(lat, lon)
        Log.d(TAG, "nx: ${nxny?.first}   ny: ${nxny?.second}")
    }

    /**
     * API 요청 시 최고/최저 기온 표시를 위해
     * 0시 - 2시면 전날 데이터를 받아오고
     * 2시 - 24시면 오늘 데이터를 받아온다.
     */
    suspend fun fetchWeatherData(): ApiResult<List<ShortTermForecast>> {
        val now = LocalDateTime.now()
        val time = now.format(timeFormatter)

        if (time < "0220") {
            // 00:00 - 02:19
            return remoteRepository.getShortTermForecast(
                baseDate = now.minusDays(1L).format(dateFormatter),
                baseTime = "0200",
                nx = nxny?.first ?: Constants.DEFAULT_NX,
                ny = nxny?.second ?: Constants.DEFAULT_NY
            )

        } else {
            // 02:20 - 23:59
            return remoteRepository.getShortTermForecast(
                baseDate = now.format(dateFormatter),
                baseTime = "0200",
                nx = nxny?.first ?: Constants.DEFAULT_NX,
                ny = nxny?.second ?: Constants.DEFAULT_NY
            )
        }
    }




    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as WeatherApplication)
                val localRepository = application.container.localRepository
                val remoteRepository = application.container.remoteRepository
                val locationRepository = application.container.locationRepository
                WeatherViewModel(
                    localRepository = localRepository,
                    remoteRepository = remoteRepository,
                    locationRepository = locationRepository
                )
            }
        }
    }

}