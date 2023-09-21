package com.example.sylvgeospatial.compose


import android.content.Context
import android.graphics.Color
import android.location.Location
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sylvgeospatial.MapState
import com.example.sylvgeospatial.clusters.ZoneClusterManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch


var addMarker = false
var addedMarker: Marker? = null
private val markers = mutableListOf<Marker>()
var addedPolyline: Polyline? = null
private val polylines = mutableListOf<Polyline>()
val polylineOptions = PolylineOptions()

val markerClickListener = GoogleMap.OnMarkerClickListener { marker ->
    if (marker == addedMarker) {
        true
    } else {
        false
    }
}
@Composable
fun MapScreen(
    state: MapState,
    setupClusterManager: (Context, GoogleMap) -> ZoneClusterManager,
    calculateZoneViewCenter: () -> LatLngBounds,
) {

    var fabIcon by remember { mutableStateOf(Icons.Default.Place) }

    val mapProperties = MapProperties(

        isMyLocationEnabled = state.lastKnownLocation != null,
    )
    val cameraPositionState = rememberCameraPositionState()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            properties = mapProperties,
            cameraPositionState = cameraPositionState
        ) {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            MapEffect(state.clusterItems) { map ->
                map.clear()
                if (state.clusterItems.isNotEmpty()) {

                    //val clusterManager = setupClusterManager(context, map)

                    map.setOnMarkerClickListener(markerClickListener)

                    map.setOnMapLoadedCallback {
                        if (state.clusterItems.isNotEmpty()) {
                            scope.launch {
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngBounds(
                                        calculateZoneViewCenter(),
                                        0
                                    ),
                                )
                            }
                        }
                    }

                    map.setOnMapClickListener { latLng ->

                        if(addMarker == true){
                            val random = java.util.Random()
                            val randomColor = random.nextInt(360)
                            val markerIcon = BitmapDescriptorFactory.defaultMarker(randomColor.toFloat())
                            addedMarker = map.addMarker(
                                MarkerOptions()
                                    .position(latLng)
                                    .title("Marker")
                                    .snippet(""+latLng)
                                    .icon(markerIcon)
                            )
                            markers.add(addedMarker!!)

                            polylineOptions
                                .add(latLng)
                                .add(latLng)
                                .color(Color.RED)
                                .width(5f)
                            addedPolyline = map.addPolyline(polylineOptions)
                            polylines.add(addedPolyline!!)
                        }

                    }

                }
            }

        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom
    ) {

        FloatingActionButton(
            onClick = {

                addMarker = !addMarker

                fabIcon = if (addMarker) Icons.Default.Add else Icons.Default.Place
            },
            modifier = Modifier.padding(16.dp)
        ) {

            Icon(
                imageVector = fabIcon,
                contentDescription = if (addMarker) "Add Marker" else "Remove Marker"
            )
        }
        FloatingActionButton(
            onClick = {

                if (markers.size > 0) {
                    markers.last().remove()
                    markers.removeLast()
                }
            },
            modifier = Modifier
                .padding(16.dp)
        ) {

            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove Marker"
            )
        }
    }

}

private suspend fun CameraPositionState.centerOnLocation(
    location: Location
) = animate(
    update = CameraUpdateFactory.newLatLngZoom(
        LatLng(location.latitude, location.longitude),
        15f
    ),
)


