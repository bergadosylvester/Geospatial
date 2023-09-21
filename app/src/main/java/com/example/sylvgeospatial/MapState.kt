package com.example.sylvgeospatial

import android.location.Location
import com.example.sylvgeospatial.clusters.ZoneClusterItem


data class MapState(
    val lastKnownLocation: Location?,
    val clusterItems: List<ZoneClusterItem>,
)
