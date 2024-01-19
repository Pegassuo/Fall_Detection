package com.example.falldetection.presentation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
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
    private lateinit var proximitySensor: Sensor
    //Layout
    private lateinit var mTextView: TextView
    private lateinit var mLinearLayout: LinearLayout
    //Magnitudes
    private var previousAccelerationMagnitude = 0.0f
    private var previousVerticalAcceleration = 0.0f
    //private var filteredAcceleration = FloatArray(3){0f}
    //Timestamp for time-based analysis
    private var lastSensorTimestamp : Long = 0L
    //Flag
    private var isFallDetected : Boolean = false
    private companion object{
        //private const val ALPHA = 0.7f
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
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        lastSensorTimestamp = System.currentTimeMillis()
        //Apply low-pass filter
        //val filteredValues = applyLowPassFilter(event.values)
        // Calculate magnitude using filtered values
        val verticalAcceleration = event.values[2]
        val currentAccelerationMagnitude = calculateMagnitude(event.values)
        val accelerationChange = currentAccelerationMagnitude - previousAccelerationMagnitude
        val verticalAccelerationChange = verticalAcceleration - previousVerticalAcceleration
        //If accuracy is unreliable do nothing
        if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }
        //A fall is detected
        if (accelerationChange > CHANGE_THRESHOLD &&
            currentAccelerationMagnitude > MAGNITUDE_THRESHOLD &&
            verticalAccelerationChange < VERTICAL_THRESHOLD) {
            //Condition to make it trigger only one time per fall
            if(!isFallDetected){
                //mLinearLayout.setBackgroundColor(Color.BLUE)
                Log.d(TAG, "Fall detected")
                //
            }
        }

        previousVerticalAcceleration = verticalAcceleration
        previousAccelerationMagnitude = currentAccelerationMagnitude
        //The flag is updated with the boolean value if a wall was/wasn't detected
        isFallDetected = (accelerationChange > CHANGE_THRESHOLD &&
                currentAccelerationMagnitude > MAGNITUDE_THRESHOLD &&
                verticalAccelerationChange < VERTICAL_THRESHOLD)

        mTextView.text = "X = ${event.values[0]}" +
                "\nY = ${event.values[1]}" +
                "\nZ = ${event.values[2]}" +
                "\nMagnitude = $currentAccelerationMagnitude"
    }
    private fun calculateMagnitude(values: FloatArray): Float{
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }

    /*
    private fun applyLowPassFilter(acceleration: FloatArray): FloatArray{
        val filteredValues = FloatArray(3)
        for(i in 0..2){
            filteredValues[i] = ALPHA * filteredValues[i] + (1- ALPHA) * acceleration[i]
        }
        //Update filteredAcceleration for the next iteration
        filteredAcceleration = filteredValues
        return filteredValues
    }
     */

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        //NONE
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
}
