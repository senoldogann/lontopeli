package com.senoldogan.luontopeli.viewmodel

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.senoldogan.luontopeli.data.local.dao.NatureSpotDao
import com.senoldogan.luontopeli.data.local.AppDatabase
import com.senoldogan.luontopeli.data.local.entity.NatureSpot
import com.senoldogan.luontopeli.location.LocationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MapViewModel – hallitsee LocationManager-instanssia ja lataa luontokohteet tietokannasta.
 * Syllabuksen viikko 3 mukainen toteutus.
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    application: Application,
    private val locationManager: LocationManager,
    private val natureSpotDao: NatureSpotDao
) : AndroidViewModel(application) {

    // Delegoidaan StateFlowt suoraan LocationManagerilta
    val routePoints: StateFlow<List<GeoPoint>> = locationManager.routePoints
    val currentLocation: StateFlow<Location?> = locationManager.currentLocation

    // Luontokohteet kartalla
    private val _natureSpots = MutableStateFlow<List<NatureSpot>>(emptyList())
    val natureSpots: StateFlow<List<NatureSpot>> = _natureSpots.asStateFlow()

    init {
        // Lataa luontokohteet heti kun ViewModel luodaan
        loadNatureSpots()
    }

    fun startTracking() = locationManager.startTracking()
    fun stopTracking() = locationManager.stopTracking()
    fun resetRoute() = locationManager.resetRoute()

    private fun loadNatureSpots() {
        viewModelScope.launch {
            // Kuuntelee Room-muutoksia reaaliajassa Flow:n avulla
            natureSpotDao.getSpotsWithLocation().collect { spots: List<NatureSpot> ->
                _natureSpots.value = spots
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Pysäytä sijainnin seuranta kun ViewModel tuhotaan
        locationManager.stopTracking()
    }
}

// Apufunktio: muuntaa Long-aikaleiman luettavaksi merkkijonoksi
fun Long.toFormattedDate(): String {
    val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(this))
}
