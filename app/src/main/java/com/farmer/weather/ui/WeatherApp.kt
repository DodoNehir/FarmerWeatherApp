package com.farmer.weather.ui

import android.Manifest
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import com.farmer.weather.ui.screens.WeatherViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.farmer.weather.ui.screens.HomeScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherApp() {
//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val TAG = "WeatherApp"
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

    val weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)

    LaunchedEffect(Unit) {
        // 처음 composition 될 때 한 번만 실행됨
        locationPermissionState.launchPermissionRequest()
    }

    LaunchedEffect(locationPermissionState.status) {
        // status가 바뀔 때마다 실행됨
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG, "위치 불러오기 성공: lat: ${location.latitude}, lon: ${location.longitude}")
                    // viewModel에 위치 전달
                    weatherViewModel.setLocation(location.latitude, location.longitude)
                } else {
                    // TODO 애뮬레이터로는 실패한다. 실제 기기로 테스트하면 성공한다.
                    Log.d(TAG, "위치 불러오기 실패. 권한은 있지만 위치를 불러올 수 없습니다.")

                }
            }
        } else {
            Log.d(TAG, "위치 불러오기 실패. 권한이 없습니다.")
        }
    }

    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
                weatherUiState = weatherViewModel.weatherUiState,
                contentPadding = innerPadding,
            )
        }
    }
}