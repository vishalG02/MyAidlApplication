package com.vis.aidl.orientation

import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.aidl.sensor.orientation.AidlOrientationInterface

private const val SENSOR_DELAY_MICROS = 20 * 1000 // 8ms

class OrientationService : LifecycleService(), SensorEventListener {

    companion object {
        val sensorData = MutableLiveData<FloatArray>()
    }

    private var sensorManager: SensorManager? = null
    private var rotationSensor: Sensor? = null

    private fun createSensorManager() {
        if (sensorManager == null) {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            rotationSensor?.let {
                addRotationSensorListener()
            }
        }
    }

    private fun addRotationSensorListener() {
        sensorManager?.registerListener(
            this,
            rotationSensor,
            SENSOR_DELAY_MICROS
        )
    }

    private val myBinder: AidlOrientationInterface.Stub = object : AidlOrientationInterface.Stub() {
        override fun orientation(): String {
            createSensorManager()
            return sensorData.value?.contentToString() ?: "No Data"
        }
    }


    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return myBinder
    }

    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent?.let {
            if (it.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                sensorData.value = it.values
            }

        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}
