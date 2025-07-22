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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


        if (dailyTemperatureEntity == null) {
            // 요청 / 저장/ 불러오기
            requestWeatherAndSave(nx, ny, now)
            val updatedEntity = localRepository.getDailyTemperature(currentDate, nx, ny)

            if (updatedEntity != null) {
                updateUiStateWith(updatedEntity, currentDate, currentTime, nx, ny)
            } else {
                Log.d(TAG, "첫 조회 결과가 없어 request & save 후 다시 조회했지만 결과가 없음")
                weatherUiState = WeatherUiState.Error
            }
        } else {
            updateUiStateWith(dailyTemperatureEntity, currentDate, currentTime, nx, ny)
        }

        // UI 업데이트 후 옛날 정보 삭제
        withContext(Dispatchers.IO) {
            val expiryDate = now.minusDays(2L).format(dateFormatter).toInt()
            localRepository.deleteDailyTemperature(expiryDate)
            localRepository.deleteShortTermForecasts(expiryDate)
        }

    }


    suspend fun requestWeatherAndSave(nx: Int, ny: Int, now: LocalDateTime) {
        Log.d(TAG, "오늘의 Daily Temperature 가 없습니다. weather API 호출합니다.")
        val apiRequestResult = fetchDailyMinMax(nx, ny, now)

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
        Log.d(TAG, "Start update UI state")

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
        val groupedByDate = result
            .filter { it.minTemperature != null || it.maxTemperature != null }
            .groupBy { it.fcstDate }

        for ((fcstDate, fcstList) in groupedByDate) {
            val min = fcstList.find { it.minTemperature != null }?.minTemperature
            val max = fcstList.find { it.maxTemperature != null }?.maxTemperature

            if (min != null && max != null) {
                val dailyTemp = DailyTemperature(
                    fcstDate = fcstDate,
                    baseDate = fcstList.first().baseDate,
                    baseTime = fcstList.first().baseTime,
                    minTemperature = min,
                    maxTemperature = max,
                    nx = nx,
                    ny = ny
                )
                localRepository.insertDailyTemperature(dailyTemp.toEntity())
                Log.d(TAG, "date: ${fcstDate} 의 min, max 저장함")
            } else {
                Log.d(TAG, " saveData 도중 date:${fcstDate} 의 min or max 가 없어 저장하지 않음")
            }
        }

        // 2. save Short Term Forecast
        localRepository.insertShortTermForecasts(result.map {
            it.toEntity()
        })
        Log.d(TAG, "전체 예보 저장함")
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


    suspend fun fetchDailyMinMax(
        nx: Int,
        ny: Int,
        now: LocalDateTime
    ): ApiResult<List<ShortTermForecast>> {
        val time = now.format(timeFormatter)
        val today = now.format(dateFormatter).toInt()
        val yesterday = now.minusDays(1L).format(dateFormatter).toInt()

        // TODO Min, MAX를 알기 위함이므로 NumOfRow를 조정해야 할 것 같다
        // 오늘 2시 10분 이후  : 오늘 2시 base 요청
        // 오늘 0시-2시10분 전 : 어제 2시 Base 요청
        if (time < "0210") {
            Log.d(TAG, "어제 02시 데이터를 요청합니다.")
            // 00:00 - 02:09
            return remoteRepository.getShortTermForecast(
                baseDate = yesterday,
                baseTime = "0200",
                nx = nx,
                ny = ny
            )
        } else {
            Log.d(TAG, "오늘 02시 데이터를 요청합니다.")
            // 02:10 - 23:59
            return remoteRepository.getShortTermForecast(
                baseDate = today,
                baseTime = "0200",
                nx = nx,
                ny = ny
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