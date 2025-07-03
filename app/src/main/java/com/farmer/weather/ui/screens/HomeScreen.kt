package com.farmer.weather.ui.screens

import android.R.attr.data
import android.util.Log.w
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

        is WeatherUiState.NoData -> ErrorScreen()
        is WeatherUiState.Error -> ErrorScreen()
        is WeatherUiState.Loading -> LoadingScreen()
    }
}

@Composable
fun WeatherInfoScreen(
    data: List<ShortTermForecast>,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = Modifier.padding(horizontal = 4.dp),
    ) {
        items(items = data) { item ->
            WeatherCard(
                weather = item,
                modifier = modifier
                    .padding(4.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.5f)
            )

        }
    }
}

@Composable
fun WeatherCard(
    weather: ShortTermForecast,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column {
            Text(text = "date: " + weather.fcstDate)
            Text(text = "time: " + weather.fcstTime)
            Text(text = "degree: " + weather.temperature)
        }
    }

}

@Preview
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.loading),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .size(200.dp)
    )
}

@Composable
fun ErrorScreen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.cancel),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .size(200.dp)
    )
}