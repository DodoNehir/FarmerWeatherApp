package com.farmer.weather

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.farmer.weather.ui.screens.HomeScreen
import com.farmer.weather.ui.viewmodel.WeatherUiState
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun pullToRefreshBox_showsIndicatorInNoData() {
        composeTestRule.setContent {

            var isRefreshing by remember { mutableStateOf(false) }
            val refreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {},
                modifier = Modifier.fillMaxSize()
            ) {
                HomeScreen(
                    weatherUiState = WeatherUiState.NoData,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        // Indicator 노출 여부 검사
        composeTestRule.onRoot().performTouchInput {
            swipeDown(startY = centerY, endY = bottom, durationMillis = 500)
        }
    }

}