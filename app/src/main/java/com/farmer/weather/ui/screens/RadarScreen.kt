package com.farmer.weather.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.farmer.weather.R
import com.farmer.weather.ui.viewmodel.RadarViewModel

@Composable
fun RadarScreen(
    radarViewModel: RadarViewModel = hiltViewModel(),
) {

    val url by radarViewModel.imageUrl.collectAsState()
    // 만약 url이 없으면?? url.isNotEmpty()로 확인을 안 하는 상태임
    RadarImage(url)

}

@Composable
fun RadarImage(
    url: String,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, panChange, _ ->
        scale *= zoomChange
        offset += panChange
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
            .clipToBounds()
            .transformable(state)
    ) {
        AsyncImage(
            model = url,
            placeholder = painterResource(R.drawable.nodata),
            contentDescription = "radar image",
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
        )

    }
}

@Preview(showBackground = true)
@Composable
fun RadarImagePreview() {
    RadarImage(url = "https://www.kma.go.kr/repositary/image/rdr/img/RDR_CMP_WRC_202508271350.png")
}
