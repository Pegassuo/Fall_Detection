package com.example.falldetection.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.falldetection.R
import kotlin.math.sqrt


class FallDetectionService: Service() {
    //Sensors
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    //Magnitudes
    private var previousAccelerationMagnitude = 0.0f
    private var previousVerticalAcceleration = 0.0f
    private lateinit var notificationManager: NotificationManager

    private val CHANNEL_ID = "fall_detection_channel"
    private val CHANGE_THRESHOLD = 10.0f
    private val MAGNITUDE_THRESHOLD = 20.0f
    private val VERTICAL_THRESHOLD = 9.0f

    //Ensures the service will restart automatically if killed by any reason
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if(sensor != null)
            accelerometer = sensor
        else {
            Log.e("FallDetectionService", "Accelerometer sensor not available")
            stopSelf()
            return
        }

        //Create notification channel to manage notifications
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Fall Detection Channel",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Fall Detection channel for foreground service notification"
            setAllowBubbles(true)
            setBypassDnd(true)
        }

        notificationManager.createNotificationChannel(channel)

        startSensorListener()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Fall Detection Running")
            .setContentText("Using sensors to detect falls")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        /*
        To run a service continuously in the background, it's often necessary
        to promote it to a foreground service, which requires displaying a
        persistent notification to the user.
         */
        startForeground(1, notification)
    }

    private val sensorEventListener = object : SensorEventListener{
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

            if (fallDetected && !FallDetectionManager.isAlertActivityRunning()) {
                triggerFallAlert()
            }

            previousVerticalAcceleration = verticalAcceleration
            previousAccelerationMagnitude = currentAccelerationMagnitude
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            //None
        }
    }

    private fun triggerFallAlert(){
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        val intent = Intent("fall_detected")
        localBroadcastManager.sendBroadcast(intent)
    }

    private fun startSensorListener(){
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun calculateMagnitude(values: FloatArray): Float {
        return sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
    }
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(sensorEventListener)
    }
}