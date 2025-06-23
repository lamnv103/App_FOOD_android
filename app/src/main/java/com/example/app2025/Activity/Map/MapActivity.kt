package com.example.app2025.Activity.Map

import LocationModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.view.WindowCompat
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Lấy dữ liệu từ Intent một cách an toàn
        val location = intent.getSerializableExtra("item") as? LocationModel

        setContent {
            val latlng = if (location != null) {
                LatLng(location.latitude, location.longitude)
            } else {
                // Default location (Hanoi) if no location is provided
                LatLng(21.0285, 105.8542)
            }
            MapScreen(latlng, location ?: LocationModel("Cửa Hàng Văn Lâm", 21.0285, 105.8542))
        }
    }
}

@Composable
fun MapScreen(latlng: LatLng, item: LocationModel) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(latlng, 15f)
    }

    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
        val (map, detail) = createRefs()

        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Green)
                .constrainAs(map) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                },
            cameraPositionState = cameraPositionState
        ) {
            Marker(
                state = MarkerState(position = latlng),
                title = item.name,
                snippet = "Marker in ${item.name}"
            )
        }

        // Fixed dialog box at the bottom of the screen
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
                .padding(16.dp)
                .constrainAs(detail) {
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        ) {
            Text(
                text = "Cửa Hàng Văn Lâm",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun ComponentActivity.enableEdgeToEdge() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
}
