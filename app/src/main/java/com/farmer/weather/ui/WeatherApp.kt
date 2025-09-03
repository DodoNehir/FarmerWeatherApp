package com.farmer.weather.ui

import android.Manifest
import android.R.attr.top
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.farmer.weather.ui.screens.HomeScreen
import com.farmer.weather.ui.screens.RadarScreen
import com.farmer.weather.ui.screens.WeatherInfoScreen
import com.farmer.weather.ui.viewmodel.WeatherUiState
import com.farmer.weather.ui.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import kotlin.properties.Delegates

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherApp(
    weatherViewModel: WeatherViewModel = hiltViewModel()
) {
//    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val TAG = "WeatherApp"
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val locationPermissionState = rememberPermissionState(
        permission = Manifest.permission.ACCESS_FINE_LOCATION
    )

//    val weatherViewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.Factory)

    LaunchedEffect(Unit) {
        // 처음 composition 될 때 한 번만 실행됨
        locationPermissionState.launchPermissionRequest()
    }

    // status가 바뀔 때마다 실행됨
    LaunchedEffect(locationPermissionState.status) {
        if (locationPermissionState.status.isGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Log.d(TAG, "위치 불러오기 성공: lat: ${location.latitude}, lon: ${location.longitude}")
                    // viewModel에 위치 전달
                    weatherViewModel.startLoadWeather(location.latitude, location.longitude)
                } else {
                    var defaultLat by Delegates.notNull<Double>()
                    var defaultLon by Delegates.notNull<Double>()

                    if (isRunningOnEmulator()) {
                        Log.d(TAG, "애뮬레이터이기 때문에 lastlocation 을 임의로 지정합니다. (고산2동)")
                        defaultLat = 35.8403
                        defaultLon = 128.6973
                    } else {
                        Log.d(
                            TAG, "실제 기기이고 권한도 있지만 마지막 위치를 불러오지 못했습니다. " +
                                    "lastlocation을 세종시의 농림축산식품부로 설정합니다."
                        )
                        defaultLat = 36.5050
                        defaultLon = 127.2655
                    }

                    weatherViewModel.startLoadWeather(defaultLat, defaultLon)
                }
            }
        } else {
            Log.d(TAG, "위치 불러오기 실패. 권한이 없습니다.")
        }
    }

//    LaunchedEffect(weatherViewModel.weatherUiState) {
//    }

    val onRefresh: () -> Unit = {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d(TAG, "refresh weather")
                weatherViewModel.refreshWeather(location.latitude, location.longitude)
            } else {
                Log.d(TAG, "refresh weather : 36.5050, 127.2655")
                weatherViewModel.refreshWeather(36.5050, 127.2655)
            }
        }
    }
    val isRefreshing = weatherViewModel.isRefreshing
    val navController = rememberNavController()
    val startDestination = WeatherScreen.INFO
    var selectedDestination by rememberSaveable { mutableIntStateOf(startDestination.ordinal) }

    Scaffold(
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                PrimaryTabRow(
                    selectedTabIndex = selectedDestination,
                    modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
                ) {
                    WeatherScreen.entries.forEachIndexed { index, destination ->
                        Tab(
                            selected = selectedDestination == index,
                            onClick = {
                                navController.navigate(route = destination.route)
                                selectedDestination = index
                            },
                            text = {
                                Text(
                                    text = destination.label,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            })
                    }
                }
                PullToRefreshBox(
                    modifier = Modifier.fillMaxSize(),
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                ) {
                    HomeScreen(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize(),
                        weatherUiState = weatherViewModel.weatherUiState,
                    )
                }

            }

        }
    }
}

fun isRunningOnEmulator(): Boolean {
    return Build.FINGERPRINT.contains("generic")
            || Build.MODEL.contains("Emulator")
            || Build.BRAND.contains("generic")
            || Build.DEVICE.contains("generic")
            || Build.HARDWARE.contains("ranchu") // 신형 에뮬
            || Build.HARDWARE.contains("goldfish") // 구형 에뮬
}

enum class WeatherScreen(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    // TODO Icon 찾기
    INFO("Info", "날씨", Icons.Default.Info, "시간대별 날씨"),
    RADAR("Radar", "레이더", Icons.Default.Place, "레이더 분포도 조회")
}