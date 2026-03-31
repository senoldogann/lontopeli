package com.senoldogan.luontopeli.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import org.osmdroid.util.GeoPoint               // GeoPoint on osmdroid-kirjaston paikkatyyppi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Androidin oma paikannuspalvelu
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager

    // Nykyinen sijainti (null ennen ensimmäistä päivitystä)
    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

    // Reittipisteet GeoPoint-listana — osmdroid käyttää GeoPoint-tyyppiä
    private val _routePoints = MutableStateFlow<List<GeoPoint>>(emptyList())
    val routePoints: StateFlow<List<GeoPoint>> = _routePoints.asStateFlow()

    // Kuuntelija joka reagoi sijainnin muutoksiin
    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            _currentLocation.value = location
            // Lisää uusi piste reittilistaan
            val newPoint = GeoPoint(location.latitude, location.longitude)
            _routePoints.value = _routePoints.value + newPoint
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    @SuppressLint("MissingPermission")
    fun startTracking() {
        try {
            // Yritä GPS ensin — tarkempi mutta kuluttaa enemmän akkua
            // Jos GPS ei käytössä, käytä verkkopaikannusta varasuunnitelmana
            val provider = when {
                locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ->
                    android.location.LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER) ->
                    android.location.LocationManager.NETWORK_PROVIDER
                else -> return  // Ei saatavilla olevaa paikannusta
            }

            locationManager.requestLocationUpdates(
                provider,
                5000L,   // Minimiväli: 5 sekuntia
                10f,     // Minimietäisyys: 10 metriä
                locationListener
            )
        } catch (_: SecurityException) {
            // Lupa ei vielä myönnetty — ei toimenpiteitä
        }
    }

    fun stopTracking() {
        locationManager.removeUpdates(locationListener)
    }

    fun resetRoute() {
        _routePoints.value = emptyList()
    }

    // Laskee reitin kokonaispituuden GPS-pisteiden väleistä
    fun calculateTotalDistance(): Float {
        val points = _routePoints.value
        if (points.size < 2) return 0f

        var total = 0f
        for (i in 0 until points.size - 1) {
            val results = FloatArray(1)
            // Location.distanceBetween laskee tarkan etäisyyden koordinaattien välillä
            Location.distanceBetween(
                points[i].latitude, points[i].longitude,
                points[i + 1].latitude, points[i + 1].longitude,
                results
            )
            total += results[0]
        }
        return total
    }
}
