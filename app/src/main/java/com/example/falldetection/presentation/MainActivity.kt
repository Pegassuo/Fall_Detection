package com.example.falldetection.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.falldetection.R
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {
    //Sensors
    private lateinit var sensorManager: SensorManager
    private lateinit var acceSensor: Sensor
    //Magnitudes
    private var previousAccelerationMagnitude = 0.0f
    private var previousVerticalAcceleration = 0.0f
    //Flag
    private var wasFallDetected : Boolean = false
    private val storeData = StoreData()
    private lateinit var recyclerView: RecyclerView
    //Button
    private lateinit var sendAlertButton: Button
    private lateinit var contactsButton: Button

    private companion object{
        private const val CHANGE_THRESHOLD = 10.0f
        private const val MAGNITUDE_THRESHOLD = 20.0f
        private const val VERTICAL_THRESHOLD = 9.0f
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        acceSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        sendAlertButton = findViewById(R.id.sendAlertBtn)
        contactsButton = findViewById(R.id.contactsBtn)
        recyclerView = findViewById(R.id.recycler_view)

        val dataFallList = storeData.getData(this, DataFall::class.java)

        //Load history of falls
        if (dataFallList != null){
            val adapter = ChipAdapter<DataFall>(dataFallList)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }

        sendAlertButton.setOnClickListener{
            val intent = Intent(this, AlertActivity::class.java)
            startActivity(intent)
        }

        contactsButton.setOnClickListener{
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent) {
        if(event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE){
            return;
        }

        val verticalAcceleration = event.values[2]
        val currentAccelerationMagnitude = calculateMagnitude(event.values)
        val accelerationChange = currentAccelerationMagnitude - previousAccelerationMagnitude
        val verticalAccelerationChange = verticalAcceleration - previousVerticalAcceleration

        val fallDetected = (accelerationChange > CHANGE_THRESHOLD &&
                currentAccelerationMagnitude > MAGNITUDE_THRESHOLD &&
                verticalAccelerationChange < VERTICAL_THRESHOLD)

        if (fallDetected && !wasFallDetected) {
            val intent = Intent(this, AlertActivity::class.java)
            startActivity(intent)
        }

        previousVerticalAcceleration = verticalAcceleration
        previousAccelerationMagnitude = currentAccelerationMagnitude
        wasFallDetected = fallDetected
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
        val dataFallList = storeData.getData(this, DataFall::class.java)
        if (dataFallList != null) {
            val adapter = ChipAdapter<DataFall>(dataFallList)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

}
