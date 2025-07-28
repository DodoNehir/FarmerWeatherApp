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

    private var currentLat: Double = Constants.DEFAULT_LAT
    private var currentLon: Double = Constants.DEFAULT_LON
    private var dongAddress: String? = null
    private var nxny: Pair<Int, Int> = locationRepository.convertToNxNy(currentLat, currentLon)
    private var now: LocalDateTime = LocalDateTime.now()

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH00")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")

//    init {
//        loadData()
//    }

    // 위치 권한이 있는 한 lat, lon 은 not null
    fun startLoadWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            // startLoadWeather 마다 시간, 위치 업데이트
            now = LocalDateTime.now()
            setLocation(lat, lon)

            if (dongAddress != null) {
                loadData()

                // UI 가 성공적으로 올라간 뒤 작업 진행
                if (weatherUiState is WeatherUiState.Success) {
                    updateForecastIfNeeded()
                    cleanUpOldWeatherData()
                }
            } else {
                weatherUiState = WeatherUiState.Error
            }
        }
    }

    // 중간에 실패하면 진행 불가
    suspend fun loadData() {
        val currentDate = now.format(dateFormatter).toInt()
        val currentTime = now.format(timeFormatter)
        val nx = nxny.first
        val ny = nxny.second
        var finalDailyTempEntity: DailyTemperatureEntity? = null

        // 1. 저장된 min, max 검색
        val dailyTemperatureEntity =
            localRepository.getDailyTemperature(currentDate, nx, ny)

        // 2. nowCasting 요청
        val nowCastingDatePair = getAvailableNowCastingBaseDateTime()
        val nowCastingResult = remoteRepository.getNowCasting(
            nowCastingDatePair.first,
            nowCastingDatePair.second,
            nx,
            ny
        )
        Log.d(TAG, "getNowCasting ApiResult: ${nowCastingResult.TAG}")
        if (nowCastingResult !is ApiResult.Success) {
            weatherUiState = mapToUiState(nowCastingResult)
            return
        }

        // 3. 기존 데이터 없으면
        if (dailyTemperatureEntity == null) {
            // 3-1. DailyTemperature 요청, 저장
            val minMaxResult = fetchDailyMinMax(nx, ny)
            Log.d(TAG, "fetchDailyMinMax ApiResult: ${minMaxResult.TAG}")
            if (minMaxResult !is ApiResult.Success) {
                weatherUiState = mapToUiState(minMaxResult)
                return
            }
            saveMinMax(minMaxResult.value)

            // 3-2. ShortTermForecast List 요청, 저장
            val forecastResult = fetchShortTermForecast()
            Log.d(TAG, "fetchShortTermForecast ApiResult: ${forecastResult.TAG}")
            if (forecastResult !is ApiResult.Success) {
                weatherUiState = mapToUiState(forecastResult)
                return
            }
            saveForecast(forecastResult.value)

            // 3-3. 다시 저장된 min, max 검색
            finalDailyTempEntity = localRepository.getDailyTemperature(currentDate, nx, ny)

            // 3-3-1. should not occur. 정상 응답에 저장도 했지만 가져오기 실패 시 프로세스 종료
            if (finalDailyTempEntity == null) {
                Log.d(TAG, "should not occur. 정상 응답에 저장도 했지만, DB 검색 실패로 프로세스 종료")
                weatherUiState = WeatherUiState.Error
                return
            }

        } else {
            // 4. 기존 데이터 있으면 그대로 사용
            finalDailyTempEntity = dailyTemperatureEntity
        }

        // 5. 날씨 리스트 검색
        val shortTermForecastEntities =
            localRepository.getShortTermForecasts(currentDate, currentTime, nx, ny)
        if (shortTermForecastEntities.isEmpty()) {
            weatherUiState = WeatherUiState.Error
            return
        }

        // 6. 상태를 Success 로 바꿈
        weatherUiState = WeatherUiState.Success(
            dongAddress = dongAddress ?: "대한민국",
            nowCasting = nowCastingResult.value,
            dailyTemperature = finalDailyTempEntity.toDomain(),
            weatherList = shortTermForecastEntities.map { it.toDomain() },
        )
    }


    fun mapToUiState(apiResult: ApiResult<*>): WeatherUiState {
        return when (apiResult) {
            is ApiResult.NoData -> WeatherUiState.NoData
            is ApiResult.Error -> WeatherUiState.Error
            else -> WeatherUiState.Error
        }
    }

    suspend fun updateForecastIfNeeded() {
        if (shouldUpdateForecast()) {
            withContext(Dispatchers.IO) {

                updateLatelyForecastAndSave()
                val result = localRepository.getShortTermForecasts(
                    now.format(dateFormatter).toInt(),
                    now.format(timeFormatter),
                    nxny.first,
                    nxny.second
                )
                if (result.isNotEmpty()) {
                    Log.d(TAG, "예보 baseTime 이 7시간 전 이므로 새로 업데이트합니다.")
                    val currentState = weatherUiState
                    if (currentState is WeatherUiState.Success) {
                        weatherUiState =
                            currentState.copy(weatherList = result.map { it.toDomain() })
                    }
                }
            }
        } else {
            Log.d(TAG, "예보가 최신 상태이므로 업데이트하지 않습니다.")
        }
    }

    suspend fun cleanUpOldWeatherData() {
        withContext(Dispatchers.IO) {
            val expiryDate = now.minusDays(2L).format(dateFormatter).toInt()
            localRepository.deleteDailyTemperature(expiryDate)
            localRepository.deleteShortTermForecasts(expiryDate)
        }
    }

    suspend fun updateLatelyForecastAndSave() {
        val forecastResult = fetchShortTermForecast()
        Log.d(TAG, "updateLately ApiResult: ${forecastResult.TAG}")

        if (forecastResult is ApiResult.Success) {
            saveForecast(forecastResult.value)
            saveMinMax(forecastResult.value) // min, max도 최신으로 업데이트
        }
    }

    suspend fun shouldUpdateForecast(): Boolean {
        val entity = localRepository.getOneForecast(
            now.format(dateFormatter).toInt(),
            now.format(timeFormatter),
            nxny.first,
            nxny.second
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

    suspend fun saveMinMax(result: List<ShortTermForecast>) {
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
                    nx = nxny.first,
                    ny = nxny.second
                )
                localRepository.insertDailyTemperature(dailyTemp.toEntity())
                Log.d(TAG, "saved min, max fcstDate: ${fcstDate}")
            } else {
                Log.d(TAG, "While saving fcstDate: ${fcstDate} there's no value (min or max)")
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
        Log.d(TAG, "dongAddress: $dongAddress")
        nxny = locationRepository.convertToNxNy(lat, lon)
        Log.d(TAG, "nx: ${nxny.first}   ny: ${nxny.second}")
    }


    suspend fun fetchDailyMinMax(
        nx: Int,
        ny: Int
    ): ApiResult<List<ShortTermForecast>> {
        val time = now.format(timeFormatter)
        val today = now.format(dateFormatter).toInt()
        val yesterday = now.minusDays(1L).format(dateFormatter).toInt()

        if (time < "0210") {
            Log.d(TAG, "time is ${time}. request yesterday 23:00.")
            // 00:00 - 02:09
            return remoteRepository.getShortTermForecast(
                baseDate = yesterday,
                baseTime = "2300",
                numOfRows = Constants.TMN_TMX_23_NUM_OF_ROWS,
                nx = nx,
                ny = ny
            )
        } else {
            Log.d(TAG, "time is ${time}. request today 02:00")
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

    suspend fun fetchShortTermForecast(): ApiResult<List<ShortTermForecast>> {
        Log.d(TAG, "start fetchShortTermForecast")
        val availableDateTimePair = getAvailableForecastBaseDateTime()
        Log.d(TAG, "availableDateTimePair: ${availableDateTimePair}")
        return remoteRepository.getShortTermForecast(
            baseDate = availableDateTimePair.first,
            baseTime = availableDateTimePair.second,
            numOfRows = Constants.FORECAST_NUM_OF_ROWS,
            nx = nxny.first,
            ny = nxny.second
        )
    }

    fun getAvailableForecastBaseDateTime(): Pair<Int, String> {
        val availableTimes = listOf(2, 5, 8, 11, 14, 17, 20, 23)
        val currentHour = now.hour

        var baseHour = availableTimes.lastOrNull { it < currentHour } ?: 23
        var baseDate = now

        if (baseHour == 23) {
            // baseHour 가 23이 되는 경우는 어제로 바꿔야 하는 경우다.
            // ex) 23:50 은 baseHour 20시로 요청 됨
            baseDate = now.minusDays(1L)
        }

        val hourString = String.format(Locale.KOREA, "%02d00", baseHour)

        return (baseDate.format(dateFormatter).toInt() to hourString)
    }

    fun getAvailableNowCastingBaseDateTime(): Pair<Int, String> {
        val availableTimes: IntRange = 0..23
        val currentHour = now.hour
        val currentMinute = now.minute

        var baseHour = availableTimes.last { it <= currentHour }
        var baseDate = now

        if (currentMinute < 13) {
            // 각 시각 13분부터 요청 가능. 한 타임 전으로 이동
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