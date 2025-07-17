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
import com.farmer.weather.data.local.DailyTemperatureEntity
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
import kotlin.text.format


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
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

//    init {
//        loadData()
//    }

    // 위치 권한이 있는 한 lat, lon 은 not null
    fun updateLocation(lat: Double, lon: Double) {
        viewModelScope.launch {

            setLocation(lat, lon)

            if (dongAddress != null && nxny != null) {
                loadData()
            } else {
                weatherUiState = WeatherUiState.Error
            }
        }
    }

    suspend fun loadData() {
        val now = LocalDateTime.now()
        val currentDate = now.format(dateFormatter).toInt()
        val currentTime = now.format(timeFormatter)
        val nx = nxny!!.first
        val ny = nxny!!.second

        val dailyTemperatureEntity =
            localRepository.getDailyTemperature(currentDate, nx, ny)


        when (shouldFetchWeather(dailyTemperatureEntity, now)) {
            0 -> {
                // 처음부터 요청 필요
                requestWeatherAndSave(nx, ny, now, 0)
                val updatedEntity = localRepository.getDailyTemperature(currentDate, nx, ny)

                if (updatedEntity != null) {
                    updateUiStateWith(updatedEntity, currentDate, currentTime, nx, ny)
                } else {
                    Log.d(TAG, "첫 조회 결과가 없어 request & save 후 다시 조회했지만 결과가 없음")
                    weatherUiState = WeatherUiState.Error
                }
            }

            1 -> {
                requestWeatherAndSave(nx, ny, now, 1)
                updateUiStateWith(dailyTemperatureEntity!!, currentDate, currentTime, nx, ny)
            }

            2 -> {
                updateUiStateWith(dailyTemperatureEntity!!, currentDate, currentTime, nx, ny)
            }
        }

    }

    fun shouldFetchWeather(savedEntity: DailyTemperatureEntity?, now: LocalDateTime): Int {
        if (savedEntity == null) return 0

        // 13시간 전 데이터라면 재요청. parse가 실패해도 재요청
        val thresholdTime = now.minusHours(13L)
        try {
            val savedTime = LocalDateTime.parse(
                savedEntity.baseDate.toString() + savedEntity.baseTime.padStart(4, '0'),
                dateTimeFormatter
            )
            if (savedTime.isBefore(thresholdTime)) {
                return 1
            } else {
                return 2
            }
        } catch (e: Exception) {
            Log.d(TAG, "local date time parse error")
            return 0
        }
    }


    suspend fun requestWeatherAndSave(nx: Int, ny: Int, now: LocalDateTime, requestFlag: Int) {
        Log.d(TAG, "weather API 호출합니다.")
        val apiRequestResult = fetchWeatherData(nx, ny, now, requestFlag)

        Log.i(TAG, "호출 결과 TAG: ${apiRequestResult.TAG}")
        when (apiRequestResult) {
            is ApiResult.Success -> {
                saveData(nx, ny, apiRequestResult.value)
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
    }


    suspend fun updateUiStateWith(
        dailyTemperatureEntity: DailyTemperatureEntity,
        currentDate: Int,
        currentTIme: String,
        nx: Int,
        ny: Int
    ) {
        Log.d(TAG, "Update UI state")

        val shortTermForecastEntities =
            localRepository.getShortTermForecasts(currentDate, currentTIme, nx, ny)

        weatherUiState = WeatherUiState.Success(
            weatherList = shortTermForecastEntities.map { it.toDomain() },
            dailyTemperature = dailyTemperatureEntity.toDomain(),
            dongAddress = dongAddress ?: "대한민국"
        )
    }

    suspend fun saveData(nx: Int, ny: Int, result: List<ShortTermForecast>) {
        // 1. save Daily Temperature
        val dailyMixedTemperatures = result.filter {
            it.minTemperature != null || it.maxTemperature != null
        }

        for (i in 0..2) {
            val index = i * 2
            val dailyTemp = DailyTemperature(
                fcstDate = dailyMixedTemperatures.get(index).fcstDate,
                baseDate =
                minTemperature = dailyMixedTemperatures.get(index).minTemperature!!,
                maxTemperature = dailyMixedTemperatures.get(index + 1).maxTemperature!!,
                nx = nx,
                ny = ny
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


    suspend fun fetchWeatherData(
        nx: Int,
        ny: Int,
        now: LocalDateTime,
        requestFlag: Int
    ): ApiResult<List<ShortTermForecast>> {
        val time = now.format(timeFormatter)
        val today = now.format(dateFormatter).toInt()
        val yesterday = now.minusDays(1L).format(dateFormatter).toInt()

        // flag 0: 2시 요청
        //      1: 14시 요청 -> 오늘 최고최저 기온은 아는데, 최신 정보로 업데이트를 또 했으면 좋겠다.
        if (requestFlag == 1) {
            return remoteRepository.getShortTermForecast(
                baseDate = today,
                baseTime = "1400",
                nx = nx,
                ny = ny
            )

        } else {
            if (time < "0220") {
                // 00:00 - 02:19
                return remoteRepository.getShortTermForecast(
                    baseDate = yesterday,
                    baseTime = "0200",
                    nx = nx,
                    ny = ny
                )

            } else {
                // 02:20 - 23:59
                return remoteRepository.getShortTermForecast(
                    baseDate = today,
                    baseTime = "0200",
                    nx = nx,
                    ny = ny
                )
            }

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