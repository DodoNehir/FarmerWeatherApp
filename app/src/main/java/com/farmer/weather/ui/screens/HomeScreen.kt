package com.farmer.weather.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.farmer.weather.R
import com.farmer.weather.domain.DailyTemperature
import com.farmer.weather.domain.NowCasting
import com.farmer.weather.domain.ShortTermForecast
import com.farmer.weather.ui.viewmodel.WeatherUiState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    weatherUiState: WeatherUiState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    when (weatherUiState) {
        is WeatherUiState.Success -> WeatherInfoScreen(
            modifier = modifier,
            dongAddress = weatherUiState.dongAddress,
            nowCasting = weatherUiState.nowCasting,
            dailyTemp = weatherUiState.dailyTemperature,
            shortTermList = weatherUiState.weatherList,
            contentPadding = contentPadding,
        )

        is WeatherUiState.NoData -> NoDataScreen(
            modifier = modifier.padding(contentPadding)
        )

        is WeatherUiState.Error -> ErrorScreen(
            modifier = modifier.padding(contentPadding)
        )

        is WeatherUiState.Loading -> LoadingScreen(
            modifier = modifier.padding(contentPadding)
        )
    }
}

@Composable
fun WeatherInfoScreen(
    modifier: Modifier = Modifier,
    dongAddress: String,
    nowCasting: NowCasting,
    dailyTemp: DailyTemperature,
    shortTermList: List<ShortTermForecast>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Log.d("WeatherInfoScreen", "nowCasting: ${nowCasting}")
    Log.d("WeatherInfoScreen", "dailyTemp: ${dailyTemp}")
    if (shortTermList.isNotEmpty()) {
        Log.d("WeatherInfoScreen", "shortTermList[0]: ${shortTermList.get(0)}")
        Log.d("WeatherInfoScreen", "shortTermList[1]: ${shortTermList.get(1)}")
    }
    LazyColumn(
        modifier = modifier.padding(contentPadding),
        // 아래 contentPadding은 리스트 안쪽 여백
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        item {
            CurrentHighlightCard(
                modifier = Modifier.fillMaxWidth(),
                dongAddress = dongAddress,
                nowCasting = nowCasting,
                forecast = shortTermList.first(),
                dailyTemp = dailyTemp,
            )

        }

        items(
            items = shortTermList.drop(1),
            key = { "${it.fcstDate}_${it.fcstTime}" }
        ) { item ->
            WeatherCard(weather = item)
        }


    }
}

@Composable
fun CurrentHighlightCard(
    modifier: Modifier = Modifier,
    dongAddress: String,
    nowCasting: NowCasting,
    forecast: ShortTermForecast,
    dailyTemp: DailyTemperature,
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dongAddress,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${nowCasting.temperature}°",
                        style = MaterialTheme.typography.displayLarge
                    )

                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = getNowCastingIcon(
                            nowCasting.precipitationType,
                            forecast.skyStatus
                        ),
                        contentDescription = "weather icon",
                        modifier = Modifier.size(100.dp)
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getNowCastingText(
                        nowCasting.precipitationType,
                        forecast.skyStatus
                    ),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(24.dp))
                // 비올때만 강수량 표시
                if (nowCasting.precipitationType in 1..7) {
                    Text(
                        text = nowCasting.rn1,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text(
                    text = "최고 ${dailyTemp.maxTemperature}° / 최저 ${dailyTemp.minTemperature}°"
                )
                Spacer(modifier = Modifier.width(24.dp))
                Text(
                    text = "습도 ${nowCasting.humidity}%"
                )

            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                val isDark = isSystemInDarkTheme()
                val arrowColor = if (isDark) Color.White else Color.Black
                Image(
                    painterResource(R.drawable.baseline_arrow_upward_24),
                    contentDescription = "wind direction",
                    modifier = Modifier.rotate(nowCasting.windDirection.toFloat()),
                    colorFilter = ColorFilter.tint(arrowColor)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${nowCasting.windSpeed}m/s"
                )
            }
        }
    }
}

