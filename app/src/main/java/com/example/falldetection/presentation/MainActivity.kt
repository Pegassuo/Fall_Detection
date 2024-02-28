package com.example.falldetection.presentation

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
import com.example.falldetection.R

class MainActivity : ComponentActivity() {
    private val storeData = StoreData()
    private lateinit var recyclerView: RecyclerView
    //Button
    private lateinit var sendAlertButton: Button
    private lateinit var contactsButton: Button

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
        super.onDestroy()
        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.registerReceiver(receiver, IntentFilter("fall_detected"))
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

}
