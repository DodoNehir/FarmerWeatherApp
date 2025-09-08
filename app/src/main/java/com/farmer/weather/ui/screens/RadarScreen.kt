package com.farmer.weather.ui.screens

import android.R.attr.top
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.farmer.weather.R
import com.farmer.weather.ui.viewmodel.RadarViewModel
import kotlinx.coroutines.launch
import net.engawapg.lib.zoomable.ScrollGesturePropagation
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun RadarScreen(
    modifier: Modifier = Modifier,
    radarViewModel: RadarViewModel = hiltViewModel(),
) {

    val url by radarViewModel.radarUrlFlow().collectAsState(initial = "")
    // 만약 url이 없으면?? url.isNotEmpty()로 확인을 안 하는 상태임
    RadarImage(url, modifier)

}

@Composable
fun RadarImage(
    url: String,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    if (url.isEmpty()) {
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.displayLarge
        )
    } else {

        val zoomState = rememberZoomState(maxScale = 6f, initialScale = 2f)

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(url)
                .build(),
            contentDescription = "radar image",
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                zoomState.setContentSize(state.painter.intrinsicSize)
            },
            modifier = Modifier
                .fillMaxSize()
                .zoomable(
                    zoomState = zoomState,
                    onDoubleTap = { position ->
                        val targetScale = when {
                            zoomState.scale < 3f -> 3f
                            zoomState.scale < 6f -> 6f
                            else -> 6f
                        }
                        coroutineScope.launch {
                            zoomState.changeScale(targetScale, position)
                        }
                    },
                    scrollGesturePropagation = ScrollGesturePropagation.NotZoomed
                )
        )

    }

}

@Preview(showBackground = true)
@Composable
fun RadarImagePreview() {
    RadarImage(url = "https://www.kma.go.kr/repositary/image/rdr/img/RDR_CMP_WRC_202508271350.png")
}