@Composable
fun WeatherCard(
    weather: ShortTermForecast,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getTimeString(weather.fcstTime),
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(modifier = Modifier.weight(1f))

            // 비 올 때만 강수량과 강수확률 표시
            if (weather.precipitationType in 1..4) {
                Column(
                    modifier = Modifier.width(136.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${weather.pcp} ",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${weather.pop} %",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = getForecastIcon(weather.precipitationType, weather.skyStatus),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${weather.temperature}°C",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }

}

@Composable
fun getNowCastingText(nowPType: Int, forecastSkyStatus: Int): String {
    // 실황의 타입은 강수타입을 세분한다. 비가 오지 않을 때 맑음/구름많음/흐림 판단을 위해 예보의 skyStatus 사용
    return when (nowPType) {
        1 -> stringResource(R.string.rain)
        2 -> stringResource(R.string.sleet)
        3 -> stringResource(R.string.snow)
        4 -> stringResource(R.string.showers)
        5 -> stringResource(R.string.raindrop)
        6 -> stringResource(R.string.sleet) // 빗방울눈날림
        7 -> stringResource(R.string.snow) // 눈날림
        0 -> when (forecastSkyStatus) {
            1 -> stringResource(R.string.clear)
            3 -> stringResource(R.string.cloudy)
            4 -> stringResource(R.string.overcast)
            else -> {
                Log.e(
                    "getNowCastingText",
                    "nowPType = 0 but forecastSkyStatus doesn't match anything"
                )
                " "
            }
        }

        else -> {
            Log.e("getNowCastingText", "nowPType not in 0..7")
            " "
        }
    }
}

@Composable
fun getNowCastingIcon(nowPType: Int, forecastSkyStatus: Int): Painter {
    return when (nowPType) {
        1 -> painterResource(R.drawable.rain)
        2 -> painterResource(R.drawable.sleet)
        3 -> painterResource(R.drawable.snow)
        4 -> painterResource(R.drawable.showers)
        5 -> painterResource(R.drawable.rain)
        6 -> painterResource(R.drawable.sleet) // 빗방울눈날림
        7 -> painterResource(R.drawable.snow) // 눈날림
        0 -> when (forecastSkyStatus) {
            1 -> painterResource(R.drawable.clear)
            3 -> painterResource(R.drawable.cloudy)
            4 -> painterResource(R.drawable.overcast)
            else -> {
                Log.e(
                    "getNowCastingText",
                    "nowPType = 0 but forecastSkyStatus doesn't match anything"
                )
                painterResource(R.drawable.cloudy)
            }
        }

        else -> {
            Log.e("getNowCastingText", "nowPType not in 0..7")
            painterResource(R.drawable.cloudy)
        }
    }
}

@Composable
fun getForecastIcon(pType: Int?, skyStatus: Int?): Painter {
    /**
     * precipitationType 강수 형태 : 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
     * skyStatus 하늘 상태 : 맑음(1), 구름많음(3), 흐림(4)
     */
    return when (pType) {
        1 -> painterResource(R.drawable.rain)
        2 -> painterResource(R.drawable.sleet)
        3 -> painterResource(R.drawable.snow)
        4 -> painterResource(R.drawable.showers)
        0 -> when (skyStatus) {
            1 -> painterResource(R.drawable.clear)
            3 -> painterResource(R.drawable.cloudy)
            4 -> painterResource(R.drawable.overcast)
            else -> {
                Log.e("getWeatherIcon", "pType = 0 이지만 skyStatus 에러")
                painterResource(R.drawable.cloudy)
            }
        }

        else -> {
            Log.e("getWeatherIcon", "pType 에러")
            painterResource(R.drawable.cloudy)
        }
    }

}

fun getTimeString(fcstTime: String): String {
    val time = fcstTime.substring(0, 2).toInt()
    if (time == 12) {
        return "오후 12시"
    } else if (time == 0) {
        return "오전 12시"
    } else if (time > 12) {
        return "오후 ${time - 12}시"
    } else {
        return "오전 ${time}시"
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherCardPreview() {
    val dummyShortTermForecast = ShortTermForecast(
        baseDate = 20250705,
        baseTime = "1500",
        fcstDate = 20250705,
        fcstTime = "1500",
        nx = 127,
        ny = 90,
        pop = 60,
        precipitationType = 0,
        pcp = "30.0 ~ 50.0mm",
        skyStatus = 1,
        temperature = 36,
        minTemperature = null,
        maxTemperature = null,
        windSpeed = 1.1,
    )
    val dummyDailyTemperature = DailyTemperature(
        fcstDate = 20250705,
        baseDate = 20250705,
        baseTime = "0200",
        nx = 127,
        ny = 90,
        minTemperature = 26,
        maxTemperature = 34
    )
    val dummyNowCasting = NowCasting(
        baseDate = 20250725,
        baseTime = "1600",
        nx = 127,
        ny = 90,
        temperature = 30.1,
        rn1 = "30.0 ~ 50.0mm",
        humidity = 60,
        precipitationType = 0,
        windDirection = 248,
        windSpeed = 2.4
    )

//    WeatherCard(weather = dummyShortTermForecast)
    CurrentHighlightCard(
        dongAddress = "신매동",
        dailyTemp = dummyDailyTemperature,
        nowCasting = dummyNowCasting,
        forecast = dummyShortTermForecast,
    )
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(100.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 10.dp
        )
    }
}


@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.cancel),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = stringResource(R.string.error),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}

@Composable
fun NoDataScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Image(
                painter = painterResource(R.drawable.nodata),
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
            Text(
                text = stringResource(R.string.no_data),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}