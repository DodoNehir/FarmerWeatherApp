package com.farmer.weather.ui.screens

import android.R.attr.data
import android.util.Log.w
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        modifier = Modifier.padding(16.dp)
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
    Card(modifier = modifier) {
        Column {
            Text(text = "date: " + weather.fcstDate)
            Text(text = "time: " + weather.fcstTime)
            Text(text = "degree: " + weather.temperature)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.loading),
        contentDescription = null,
        modifier = modifier
            .size(100.dp)
    )
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