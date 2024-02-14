package com.example.falldetection.presentation

import android.content.Context
import android.location.LocationManager
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import com.example.falldetection.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlertActivity: ComponentActivity() {
    private lateinit var progressBar: ProgressBar
    val storeData = StoreData()
    val activityContext = this
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFomat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button
    private var locationManager: LocationManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert)

        FallDetectionManager.setAlertActivityRunning(true)
        acceptButton = findViewById(R.id.acceptButton)
        declineButton = findViewById(R.id.declineButton)
        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = 100
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

        //Countdown timer
        val countDownTimer = object : CountDownTimer(15000, 200){
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((millisUntilFinished * 100)/15000).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                progressBar.progress = 0
                acceptButton.isEnabled = false
                sendAlert()
            }
        }

        countDownTimer.start()

        acceptButton.setOnClickListener{
            countDownTimer.cancel()
            acceptButton.isEnabled = false
            sendAlert()
        }

        declineButton.setOnClickListener{
            countDownTimer.cancel()
            finish()
        }

    }

    override fun onDestroy() {
        FallDetectionManager.setAlertActivityRunning(false)
        super.onDestroy()
    }

    private fun sendAlert(){
        val newFall = DataFall(
            id = 0,
            ubicacion = "Guayaquil, Guayas, Ecuador",
            fecha = LocalDate.now().format(dateFormat),
            hora = LocalTime.now().format(timeFomat)
        )
        storeData.saveData(activityContext, newFall)
    }
}