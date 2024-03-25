package com.example.falldetection

object FallDetectionManager {
    private var isAlertActivityRunning = false

    fun setAlertActivityRunning(isRunning : Boolean){
        isAlertActivityRunning = isRunning
    }

    fun isAlertActivityRunning(): Boolean{
        return isAlertActivityRunning
    }
}