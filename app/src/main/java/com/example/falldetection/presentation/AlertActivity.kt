package com.example.falldetection.presentation

import android.os.Bundle
import android.os.CountDownTimer
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert)

        progressBar = findViewById(R.id.progressBar)
        progressBar.progress = 100

        //Countdown timer
        val countDownTimer = object : CountDownTimer(15000, 200){
            override fun onTick(millisUntilFinished: Long) {
                val progress = ((millisUntilFinished * 100)/15000).toInt()
                progressBar.progress = progress
            }

            override fun onFinish() {
                progressBar.progress = 0
                val newFall = DataFall(
                    id = 0,
                    ubicacion = "Guayaquil, Guayas, Ecuador",
                    fecha = LocalDate.now().format(dateFormat),
                    hora = LocalTime.now().format(timeFomat)
                )
                storeData.saveData(activityContext, newFall)
                finish()
            }
        }

        countDownTimer.start()

    }
}