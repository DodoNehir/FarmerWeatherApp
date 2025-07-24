package com.farmer.weather.ui.screens

import android.util.Log
import androidx.collection.intListOf
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
import com.farmer.weather.data.local.DailyTemperatureEntity
import com.farmer.weather.data.local.LocalRepository
import com.farmer.weather.data.local.toDomain
import com.farmer.weather.data.local.toEntity
import com.farmer.weather.data.location.LocationRepository
import com.farmer.weather.data.remote.ApiResult
import com.farmer.weather.data.remote.RemoteRepository
import com.farmer.weather.domain.DailyTemperature
import com.farmer.weather.domain.NowCasting
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale


sealed interface WeatherUiState {

    data class Success(
        val nowCasting: NowCasting,
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

        val nowCastingDatePair = getAvailableNowCastingBaseDateTime(now)
        val nowCastingResult = remoteRepository.getNowCasting(
            nowCastingDatePair.first,
            nowCastingDatePair.second,
            nx,
            ny
        )
        when (nowCastingResult) {
            is ApiResult.Success -> {
                Log.d(TAG, "NowCasting 받기 성공")

                if (dailyTemperatureEntity == null) {
                    requestWeatherAndSave(nx, ny, now)
                    val updatedEntity = localRepository.getDailyTemperature(currentDate, nx, ny)

                    if (updatedEntity != null) {
                        updateUiStateWith(
                            updatedEntity,
                            nowCastingResult.value,
                            currentDate,
                            currentTime,
                            nx,
                            ny
                        )
                    } else {
                        Log.d(TAG, "첫 조회 결과가 없어 request & save 후 다시 조회했지만 결과가 없음")
                        weatherUiState = WeatherUiState.Error
                    }
                } else {
                    updateUiStateWith(
                        dailyTemperatureEntity,
                        nowCastingResult.value,
                        currentDate,
                        currentTime,
                        nx,
                        ny
                    )
                }

                // 현재 상태가 Success 일때만 업데이트 확인하기
                val preState = weatherUiState
                if (preState is WeatherUiState.Success) {
                    if (shouldUpdateForecast(nx, ny, now)) {
                        withContext(Dispatchers.IO) {
                            updateLatelyForecastAndSave(nx, ny, now)

                            val result = localRepository.getShortTermForecasts(
                                currentDate,
                                currentTime,
                                nx,
                                ny
                            )

                            Log.d(TAG, "이전에 받은 예보가 7시간 전의 예보이므로 새로 업데이트합니다.")
                            weatherUiState =
                                preState.copy(weatherList = result.map { it.toDomain() })
                        }
                    } else {
                        Log.d(TAG, "예보가 최신 상태이므로 업데이트하지 않습니다.")
                    }
                }

                // UI 업데이트 후 옛날 정보 삭제
                withContext(Dispatchers.IO) {
                    val expiryDate = now.minusDays(2L).format(dateFormatter).toInt()
                    localRepository.deleteDailyTemperature(expiryDate)
                    localRepository.deleteShortTermForecasts(expiryDate)
                }
            }

            is ApiResult.NoData -> {
                Log.d(TAG, "NowCasting을 받으려 했지만 NoData")
                weatherUiState = WeatherUiState.NoData
                return
            }

            is ApiResult.Error -> {
                Log.d(TAG, "NowCasting을 받으려 했지만 Error")
                weatherUiState = WeatherUiState.Error
                return
            }
        }


    }


    suspend fun requestWeatherAndSave(nx: Int, ny: Int, now: LocalDateTime) {
        Log.d(TAG, "오늘의 Daily Temperature 가 없습니다. min,max와 최신 예보를 가져오고 저장합니다.")

        val minMaxResult = fetchDailyMinMax(nx, ny, now)
        Log.d(TAG, "min max 결과 TAG: ${minMaxResult.TAG}")
        when (minMaxResult) {
            is ApiResult.Success -> {
                saveMinMax(nx, ny, minMaxResult.value)
            }

            is ApiResult.NoData -> {
                weatherUiState = WeatherUiState.NoData
            }

            is ApiResult.Error -> {
                weatherUiState = WeatherUiState.Error
                Log.e(
                    TAG,
                    "error code: ${minMaxResult.code} / message: ${minMaxResult.message} / exception: ${minMaxResult.exception}"
                )
            }
        }

        val forecastResult = fetchLatelyForecast(nx, ny, now)
        Log.d(TAG, "최신 날씨 결과 TAG: ${forecastResult.TAG}")
        when (forecastResult) {
            is ApiResult.Success -> {
                saveForecast(forecastResult.value)
            }

            is ApiResult.NoData -> {
                weatherUiState = WeatherUiState.NoData
            }

            is ApiResult.Error -> {
                weatherUiState = WeatherUiState.Error
                Log.e(
                    TAG,
                    "error code: ${forecastResult.code} / message: ${forecastResult.message} / exception: ${forecastResult.exception}"
                )
            }
        }
    }

    suspend fun updateLatelyForecastAndSave(nx: Int, ny: Int, now: LocalDateTime) {
        val forecastResult = fetchLatelyForecast(nx, ny, now)
        Log.d(TAG, "최신 날씨 결과 TAG: ${forecastResult.TAG}")

        if (forecastResult is ApiResult.Success) {
            saveForecast(forecastResult.value)
            saveMinMax(nx, ny, forecastResult.value) // min, max도 최신으로 업데이트
        }
    }

