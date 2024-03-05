package com.example.falldetectionphone

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wearable.*
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {
    private lateinit var fallList: List<DataFall>
    private lateinit var fallAdapter: FallDataAdapter
    private lateinit var mDataClient: DataClient
    private val FALL_DATA_PATH = "fall_data.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        mDataClient = Wearable.getDataClient(this)
        mDataClient.addListener(this)

        //Not tested yet
        requestFallDataFromWear()
        //fallList = loadFallDataFromLocalFile()

        fallList = createInitialFallList()


        val fallRecyclerView = findViewById<RecyclerView>(R.id.fall_list)
        fallAdapter = FallDataAdapter(fallList)
        fallRecyclerView.adapter = fallAdapter
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for(event in dataEvents){
            if(event.type == DataEvent.TYPE_CHANGED){
                val dataItem = event.dataItem
                if(dataItem.uri.path == "/fall_data"){
                    val fallList = extractFallDataFromDataItem(dataItem)
                    saveFallDataToLocalFile(fallList)
                    fallAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun requestFallDataFromWear(){
        val putDataMapReq = PutDataMapRequest.create("/request_fall_data")
        val putDataReq = putDataMapReq.asPutDataRequest()
        mDataClient.putDataItem(putDataReq)
    }

    private fun extractFallDataFromDataItem(dataItem: DataItem): List<DataFall>{
        val dataMapItem = DataMapItem.fromDataItem(dataItem)
        val dataMap = dataMapItem.dataMap
        val dataString = dataMap.getString("fall_data") ?: ""

        try{
            val jsonArray = JSONArray(dataString)
            val fallList = mutableListOf<DataFall>()
            for(i in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(i)
                val id = jsonObject.getInt("id")
                val fecha = jsonObject.getString("fecha") ?: ""
                val hora = jsonObject.getString("hora") ?: ""
                val latitude = jsonObject.getDouble("latitude")
                val longitude = jsonObject.getDouble("longitude")
                val contacts = parseContactsFromJsonObject(jsonObject)

                fallList.add(DataFall(id, latitude, longitude, fecha, hora, contacts))
            }
            return fallList
        }catch (e: Exception) {
            Log.e("PhoneActivity", "Error parsing fall data: ", e)
            return emptyList()
        }
    }

    private fun parseContactsFromJsonObject(jsonObject: JSONObject): MutableList<DataContact>{
        val contacts = mutableListOf<DataContact>()
        if(!jsonObject.has("contacts")){
            return contacts
        }
        val contactArray = jsonObject.getJSONArray("contacts")
        for(i in 0 until contactArray.length()){
            val contactObject = contactArray.getJSONObject(i)
            val name = contactObject.getString("name") ?: ""
            val number = contactObject.getString("number") ?: ""
            contacts.add(DataContact(name, number))
        }
        return contacts
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

    private fun createInitialFallList(): List<DataFall> {
        val jsonString = """
        [{"contacts":[{"name":"Carlos","number":"0969432043"}],"fecha":"04-03-2024","hora":"10:43 PM","id":1,"latitude":37.4219983,"longitude":-122.084},
         {"contacts":[{"name":"Carlos","number":"0969432043"},{"name":"Luis","number":"0969432044"}],"fecha":"04-03-2024","hora":"10:44 PM","id":2,"latitude":37.4219983,"longitude":-122.084}]
        """
        val jsonArray = JSONArray(jsonString)
        val fallList = mutableListOf<DataFall>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getInt("id")
            val fecha = jsonObject.getString("fecha") ?: ""
            val hora = jsonObject.getString("hora") ?: ""
            val latitude = jsonObject.getDouble("latitude")
            val longitude = jsonObject.getDouble("longitude")
            val contacts = parseContactsFromJsonObject(jsonObject)
            fallList.add(DataFall(id, latitude, longitude, fecha, hora, contacts))
        }
        return fallList
    }
}
