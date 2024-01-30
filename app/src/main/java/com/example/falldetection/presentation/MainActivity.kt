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
import android.view.ViewGroup
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
    val storeFall = StoreFall()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ChipAdapter

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

        val dataFallList = storeFall.getData(this) as? MutableList<DataFall>

        recyclerView = findViewById(R.id.recycler_view)
        //Load history of falls
        if (dataFallList != null){
            val adapter = ChipAdapter(dataFallList)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
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

    fun loadData(container: ViewGroup, dataFallList: MutableList<DataFall>){
        adapter = ChipAdapter(dataFallList)
        recyclerView.adapter = adapter
    }
}
