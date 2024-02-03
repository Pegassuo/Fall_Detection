package com.example.falldetection.presentation

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import com.example.falldetection.R

class AlertActivity: ComponentActivity() {
    private lateinit var progressBar: ProgressBar
    val storeFall = StoreFall()
    val activityContext = this
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
                storeFall.saveData(activityContext)
                finish()
            }
        }

        countDownTimer.start()

    }
}