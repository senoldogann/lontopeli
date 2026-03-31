package com.senoldogan.luontopeli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senoldogan.luontopeli.data.local.entity.WalkSession
import com.senoldogan.luontopeli.data.repository.WalkRepository
import com.senoldogan.luontopeli.sensor.StepCounterManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * WalkViewModel – hallitsee kävelysessioita ja sensoridatan lukemista.
 * Syllabuksen viikko 2 mukainen toteutus.
 */
@HiltViewModel
class WalkViewModel @Inject constructor(
    private val repository: WalkRepository,
    private val sensorManager: StepCounterManager
) : ViewModel() {

    // Aktiivinen sessio (null jos ei käynnissä)
    private val _activeSession = MutableStateFlow<WalkSession?>(null)
    val activeSession: StateFlow<WalkSession?> = _activeSession.asStateFlow()

    // Reaaliaikaiset sensorilukemat (vain UI-näyttöön sessiossa)
    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    private val _distance = MutableStateFlow(0f)
    val distance: StateFlow<Float> = _distance.asStateFlow()

    private val _gyroData = MutableStateFlow(floatArrayOf(0f, 0f, 0f))
    val gyroData: StateFlow<FloatArray> = _gyroData.asStateFlow()

    init {
        // Tarkista onko tietokannassa kesken jäänyttä sessiota
        viewModelScope.launch {
            repository.getActiveSession()?.let { session ->
                _activeSession.value = session
                _steps.value = session.stepCount
                _distance.value = session.distanceMeters
                startSensors()
            }
        }
    }

    fun startWalk() {
        if (_activeSession.value != null) return

        val newSession = WalkSession(
            startTime = System.currentTimeMillis(),
            isActive = true
        )

        viewModelScope.launch {
            repository.insertSession(newSession)
            _activeSession.value = newSession
            _steps.value = 0
            _distance.value = 0f
            startSensors()
        }
    }

    fun stopWalk() {
        val session = _activeSession.value ?: return
        
        viewModelScope.launch {
            val finalSession = session.copy(
                endTime = System.currentTimeMillis(),
                stepCount = _steps.value,
                distanceMeters = _distance.value,
                isActive = false
            )
            repository.updateSession(finalSession)
            _activeSession.value = null
            sensorManager.stopAll()
        }
    }

    private fun startSensors() {
        if (!sensorManager.isStepSensorAvailable()) {
            // Logiikka varasuunnitelmalle jos askelmittaria ei ole
        }

        sensorManager.startStepCounting {
            _steps.value += 1
            // Lasketaan etäisyys askeleista
            _distance.value = _steps.value * StepCounterManager.STEP_LENGTH_METERS
            
            // Päivitetään tietokantaan väliajoin tai joka askeleella
            updateSessionStats()
        }

        sensorManager.startGyroscope { x, y, z ->
            _gyroData.value = floatArrayOf(x, y, z)
        }
    }

    private fun updateSessionStats() {
        val session = _activeSession.value ?: return
        viewModelScope.launch {
            repository.updateSession(
                session.copy(
                    stepCount = _steps.value,
                    distanceMeters = _distance.value
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopAll()
    }
}
