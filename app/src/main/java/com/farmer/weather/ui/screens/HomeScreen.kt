package com.farmer.weather.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.farmer.weather.R
import com.farmer.weather.domain.ShortTermForecast

@Composable
fun HomeScreen(
    weatherUiState: WeatherUiState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier,
) {
    when (weatherUiState) {
        is WeatherUiState.Success -> WeatherInfoScreen(
            data = weatherUiState.data,
            modifier = Modifier.fillMaxWidth()
        )

        is WeatherUiState.NoData -> NoDataScreen(
            modifier = Modifier.fillMaxSize()
        )

        is WeatherUiState.Error -> ErrorScreen(
            modifier = Modifier.fillMaxSize()
        )

        is WeatherUiState.Loading -> LoadingScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun WeatherInfoScreen(
    data: List<ShortTermForecast>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = Modifier.padding(8.dp)
    ) {
        items(items = data) { item ->
            WeatherCard(weather = item)
        }
    }
}

@Composable
fun WeatherCard(
    weather: ShortTermForecast,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = getTimeString(weather.fcstTime),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.weight(1f))

            // 비 올 때만 강수량과 강수확률 표시
            if (weather.precipitationType in 1..4) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
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
                painter = getWeatherIcon(weather.precipitationType, weather.skyStatus),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "${weather.temperature}°C",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }

}

@Composable
fun getWeatherIcon(pType: Int?, skyStatus: Int?): Painter {
    /**
     * precipitationType 강수 형태 : 없음(0), 비(1), 비/눈(2), 눈(3), 소나기(4)
     * skyStatus 하늘 상태 : 맑음(1), 구름많음(3), 흐림(4)
     */
    return when (pType) {
        1 -> painterResource(R.drawable.rainy3d)
        2 -> painterResource(R.drawable.rainsnow)
        3 -> painterResource(R.drawable.snowflake)
        4 -> painterResource(R.drawable.sonagi)
        0 -> when (skyStatus) {
            1 -> painterResource(R.drawable.sunny3d)
            3 -> painterResource(R.drawable.sun)
            4 -> painterResource(R.drawable.cloud)
            else -> {
                Log.e("getWeatherIcon", "pType = 0 이지만 skyStatus 에러")
                painterResource(R.drawable.sun)
            }
        }

        else -> {
            Log.e("getWeatherIcon", "pType 에러")
            painterResource(R.drawable.sun)
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
        baseDate = "20250705",
        baseTime = "1500",
        fcstDate = "20250705",
        fcstTime = "1500",
        pop = 60,
        precipitationType = 1,
        pcp = "16.3mm",
        skyStatus = 1,
        temperature = 36,
        minTemperature = 25,
        maxTemperature = 36,
        windSpeed = 1,
    )

    WeatherCard(weather = dummyShortTermForecast)
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.loading),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
    }
}


@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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

@Composable
fun NoDataScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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