    suspend fun shouldUpdateForecast(nx: Int, ny: Int, now: LocalDateTime): Boolean {
        val entity = localRepository.getOneForecast(
            now.format(dateFormatter).toInt(),
            now.format(timeFormatter),
            nx,
            ny
        ) ?: return true

        val stored = entity.toDomain()
        val storedDate =
            try {
                LocalDateTime.parse(
                    stored.baseDate.toString() + stored.baseTime,
                    dateTimeFormatter
                )
            } catch (e: Exception) {
                return true // parse 에 실패해도 다시 업데이트하기
            }

        return storedDate.isBefore(now.minusHours(7L))
    }

    suspend fun updateUiStateWith(
        dailyTemperatureEntity: DailyTemperatureEntity,
        nowCasting: NowCasting,
        currentDate: Int,
        currentTime: String,
        nx: Int,
        ny: Int
    ) {
        Log.d(TAG, "Start update UI state")


        val shortTermForecastEntities =
            localRepository.getShortTermForecasts(currentDate, currentTime, nx, ny)


        weatherUiState = WeatherUiState.Success(
            nowCasting = nowCasting,
            weatherList = shortTermForecastEntities.map { it.toDomain() },
            dailyTemperature = dailyTemperatureEntity.toDomain(),
            dongAddress = dongAddress ?: "대한민국"
        )
    }

    suspend fun saveMinMax(nx: Int, ny: Int, result: List<ShortTermForecast>) {
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
                Log.d(TAG, "save min, max at ${fcstDate}")
            } else {
                Log.d(TAG, " saveMinMax 도중 ${fcstDate}  min or max 가 없어 저장하지 않음")
            }
        }
    }

    suspend fun saveForecast(result: List<ShortTermForecast>) {
        localRepository.insertShortTermForecasts(result.map {
            it.toEntity()
        })
        Log.d(TAG, "예보 저장")
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

        if (time < "0210") {
            Log.d(TAG, "어제 02시 데이터를 요청합니다.")
            // 00:00 - 02:09
            return remoteRepository.getShortTermForecast(
                baseDate = yesterday,
                baseTime = "2300",
                numOfRows = Constants.TMN_TMX_23_NUM_OF_ROWS,
                nx = nx,
                ny = ny
            )
        } else {
            Log.d(TAG, "오늘 02시 데이터를 요청합니다.")
            // 02:10 - 23:59
            return remoteRepository.getShortTermForecast(
                baseDate = today,
                baseTime = "0200",
                numOfRows = Constants.TMN_TMX_2_NUM_OF_ROWS,
                nx = nx,
                ny = ny
            )
        }

    }

    suspend fun fetchLatelyForecast(
        nx: Int,
        ny: Int,
        now: LocalDateTime
    ): ApiResult<List<ShortTermForecast>> {
        Log.d(TAG, "최신 예보를 조회합니다.")
        val availableDateTimePair = getAvailableForecastBaseDateTime(now)
        return remoteRepository.getShortTermForecast(
            baseDate = availableDateTimePair.first,
            baseTime = availableDateTimePair.second,
            numOfRows = Constants.FORECAST_NUM_OF_ROWS,
            nx = nx,
            ny = ny
        )
    }

    fun getAvailableForecastBaseDateTime(now: LocalDateTime): Pair<Int, String> {
        val availableTimes = listOf(2, 5, 8, 11, 14, 17, 20, 23)
        val currentHour = now.hour
        val currentMinute = now.minute

        var baseHour = availableTimes.lastOrNull { it <= currentHour } ?: 23
        var baseDate = now

        if (baseHour == currentHour && currentMinute < 10) {
            // 각 시각 10분 이후에만 요청 가능. 한 타임 전으로 이동
            val currentIndex = availableTimes.indexOf(baseHour)
            if (currentIndex == 0) {
                // 전날로 요청하기
                baseDate = now.minusDays(1L)
                baseHour = 23
            } else {
                baseHour = availableTimes[currentIndex - 1]
            }
        }

        val hourString = String.format(Locale.KOREA, "%02d00", baseHour)

        return (baseDate.format(dateFormatter).toInt() to hourString)
    }

    fun getAvailableNowCastingBaseDateTime(now: LocalDateTime): Pair<Int, String> {
        val availableTimes: IntRange = 0..23
        val currentHour = now.hour
        val currentMinute = now.minute

        var baseHour = availableTimes.last { it <= currentHour }
        var baseDate = now

        if (currentMinute < 10) {
            // 각 시각 10분 이후에만 요청 가능. 한 타임 전으로 이동
            val currentIndex = availableTimes.indexOf(baseHour)
            if (currentIndex == 0) {
                // 전날로 요청하기
                baseDate = now.minusDays(1L)
                baseHour = 23
            } else {
                baseHour = availableTimes.elementAt(currentIndex - 1)
            }
        }

        val hourString = String.format(Locale.KOREA, "%02d00", baseHour)

        return (baseDate.format(dateFormatter).toInt() to hourString)
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