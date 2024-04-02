package com.example.falldetection

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {
    private lateinit var fallCounts: TextView
    private lateinit var fallList: List<DataFall>
    private lateinit var fallAdapter: FallDataAdapter
    private val mMessageClient by lazy { Wearable.getMessageClient(this) }
    private val FALL_DATA_PATH = "fall_data.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        fallCounts = findViewById(R.id.fall_counts)

        mMessageClient.addListener(this)

        fallList = loadFallDataFromLocalFile()
        fallCounts.text = "${fallList.size} registradas"


        val fallRecyclerView = findViewById<RecyclerView>(R.id.fall_list)
        fallAdapter = FallDataAdapter(fallList)
        fallRecyclerView.adapter = fallAdapter
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/fall_data") {
            val message = String(messageEvent.data, StandardCharsets.UTF_8)
            val dataList = Gson().fromJson(message, Array<DataFall>::class.java).toList()
            saveFallDataToLocalFile(dataList)
            fallList = dataList
            fallAdapter.updateData(fallList)
        }
    }

    private fun requestFallDataFromWear(){
        val request = PutDataMapRequest.create("/request_fall_data")
        val requestMessage = request.asPutDataRequest()
        mMessageClient.sendMessage(requestMessage.uri.host.toString(), requestMessage.uri.path.toString(), requestMessage.data)
    }
    private fun saveFallDataToLocalFile(fallList: List<DataFall>){
        try{
            val file = File(filesDir, FALL_DATA_PATH)
            val outputStream = FileOutputStream(file)
            val data = Gson().toJson(fallList).toByteArray()
            outputStream.write(data)
            outputStream.close()
        }catch(e: Exception){
            Log.e("MainActivity", "Error saving fall data to file: ", e)
        }

    }

    private fun loadFallDataFromLocalFile(): List<DataFall>{
        val file = File(filesDir, FALL_DATA_PATH)
        if(file.exists()){
            val jsonString = file.readText()
            return try{
                Gson().fromJson(jsonString, Array<DataFall>::class.java).toList()
            }catch(e: Exception){
                Log.e("MainActiviy", "Error parsing fall data from file: ", e)
                emptyList()
            }
        }else {
            return emptyList()
        }
    }
    override fun onResume() {
        requestFallDataFromWear()
        super.onResume()
    }
    override fun onDestroy() {
        mMessageClient.removeListener(this)
        super.onDestroy()
    }
}
