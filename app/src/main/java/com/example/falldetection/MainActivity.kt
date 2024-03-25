package com.example.falldetection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.google.gson.Gson
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {
    private val storeData = StoreData()
    private lateinit var recyclerView: RecyclerView
    //Button
    private lateinit var sendAlertButton: Button
    private lateinit var contactsButton: Button

    private val mMessageClient by lazy { Wearable.getMessageClient(this) }

    private val receiver = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == "fall_detected"){
                val alertIntent = Intent(this@MainActivity, AlertActivity::class.java)
                startActivity(alertIntent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        val intent = Intent(this, FallDetectionService::class.java)
        startService(intent)

        sendAlertButton = findViewById(R.id.sendAlertBtn)
        contactsButton = findViewById(R.id.contactsBtn)
        recyclerView = findViewById(R.id.recycler_view)

        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, IntentFilter("fall_detected"))

        /* Write empty array in falls on physical watch
        val empty = mutableListOf<String>()
        storeData.saveJson(this, empty, "falls")
        storeData.saveJson(this, empty, "contacts")
         */

        mMessageClient.addListener(this)

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

    override fun onDestroy() {
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, IntentFilter("fall_detected"))
        mMessageClient.removeListener(this)
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        val dataFallList = storeData.getData(this, DataFall::class.java)
        if (dataFallList != null) {
            val adapter = ChipAdapter<DataFall>(dataFallList)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/request_fall_data") {
            Log.d("Wear", "Received request for fall data")
            val dataFallList = storeData.getData(this, DataFall::class.java)
            if (dataFallList != null) {
                sendFallDataToPhone(dataFallList)
            }
        }
    }

    private fun sendFallDataToPhone(fallList: List<DataFall>){
        val request = PutDataMapRequest.create("/fall_data")
        val jsonString = Gson().toJson(fallList)
        val requestMessage = request.asPutDataRequest()
        mMessageClient.sendMessage(requestMessage.uri.host.toString(), requestMessage.uri.path.toString(), jsonString.toByteArray(StandardCharsets.UTF_8))
    }

}
