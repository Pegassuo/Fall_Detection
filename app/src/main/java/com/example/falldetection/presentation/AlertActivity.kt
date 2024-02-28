package com.example.falldetection.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.ComponentActivity
import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.MessageAttributeValue
import aws.sdk.kotlin.services.sns.model.PublishRequest
import com.example.falldetection.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class AlertActivity: ComponentActivity() {
    private lateinit var progressBar: ProgressBar
    private val storeData = StoreData()
    private val activityContext = this
    private val dateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val timeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button
    private var audioPlayer: MediaPlayer? = null
    private val fusedLocalClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.alert)

        FallDetectionManager.setAlertActivityRunning(true)
        acceptButton = findViewById(R.id.acceptButton)
        declineButton = findViewById(R.id.declineButton)
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
            releaseAudioPlayer()
            finish()
        }

    }
    override fun onDestroy() {
        releaseAudioPlayer()
        FallDetectionManager.setAlertActivityRunning(false)
        super.onDestroy()
    }
    private fun sendAlert(){
        val dataContactList = storeData.getData(this, DataContact::class.java)

        val newFall = DataFall(
            id = 0,
            latitude = 0.0,
            longitude = 0.0,
            fecha = LocalDate.now().format(dateFormat),
            hora = LocalTime.now().format(timeFormat),
            contacts = dataContactList
        )
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getLocation(newFall) { updatedNewFall ->
                storeData.saveData(activityContext, updatedNewFall)
                /* No activar hasta querer hacer pruebas con sms
                CoroutineScope(Dispatchers.IO).launch {
                    pubTextSMS("Una caída ha sido detectada ${newFall.fecha} ${newFall.hora} en la siguiente ubicación https://www.google.com/maps/search/?api=1&query=${newFall.latitude},${newFall.longitude}"
                        , "+593969432043")
                }
                 */
            }
        }else{
            storeData.saveData(activityContext, newFall)
            /* No activar hasta querer hacer pruebas con sms
            CoroutineScope(Dispatchers.IO).launch {
                pubTextSMS("Una caída ha sido detectada ${newFall.fecha} ${newFall.hora}"
                    , "+593969432043")
            }
             */
        }

        audioPlayer = MediaPlayer.create(this@AlertActivity, R.raw.alarm)
        audioPlayer?.setVolume(1.0f, 1.0f)
        audioPlayer?.isLooping = true
        audioPlayer?.start()

    }

    private fun releaseAudioPlayer(){
        audioPlayer?.stop()
        audioPlayer?.release()
        audioPlayer = null
    }


    suspend fun pubTextSMS(messageVal: String?, phoneNumberVal: String?){
        val dotenv = dotenv {
            directory = "/assets"
            filename = "env"
        }

        val stacticCredentials = StaticCredentialsProvider{
            accessKeyId = dotenv["ACCESSKEYID"]
            secretAccessKey = dotenv["SECRETACCESSKEY"]
        }

        val attributes = mutableMapOf<String, MessageAttributeValue>()

        attributes["AWS.SNS.SMS.SMSType"] = MessageAttributeValue.invoke {
            stringValue = "Promotional"
            dataType = "String"
        }

        val request = PublishRequest{
            message = messageVal
            phoneNumber = phoneNumberVal
            messageAttributes = attributes
        }

        SnsClient{
            credentialsProvider = stacticCredentials
            region = "sa-east-1"
        }.use { snsClient ->
            snsClient.publish(request)
        }
    }

    private fun getLocation(newFall: DataFall, saveCallback: (DataFall) -> Unit){
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0).build()

        val locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                if(locationResult.locations.isNotEmpty()){
                    val location = locationResult.locations[0]
                    newFall.latitude = location.latitude
                    newFall.longitude = location.longitude
                    saveCallback(newFall)
                }else{
                    saveCallback(newFall)
                }
                fusedLocalClient.removeLocationUpdates(this)
            }
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocalClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }else{
            Log.e("AlertActivity", "Location permission not granted, can't retrieve location")
        }
    }

}