package com.farmer.weather.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.farmer.weather.ui.screens.WeatherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farmer.weather.ui.screens.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherApp() {
//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            val weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                weatherUiState = weatherViewModel.weatherUiState,
                contentPadding = innerPadding,
            )
        }
    }
}