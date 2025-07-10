package com.farmer.weather.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
    modifier: Modifier = Modifier,
    weatherUiState: WeatherUiState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    // TODO 날씨 데이터가 현재 시각 이전의 정보는 표시하지 않도록 수정하기
    when (weatherUiState) {
        is WeatherUiState.Success -> WeatherInfoScreen(
            modifier = modifier,
            data = weatherUiState.weatherList,
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
    data: List<ShortTermForecast>,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    Column(
        modifier = modifier.padding(contentPadding)
    ) {
        CurrentHighlightCard(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            weather = data.first()
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
        ) {
            items(items = data.drop(1)) { item ->
                WeatherCard(weather = item)
            }
        }

    }
}

@Composable
fun CurrentHighlightCard(
    modifier: Modifier = Modifier,
    weather: ShortTermForecast
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1.5f)) {
                // TODO 값이 null일 때 그냥 null 로 표기되는 문제
                // TODO 지나간 시간들을 표시하지 않기 위해서 : DB에 우선 저장 후
                //  불러올 때 현재 시간것부터 불러오도록 하면 될 것 같다.
                Text(
                    text = "${weather.temperature}°",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = getWeatherIconString(weather.precipitationType, weather.skyStatus),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                // TODO 최저기온은 당일 6시, 최고기온은 15시 예보에 있음
                //  나중에 DB에 저장을 위해 당일 5시 발표 자료를 조회요청 해야 할 듯
                //  5시 20분 이후에 하는 요청은 오늘 날짜 5시 발표 자료를 가져온다.
                //  00시부터 5시 20분 사이에 UI를 그려야 한다면 그 전날의 5시 걸로 요청해서 가져와야 함
                Text(
                    text = "최고 ${weather.maxTemperature}° / 최저 ${weather.minTemperature}°"
                )
                Text(
                    text = "강수확률 ${weather.pop}%  풍속 ${weather.windSpeed}m/s"
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TODO lottie 이미지?? 날씨에 따라 움직이면 좋겠지만 지금은 3d image
                Image(
                    painter = getWeatherIcon(weather.precipitationType, weather.skyStatus),
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
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
            )
        }
    }

}

@Composable
fun getWeatherIconString(pType: Int?, skyStatus: Int?): String {
    return when (pType) {
        1 -> stringResource(R.string.rain)
        2 -> stringResource(R.string.sleet)
        3 -> stringResource(R.string.snow)
        4 -> stringResource(R.string.showers)
        0 -> when (skyStatus) {
            1 -> stringResource(R.string.clear)
            3 -> stringResource(R.string.cloudy)
            4 -> stringResource(R.string.overcast)
            else -> {
                Log.e("getWeatherIconString", "pType = 0 이지만 skyStatus 에러")
                " "
            }
        }

        else -> {
            Log.e("getWeatherIconString", "pType 에러")
            " "
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
        windSpeed = 1.1,
    )

//    WeatherCard(weather = dummyShortTermForecast)
    CurrentHighlightCard(weather = dummyShortTermForecast)
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