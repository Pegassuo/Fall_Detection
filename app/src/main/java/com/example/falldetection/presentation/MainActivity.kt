package com.example.falldetection.presentation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.falldetection.R
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    //Sensors
    private lateinit var sensorManager: SensorManager
    private lateinit var acceSensor: Sensor
    //Layout
    private lateinit var mTextView: TextView
    private lateinit var mLinearLayout: LinearLayout
    //Magnitudes
    private var previousAccelerationMagnitude = 0.0f
    private var previousVerticalAcceleration = 0.0f
    //Flag
    private var wasFallDetected : Boolean = false
    val storeFall = StoreFall()

    private companion object{
        private const val CHANGE_THRESHOLD = 10.0f
        private const val MAGNITUDE_THRESHOLD = 20.0f
        private const val VERTICAL_THRESHOLD = 9.0f
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mTextView = findViewById(R.id.text_values)
        mLinearLayout = findViewById(R.id.root)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        //Check for info
        //storeFall.saveData(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        val verticalAcceleration = event.values[2]
        val currentAccelerationMagnitude = calculateMagnitude(event.values)
        val accelerationChange = currentAccelerationMagnitude - previousAccelerationMagnitude
        val verticalAccelerationChange = verticalAcceleration - previousVerticalAcceleration

        if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }

        var fallDetected = (accelerationChange > CHANGE_THRESHOLD &&
                currentAccelerationMagnitude > MAGNITUDE_THRESHOLD &&
                verticalAccelerationChange < VERTICAL_THRESHOLD)

        if (fallDetected && !wasFallDetected) {
            Log.d(TAG, "Fall detected")
        }

        previousVerticalAcceleration = verticalAcceleration
        previousAccelerationMagnitude = currentAccelerationMagnitude
        wasFallDetected = fallDetected

        mTextView.text = "X = ${event.values[0]}" +
                "\nY = ${event.values[1]}" +
                "\nZ = ${event.values[2]}" +
                "\nMagnitude = $currentAccelerationMagnitude"
    }
    private fun calculateMagnitude(values: FloatArray): Float{
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //NONE
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, acceSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
}
