package com.senoldogan.luontopeli.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

@Singleton
class StepCounterManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // SensorManager on Android-järjestelmäpalvelu sensorien käyttöön
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Haetaan STEP_DETECTOR-sensori (tapahtuma per askel)
    // TYPE_STEP_COUNTER olisi kumulatiivinen (ei nollaudu sovelluskohtaisesti)
    private val stepSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    // Gyroskooppisensori kääntöliikkeen tunnistukseen
    private val gyroSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    // Kiihtyvyysanturi varalaitteille (simulaattori)
    private val accelSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var stepListener: SensorEventListener? = null
    private var gyroListener: SensorEventListener? = null
    private var accelListener: SensorEventListener? = null

    // Muuttujat askeleen tunnistukseen kiihtyvyysanturilla
    private var lastMagnitude = 0f
    private val stepThreshold = 12f // Kynnysarvo askeleelle (G-voima)
    private var lastStepTime = 0L

    // Aloita askelmittaus
    fun startStepCounting(onStep: () -> Unit) {
        if (stepSensor != null) {
            // Käytetään virallista askelmittaria jos saatavilla
            stepListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                        onStep()
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }
            sensorManager.registerListener(stepListener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
        } else {
            // FALLBACK: Käytetään kiihtyvyysanturia (simulaattori)
            accelListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                        val x = event.values[0]
                        val y = event.values[1]
                        val z = event.values[2]
                        val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
                        
                        val currentTime = System.currentTimeMillis()
                        // Tunnista "piikki" kiihtyvyydessä (liike ylittää kynnyksen)
                        if (magnitude > stepThreshold && (currentTime - lastStepTime) > 300) {
                            onStep()
                            lastStepTime = currentTime
                        }
                        lastMagnitude = magnitude
                    }
                }
                override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
            }
            sensorManager.registerListener(accelListener, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    // Lopeta askelmittaus ja vapauta resurssit
    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }

    // Aloita gyroskooppidatan lukeminen
    // onRotation(x, y, z) – rad/s kullakin akselilla
    fun startGyroscope(onRotation: (Float, Float, Float) -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    // values[0] = pyöriminen x-akselin ympäri (pitch)
                    // values[1] = pyöriminen y-akselin ympäri (roll)
                    // values[2] = pyöriminen z-akselin ympäri (yaw)
                    onRotation(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }

        gyroSensor?.let {
            sensorManager.registerListener(
                gyroListener,
                it,
                SensorManager.SENSOR_DELAY_GAME  // Tiheämpi päivitysväli pelitoiminnoille
            )
        }
    }

    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    // Vapauta kaikki sensorit kerralla
    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
        accelListener?.let { sensorManager.unregisterListener(it) }
        accelListener = null
    }

    // Tarkista tukeeko laite askelmittaria
    fun isStepSensorAvailable(): Boolean = stepSensor != null

    companion object {
        // Askelpituuden arviointi (keskiarvot):
        // Mies: ~0.78 m/askel
        // Nainen: ~0.70 m/askel
        // Yksinkertainen arvio: 0.74 m/askel
        const val STEP_LENGTH_METERS = 0.74f
    }
}
