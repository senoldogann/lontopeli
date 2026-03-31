package com.senoldogan.luontopeli.ui.map

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.senoldogan.luontopeli.viewmodel.MapViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsDisplay
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateToCamera: () -> Unit
) {
    val context = LocalContext.current
    val routePoints by viewModel.routePoints.collectAsState()
    val currentLocation by viewModel.currentLocation.collectAsState()
    val natureSpots by viewModel.natureSpots.collectAsState()

    // Luvat (Accompanist Permissions)
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Alusta osmdroid-konfiguraatio (tarvitaan välimuistia varten)
    Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))

    Scaffold(
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Kamera-painike (Vk 4)
                FloatingActionButton(onClick = onNavigateToCamera) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
                }
                // Sijainti-painike
                FloatingActionButton(onClick = { viewModel.startTracking() }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Start Tracking")
                }
            }
        }
    ) { padding ->
        if (permissionState.allPermissionsGranted) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.ALWAYS)
                        controller.setZoom(15.0)
                    }
                },
                update = { mapView ->
                    // 1. Tyhjennä vanhat overlayt (paitsi pohjakartta)
                    mapView.overlays.clear()

                    // 2. Piirrä reitti (Polyline)
                    if (routePoints.size >= 2) {
                        val polyline = Polyline().apply {
                            setPoints(routePoints)
                            outlinePaint.color = android.graphics.Color.BLUE
                            outlinePaint.strokeWidth = 5f
                        }
                        mapView.overlays.add(polyline)
                    }

                    // 3. Lisää luontokohteet (Markerit)
                    natureSpots.forEach { spot ->
                        val marker = Marker(mapView).apply {
                            position = GeoPoint(spot.latitude, spot.longitude)
                            title = spot.name
                            snippet = "Tunnistettu: ${spot.plantLabel ?: "Ei tiedossa"}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(marker)
                    }

                    // 4. Päivitä nykyinen sijainti ja keskitä kartta
                    currentLocation?.let { loc ->
                        val point = GeoPoint(loc.latitude, loc.longitude)
                        mapView.controller.animateTo(point)
                        
                        val userMarker = Marker(mapView).apply {
                            position = point
                            title = "Olet tässä"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        }
                        mapView.overlays.add(userMarker)
                    }

                    mapView.invalidate() // Pakota uudelleenpiirto
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            // Lupa-näkymä jos lupia ei ole myönnetty
            PermissionRequestScreen { permissionState.launchMultiplePermissionRequest() }
        }
    }

    // Pysäytä seuranta kun poistutaan näkymästä (tai hallitse ViewModelissa)
    DisposableEffect(Unit) {
        onDispose {
            // viewModel.stopTracking() // Valinnainen, riippuen halutaanko taustaseurantaa
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequestPermission: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sijaintilupa tarvitaan kartan käyttämiseen")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequestPermission) {
                Text("Myönnä luvat")
            }
        }
    }
}
