/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.falldetection.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.falldetection.R


class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var mTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mTextView = findViewById(R.id.text_values)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }

        mTextView.text = "X = ${event.values[0]}\nY = ${event.values[1]}\nZ = ${event.values[2]}"
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
        sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL)
    }
}
